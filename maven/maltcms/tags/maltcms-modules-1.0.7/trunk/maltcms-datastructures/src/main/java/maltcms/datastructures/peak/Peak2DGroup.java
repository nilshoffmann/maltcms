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
 * $Id: Peak1DGroup.java 159 2010-08-31 18:44:07Z nilshoffmann $
 */
package maltcms.datastructures.peak;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import cross.annotations.NoFeature;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class Peak2DGroup extends Peak2D implements Iterable<Peak2D> {

	/**
     * 
     */
	private static final long serialVersionUID = 5613625440101236042L;

	private TreeSet<Peak2D> l;

	public Peak2DGroup(Peak2D... p) {
		this.l = new TreeSet<Peak2D>(new Peak2DComparator());
		this.l.addAll(Arrays.asList(p));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * maltcms.datastructures.array.IFeatureVector#getFeature(java.lang.String)
	 */
	@Override
	public Array getFeature(String name) {
		return super.getFeature(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.array.IFeatureVector#getFeatureNames()
	 */
	@Override
	public List<String> getFeatureNames() {
		return super.getFeatureNames();
	}

	public Array getMasses() {
		int size = this.l.size();
		ArrayDouble.D1 masses = new ArrayDouble.D1(size);
		Iterator<Peak2D> miter = this.l.iterator();
		int i = 0;
		while (miter.hasNext()) {
			Peak2D p = miter.next();
			masses.set(i++, p.getMw());
		}
		return masses;
	}

	public Array getIntensities() {
		int size = this.l.size();
		ArrayDouble.D1 intensities = new ArrayDouble.D1(size);
		Iterator<Peak2D> miter = this.l.iterator();
		int i = 0;
		while (miter.hasNext()) {
			Peak1D p = miter.next();
			intensities.set(i++, p.getIntensity());
		}
		return intensities;
	}

	private final class Peak2DComparator implements Comparator<Peak2D> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Peak2D o1, Peak2D o2) {
			return Double.compare(o1.getMw(), o2.getMw());
		}

	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		Iterator<Peak2D> p = this.l.iterator();
		while (p.hasNext()) {
			sb.append(p.next() + "\n");
		}
		return sb.toString();
	}

	@Override
	@NoFeature
	public Iterator<Peak2D> iterator() {
		return this.l.iterator();
	}

	public static void main(String[] args) {
		Peak1D p1 = new Peak1D(30, 35, 40, 213.932, 144);
		p1.setMw(58.32);
		Peak1D p2 = new Peak1D(28, 35, 42, 3134.932, 1567);
		p2.setMw(73.923);
		Peak1D p3 = new Peak1D(26, 35, 41, 252.932, 235);
		p3.setMw(314.123);
		Peak1D p4 = new Peak1D(30, 35, 40, 2367.932, 1267);
		p4.setMw(124.25);
		Peak1DGroup pg = new Peak1DGroup(p1, p2, p3, p4);
		System.out.println(pg.getFeatureNames());
		System.out.println(pg.getFeature("Masses"));
		System.out.println(pg.getFeature("Intensities"));
		// System.out.println(pg.getFeature("Peaks"));
		// System.out.println(pg.getFeature("FeatureNames"));
		for (Peak1D p : pg) {
			System.out.print(p);
		}

		Peak2D p21 = new Peak2D();
		p21.setApexIndex(50);
		p21.setStartIndex(30);
		p21.setStopIndex(80);
		p21.setFirstRetTime(380);
		p21.setSecondRetTime(3.12);
		p21.setMw(123.23);
		// System.out.println(p21.getFeature("ApexIndex"));
		// System.out.println(p21.getFeature("StartIndex"));
		// System.out.println(p21.getFeature("StopIndex"));
		// System.out.println(p21.getFeature("FirstRetTime"));
		// System.out.println(p21.getFeature("SecondRetTime"));
		Peak2D p22 = new Peak2D();
		p22.setApexIndex(50);
		p22.setStartIndex(30);
		p22.setStopIndex(80);
		p22.setFirstRetTime(380);
		p22.setSecondRetTime(3.12);
		p22.setMw(456.014);

		Peak2DGroup pg2 = new Peak2DGroup(p21, p22);
		System.out.println(pg2.getFeatureNames());
		System.out.println(pg2.getFeature("Masses"));
		System.out.println(pg2.getFeature("Intensities"));
		System.out.println(pg2.getFeature("FirstRetTime"));
		System.out.println(pg2.getFeature("SecondRetTime"));
		// System.out.println(pg.getFeature("Peaks"));
		// System.out.println(pg.getFeature("FeatureNames"));
		for (Peak2D p : pg2) {
			System.out.print(p);
		}
	}

}
