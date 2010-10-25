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
 * $Id: IExperiment.java 159 2010-08-31 18:44:07Z nilshoffmann $
 */

package maltcms.datastructures.ms;

import java.util.HashMap;

import cross.IConfigurable;
import cross.datastructures.fragments.IFileFragment;
import cross.exception.ResourceNotAvailableException;

/**
 * Interface representing an experiment. Delegate/Facade to cover a real
 * IFileFragment.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public interface IExperiment extends IConfigurable, IFileFragment {

	public IFileFragment getFileFragment();

	public HashMap<String, String> getMetadata();

	public void setFileFragment(IFileFragment ff);

	public void setMetadata(String key, String value);

	public String getMetadata(String key) throws ResourceNotAvailableException;

}
