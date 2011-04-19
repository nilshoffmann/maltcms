/*
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
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
 * $Id: Tuple2DD.java 43 2009-10-16 17:22:55Z nilshoffmann $
 */

package cross.datastructures.tuple;

/**
 * Typed specialization of Tuple2D for Double, providing additional methods for
 * arithmetic with Tuple2DD.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class Tuple2DD extends Tuple2D<Double, Double> {

	public static Tuple2D<Double, Double> diff(
	        final Tuple2D<Double, Double> t1, final Tuple2D<Double, Double> t2) {
		return new Tuple2DD(t1.getFirst() - t2.getFirst(), t1.getSecond()
		        - t2.getSecond());
	}

	public static Double dot(final Tuple2D<Double, Double> t1,
	        final Tuple2D<Double, Double> t2) {
		return t1.getFirst() * t2.getFirst() + t1.getSecond() * t2.getSecond();
	}

	public static Double len(final Tuple2D<Double, Double> t1) {
		return 1.0f / Math.sqrt(Tuple2DD.dot(t1, t1));
	}

	public static Tuple2D<Double, Double> mult(
	        final Tuple2D<Double, Double> l1, final Double mult) {
		return new Tuple2DD(l1.getFirst() * mult, l1.getSecond() * mult);
	}

	public static Tuple2D<Double, Double> trans(final Tuple2D<Double, Double> t1) {
		return new Tuple2DD(t1.getSecond(), t1.getFirst());
	}

	public Tuple2DD(final Double t1, final Double t2) {
		super(t1, t2);
	}

}
