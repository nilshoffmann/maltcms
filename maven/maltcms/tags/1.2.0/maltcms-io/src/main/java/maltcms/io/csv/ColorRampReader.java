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
package maltcms.io.csv;

import java.util.Vector;

import org.slf4j.Logger;

import cross.Logging;
import cross.datastructures.tuple.Tuple2D;

/**
 * Reads in custom color ramps.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class ColorRampReader {

	private final Logger log = Logging.getLogger(this);

	private static int[][] defaultRamp = { { 0, 0, 255 }, { 0, 3, 255 },
	        { 0, 7, 255 }, { 0, 11, 255 }, { 0, 15, 255 }, { 0, 19, 255 },
	        { 0, 23, 255 }, { 0, 27, 255 }, { 0, 31, 255 }, { 0, 35, 255 },
	        { 0, 39, 255 }, { 0, 43, 255 }, { 0, 47, 255 }, { 0, 51, 255 },
	        { 0, 55, 255 }, { 0, 59, 255 }, { 0, 63, 255 }, { 0, 67, 255 },
	        { 0, 71, 255 }, { 0, 75, 255 }, { 0, 79, 255 }, { 0, 83, 255 },
	        { 0, 87, 255 }, { 0, 91, 255 }, { 0, 95, 255 }, { 0, 99, 255 },
	        { 0, 103, 255 }, { 0, 107, 255 }, { 0, 111, 255 }, { 0, 115, 255 },
	        { 0, 119, 255 }, { 0, 123, 255 }, { 0, 127, 255 }, { 0, 132, 255 },
	        { 0, 135, 255 }, { 0, 140, 255 }, { 0, 143, 255 }, { 0, 148, 255 },
	        { 0, 151, 255 }, { 0, 156, 255 }, { 0, 159, 255 }, { 0, 164, 255 },
	        { 0, 167, 255 }, { 0, 172, 255 }, { 0, 175, 255 }, { 0, 180, 255 },
	        { 0, 183, 255 }, { 0, 188, 255 }, { 0, 191, 255 }, { 0, 196, 255 },
	        { 0, 199, 255 }, { 0, 204, 255 }, { 0, 207, 255 }, { 0, 212, 255 },
	        { 0, 215, 255 }, { 0, 220, 255 }, { 0, 223, 255 }, { 0, 228, 255 },
	        { 0, 231, 255 }, { 0, 236, 255 }, { 0, 239, 255 }, { 0, 244, 255 },
	        { 0, 247, 255 }, { 0, 252, 255 }, { 0, 255, 254 }, { 0, 255, 249 },
	        { 0, 255, 246 }, { 0, 255, 241 }, { 0, 255, 238 }, { 0, 255, 233 },
	        { 0, 255, 230 }, { 0, 255, 225 }, { 0, 255, 222 }, { 0, 255, 217 },
	        { 0, 255, 214 }, { 0, 255, 209 }, { 0, 255, 206 }, { 0, 255, 201 },
	        { 0, 255, 198 }, { 0, 255, 193 }, { 0, 255, 190 }, { 0, 255, 185 },
	        { 0, 255, 182 }, { 0, 255, 177 }, { 0, 255, 174 }, { 0, 255, 169 },
	        { 0, 255, 166 }, { 0, 255, 161 }, { 0, 255, 158 }, { 0, 255, 153 },
	        { 0, 255, 150 }, { 0, 255, 145 }, { 0, 255, 142 }, { 0, 255, 137 },
	        { 0, 255, 134 }, { 0, 255, 129 }, { 0, 255, 126 }, { 0, 255, 122 },
	        { 0, 255, 118 }, { 0, 255, 114 }, { 0, 255, 110 }, { 0, 255, 106 },
	        { 0, 255, 102 }, { 0, 255, 98 }, { 0, 255, 94 }, { 0, 255, 90 },
	        { 0, 255, 86 }, { 0, 255, 82 }, { 0, 255, 78 }, { 0, 255, 74 },
	        { 0, 255, 70 }, { 0, 255, 66 }, { 0, 255, 61 }, { 0, 255, 57 },
	        { 0, 255, 53 }, { 0, 255, 49 }, { 0, 255, 45 }, { 0, 255, 41 },
	        { 0, 255, 37 }, { 0, 255, 33 }, { 0, 255, 29 }, { 0, 255, 25 },
	        { 0, 255, 21 }, { 0, 255, 17 }, { 0, 255, 13 }, { 0, 255, 9 },
	        { 0, 255, 5 }, { 0, 255, 1 }, { 1, 255, 0 }, { 5, 255, 0 },
	        { 9, 255, 0 }, { 13, 255, 0 }, { 17, 255, 0 }, { 21, 255, 0 },
	        { 25, 255, 0 }, { 29, 255, 0 }, { 33, 255, 0 }, { 37, 255, 0 },
	        { 41, 255, 0 }, { 45, 255, 0 }, { 49, 255, 0 }, { 53, 255, 0 },
	        { 57, 255, 0 }, { 61, 255, 0 }, { 66, 255, 0 }, { 70, 255, 0 },
	        { 74, 255, 0 }, { 78, 255, 0 }, { 82, 255, 0 }, { 86, 255, 0 },
	        { 90, 255, 0 }, { 94, 255, 0 }, { 98, 255, 0 }, { 102, 255, 0 },
	        { 106, 255, 0 }, { 110, 255, 0 }, { 114, 255, 0 }, { 118, 255, 0 },
	        { 122, 255, 0 }, { 126, 255, 0 }, { 129, 255, 0 }, { 134, 255, 0 },
	        { 137, 255, 0 }, { 142, 255, 0 }, { 145, 255, 0 }, { 150, 255, 0 },
	        { 153, 255, 0 }, { 158, 255, 0 }, { 161, 255, 0 }, { 166, 255, 0 },
	        { 169, 255, 0 }, { 174, 255, 0 }, { 177, 255, 0 }, { 182, 255, 0 },
	        { 185, 255, 0 }, { 190, 255, 0 }, { 193, 255, 0 }, { 198, 255, 0 },
	        { 201, 255, 0 }, { 206, 255, 0 }, { 209, 255, 0 }, { 214, 255, 0 },
	        { 217, 255, 0 }, { 222, 255, 0 }, { 225, 255, 0 }, { 230, 255, 0 },
	        { 233, 255, 0 }, { 238, 255, 0 }, { 241, 255, 0 }, { 246, 255, 0 },
	        { 249, 255, 0 }, { 254, 255, 0 }, { 255, 252, 0 }, { 255, 247, 0 },
	        { 255, 244, 0 }, { 255, 239, 0 }, { 255, 236, 0 }, { 255, 231, 0 },
	        { 255, 228, 0 }, { 255, 223, 0 }, { 255, 220, 0 }, { 255, 215, 0 },
	        { 255, 212, 0 }, { 255, 207, 0 }, { 255, 204, 0 }, { 255, 199, 0 },
	        { 255, 196, 0 }, { 255, 191, 0 }, { 255, 188, 0 }, { 255, 183, 0 },
	        { 255, 180, 0 }, { 255, 175, 0 }, { 255, 172, 0 }, { 255, 167, 0 },
	        { 255, 164, 0 }, { 255, 159, 0 }, { 255, 156, 0 }, { 255, 151, 0 },
	        { 255, 148, 0 }, { 255, 143, 0 }, { 255, 140, 0 }, { 255, 135, 0 },
	        { 255, 132, 0 }, { 255, 127, 0 }, { 255, 123, 0 }, { 255, 119, 0 },
	        { 255, 115, 0 }, { 255, 111, 0 }, { 255, 107, 0 }, { 255, 103, 0 },
	        { 255, 99, 0 }, { 255, 95, 0 }, { 255, 91, 0 }, { 255, 87, 0 },
	        { 255, 83, 0 }, { 255, 79, 0 }, { 255, 75, 0 }, { 255, 71, 0 },
	        { 255, 67, 0 }, { 255, 63, 0 }, { 255, 59, 0 }, { 255, 55, 0 },
	        { 255, 51, 0 }, { 255, 47, 0 }, { 255, 43, 0 }, { 255, 39, 0 },
	        { 255, 35, 0 }, { 255, 31, 0 }, { 255, 27, 0 }, { 255, 23, 0 },
	        { 255, 19, 0 }, { 255, 15, 0 }, { 255, 11, 0 }, { 255, 7, 0 },
	        { 255, 3, 0 }, { 255, 0, 0 } };

	public int[][] getDefaultRamp() {
		return ColorRampReader.defaultRamp;
	}

	public int[][] readColorRamp(final String url) {
		final CSVReader csvr = new CSVReader();
		csvr.setFirstLineHeaders(false);
		final Tuple2D<Vector<Vector<String>>, Vector<String>> t = csvr
		        .read(url);
		if (t.getFirst().isEmpty()) {
			this.log.warn("Could not retrieve color ramp, using default!");
			return ColorRampReader.defaultRamp;
		} else {
			final Vector<Vector<String>> rows = t.getFirst();
			this.log.debug("Read {} color rows.", rows.size());
			final int[][] ramp = new int[rows.size()][];
			int i = 0;
			for (final Vector<String> v : rows) {
				this.log.debug("Row {}={}", i, v.toString());
				ramp[i] = new int[v.size() - 1];
				// skip the first column
				for (int j = 0; j < ramp[i].length; j++) {
					ramp[i][j] = Integer.parseInt(v.get(j + 1));
				}
				i++;
			}
			return ramp;
		}
	}

}
