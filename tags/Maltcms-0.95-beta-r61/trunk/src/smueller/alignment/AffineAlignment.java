/*
 * Copyright (C) 2008, 2009 Soeren Mueller,Nils Hoffmann Nils.Hoffmann A T
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
 * $Id$
 */

package smueller.alignment;

import smueller.SymbolicRepresentationAlignment;
import cross.datastructures.fragments.IFileFragment;

/**
 * @author Soeren Mueller, smueller@cebitec.uni-bielefeld.de
 */
public class AffineAlignment extends Alignment {
	// Vertikale Matrix
	private double[][] ver;

	// Horizontale Matrix
	private double[][] hor;

	public AffineAlignment(final IFileFragment ref1, final IFileFragment query1) {
		super(ref1, query1);
	}

	// �berschriebene Methode zur Matrixberechnung, um affine Gapkosten nach
	// Gotoh zu ber�cksichtigen
	@Override
	public void computeMatrix(final String u, final String v) {
		this.seq1 = u;
		this.seq2 = v;
		final int m = u.length();
		final int n = v.length();
		this.matrix = new double[m][n];
		this.pwd = new double[m][n];
		this.ver = new double[m][n];
		this.hor = new double[m][n];
		this.pathway = new int[m][n][3];
		this.matrix[0][0] = 0;
		this.pathway[0][0][0] = 0;

		// Erste Zeile der Matritzen initialisieren
		for (int is = 1; is < n; is++) {
			final double dv = vergleiche((chartoNumber(this.seq2.charAt(is))),
			        0);
			this.pwd[0][is] = dv;
			this.matrix[0][is] = Math
			        .round((this.matrix[0][is - 1] + dv) * 100) / 100.00;
			this.pathway[0][is][0] = 1;
			this.ver[0][is] = 100000;
			this.hor[0][is] = 100000;

		}
		// Erste Spalte der Matritzen initialisieren
		for (int ju = 1; ju < m; ju++) {
			final double dv = vergleiche(0,
			        (chartoNumber(this.seq1.charAt(ju))));
			this.pwd[ju][0] = dv;
			this.matrix[ju][0] = Math
			        .round((this.matrix[ju - 1][0] + dv) * 100) / 100.00;
			this.pathway[ju][0][0] = 2;
			this.ver[ju][0] = 100000;
			this.hor[ju][0] = 100000;

		}
		// Matrix von (1,1) bis (m,n) mit Werten f�llen und Pathway f�r
		// Backtracking speichern
		for (int i = 1; i < n; i++) {
			for (int j = 1; j < m; j++) {
				final double dv = vergleiche(chartoNumber(this.seq1.charAt(j)),
				        chartoNumber(this.seq2.charAt(i)));
				this.pwd[j][i] = dv;
				// Horzontale/Vertikale Matrix an der Stelle (i,j) berechnen
				this.ver[j][i] = Math
				        .min(
				                this.matrix[j - 1][i]
				                        + SymbolicRepresentationAlignment
				                                .getGapinit()
				                        + vergleiche((chartoNumber(this.seq1
				                                .charAt(j))), 0),
				                this.ver[j - 1][i]
				                        + vergleiche((chartoNumber(this.seq1
				                                .charAt(j))), 0));
				this.hor[j][i] = Math.min(this.matrix[j][i - 1]
				        + SymbolicRepresentationAlignment.getGapinit()
				        + vergleiche(0, (chartoNumber(this.seq2.charAt(i)))),
				        this.hor[j][i - 1]
				                + vergleiche(0, (chartoNumber(this.seq2
				                        .charAt(i)))));

				// x,y (insertion/deletion)aus Matritzen �bernehmen, z
				// (diagonale)berechnen
				this.x = Math.round((this.hor[j][i]) * 100) / 100.00;
				this.y = Math.round((this.ver[j][i]) * 100) / 100.00;
				this.z = Math.round((this.matrix[j - 1][i - 1] + vergleiche(
				        (chartoNumber(this.seq1.charAt(j))),
				        (chartoNumber(this.seq2.charAt(i))))) * 100) / 100.00;

				// Minimalen Weg in Matrix �bernehmen
				this.matrix[j][i] = mini(this.x, this.y, this.z);

				// Pfad, von wo man gekommen ist, f�r Backtracking speichern
				vergleich: {
					if ((this.x == mini(this.x, this.y, this.z))
					        && (this.y == mini(this.x, this.y, this.z))
					        && (this.z == mini(this.x, this.y, this.z))) {
						this.pathway[j][i][0] = 1;
						this.pathway[j][i][1] = 2;
						this.pathway[j][i][2] = 3;
						break vergleich;
					}
					if ((this.x == mini(this.x, this.y, this.z))
					        && (this.y == mini(this.x, this.y, this.z))) {
						this.pathway[j][i][0] = 1;
						this.pathway[j][i][1] = 2;
						break vergleich;
					}
					if ((this.y == mini(this.x, this.y, this.z))
					        && (this.z == mini(this.x, this.y, this.z))) {
						this.pathway[j][i][0] = 2;
						this.pathway[j][i][1] = 3;
						break vergleich;
					}
					if ((this.x == mini(this.x, this.y, this.z))
					        && (this.z == mini(this.x, this.y, this.z))) {
						this.pathway[j][i][0] = 1;
						this.pathway[j][i][1] = 3;
						break vergleich;
					}
					if (this.x == mini(this.x, this.y, this.z)) {
						this.pathway[j][i][0] = 1;
					}
					if (this.y == mini(this.x, this.y, this.z)) {
						this.pathway[j][i][0] = 2;
					}
					if (this.z == mini(this.x, this.y, this.z)) {
						this.pathway[j][i][0] = 3;
					}
				}
			}
		}
	}

}
