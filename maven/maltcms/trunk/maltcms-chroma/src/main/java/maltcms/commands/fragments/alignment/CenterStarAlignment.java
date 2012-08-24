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
package maltcms.commands.fragments.alignment;

import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import maltcms.commands.fragments.warp.ChromatogramWarp;
import maltcms.datastructures.alignment.AlignmentFactory;
import maltcms.datastructures.peak.Peak;
import maltcms.io.csv.CSVWriter;
import maltcms.io.xml.bindings.alignment.Alignment;
import maltcms.tools.MaltcmsTools;

import org.apache.commons.configuration.Configuration;
import java.util.Arrays;

import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import cross.Factory;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.datastructures.tools.FragmentTools;
import cross.tools.MathTools;
import cross.tools.StringTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the center star approximation for multiple alignment.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@RequiresVariables(names = {"var.minimizing_array_comp",
    "var.pairwise_distance_matrix", "var.pairwise_distance_names"})
@ProvidesVariables(names = {"var.multiple_alignment",
    "var.multiple_alignment_names", "var.multiple_alignment_type",
    "var.multiple_alignment_creator"})
@Data
@Slf4j
@ServiceProvider(service=AFragmentCommand.class)
public class CenterStarAlignment extends AFragmentCommand {

    @Configurable(name = "var.pairwise_distance_matrix",
    value = "pairwise_distance_matrix")
    private String pairwiseDistanceMatrixVariableName = "pairwise_distance_matrix";
    @Configurable(name = "var.pairwise_distance_names",
    value = "pairwise_distance_names")
    private String pairwiseDistanceNamesVariableName = "pairwise_distance_names";
    @Configurable(name = "var.minimizing_array_comp",
    value = "minimizing_array_comp")
    private String minimizingArrayCompVariableName = "minimizing_array_comp";
    @Configurable(name = "var.multiple_alignment")
    private String multipleAlignmentVariableName = "multiple_alignment";
    @Configurable(name = "var.multiple_alignment_names")
    private String multipleAlignmentNamesVariableName = "multiple_alignment_names";
    @Configurable(name = "var.multiple_alignment_type")
    private String multipleAlignmentTypeVariableName = "multiple_alignment_type";
    @Configurable(name = "var.multiple_alignment_creator")
    private String multipleAlignmentCreatorVariableName = "multiple_alignment_creator";
    private boolean minimizeDist;
    @Configurable(value = "false")
    private boolean alignToFirst = false;
    @Configurable
    private String centerSequence = "";

    @Override
    public String toString() {
        return getClass().getName();
    }
    
    /*
     * (non-Javadoc)
     *
     * @see cross.commands.ICommand#apply(java.lang.Object)
     */
    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        final IFileFragment pwd = MaltcmsTools.getPairwiseDistanceFragment(t);
        final ArrayDouble.D2 pwdist = (ArrayDouble.D2) pwd.getChild(
                this.pairwiseDistanceMatrixVariableName).getArray();
        final ArrayChar.D2 names1 = (ArrayChar.D2) pwd.getChild(
                this.pairwiseDistanceNamesVariableName).getArray();
        log.debug("Trying to access variable: {}",
                this.minimizingArrayCompVariableName);
        final ArrayInt.D0 minimizeDistA = (ArrayInt.D0) pwd.getChild(
                this.minimizingArrayCompVariableName).getArray();
        if (minimizeDistA.get() == 0) {
            this.minimizeDist = false;
        } else {
            this.minimizeDist = true;
        }
        // Second step:
        // find sequence minimizing distance to all other sequences
        final IFileFragment centerSeq = findCenterSequence(pwdist, names1);

        final CSVWriter csvw = Factory.getInstance().getObjectFactory().
                instantiate(CSVWriter.class);
        csvw.setWorkflow(getWorkflow());
        List<List<String>> tble = new ArrayList<List<String>>();
        tble.add(Arrays.asList(centerSeq.getName()));
        csvw.writeTableByRows(getWorkflow().getOutputDirectory(this).
                getAbsolutePath(), "center-star.csv", tble,
                WorkflowSlot.CLUSTERING);

