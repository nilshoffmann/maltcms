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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import cross.datastructures.feature.IFeatureVector;
import maltcms.tools.PublicMemberGetters;
import maltcms.tools.ArrayTools;
import ucar.ma2.Array;
import cross.exception.ResourceNotAvailableException;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 *         Peak1D models a standard 1D chromatographic peak. If you want to
 *         model mass spectral peaks over time, use a Peak1DGroup instance.
 * 
 */
public class Peak1D implements Serializable, IFeatureVector {
	/**
     * 
     */
	private static final long serialVersionUID = -8878754902179218064L;
	int startIndex = -1;
	int apexIndex = -1;
	int stopIndex = -1;
	double intensity = -1;
	double startTime = -1;
	double stopTime = -1;
	double apexTime = -1;
	double area = -1;
	double mw = -1;

	String name = "";

	String file = "";

	public Peak1D() {

	}

	public Peak1D(final int startIndex, final int apexIndex,
	        final int stopIndex, final double area, final double intensity) {
		this.startIndex = startIndex;
		this.apexIndex = apexIndex;
		this.stopIndex = stopIndex;
		this.area = area;
		this.intensity = intensity;
		this.name = "";
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getApexIndex() {
		return apexIndex;
	}

	public void setApexIndex(int apexIndex) {
		this.apexIndex = apexIndex;
	}

	public int getStopIndex() {
		return stopIndex;
	}

	public void setStopIndex(int stopIndex) {
		this.stopIndex = stopIndex;
	}

	public double getIntensity() {
		return intensity;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setIntensity(double intensity) {
		this.intensity = intensity;
	}

	public double getStartTime() {
		return startTime;
	}

	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	public double getStopTime() {
		return stopTime;
	}

	public void setStopTime(double stopTime) {
		this.stopTime = stopTime;
	}

	public double getApexTime() {
		return apexTime;
	}

	public void setApexTime(double apexTime) {
		this.apexTime = apexTime;
	}

	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}

	public double getMw() {
		return mw;
	}

	public void setMw(double mw) {
		this.mw = mw;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Peak for file " + getFile() + " ");
		sb.append("from index " + getStartIndex() + " to " + getStopIndex()
		        + " apex at " + getApexIndex() + " ");
		sb.append("from rt " + getStartTime() + " to " + getStopTime()
		        + " apex at " + getApexTime() + " ");
		sb.append("area " + getArea() + " apex intensity " + getIntensity()
		        + " mass " + getMw() + "\n");
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

	public static void main(String[] args) {
		Peak1D p = new Peak1D(30, 35, 40, 123124.932, 12354);
		System.out.println(p.getFeature("StartIndex"));
		System.out.println(p.getFeature("StopIndex"));
		System.out.println(p.getFeature("Area"));
		System.out.println(p.getFeature("Intensity"));
	}
}
