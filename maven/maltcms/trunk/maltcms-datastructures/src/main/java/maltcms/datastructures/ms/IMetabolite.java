/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
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

    public double getMw();

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

    public void setMw(double mw);

    public void setShortName(String sname);

    public void setSP(String sp);

    public void update(IMetabolite m);
}
