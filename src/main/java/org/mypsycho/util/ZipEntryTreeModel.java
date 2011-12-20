/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * XXX Doc
 * <p>Detail ... </p>
 * @author Peransin Nicolas
 */
public class ZipEntryTreeModel implements TreeModel {
    
    public static final String ROOT_TOKEN = "root";
    public static final String ROOT_ID = "-1";
    public static final String PARENT_TOKEN = "parent.";
    public static final String ENTRY_TOKEN = "entry.";
    public static final String SIZE_TOKEN = "size."; // long,  null <=> dir
    
    final File source;
    // Key may be null
    Map<String, ZipEntry[]> childrenByDirname = new HashMap<String, ZipEntry[]>();
    static final String ROOT_PATH = "";
    

    
    public ZipEntryTreeModel(File zipSource) throws IOException {
        source = zipSource;
        
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipSource));
        Map<String, List<ZipEntry>> buildMap = new HashMap<String, List<ZipEntry>>();
        try {
            for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                String name = entry.getName();
                String path = ROOT_PATH;
                
                int start = 0;
                // We build ZipEntry for each directory
                for (int index = name.indexOf(CompressUtil.ZIP_FILE_SEPARATOR, start); 
                            index != -1; 
                            index = name.indexOf(CompressUtil.ZIP_FILE_SEPARATOR, start)) {
                    List<ZipEntry> children = buildMap.get(path);
                    if (children == null) {
                        children = new ArrayList<ZipEntry>();
                        buildMap.put(path, children);
                    }
                    String subPathName = name.substring(start, index);
                    path = path + subPathName + CompressUtil.ZIP_FILE_SEPARATOR;
                    
                    boolean exist = false;
                    for (int iChild=0; iChild<children.size() && !exist; iChild++) {
                        exist = path.equals(((ZipEntry) children.get(iChild)).getName());
                    }
                    if (!exist) {
                        children.add(new ZipEntry(path));
                    }
                    start = path.length();
                }
                
                if (entry.isDirectory()) // <=> name.endsWith(ZIP_FILE_SEPARATOR)
                    continue; // already added !!!

                List<ZipEntry> children = buildMap.get(path);
                if (children == null) {
                    children = new ArrayList<ZipEntry>();
                    buildMap.put(path, children);
                }
                children.add(entry);
            }
        } finally {
            zis.close();
        }
        
        optimizeChildren(buildMap);
    }

    public ZipEntryTreeModel(Properties coded) {
        this(coded, "");
    }

    public ZipEntryTreeModel(Properties coded, String prefix) {
        source = new File(coded.getProperty(prefix + ROOT_TOKEN));
        
        int id = 0;
        Map<String, List<ZipEntry>> buildMap = new HashMap<String, List<ZipEntry>>();
        for (String name = coded.getProperty(prefix + ENTRY_TOKEN + id); name != null;
                name = coded.getProperty(prefix + ENTRY_TOKEN + id)) {
            ZipEntry entry = new ZipEntry(name);
            if (coded.getProperty(prefix + SIZE_TOKEN + id) != null) {
                entry.setSize(Long.parseLong(coded.getProperty(prefix + SIZE_TOKEN + id)));
            }
            
            String parentCode = coded.getProperty(prefix + PARENT_TOKEN + id);
            String path = ROOT_PATH;
            if (!ROOT_ID.equals(parentCode)) {
                path = coded.getProperty(prefix + ENTRY_TOKEN + parentCode);
            }
            
            List<ZipEntry> children = buildMap.get(path);
            if (children == null) {
                children = new ArrayList<ZipEntry>();
                buildMap.put(path, children);
            }
            children.add(entry);
            id++;
        } 

        optimizeChildren(buildMap);
    }

    private void optimizeChildren(Map<String, List<ZipEntry>> build) {
        // We trim the list to array => Save space
        for (String path : build.keySet()) {
            List<ZipEntry> childList = build.get(path);
            ZipEntry[] children = new ZipEntry[childList.size()];
            childList.toArray(children);
            Arrays.sort(children, ZIP_ENTRY_COMPARATOR);
            childrenByDirname.put(path, children);
        }  
    }
    
    public Properties encode() {
        return encode("");
    }
    
    public Properties encode(String prefix) {
        Properties result = new Properties();
        result.put(prefix + ROOT_TOKEN, source.getPath());
        
        int nextId = 0;
        ZipEntry[] children = getChildren(source);
        if (children!=null) {
            for (int iChild=0; iChild<children.length; iChild++) {
                result.put(prefix + PARENT_TOKEN + nextId, ROOT_ID);
                nextId = encode(children[iChild], result, nextId, prefix);
            }
        }
        
        return result;
    }

    public int encode(ZipEntry node, Properties store, int id, String prefix) {
        store.put(prefix + ENTRY_TOKEN + id, node.getName());
        
        int nextId = id+1;
        // Note : file <=> length not null
        if (node.isDirectory()) {
            // null when no child
            ZipEntry[] children = getChildren(node);
            if (children!=null) {
                for (int iChild=0; iChild<children.length; iChild++) {
                    store.put(prefix + PARENT_TOKEN + nextId, Integer.toString(id));
                    nextId = encode(children[iChild], store, nextId, prefix);
                }
            }
        } else {
            store.put(prefix + SIZE_TOKEN + id, Long.toString(node.getSize()));
        }
        return nextId;
    }
    
    /**
     * Note :
     * Multi-thread safe
     */
    static final Comparator<ZipEntry> ZIP_ENTRY_COMPARATOR = new Comparator<ZipEntry>() {
        
        public int compare(ZipEntry o1, ZipEntry o2) {
           
            // Directories are before files
            int t1 = o1.isDirectory() ? 0 : 1; 
            int t2 = o2.isDirectory() ? 0 : 1;
            if (t1 != t2)
                return t1 - t2;

            // Ignore case ? => Need to know if system is case-sensitive
            return CompressUtil.getEntryShortName(o1).compareToIgnoreCase(
                        CompressUtil.getEntryShortName(o2)); 
        }
    };
 
    
    ZipEntry[] getChildren(Object parent) {
        String path = source.equals(parent) ? ROOT_PATH  
                    : ((ZipEntry) parent).getName();
        return (ZipEntry[]) childrenByDirname.get(path);
    }

    public Object getChild(Object parent, int index) {
        ZipEntry[] children = getChildren(parent);
        return children[index];
    }

    public int getChildCount(Object parent) {
        ZipEntry[] children = getChildren(parent);
        return (children != null) ? children.length : 0;
    }

    public int getIndexOfChild(Object parent, Object child) {
        ZipEntry[] children = getChildren(parent);
        for (int iChild=0; iChild<children.length; iChild++) {
            if (children[iChild] == child)
                return iChild;
        }
        return -1;
    }
    
    public Object getRoot() {
        return source;
    }

    public boolean isLeaf(Object node) {
        if (getRoot().equals(node))
            return false;
        return !((ZipEntry) node).isDirectory();
    }

    // Not modifiable => no need to have listeners
    public void addTreeModelListener(TreeModelListener l) {}
    public void removeTreeModelListener(TreeModelListener l) {}
    public void valueForPathChanged(TreePath path, Object newValue) {}
}