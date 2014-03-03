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
package maltcms.commands.fragments.warp;

import cross.Factory;
import cross.annotations.Configurable;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FragmentTools;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.alignment.AnchorPairSet;
import maltcms.datastructures.ms.IAnchor;
import maltcms.datastructures.warp.MZIWarpInput;
import maltcms.tools.ArrayTools;
import maltcms.tools.MaltcmsTools;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.MAMath;
import ucar.nc2.Dimension;

/**
 * Instead of warping one time series to the time scale of another one, warp
 * both to their common time, given by the steps of the alignment map.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class PathWarp extends AFragmentCommand {

    private final String description = "Warps binned mass spectra according to a given alignment map. Merges aligned mass spectra to mean mass spectra.";
    private final WorkflowSlot workflowSlot = WorkflowSlot.WARPING;
    @Configurable(name = "var.total_intensity", value = "total_intensity")
    private String total_intensity = "total_intensity";
    @Configurable(name = "var.anchors.retention_scans",
        value = "retention_scans")
    private String riname = "retention_scans";
    @Configurable(name = "var.anchors.retention_index_names",
        value = "retention_index_names")
    private String rinamename = "retention_index_names";
    @Configurable(name = "var.scan_index", value = "scan_index")
    private String indexVar = "scan_index";
    @Configurable(name = "var.mass_values", value = "mass_values")
    private String massValuesVar = "mass_values";
    @Configurable(name = "var.intensity_values", value = "intensity_values")
    private String intensValuesVar = "intensity_values";
    @Configurable
    private boolean average = false;
    @Configurable(
        name = "maltcms.datastructures.alignment.DefaultPairSet.minScansBetweenAnchors",
        value = "5")
    private int minScansBetweenAnchors = 5;

    /**
     *
     * @param m
     * @param tf
     * @return
     */
    public IFileFragment apply(final MZIWarpInput m, final IFileFragment tf) {
        processIndexedArrays(m, tf);
        processArrays(m, tf);
        final int qlen = m.getQueryFileFragment().getChild(this.total_intensity).
            getArray().getShape()[0];
        final int rlen = m.getReferenceFileFragment().getChild(
            this.total_intensity).getArray().getShape()[0];
        processRIs(m, tf, rlen, qlen, this.minScansBetweenAnchors);
        return tf;
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.commands.ICommand#apply(java.lang.Object)
     */
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        final TupleND<IFileFragment> ret = new TupleND<IFileFragment>();
        for (final IFileFragment ff : t) {
            final MZIWarpInput mwi = new MZIWarpInput(ff, getWorkflow());
            IFileFragment tf = new FileFragment(getWorkflow()
                .getOutputDirectory(this), ff.getName());
            tf.addSourceFile(ff);
            tf = apply(mwi, tf);
            log.debug("{}", tf.toString());
            log.debug("Path AWarp saving to {}", tf.getName());
            final DefaultWorkflowResult dwr = new DefaultWorkflowResult(tf.getUri(), this, getWorkflowSlot(), ff);
            getWorkflow().append(dwr);
            tf.save();
            ret.add(tf);
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.commands.fragments.AFragmentCommand#configure(org.apache.commons
     * .configuration.Configuration)
     */
    @Override
    public void configure(final Configuration cfg) {
        super.configure(cfg);
        this.total_intensity = cfg.getString("var.total_intensity",
            "total_intensity");
        this.riname = cfg.getString("var.anchors.retention_scans",
            "retention_scans");
        this.riname = cfg.getString("var.anchors.retention_scans",
            "retentions_scans");
        this.rinamename = cfg.getString("var.anchors.retention_index_names",
            "retention_index_names");
        this.indexVar = cfg.getString("var.scan_index", "scan_index");
        this.massValuesVar = cfg.getString("var.mass_values", "mass_values");
        this.intensValuesVar = cfg.getString("var.intensity_values",
            "intensity_values");
    }

    private void processArray(final MZIWarpInput m, final IFileFragment tf,
        final String varname) {
        try {
            final Array q = m.getQueryFileFragment().getChild(varname)
                .getArray();
            final Array r = m.getReferenceFileFragment().getChild(varname)
                .getArray();
            IVariableFragment vf = null;
            if (tf.hasChild(varname)) {
                vf = tf.getChild(varname);
            } else {
                vf = new VariableFragment(tf, varname);
            }
            final Dimension srcd = m.getReferenceFileFragment().getChild(
                varname).getDimensions()[0];
            vf.setDimensions(new Dimension[]{new Dimension(srcd.getName(), m
                .getPath().size(), srcd.isShared(), srcd.isUnlimited(),
                srcd.isVariableLength())});
            final Array warped = Array.factory(r.getElementType(),
                new int[]{m.getPath().size()});
            EvalTools.eqI(r.getRank(), 1, this);
            EvalTools.eqI(warped.getRank(), 1, this);
            final Index warpedIndex = warped.getIndex();
            final Index qi = q.getIndex();
            final Index ri = r.getIndex();
            int idx = 0;
            for (final Tuple2DI t : m.getPath()) {
                // log.debug("Processing {},{} with sizes {}, {}",
                // new Object[] { t.getFirst(), t.getSecond(),
                // r.getShape()[0], q.getShape()[0] });
                qi.set(t.getSecond());
                ri.set(t.getFirst());
                warpedIndex.set(idx);
                warped.setDouble(warpedIndex, (r.getDouble(ri) + q
                    .getDouble(qi)) / 2.0d);
                idx++;
            }
            vf.setArray(warped);
        } catch (final ResourceNotAvailableException e) {
            log.warn("Could not process " + varname);
        }
    }

    /**
     *
     * @param m
     * @param tf
     */
    public void processArrays(final MZIWarpInput m, final IFileFragment tf) {
        final ArrayList<String> al = FragmentTools.getDefaultVars();
        for (final String s : al) {
            if (!s.equals(Factory.getInstance().getConfiguration().getString(
                "var.binned_mass_values"))
                && !s.equals(Factory.getInstance().getConfiguration().
                    getString("var.binned_intensity_values"))
                && !s.equals(Factory.getInstance().getConfiguration().
                    getString("var.binned_scan_index"))
                && !s.equals(Factory.getInstance().getConfiguration().
                    getString("var.scan_index"))
                && !s.equals(Factory.getInstance().getConfiguration().
                    getString("var.mass_values"))
                && !s.equals(Factory.getInstance().getConfiguration().
                    getString("var.intensity_values"))) {
                processArray(m, tf, s);

            }
        }
    }

    /**
     *
     * @param m
     * @param tf
     */
    public void processIndexedArrays(final MZIWarpInput m,
        final IFileFragment tf) {

        final IVariableFragment lhssi = m.getReferenceFileFragment().getChild(
            "scan_index");
        final IVariableFragment lhsm = m.getReferenceFileFragment().getChild(
            "mass_values");
        final IVariableFragment lhsi = m.getReferenceFileFragment().getChild(
            "intensity_values");

        final IVariableFragment rhssi = m.getQueryFileFragment().getChild(
            "scan_index");
        final IVariableFragment rhsm = m.getQueryFileFragment().getChild(
            "mass_values");
        final IVariableFragment rhsi = m.getQueryFileFragment().getChild(
            "intensity_values");

        lhsm.setIndex(lhssi);
        lhsi.setIndex(lhssi);

        rhsm.setIndex(rhssi);
        rhsi.setIndex(rhssi);

        final List<Array> lhsI = lhsi.getIndexedArray();
        final List<Array> rhsI = rhsi.getIndexedArray();
        final List<Array> lhsM = lhsm.getIndexedArray();
        final List<Array> rhsM = rhsm.getIndexedArray();
        log.debug("lhs scans: " + lhsI.size() + " rhs scans: "
            + rhsI.size());
        log.debug("lhs points: "
            + Arrays.toString(lhsi.getArray().getShape()) + " rhs points: "
            + Arrays.toString(rhsi.getArray().getShape()));
        final Tuple2D<List<Array>, List<Array>> t = ArrayTools.merge2(lhsM,
            lhsI, rhsM, rhsI, m.getPath(), this.average);
        final List<Array> mergedMasses = t.getFirst();
        final List<Array> mergedIntens = t.getSecond();
        // Update index variable
        IVariableFragment indexVar = null;
        if (tf.hasChild(this.indexVar)) {
            indexVar = tf.getChild(this.indexVar);
        } else {
            indexVar = new VariableFragment(tf, this.indexVar);
        }
        final ArrayInt.D1 index = new ArrayInt.D1(mergedMasses.size());
        int offset = 0;
        for (int i = 0; i < mergedMasses.size(); i++) {
            index.set(i, offset);
            offset += mergedMasses.get(i).getShape()[0];
        }
        indexVar.setArray(index);
        // Set all arrays
        IVariableFragment ivf1 = null, ivf2 = null;
        if (tf.hasChild(this.massValuesVar)) {
            ivf1 = tf.getChild(this.massValuesVar);
        } else {
            ivf1 = new VariableFragment(tf, this.massValuesVar);
        }
        if (tf.hasChild(this.intensValuesVar)) {
            ivf2 = tf.getChild(this.intensValuesVar);
        } else {
            ivf2 = new VariableFragment(tf, this.intensValuesVar);
        }
        ivf1.setIndex(indexVar);
        ivf2.setIndex(indexVar);

        final List<Array> warpedMasses = new ArrayList<Array>();
        for (final Array a : mergedMasses) {
            final ArrayDouble.D1 ad = new ArrayDouble.D1(a.getShape()[0]);
            MAMath.copyDouble(ad, a);
            warpedMasses.add(ad);
        }
        ivf1.setIndexedArray(warpedMasses);
        final List<Array> warpedIntensities = new ArrayList<Array>();
        for (final Array a : mergedIntens) {
            final ArrayDouble.D1 ad = new ArrayDouble.D1(a.getShape()[0]);
            MAMath.copyDouble(ad, a);
            warpedIntensities.add(ad);
        }
        ivf2.setIndexedArray(warpedIntensities);
        // }
    }

    /**
     *
     * @param m
     * @param tf
     * @param refScanNo
     * @param queryScanNo
     * @param minScansBetweenAnchors
     */
    public void processRIs(final MZIWarpInput m, final IFileFragment tf,
        final int refScanNo, final int queryScanNo,
        final int minScansBetweenAnchors) {

        try {
            final Tuple2D<List<IAnchor>, List<IAnchor>> t = MaltcmsTools
                .getAnchors(m.getReferenceFileFragment(), m
                    .getQueryFileFragment());
            final AnchorPairSet aps = new AnchorPairSet(t.getFirst(), t
                .getSecond(), refScanNo, queryScanNo,
                minScansBetweenAnchors);
            final ArrayList<Integer> ri = new ArrayList<Integer>();
            final ArrayList<String> riName = new ArrayList<String>();
            int pairindex = 0;
            final List<Tuple2D<Integer, Integer>> cscans = aps
                .getCorrespondingScans();
            // for each aligned index pair
            int idx = 0;
            for (final Tuple2DI pair : m.getPath()) {
                // log.info("Checking aligned indices: {}", pair);
                // for each pair of anchors
                Iterator<Tuple2D<IAnchor, IAnchor>> aiter = aps.iterator();
                for (int k = 0; k < pairindex; k++) {
                    aiter.next();
                }
                for (int i = pairindex; i < cscans.size(); i++) {
                    Tuple2D<IAnchor, IAnchor> currentAnchor = aiter.next();
                    // retrieve paired anchor indices
                    final Tuple2D<Integer, Integer> ps = cscans.get(i);
                    // log.info("Checking aligned indices: {}", pair);
                    // log.info("Comparing with anchors: {}", ps);
                    // if anchor positions coincide with aligned path indices,
                    // keep anchors
                    if ((ps.getFirst().intValue() == pair.getFirst().intValue())
                        && (ps.getSecond().intValue() == pair.getSecond()
                        .intValue())) {
                        log.debug("Found a match: {}", pair);
                        String name = null;
                        name = currentAnchor.getFirst().getName();
                        log.debug("Adding anchor with name: {}", name);
                        riName.add(name);
                        ri.add(Integer.valueOf(idx));
                        pairindex++;
                    }
                }
                idx++;
            }
            if (ri.size() > 0) {
                final ArrayInt.D1 target = new ArrayInt.D1(ri.size());
                int i = 0;
                for (final Integer itg : ri) {
                    target.set(i++, itg.intValue());
                }
                final ArrayChar.D2 names = cross.datastructures.tools.ArrayTools
                    .createStringArray(riName.size(), 256);
                i = 0;
                for (final String s : riName) {
                    names.setString(i++, s);
                }
                final IVariableFragment rif = new VariableFragment(tf,
                    this.riname);
                rif.setArray(target);
                final IVariableFragment rin = new VariableFragment(tf,
                    this.rinamename);
                rin.setArray(names);
            } else {
                log.warn("Did not find any aligned anchors!");
            }
        } catch (final ResourceNotAvailableException e) {
            log.warn("Could not process " + this.riname);
        } catch (final IllegalArgumentException e) {
            log.warn("Failed to process " + this.riname);
        }

    }
}
