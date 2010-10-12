/*
 * Copyright (C) 2009, 2010 Mathias Wilhelm mwilhelm A T
 * TechFak.Uni-Bielefeld.DE
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
 * $Id$
 */
package maltcms.ui.charts;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.commons.configuration.Configuration;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.slf4j.Logger;

import cross.Logging;
import cross.tools.StringTools;

/**
 * This class is an extension of the normal {@link PlotRunner} and will generate
 * a serialized file. Do not use the inherit constructor from {@link PlotRunner}
 * . It will throw a {@link RuntimeException}.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
public class EPlotRunner extends PlotRunner {

	private Logger log = Logging.getLogger(this);

	private JFreeChart chart = null;

	/**
	 * Old constructor.
	 * 
	 * @param plot1
	 *            plot
	 * @param title1
	 *            title
	 * @param filename1
	 *            filename
	 * @param outputdir
	 *            output directory
	 */
	public EPlotRunner(final Plot plot1, final String title1,
	        final String filename1, final File outputdir) {
		super(plot1, title1, filename1, outputdir);
		throw new RuntimeException(
		        "Use the original PlotRunner instead of the EPlotRunner.");
	}

	/**
	 * Default constructor.
	 * 
	 * @param ichart
	 *            this chart will be plotted
	 * @param ifilename
	 *            filename of background image
	 */
	public EPlotRunner(final JFreeChart ichart, final String ifilename,
	        final File outputdir) {
		super(ichart.getPlot(), ichart.getTitle().toString(), ifilename,
		        outputdir);
		this.chart = ichart;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final Configuration cfg) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JFreeChart call() throws Exception {
		this.log
		        .info("#############################################################################");
		final String s = this.getClass().getName();
		this.log.info("# {} running", s);
		this.log
		        .info("#############################################################################");

		if (isSerializeJFreeChart()) {
			try {
				final ObjectOutputStream oos = new ObjectOutputStream(
				        new BufferedOutputStream(new FileOutputStream(
				                StringTools.removeFileExt(getFile()
				                        .getAbsolutePath())
				                        + ".serialized")));
				oos.writeObject(this.chart);
				oos.flush();
				oos.close();
			} catch (final FileNotFoundException e) {
				log.warn("{}", e.getLocalizedMessage());
			} catch (final IOException e) {
				log.warn("{}", e.getLocalizedMessage());
			}
		}
		return this.chart;
	}

}
