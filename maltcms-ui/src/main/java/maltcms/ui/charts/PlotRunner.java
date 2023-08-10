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
import cross.tools.StringTools;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.Callable;
import javax.swing.JFrame;

import maltcms.tools.ImageTools;
import org.apache.commons.configuration.Configuration;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.ui.RectangleInsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callable which returns a JFreeChart object. Chart is saved to file, if
 * running in headless mode.
 *
 * @author Nils Hoffmann
 * 
 */
public class PlotRunner implements Callable<JFreeChart>, IConfigurable {
    
    private static Logger log = LoggerFactory.getLogger(PlotRunner.class);

    private Plot plot = null;
    private String title = "";
    private String filetype = "svg";
    private String filename = "";
    private File outputDir = null;
    private String fontFamily = "Lucida Sans";
    private int imgwidth = 1024, imgheight = 768;
    private boolean sizeOverride = false;
    private boolean headless = true;
    private boolean stop = false;
    private File file = null;
    private JFrame targetContainer = null;
    private boolean serializeJFreeChart = false;
    private boolean saveGraphics = true;

    /**
     * <p>Constructor for PlotRunner.</p>
     *
     * @param plot1 a {@link org.jfree.chart.plot.Plot} object.
     * @param title1 a {@link java.lang.String} object.
     * @param filename1 a {@link java.lang.String} object.
     * @param outputDir a {@link java.io.File} object.
     */
    public PlotRunner(final Plot plot1, final String title1,
            final String filename1, final File outputDir) {
        this.plot = plot1;
        this.title = title1;
        this.filename = filename1;
        this.outputDir = outputDir;
        this.filetype = StringTools.getFileExtension(filename1);
        this.file = new File(outputDir, StringTools.deBlank(filename));
        this.file.getParentFile().mkdirs();
    }

    /** {@inheritDoc} */
    @Override
    public JFreeChart call() throws Exception {
        log
                .info("#############################################################################");
        final String s = this.getClass().getName();
        log.info("# {} running", s);
        log
                .info("#############################################################################");
        if (this.plot.getBackgroundImage() != null) {
            this.imgheight = this.plot.getBackgroundImage().getHeight(null);
            this.imgwidth = this.plot.getBackgroundImage().getWidth(null);
            this.sizeOverride = true;
        }
        final StandardChartTheme sct = (StandardChartTheme) StandardChartTheme
                .createLegacyTheme();
        final Font elf = new Font(this.fontFamily, Font.BOLD, 20);
        final Font lf = new Font(this.fontFamily, Font.BOLD, 14);
        final Font rf = new Font(this.fontFamily, Font.PLAIN, 12);
        final Font sf = new Font(this.fontFamily, Font.PLAIN, 10);
        // color of outside of chart
        //sct.setWallPaint(Color.WHITE);
        sct.setDomainGridlinePaint(Color.LIGHT_GRAY);
        // background of chart???
        sct.setChartBackgroundPaint(Color.WHITE);
        sct.setLegendBackgroundPaint(Color.WHITE);
        // background of plot
        sct.setPlotBackgroundPaint(Color.WHITE);
        sct.setRangeGridlinePaint(Color.DARK_GRAY);
        sct.setExtraLargeFont(elf);
        sct.setLargeFont(lf);
        sct.setRegularFont(rf);
        sct.setSmallFont(sf);
        this.plot.setInsets(RectangleInsets.ZERO_INSETS);
        
        final JFreeChart jfc = new JFreeChart(this.plot);
        sct.apply(jfc);
        jfc.setAntiAlias(true);
        jfc.setTitle(this.title);
        jfc.setBackgroundPaint(Color.WHITE);
        if (this.headless && this.saveGraphics) {
            log.info("Creating plot {} with filename {}", this.title,
                    getFile().getAbsolutePath());
            ImageTools
                    .writeImage(jfc, getFile(), this.imgwidth, this.imgheight);
        }
        if (this.serializeJFreeChart) {
            try {
                final String filename = StringTools.removeFileExt(getFile()
                        .getAbsolutePath())
                        + ".serialized";
                log.info("Creating serialized plot {} with filename {}",
                        this.title, filename);
                try (ObjectOutputStream oos = new ObjectOutputStream(
                        new BufferedOutputStream(new FileOutputStream(filename)))) {
                    oos.writeObject(jfc);
                    oos.flush();
                }
            } catch (final FileNotFoundException e) {
                log.warn("{}", e.getLocalizedMessage());
            } catch (final IOException e) {
                log.warn("{}", e.getLocalizedMessage());
            }
        }
        return jfc;
    }

    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration cfg) {
        if (!this.sizeOverride) {
            this.imgwidth = cfg.getInt(this.getClass().getName() + ".imgwidth", 1280);
            this.imgheight = cfg.getInt(this.getClass().getName()
                    + ".imgheight", 1024);
        }
        this.filetype = cfg.getString(this.getClass().getName() + ".filetype", "png");
        this.headless = cfg.getBoolean(this.getClass().getName() + ".headless",
                true);
        this.fontFamily = cfg.getString(this.getClass().getName()
                + ".fontFamily", "sans");
        this.serializeJFreeChart = cfg.getBoolean(this.getClass().getName()
                + ".serializeJFreeChart", false);
        this.saveGraphics = cfg.getBoolean(this.getClass().getName()
                + ".saveGraphics", true);
        log.debug("configure called on {}", this.getClass().getName());
        log.debug("filetype = {}", this.filetype);
        log.debug("Image height: {} width: {}", this.imgheight,
                this.imgwidth);
    }

