/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.layout;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

/**
 * XXX Doc
 * <p>Detail ... </p>
 * @author Peransin Nicolas
 */
public class ZoomSutPane extends JPanel {

    /**
	 * Generated serialized version
	 */
	private static final long serialVersionUID = -3460158066343707875L;

	protected LayoutManager getSuperLayout() { return null; };

    public ZoomSutPane() {
//        super(getSuperLayout());
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected ZoomLayout lo = new ZoomLayout();
    protected JLabel lbl1 = new JLabel();
    protected ZoomConstraint lbl1Cst = new ZoomConstraint(10, 10, 50, 30);
    protected JLabel lbl2 = new JLabel();
    protected JLabel lbl3 = new JLabel();
    protected ZoomConstraint lbl3Cst = new ZoomConstraint(100, 100, 60, 60, 30);
    protected JPanel cadre = new JPanel();
    protected ZoomConstraint cadrePos = new ZoomConstraint(50, 400, 200, 60);

    protected JTabbedPane jTabbedPane1 = new JTabbedPane();
//    protected ZoomConstraint tabPos = new ZoomConstraint(300, 50, 200, 100);
        protected ZoomConstraint tabPos = new ZoomConstraint(300, 50);
//    protected JLabel jTabbedPane2 = new JLabel();


//    protected ZoomLayout nu = new ZoomLayout();
    protected JPanel moving = new JPanel();
    protected ZoomConstraint movCst = new ZoomConstraint(150, 400, 100, 60);

    private void jbInit() throws Exception {
        border1 = BorderFactory.createLineBorder(Color.blue,2);
        lo.setHeight(500);
        lo.setWidth(600);

        setLayout(lo);
        setOpaque(true);
        setBackground(Color.WHITE);

        lbl1.setText("text 1");
        lbl1.setBorder(BorderFactory.createLineBorder(Color.green));
        lbl1.setOpaque(true);
        lbl1.setBackground(Color.cyan);
//        nu.setHeight(800);
//        nu.setWidth(300);
        lbl3Cst.setY(200);
        lbl1Cst.setWidth(200);
        jTabbedPane1.setBackground(Color.white);
//        jTabbedPane1.setAlignmentY((float) 0.5);
        cadre.setBorder(border1);
        jLabel1.setForeground(Color.black);
        jLabel1.setRequestFocusEnabled(true);
        jLabel1.setText("t");
        jLabel2.setText("jLabel2");
        jLabel3.setText("jLabel3");
        jLabel4.setText("jLabel4");
//        add(lbl1, lbl1Cst);
        JCheckBox c = new JCheckBox("lbl");
        c.setEnabled(false);
        c.setOpaque(false);
        add(c, lbl1Cst);
        this.add(jTabbedPane1, tabPos);

//        JCheckBox c2 = new JCheckBox("lbl");
        ZoomLabel c2 = new ZoomLabel();
        c2.setOpaque(false);
//        c2.setSelected(true);
        c2.setEnabled(false);
        c2.setBorder(BorderFactory.createLineBorder(Color.CYAN, 1));
        add(c2, new ZoomConstraint(10, 30, 200, 30));

        jTabbedPane1.add(jLabel1, "Tab 1");
        jTabbedPane1.add(jLabel2, "jLabel2");
        jTabbedPane1.add(jPanel1, "jPanel1");
        jPanel1.add(jLabel3, null);
        jPanel1.add(jLabel4, null);


        lbl3Cst.setFont(30);
        lbl3Cst.setHeight(94);
        lbl3Cst.setWidth(94);
        lbl3Cst.setX(60);


        this.add(lbl3, lbl3Cst);

        Float.toString(0.0f);

        cadre.setOpaque(false);
//        cadre.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.BLUE));
        add(cadre, cadrePos);
        moving.setName("moving");
//        moving.setLayout(new ZoomLayout(100, 60));
        moving.setLayout(new ZoomLayout());
        moving.setOpaque(true);
        moving.setBackground(Color.GREEN);

        JPanel in1 = new JPanel();
        in1.setOpaque(true);
        in1.setBackground(new Color(0, 200, 0));
        moving.add(in1, new ZoomConstraint(20, 20, 20, 10));

        JPanel in2 = new JPanel();
        in2.setName("in2");
        in2.setOpaque(true);
        in2.setBackground(new Color(0, 100, 0));
        moving.add(in2, new ZoomConstraint(60, 20, 20, 20));

        add(moving, movCst);
//    protected ZoomConstraint movCst = new ZoomConstraint(150, 400, 100, 60);

        lbl3.setText("Z00M-30");
        lbl3.setOpaque(true);
        lbl3.setBackground(Color.red);

        lbl2.setText("null == FullContainer");
        lbl2.setHorizontalAlignment(SwingConstants.CENTER);
        lbl2.setBorder(BorderFactory.createLineBorder(Color.red));

        this.add(lbl2);

//        ColorComboBox cb = new ColorComboBox();

/*
        Object[] vals = { "a", "b", "c"};
        JComboBox cb = new JComboBox(vals);

        this.add(cb, lbl4Cst);
        System.out.println("\t\tSelected color " + cb.getSelectedItem().toString());
*/

    }

    protected int incr=2;
    protected Border border1;
    protected JLabel jLabel1 = new JLabel();
    protected JLabel jLabel2 = new JLabel();
    protected JPanel jPanel1 = new JPanel();
    protected JLabel jLabel3 = new JLabel();
    protected JLabel jLabel4 = new JLabel();
//    protected OrbConnect orbConnect1 = new OrbConnect();

    public void start() {
        try {
            new CardLayout();
            while (true) {

                if ( (movCst.bounds.width >= 100) || (movCst.bounds.width <= -100) ) {
                    incr = -incr;
                }
                movCst.bounds.width = movCst.bounds.width+incr;

                Thread.sleep(50);
                revalidate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*
        protected ZoomLayout lo = new ZoomLayout();
        protected JLabel lbl1 = new JLabel();
        protected ZoomConstraint lbl1Cst = new ZoomConstraint(10, 10, 50, 30);
        protected JLabel lbl2 = new JLabel();
        protected JLabel lbl3 = new JLabel();
        protected ZoomConstraint lbl3Cst = new ZoomConstraint(100, 100, 60, 60, 18);


        private void jbInit() throws Exception {
            lo.setHeight(400);
            lo.setWidth(200);
            setLayout(lo);

            lbl1.setText("Text1");
            lbl1.setBorder(BorderFactory.createLineBorder(Color.green));
            lbl1.setOpaque(true);
            lbl1.setBackground(Color.cyan);
            add(lbl1, lbl1Cst);

            lbl2.setText("ABC");
            add(lbl2);
            lbl2.setBorder(BorderFactory.createLineBorder(Color.red));


            lbl3.setText("Z000M");
            lbl3.setOpaque(true);
            lbl3.setBackground(Color.red);
            add(lbl3, lbl3Cst);

        }

     */

//    Object toto;

    public class ZoomLabel extends JLabel implements Zoomable {

    	/**
		 * Generated serialized version
		 */
		private static final long serialVersionUID = -8934608067455707695L;

		public void zoom(float x, float y) {
            setText("zoom " + x + ", " + y);
        }
    }

    public static void param(int[] o) {}

    public static void main0(String[] args) {

        param(new int[] {1, 2});

        String nb = ".23456789";
        String t = "";
        for (int ind=0; ind<10; ind++) {
            t = t + ind + nb;
        }

        for (int ind=0; ind<0; ind++)
         {
            System.out.println(t);
//        System.out.println("It took " + (System.currentTimeMillis() - start) );
        }

//        EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                System.out.println("In Event queue");
//            }
//        });

        String src = "my $$tring";
        String res = src.replaceAll("[$][$]", "S");
        System.out.println("Result : " + res);


//
//        try {
//            Thread.sleep(500);
//        } catch (Exception e) {}
//
//        System.out.println("Starting SubThread");
//        Thread tr = new Thread(new Runnable() {
//            public void run() {
//                try {
//                    Thread.sleep(10000);
//                    System.out.println("SubThread over");
//                } catch (InterruptedException ex) {
//                    System.out.println("SubThread interrupted");
//                }
//            }
//        });
//
//        tr.start();
//
//        try {
//            tr.join(500);
//        } catch (InterruptedException ex) {
//            System.out.println("Join stopped");
//        }
//        if (tr.isAlive())
//            tr.stop();
//        System.out.println("Join over");


     }


    public static void main(String[] args) {

        String src = "MyVar$$_IN";
        System.out.println("Replace xxx : <" + src.replaceAll("\\$\\$", "xxx") + "> [" + src + "]");


        JFrame f = new JFrame("Dir < 0");
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { System.exit(0); }
        }); // Gestion du bouton close


//Math.floor(0.0);
        ZoomSutPane t = new ZoomSutPane();
        f.getContentPane().add(t, BorderLayout.CENTER);


        JPanel upper = new JPanel();

        final JComboBox cb = new JComboBox(new String[] {"0", "1", "2", "4"});
        cb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("ComboBox select : " + cb.getSelectedItem());
            }
        });
        upper.add(cb);