        log.info("Center sequence is: {}", centerSeq.getName());
        final TupleND<IFileFragment> alignments = MaltcmsTools.
                getAlignmentsFromFragment(pwd);
        final List<IFileFragment> warpedFiles = saveAlignment(this.getClass(),
                t, alignments, Factory.getInstance().getConfiguration().
                getString("var.total_intensity", "total_intensity"),
                centerSeq);

        log.info("Saving alignment files!");
        for (final IFileFragment iff : warpedFiles) {
            log.debug("Saving warped file {}", iff.getAbsolutePath());
            log.debug("Source files {}, dimensions: {}", iff.getChild("source_files").getArray(),Arrays.deepToString(iff.getChild("source_files").getDimensions()));
            iff.save();
            iff.clearArrays();
        }
        return new TupleND<IFileFragment>(warpedFiles);
    }

    @Override
    public void configure(final Configuration cfg) {
        this.pairwiseDistanceMatrixVariableName = cfg.getString(
                "var.pairwise_distance_matrix", "pairwise_distance_matrix");
        this.pairwiseDistanceNamesVariableName = cfg.getString(
                "var.pairwise_distance_names", "pairwise_distance_names");
        this.minimizingArrayCompVariableName = cfg.getString(
                "var.minimizing_array_comp", "minimizing_array_comp");
    }

    private void addMultipleAlignment(final IFileFragment f,
            final List<List<String>> table) {
        IVariableFragment maMatrix = new VariableFragment(f,
                this.multipleAlignmentVariableName);
        ArrayChar.D3 matrix = new ArrayChar.D3(table.get(0).size() - 1, table.
                size(), 1024);
        Index mi = matrix.getIndex();

        // columns
        for (int j = 0; j < table.size(); j++) {
            // rows
            for (int i = 1; i < table.get(j).size(); i++) {
                mi.set(i - 1, j);
                matrix.setString(mi, table.get(j).get(i));
            }
        }
        maMatrix.setArray(matrix);

        IVariableFragment maNames = new VariableFragment(f,
                this.multipleAlignmentNamesVariableName);
        ArrayChar.D2 names = cross.datastructures.tools.ArrayTools.
                createStringArray(table.size(), 1024);
        for (int j = 0; j < table.size(); j++) {
            names.setString(j, table.get(j).get(0));
        }
        maNames.setArray(names);

        IVariableFragment maType = new VariableFragment(f,
                this.multipleAlignmentTypeVariableName);
        ArrayChar.D1 type = new ArrayChar.D1(1024);
        type.setString("complete");
        maType.setArray(type);

        IVariableFragment maCreator = new VariableFragment(f,
                this.multipleAlignmentCreatorVariableName);
        ArrayChar.D1 creator = new ArrayChar.D1(1024);
        creator.setString(this.getClass().getCanonicalName());
        maCreator.setArray(creator);
    }

    /**
     * TODO implement for release 1.1
     *
     * @param tuple
     * @param ll
     */
    private void saveToXMLAlignment(final TupleND<IFileFragment> tuple,
            final List<List<Peak>> ll) {
        AlignmentFactory af = new AlignmentFactory();
        Alignment a = af.createNewAlignment(this.getClass().getName(), false);
        HashMap<IFileFragment, List<Integer>> fragmentToScanIndexMap = new HashMap<IFileFragment, List<Integer>>();
        for (final List<Peak> l : ll) {
            log.debug("Adding {} peaks: {}", l.size(), l);
            HashMap<String, Peak> fragToPeak = new HashMap<String, Peak>();
            for (final Peak p : l) {
                fragToPeak.put(p.getAssociation(), p);
            }
            for (final IFileFragment iff : tuple) {
                int scanIndex = -1;
                if (fragToPeak.containsKey(iff.getName())) {
                    Peak p = fragToPeak.get(iff.getName());
                    scanIndex = p.getScanIndex();
                }

                List<Integer> scans = null;
                if (fragmentToScanIndexMap.containsKey(iff)) {
                    scans = fragmentToScanIndexMap.get(iff);
                } else {
                    scans = new ArrayList<Integer>();
                    fragmentToScanIndexMap.put(iff, scans);
                }

                scans.add(scanIndex);
            }
        }

        for (IFileFragment iff : fragmentToScanIndexMap.keySet()) {
            af.addScanIndexMap(a, new File(iff.getAbsolutePath()).toURI(),
                    fragmentToScanIndexMap.get(iff), false);
        }
        File out = new File(getWorkflow().getOutputDirectory(this),
                "centerStarAlignment.maltcmsAlignment.xml");
        af.save(a, out);
        DefaultWorkflowResult dwr = new DefaultWorkflowResult(out, this,
                WorkflowSlot.ALIGNMENT, tuple.toArray(new IFileFragment[]{}));
        getWorkflow().append(dwr);
    }

    private IFileFragment findCenterSequence(final ArrayDouble.D2 a,
            final ArrayChar.D2 names) {
        if (this.centerSequence != null && !this.centerSequence.isEmpty()) {
            for (int i = 0; i < a.getShape()[0]; i++) {
                if (StringTools.removeFileExt(names.getString(i)).equals(
                        StringTools.removeFileExt(this.centerSequence))) {
                    log.info("Using user defined center: {}",
                            this.centerSequence);
                    return Factory.getInstance().getFileFragmentFactory().create(
                            this.centerSequence);
                }
            }
        }
        if (this.alignToFirst) {
            return Factory.getInstance().getFileFragmentFactory().create(names.
                    getString(0));
        }
        final int rows = a.getShape()[0];
        final int cols = a.getShape()[1];
        final double[] sums = new double[rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (i != j) {
                    sums[i] += a.get(i, j);
                }
            }
        }
        for (int i = 0; i < rows; i++) {
            log.debug("Sum of values for {}={}", names.getString(i),
                    sums[i]);
        }

        int optIndex = -1;
        double optVal = this.minimizeDist ? Double.POSITIVE_INFINITY
                : Double.NEGATIVE_INFINITY;
        for (int i = 0; i < rows; i++) {
            if (this.minimizeDist) {
                // if (this.minimizeDist) {
                if (sums[i] < optVal) {
                    optVal = Math.min(optVal, sums[i]);
                    optIndex = i;
                }
            } else {
                if (sums[i] > optVal) {
                    optVal = Math.max(optVal, sums[i]);
                    optIndex = i;
                }
            }
        }
        final IFileFragment centerSeq = Factory.getInstance().
                getFileFragmentFactory().create(names.getString(optIndex));
        return centerSeq;
    }

    @Override
    public String getDescription() {
        return "Creates a multiple alignment by selecting a reference chromatogram based on highest overall similarity or lowest overall distance of reference to other chromatograms.";
    }

    /**
     * @return the minimizingArrayCompVariableName
     */
