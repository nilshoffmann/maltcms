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

package maltcms.datastructures.constraint;

import java.util.Arrays;
import java.util.List;

/**
 * Abstraction to handle all alignment constraints limiting the number of
 * elements to be looked at equally.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class IndexConstraintSet implements IIndexRange {

	private Integer axis[] = null;

	private Integer[] lowerBound = null;

	private Integer[] upperBound = null;

	private boolean tighten = true;

	private final List<IIndexRange> constraints;

	/**
	 * 
	 * @param axis1
	 *            the axis to link this constraint set to
	 * @param constraints1
	 *            the constraints
	 * @param tighten1
	 *            whether constraints should only tighten already existing
	 *            bounds
	 */
	public IndexConstraintSet(final Integer[] axis1, final boolean tighten1,
	        final IIndexRange... constraints1) {
		this.axis = axis1;
		this.tighten = tighten1;
		this.constraints = Arrays.asList(constraints1);
		adjustBounds(this.constraints);
	}

	public void addConstraint(final IIndexRange c) {
		this.constraints.add(c);
		adjustBounds(Arrays.asList(c));
	}

	protected void adjustBounds(final List<IIndexRange> constraints1) {
		System.out.println("Adjusting bounds!");
		if ((this.lowerBound == null) || (this.upperBound == null)) {
			this.lowerBound = new Integer[this.axis.length];
			this.upperBound = new Integer[this.axis.length];
			for (int i = 0; i < this.axis.length; i++) {
				this.lowerBound[i] = -1;
				this.upperBound[i] = -1;
			}
		}

		for (int k = 0; k < this.axis.length; k++) {
			System.out.println("Old bounds for axis " + k + "; Lower: "
			        + this.lowerBound[k] + ", upper: " + this.upperBound[k]);
			for (final IIndexRange range : constraints1) {
				int lb = -1;
				int ub = -1;
				if (this.lowerBound[k] >= 0) {
					if (this.tighten) {
						lb = Math.max(range.getLowerBound(k),
						        this.lowerBound[k]);
					} else {
						lb = Math.min(range.getLowerBound(k),
						        this.lowerBound[k]);
					}
					if ((lb < 0) || (ub == Integer.MAX_VALUE)) {
						throw new IllegalArgumentException(
						        "Lower bound violation " + lb + "!");
					}
				} else {
					this.lowerBound[k] = range.getLowerBound(k);
				}
				if (this.upperBound[k] >= 0) {
					if (this.tighten) {
						ub = Math.min(range.getUpperBound(k),
						        this.upperBound[k]);
					} else {
						ub = Math.max(range.getUpperBound(k),
						        this.upperBound[k]);
					}
					if ((ub < lb) || (ub < 0) || (ub == Integer.MAX_VALUE)) {
						throw new IllegalArgumentException(
						        "Upper bound violation" + ub + "! Lower bound "
						                + lb);
					}
				} else {
					this.upperBound[k] = range.getUpperBound(k);
				}

			}
			System.out.println("New bounds for axis " + k + "; Lower: "
			        + this.lowerBound[k] + ", upper: " + this.upperBound[k]);
		}
	}

	public boolean allowed(final Integer dim, final Integer index) {
		if (((getLowerBound(dim) <= index) && (getUpperBound(dim) > index))) {
			return true;
		}
		return false;
	}

	public Integer getLowerBound(final Integer k) {
		return this.lowerBound[k];
	}

	public Integer getUpperBound(final Integer k) {
		return this.upperBound[k];
	}

	public boolean next(final Integer... is) {
		boolean b = true;
		for (final IIndexRange ir : this.constraints) {
			b = b && ir.next(is);
		}
		if (b) {
			adjustBounds(this.constraints);
		}
		return b;
		// adjustBounds(this.constraints.toArray(new IIndexRange[] {}));
	}

}
