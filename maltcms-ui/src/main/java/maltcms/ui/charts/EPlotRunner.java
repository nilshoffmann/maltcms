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

import cross.tools.StringTools;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;

/**
 * This class is an extension of the normal {@link maltcms.ui.charts.PlotRunner} and will generate
 * a serialized file. Do not use the inherit constructor from {@link maltcms.ui.charts.PlotRunner}
 * . It will throw a {@link java.lang.RuntimeException}.
 *
 * @author Mathias Wilhelm
 * 
 */
@Slf4j
public class EPlotRunner extends PlotRunner {

    private JFreeChart chart = null;

    /**
     * Old constructor.
     *
     * @param plot1 plot
     * @param title1 title
     * @param filename1 filename
     * @param outputdir output directory
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
     * @param ichart this chart will be plotted
     * @param ifilename filename of background image
     * @param outputdir a {@link java.io.File} object.
     */
    public EPlotRunner(final JFreeChart ichart, final String ifilename,
            final File outputdir) {
        super(ichart.getPlot(), ichart.getTitle().toString(), ifilename,
                outputdir);
        this.chart = ichart;
    }

    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration cfg) {
    }

    /** {@inheritDoc} */
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
                try (ObjectOutputStream oos = new ObjectOutputStream(
                        new BufferedOutputStream(new FileOutputStream(
                                        StringTools.removeFileExt(getFile()
                                                .getAbsolutePath())
                                        + ".serialized")))) {
                                            oos.writeObject(this.chart);
                                            oos.flush();
                                        }
            } catch (final FileNotFoundException e) {
                log.warn("{}", e.getLocalizedMessage());
            } catch (final IOException e) {
                log.warn("{}", e.getLocalizedMessage());
            }
        }
        return this.chart;
    }
}
