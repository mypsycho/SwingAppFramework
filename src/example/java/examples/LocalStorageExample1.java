
/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */ 

package examples;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.LocalStorage;
import org.jdesktop.application.ResourceMap;
import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import javax.swing.AbstractListModel;
import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;


/**
 * A simple demonstration of the {@code LocalStorage} class: loads and saves
 * a {@code LinkedHashMap} (a {@code HashMap} whose entries have a stable order).
 * To try it, add some entries to the Map by pressing the "Add Random Entry"
 * button or by entering key/value strings in the corresponding text fields.
 * Then save the Map (to a file), clear, and load the saved Map.
 * with the corresponding buttons.
 * <p>
 * The map is saved with the {@code LocalStorage} 
 * {@link LocalStorage#save save} method like this:
 * <pre>
 * LinkedHashMap&lt;String, String&gt; map = listModel.getMap();
 * ApplicationContext.getInstance().getLocalStorage().save(map, "map.xml");
 * </pre>
 * And loaded with the {@code LocalStorage} 
 * {@link LocalStorage#load load} method like this:
 * <pre>
 * Object map = ApplicationContext.getInstance().getLocalStorage().load("map.xml");
 * listModel.setMap((LinkedHashMap&lt;String, String&gt;)map);
 * </pre>
 * The {@code LocalStorage.save/load} methods can be applied to 
 * anything supported by Java Beans Persistence, i.e. any Java Bean
 * as well as most of the primitive and utility Java classes.
 * The {@code LocalStorage.save} method is implemented
 * with the {@link java.beans.XMLEncoder XMLEncoder} class
 * and the {@code LocalStorage.load} method with 
 * the {@link java.beans.XMLDecoder XMLDecoder} class.
 * Take a look at the contents of {@code "map.xml"} by cut and pasting the
 * complete pathname from the bottom of the GUI into your favorite
 * text editor.
 * 
 * @see ApplicationContext#getLocalStorage
 * @see LocalStorage#load
 * @see LocalStorage#save
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class LocalStorageExample1 extends Application {
    private final static String file = "map.xml";
    private final Random random = new Random();
    private JTextField keyField = null;
    private JTextField valueField = null;
    private JTextField messageField = null;
    private MapListModel listModel = null;

    @Action public void addKeyValueEntry() {
	String key = keyField.getText().trim();
	String value = valueField.getText().trim();
	listModel.put(key, value);
    }

    @Action public void addRandomEntry() {
	String key = keyField.getText().trim() + random.nextInt(10000);
	String value = valueField.getText().trim() + random.nextInt(10000);
	listModel.put(key, value);
    }

    @Action public void clearMap() {
	listModel.clear();
    }

    @Action public void loadMap() throws IOException {
	Object map = getContext().getLocalStorage().load(file);	
	listModel.setMap((LinkedHashMap<String, String>)map);
	showFileMessage("loadedFile", file);
    }

    @Action public void saveMap() throws IOException {
	LinkedHashMap<String, String> map = listModel.getMap();
	getContext().getLocalStorage().save(map, file);
	showFileMessage("savedFile", file);
    }

    private void showFileMessage(String messageKey, String file) {
	File dir = getContext().getLocalStorage().getDirectory();
	File path = (dir == null) ? new File(file) : new File(dir, file);
	ResourceMap resourceMap = getContext().getResourceMap();
	String message = resourceMap.getString(messageKey, path.toString());
	messageField.setText(message);
    }

    @Override protected void startup() {
	keyField = new JTextField(" Key ", 16);
	valueField = new JTextField(" Value ", 16);
	messageField = new JTextField();
	messageField.setEditable(false);
	JButton addEntryButton = new JButton();
	JButton saveButton = new JButton();
	JButton loadButton = new JButton();
	JButton clearButton = new JButton();

	/* Create JScrollPane/JList that displays listModel
	 */
	listModel = new MapListModel();
	JList mapList = new JList(listModel);
	mapList.setPrototypeCellValue("Hello = World");
        mapList.setVisibleRowCount(12);
	Border border = new EmptyBorder(2, 4, 2, 4);
	JScrollPane scrollPane = new JScrollPane(mapList);
	scrollPane.setBorder(border);

	/* Lookup up the Actions for this class/object in the
	 * ApplicationContext, and bind them to the GUI controls.
	 */
	ActionMap actionMap = getContext().getActionMap();
	addEntryButton.setAction(actionMap.get("addRandomEntry"));
	keyField.setAction(actionMap.get("addKeyValueEntry"));
	valueField.setAction(actionMap.get("addKeyValueEntry"));
	saveButton.setAction(actionMap.get("saveMap"));
	loadButton.setAction(actionMap.get("loadMap"));
	clearButton.setAction(actionMap.get("clearMap"));

	JPanel northPanel = new JPanel();
	northPanel.add(keyField);
	northPanel.add(valueField);
	northPanel.add(addEntryButton);

	JPanel buttonPanel = new JPanel();
	buttonPanel.add(saveButton);
	buttonPanel.add(loadButton);
	buttonPanel.add(clearButton);

	JPanel centerPanel = new JPanel(new BorderLayout());
	centerPanel.add(scrollPane, BorderLayout.CENTER);
	centerPanel.add(buttonPanel, BorderLayout.SOUTH);

	JFrame appFrame = new JFrame(getClass().getSimpleName());
	appFrame.add(centerPanel, BorderLayout.CENTER);
	appFrame.add(northPanel, BorderLayout.NORTH);
	appFrame.add(messageField, BorderLayout.SOUTH);
	appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	appFrame.pack();
	appFrame.setLocationRelativeTo(null);
	appFrame.setVisible(true);
    }


    /* A ListModel that encapsulates a LinkedHashMap<String, String>
     * The value of each ListModel element is just a string: 
     * "key = value".
     */
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
	    }
	    else {
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
        Application.launch(LocalStorageExample1.class, args);
    }
}
