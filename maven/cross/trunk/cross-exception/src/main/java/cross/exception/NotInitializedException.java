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
 * $Id: NotInitializedException.java 129 2010-06-25 11:57:02Z nilshoffmann $
 */

package cross.exception;

public class NotInitializedException extends RuntimeException {

	/**
     * 
     */
	private static final long serialVersionUID = -277507898414704248L;

	public NotInitializedException(final String arg0) {
		super(arg0);
	}

	public NotInitializedException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

	public NotInitializedException(final Throwable arg0) {
		super(arg0);
	}

}
