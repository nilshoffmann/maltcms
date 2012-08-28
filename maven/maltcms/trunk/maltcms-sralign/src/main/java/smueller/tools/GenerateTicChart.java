/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
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
