/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.TableCellRenderer;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 */
public class ButtonTableRenderer extends JPanel implements TableCellRenderer {


    JButton editor = new JButton("...");
    TableCellRenderer defaultRenderer;



    protected boolean editorAlwaysShown;

    public ButtonTableRenderer() {
        this(null);
    }

    public ButtonTableRenderer(TableCellRenderer dRenderer) {
        this(dRenderer, true);
    }

    public JButton getButton() {
        return editor;
    }

    public ButtonTableRenderer(TableCellRenderer dRenderer, boolean alwaysShown) {
        super(new BorderLayout());

        defaultRenderer = dRenderer;
        if (isButtonOnly()) { // In fine we has 3 component in the tool bar
            editorAlwaysShown = true;
        } else {
            editorAlwaysShown = alwaysShown;
            Insets buttonMarge = editor.getMargin();
            buttonMarge.left = buttonMarge.top;
            buttonMarge.right = buttonMarge.top;
            editor.setMargin(buttonMarge);
            add(editor, BorderLayout.LINE_END);
        }
    }


    public void activate(JTable table) {
        for (MouseListener l : table.getMouseListeners()) {
            if (l == this)
                return; // already activated
        }
        
        table.addMouseListener(inputListener);
        table.addMouseMotionListener(inputListener);
    }


    protected boolean isButtonOnly() {
        return defaultRenderer == null;
    }


    Cell drawn = new Cell(); // the last drawn cell
    static final int DEFAULT_RENDERER_INDEX = 1;
    public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

        // We could use value if value is button !!!
        drawn.update(table, row, column);
        boolean drawPointed = drawn.equals(pointed);

        editor.setFont(table.getFont());
        editor.setEnabled(isButtonEnable(table, row, column));

        if (!editorAlwaysShown) {
            editor.setVisible(table.getSelectionModel().isSelectedIndex(row));
            /*
            if (hit.isValid()) { // We are in a click
                editor.setVisible(drawn.equals(hit));
            } else {
                // We should balance the visibility with selection policy
                //   Here we only care for row selection

            }
             */
        }
        ButtonModel bm = editor.getModel();
        if (hit.isValid()) { // We are in a click
            bm.setRollover(false);
            bm.setArmed(drawPointed && isOnButton && hit.equals(pointed));
            bm.setPressed(drawn.equals(hit));
        } else {
            bm.setRollover(drawPointed && isOnButton);
            bm.setPressed(false);
            bm.setArmed(false);
        }

