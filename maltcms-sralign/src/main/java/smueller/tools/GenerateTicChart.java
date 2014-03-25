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
package smueller.tools;

import maltcms.ui.charts.AChart;
import maltcms.ui.charts.XYChart;
import org.jfree.chart.plot.XYPlot;
import ucar.ma2.Array;

// Plot erzeugen
/**
 * @author Soeren Mueller, smueller@cebitec.uni-bielefeld.de
 */
public class GenerateTicChart {

    /**
     *
     * @param name
     * @param besch
     * @param a
     * @return
     */
    public static AChart<XYPlot> generatePlot(final String name,
            final String besch, final Array a) {
        final String[] beschr = new String[1];
        beschr[0] = besch;
        final Array[] test = new Array[1];
        test[0] = a;
        final AChart<XYPlot> graph = new XYChart(name, beschr, test,
                "Retention Time", "TIC");
        return graph;
    }
}
