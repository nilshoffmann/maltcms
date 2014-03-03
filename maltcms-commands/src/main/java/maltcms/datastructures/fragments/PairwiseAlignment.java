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
package maltcms.datastructures.fragments;

import cross.Factory;
import cross.IConfigurable;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FragmentTools;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.workflow.DefaultWorkflow;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import cross.io.IFileFragmentProvider;
import cross.tools.StringTools;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.distances.DtwRecurrence;
import maltcms.commands.distances.IRecurrence;
import maltcms.commands.distances.PairwiseFeatureSimilarity;
import maltcms.datastructures.alignment.DefaultPairSet;
import maltcms.datastructures.array.IArrayD2Double;
import maltcms.datastructures.ms.IAnchor;
import maltcms.io.csv.CSVWriter;
import maltcms.tools.PathTools;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math.analysis.interpolation.UnivariateRealInterpolator;
import org.jdom.Element;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayDouble.D0;
import ucar.ma2.ArrayDouble.D1;
import ucar.ma2.DataType;
import ucar.nc2.Dimension;

/**
 * Implementation of IFileFragmentProvider for PairwiseAlignment.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Slf4j
@ProvidesVariables(names = {"var.minimizing_array_comp"})
public class PairwiseAlignment implements IFileFragmentProvider, IConfigurable,
    IWorkflowElement {

    private IFileFragment ff;
    @Configurable(name = "alignment.save.pairwise.distance.matrix")
    private boolean savePWDM;
    @Configurable(name = "alignment.save.cumulative.distance.matrix")
    private boolean saveCDM;
    private IArrayD2Double alignment;
    private IArrayD2Double distance;
    private boolean isMinimize;
    private int refsize, querysize;
    private IRecurrence cd;
    private D0 result;
    private D1 resultVector;
    private PairwiseFeatureSimilarity pwd;
    private IFileFragment ref;
    private IFileFragment target;
    private List<Tuple2DI> path;
    private Class<?> creator;
    private List<Tuple2DI> localPathOptima;
    private DefaultPairSet<IAnchor> anchors;
    @Configurable(name = "var.alignment.cumulative_distance")
    private String cumulativeDistanceVariableName = "cumulative_distance";
    @Configurable(name = "var.alignment.pairwise_distance")
    private String pairwiseDistanceVariableName = "pairwise_distance";
    @Configurable(name = "var.minimizing_array_comp")
    private String arrayComparatorVariableName = "array_comp";
    @Configurable(name = "alignment.algorithm.distance")
    private String arrayDistanceClassName = "maltcms.commands.distances.ArrayLp";
    private IWorkflow iw;
    private ArrayByte.D2 predecessors;
    @Configurable(name = "normalizeAlignmentValueByMapWeights")
    private boolean normalizeAlignmentValueByMapWeights;
    private List<Tuple2DI> interppath;

    public List<Tuple2DI> getInterppath() {
        return interppath;
    }

    public void setInterppath(List<Tuple2DI> interppath) {
        this.interppath = interppath;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.io.misc.IXMLSerializable#appendXML(org.jdom.Element)
     */
    @Override
    public void appendXML(final Element e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void configure(final Configuration cfg) {
        this.saveCDM = cfg.getBoolean(
            "alignment.save.cumulative.distance.matrix", false);
        this.savePWDM = cfg.getBoolean(
            "alignment.save.pairwise.distance.matrix", false);
        this.cumulativeDistanceVariableName = cfg.getString(
            "var.alignment.cumulative_distance", "cumulative_distance");
        this.pairwiseDistanceVariableName = cfg.getString(
            "var.alignment.pairwise_distance", "pairwise_distance");
        this.arrayComparatorVariableName = cfg.getString(
            "var.minimizing_array_comp", "array_comp");
        this.arrayDistanceClassName = cfg.getString(
            "alignment.algorithm.distance",
            "maltcms.commands.distances.ArrayLp");
        this.normalizeAlignmentValueByMapWeights = cfg.getBoolean(this
            .getClass().getName()
            + ".normalizeAlignmentValueByMapWeights", false);
    }

    /**
     * @return the alignment
     */
    public IArrayD2Double getAlignment() {
        return this.alignment;
    }

    /**
     * @return the arrayDistanceClassName
     */
    public String getArrayDistanceClassName() {
        return this.arrayDistanceClassName;
    }

    /**
     * @return the cumulativeDistanceClass
     */
    public IRecurrence getCd() {
        return this.cd;
    }

    /**
     * @return the creator
     */
    public Class<?> getCreator() {
        return this.creator;
    }

    /**
     * @return the distance
     */
    public IArrayD2Double getDistance() {
        return this.distance;
    }

    /**
     * @return the ff
     */
    public IFileFragment getFf() {
        return this.ff;
    }

    /**
     * Retrieve paired anchors
     *
     * @return
     */
    public DefaultPairSet<IAnchor> getAnchors() {
        return anchors;
    }

    /**
     * Set paired anchors
     *
     * @param anchors
     */
    public void setAnchors(DefaultPairSet<IAnchor> anchors) {
        this.anchors = anchors;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflow()
     */
    @Override
    public IWorkflow getWorkflow() {
        return this.iw;
    }

    /**
     * @return the path
     */
    public List<Tuple2DI> getPath() {
        return this.path;
    }

    /**
     * @return the predecessors
     */
    public ArrayByte.D2 getPredecessors() {
        return this.predecessors;
    }

    /**
     * @return the pwd
     */
    public PairwiseFeatureSimilarity getPwd() {
        return this.pwd;
    }

    /**
     * @return the querysize
     */
    public int getQuerysize() {
        return this.querysize;
    }

    /**
     * @return the ref
     */
    public IFileFragment getRef() {
        return this.ref;
    }

    /**
     * @return the refsize
     */
    public int getRefsize() {
        return this.refsize;
    }

    /**
     * @return the result
     */
    public D0 getResult() {
        return this.result;
    }

    /**
     * @return the resultVector
     */
    public D1 getResultVector() {
        return this.resultVector;
    }

    /**
     * @return the target
     */
    public IFileFragment getTarget() {
        return this.target;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.ALIGNMENT;
    }

    /**
     * @return the isMinimize
     */
    public boolean isMinimize() {
        return this.isMinimize;
    }

    /**
     * @return the normalizeAlignmentValueByMapWeights
     */
    public boolean isNormalizeByMapWeights() {
        return this.normalizeAlignmentValueByMapWeights;
    }

    /**
     * @return the saveCDM
     */
    public boolean isSaveCDM() {
        return this.saveCDM;
    }

    /**
     * @return the savePWDM
     */
    public boolean isSavePWDM() {
        return this.savePWDM;
    }

    @Override
    public IFileFragment provideFileFragment() {
        if (this.ff == null) {
            long start = System.currentTimeMillis();
            if (this.iw == null) {
                this.iw = new DefaultWorkflow();
            }
            this.ff = FragmentTools.createFragment(this.ref, this.target,
                getWorkflow().getOutputDirectory(this));
            final PathTools pt = Factory.getInstance().getObjectFactory()
                .instantiate(PathTools.class);
            long time = start;
            if (this.path == null) {
                EvalTools.notNull(new Object[]{this.ff, this.alignment,
                    this.distance, this.isMinimize, this.cd}, this);
                // path = pt.makeMap(ff, this.alignment, this.distance,
                // this.isMinimize, this.cd);
                this.path = pt.traceback(this.predecessors, this.ref,
                    this.target);
                // this.interppath = interpolatePath(this.path, this.alignment);
                time = System.currentTimeMillis() - start;
                log.info("Calculated traceback in {} milliseconds", time);
                start = System.currentTimeMillis();
                pt.savePathCSV(this.ff, this.alignment, this.distance,
                    this.path, getWorkflow(), isMinimize());
                pt.decorate(this.ff, this.distance);
            } else {
                PathTools.getFragments(this.ff, this.path, this.distance);
            }
            List<List<String>> rows = new ArrayList<List<String>>();

            List<String> row = new ArrayList<String>();
            row.add(this.ref.getName());
            row.add(this.target.getName());
            rows.add(row);
            CSVWriter csvw = Factory.getInstance().getObjectFactory()
                .instantiate(CSVWriter.class);
            csvw.setWorkflow(getWorkflow());
            csvw.writeTableByRows(getWorkflow().getOutputDirectory(this)
                .getAbsolutePath(), StringTools.removeFileExt(this.ff
                    .getName())
                + "_names.txt", rows, WorkflowSlot.ALIGNMENT);
            if (this.anchors != null) {
//                row.add(this.ref.getName());
//                row.add(this.target.getName());
//                rows.add(row);
                for (Tuple2D<IAnchor, IAnchor> t : this.anchors) {
                    row = new ArrayList<String>();
                    row.add(t.getFirst().getScanIndex() + "");
                    row.add(t.getSecond().getScanIndex() + "");
                    rows.add(row);
                }
                CSVWriter csvw2 = Factory.getInstance().getObjectFactory()
                    .instantiate(CSVWriter.class);
                csvw2.setWorkflow(getWorkflow());
                csvw2.writeTableByRows(getWorkflow().getOutputDirectory(this)
                    .getAbsolutePath(), StringTools.removeFileExt(this.ff
                        .getName())
                    + "_anchors.csv", rows, WorkflowSlot.ALIGNMENT);
            }
            final double expw = pt.getNexp()
                * this.pwd.getSimilarityFunction().getExpansionWeight();
            final double compw = pt.getNcomp()
                * this.pwd.getSimilarityFunction().getCompressionWeight();
            final double diagw = pt.getNdiag()
                * this.pwd.getSimilarityFunction().getMatchWeight();
            final double gapPenaltiesW = (pt.getNexp() + pt.getNcomp())
                * this.cd.getGlobalGapPenalty();
            // log.info("Alignment arrayDistanceClassName: {}",
            // this.alignment.get(this.alignment
            // .rows() - 1, this.alignment.columns() - 1)
            // / path.size());
            final int maplength = this.path.size();

            if (this.saveCDM) {
                final IVariableFragment vf = new VariableFragment(this.ff,
                    this.cumulativeDistanceVariableName, null);
                vf.setDimensions(new Dimension[]{
                    new Dimension("reference_scan", this.refsize, true,
                    false, false),
                    new Dimension("query_scan", this.querysize, true,
                    false, false)});
                vf.setDataType(DataType.DOUBLE);
                vf.setArray(this.alignment.getArray());
                // CSVWriter csvw = new CSVWriter();
                // csvw.write(new File(target.getAbsolutePath()).getParent(),
                // target.getName()+"_cdist.csv", this.alignment);
            }
            if (this.savePWDM) {
                final IVariableFragment vf = new VariableFragment(this.ff,
                    this.pairwiseDistanceVariableName, null);
                vf.setDimensions(new Dimension[]{
                    new Dimension("reference_scan", this.refsize, true,
                    false, false),
                    new Dimension("query_scan", this.querysize, true,
                    false, false)});
                vf.setDataType(DataType.DOUBLE);
                vf.setArray(this.distance.getArray());
                // CSVWriter csvw = new CSVWriter();
                // csvw.write(new File(target.getAbsolutePath()).getParent(),
                // target.getName()+"_pwdist.csv", this.distance);
            }
            String arrayComparatorVariableName = Factory.getInstance()
                .getConfiguration().getString(
                    "var.alignment.pairwise_distance.class",
                    "pairwise_distance_class");
            String arrayDistanceClassName = Factory.getInstance()
                .getConfiguration().getString(
                    "var.alignment.cumulative_distance.class",
                    "cumulative_distance_class");
            String alignmentClassVariableName = Factory.getInstance()
                .getConfiguration().getString("var.alignment.class",
                    "alignment_class");
            String alignmentClassName = "maltcms.commands.distances.dtw.MZIDynamicTimeWarp";
            FragmentTools.createString(this.ff, arrayComparatorVariableName,
                this.pwd.getSimilarityFunction().getClass().getName());
            FragmentTools.createString(this.ff, arrayDistanceClassName, this.cd
                .getClass().getName());
            FragmentTools.createString(this.ff, alignmentClassVariableName,
                alignmentClassName);
            if (this.result == null) {
                this.result = new ArrayDouble.D0();
                double distance1 = this.alignment
                    .get(this.alignment.rows() - 1, this.alignment
                        .columns() - 1);
                if (this.normalizeAlignmentValueByMapWeights) {
                    distance1 = (distance1 - gapPenaltiesW)
                        / (expw + compw + diagw);
                    log.info(
                        "Alignment value normalized by path weights: {}",
                        distance1);
                }
                this.result.set(distance1);
            }
            final String distvar = Factory.getInstance().getConfiguration()
                .getString("var.alignment.distance", "distance");
            final IVariableFragment dvar = new VariableFragment(this.ff,
                distvar);
            dvar.setArray(this.result);

            this.resultVector = new ArrayDouble.D1(1);
            this.resultVector.set(0, this.result.get());
            time = System.currentTimeMillis() - start;
            log.debug("Set Variables on {} in {} milliseconds", this.ff
                .getName(), time);
        }
        return this.ff;
    }

    private List<Tuple2DI> interpolatePath(List<Tuple2DI> path,
        IArrayD2Double alignment) {
        List<Tuple2DI> interp = new ArrayList<Tuple2DI>();
        List<Tuple2DI> strictlyIncreasingPoints = new ArrayList<Tuple2DI>();
        try {
            Tuple2DI p = path.get(0);// start point
            strictlyIncreasingPoints.add(p);

            for (int i = 0; i < path.size() - 1; i++) {
                Tuple2DI q = path.get(i);
                if (q.getFirst() > p.getFirst()
                    && q.getSecond() > p.getSecond()) {
                    if (i < 10) {
                        log.info("Adding q={}", q);
                    }
                    strictlyIncreasingPoints.add(q);
                    p = q;
                }

            }
            p = strictlyIncreasingPoints
                .get(strictlyIncreasingPoints.size() - 1);
            Tuple2DI q = new Tuple2DI(alignment.rows() - 1,
                alignment.columns() - 1);
            if (q.getFirst() > p.getFirst() && q.getSecond() > p.getSecond()) {
                strictlyIncreasingPoints.add(q);
            } else {
                strictlyIncreasingPoints
                    .remove(strictlyIncreasingPoints.size() - 1);
                strictlyIncreasingPoints.add(q);
            }
            log.info("Number of Surviving Points: {}",
                strictlyIncreasingPoints.size());
            double[] x = new double[strictlyIncreasingPoints.size()];
            double[] y = new double[strictlyIncreasingPoints.size()];
            for (int i = 0; i < strictlyIncreasingPoints.size(); i++) {
                x[i] = strictlyIncreasingPoints.get(i).getFirst();
                y[i] = strictlyIncreasingPoints.get(i).getSecond();
            }
            log.info("x = {}", Arrays.toString(Arrays
                .copyOfRange(x, 0, 10)));
            log.info("y = {}", Arrays.toString(Arrays
                .copyOfRange(y, 0, 10)));
            UnivariateRealInterpolator interpolator = new SplineInterpolator();
            UnivariateRealFunction function = interpolator.interpolate(x, y);

            for (int i = 0; i < alignment.rows(); i += 10) {
                Tuple2DI ip = new Tuple2DI(i, (int) (Math.round(function
                    .value((double) i))));
                interp.add(ip);
            }
            if (interp.get(interp.size() - 1).getFirst() != alignment.columns() - 1) {
                Tuple2DI ip = new Tuple2DI(alignment.columns() - 1,
                    (int) (Math.round(function.value((double) alignment
                            .columns() - 1))));
                interp.add(ip);
            }
        } catch (MathException e) {
            log.error(e.getLocalizedMessage());
        }
        return interp;
    }

    public void setAlignment(final IArrayD2Double al) {
        // ArrayDouble.D2 d2 = new ArrayDouble.D2(al.columns(),al.rows());
        // for(int i=0;i<al.rows();i++) {
        // for(int j=0;j<al.columns();j++) {
        // d2.set(j, i, al.get(i, j));
        // }
        // }
        this.alignment = al;
    }

    /**
     * @param arrayDistanceClassName the arrayDistanceClassName to set
     */
    public void setArrayDistanceClassName(final String arrayDistanceClassName) {
        this.arrayDistanceClassName = arrayDistanceClassName;
    }

    /**
     * @param cumulativeDistanceClass the cumulativeDistanceClass to set
     */
    public void setCd(final IRecurrence cd) {
        this.cd = cd;
    }

    /**
     * @param creator the creator to set
     */
    public void setCreator(final Class<?> creator) {
        this.creator = creator;
    }

    public void setCumulativeDistance(final DtwRecurrence cd1) {
        this.cd = cd1;
    }

    /**
     * @param distance the distance to set
     */
    public void setDistance(final IArrayD2Double distance) {
        this.distance = distance;
    }

    /**
     * @param ff the ff to set
     */
    public void setFf(final IFileFragment ff) {
        this.ff = ff;
    }

    public void setFileFragments(final IFileFragment ref1,
        final IFileFragment target1, final Class<?> creator1) {
        this.ref = ref1;
        this.target = target1;
        this.creator = creator1;
    }

    public void setIsMinimizing(final boolean b) {
        this.isMinimize = b;
    }

    /*
     * (non-Javadoc)
     *
     * @seecross.datastructures.workflow.IWorkflowElement#setWorkflow(cross.
     * datastructures.workflow.IWorkflow)
     */
    @Override
    public void setWorkflow(final IWorkflow iw1) {
        this.iw = iw1;

    }

    /**
     * @param isMinimize the isMinimize to set
     */
    public void setMinimize(final boolean isMinimize) {
        this.isMinimize = isMinimize;
    }

    /**
     * @param normalizeAlignmentValueByMapWeights the
     *                                            normalizeAlignmentValueByMapWeights to set
     */
    public void setNormalizeByMapLength(final boolean normalizeByMapWeights) {
        this.normalizeAlignmentValueByMapWeights = normalizeByMapWeights;
    }

    public void setNumberOfScansQuery(final int n) {
        this.querysize = n;
    }

    public void setNumberOfScansReference(final int n) {
        this.refsize = n;
    }

    public void setPairwiseDistance(final PairwiseFeatureSimilarity pwd1) {
        this.pwd = pwd1;
    }

    public void setPairwiseDistances(final IArrayD2Double pwd1) {
        // ArrayDouble.D2 d2 = new ArrayDouble.D2(pwd1.columns(),pwd1.rows());
        // for(int i=0;i<pwd1.rows();i++) {
        // for(int j=0;j<pwd1.columns();j++) {
        // d2.set(j, i, pwd1.get(i, j));
        // }
        // }
        this.distance = pwd1;
    }

    public void setPath(final List<Tuple2DI> path1) {
        this.path = path1;
    }

    /**
     * @param predecessors the predecessors to set
     */
    public void setPredecessors(final ArrayByte.D2 predecessors) {
        this.predecessors = predecessors;
    }

    /**
     * @param pwd the pwd to set
     */
    public void setPwd(final PairwiseFeatureSimilarity pwd) {
        this.pwd = pwd;
    }

    /**
     * @param querysize the querysize to set
     */
    public void setQuerysize(final int querysize) {
        this.querysize = querysize;
    }

    /**
     * @param ref the ref to set
     */
    public void setRef(final IFileFragment ref) {
        this.ref = ref;
    }

    /**
     * @param refsize the refsize to set
     */
    public void setRefsize(final int refsize) {
        this.refsize = refsize;
    }

    public void setResult(final double d) {
        this.result = new ArrayDouble.D0();
        this.result.set(d);
    }

    /**
     * @param saveCDM the saveCDM to set
     */
    public void setSaveCDM(final boolean saveCDM) {
        this.saveCDM = saveCDM;
    }

    /**
     * @param savePWDM the savePWDM to set
     */
    public void setSavePWDM(final boolean savePWDM) {
        this.savePWDM = savePWDM;
    }

    /**
     * @param target the target to set
     */
    public void setTarget(final IFileFragment target) {
        this.target = target;
    }

    public void setTraceMatrix(final ArrayByte.D2 predecessors1) {
        this.predecessors = predecessors1;
    }
}
