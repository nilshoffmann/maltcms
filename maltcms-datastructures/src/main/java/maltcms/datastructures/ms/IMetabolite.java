/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
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

import cross.datastructures.tuple.Tuple2D;
import java.net.URI;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;

/**
 * Interface representing a Metabolite.
 *
 * @author Nils Hoffmann
 * 
 */
public interface IMetabolite extends IRetentionInfo {

    /**
     * <p>getComments.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getComments();

    /**
     * <p>getDate.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDate();

    /**
     * <p>getFormula.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFormula();

    /**
     * <p>getLink.</p>
     *
     * @return a {@link java.net.URI} object.
     */
    public URI getLink();

    /**
     * <p>getID.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getID();

    /**
     * <p>getMassSpectrum.</p>
     *
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    public Tuple2D<ArrayDouble.D1, ArrayInt.D1> getMassSpectrum();

    /**
     * <p>getMaxIntensity.</p>
     *
     * @return a double.
     */
    public double getMaxIntensity();

    /**
     * <p>getMaxMass.</p>
     *
     * @return a double.
     */
    public double getMaxMass();

    /**
     * <p>getMinIntensity.</p>
     *
     * @return a double.
     */
    public double getMinIntensity();

    /**
     * <p>getMinMass.</p>
     *
     * @return a double.
     */
    public double getMinMass();

    /**
     * <p>getMW.</p>
     *
     * @return a int.
     */
    public int getMW();

    /**
     * <p>getMw.</p>
     *
     * @return a double.
     */
    public double getMw();

    /**
     * <p>getShortName.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getShortName();

    /**
     * <p>getSP.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSP();

    /**
     * <p>setComments.</p>
     *
     * @param comments a {@link java.lang.String} object.
     */
    public void setComments(String comments);

    /**
     * <p>setDate.</p>
     *
     * @param date a {@link java.lang.String} object.
     */
    public void setDate(String date);

    /**
     * <p>setFormula.</p>
     *
     * @param formula a {@link java.lang.String} object.
     */
    public void setFormula(String formula);

    /**
     * <p>setID.</p>
     *
     * @param id a {@link java.lang.String} object.
     */
    public void setID(String id);

    /**
     * <p>setLink.</p>
     *
     * @param link a {@link java.net.URI} object.
     */
    public void setLink(URI link);

    /**
     * <p>setMassSpectrum.</p>
     *
     * @param masses a {@link ucar.ma2.ArrayDouble.D1} object.
     * @param intensities a {@link ucar.ma2.ArrayInt.D1} object.
     */
    public void setMassSpectrum(ArrayDouble.D1 masses, ArrayInt.D1 intensities);

    /**
     * <p>setMaxIntensity.</p>
     *
     * @param intens a double.
     */
    public void setMaxIntensity(double intens);

    /**
     * <p>setMaxMass.</p>
     *
     * @param m a double.
     */
    public void setMaxMass(double m);

    /**
     * <p>setMinIntensity.</p>
     *
     * @param intens a double.
     */
    public void setMinIntensity(double intens);

    /**
     * <p>setMinMass.</p>
     *
     * @param m a double.
     */
    public void setMinMass(double m);

    /**
     * <p>setMW.</p>
     *
     * @param mw a int.
     */
    public void setMW(int mw);

    /**
     * <p>setMw.</p>
     *
     * @param mw a double.
     */
    public void setMw(double mw);

    /**
     * <p>setShortName.</p>
     *
     * @param sname a {@link java.lang.String} object.
     */
    public void setShortName(String sname);

    /**
     * <p>setSP.</p>
     *
     * @param sp a {@link java.lang.String} object.
     */
    public void setSP(String sp);

    /**
     * <p>update.</p>
     *
     * @param m a {@link maltcms.datastructures.ms.IMetabolite} object.
     */
    public void update(IMetabolite m);
}
