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
package maltcms.commands.fragments2d.testing;

import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import maltcms.commands.distances.dtwng.AlignmentFactory;
import maltcms.commands.distances.dtwng.FeatureVectorDtwSimilarity;
import maltcms.commands.distances.dtwng.IAlignment;
import maltcms.commands.distances.dtwng.IOptimizationFunction;
import maltcms.commands.distances.dtwng.ThreePredecessorsOptimization;
import maltcms.commands.distances.dtwng.TwoFeatureVectorOperation;
import maltcms.datastructures.array.IFeatureVector;
import maltcms.datastructures.constraint.ConstraintFactory;
import maltcms.datastructures.feature.FeatureVectorFactory;
import maltcms.math.functions.DtwPairwiseSimilarity;
import maltcms.math.functions.similarities.ArrayCos;
import maltcms.tools.ArrayTools2;
import maltcms.tools.ImageTools;
import ucar.ma2.Array;

/**
 *
 * @author Mathias Wilhelm
 */
@RequiresVariables(names = {"var.maxms_1d_horizontal",
    "var.maxms_1d_horizontal_index", "var.maxms_1d_vertical",
    "var.maxms_1d_vertical_index", "var.total_intensity",
    "var.second_column_scan_index"})
@RequiresOptionalVariables(names = {""})
@ProvidesVariables(names = {"var.warp_path_i", "var.warp_path_j"})
//@ServiceProvider(service = AFragmentCommand.class)
public class Alignment2D extends AFragmentCommand {