//    public File getFile() {
//        if (this.file == null) {
//            String ext = StringTools.getFileExtension(this.filename);
//            String fname = ext.isEmpty() ? this.filename : StringTools
//                .removeFileExt(this.filename);
//            fname = StringTools.deBlank(fname);// + "." + this.filetype;
//            this.file = new File(this.outputDir, fname + "." + filetype);//FileTools.prepareOutput(this.outputDir
//            //.getAbsolutePath(), fname, filetype);
//            this.file.getParentFile().mkdirs();
//        }
//        log.debug("Filename: " + this.file.getAbsolutePath());
//        return this.file;
//    }
    
    /**
     * <p>Getter for the field <code>file</code>.</p>
     *
     * @return the file
     */
    public File getFile() {
        return this.file;
    }
    
    /**
     * <p>Getter for the field <code>filename</code>.</p>
     *
     * @return the filename
     */
    public String getFilename() {
        return this.filename;
    }

    /**
     * <p>Getter for the field <code>filetype</code>.</p>
     *
     * @return the filetype
     */
    public String getFiletype() {
        return this.filetype;
    }

    /**
     * <p>Getter for the field <code>fontFamily</code>.</p>
     *
     * @return the fontFamily
     */
    public String getFontFamily() {
        return this.fontFamily;
    }

    /**
     * <p>Getter for the field <code>imgheight</code>.</p>
     *
     * @return the imgheight
     */
    public int getImgheight() {
        return this.imgheight;
    }

    /**
     * <p>Getter for the field <code>imgwidth</code>.</p>
     *
     * @return the imgwidth
     */
    public int getImgwidth() {
        return this.imgwidth;
    }

    /**
     * <p>Getter for the field <code>plot</code>.</p>
     *
     * @return the plot
     */
    public Plot getPlot() {
        return this.plot;
    }

    /**
     * <p>Getter for the field <code>targetContainer</code>.</p>
     *
     * @return a {@link javax.swing.JFrame} object.
     */
    public JFrame getTargetContainer() {
        return this.targetContainer;
    }

    /**
     * <p>Getter for the field <code>title</code>.</p>
     *
     * @return the title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * <p>isHeadless.</p>
     *
     * @return the headless
     */
    public boolean isHeadless() {
        return this.headless;
    }

    /**
     * <p>isSaveGraphics.</p>
     *
     * @return a boolean.
     */
    public boolean isSaveGraphics() {
        return this.saveGraphics;
    }

    /**
     * <p>isSerializeJFreeChart.</p>
     *
     * @return a boolean.
     */
    public boolean isSerializeJFreeChart() {
        return this.serializeJFreeChart;
    }

    /**
     * <p>isSizeOverride.</p>
     *
     * @return the sizeOverride
     */
    public boolean isSizeOverride() {
        return this.sizeOverride;
    }

    /**
     * <p>isStop.</p>
     *
     * @return a boolean.
     */
    public boolean isStop() {
        return this.stop;
    }

    /**
     * <p>Setter for the field <code>file</code>.</p>
     *
     * @param file the file to set
     */
    public void setFile(final File file) {
        this.file = file;
    }

    /**
     * <p>Setter for the field <code>filename</code>.</p>
     *
     * @param filename the filename to set
     */
    public void setFilename(final String filename) {
        this.filename = filename;
    }

    /**
     * <p>Setter for the field <code>filetype</code>.</p>
     *
     * @param filetype the filetype to set
     */
    public void setFiletype(final String filetype) {
        this.filetype = filetype;
    }

    /**
     * <p>Setter for the field <code>fontFamily</code>.</p>
     *
     * @param fontFamily the fontFamily to set
     */
    public void setFontFamily(final String fontFamily) {
        this.fontFamily = fontFamily;
    }

    /**
     * <p>Setter for the field <code>headless</code>.</p>
     *
     * @param b a boolean.
     */
    public void setHeadless(final boolean b) {
        this.headless = b;
    }

    /**
     * <p>Setter for the field <code>imgheight</code>.</p>
     *
     * @param imgheight the imgheight to set
     */
    public void setImgheight(final int imgheight) {
        this.imgheight = imgheight;
    }

    /**
     * <p>Setter for the field <code>imgwidth</code>.</p>
     *
     * @param imgwidth the imgwidth to set
     */
    public void setImgwidth(final int imgwidth) {
        this.imgwidth = imgwidth;
    }

    /**
     * <p>Setter for the field <code>plot</code>.</p>
     *
     * @param plot the plot to set
     */
    public void setPlot(final Plot plot) {
        this.plot = plot;
    }

    /**
     * <p>Setter for the field <code>saveGraphics</code>.</p>
     *
     * @param saveGraphics1 a boolean.
     */
    public void setSaveGraphics(final boolean saveGraphics1) {
        this.saveGraphics = saveGraphics1;
    }

    /**
     * <p>Setter for the field <code>serializeJFreeChart</code>.</p>
     *
     * @param serializeJFreeChart1 a boolean.
     */
    public void setSerializeJFreeChart(final boolean serializeJFreeChart1) {
        this.serializeJFreeChart = serializeJFreeChart1;
    }

    /**
     * <p>Setter for the field <code>sizeOverride</code>.</p>
     *
     * @param sizeOverride the sizeOverride to set
     */
    public void setSizeOverride(final boolean sizeOverride) {
        this.sizeOverride = sizeOverride;
    }

    /**
     * <p>Setter for the field <code>stop</code>.</p>
     *
     * @param stop1 a boolean.
     */
    public void setStop(final boolean stop1) {
        this.stop = stop1;
    }

    /**
     * <p>Setter for the field <code>targetContainer</code>.</p>
     *
     * @param jf a {@link javax.swing.JFrame} object.
     */
    public void setTargetContainer(final JFrame jf) {
        this.targetContainer = jf;
    }

    /**
     * <p>Setter for the field <code>title</code>.</p>
     *
     * @param title the title to set
     */
    public void setTitle(final String title) {
        this.title = title;
    }
}
