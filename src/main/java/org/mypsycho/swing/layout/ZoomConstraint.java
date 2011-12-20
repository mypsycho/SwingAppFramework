/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.layout;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**
 * <p>Titre : </p>
 * <p>Description : </p>
 * <p>Copyright : Copyright (c) 2003</p>
 * <p>Société : </p>
 * @author PERANSIN Nicolas
 * @version 1.0
 */
public class ZoomConstraint implements Serializable {

    /**
	 * Generated serialized version
	 */
	private static final long serialVersionUID = 5478805195452655932L;

	public static final int NOT_DEFINED = Integer.MIN_VALUE;

    public Rectangle bounds = new Rectangle(0, 0, NOT_DEFINED, NOT_DEFINED);

    protected int font = -1;
//    protected float fontF = 10.0f;
//    protected int thickness = -1;



    // Seulement un zoom de taille
    public ZoomConstraint() {}

    public ZoomConstraint(int x, int y) {
        bounds.x = x;
        bounds.y = y;
    }

    public ZoomConstraint(Rectangle r) {
        bounds = r;
    }

    public ZoomConstraint(int x, int y, int w, int h) {
        bounds.x = x;
        bounds.y = y;
        bounds.width = w;
        bounds.height = h;
    }

    // Zoom de font
    public ZoomConstraint(Rectangle r, int f) {
        bounds = r;
        font = f;
    }

    public ZoomConstraint(int x, int y, int w, int h, int f) {
        bounds.x = x;
        bounds.y = y;
        bounds.width = w;
        bounds.height = h;
        font = f;
    }

//    public ZoomConstraint(Rectangle r, float f) {
//        bounds = r;
//        fontF = f;
//    }
//
//    public ZoomConstraint(int x, int y, int w, int h, float f) {
//        bounds.x = x;
//        bounds.y = y;
//        bounds.width = w;
//        bounds.height = h;
//
//        fontF = f;
//    }
//
//    // Zoom de font et thickness
//    public ZoomConstraint(Rectangle r, int f, int t) {
//        bounds = r;
//        font = f;
//        thickness = t;
//    }
//
//    public ZoomConstraint(int x, int y, int w, int h, int f, int t) {
//        bounds.x = x;
//        bounds.y = y;
//        bounds.width = w;
//        bounds.height = h;
//
//        font = f;
//        thickness = t;
//    }
//
//    public ZoomConstraint(Rectangle r, float f, int t) {
//        bounds = r;
//        fontF = f;
//        thickness = t;
//    }
//
//    public ZoomConstraint(int x, int y, int w, int h, float f, int t) {
//        bounds.x = x;
//        bounds.y = y;
//        bounds.width = w;
//        bounds.height = h;
//
//        fontF = f;
//        thickness = t;
//    }


    // Setters and Getters
    //   Rectangle
    public Rectangle getBounds() { return bounds; }
    public void setBounds(Rectangle bounds) { this.bounds = bounds; }

    public int getX() { return bounds.x; }
    public void setX(int x) { this.bounds.x = x; }

    public int getY() { return bounds.y; }
    public void setY(int y) { this.bounds.y = y; }

    public int getWidth() { return bounds.width; }
    public void setWidth(int w) { this.bounds.width = w; }

    public int getHeight() { return bounds.height; }
    public void setHeight(int h) { this.bounds.height = h; }

    //   Optional attributs
    public void setFont(int font) { this.font = font; }
    public int getFont() { return font; }
//    public int getThickness() { return thickness; }
//    public void setThickness(int thick) { this.thickness = thick; }
//    public void setFontF(float fontf) { this.fontF = fontf; }
//    public float getFontF() { return fontF; }
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
    }



} // endClass ZoomConstraint