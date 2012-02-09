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
package maltcms.commands.fragments2d.warp.visualization;

import java.awt.image.BufferedImage;
import java.util.List;

import maltcms.ui.charts.AChart;

import org.jfree.chart.JFreeChart;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import cross.IConfigurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;

/**
 * This interface provies all method to be an visualization for the 2d time warp
 * path.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
public interface IVisualization extends IConfigurable {

	/**
	 * Will create a {@link BufferedImage} from the given scanlines and a warp
	 * path.
	 * 
	 * @param scanlinesi
	 *            scanlines from the left chromatogram
	 * @param scanlinesj
	 *            scanlines from the top chromatogram
	 * @param warpPathi
	 *            warp path for the left chromatogram
	 * @param warpPathj
	 *            warp path for the top chromatogram
	 * @return a {@link BufferedImage} which contains both chromatograms
	 */
	BufferedImage createImage(List<Array> scanlinesi, List<Array> scanlinesj,
	        Array warpPathi, Array warpPathj);

	/**
	 * Creates a {@link AChart}.
	 * 
	 * @param filename
	 *            background image filename
	 * @param samplenamei
	 *            sample name of the first chromatogram
	 * @param samplenamej
	 *            sample name of the second chromatogram
	 * @param rettimei
	 *            first and second retention time of the first chromatogram
	 * @param rettimej
	 *            first and second retention time of the second chromatogram
	 * @return {@link JFreeChart}
	 */
	JFreeChart createChart(String filename, String samplenamei,
	        String samplenamej,
	        Tuple2D<ArrayDouble.D1, ArrayDouble.D1> rettimei,
	        Tuple2D<ArrayDouble.D1, ArrayDouble.D1> rettimej);

	/**
	 * Adds the peak marker.
	 * 
	 * @param chart
	 *            chart
	 * @param warpPathij
	 *            warp path for i and j
	 * @param ref
	 *            reference
	 * @param query
	 *            query
	 * @param rettimei
	 *            reference retention time
	 * @param rettimej
	 *            query rettention time
	 * @param spm
	 *            scans per modualtion
	 * @return new chart
	 */
	JFreeChart addPeakMarker(JFreeChart chart,
	        Tuple2D<Array, Array> warpPathij, IFileFragment ref,
	        IFileFragment query,
	        Tuple2D<ArrayDouble.D1, ArrayDouble.D1> rettimei,
	        Tuple2D<ArrayDouble.D1, ArrayDouble.D1> rettimej, int spm);

	/**
	 * Setter.
	 * 
	 * @param nBinSize
	 *            new bin size
	 */
	void setBinSize(int nBinSize);

	/**
	 * Getter.
	 * 
	 * @return bin size
	 */
	int getBinSize();

	/**
	 * Setter.
	 * 
	 * @param nNormalize
	 *            normalize 1-N nodes in warp path
	 */
	void setNormalize(final boolean nNormalize);
}
