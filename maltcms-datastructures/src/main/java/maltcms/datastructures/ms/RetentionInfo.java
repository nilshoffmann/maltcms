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
 * Concrete implementation of {@link maltcms.datastructures.ms.IRetentionInfo}.
 *
 * @author Nils Hoffmann
 *
 */
public class RetentionInfo implements IRetentionInfo {

    private double ri = -1;
    private double rt = -1;
    private String rtu = "seconds";
    private String name = "";
    private int scanIndex = -1;

    @Override
    public int compareTo(final IAnchor o) {
        if (o instanceof IRetentionInfo) {
            // If no retention index is given, use names as comparison criterion
            if ((((IRetentionInfo) o).getRetentionIndex() < 0)
                    || (getRetentionIndex() < 0)) {
                return getName().compareTo(o.getName());
            }
            // if retention index is given
            if (((IRetentionInfo) o).getRetentionIndex() > getRetentionIndex()) {
                return -1;
            }
            if (((IRetentionInfo) o).getRetentionIndex() < getRetentionIndex()) {
                return 1;
            }
            return 0;
        }
        return toString().compareTo(o.toString());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public double getRetentionIndex() {
        return this.ri;
    }

    @Override
    public double getRetentionTime() {
        return this.rt;
    }

    @Override
    public String getRetentionTimeUnit() {
        return this.rtu;
    }

    @Override
    public int getScanIndex() {
        return this.scanIndex;
    }

    @Override
    public void setName(final String s) {
        this.name = s;
    }

    @Override
    public void setRetentionIndex(final double d) {
        this.ri = d;
    }

    @Override
    public void setRetentionTime(final double d) {
        this.rt = d;
    }

    @Override
    public void setRetentionTimeUnit(final String s) {
        this.rtu = s;
    }

    @Override
    public void setScanIndex(final int scan) {
        this.scanIndex = scan;
    }

    @Override
    public String toString() {
        if (getRetentionIndex() <= 0) {
            return "" + getName();
        } else {
            return "" + getRetentionIndex();
        }
    }
}