//    public String getMinimizingArrayCompVariableName() {
//        return this.minimizingArrayCompVariableName;
//    }

    private IFileFragment getOriginalFileFor(final IFileFragment iff) {
        // final List<IFileFragment> l = Factory.getInstance()
        // .getInputDataFactory().getInitialFiles();
        // depth first search for first file fragment with the same name
        // and no further source files
        // final String iffname = StringTools.removeFileExt(iff.getName());
        // final Stack<IFileFragment> history = new Stack<IFileFragment>();
        // history.push(iff);
        // while (!history.isEmpty()) {
        // IFileFragment work = history.pop();
        // log.info("Exploring whether {} is a source file!", work
        // .getAbsolutePath());
        // Collection<IFileFragment> c = work.getSourceFiles();
        // if (c.isEmpty()
        // && StringTools.removeFileExt(work.getName())
        // .equals(iffname)) {
        // log.info("Yes");
        // return work;
        // }
        // for (IFileFragment ff : c) {
        // history.push(ff);
        // }
        // }
        // // for (final IFileFragment f : l) {
        // // Collection<IFileFragment> c = f.getSourceFiles();
        // // final String fname = StringTools.removeFileExt(f.getName());
        // //
        // // if (iffname.equals(fname)) {
        // // return f;
        // // }
        // // }
        // log.warn("Could not find original file, returning argument!");
        return iff;
        // throw new ConstraintViolationException(
        // "Could not find original file, returning argument!");
        // // return iff;
    }

