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
package maltcms.datastructures.peak;

import cross.datastructures.tuple.Tuple2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.ms.IMetabolite;
import maltcms.datastructures.ms.Metabolite;

/**
 * Dataholder for all important information about a peak.
 *
 * @author Mathias Wilhelm
 * 
 */
@Slf4j
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
    private List<Tuple2D<Double, IMetabolite>> identification = new ArrayList<>();
    private Peak2D reference = null;

    /**
     * Default constructor.
     */
    public Peak2D() {
        super();
        this.identification = new ArrayList<>();
        final ArrayList<Tuple2D<Double, IMetabolite>> hitsD = new ArrayList<>();
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
     * {@inheritDoc}
     *
     * Getter.
     */
    @Override
    public String getName() {
        if ((this.identification != null) && (this.identification.size() > 0)) {
            return this.identification.get(0).getSecond().getID();
        }

        return "Wut?";
    }

    /**
     * Getter.
     *
     * @return list of score an {@link maltcms.datastructures.ms.IMetabolite}.
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
     * @param pa a {@link maltcms.datastructures.peak.PeakArea2D} object.
     */
    public void setPeakArea(final PeakArea2D pa) {
        this.peakArea = pa;
    }

    /**
     * Setter.
     *
     * @param nFirstRetTime first retention time
     */
    public void setFirstRetTime(final double nFirstRetTime) {
        this.firstRetTime = nFirstRetTime;
    }

    /**
     * Setter.
     *
     * @param nIndex internal index
     */
    public void setIndex(final int nIndex) {
        this.index = nIndex;
    }

    /**
     * Setter.
     *
     * @param l list of score an {@link maltcms.datastructures.ms.IMetabolite}.
     */
    public void setNames(final List<Tuple2D<Double, IMetabolite>> l) {
        this.identification = l;
    }

    /**
     * Setter.
     *
     * @param nScanIndex scan index
     */
    public void setScanIndex(final int nScanIndex) {
        this.scanIndex = nScanIndex;
    }

    /**
     * Setter.
     *
     * @param nSecondRetTime second retention time
     */
    public void setSecondRetTime(final double nSecondRetTime) {
        this.secondRetTime = nSecondRetTime;
    }

    /**
     * <p>getFirstScanIndex.</p>
     *
     * @return a int.
     */
    public int getFirstScanIndex() {
        return this.peakArea.getSeedPoint().x;
    }

    /**
     * <p>getSecondScanIndex.</p>
     *
     * @return a int.
     */
    public int getSecondScanIndex() {
        return this.peakArea.getSeedPoint().y;
    }

    /**
     * <p>normalizeTo.</p>
     *
     * @param reference a {@link maltcms.datastructures.peak.Peak2D} object.
     */
    public void normalizeTo(Peak2D reference) {
        this.reference = reference;
        this.peakArea.normalizeTo(reference);
    }

    /**
     * <p>Getter for the field <code>reference</code>.</p>
     *
     * @return a {@link maltcms.datastructures.peak.Peak2D} object.
     */
    public Peak2D getReference() {
        return this.reference;
    }

    /** {@inheritDoc} */
    @Override
    public double getArea() {
        if (this.peakArea == null) {
            return Double.NaN;
        }
        return this.peakArea.getAreaIntensity();
    }

    /** {@inheritDoc} */
    @Override
    public void setArea(double d) {
        throw new UnsupportedOperationException(
                "Not supported in Peak2D. Please use the PeakArea Object to do this.");
    }

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {
        Peak2D p = new Peak2D();
        p.setApexIndex(50);
        p.setStartIndex(30);
        p.setStopIndex(80);
        p.setFirstRetTime(380);
        p.setSecondRetTime(3.12);
        log.info("ApexIndex: {}",p.getFeature("ApexIndex"));
        log.info("StartIndex: {}",p.getFeature("StartIndex"));
        log.info("StopIndex: {}",p.getFeature("StopIndex"));
        log.info("FirstRetTime: {}",p.getFeature("FirstRetTime"));
        log.info("SecondRetTime: {}",p.getFeature("SecondRetTime"));
        // log.info(p.getFeature("PeakArea"));
    }
}
