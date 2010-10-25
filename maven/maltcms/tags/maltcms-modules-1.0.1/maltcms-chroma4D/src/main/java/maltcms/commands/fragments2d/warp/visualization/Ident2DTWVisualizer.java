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
 * $Id: Ident2DTWVisualizer.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */
package maltcms.commands.fragments2d.warp.visualization;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import cross.datastructures.tuple.Tuple2D;

/**
 * Basically the same as {@link Default2DTWVisualizer} but dont uses the warp
 * path.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
public class Ident2DTWVisualizer extends Default2DTWVisualizer {

	// private Logger log = Logging.getLogger(this.getClass());

	private int currentrasterline = -1;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferedImage createImage(final List<Array> scanlinesi,
	        final List<Array> scanlinesj, final Array warpPathi,
	        final Array warpPathj) {

		final int imageheight = (int) (scanlinesi.get(0).getSize());

		final List<Tuple2D<Array, Array>> outputintensities = new ArrayList<Tuple2D<Array, Array>>();
		final List<Tuple2D<Integer, Integer>> outputintensitiescounter = new ArrayList<Tuple2D<Integer, Integer>>();

		final Tuple2D<double[], Tuple2D<double[], double[]>> sb = getSampleAndBreakpointTable(
		        scanlinesi, scanlinesj);

		final ArrayDouble.D1 emptyScanline = new ArrayDouble.D1(imageheight);
		maltcms.tools.ArrayTools.fill(emptyScanline, 0);
		for (int i = 0; i < Math.max(scanlinesi.size(), scanlinesj.size()); i++) {
			final Array scanlinei;
			final Array scanlinej;
			if (i < scanlinesi.size()) {
				scanlinei = scanlinesi.get(i);
			} else {
				scanlinei = emptyScanline;
			}
			if (i < scanlinesj.size()) {
				scanlinej = scanlinesj.get(i);
			} else {
				scanlinej = emptyScanline;
			}
			outputintensities.add(new Tuple2D<Array, Array>(scanlinei,
			        scanlinej));
			outputintensitiescounter.add(new Tuple2D<Integer, Integer>(1, 1));
			this.currentrasterline++;
		}

		return ci(outputintensities, outputintensitiescounter, imageheight, sb
		        .getFirst(), sb.getSecond().getFirst(), sb.getSecond()
		        .getSecond());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final Configuration cfg) {
		super.configure(cfg);
	}

}