//    /**
//     * @return the pairwiseDistanceMatrixVariableName
//     */
//    public String getPairwiseDistanceMatrixVariableName() {
//        return this.pairwiseDistanceMatrixVariableName;
//    }

//    /**
//     * @return the pairwiseDistanceNamesVariableName
//     */
//    public String getPairwiseDistanceNamesVariableName() {
//        return this.pairwiseDistanceNamesVariableName;
//    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.ALIGNMENT;
    }

//    /**
//     * @return the minimizeDist
//     */
//    public boolean isMinimizeDist() {
//        return this.minimizeDist;
//    }

    private List<IFileFragment> saveAlignment(final Class<?> creator,
            final TupleND<IFileFragment> t,
            final TupleND<IFileFragment> alignments, final String ticvar,
            final IFileFragment top) {
        // Set representative name
        final String refname = StringTools.removeFileExt(top.getName());
        log.debug("Ref is {}", refname);
        log.debug("top: {}", top);
        log.debug("ticvar is {}", ticvar);
        // List to hold alignments, where representative is LHS
        final ArrayList<IFileFragment> repOnLHS = new ArrayList<IFileFragment>();
        // List to hold alignment, where representative is RHS
        final ArrayList<IFileFragment> repOnRHS = new ArrayList<IFileFragment>();
        // List of warped files
        final List<IFileFragment> warped = new ArrayList<IFileFragment>();
        // warped.add(top);
        // final ChromatogramWarp cw = Factory.getInstance().getObjectFactory()
        // .instantiate(ChromatogramWarp.class);
        // cw.setWorkflow(getWorkflow());
        prepareFileFragments(alignments, top, refname, repOnLHS, repOnRHS,
                warped, null);

        log.debug(
                "#alignments to reference: {}, #alignments to query: {}",
                repOnLHS.size(), repOnRHS.size());
        final List<List<String>> table = prepareMultipleAlignmentTable(ticvar,
                top);
        addMultipleAlignmentColumns(top, repOnLHS, repOnRHS, warped, null,
                table);
        // create alignment table fragment
        IFileFragment maFragment = new FileFragment(getWorkflow().
                getOutputDirectory(this), "multiple-alignment.cdf");
        addMultipleAlignment(maFragment, table);
        maFragment.save();
        DefaultWorkflowResult dfw = new DefaultWorkflowResult(new File(
                maFragment.getAbsolutePath()), this, WorkflowSlot.ALIGNMENT,
                warped.toArray(new IFileFragment[]{}));
        getWorkflow().append(dfw);
        for (IFileFragment ifrag : warped) {
            ifrag.addSourceFile(maFragment);
        }

        saveScanAcquisitionTimes(t, table);
        saveMultipleAlignmentTable(creator, table);

        return warped;
    }

    private void saveScanAcquisitionTimes(final TupleND<IFileFragment> t,
            final List<List<String>> table) {
        // alignment table is organized in columns, one for each FileFragment
        final HashMap<String, IFileFragment> nameToFragment = new HashMap<String, IFileFragment>();
        for (IFileFragment iff : t) {
            nameToFragment.put(StringTools.removeFileExt(iff.getName()), iff);
        }
        final List<List<String>> satTable = new ArrayList<List<String>>();
        // this is a fix, default rounding convention is HALF_EVEN,
        // which allows less error to accumulate, but is seldomly used
        // outside of java...
        final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
                Locale.US);
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.applyPattern("0.000");
        for (int column = 0; column < table.size(); column++) {
            List<String> rindices = table.get(column);
            log.debug("Processing row {}: {}", column, rindices);
            ArrayList<String> colList = new ArrayList<String>();
            colList.add(rindices.get(0));
            IFileFragment iff = nameToFragment.get(colList.get(0));
            log.debug("Using FileFragment {} as source!", iff.getAbsolutePath());
            for (int row = 1; row < rindices.size(); row++) {
                String c = rindices.get(row);
                String[] split = c.split(",");
                log.debug("Split row {}: {}", row, Arrays.toString(split));
                // since we can have multiple indices mapped from one
                // chromatogram
                // to the reference, we need to assign a scan acquisition time
                // -> use median
                double sat[] = new double[split.length];
                int splitIdx = 0;
                for (String st : split) {
                    int sidx = Integer.parseInt(st);
                    log.debug("Reading sat at scan {}", sidx);
                    sat[splitIdx++] = MaltcmsTools.getScanAcquisitionTime(iff,
                            sidx);
                }
                double satv = MathTools.median(sat) / 60.0;
                log.debug("Value of sat: {}", satv);
                colList.add(df.format(satv));// String.format("%.4f",
                // String.valueOf(sat)));
            }
            satTable.add(colList);
        }
        final CSVWriter csv = Factory.getInstance().getObjectFactory().
                instantiate(CSVWriter.class);
        csv.setWorkflow(getWorkflow());
        csv.writeTableByCols(getWorkflow().getOutputDirectory(this).
                getAbsolutePath(), "multiple-alignmentRT.csv", satTable,
                WorkflowSlot.ALIGNMENT);
    }

    /**
     * @param creator
     * @param table
     */
    private File saveMultipleAlignmentTable(final Class<?> creator,
            final List<List<String>> table) {
        final CSVWriter csv = Factory.getInstance().getObjectFactory().
                instantiate(CSVWriter.class);
        csv.setWorkflow(getWorkflow());
        return csv.writeTableByCols(getWorkflow().getOutputDirectory(this).
                getAbsolutePath(), "multiple-alignment.csv", table,
                WorkflowSlot.ALIGNMENT);
    }

    /**
     * @param alignment
     * @param top
     * @param refname
     * @param repOnLHS
     * @param repOnRHS
     * @param warped
     * @param cw
     */
    private void prepareFileFragments(final TupleND<IFileFragment> alignment,
            final IFileFragment top, final String refname,
            final ArrayList<IFileFragment> repOnLHS,
            final ArrayList<IFileFragment> repOnRHS,
            final List<IFileFragment> warped, final ChromatogramWarp cw) {

        // final IFileFragment centerCopy = Factory.getInstance()
        // .getFileFragmentFactory().create(
        // new File(getWorkflow().getOutputDirectory(this), top
        // .getName()), top);
        // final IFileFragment centerCopy = cw.copyReference(
        // getOriginalFileFor(top), getWorkflow());
        // centerCopy.addSourceFile(top);
        // DefaultWorkflowResult dwr = new DefaultWorkflowResult(new File(
        // centerCopy.getAbsolutePath()), this, WorkflowSlot.WARPING,
        // centerCopy);
        // getWorkflow().append(dwr);
        // warped.add(centerCopy);
        FileFragment newTop = new FileFragment(getWorkflow().getOutputDirectory(
                this), top.getName());
        newTop.addSourceFile(top);
        warped.add(newTop);
        log.info("Reference is {}", refname);
        for (final IFileFragment iff : alignment) {
            // log.info("Processing {}", iff.getAbsolutePath());
            final IFileFragment lhs = FragmentTools.getLHSFile(iff);
            final IFileFragment rhs = FragmentTools.getRHSFile(iff);
            final String lhsName = StringTools.removeFileExt(lhs.getName());
            final String rhsName = StringTools.removeFileExt(rhs.getName());
            // log.info("lhs: {}, rhs: {}", lhs.getAbsolutePath(), rhs
            // .getAbsolutePath());
            if (lhsName.equals(refname)) {
                log.info("Projecting {} to {} (lhs)", rhs.getName(),refname);
                FileFragment nrhs = new FileFragment(getWorkflow().
                        getOutputDirectory(this), rhs.getName());
                nrhs.addSourceFile(rhs);
                warped.add(nrhs);
                repOnLHS.add(iff);
            } else if (refname.equals(rhsName)) {
                log.info("Projecting {} to {} (rhs)", lhs.getName(),refname);
                FileFragment nlhs = new FileFragment(getWorkflow().
                        getOutputDirectory(this), lhs.getName());
                nlhs.addSourceFile(lhs);
                warped.add(nlhs);
                repOnRHS.add(iff);
            } else {
                log.debug(
                        "Name of reference {} not contained, skipping alignment!",
                        refname);
            }
        }
    }

    /**
     * @param top
     * @param repOnLHS
     * @param repOnRHS
     * @param warped
     * @param cw
     * @param table
     */
    private void addMultipleAlignmentColumns(final IFileFragment top,
            final ArrayList<IFileFragment> repOnLHS,
            final ArrayList<IFileFragment> repOnRHS,
            final List<IFileFragment> warped, final ChromatogramWarp cw,
            final List<List<String>> table) {
        // top/representative is on lhs side of alignment
        for (int i = 0; i < repOnLHS.size(); i++) {
            mapToLHS(top, repOnLHS, warped, cw, table, i);
        }
        // Alignments where rep is on rhs
        // so retrieve warp path and lhs array and warp to rhs array via path
        for (int i = 0; i < repOnRHS.size(); i++) {
            mapToRHS(top, repOnRHS, warped, cw, table, i);
        }
    }

    /**
     * @param ticvar
     * @param top
     * @return
     */
    private List<List<String>> prepareMultipleAlignmentTable(
            final String ticvar, final IFileFragment top) {
        final List<List<String>> table = new ArrayList<List<String>>();
        final Array topa = top.getChild(ticvar).getArray();
        final ArrayList<String> topCol = new ArrayList<String>(
                topa.getShape()[0] + 1);
        topCol.add(StringTools.removeFileExt(top.getName()));
        for (int i = 0; i < topa.getShape()[0]; i++) {
            topCol.add(i + "");
        }
        table.add(topCol);
        return table;
    }

    /**
     * @param top
     * @param repOnLHS
     * @param warped
     * @param cw
     * @param table
     * @param i
     */
    private void mapToLHS(final IFileFragment top,
            final ArrayList<IFileFragment> repOnLHS,
            final List<IFileFragment> warped, final ChromatogramWarp cw,
            final List<List<String>> table, int i) {
        final List<Tuple2DI> al = MaltcmsTools.getWarpPath(repOnLHS.get(i));
        // warpRHS(top, repOnLHS, warped, cw, i, al);
        final String name = StringTools.removeFileExt(FragmentTools.getRHSFile(
                repOnLHS.get(i)).getName());
        final ArrayList<String> column = new ArrayList<String>();
        column.add(name);
        int lastLIndex = 0;
        int mapCount = 0;
        final double value = 0;
        StringBuffer sb = new StringBuffer(1);
        // map from right to left
        // if multiple elements of right are mapped to single left
        // add as comma separated list
        // else if multiple elements of left are mapped to single right
        // insert right value accordingly often
        for (final Tuple2DI tpl : al) {
            log.debug("value={}", value);
            if (mapCount == 0) {
                column.add(tpl.getSecond() + "");
                lastLIndex = tpl.getFirst();
            } else {
                // compression from rhs to lhs, last element was start of
                // compression
                if (lastLIndex == tpl.getFirst()) {
                    sb.append(tpl.getSecond() + ",");
                } else {// different lhs indices, leaving compression range
                    if (sb.length() > 0) {// StringBuffer length > 1 =>
                        // compression
                        sb.append(tpl.getSecond());
                        column.add(sb.toString());// add compression range
                        // to column
                        sb = new StringBuffer(1);// reset StringBuffer
                    } else {// String Buffer length == 0, match
                        column.add(sb.append(tpl.getSecond()).toString());
                        sb = new StringBuffer(1);
                    }
                }
                lastLIndex = tpl.getFirst();
            }
            mapCount++;
        }
        table.add(column);
    }

    /**
     * @param top
     * @param repOnRHS
     * @param warped
     * @param cw
     * @param table
     * @param i
     */
    private void mapToRHS(final IFileFragment top,
            final ArrayList<IFileFragment> repOnRHS,
            final List<IFileFragment> warped, final ChromatogramWarp cw,
            final List<List<String>> table, int i) {
        final List<Tuple2DI> al = MaltcmsTools.getWarpPath(repOnRHS.get(i));
        // warpLHS(top, repOnRHS, warped, cw, i, al);
        final String name = StringTools.removeFileExt(FragmentTools.getLHSFile(
                repOnRHS.get(i)).getName());
        final ArrayList<String> column = new ArrayList<String>();
        column.add(name);
        int lastRIndex = 0;
        int mapCount = 0;
        final double value = 0;
        StringBuffer sb = new StringBuffer(1);
        // map from right to left
        // if multiple elements of right are mapped to single left
        // add as comma separated list
        // else if multiple elements of left are mapped to single right
        // insert right value accordingly often
        for (final Tuple2DI tpl : al) {
            log.debug("value={}", value);
            if (mapCount == 0) {
                column.add(tpl.getFirst() + "");
                lastRIndex = tpl.getSecond();
            } else {
                // compression from rhs to lhs, last element was start of
                // compression
                if (lastRIndex == tpl.getSecond()) {
                    sb.append(tpl.getFirst() + ",");
                } else {// different lhs indices, leaving compression range
                    if (sb.length() > 0) {// StringBuffer length > 1 =>
                        // compression
                        sb.append(tpl.getFirst());
                        column.add(sb.toString());// add compression range
                        // to column
                        sb = new StringBuffer(1);// reset StringBuffer
                    } else {// String Buffer length == 0, match
                        column.add(sb.append(tpl.getFirst()).toString());
                        sb = new StringBuffer(1);
                    }
                }
                lastRIndex = tpl.getSecond();
            }
            mapCount++;
        }
        table.add(column);
    }

    /**
     * @param top
     * @param repOnRHS
     * @param warped
     * @param cw
     * @param i
     * @param al
     */
    private void warpLHS(final IFileFragment top,
            final ArrayList<IFileFragment> repOnRHS,
            final List<IFileFragment> warped, final ChromatogramWarp cw, int i,
            final List<Tuple2DI> al) {
        DefaultWorkflowResult dwr;
        final IFileFragment ifwarped = cw.warp(top,
                getOriginalFileFor(FragmentTools.getLHSFile(repOnRHS.get(i))),
                FragmentTools.getLHSFile(repOnRHS.get(i)), al, false,
                getWorkflow());
        dwr = new DefaultWorkflowResult(new File(ifwarped.getAbsolutePath()),
                this, WorkflowSlot.WARPING, ifwarped);
        getWorkflow().append(dwr);
        warped.add(ifwarped);
    }

    /**
     * @param top
     * @param repOnLHS
     * @param warped
     * @param cw
     * @param i
     * @param al
     */
    private void warpRHS(final IFileFragment top,
            final ArrayList<IFileFragment> repOnLHS,
            final List<IFileFragment> warped, final ChromatogramWarp cw, int i,
            final List<Tuple2DI> al) {
        DefaultWorkflowResult dwr;
        final IFileFragment ifwarped = cw.warp(top,
                getOriginalFileFor(FragmentTools.getRHSFile(repOnLHS.get(i))),
                FragmentTools.getRHSFile(repOnLHS.get(i)), al, true,
                getWorkflow());
        dwr = new DefaultWorkflowResult(new File(ifwarped.getAbsolutePath()),
                this, WorkflowSlot.WARPING, ifwarped);
        getWorkflow().append(dwr);
        warped.add(ifwarped);
    }

    /**
     * @param minimizeDist
     *            the minimizeDist to set
     */
//    public void setMinimizeDist(final boolean minimizeDist) {
//        this.minimizeDist = minimizeDist;
//    }

    /**
     * @param minimizingArrayCompVariableName
     *            the minimizingArrayCompVariableName to set
     */
//    public void setMinimizingArrayCompVariableName(
//            final String minimizingArrayCompVariableName) {
//        this.minimizingArrayCompVariableName = minimizingArrayCompVariableName;
//    }

    /**
     * @param pairwiseDistanceMatrixVariableName
     *            the pairwiseDistanceMatrixVariableName to set
     */
//    public void setPairwiseDistanceMatrixVariableName(
//            final String pairwiseDistanceMatrixVariableName) {
//        this.pairwiseDistanceMatrixVariableName = pairwiseDistanceMatrixVariableName;
//    }

    /**
     * @param pairwiseDistanceNamesVariableName
     *            the pairwiseDistanceNamesVariableName to set
     */
//    public void setPairwiseDistanceNamesVariableName(
//            final String pairwiseDistanceNamesVariableName) {
//        this.pairwiseDistanceNamesVariableName = pairwiseDistanceNamesVariableName;
//    }
}
