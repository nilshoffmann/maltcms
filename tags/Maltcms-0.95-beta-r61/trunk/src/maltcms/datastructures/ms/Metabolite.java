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
import ucar.ma2.IndexIterator;
import ucar.ma2.MAMath;
import ucar.ma2.ArrayDouble.D1;
import ucar.ma2.MAMath.MinMax;
import cross.datastructures.tuple.Tuple2D;

/**
 * Concrete implementation of a Metabolite.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class Metabolite implements IMetabolite {

	private ArrayDouble.D1 masses = null;

	private ArrayInt.D1 intensities = null;

	private String name = null;

	private String id = null;

	private String id_type = null;

	private int dbno = -1;

	private String comments = null;

	private double min_mass, max_mass, min_intensity, max_intensity,
	        min_intensity_norm, max_intensity_norm;

	private String date;

	private double ri = -1.0d;

	private String sp;

	private String sname;

	private String formula;

	private double retentionTime;

	private String retentionTimeUnit;

	private int scanIndex = -1;

	private int mw;

	public Metabolite() {

	}

	public Metabolite(final String name1, final String id1,
	        final String id_type1, final int dbno1, final String comments1,
	        final String formula1, final String date1, final double ri1,
	        final double retentionTime1, final String retentionTimeUnit1,
	        final int mw1, final String sp1, final String shortName,
	        final ArrayDouble.D1 masses1, final ArrayInt.D1 intensities1) {
		this();
		this.name = name1;
		this.id = id1;
		this.id_type = id_type1;
		this.dbno = dbno1;
		this.comments = comments1;
		this.formula = formula1;
		this.date = date1;
		this.ri = ri1;
		this.retentionTime = retentionTime1;
		this.retentionTimeUnit = retentionTimeUnit1;
		this.mw = mw1;
		this.sp = sp1;
		this.sname = shortName;
		setMassSpectrum(masses1, intensities1);
	}

	public int compareTo(final IAnchor o) {
		if (o instanceof IMetabolite) {
			// TODO include distance based comparison
		}
		if (o instanceof IRetentionInfo) {
			// If no retention index is given, use names as comparison criterion
			if ((((IRetentionInfo) o).getRetentionIndex() < 0)
			        || (getRetentionIndex() < 0)) {
				return getName().compareTo(o.getName());
			}
			if (((IRetentionInfo) o).getRetentionIndex() < getRetentionIndex()) {
				return 1;
			}
			if (((IRetentionInfo) o).getRetentionIndex() > getRetentionIndex()) {
				return -1;
			}
			return 0;
		} else {
			if (o.getScanIndex() < getScanIndex()) {
				return 1;
			}
			if (o.getScanIndex() > getScanIndex()) {
				return -1;
			}
			return 0;
		}
	}

	public String getComments() {
		return this.comments;
	}

	public String getDate() {
		return this.date;
	}

	public int getDBNO() {
		return this.dbno;
	}

	public String getFormula() {
		return this.formula;
	}

	public String getID() {
		return this.id;
	}

	public String getIDType() {
		return this.id_type;
	}

	public Tuple2D<D1, ucar.ma2.ArrayInt.D1> getMassSpectrum() {
		return new Tuple2D<D1, ucar.ma2.ArrayInt.D1>(this.masses,
		        this.intensities);
	}

	public double getMaxIntensity() {
		return this.max_intensity;
	}

	public double getMaxMass() {
		return this.max_mass;
	}

	public double getMinIntensity() {
		return this.min_intensity;
	}

	public double getMinMass() {
		return this.min_mass;
	}

	public int getMW() {
		return this.mw;
	}

	public String getName() {
		return this.name;
	}

	public double getRetentionIndex() {
		return this.ri;
	}

	public double getRetentionTime() {
		return this.retentionTime;
	}

	public String getRetentionTimeUnit() {
		return this.retentionTimeUnit;
	}

	public int getScanIndex() {
		return this.scanIndex;
	}

	public String getShortName() {
		return this.sname;
	}

	public String getSP() {
		return this.sp;
	}

	public void setComments(final String comments1) {
		this.comments = comments1;
	}

	public void setDate(final String date1) {
		this.date = date1;
	}

	public void setFormula(final String formula1) {
		this.formula = formula1;
	}

	public void setID(final String id1) {
		this.id = id1;
	}

	public void setMassSpectrum(final D1 masses1,
	        final ucar.ma2.ArrayInt.D1 intensities1) {
		if ((masses1 != null) && (intensities1 != null)) {
			final MinMax mm = MAMath.getMinMax(masses1);
			this.min_mass = mm.min;
			this.max_mass = mm.max;
			final MinMax mi = MAMath.getMinMax(intensities1);
			this.min_intensity = mi.min;
			this.max_intensity = mi.max;
			this.masses = masses1;
			this.intensities = intensities1;
			// final MAVector mav = new MAVector(this.intensities);
			// mav.normalize();
			// System.out.println(mav);
			// final MinMax m = MAMath.getMinMax(this.intensities);
			// this.min_intensity_norm = m.min;
			// this.max_intensity_norm = m.max;
		}
	}

	public void setMaxIntensity(final double intens) {
		this.max_intensity = intens;
	}

	public void setMaxMass(final double m) {
		this.max_mass = m;
	}

	public void setMinIntensity(final double intens) {
		this.min_intensity = intens;
	}

	public void setMinMass(final double m) {
		this.min_mass = m;
	}

	public void setMW(final int mw1) {
		this.mw = mw1;
	}

	public void setName(final String s) {
		this.name = s;
	}

	public void setRetentionIndex(final double d) {
		this.ri = d;
	}

	public void setRetentionTime(final double d) {
		this.retentionTime = d;
	}

	public void setRetentionTimeUnit(final String s) {
		this.retentionTimeUnit = s;
	}

	public void setScanIndex(final int scan) {
		this.scanIndex = scan;
	}

	public void setShortName(final String sname1) {
		this.sname = sname1;
	}

	public void setSP(final String sp1) {
		this.sp = sp1;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append("Name: " + getName());
		sb.append("\n");
		sb.append("Synon: DATE:" + getDate());
		sb.append("\n");
		sb.append("Synon: NAME:" + getShortName());
		sb.append("\n");
		sb.append("Synon: SP:" + getSP());
		sb.append("\n");
		sb.append("Synon: " + getIDType() + ":" + getID());
		sb.append("\n");
		sb.append("Synon: RI:" + getRetentionIndex());
		sb.append("\n");
		if (getRetentionTime() > 0) {
			sb.append("Synony: RT:" + getRetentionTimeUnit()
			        + getRetentionTime());
			sb.append("\n");
		}
		sb.append("Comments: " + getComments());
		sb.append("\n");
		if (getFormula() != null) {
			sb.append("Formula: " + getFormula());
			sb.append("\n");
		}
		if (getMW() > 0) {
			sb.append("MW: " + getMW());
			sb.append("\n");
		}
		sb.append("DB#: " + getDBNO());
		sb.append("\n");
		sb.append("Num Peaks: " + this.masses.getShape()[0]);
		sb.append("\n");
		final IndexIterator mi = this.masses.getIndexIterator();
		final IndexIterator ii = this.intensities.getIndexIterator();
		int linepointcount = 0;
		while (mi.hasNext() && ii.hasNext()) {
			sb.append(mi.getIntNext() + " " + ii.getIntNext() + ";");
			if (linepointcount == 5) {
				sb.append("\n");
				linepointcount = 0;
			} else {
				sb.append(" ");
				linepointcount++;
			}
		}
		sb.append("\n");
		return sb.toString();
	}

	public void update(final IMetabolite m) {
		if (getID().equals(m.getID())) {
			if ((m.getComments() != null) && !m.getComments().equals("")) {
				setComments(m.getComments());
			}
			if ((m.getDate() != null) && !m.getDate().equals("")) {
				setDate(m.getDate());
			}
			if ((m.getFormula() != null) && !m.getFormula().equals("")) {
				setFormula(m.getFormula());
			}
			if ((m.getName() != null) && !m.getName().equals("")) {
				setName(m.getName());
			}
			if ((m.getMassSpectrum().getFirst() != null)
			        && (m.getMassSpectrum().getSecond() != null)) {
				setMassSpectrum(m.getMassSpectrum().getFirst(), m
				        .getMassSpectrum().getSecond());
			}
			if (m.getMW() > 0) {
				setMW(m.getMW());
			}
			if (m.getRetentionIndex() > 0) {
				setRetentionIndex(m.getRetentionIndex());
			}
			if (m.getRetentionTime() > 0) {
				setRetentionTime(m.getRetentionTime());
			}
			if ((m.getRetentionTimeUnit() != null)
			        && !m.getRetentionTimeUnit().equals("")) {
				setRetentionTimeUnit(m.getRetentionTimeUnit());
			}
			if ((m.getShortName() != null) && !m.getShortName().equals("")) {
				setShortName(m.getShortName());
			}
			if ((m.getSP() != null) && !m.getSP().equals("")) {
				setSP(m.getSP());
			}
		} else {
			throw new IllegalArgumentException(
			        "IDs do not match, cannot update!");
		}

	}

}
