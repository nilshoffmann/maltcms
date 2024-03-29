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

import cross.datastructures.cache.SerializableArray;
import cross.datastructures.tuple.Tuple2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayDouble.D1;
import ucar.ma2.ArrayInt;
import ucar.ma2.IndexIterator;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;

/**
 * Concrete implementation of a Metabolite.
 *
 * @author Nils Hoffmann
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
    private double molecularWeight;
    private URI link;

    /**
     * <p>Constructor for Metabolite.</p>
     */
    public Metabolite() {
    }

    /**
     * <p>Constructor for Metabolite.</p>
     *
     * @param name1 a {@link java.lang.String} object.
     * @param id1 a {@link java.lang.String} object.
     * @param id_type1 a {@link java.lang.String} object.
     * @param dbno1 a int.
     * @param comments1 a {@link java.lang.String} object.
     * @param formula1 a {@link java.lang.String} object.
     * @param date1 a {@link java.lang.String} object.
     * @param ri1 a double.
     * @param retentionTime1 a double.
     * @param retentionTimeUnit1 a {@link java.lang.String} object.
     * @param mw1 a int.
     * @param sp1 a {@link java.lang.String} object.
     * @param shortName a {@link java.lang.String} object.
     * @param masses1 a {@link ucar.ma2.ArrayDouble.D1} object.
     * @param intensities1 a {@link ucar.ma2.ArrayInt.D1} object.
     */
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
        this.molecularWeight = mw1;
        this.sp = sp1;
        this.sname = shortName;
        setMassSpectrum(masses1, intensities1);
    }

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
    public String getComments() {
        return this.comments;
    }

    /** {@inheritDoc} */
    @Override
    public String getDate() {
        return this.date;
    }

    /**
     * <p>getDBNO.</p>
     *
     * @return a int.
     */
    public int getDBNO() {
        return this.dbno;
    }

    /** {@inheritDoc} */
    @Override
    public String getFormula() {
        return this.formula;
    }

    /** {@inheritDoc} */
    @Override
    public String getID() {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public URI getLink() {
        return this.link;
    }

    /**
     * <p>getIDType.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIDType() {
        return this.id_type;
    }

    /** {@inheritDoc} */
    @Override
    public Tuple2D<D1, ucar.ma2.ArrayInt.D1> getMassSpectrum() {
        return new Tuple2D<>(this.masses,
                this.intensities);
    }

    /** {@inheritDoc} */
    @Override
    public double getMaxIntensity() {
        return this.max_intensity;
    }

    /** {@inheritDoc} */
    @Override
    public double getMaxMass() {
        return this.max_mass;
    }

    /** {@inheritDoc} */
    @Override
    public double getMinIntensity() {
        return this.min_intensity;
    }

    /** {@inheritDoc} */
    @Override
    public double getMinMass() {
        return this.min_mass;
    }

    /** {@inheritDoc} */
    @Override
    public int getMW() {
        return this.mw;
    }

    /** {@inheritDoc} */
    @Override
    public double getMw() {
        return this.molecularWeight;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return this.name;
    }

    /** {@inheritDoc} */
    @Override
    public double getRetentionIndex() {
        return this.ri;
    }

    /** {@inheritDoc} */
    @Override
    public double getRetentionTime() {
        return this.retentionTime;
    }

    /** {@inheritDoc} */
    @Override
    public String getRetentionTimeUnit() {
        return this.retentionTimeUnit;
    }

    /** {@inheritDoc} */
    @Override
    public int getScanIndex() {
        return this.scanIndex;
    }

    /** {@inheritDoc} */
    @Override
    public String getShortName() {
        return this.sname;
    }

    /** {@inheritDoc} */
    @Override
    public String getSP() {
        return this.sp;
    }

    /** {@inheritDoc} */
    @Override
    public void setComments(final String comments1) {
        this.comments = comments1;
    }

    /** {@inheritDoc} */
    @Override
    public void setDate(final String date1) {
        this.date = date1;
    }

    /** {@inheritDoc} */
    @Override
    public void setFormula(final String formula1) {
        this.formula = formula1;
    }

    /** {@inheritDoc} */
    @Override
    public void setID(final String id1) {
        this.id = id1;
    }

    /** {@inheritDoc} */
    @Override
    public void setLink(final URI link) {
        this.link = link;
    }

    /** {@inheritDoc} */
    @Override
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
            // log.info(mav);
            // final MinMax m = MAMath.getMinMax(this.intensities);
            // this.min_intensity_norm = m.min;
            // this.max_intensity_norm = m.max;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setMaxIntensity(final double intens) {
        this.max_intensity = intens;
    }

    /** {@inheritDoc} */
    @Override
    public void setMaxMass(final double m) {
        this.max_mass = m;
    }

    /** {@inheritDoc} */
    @Override
    public void setMinIntensity(final double intens) {
        this.min_intensity = intens;
    }

    /** {@inheritDoc} */
    @Override
    public void setMinMass(final double m) {
        this.min_mass = m;
    }

    /** {@inheritDoc} */
    @Override
    public void setMW(final int mw1) {
        this.mw = mw1;
        this.molecularWeight = mw1;
    }

    /** {@inheritDoc} */
    @Override
    public void setMw(final double mw1) {
        this.molecularWeight = mw1;
        this.mw = (int) mw1;
    }

    /** {@inheritDoc} */
    @Override
    public void setName(final String s) {
        this.name = s;
    }

    /** {@inheritDoc} */
    @Override
    public void setRetentionIndex(final double d) {
        this.ri = d;
    }

    /** {@inheritDoc} */
    @Override
    public void setRetentionTime(final double d) {
        this.retentionTime = d;
    }

    /** {@inheritDoc} */
    @Override
    public void setRetentionTimeUnit(final String s) {
        this.retentionTimeUnit = s;
    }

    /** {@inheritDoc} */
    @Override
    public void setScanIndex(final int scan) {
        this.scanIndex = scan;
    }

    /** {@inheritDoc} */
    @Override
    public void setShortName(final String sname1) {
        this.sname = sname1;
    }

    /** {@inheritDoc} */
    @Override
    public void setSP(final String sp1) {
        this.sp = sp1;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("Name: " + getName());
        sb.append("\n");
        sb.append("Synon: DATE:" + getDate());
        sb.append("\n");
        sb.append("Synon: NAME:" + getShortName());
        sb.append("\n");
        if (link != null) {
            sb.append("Synon: LINK:" + getLink());
            sb.append("\n");
        }
        sb.append("Synon: SP:" + getSP());
        sb.append("\n");
        sb.append("Synon: " + getIDType() + ":" + getID());
        sb.append("\n");
        sb.append("Synon: RI:" + getRetentionIndex());
        sb.append("\n");
        if (getRetentionTime() > 0) {
            sb.append("Synon: RT:" + getRetentionTimeUnit()
                    + getRetentionTime());
            sb.append("\n");
        }
        if (getMw() >= 0) {
            sb.append("Synon: MW:" + getMw());
            sb.append("\n");
        }
        sb.append("Comments: " + getComments());
        sb.append("\n");
        if (getFormula() != null) {
            sb.append("Formula: " + getFormula());
            sb.append("\n");
        }
        if (getMW() >= 0) {
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

    /** {@inheritDoc} */
    @Override
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

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(id);
        out.writeUTF(id_type);
        out.writeInt(dbno);
        out.writeUTF(name);
        out.writeUTF(sname);
        out.writeUTF(comments);
        out.writeUTF(date);
        out.writeUTF(sp);
        out.writeUTF(formula);
        out.writeInt(mw);
        out.writeDouble(molecularWeight);
        out.writeInt(scanIndex);
        out.writeDouble(retentionTime);
        out.writeUTF(retentionTimeUnit);
        out.writeDouble(ri);
        out.writeObject(link);
        out.writeDouble(min_mass);
        out.writeDouble(max_mass);
        out.writeDouble(min_intensity);
        out.writeDouble(max_intensity);
        out.writeDouble(min_intensity_norm);
        out.writeDouble(max_intensity_norm);
        if (masses != null) {
            out.writeObject(new SerializableArray(masses));
        } else {
            out.writeObject(new NullArray());
        }
        if (intensities != null) {
            out.writeObject(new SerializableArray(intensities));
        } else {
            out.writeObject(new NullArray());
        }
    }

    private final class NullArray implements Serializable {

    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        id = in.readUTF();
        id_type = in.readUTF();
        dbno = in.readInt();
        name = in.readUTF();
        sname = in.readUTF();
        comments = in.readUTF();
        date = in.readUTF();
        sp = in.readUTF();
        formula = in.readUTF();
        mw = in.readInt();
        molecularWeight = in.readDouble();
        scanIndex = in.readInt();
        retentionTime = in.readDouble();
        retentionTimeUnit = in.readUTF();
        ri = in.readDouble();
        link = (URI) in.readObject();
        min_mass = in.readDouble();
        max_mass = in.readDouble();
        min_intensity = in.readDouble();
        max_intensity = in.readDouble();
        min_intensity_norm = in.readDouble();
        max_intensity_norm = in.readDouble();
        Object mobj = in.readObject();
        if (mobj != null) {
            if (!(mobj instanceof NullArray)) {
                masses = (ArrayDouble.D1) ((SerializableArray) mobj).getArray();
            }
        }
        Object iobj = in.readObject();
        if (iobj != null) {
            if (!(iobj instanceof NullArray)) {
                intensities = (ArrayInt.D1) ((SerializableArray) in.readObject()).getArray();
            }
        }
    }
}
