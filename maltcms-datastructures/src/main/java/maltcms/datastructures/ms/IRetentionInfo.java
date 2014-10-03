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

/**
 * Interface extending a simple anchor, adding retention index and time info.
 *
 * @author Nils Hoffmann
 * 
 */
public interface IRetentionInfo extends IAnchor {

    /**
     * <p>getRetentionIndex.</p>
     *
     * @return a double.
     */
    public abstract double getRetentionIndex();

    /**
     * <p>getRetentionTime.</p>
     *
     * @return a double.
     */
    public abstract double getRetentionTime();

    /**
     * <p>getRetentionTimeUnit.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getRetentionTimeUnit();

    /**
     * <p>setRetentionIndex.</p>
     *
     * @param d a double.
     */
    public abstract void setRetentionIndex(double d);

    /**
     * <p>setRetentionTime.</p>
     *
     * @param d a double.
     */
    public abstract void setRetentionTime(double d);

    /**
     * <p>setRetentionTimeUnit.</p>
     *
     * @param s a {@link java.lang.String} object.
     */
    public abstract void setRetentionTimeUnit(String s);
}
