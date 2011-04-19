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
 * $Id: ResourceNotAvailableException.java 43 2009-10-16 17:22:55Z nilshoffmann
 * $
 */

package cross.exception;

public class ResourceNotAvailableException extends RuntimeException {

	/**
     * 
     */
	private static final long serialVersionUID = -277507898414704248L;

	public ResourceNotAvailableException(final String arg0) {
		super(arg0);
	}

	public ResourceNotAvailableException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

	public ResourceNotAvailableException(final Throwable arg0) {
		super(arg0);
	}

}