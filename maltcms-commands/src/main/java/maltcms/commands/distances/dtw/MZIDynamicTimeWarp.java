/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
package maltcms.commands.distances.dtw;

import cross.annotations.Configurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.distances.PairwiseFeatureSequenceSimilarity;
import maltcms.tools.MaltcmsTools;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;

/**
 * Implementation of Pairwise Dynamic-Time-Warping for time-series data with an
 * evenly gridded array of mass over charge (mz) vs intensity for each
 * time-point (scan).
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Slf4j
@Data
@ServiceProvider(service = PairwiseFeatureSequenceSimilarity.class)
public class MZIDynamicTimeWarp extends ADynamicTimeWarp {

    @Configurable
    private int numberOfEICsToSelect = 0;
    @Configurable
    private boolean useSparseArrays = false;
    @Configurable(name = "var.mass_values")
    protected String mass_values = "mass_values";
    @Configurable(name = "var.intensity_values")
    protected String intensity_values = "intensity_values";
    @Configurable(name = "var.mass_range_min")
    protected String mass_range_min = "mass_range_min";
    @Configurable(name = "var.mass_range_max")
    protected String mass_range_max = "mass_range_max";
    @Configurable(name = "var.scan_index")
    protected String scan_index = "scan_index";
    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.commands.distances.dtw.ADynamicTimeWarp#configure(org.apache.
     * commons.configuration.Configuration)
     */

    @Override
    public void configure(final Configuration cfg) {
        super.configure(cfg);
        this.mass_values = cfg.getString("var.mass_values", "mass_values");
        this.intensity_values = cfg.getString("var.intensity_values",
            "intensity_values");
        this.mass_range_min = cfg.getString("var.mass_range_min",
            "mass_range_min");
        this.mass_range_max = cfg.getString("var.mass_range_max",
            "mass_range_max");
        this.scan_index = cfg.getString("var.scan_index", "scan_index");
    }

    @Override
    public Tuple2D<List<Array>, List<Array>> createTuple(
        final Tuple2D<IFileFragment, IFileFragment> t) {
        List<Array> intens1 = null;
        List<Array> intens2 = null;
        if (this.useSparseArrays) {
            final Tuple2D<Double, Double> tple = MaltcmsTools.getMinMaxMassRange(t.
                getFirst(), t.getSecond());
            synchronized (t.getFirst()) {
                intens1 = MaltcmsTools.prepareSparseMZI(t.getFirst(),
                    this.scan_index, this.mass_values,
                    this.intensity_values, tple.getFirst(), tple.getSecond());
            }
            synchronized (t.getSecond()) {
                intens2 = MaltcmsTools.prepareSparseMZI(t.getSecond(),
                    this.scan_index, this.mass_values,
                    this.intensity_values, tple.getFirst(), tple.getSecond());
            }
        } else {
            synchronized (t.getFirst()) {
                final IVariableFragment index1 = t.getFirst().getChild(
                    "binned_" + this.scan_index);
                final IVariableFragment binnedMassValues1 = t.getFirst().getChild("binned_" + this.mass_values);
                final IVariableFragment binnedIntensityValues1 = t.getFirst().getChild("binned_" + this.intensity_values);
                if (binnedMassValues1.getIndex() == null) {
                    binnedMassValues1.setIndex(
                        index1);
                    binnedIntensityValues1.
                        setIndex(index1);
                }
                intens1 = binnedIntensityValues1.getIndexedArray();
                EvalTools.notNull(intens1, this);
            }
            synchronized (t.getSecond()) {
                final IVariableFragment index2 = t.getSecond().getChild(
                    "binned_" + this.scan_index);
                final IVariableFragment binnedMassValues2 = t.getSecond().getChild("binned_" + this.mass_values);
                final IVariableFragment binnedIntensityValues2 = t.getSecond().getChild("binned_" + this.intensity_values);
                if (binnedMassValues2.getIndex() == null) {
                    binnedMassValues2.setIndex(
                        index2);
                    binnedIntensityValues2.
                        setIndex(index2);
                }
                intens2 = binnedIntensityValues2.getIndexedArray();
                EvalTools.notNull(intens2, this);
            }
        }

        Tuple2D<List<Array>, List<Array>> tuple = null;
        // If set to a number > 0, try to select as many eics according to their
        // rank by variance (decreasing)
        if (this.numberOfEICsToSelect > 0) {
            log.info("Using {} eics with highest variance",
                this.numberOfEICsToSelect);
            List<Tuple2D<Double, Double>> eics1 = null;
            synchronized (t.getFirst()) {
                eics1 = MaltcmsTools.rankEICsByVariance(t.getFirst(), intens1,
                    this.numberOfEICsToSelect, this.getClass(),
                    getWorkflow().getOutputDirectory(this));
            }
            List<Tuple2D<Double, Double>> eics2 = null;
            synchronized (t.getSecond()) {
                eics2 = MaltcmsTools.rankEICsByVariance(t.getSecond(), intens2,
                    this.numberOfEICsToSelect, this.getClass(),
                    getWorkflow().getOutputDirectory(this));
            }
            final Integer[] pairedEics = MaltcmsTools.pairedEICs(eics1, eics2);
            tuple = new Tuple2D<List<Array>, List<Array>>(MaltcmsTools.copyEics(
                intens1, pairedEics), MaltcmsTools.copyEics(
                    intens2, pairedEics));

        } else {// if set to 0, simply use all eics
            log.info("Using all eics");
            tuple = new Tuple2D<List<Array>, List<Array>>(intens1, intens2);
        }

        this.ref_num_scans = intens1.size();
        this.query_num_scans = intens2.size();
        return tuple;
    }
}
