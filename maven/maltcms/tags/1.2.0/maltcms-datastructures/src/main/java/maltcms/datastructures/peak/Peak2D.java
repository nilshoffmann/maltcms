/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package maltcms.datastructures.peak;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import maltcms.datastructures.ms.IMetabolite;
import maltcms.datastructures.ms.Metabolite;
import cross.datastructures.tuple.Tuple2D;

/**
 * Dataholder for all important information about a peak.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
public class Peak2D extends Peak1D implements Serializable {

	/**
     * 
     */
	private static final long serialVersionUID = 6343209775465424292L;
	private PeakArea2D peakArea = null;
	private int scanIndex = -1;
	private int index = -1;
	private double firstRetTime = -1.0d;
	private double secondRetTime = -1.0d;
	private List<Tuple2D<Double, IMetabolite>> identification = new ArrayList<Tuple2D<Double, IMetabolite>>();
	private Peak2D reference = null;

	/**
	 * Default constructor.
	 * 
	 * @param pa
	 *            peak area
	 */
	public Peak2D() {
		super();
		this.identification = new ArrayList<Tuple2D<Double, IMetabolite>>();
		final ArrayList<Tuple2D<Double, IMetabolite>> hitsD = new ArrayList<Tuple2D<Double, IMetabolite>>();
		final Metabolite m = new Metabolite();
		m.setID("Unkown");
		hitsD.add(new Tuple2D<Double, IMetabolite>(1.0, m));
		this.setNames(hitsD);
	}

	/**
	 * Getter.
	 * 
	 * @return first retention time
	 */
	public double getFirstRetTime() {
		return this.firstRetTime;
	}

	/**
	 * Getter.
	 * 
	 * @return internal index
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * Getter.
	 * 
	 * @return name
	 */
	public String getName() {
		if ((this.identification != null) && (this.identification.size() > 0)) {
			return this.identification.get(0).getSecond().getID();
		}

		return "Wut?";
	}

	/**
	 * Getter.
	 * 
	 * @return list of score an {@link IMetabolite}.
	 */
	public List<Tuple2D<Double, IMetabolite>> getNames() {
		return this.identification;
	}

	/**
	 * Getter.
	 * 
	 * @return peak area
	 */
	public PeakArea2D getPeakArea() {
		return this.peakArea;
	}

	/**
	 * Getter.
	 * 
	 * @return getFirstRetTime() + getSecondRetTime()
	 */
	public double getRetentionTime() {
		return this.firstRetTime + this.secondRetTime;
	}

	/**
	 * Getter.
	 * 
	 * @return scan index
	 */
	public int getScanIndex() {
		return this.scanIndex;
	}

	/**
	 * Getter.
	 * 
	 * @return second retention time
	 */
	public double getSecondRetTime() {
		return this.secondRetTime;
	}

	/**
	 * Getter.
	 * 
	 * @return similarity
	 */
	public double getSim() {
		if ((this.identification != null) && (this.identification.size() > 0)) {
			return this.identification.get(0).getFirst();
		}

		return 0.0d;
	}

	/**
	 * Setter.
	 * 
	 * @param
	 */
	public void setPeakArea(final PeakArea2D pa) {
		this.peakArea = pa;
	}

	/**
	 * Setter.
	 * 
	 * @param nFirstRetTime
	 *            first retention time
	 */
	public void setFirstRetTime(final double nFirstRetTime) {
		this.firstRetTime = nFirstRetTime;
	}

	/**
	 * Setter.
	 * 
	 * @param nIndex
	 *            internal index
	 */
	public void setIndex(final int nIndex) {
		this.index = nIndex;
	}

	/**
	 * Setter.
	 * 
	 * @param l
	 *            list of score an {@link IMetabolite}.
	 */
	public void setNames(final List<Tuple2D<Double, IMetabolite>> l) {
		this.identification = l;
	}

	/**
	 * Setter.
	 * 
	 * @param nScanIndex
	 *            scan index
	 */
	public void setScanIndex(final int nScanIndex) {
		this.scanIndex = nScanIndex;
	}

	/**
	 * Setter.
	 * 
	 * @param nSecondRetTime
	 *            second retention time
	 */
	public void setSecondRetTime(final double nSecondRetTime) {
		this.secondRetTime = nSecondRetTime;
	}

	public int getFirstScanIndex() {
		return this.peakArea.getSeedPoint().x;
	}

	public int getSecondScanIndex() {
		return this.peakArea.getSeedPoint().y;
	}

	public void normalizeTo(Peak2D reference) {
		this.reference = reference;
		this.peakArea.normalizeTo(reference);
	}

	public Peak2D getReference() {
		return this.reference;
	}

	@Override
	public double getArea() {
		if (this.peakArea == null) {
			return Double.NaN;
		}
		return this.peakArea.getAreaIntensity();
	}

	public void setArea(double d) {
		throw new UnsupportedOperationException(
		        "Not supported in Peak2D. Please use the PeakArea Object to do this.");
	}

	public static void main(String[] args) {
		Peak2D p = new Peak2D();
		p.setApexIndex(50);
		p.setStartIndex(30);
		p.setStopIndex(80);
		p.setFirstRetTime(380);
		p.setSecondRetTime(3.12);
		System.out.println(p.getFeature("ApexIndex"));
		System.out.println(p.getFeature("StartIndex"));
		System.out.println(p.getFeature("StopIndex"));
		System.out.println(p.getFeature("FirstRetTime"));
		System.out.println(p.getFeature("SecondRetTime"));
		// System.out.println(p.getFeature("PeakArea"));
	}

}
