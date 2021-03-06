/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. 
 * Copyright (C) 2011 Peransin Nicolas. All rights reserved. 
 * Use is subject to license terms.
 */
package examples;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.mypsycho.swing.app.Application;
import org.mypsycho.swing.app.ApplicationContext;
import org.mypsycho.swing.app.ApplicationListener;
import org.mypsycho.swing.app.session.LocalStorage;
import org.mypsycho.swing.app.utils.SwingHelper;
import org.mypsycho.text.TextMap;


/**
 * A simple demonstration of the {@code LocalStorage} class: loads and saves
 * a {@code LinkedHashMap} (a {@code HashMap} whose entries have a stable order).
 * To try it, add some entries to the Map by pressing the "Add Random Entry"
 * button or by entering key/value strings in the corresponding text fields.
 * Then save the Map (to a file), clear, and load the saved Map.
 * with the corresponding buttons.
 * <p>
 * The map is saved with the {@code LocalStorage} {@link LocalStorage#save save} method like this:
 * 
 * <pre>
 * LinkedHashMap&lt;String, String&gt; map = listModel.getMap();
 * ApplicationContext.getInstance().getLocalStorage().save(map, &quot;map.xml&quot;);
 * </pre>
 * 
 * And loaded with the {@code LocalStorage} {@link LocalStorage#load load} method like this:
 * 
 * <pre>
 * Object map = ApplicationContext.getInstance().getLocalStorage().load(&quot;map.xml&quot;);
 * listModel.setMap((LinkedHashMap&lt;String, String&gt;) map);
 * </pre>
 * 
 * The {@code LocalStorage.save/load} methods can be applied to anything supported by Java Beans
 * Persistence, i.e. any Java Bean as well as most of the primitive and utility Java classes. The
 * {@code LocalStorage.save} method is implemented with the {@link java.beans.XMLEncoder XMLEncoder}
 * class and the {@code LocalStorage.load} method with the {@link java.beans.XMLDecoder XMLDecoder}
 * class. Take a look at the contents of {@code "map.xml"} by cut and pasting the complete pathname
 * from the bottom of the GUI into your favorite text editor.
 * 
 * @see ApplicationContext#getLocalStorage
 * @see LocalStorage#load
 * @see LocalStorage#save
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class LocalStorageExample1 extends Application {

    private final static String file = "map.xml";
    private final Random random = new Random();
    private JTextField keyField = new JTextField(" Key ", 16);
    private JTextField valueField = new JTextField(" Value ", 16);
    private JTextField messageField = new JTextField();
    private MapListModel listModel = null;
    private TextMap texts = new TextMap(this);
    
    public void addKeyValueEntry() {
        String key = keyField.getText().trim();
        String value = valueField.getText().trim();
        listModel.put(key, value);
    }

    public void addRandomEntry() {
        String key = keyField.getText().trim() + random.nextInt(10000);
        String value = valueField.getText().trim() + random.nextInt(10000);
        listModel.put(key, value);
    }

    public void clearMap() {
        listModel.clear();
    }

    public void loadMap() throws IOException {
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, String> map = (LinkedHashMap<String, String>) 
                getContext().getLocalStorage().load(file);
        listModel.setMap(map);
        showFileMessage("loadedFile", file);
    }

    public void saveMap() throws IOException {
        LinkedHashMap<String, String> map = listModel.getMap();
        getContext().getLocalStorage().save(map, file);
        showFileMessage("savedFile", file);
    }

    private void showFileMessage(String messageKey, String file) {
        File dir = getContext().getLocalStorage().getDirectory();
        File path = (dir == null) ? new File(file) : new File(dir, file);
        messageField.setText(texts.get(messageKey, path));
    }

    @Override
    protected void startup() {

        messageField.setEditable(false);


        /*
         * Create JScrollPane/JList that displays listModel
         */
        listModel = new MapListModel();
        JList mapList = new JList(listModel);
        mapList.setPrototypeCellValue("Hello = World");
        mapList.setVisibleRowCount(12);


        /*
         * Lookup up the Actions for this class/object in the
         * ApplicationContext, and bind them to the GUI controls.
         * --> Look at LocalStorageExample1.properties
         */
        JFrame appFrame = new JFrame();
        SwingHelper h = new SwingHelper("f", appFrame);
        h.with("fields", new FlowLayout(), BorderLayout.PAGE_START)
            .add("key", keyField)
            .add("value", valueField)
            .add("add", new JButton())
            .back();
        h.with("center", new BorderLayout(), BorderLayout.CENTER)
            .add("mapList", new JScrollPane(mapList), BorderLayout.CENTER)
            .with("buttons", new FlowLayout(), BorderLayout.PAGE_END)
                .add("save", new JButton())
                .add("load", new JButton())
                .add("clear", new JButton())
                .back()
            .back();
        h.add("message", messageField, BorderLayout.PAGE_END);

        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        show(appFrame);
    }

    /*
     * A ListModel that encapsulates a LinkedHashMap<String, String>
     * The value of each ListModel element is just a string:
     * "key = value".
     */
    @SuppressWarnings("serial")
    private static class MapListModel extends AbstractListModel {

        private final LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        private List<String> keys = null;

        private List<String> getKeys() {
            if (keys == null) {
                keys = new ArrayList<String>(map.keySet());
            }
            return keys;
        }

        public void put(String key, String value) {
            int index = -1;
            if (map.containsKey(key)) {
                index = getKeys().indexOf(key);
            } else {
                index = map.size();
                keys = null;
            }
            map.put(key, value);
            fireContentsChanged(this, index, index);
        }

        public void clear() {
            if (map.size() > 0) {
                int lastIndex = map.size() - 1;
                map.clear();
                keys = null;
                fireIntervalRemoved(this, 0, lastIndex);
            }
        }

        public LinkedHashMap<String, String> getMap() {
            return new LinkedHashMap<String, String>(map);
        }

        public void setMap(LinkedHashMap<String, String> newMap) {
            int oldLastIndex = Math.max(map.size() - 1, 0);
            map.clear();
            map.putAll(newMap);
            int newLastIndex = Math.max(map.size() - 1, 0);
            fireContentsChanged(this, 0, Math.max(oldLastIndex, newLastIndex));
        }

        public int getSize() {
            return map.size();
        }

        public Object getElementAt(int index) {
            String key = getKeys().get(index);
            return key + " = " + map.get(key);
        }
    }

    public static void main(String[] args) {
        Application app = new LocalStorageExample1();
        app.addApplicationListener(ApplicationListener.console);
        app.launch(args);
    }

    
    /**
     * Returns the texts.
     *
     * @return the texts
     */
    public TextMap getTexts() {
        return texts;
    }
}