        if (!isButtonOnly()) {
            if (getComponentCount()>DEFAULT_RENDERER_INDEX)
                remove(DEFAULT_RENDERER_INDEX);
            Component comp = defaultRenderer.getTableCellRendererComponent(table, 
                        value, isSelected, hasFocus, row, column);

            add(comp, BorderLayout.CENTER, DEFAULT_RENDERER_INDEX);
            return this;
        } else {
            return editor;
        }
    }

    /**
     * Aimed to be redefined.
     * @param source
     * @param row
     * @param col
     * @return
     */
    public boolean isButtonEnable(JTable source, int row, int col) {
        return true;
    }

    /*abstract*/ public void editCell(JTable source, int row, int col) {
        // TODO Call a JDialog
        JOptionPane.showMessageDialog(source, "Edition of " + row, 
                    "Edit value", JOptionPane.WARNING_MESSAGE);
    }

    Cell lastPointed = new Cell(); 
    Cell pointed = new Cell();
    Cell hit = new Cell();

    boolean wasOnButton = false; // Concern only pointed
    boolean isOnButton = false;  

    boolean computeOnButton(JTable t) {
        if (isButtonOnly())
            return true;

        // We should ensure (drawn == pointed)
        // ERROR !!!
        
        Rectangle cellRect = t.getCellRect(pointed.row, pointed.column, false);
        pointed.point.translate(-cellRect.x, -cellRect.y);
        return editor.getBounds().contains(pointed.point);
    }
    

    protected void redrawCell(Cell cell) {
        if (cell.isValid()) {
            cell.table.repaint(cell.table.getCellRect(cell.row, cell.column, true));
        }
    }

    
    
    MouseInputAdapter inputListener  = new MouseInputAdapter() {
        public void mouseExited(MouseEvent e) {
            if (lastPointed.table == e.getSource()) {
                pointed.reset();
                redrawCell(lastPointed);
                lastPointed.reset();
            }
        }
    
        public void mousePressed(MouseEvent e) {
            if (!checkCellOnRenderer(e, hit)) {
                // Pressed is not in the renderer: hit is not valid 
                return;
            }
            if (pointed.equals(hit) && (wasOnButton || isButtonOnly())) {
                // draw pointed
                redrawCell(pointed);
            } else {
                hit.reset();
            }
        }
        public void mouseReleased(MouseEvent e) {
            if (hit.isValid()) {
                checkCellOnRenderer(e, pointed);
                if (hit.equals(pointed)) {
                    isOnButton = computeOnButton((JTable) e.getSource());
                    if (isOnButton && isButtonEnable(hit.table, hit.row, hit.column)) {
                        editCell(hit.table, hit.row, hit.column);
                    }
                }
                wasOnButton = false;
                redrawCell(hit);
                hit.reset();
                lastPointed.reset();
                mouseMoved(e);
            }
        }
        public void mouseDragged(MouseEvent e) {
            if (!hit.isValid()) // not armed
                return;
            if (!checkCellOnRenderer(e, pointed) || !pointed.equals(hit)) {
                if (lastPointed.equals(hit)) {
                    redrawCell(hit);
                }
                lastPointed.update(pointed);
                return;
            }
    
            // We are on renderer and (hit == pointed)
            isOnButton = computeOnButton((JTable) e.getSource());
            if (!lastPointed.equals(hit) || (isOnButton != wasOnButton)) {
                redrawCell(pointed);
            }
            wasOnButton = isOnButton;
            lastPointed.update(pointed);
        }
    
    
    
    
    
        public void mouseMoved(MouseEvent e) {
            if (!checkCellOnRenderer(e, pointed)) {
                redrawCell(lastPointed);
                lastPointed.reset();
                return;
            }
    
            boolean redrawPointed = false;
            if (!lastPointed.equals(pointed)) {
                wasOnButton = false;
                redrawCell(lastPointed);
                redrawPointed = true;
            }
    
            isOnButton = computeOnButton((JTable) e.getSource());
            if (redrawPointed || (isOnButton != wasOnButton)) {
                redrawCell(pointed);
            }
            lastPointed.update(pointed);
            wasOnButton = isOnButton;
        }
    };


    protected boolean checkCellOnRenderer(MouseEvent e, Cell c) {
        JTable source = (JTable) e.getSource();
        c.point = e.getPoint();
        int hitColumn = source.columnAtPoint(c.point);
        int hitRow = source.rowAtPoint(c.point);

        if (source.getCellRenderer(hitRow, hitColumn) != this) {
            c.reset();
            return false;
        }
        c.table = source;
        c.row = hitRow;
        c.column = hitColumn;
        return true;
    }

    static class Cell {
        JTable table = null;
        int row = -1;
        int column = -1;
        Point point = null; // some cache : not meant to last

        void update(Cell c) {
            table = c.table;
            row = c.row;
            column = c.column;
        }

        void update(JTable t, int r, int c) {
            table = t;
            row = r;
            column = c;
        }

        void reset() {
            update(null, -1, -1);
        }

        boolean isValid() {
            return (table != null) && (row != -1) && (column != -1);
        }

        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Cell))
                return false;
            Cell other = (Cell) o;
            return (table == other.table) 
            && (row == other.row) 
            && (column == other.column);
        }
    }

}


