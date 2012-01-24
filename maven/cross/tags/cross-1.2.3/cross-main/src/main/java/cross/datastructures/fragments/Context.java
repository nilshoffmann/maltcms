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
 * $Id: Context.java 43 2009-10-16 17:22:55Z nilshoffmann $
 */

package cross.datastructures.fragments;

import cross.datastructures.Vars;

/**
 * Context associated to a {@link cross.datastructures.fragments.Fragment} by
 * name. Currently unused.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public interface Context {
	public Double get(String s);

	public String getQualifiedName(Vars v);

	public boolean has(String s);

	public void set(String s, Double d);
}