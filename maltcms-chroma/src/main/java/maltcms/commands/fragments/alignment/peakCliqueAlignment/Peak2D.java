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
package maltcms.commands.fragments.alignment.peakCliqueAlignment;

import cross.datastructures.cache.SerializableArray;
import cross.exception.ResourceNotAvailableException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */
@Data
public class Peak2D extends PeakNG {

    private double firstColumnElutionTime;
    private double secondColumnElutionTime;

    public Peak2D(int scanIndex, Array array, double sat, String association, int associationId) {
        super(scanIndex, array, sat, association, associationId);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(" at rt1: ").append(firstColumnElutionTime).append(" and at rt2: ").append(secondColumnElutionTime);
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.datastructures.array.IFeatureVector#getFeature(java.lang.String)
     */
    @Override
    public Array getFeature(String name) {
        switch (name) {
            case "first_column_elution_time":
                return Array.factory(this.firstColumnElutionTime);
            case "second_column_elution_time":
                return Array.factory(this.secondColumnElutionTime);
        }
        Array retVal = super.getFeature(name);
        if (retVal != null) {
            return retVal;
        }
        throw new ResourceNotAvailableException("No such feature: " + name);
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IFeatureVector#getFeatureNames()
     */
    @Override
    public List<String> getFeatureNames() {
        List<String> superFeatureNames = super.getFeatureNames();
        LinkedList<String> allFeatures = new LinkedList<>(superFeatureNames);
        allFeatures.addAll(Arrays.asList("first_column_elution_time", "second_column_elution_time"));
        return allFeatures;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Peak2D) {
            return getUniqueId().equals(((Peak2D) o).getUniqueId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getUniqueId().hashCode();
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeDouble(firstColumnElutionTime);
        out.writeDouble(secondColumnElutionTime);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        firstColumnElutionTime = in.readDouble();
        secondColumnElutionTime = in.readDouble();
    }

}
