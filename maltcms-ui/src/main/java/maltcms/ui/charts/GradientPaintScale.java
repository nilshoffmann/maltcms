/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code
 * under. Certain files or entire directories may not be covered by this
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package maltcms.ui.charts;

import cross.IConfigurable;
import java.awt.Color;
import java.awt.Component;
import java.awt.Paint;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Arrays;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import lombok.extern.slf4j.Slf4j;
import maltcms.tools.ImageTools;
import org.apache.commons.configuration.Configuration;
import org.jfree.chart.renderer.PaintScale;

/**
 * <p>GradientPaintScale class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@Slf4j
public class GradientPaintScale implements PaintScale, IConfigurable,
        Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1121734275349109897L;
    private int[][] ramp = null;
    private double[] sampleTable = null;
    private double[] breakPoints = null;
    private double[] st = null;
    private double min = 0.0d;
    private double max = 1.0d;
    private double alpha = 0.0d;
    private double beta = 1.0d;
    private BufferedImage lookupImage;
    private Color[] colors;
    private Color[] lookupColors;

    // public GradientPaintScale(double[] sampleTable, double[] breakPoints,
    // String colorRampLocation, double min, double max) {
    // this(sampleTable, breakPoints, new ColorRampReader()
    // .readColorRamp(colorRampLocation), min, max);
    // }
    // public GradientPaintScale(double[] sampleTable, double[] breakPoints,
    // int[][] colorRamp, double min, double max) {
    // this.sampleTable = sampleTable;
    // this.st = this.sampleTable;
    // this.breakPoints = breakPoints;
    // this.ramp = colorRamp;
    // this.min = min;
    // this.max = max;
    // this.colors = ImageTools.rampToColorArray(colorRamp);
    // this.lookupImage = createLookupImage(this.st, this.colors);
    // this.lookupColors = createLookupColors();
    // }
    //
    // public GradientPaintScale(double[] sampleTable, double[] breakPoints,
    // double min, double max) {
    // this(sampleTable, breakPoints, new ColorRampReader().getDefaultRamp(),
    // min, max);
    // }
    /**
     * <p>Constructor for GradientPaintScale.</p>
     *
     * @param sampleTable an array of double.
     * @param min a double.
     * @param max a double.
     * @param colors an array of {@link java.awt.Color} objects.
     */
    public GradientPaintScale(double[] sampleTable, double min, double max,
            Color[] colors) {
        this.sampleTable = sampleTable;
        this.st = this.sampleTable;
        this.min = min;
        this.max = max;
        this.colors = colors;
        this.lookupImage = createLookupImage(this.st, this.colors);
        this.lookupColors = createLookupColors();
    }

    private BufferedImage createLookupImage(double[] sampleTable,
            Color... colors) {
        // log.info("" + sampleTable.length + " samples");
        // log.info("" + colors.length + " colors");
        return ImageTools.createColorRampImage(sampleTable,
                BufferedImage.TRANSLUCENT, colors);
    }

    /**
     * <p>Setter for the field <code>ramp</code>.</p>
     *
     * @param r an array of int.
     */
    public void setRamp(int[][] r) {
        this.ramp = r;
        this.colors = ImageTools.rampToColorArray(this.ramp);
    }

    /**
     * <p>Getter for the field <code>ramp</code>.</p>
     *
     * @return an array of int.
     */
    public int[][] getRamp() {
        return this.ramp;
    }

    /**
     * <p>Getter for the field <code>alpha</code>.</p>
     *
     * @return a double.
     */
    public double getAlpha() {
        return this.alpha;
    }

    /**
     * <p>Getter for the field <code>beta</code>.</p>
     *
     * @return a double.
     */
    public double getBeta() {
        return this.beta;
    }

    /**
     * <p>Setter for the field <code>alpha</code>.</p>
     *
     * @param alpha a double.
     */
    public void setAlpha(double alpha) {
        this.alpha = alpha;
        this.lookupImage = ImageTools.createModifiedLookupImage(this.colors,
                this.st, this.alpha, this.beta, Transparency.TRANSLUCENT, 1.0f);
        this.lookupColors = createLookupColors();
    }

    /**
     * <p>Setter for the field <code>beta</code>.</p>
     *
     * @param beta a double.
     */
    public void setBeta(double beta) {
        this.beta = beta;
        this.lookupImage = ImageTools.createModifiedLookupImage(this.colors,
                this.st, this.alpha, this.beta, Transparency.TRANSLUCENT, 1.0f);
        this.lookupColors = createLookupColors();
    }

    /**
     * <p>setAlphaBeta.</p>
     *
     * @param alpha a double.
     * @param beta a double.
     */
    public void setAlphaBeta(double alpha, double beta) {
        this.alpha = alpha;
        this.beta = beta;
        this.lookupImage = ImageTools.createModifiedLookupImage(this.colors,
                this.st, this.alpha, this.beta, Transparency.TRANSLUCENT, 1.0f);
        this.lookupColors = createLookupColors();
    }

    private Color[] createLookupColors() {
        Color[] c = new Color[this.st.length];
        for (int i = 0; i < c.length; i++) {
            c[i] = new Color(this.lookupImage.getRGB(i, 0));
        }
        return c;
    }

    /**
     * <p>setUpperBound.</p>
     *
     * @param ub a double.
     */
    public void setUpperBound(double ub) {
        this.max = ub;
    }

    /**
     * <p>setLowerBound.</p>
     *
     * @param lb a double.
     */
    public void setLowerBound(double lb) {
        this.min = lb;
    }

    /** {@inheritDoc} */
    @Override
    public double getUpperBound() {
        return this.max;
    }

    /** {@inheritDoc} */
    @Override
    public Paint getPaint(double arg0) {
        double relativeIndex = ((arg0 - this.min) / (this.max - this.min));
        // log.info("RelIdx: " + relativeIndex);
        int sample = Math.max(0, Math.min(
                (int) ((this.st.length - 1) * relativeIndex),
                this.st.length - 1));
        // log.info("value: " + arg0);
        // log.info("Sample: " + sample);
        return this.lookupColors[Math.max(0, Math
                .min(this.lookupImage.getWidth() - 1,
                        (int) ((this.st.length - 1) * this.st[sample])))];
        // return new Color(this.ramp[(int) arg0][0], this.ramp[(int) arg0][1],
        // this.ramp[(int) arg0][2]);
        //
        // double v = ImageTools.getSample(this.st, this.breakPoints, arg0);
        // // log.info("Value: " + arg0 + " Sample value: " + v);
        // final int floor = Math.max(0, ((int) Math.floor(v
        // * (this.ramp.length - 1))));
        // final int ceil = Math.min(this.ramp.length - 1, ((int) Math.ceil(v
        // * this.ramp.length)));
        // int v1, v2, v3;
        // // if (floor != ceil) {
        // // log.info("Floor: " + floor + " ceil: " + ceil);
        // // }
        // // if (floor == ceil) {
        // // log.info("Floor=ceil");
        // v1 = this.ramp[floor][0];
        // v2 = this.ramp[floor][1];
        // v3 = this.ramp[floor][2];
        // // } else {
        // // // log.info("Interpolation");
        // // v1 = (int) Math.floor(255.0 * MathTools.getLinearInterpolatedY(
        // // floor, this.ramp[floor][0], ceil, this.ramp[ceil][0], v));
        // // v2 = (int) Math.floor(255.0 * MathTools.getLinearInterpolatedY(
        // // floor, this.ramp[floor][1], ceil, this.ramp[ceil][1], v));
        // // v3 = (int) Math.floor(255.0 * MathTools.getLinearInterpolatedY(
        // // floor, this.ramp[floor][2], ceil, this.ramp[ceil][2], v));
        // // }
        // // log.info(v1+" "+v2+" "+v3);
        // if (v1 < 0) {
        // v1 = 0;
        // }
        // if (v1 > 255) {
        // v1 = 255;
        // }
        // if (v2 < 0) {
        // v2 = 0;
        // }
        // if (v2 > 255) {
        // v2 = 255;
        // }
        // if (v3 < 0) {
        // v3 = 0;
        // }
        // if (v3 > 255) {
        // v3 = 255;
        // }
        // return new Color(v1, v2, v3);
    }

    /** {@inheritDoc} */
    @Override
    public double getLowerBound() {
        return this.min;
    }

    /**
     * <p>Getter for the field <code>lookupImage</code>.</p>
     *
     * @return a {@link java.awt.image.BufferedImage} object.
     */
    public BufferedImage getLookupImage() {
        return this.lookupImage;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.IConfigurable#configure(org.apache.commons.configuration.Configuration
     * )
     */
    /** {@inheritDoc} */
    @Override
    public void configure(Configuration cfg) {
        
    }

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {
        double[] st = ImageTools.createSampleTable(256);
        log.info(Arrays.toString(st));
        double min = 564.648;
        double max = 24334.234;
        GradientPaintScale gps = new GradientPaintScale(st, min, max,
                new Color[]{Color.BLACK, Color.RED, Color.orange,
                    Color.yellow, Color.white});
        double val = min;
        double incr = (max - min) / (st.length - 1);
        log.info("Increment: " + incr);
        for (int i = 0; i < st.length; i++) {
            log.info("Value: " + val);
            gps.getPaint(val);
            val += incr;
        }
        log.info("Printing min and max values");
        log.info("Min: " + min + " gps min: " + gps.getPaint(min));
        log.info("Max: " + max + " gps max: " + gps.getPaint(max));
        JList jl = new JList();
        DefaultListModel dlm = new DefaultListModel();
        jl.setModel(dlm);
        jl.setCellRenderer(new ListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list,
                    Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                if (value instanceof JLabel) {
                    // Border b =
                    // BorderFactory.createCompoundBorder(BorderFactory
                    // .createEmptyBorder(0, 0, 5, 0), BorderFactory
                    // .createLineBorder(Color.BLACK, 1));
                    // ((JLabel) value).setBorder(b);
                    return (Component) value;
                }
                return new JLabel(value.toString());
            }
        });
        JFrame jf = new JFrame();
        jf.add(new JScrollPane(jl));
        jf.setVisible(true);
        jf.setSize(200, 400);
        for (int alpha = -10; alpha <= 10; alpha++) {
            for (int beta = 1; beta <= 20; beta++) {
                gps.setAlphaBeta(alpha, beta);
                // log.info(Arrays.toString(gps.st));
                // log.info(Arrays.toString(gps.sampleTable));
                BufferedImage bi = gps.getLookupImage();
                ImageIcon ii = new ImageIcon(bi);
                dlm.addElement(new JLabel(ii));
            }
        }

    }
}
