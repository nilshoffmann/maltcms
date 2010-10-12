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

package cross.exception;

/**
 * Custom Exception to allow for a given message string.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class NotImplementedException extends RuntimeException {

	/**
     * 
     */
	private static final long serialVersionUID = 8040922236426369927L;

	public NotImplementedException() {
		this("This method has not yet been implemented!");
	}

	public NotImplementedException(final String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public NotImplementedException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public NotImplementedException(final Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
}
