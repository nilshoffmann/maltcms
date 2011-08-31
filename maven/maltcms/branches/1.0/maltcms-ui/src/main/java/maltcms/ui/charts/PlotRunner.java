/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: PlotRunner.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */

package maltcms.ui.charts;

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
import org.jfree.ui.RectangleInsets;
import org.slf4j.Logger;

import cross.IConfigurable;
import cross.Logging;
import cross.datastructures.tools.FileTools;
import cross.tools.StringTools;

/**
 * Callable which returns a JFreeChart object. Chart is saved to file, if
 * running in headless mode.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class PlotRunner implements Callable<JFreeChart>, IConfigurable {

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

	private final Logger log = Logging.getLogger(this);

	private boolean serializeJFreeChart = true;

	private boolean saveGraphics = true;

	public PlotRunner(final Plot plot1, final String title1,
	        final String filename1, final File outputDir) {
		this.plot = plot1;
		this.title = title1;
		this.filename = filename1;
		this.outputDir = outputDir;
	}

	@Override
	public JFreeChart call() throws Exception {
		this.log
		        .info("#############################################################################");
		final String s = this.getClass().getName();
		this.log.info("# {} running", s);
		this.log
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
		sct.setWallPaint(Color.WHITE);
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
			this.log.info("Creating plot {} with filename {}", this.title,
			        getFile().getAbsolutePath());
			ImageTools
			        .writeImage(jfc, getFile(), this.imgwidth, this.imgheight);
		}
		if (this.serializeJFreeChart) {
			try {
				final String filename = StringTools.removeFileExt(getFile()
				        .getAbsolutePath())
				        + ".serialized";
				this.log.info("Creating serialized plot {} with filename {}",
				        this.title, filename);
				final ObjectOutputStream oos = new ObjectOutputStream(
				        new BufferedOutputStream(new FileOutputStream(filename)));
				oos.writeObject(jfc);
				oos.flush();
				oos.close();
			} catch (final FileNotFoundException e) {
				this.log.warn("{}", e.getLocalizedMessage());
			} catch (final IOException e) {
				this.log.warn("{}", e.getLocalizedMessage());
			}
		}
		return jfc;
	}

	@Override
	public void configure(final Configuration cfg) {
		if (!this.sizeOverride) {
			this.imgwidth = cfg.getInt(this.getClass().getName() + ".imgwidth");
			this.imgheight = cfg.getInt(this.getClass().getName()
			        + ".imgheight");
		}
		this.filetype = cfg.getString(this.getClass().getName() + ".filetype");
		this.headless = cfg.getBoolean(this.getClass().getName() + ".headless",
		        true);
		this.fontFamily = cfg.getString(this.getClass().getName()
		        + ".fontFamily");
		this.serializeJFreeChart = cfg.getBoolean(this.getClass().getName()
		        + ".serializeJFreeChart", true);
		this.saveGraphics = cfg.getBoolean(this.getClass().getName()
		        + ".saveGraphics", true);
		this.log.debug("configure called on {}", this.getClass().getName());
		this.log.debug("filetype = {}", this.filetype);
		this.log.debug("Image height: {} width: {}", this.imgheight,
		        this.imgwidth);
	}

	public File getFile() {
		if (this.file == null) {
			String ext = StringTools.getFileExtension(this.filename);
			String fname = ext.isEmpty() ? this.filename : StringTools
			        .removeFileExt(this.filename);
			fname = StringTools.deBlank(fname);// + "." + this.filetype;
			this.file = FileTools.prepareOutput(this.outputDir
			        .getAbsolutePath(), fname, ext);
		}
		System.out.println("Filename: " + this.file.getAbsolutePath());
		return this.file;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return this.filename;
	}

	/**
	 * @return the filetype
	 */
	public String getFiletype() {
		return this.filetype;
	}

	/**
	 * @return the fontFamily
	 */
	public String getFontFamily() {
		return this.fontFamily;
	}

	/**
	 * @return the imgheight
	 */
	public int getImgheight() {
		return this.imgheight;
	}

	/**
	 * @return the imgwidth
	 */
	public int getImgwidth() {
		return this.imgwidth;
	}

	/**
	 * @return the plot
	 */
	public Plot getPlot() {
		return this.plot;
	}

	public JFrame getTargetContainer() {
		return this.targetContainer;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * @return the headless
	 */
	public boolean isHeadless() {
		return this.headless;
	}

	public boolean isSaveGraphics() {
		return this.saveGraphics;
	}

	public boolean isSerializeJFreeChart() {
		return this.serializeJFreeChart;
	}

	/**
	 * @return the sizeOverride
	 */
	public boolean isSizeOverride() {
		return this.sizeOverride;
	}

	public boolean isStop() {
		return this.stop;
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile(final File file) {
		this.file = file;
	}

	/**
	 * @param filename
	 *            the filename to set
	 */
	public void setFilename(final String filename) {
		this.filename = filename;
	}

	/**
	 * @param filetype
	 *            the filetype to set
	 */
	public void setFiletype(final String filetype) {
		this.filetype = filetype;
	}

	/**
	 * @param fontFamily
	 *            the fontFamily to set
	 */
	public void setFontFamily(final String fontFamily) {
		this.fontFamily = fontFamily;
	}

	public void setHeadless(final boolean b) {
		this.headless = b;
	}

	/**
	 * @param imgheight
	 *            the imgheight to set
	 */
	public void setImgheight(final int imgheight) {
		this.imgheight = imgheight;
	}

	/**
	 * @param imgwidth
	 *            the imgwidth to set
	 */
	public void setImgwidth(final int imgwidth) {
		this.imgwidth = imgwidth;
	}

	/**
	 * @param plot
	 *            the plot to set
	 */
	public void setPlot(final Plot plot) {
		this.plot = plot;
	}

	public void setSaveGraphics(final boolean saveGraphics1) {
		this.saveGraphics = saveGraphics1;
	}

	public void setSerializeJFreeChart(final boolean serializeJFreeChart1) {
		this.serializeJFreeChart = serializeJFreeChart1;
	}

	/**
	 * @param sizeOverride
	 *            the sizeOverride to set
	 */
	public void setSizeOverride(final boolean sizeOverride) {
		this.sizeOverride = sizeOverride;
	}

	public void setStop(final boolean stop1) {
		this.stop = stop1;
	}

	public void setTargetContainer(final JFrame jf) {
		this.targetContainer = jf;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(final String title) {
		this.title = title;
	}

}
