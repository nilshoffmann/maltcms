/**
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
 */
/*
 * 
 *
 * $Id$
 */

package cross.tools;




import cross.exception.ConstraintViolationException;

/**
 * Utility class for evaluation of constraints.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class EvalTools {

	public static void eq(final Object a, final Object b) {
		if (!a.equals(b)) {
			throw new ConstraintViolationException(
			        "Objects a and b are not equal!");
		}
	}

	public static void eqD(final double d, final double e, final Object caller)
	        throws ConstraintViolationException {
		if (d != e) {
			throw new ConstraintViolationException("Called from "
			        + caller.toString() + ": Values are not equal: " + d + "!="
			        + e);
		}
	}

	public static void eqI(final int i, final int j, final Object caller)
	        throws ConstraintViolationException {
		if (i != j) {
			throw new ConstraintViolationException("Called from "
			        + caller.toString() + ": Values are not equal :" + i + "!="
			        + j);
		}
	}
        
	public static void inRangeD(final double min, final double max,
	        final double v, final Object caller)
	        throws ConstraintViolationException {
		// for(double d:v){
		if (v < min) {
			throw new ConstraintViolationException("Called from "
			        + caller.toString()
			        + ": Value was smaller than minimum supplied: " + v + "<"
			        + min);
		}
		if (v > max) {
			throw new ConstraintViolationException("Called from "
			        + caller.toString()
			        + ": Value was greater than maximum supplied: " + v + ">"
			        + max);
		}
		// }
	}

	public static void inRangeI(final int min, final int max, final int v,
	        final Object caller) throws ConstraintViolationException {
		// for(int i:v){
		if (v < min) {
			throw new ConstraintViolationException("Called from "
			        + caller.toString()
			        + ": Value was smaller than minimum supplied: " + v + "<"
			        + min);
		}
		if (v > max) {
			throw new ConstraintViolationException("Called from "
			        + caller.toString()
			        + ": Value was greater than maximum supplied: " + v + ">"
			        + max);
		}
		// }
	}
        
        public static void gt(final int min, final int v, final Object caller) throws ConstraintViolationException {
            if(!(v>min)) {
                throw new ConstraintViolationException("Called from "+caller.toString()+": Value was smaller or equal than expected: "+ v+"<="+min+". Should be greater!");
            }
        }
        
        public static void geq(final int min, final int v, final Object caller) throws ConstraintViolationException {
            if(!(v>=min)) {
                throw new ConstraintViolationException("Called from "+caller.toString()+": Value was smaller than expected: "+ v+"<"+min+". Should be greater or equal!");
            }
        }
        
        public static void lt(final int max, final int v, final Object caller) throws ConstraintViolationException {
            if(!(v<max)) {
                throw new ConstraintViolationException("Called from "+caller.toString()+": Value was larger or equal than expected: "+ v+">="+max+". Should be smaller!");
            }
        }
        
        public static void leq(final int max, final int v, final Object caller) throws ConstraintViolationException {
            if(!(v<=max)) {
                throw new ConstraintViolationException("Called from "+caller.toString()+": Value was larger than expected: "+ v+">"+max+". Should be smaller or equal!");
            }
        }

	public static void neq(final Object a, final Object b) {
		if (a.equals(b)) {
			throw new ConstraintViolationException("Objects a and b are equal!");
		}
	}

	public static void neqD(final double i, final double j, final Object caller)
	        throws ConstraintViolationException {
		if (i == j) {
			throw new ConstraintViolationException("Called from "
			        + caller.toString()
			        + ": Values are equal, but should not be :" + i + "!=" + j);
		}
	}

	public static void neqI(final int i, final int j, final Object caller)
	        throws ConstraintViolationException {
		if (i == j) {
			throw new ConstraintViolationException("Called from "
			        + caller.toString() + ": Values are equal :" + i + "!=" + j);
		}
	}

	public static void notNull(final Object o, final Object caller)
	        throws ConstraintViolationException {
		final String message = "Argument was null!";
		EvalTools.notNull(o, "Called from " + caller.toString() + ": "
		        + message, caller);
	}

	public static void notNull(final Object o, final String message,
	        final Object caller) throws ConstraintViolationException {
		if (o == null) {
			throw new ConstraintViolationException("Called from "
			        + caller.toString() + ": " + message);
		}
	}

	public static void notNull(final Object[] o, final Object caller)
	        throws ConstraintViolationException {
		int i = 0;
		for (final Object ob : o) {
			try {
				EvalTools.notNull(ob, caller);
			} catch (final ConstraintViolationException cve) {
				throw new ConstraintViolationException("Called from "
				        + caller.toString() + ": Argument " + (i + 1) + " of "
				        + o.length + " was null!");
			}
			i++;
		}
	}

}
