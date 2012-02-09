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
package smueller.datastructure;

/**
 * @author Soeren Mueller, smueller@cebitec.uni-bielefeld.de
 */
public class NewDistanceMatrix extends DistanceMatrix {

	public NewDistanceMatrix(final double[] bp) {
		super(bp);
		final int l = bp.length - 2;
		this.distmat = new double[bp.length][bp.length];
		for (int i = 0; i < bp.length; i++) {
			this.distmat[i][0] = 3;
		}
		for (int j = 0; j < bp.length; j++) {
			this.distmat[0][j] = 3;
		}
		for (int i = 1; i <= l + 1; i++) {
			for (int j = 1; j <= l + 1; j++) {

				if (i - j == 0) {
					this.distmat[i][j] = 0;

				} else if (Math.abs(i - j) == 1) {
					this.distmat[i][j] = 1;

				} else {
					this.distmat[i][j] = 10;
				}
			}
		}
	}

}
