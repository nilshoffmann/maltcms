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

package maltcms.datastructures.ms;

import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import cross.datastructures.tuple.Tuple2D;

/**
 * Interface representing a Metabolite.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public interface IMetabolite extends IRetentionInfo {

	public String getComments();

	public String getDate();

	public String getFormula();

	public String getID();

	public Tuple2D<ArrayDouble.D1, ArrayInt.D1> getMassSpectrum();

	public double getMaxIntensity();

	public double getMaxMass();

	public double getMinIntensity();

	public double getMinMass();

	public int getMW();

	public String getShortName();

	public String getSP();

	public void setComments(String comments);

	public void setDate(String date);

	public void setFormula(String formula);

	public void setID(String id);

	public void setMassSpectrum(ArrayDouble.D1 masses, ArrayInt.D1 intensities);

	public void setMaxIntensity(double intens);

	public void setMaxMass(double m);

	public void setMinIntensity(double intens);

	public void setMinMass(double m);

	public void setMW(int mw);

	public void setShortName(String sname);

	public void setSP(String sp);

	public void update(IMetabolite m);

}
