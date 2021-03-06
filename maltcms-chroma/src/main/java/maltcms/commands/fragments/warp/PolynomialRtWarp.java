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
package maltcms.commands.fragments.warp;

import cross.annotations.Configurable;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.NotImplementedException;
import cross.exception.ResourceNotAvailableException;
import cross.tools.StringTools;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.ms.IAnchor;
import maltcms.io.csv.CSVReader;
import maltcms.tools.ArrayTools;
import maltcms.tools.MaltcmsTools;
import org.apache.commons.configuration.Configuration;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.MAMath;

/**
 * Use Objects of this class to apply an alignment, warping a source
 * chromatogram to the time/scans of reference chromatogram.
 *
 * @author Nils Hoffmann
 * 
 * @since 1.3.2
 */
@RequiresVariables(names = {"var.multiple_alignment",
    "var.multiple_alignment_names", "var.multiple_alignment_type",
    "var.multiple_alignment_creator"})
@Slf4j
@Data
//@ServiceProvider(service = AFragmentCommand.class)
public class PolynomialRtWarp extends AFragmentCommand {

    private final String description = "Warps Chromatograms to a given reference, according to alignment paths.";
    private final WorkflowSlot workflowSlot = WorkflowSlot.WARPING;
    @Configurable
    private List<String> indexedVars = Arrays.asList("mass_values",
            "intensity_values");
    @Configurable
    private List<String> plainVars = Arrays.asList("total_intensity",
            "scan_acquisition_time");
    @Configurable
    private String indexVar = "scan_index";
    @Configurable(name = "var.anchors.retention_scans")
    private String anchorScanIndexVariableName = "retention_scans";
    @Configurable(name = "var.anchors.retention_index_names")
    private String anchorNameVariableName = "retention_index_names";
    @Configurable(name = "var.multiple_alignment")
    private String multipleAlignmentVariableName = "multiple_alignment";
    @Configurable(name = "var.multiple_alignment_names")
    private String multipleAlignmentNamesVariableName = "multiple_alignment_names";
    @Configurable(name = "var.multiple_alignment_type")
    private String multipleAlignmentTypeVariableName = "multiple_alignment_type";
    @Configurable(name = "var.multiple_alignment_creator")
    private String multipleAlignmentCreatorVariableName = "multiple_alignment_creator";
    @Configurable
    private boolean averageCompressions = false;
    @Configurable
    private String alignmentLocation = "";
    @Configurable(name = "var.scan_acquisition_time")
    private String satvar = "scan_acquisition_time";

    private AlignmentTable buildTableFromFragment(IFileFragment f) {
        try {
            IVariableFragment ma = f.getChild(this.multipleAlignmentVariableName);
            ArrayChar.D3 maa = (ArrayChar.D3) ma.getArray();
            IVariableFragment maNames = f.getChild(
                    this.multipleAlignmentNamesVariableName);
            ArrayChar.D2 mana = (ArrayChar.D2) maNames.getArray();
            AlignmentTable at = new AlignmentTable(maa, mana);

            return at;
        } catch (ResourceNotAvailableException rnae) {
            log.warn("Could not retrieve alignment from FileFragment!");

        }
        return null;
    }

    private String nameOf(IFileFragment f) {
        return StringTools.removeFileExt(f.getName());
    }

    private double rtFor(Map<String, IFileFragment> nameToFragment, String name, int scanIndex) {
        IFileFragment fragment = nameToFragment.get(name);
        EvalTools.notNull(fragment, this);
        return fragment.getChild(satvar).getArray().getDouble(scanIndex);
    }
    
//    private double rtForGroup(int[] scanIndex)

