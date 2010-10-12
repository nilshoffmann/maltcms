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

package cross.datastructures.tuple;

/**
 * Typed specialization of Tuple2D for Float, providing additional methods for
 * arithmetic with Tuple2DF.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class Tuple2DF extends Tuple2D<Float, Float> {

	public static Tuple2D<Float, Float> diff(final Tuple2D<Float, Float> t1,
	        final Tuple2D<Float, Float> t2) {
		return new Tuple2DF(t1.getFirst() - t2.getFirst(), t1.getSecond()
		        - t2.getSecond());
	}

	public static Float dot(final Tuple2D<Float, Float> t1,
	        final Tuple2D<Float, Float> t2) {
		return t1.getFirst() * t2.getFirst() + t1.getSecond() * t2.getSecond();
	}

	public static Float len(final Tuple2D<Float, Float> t1) {
		return 1.0f / (float) Math.sqrt(Tuple2DF.dot(t1, t1));
	}

	public static Tuple2D<Float, Float> mult(final Tuple2D<Float, Float> l1,
	        final Float mult) {
		return new Tuple2DF(l1.getFirst() * mult, l1.getSecond() * mult);
	}

	public static Tuple2D<Float, Float> trans(final Tuple2D<Float, Float> t1) {
		return new Tuple2DF(t1.getSecond(), t1.getFirst());
	}

	public Tuple2DF(final Float t1, final Float t2) {
		super(t1, t2);
	}

}