    @Configurable(name = "var.maxms_1d_horizontal",
            value = "maxms_1d_horizontal")
    private String horizontalVar = "maxms_1d_horizontal";
    @Configurable(name = "var.maxms_1d_horizontal_index",
            value = "maxms_1d_horizontal_index")
    private String horizontalIndexVar = "maxms_1d_horizontal_index";
    @Configurable(name = "var.maxms_1d_vertical", value = "maxms_1d_vertical")
    private String verticalVar = "maxms_1d_vertical";
    @Configurable(name = "var.maxms_1d_vertical_index",
            value = "maxms_1d_vertical_index")
    private String verticalIndexVar = "maxms_1d_vertical_index";
    @Configurable(name = "var.total_intensity", value = "total_intensity")
    private String totalIntensity = "total_intensity";
    @Configurable(name = "var.second_column_scan_index",
            value = "second_column_scan_index")
    private String secondColumnScanIndexVar = "second_column_scan_index";
    private TwoFeatureVectorOperation dtwSimilarity;
    @Configurable(value = "false")
    private boolean filter = false;
    @Configurable(value = "false")
    private boolean scale = false;

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return getClass().getName();
    }

    /**
     *
     */
    public Alignment2D() {
        FeatureVectorDtwSimilarity sim = new FeatureVectorDtwSimilarity();
        DtwPairwiseSimilarity idsf = new DtwPairwiseSimilarity();
        idsf.setDenseMassSpectraSimilarity(new ArrayCos());
        sim.setScoreFunction(idsf);
        sim.setArrayFeatureName("FEATURE0");
        dtwSimilarity = sim;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "2D Alignment in spe";
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {

        if (t.size() < 2) {
            return t;
        }

        IFileFragment lhsff, rhsff;
        List<IFeatureVector> l1h, l1v, l2h, l2v;
        final AlignmentFactory af = new AlignmentFactory();
        IAlignment ia;
        // prepare recursion
        final IOptimizationFunction iof = new ThreePredecessorsOptimization();
        for (String s : iof.getStates()) {
            if (s.equals("NW")) {
                iof.setWeight(s, 2.25d);
            }
        }

        double cost;
        List<Array> scanlinesi, scanlinesj;
        for (int i = 0; i < t.size(); i++) {
            lhsff = t.get(i);

            scanlinesi = getScanlineFor(lhsff);

            for (int j = i + 1; j < t.size(); j++) {
                rhsff = t.get(j);

                scanlinesj = getScanlineFor(rhsff);

                // prepare alignment
                ia = af.getDTWInstance();
                // set alignment properties
                ia.setLeftHandSideId(lhsff.getName());
                ia.setRightHandSideId(rhsff.getName());
                // set recursion and pairwise function
                ia.setOptimizationFunction(iof);
                ia.setPairwiseFeatureVectorOperation(dtwSimilarity);

                // HORIZONTAL
                // prepare feature vectors
                l1h = getFeatureList(lhsff, this.horizontalVar,
                        this.horizontalIndexVar, this.filter, this.scale);
                l2h = getFeatureList(rhsff, this.horizontalVar,
                        this.horizontalIndexVar, this.filter, this.scale);
                System.out.println("l1v size: " + l1h.size());

                // set constraints
                ia.setConstraints(ConstraintFactory.getInstance().
                        createBandConstraint(0, 0, l1h.size(),
                                l2h.size(), 0.5));
                // apply and retrieve score
                cost = ia.apply(l1h, l2h);
                System.out.println("DTW score h: " + cost);
                // retrieve map
                List<Point> h = ia.getMap();

                for (Point p : h) {
                    System.out.println(p.x + " - " + p.y);
                }

                // VERTICAL
                // prepare feature vectors
                l1v = getFeatureList(lhsff, this.verticalVar,
                        this.verticalIndexVar, this.filter, this.scale);
                l2v = getFeatureList(rhsff, this.verticalVar,
                        this.verticalIndexVar, this.filter, this.scale);
                System.out.println("l1v size: " + l1v.size());

                // set constraints
                ia.setConstraints(ConstraintFactory.getInstance().
                        createBandConstraint(0, 0, l1v.size(),
                                l2v.size(), 0.5));
                // apply and retrieve score
                cost = ia.apply(l1v, l2v);
                System.out.println("DTW score v: " + cost);
                // retrieve map
                List<Point> v = ia.getMap();

                Visualization2D vis = new Visualization2D();

                final BufferedImage image = vis.createImage(scanlinesi,
                        scanlinesj, h, v);

                final String baseFilename = StringTools.removeFileExt(lhsff.
                        getName())
                        + "_vs_" + StringTools.removeFileExt(rhsff.getName());
                final String filename = baseFilename + "_rgb";
                final File out = ImageTools.saveImage(image, filename, "png",
                        getWorkflow().getOutputDirectory(this), this);

                // for (Point p : v) {
                // System.out.println(p.x + ":" + p.y);
                // }
            }
        }

        return t;
    }

    private List<IFeatureVector> getFeatureList(final IFileFragment ff,
            final String varName, final String indexName, final boolean filter,
            final boolean scale) {
        final FeatureVectorFactory fvf = FeatureVectorFactory.getInstance();
        IVariableFragment ivf = ff.getChild(varName);
        IVariableFragment si = ff.getChild(indexName);
        ivf.setIndex(si);

        List<Array> feature = ff.getChild(varName).getIndexedArray();
        if (filter) {
            final List<Integer> hold = new ArrayList<>();
            feature = ArrayTools2.filterInclude(feature, hold);
        }
        if (scale) {
            feature = ArrayTools2.sqrt(feature);
        }

        final List<IFeatureVector> featureList = fvf.createFeatureVectorList(
                feature);

        return featureList;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.ALIGNMENT;
    }

    /**
     * Getter.
     *
     * @param ff file fragment
     * @return scanlines
     */
    protected List<Array> getScanlineFor(final IFileFragment ff) {
        IVariableFragment ticVar = ff.getChild(this.totalIntensity);
        IVariableFragment si = ff.getChild(this.secondColumnScanIndexVar);
        ticVar.setIndex(si);
        final List<Array> scanlines = ff.getChild(this.totalIntensity).
                getIndexedArray();
        return scanlines;
    }
}
