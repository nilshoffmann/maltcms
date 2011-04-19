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
 * $Id: IChromatogram.java 115 2010-04-23 15:42:15Z nilshoffmann $
 */

package maltcms.datastructures.ms;

import cross.IConfigurable;
import java.util.List;

import ucar.ma2.Array;
import cross.datastructures.fragments.IFileFragment;

/**
 * Interface representing a Chromatogram.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public interface IChromatogram extends IConfigurable{

	public IFileFragment getParent();

	public int getNumberOfScans();

	public List<Array> getIntensities();

	public List<Array> getMasses();

	public String getScanAcquisitionTimeUnit();

	public Array getScanAcquisitionTime();

	public int getIndexFor(double scan_acquisition_time);

}
