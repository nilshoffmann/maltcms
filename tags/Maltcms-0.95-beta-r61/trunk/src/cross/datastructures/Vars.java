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

/**
 * Created by hoffmann at 28.02.2007
 */
package cross.datastructures;

/**
 * Variables for use in evaluation with {@link cross.tools.EvalTools}.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public enum Vars {
	Mean {
		@Override
		public String toString() {
			return "Mean";
		}
	},
	Max {
		@Override
		public String toString() {
			return "Max";
		}
	},
	Min {
		@Override
		public String toString() {
			return "Min";
		}
	},
	Variance {
		@Override
		public String toString() {
			return "Variance";
		}
	},
	Size {
		@Override
		public String toString() {
			return "Size";
		}
	},
	Next {
		@Override
		public String toString() {
			return "Next";
		}
	},
	Previous {
		@Override
		public String toString() {
			return "Previous";
		}
	},
	Current {
		@Override
		public String toString() {
			return "Current";
		}
	},
	First {
		@Override
		public String toString() {
			return "First";
		}
	},
	Last {
		@Override
		public String toString() {
			return "Last";
		}
	},
	Index {
		@Override
		public String toString() {
			return "Index";
		}
	},
	Length {
		@Override
		public String toString() {
			return "Length";
		}
	},
	None {
		@Override
		public String toString() {
			return "None";
		}
	};

	public static Vars fromString(final String s)
	        throws IllegalArgumentException {
		// System.out.println("Enum from: "+s);
		if (s.equalsIgnoreCase("Mean")) {
			return Vars.Mean;
		} else if (s.equalsIgnoreCase("Max")) {
			return Vars.Max;
		} else if (s.equalsIgnoreCase("Min")) {
			return Vars.Min;
		} else if (s.equalsIgnoreCase("Variance")) {
			return Vars.Variance;
		} else if (s.equalsIgnoreCase("Size")) {
			return Vars.Size;
		} else if (s.equalsIgnoreCase("Next")) {
			return Vars.Next;
		} else if (s.equalsIgnoreCase("Previous")) {
			return Vars.Previous;
		} else if (s.equalsIgnoreCase("Current")) {
			return Vars.Current;
		} else if (s.equalsIgnoreCase("First")) {
			return Vars.First;
		} else if (s.equalsIgnoreCase("Last")) {
			return Vars.Last;
		} else if (s.equalsIgnoreCase("Index")) {
			return Vars.Index;
		} else if (s.equalsIgnoreCase("Length")) {
			return Vars.Length;
		} else if (s.equalsIgnoreCase("None")) {
			return Vars.None;
		} else {
			throw new IllegalArgumentException("Unknown Enum member " + s + "!");
		}
	}

	public static Vars fromString(final String s, final String prefix) {
		return Vars.fromString(s.substring(prefix.length()));
	}

	public static String toString(final Vars v, final String prefix) {
		return prefix + v.toString();
	}
}
