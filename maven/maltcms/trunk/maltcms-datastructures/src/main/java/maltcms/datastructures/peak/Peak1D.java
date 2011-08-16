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
 * $Id: Peak1D.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */
package maltcms.datastructures.peak;

import maltcms.datastructures.peak.annotations.PeakAnnotation;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import maltcms.datastructures.array.IFeatureVector;
import maltcms.tools.PublicMemberGetters;
import maltcms.tools.ArrayTools;
import ucar.ma2.Array;
import cross.exception.ResourceNotAvailableException;
import java.io.File;
import lombok.Data;
import maltcms.tools.MaltcmsTools;
import ucar.ma2.MAMath;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 *         Peak1D models a standard 1D chromatographic peak. If you want to
 *         model mass spectral peaks over time, use a Peak1DGroup instance.
 * 
 */
@Data
public class Peak1D implements Serializable, IFeatureVector, Iterable<Peak1D> {
	/**
     * 
     */
	private static final long serialVersionUID = -8878754902179218064L;
	private int startIndex = -1;
	private int apexIndex = -1;
	private int stopIndex = -1;
	private double apexIntensity = -1;
	private double startTime = -1;
	private double stopTime = -1;
	private double apexTime = -1;
	private double area = -1;
	private double startMass = -1;
        private double stopMass = -1;
        private double[] extractedIonCurrent;
	private String file = "";
        private PeakType peakType = PeakType.UNDEFINED;
        private List<PeakAnnotation> peakAnnotations;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * maltcms.datastructures.array.IFeatureVector#getFeature(java.lang.String)
	 */
	@Override
	public Array getFeature(String name) {
		PublicMemberGetters<Peak1D> pmg = new PublicMemberGetters<Peak1D>(
		        getClass());
		Method m = pmg.getMethodForGetterName(name);
		if (m == null)
			throw new ResourceNotAvailableException(
			        "Could not find compatible method for feature with name : "
			                + name);
		try {
			Object o = m.invoke(this, new Object[] {});
			// if (o.getClass().isPrimitive()) {
			// throw new NotImplementedException();
			// }
			if (o == null) {
				throw new ResourceNotAvailableException(
				        "Can not create array representation of object for method: "
				                + name);
			}
                        if(o instanceof Array) {
                            return (Array)o;
                        }
			return ArrayTools.factoryScalar(o);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new ResourceNotAvailableException(
		        "Could not find compatible method for feature with name : "
		                + name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.array.IFeatureVector#getFeatureNames()
	 */
	@Override
	public List<String> getFeatureNames() {
		PublicMemberGetters<Peak1D> pmg = new PublicMemberGetters<Peak1D>(
		        getClass(), "Feature", "FeatureNames");
		return Arrays.asList(pmg.getGetterNames());
	}

//	public static void main(String[] args) {
//		Peak1D p = new Peak1D(30, 35, 40, 123124.932, 12354);
//		System.out.println(p.getFeature("StartIndex"));
//		System.out.println(p.getFeature("StopIndex"));
//		System.out.println(p.getFeature("Area"));
//		System.out.println(p.getFeature("Intensity"));
//	}

    @Override
    public Iterator<Peak1D> iterator() {
        final Peak1D thisPeak = this;
        return new Iterator<Peak1D>() {
            private Peak1D peak = thisPeak;
            private boolean hasNext = true;
            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public Peak1D next() {
                Peak1D returnedPeak = this.peak;
                this.peak = null;
                hasNext = false;
                return returnedPeak;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }
}