        JButton b = new JButton("Ok");
//        b.imageUpdate(null, 0, 0, 0, 0, 0);
        b.addMouseListener(new BorderWritter(b));
        b.paintComponents(null);

        upper.add(b);
        b = new JButton("Normal");
//        b.addMouseListener(new BorderWritter(b));
        upper.add(b);
        f.getContentPane().add(upper, BorderLayout.NORTH);

        f.pack();
        f.setVisible(true);
        t.start();
    }


    public static class BorderWritter implements java.awt.event.MouseListener {
        JButton b;
        boolean in = false;
        Border sensitivePressed = null;

        Border unSensitive = null;
        Border sensitive = null;
        JComponent j;

        public BorderWritter(JButton b) {
            this.b = b;
            sensitivePressed = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
//            sensitivePressed = new javax.swing.plaf.BorderUIResource.BevelBorderUIResource(BevelBorder.LOWERED);
//            sensitive = BorderFactory.createBevelBorder(BevelBorder.RAISED);
            sensitive = new javax.swing.plaf.BorderUIResource.BevelBorderUIResource(BevelBorder.RAISED);
            Insets inset = sensitive.getBorderInsets(b);
            unSensitive = BorderFactory.createEmptyBorder(inset.top, inset.left, inset.bottom, inset.right);
            if (b.getBorder() instanceof CompoundBorder) {
                CompoundBorder bd = (CompoundBorder) b.getBorder();
                Border in = bd.getInsideBorder();
                sensitivePressed = BorderFactory.createCompoundBorder(sensitivePressed, in);
                sensitive = BorderFactory.createCompoundBorder(sensitive, in);
                unSensitive = BorderFactory.createCompoundBorder(unSensitive, in);
            }
            b.setBorder(unSensitive);
//            b.setRolloverEnabled(true);

        }

        public void mouseClicked(java.awt.event.MouseEvent e) {}


        public void mouseEntered(java.awt.event.MouseEvent e) {
            in = true;
            if (b.isEnabled()) {
                if (b.getModel().isPressed()) {
                    b.setBorder(sensitivePressed);
                } else if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == 0) {
                    b.setBorder(sensitive);
                }
            }
        }

        public void mouseExited(java.awt.event.MouseEvent e) {
//            System.out.print("!");
//            System.out.flush();
            in = false;
//            if (b.getModel().isPressed())
//                b.setBorder(sensitivePressed);
//            else
                b.setBorder(unSensitive);
        }

        public void mousePressed(java.awt.event.MouseEvent e) {
                b.setBorder(sensitivePressed);
        }

        public void mouseReleased(java.awt.event.MouseEvent e) {
            if (in && b.isEnabled()) {
                b.setBorder(sensitive);
            } else {
                b.setBorder(unSensitive);
            }
        }
    }






} // endClass ZoomTest
