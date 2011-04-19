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
 * $Id: IScanProvider.java 110 2010-03-25 15:21:19Z nilshoffmann $
 */

package maltcms.datastructures.ms;

import cross.IConfigurable;

/**
 * Interface giving access to specific scans within an experiment.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 * @param <T extends IScan> provides concrete implementations of an IScan
 */
public interface IScanProvider<T extends IScan> extends IConfigurable, Iterable<T> {

	/**
	 * 
	 * @param i the scan index to retrieve
	 * @return the IScan
	 */
	public T getScan(int i);
	
	/**
	 * 
	 * @return the number of scans
	 */
	public int getNumberOfScans();

}
