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
 * $Id$
 */
package maltcms.commands.distances;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import cross.Logging;
import cross.annotations.Configurable;

/**
 * Hamming distance between binary vectors.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class ArrayHamming implements IArrayDoubleComp {

	@Configurable(name = "expansion_weight")
	private double wExp = 1.0d;
	@Configurable(name = "compression_weight")
	private double wComp = 1.0d;
	@Configurable(name = "diagonal_weight")
	private double wDiag = 1.0d;

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.commands.distances.IArrayComp#apply(int, int, double,
	 * double, ucar.ma2.Array, ucar.ma2.Array)
	 */
	@Override
	public Double apply(final int i1, final int i2, final double time1,
	        final double time2, final Array t1, final Array t2) {
		int d = 0;
		final IndexIterator it1 = t1.getIndexIterator();
		final IndexIterator it2 = t2.getIndexIterator();
		while (it1.hasNext() && it2.hasNext()) {
			boolean b1 = (it1.getDoubleNext()) > 0 ? true : false;
			final boolean b2 = (it2.getDoubleNext()) > 0 ? true : false;
			b1 = ((b1 && !b2) || (!b1 && b2));
			if (b1) {
				d++;
			}
		}
		return (double) d;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.IConfigurable#configure(org.apache.commons.configuration.Configuration
	 * )
	 */
	@Override
	public void configure(final Configuration cfg) {
		this.wComp = cfg.getDouble(this.getClass().getName()
		        + ".compression_weight", 1.0d);
		this.wExp = cfg.getDouble(this.getClass().getName()
		        + ".expansion_weight", 1.0d);
		this.wDiag = cfg.getDouble(this.getClass().getName()
		        + ".diagonal_weight", 2.25d);
		StringBuilder sb = new StringBuilder();
		sb.append("wComp: " + this.wComp + ", ");
		sb.append("wExp: " + this.wExp + ", ");
		sb.append("wDiag: " + this.wDiag);
		Logging.getLogger(this).info("Parameters of class {}: {}",
		        this.getClass().getName(), sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.commands.distances.IArrayComp#getCompressionWeight()
	 */
	@Override
	public double getCompressionWeight() {
		return this.wComp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.commands.distances.IArrayComp#getDiagonalWeight()
	 */
	@Override
	public double getDiagonalWeight() {
		return this.wDiag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.commands.distances.IArrayComp#getExpansionWeight()
	 */
	@Override
	public double getExpansionWeight() {
		return this.wExp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.commands.distances.IArrayComp#minimize()
	 */
	@Override
	public boolean minimize() {
		return true;
	}

}
