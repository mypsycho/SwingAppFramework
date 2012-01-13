package com.mypsycho.util;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import org.mypsycho.util.CompressUtil;
import org.mypsycho.util.ZipEntryTreeModel;


public class CompressUtilUi {
   
    
    
    public static void main(String[] args) {
        try {
            final File zip = new File(args[2]);
            File f = new File(args[1]);
            if ("C".equals(args[0])) {
                CompressUtil.compressDirectory(zip, f);
                System.out.println("Compressed result " + zip.length());

            } else if ("V".equals(args[0]) || "S".equals(args[0])) {
                JFrame frame;
                // new JTree().isEditable()
                
                final ZipEntryTreeModel model;
                if ("V".equals(args[0])) {
                    frame = new JFrame("Show zip " + f.getPath());
                    model = new ZipEntryTreeModel(f);
                } else {
                    frame = new JFrame("Show zip description " + f.getPath());
                    Properties coded = new Properties();
                    InputStream codedStorage = new FileInputStream(f);
                    try {
                        coded.load(codedStorage);
                    } finally {
                        codedStorage.close();
                    }
                    model = new ZipEntryTreeModel(coded);
                }
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                JTree tree = new JTree(model) {
                    public String convertValueToText(Object value, boolean selected,
                                boolean expanded, boolean leaf, int row, boolean hasFocus) {
                        if (value instanceof ZipEntry) {
                            if (((ZipEntry) value).isDirectory())
                                return CompressUtil.getEntryShortName((ZipEntry) value);
                            return CompressUtil.getEntryShortName((ZipEntry) value) 
                                    + " (" + ((ZipEntry) value).getSize() + ")";
                        } else if (value instanceof File) { // root
                                return ((File) value).getPath();
                        } else {
                            return super.convertValueToText(value, selected,
                                expanded, leaf, row, hasFocus);
                        }
                    }
                };
                tree.setRootVisible(false);
                
                // System.out.println("tree editable : " + tree.isEditable());
                frame.getContentPane().add(new JScrollPane(tree), BorderLayout.CENTER);
                JButton code = new JButton("encode");
                code.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            model.encode().store(new FileOutputStream(zip), null);
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                });
                code.setEnabled("V".equals(args[0]));
                frame.getContentPane().add(code, BorderLayout.PAGE_END);
                
                frame.pack();
                frame.setVisible(true);
                
            } else if ("U".equals(args[0])) {
                System.out.println("Compressed src " + zip.length());
                CompressUtil.uncompressDirectory(zip, f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 
    
}