    /*
     * (non-Javadoc)
     *
     * @see cross.commands.ICommand#apply(java.lang.Object)
     */
    /** {@inheritDoc} */
    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        throw new NotImplementedException();
//        TupleND<IFileFragment> wt = createWorkFragments(t);
//        Map<String, IFileFragment> sourceFragmentMap = new HashMap<String, IFileFragment>();
//        for (IFileFragment f : t) {
//            sourceFragmentMap.put(nameOf(f), f);
//        }
//        log.debug("{}", t);
//        log.debug("{}", wt);
//        IFileFragment ref = null;
//        // TODO enable arbitrary reference selection
//        AlignmentTable at = null;
//        // alignment location overrides, so try first
//        if (this.alignmentLocation == null || this.alignmentLocation.isEmpty()) {
//            log.info("Using alignment from fragment");
//            at = buildTableFromFragment(t.get(0));
//        } else {
//            log.info("Using alignmentLocation");
//            at = new AlignmentTable(new File(this.alignmentLocation));
//        }
//        log.info("Warping scan acquisition time!");
//        EvalTools.notNull(at, this);
//        String refname = at.getRefName();
//
//        IFileFragment refOrig = null;
//        for (IFileFragment f : t) {
//            if (nameOf(f).equals(refname)) {
//                refOrig = f;
//            }
//        }
//        EvalTools.notNull(refOrig, this);
//
//        log.info("Reference is {}", refOrig.getName());
//        for (int i = 0; i < wt.size(); i++) {
//            IFileFragment queryTarget = wt.get(i);
//            IFileFragment querySource = t.get(i);
//            log.info("Processing {}/{}", (i + 1), wt.size());
//            String queryTargetName = nameOf(queryTarget);
//            log.info("Warping {}", querySource.getName());
//            refOrig = new FileFragment(refOrig.getUri());
////            IFileFragment res = warp(refOrig, querySource, queryTarget,
////                    at.getPathFor(refOrig, queryTarget), true,
////                    getWorkflow());
////            res.save();
////            DefaultWorkflowResult dwr = new DefaultWorkflowResult(res.getUri(), this, WorkflowSlot.WARPING, res);
////            getWorkflow().append(dwr);
//        }
//        return wt;
    }

    class AlignmentTable {

        HashMap<String, List<int[]>> columnMap = new HashMap<>();
        String refName = "";

        AlignmentTable(ArrayChar.D3 table, ArrayChar.D2 names) {
            refName = names.getString(0);
            int lines = table.getShape()[0];
            int cols = table.getShape()[1];
            Index tableIndex = table.getIndex();
            for (int i = 0; i < lines; i++) {
                for (int j = 0; j < cols; j++) {
                    String s = table.getString(tableIndex.set(i, j));
                    String colName = names.getString(j);
                    String[] entry = s.split(",");
                    int[] vals = new int[entry.length];
                    int k = 0;
                    for (String es : entry) {
                        vals[k++] = Integer.parseInt(es);
                    }
                    List<int[]> l = null;
                    if (columnMap.containsKey(colName)) {
                        l = columnMap.get(colName);
                        l.add(vals);
                    } else {
                        l = new ArrayList<>();
                        l.add(vals);
                        this.columnMap.put(colName, l);
                    }
                }
            }
        }

        AlignmentTable(File f) {
            CSVReader csvr = new CSVReader();
            try {
                Tuple2D<Vector<Vector<String>>, Vector<String>> table = csvr.
                        read(new FileInputStream(f));
                refName = table.getSecond().get(0);
                for (Vector<String> line : table.getFirst()) {
                    int col = 0;
                    for (String s : line) {
                        String colName = table.getSecond().get(col);
                        String[] entry = s.split(",");
                        int[] vals = new int[entry.length];
                        int i = 0;
                        for (String es : entry) {
                            vals[i++] = Integer.parseInt(es);
                        }
                        List<int[]> l = null;
                        if (columnMap.containsKey(colName)) {
                            l = columnMap.get(colName);
                            l.add(vals);
                        } else {
                            l = new ArrayList<>();
                            l.add(vals);
                            this.columnMap.put(colName, l);
                        }
                        col++;
                    }
                }
            } catch (FileNotFoundException e) {
   
                log.warn(e.getLocalizedMessage());
            }
        }

        String getRefName() {
            return refName;
        }

        List<Tuple2DI> getPathFor(IFileFragment ref, IFileFragment query) {
            List<int[]> refIdx = columnMap.get(StringTools.removeFileExt(ref.
                    getName()));
            List<int[]> queryIdx = columnMap.get(StringTools.removeFileExt(query.
                    getName()));
            EvalTools.eqI(refIdx.size(), queryIdx.size(), this);
            List<Tuple2DI> path = new ArrayList<>();
            for (int i = 0; i < refIdx.size(); i++) {
                int[] ridx = refIdx.get(i);
                int[] qidx = queryIdx.get(i);
                for (int j = 0; j < qidx.length; j++) {
                    path.add(new Tuple2DI(ridx[0], qidx[j]));
                }
            }
            return path;
        }
        
        int[] getGroupScanIndices(int row) {
            int[] rowIndices = new int[columnMap.size()];
            int j = 0;
            for(String header:columnMap.keySet()) {
                rowIndices[j]=columnMap.get(header).get(row)[0];
                j++;
            }
            return rowIndices;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration cfg) {
        this.anchorScanIndexVariableName = cfg.getString(
                "var.anchors.retention_scans", "retention_scans");
        this.anchorNameVariableName = cfg.getString(
                "var.anchors.retention_names", "retention_names");
        this.satvar = cfg.getString("var.scan_acquisition_time",
                "scan_acquisition_time");
    }

    /**
     * Warps by projecting data from originalFile to ref, given the alignment
     * path. toLHS determines, whether the alignment reference ref is on the
     * left hand side of the alignment path, or on the right hand side.
     * processedFile is used to keep a backpointer to processing, like anchor
     * finding. The returned FileFragment will have processedFile as its source
     * file, not originalFile, since we want to keep additional information,
     * which we already found out.
     *
     * @param path a {@link java.util.List} object.
     * @param toLHS a boolean.
     * @param querySource a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param queryTarget a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param iw a {@link cross.datastructures.workflow.IWorkflow} object.
     * @return a {@link cross.datastructures.fragments.IFileFragment} object.
     */
    public IFileFragment warp(final IFileFragment querySource, final IFileFragment queryTarget,
            final List<Tuple2DI> path, final boolean toLHS, final IWorkflow iw) {
//        warp1D(queryTarget, querySource, path, this.plainVars, toLHS);
        //warpAnchors(queryTarget, querySource, path, toLHS);
        queryTarget.addSourceFile(querySource);
        return queryTarget;
    }

    /**
     * Warp non-indexed arrays, which are for example scan_acquisisition_time,
     * tic etc.
     *
     * @param warpedB target IFileFragment to store warped arrays
     * @param ref reference IFileFragment
     * @param toBeWarped the to-be-warped IFileFragment
     * @param path alignment path of alignment between a and b
     * @param plainVars list of variable names, which should be warped
     * @param toLHS warp to left hand side
     * @return FileFragment containing warped data
     */
    public IFileFragment warp1D(final IFileFragment warpedB,
            final IFileFragment ref, final IFileFragment toBeWarped,
            final List<Tuple2DI> path, final List<String> plainVars,
            final boolean toLHS) {
        for (final String s : plainVars) {
            if (s.equals(this.indexVar)) {
                continue;
            }
            if (s.equals(this.satvar)) {
                warpSAT(warpedB, ref, toBeWarped, path, plainVars, toLHS);
                continue;
            }
            log.info("Warping variable {}", s);
            IVariableFragment var = null;
            Array warpedA = null;
            if (warpedB.hasChild(s)) {
                var = warpedB.getChild(s);
            } else {
                var = new VariableFragment(warpedB, s);
            }
            log.info("{}", ref);
            IVariableFragment refVar = ref.getChild(s);
            final Array refA = refVar.getArray();
            IVariableFragment tbwVar = toBeWarped.getChild(s);
            warpedA = tbwVar.getArray();
            if (toLHS) {// a is on lhs of path
                log.debug("Warping to lhs {}, {} from file {}",
                        new Object[]{ref.getName(), s,
                            toBeWarped.getName()});

                // there exists a subtle bug, if variables in original file has
                // an empty array
                // then this array might be null
                if (warpedA != null) {
                    EvalTools.notNull(refA, "refA was null", this);
                    EvalTools.notNull(path, "path was null", this);
                    EvalTools.notNull(warpedA, "warpedA was null", this);
                    // refA is only needed for correct shape and data type
                    warpedA = ArrayTools.projectToLHS(refA, path, warpedA, true);
                }

            } else { // whether b is on lhs of path
                log.debug("Warping to lhs {}, {} from file {}",
                        new Object[]{ref.getName(), s,
                            toBeWarped.getName()});
                // there exists a subtle bug, if variables in original file has
                // an empty array
                // then this array might be null
                if (warpedA != null) {
                    EvalTools.notNull(refA, "refA was null", this);
                    EvalTools.notNull(path, "path was null", this);
                    EvalTools.notNull(warpedA, "warpedA was null", this);
                    // refA is only needed for correct shape and data type
                    warpedA = ArrayTools.projectToRHS(refA, path, warpedA, true);
                }
            }
            var.setArray(warpedA);
        }
        return warpedB;
    }

    /**
     * Warp indexed arrays, which are lists of 1D arrays, so pseudo 2D. In this
     * case limited to mass_values and intensity_values.
     *
     * @param warpedB target IFileFragment to store warped arrays
     * @param ref reference IFileFragment
     * @param toBeWarped the to-be-warped IFileFragment
     * @param path alignment path of alignment between a and b
     * @param toLHS whether warping should be done from right to left (true) or
     * vice versa (false)
     * @return FileFragment containing warped data
     * @param indexedVars a {@link java.util.List} object.
     */
    public IFileFragment warp2D(final IFileFragment warpedB,
            final IFileFragment ref, final IFileFragment toBeWarped,
            final List<Tuple2DI> path, final List<String> indexedVars,
            final boolean toLHS) {
        // indexVar.setArray(b.getChild(this.indexVar).getArray());
        // log.info("Index Variable {}",indexVar.getArray());
        // for(String s:indexedVars) {
        final String s1 = "mass_values";
        final String s2 = "intensity_values";

        IVariableFragment ivf1 = null, ivf2 = null;
        List<Array> tbwa1 = null, tbwa2 = null;
        Tuple2D<List<Array>, List<Array>> t = null;
        final ArrayList<Tuple2DI> al = new ArrayList<>(path.size());
        al.addAll(path);
        try {
            log.debug("Processing {} indexed by {} from file {}",
                    new Object[]{s1, this.indexVar, toBeWarped.getName()});
            log.debug("Processing {} indexed by {} from file {}",
                    new Object[]{s2, this.indexVar, toBeWarped.getName()});
            IVariableFragment iv = ref.getChild(this.indexVar);
            IVariableFragment s1v = ref.getChild(s1);
            s1v.setIndex(iv);
            IVariableFragment s2v = ref.getChild(s2);
            s2v.setIndex(iv);
            final List<Array> aA1 = s1v.getIndexedArray();
            final List<Array> aA2 = s2v.getIndexedArray();
            IVariableFragment tiv = toBeWarped.getChild(this.indexVar);
            IVariableFragment t1v = toBeWarped.getChild(s1);
            t1v.setIndex(tiv);
            IVariableFragment t2v = toBeWarped.getChild(s2);
            t2v.setIndex(tiv);
            tbwa1 = t1v.getIndexedArray();
            tbwa2 = t2v.getIndexedArray();
            // aA is only needed for correct shape and data type
            // if(toLHS) {
            // t = ArrayTools.project2(toLHS,bA1, bA2, al, aA1, aA2);
            // }else{
            // t = ArrayTools.project2(toLHS,aA1, aA2, al, bA1, bA2);
            // }
            t = ArrayTools.project2(toLHS, aA1, aA2, al, tbwa1, tbwa2,
                    this.averageCompressions);
            tbwa1 = t.getFirst();
            tbwa2 = t.getSecond();
            // Update index variable
            IVariableFragment indexVar = null;
            if (warpedB.hasChild(this.indexVar)) {
                indexVar = warpedB.getChild(this.indexVar);
            } else {
                indexVar = new VariableFragment(warpedB, this.indexVar);
            }
            final ArrayInt.D1 index = new ArrayInt.D1(tbwa1.size());
            int offset = 0;
            for (int i = 0; i < tbwa1.size(); i++) {
                index.set(i, offset);
                offset += tbwa1.get(i).getShape()[0];
            }
            indexVar.setArray(index);
            // Set all arrays
            if (warpedB.hasChild(s1)) {
                ivf1 = warpedB.getChild(s1);
            } else {
                ivf1 = new VariableFragment(warpedB, s1);
            }
            if (warpedB.hasChild(s2)) {
                ivf2 = warpedB.getChild(s2);
            } else {
                ivf2 = new VariableFragment(warpedB, s2);
            }
            ivf1.setIndex(indexVar);
            ivf2.setIndex(indexVar);

            final List<Array> warpedMasses = new ArrayList<>();
            for (final Array a : tbwa1) {
                final ArrayDouble.D1 ad = new ArrayDouble.D1(a.getShape()[0]);
                MAMath.copyDouble(ad, a);
                warpedMasses.add(ad);
            }
            ivf1.setIndexedArray(warpedMasses);
            final List<Array> warpedIntensities = new ArrayList<>();
            for (final Array a : tbwa2) {
                final ArrayDouble.D1 ad = new ArrayDouble.D1(a.getShape()[0]);
                MAMath.copyDouble(ad, a);
                warpedIntensities.add(ad);
            }
            ivf2.setIndexedArray(warpedIntensities);
            // }
        } catch (final ResourceNotAvailableException rnae) {
            log.warn("Could not warp scans: {}", rnae);
        }
        return warpedB;
    }

    private IFileFragment warpSAT(final IFileFragment warpedB,
            final IFileFragment ref, final IFileFragment toBeWarped,
            final List<Tuple2DI> path, final List<String> plainVars,
            final boolean toLHS) {
        log.info("Warping scan acquisition time");
        IVariableFragment var = null;
        Array warpedA = null;
        final String s = this.satvar;
        if (warpedB.hasChild(s)) {
            var = warpedB.getChild(s);
        } else {
            var = new VariableFragment(warpedB, s);
        }
        if (toLHS) {// a is on lhs of path
            log.debug("Warping to lhs {}, {} from file {}", new Object[]{
                ref.getName(), s, toBeWarped.getName()});
            final Array refA = ref.getChild(s).getArray();
            EvalTools.notNull(refA, this);
            warpedA = toBeWarped.getChild(s).getArray();
            // there exists a subtle bug, if variables in original file has
            // an empty array
            // then this array might be null
            if (warpedA != null) {
                // refA is only needed for correct shape and data type
                warpedA = projectToLHS(refA, path, warpedA, true);
            }

        } else { // whether b is on lhs of path
            log.debug("Warping to lhs {}, {} from file {}", new Object[]{
                ref.getName(), s, toBeWarped.getName()});
            final Array refA = ref.getChild(s).getArray();
            EvalTools.notNull(refA, this);
            warpedA = toBeWarped.getChild(s).getArray();
            // there exists a subtle bug, if variables in original file has
            // an empty array
            // then this array might be null
            if (warpedA != null) {
                // refA is only needed for correct shape and data type
                warpedA = projectToRHS(refA, path, warpedA, true);
            }
        }
        var.setArray(warpedA);
        return warpedB;
    }

    /**
     * <p>projectToLHS.</p>
     *
     * @param lhs a {@link ucar.ma2.Array} object.
     * @param al a {@link java.util.List} object.
     * @param rhs a {@link ucar.ma2.Array} object.
     * @param average a boolean.
     * @return a {@link ucar.ma2.Array} object.
     */
    public static Array projectToLHS(final Array lhs, final List<Tuple2DI> al,
            final Array rhs, final boolean average) {
        final Array rhsm = Array.factory(lhs.getElementType(), lhs.getShape());
        final Index rhsmi = rhsm.getIndex();
        final Index lhsi = lhs.getIndex();
        for (final Tuple2DI tpl : al) {
            rhsmi.set(tpl.getFirst());
            rhsm.setDouble(rhsmi, lhs.getDouble(lhsi.set(tpl.getFirst())));
        }
        return rhsm;
    }

    /**
     * <p>projectToRHS.</p>
     *
     * @param rhs a {@link ucar.ma2.Array} object.
     * @param al a {@link java.util.List} object.
     * @param lhs a {@link ucar.ma2.Array} object.
     * @param average a boolean.
     * @return a {@link ucar.ma2.Array} object.
     */
    public static Array projectToRHS(final Array rhs, final List<Tuple2DI> al,
            final Array lhs, final boolean average) {
        final Array lhsm = Array.factory(rhs.getElementType(), rhs.getShape());
        final Index lhsmi = lhsm.getIndex();
        final Index rhsi = rhs.getIndex();
        for (final Tuple2DI tpl : al) {
            lhsmi.set(tpl.getSecond());
            lhsm.setDouble(lhsmi, rhs.getDouble(rhsi.set(tpl.getSecond())));
        }
        return lhsm;
    }

    /**
     * Warp anchors according to pairwise scan mapping in path, reads anchors
     * from processedFile and stores them in warped file. If toLHS is true,
     * assumes target scale of warp on left side of path, processedFile should
     * contain the data to the right side of the path.
     *
     * @param queryTarget a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param path a {@link java.util.List} object.
     * @param toLHS a boolean.
     * @param querySource a {@link cross.datastructures.fragments.IFileFragment} object.
     */
    public void warpAnchors(final IFileFragment queryTarget,
            final IFileFragment querySource, final List<Tuple2DI> path,
            final boolean toLHS) {
        final List<IAnchor> l = MaltcmsTools.prepareAnchors(querySource);
        if (l.isEmpty()) {
            return;
        }
        final ArrayInt.D1 anchPos = new ArrayInt.D1(l.size());
        final ArrayChar.D2 anchNames = cross.datastructures.tools.ArrayTools.
                createStringArray(l.size(), 1024);
        for (int j = 0; j < l.size(); j++) {
            if (toLHS) {
                final int warpedAnchor = ArrayTools.getNewIndexOnLHS(l.get(j).
                        getScanIndex(), path);
                anchPos.set(j, warpedAnchor);
                anchNames.setString(j, l.get(j).getName());
            } else {
                final int warpedAnchor = ArrayTools.getNewIndexOnRHS(l.get(j).
                        getScanIndex(), path);
                anchPos.set(j, warpedAnchor);
                anchNames.setString(j, l.get(j).getName());
            }
        }
        IVariableFragment anchorScanIndex = null;
        try {
            anchorScanIndex = queryTarget.getChild(
                    this.anchorScanIndexVariableName);
            queryTarget.removeChild(anchorScanIndex);
        } catch (ResourceNotAvailableException rnae) {
        }
        anchorScanIndex = new VariableFragment(queryTarget,
                this.anchorScanIndexVariableName);

        anchorScanIndex.setArray(anchPos);

        IVariableFragment anchorName = null;
        try {
            anchorName = queryTarget.getChild(this.anchorNameVariableName);
            queryTarget.removeChild(anchorName);
        } catch (ResourceNotAvailableException rnae) {
        }
        anchorName = new VariableFragment(queryTarget,
                this.anchorNameVariableName);

        anchorName.setArray(anchNames);
    }
}
