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
package maltcms.commands.fragments2d.warp;

import cross.annotations.Configurable;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments2d.testing.Visualization2D;
import maltcms.tools.ArrayTools2;
import maltcms.tools.MaltcmsTools;
import maltcms.tools.PathTools;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.Index;

/**
 * Creates all reference-query horizontal tic scanlines vectors.
 *
 * @author Mathias Wilhelm
 */
@Slf4j
@Data
@RequiresVariables(names = {"var.total_intensity", "var.modulation_time",
    "var.scan_rate", "var.second_column_scan_index"})
@ServiceProvider(service = AFragmentCommand.class)
public class CreateHorizontalTicVector extends AFragmentCommand {

    @Configurable(name = "var.warp_path_i", value = "warp_path_i")
    private String warpPathi = "warp_path_i";
    @Configurable(name = "var.warp_path_j", value = "warp_path_j")
    private String warpPathj = "warp_path_j";
    @Configurable(name = "var.total_intensity", value = "total_intensity")
    private String totalIntensity = "total_intensity";
    @Configurable(name = "var.modulation_time", value = "modulation_time")
    private String modulationVar = "modulation_time";
    @Configurable(name = "var.scan_rate", value = "scan_rate")
    private String scanRateVar = "scan_rate";
    @Configurable(name = "var.second_column_scan_index",
        value = "second_column_scan_index")
    private String secondColumnScanIndexVar = "second_column_scan_index";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Creates the horizontal TIC-vector after warping the first time axis.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {

        final IFileFragment pwHorizontalAlignmentFragment = MaltcmsTools.
            getPairwiseDistanceFragment(t, "-horizontal");

        final ArrayList<IFileFragment> ret = new ArrayList<IFileFragment>();
        IFileFragment fret;
        for (IFileFragment ff : t) {
            fret = new FileFragment(
                new File(getWorkflow().getOutputDirectory(this),
                    ff.getName()));
            fret.addSourceFile(ff);
            ret.add(fret);
        }

        IFileFragment ref, query;
        final Visualization2D vis = new Visualization2D();
        final Index idx = Index.scalarIndexImmutable;
        Double modulationi, modulationj;
        Integer scanRatei, scanRatej;
        List<Array> scanlinesi, scanlinesj, horizontalRefScanlines, horizontalQueryScanlines;
        IFileFragment alignmentHorizontal, resRef, resQuery;
        Array warpi, warpj;
        Tuple2D<List<Array>, List<Array>> scanlines;
        IVariableFragment hrs, hqs, hrsIdx, hqsIdx;
        String refname, queryname;
        for (int i = 0; i < t.size(); i++) {
            resRef = ret.get(i);
            ref = t.get(i);
            for (int j = i + 1; j < t.size(); j++) {
                query = t.get(j);
                resQuery = ret.get(j);

                modulationi = ref.getChild(this.modulationVar).getArray().
                    getDouble(idx);
                modulationj = query.getChild(this.modulationVar).getArray().
                    getDouble(idx);
                scanRatei = ref.getChild(this.scanRateVar).getArray().getInt(
                    idx);
                scanRatej = query.getChild(this.scanRateVar).getArray().getInt(
                    idx);

                scanlinesi = getScanlineFor(ref, modulationi.intValue()
                    * scanRatei.intValue());
                scanlinesj = getScanlineFor(query, modulationj.intValue()
                    * scanRatej.intValue());

                alignmentHorizontal = MaltcmsTools.getPairwiseAlignment(
                    pwHorizontalAlignmentFragment, ref, query);
                warpi = alignmentHorizontal.getChild(this.warpPathi).getArray();
                warpj = alignmentHorizontal.getChild(this.warpPathj).getArray();

                scanlines = vis.createNewScanlines(scanlinesi, scanlinesj,
                    PathTools.pointListFromArrays(warpi, warpj), false,
                    false);

                horizontalRefScanlines = ArrayTools2.transpose(scanlines.
                    getFirst());
                horizontalQueryScanlines = ArrayTools2.transpose(scanlines.
                    getSecond());

                refname = StringTools.removeFileExt(ref.getName());
                queryname = StringTools.removeFileExt(query.getName());

                hrs = new VariableFragment(resRef, refname + "_" + queryname
                    + "-tv");
                hrs.setIndexedArray(horizontalRefScanlines);
                hrsIdx = new VariableFragment(resRef, refname + "_" + queryname
                    + "-idx");
                hrsIdx.setArray(
                    ArrayTools2.getIndexArray(horizontalRefScanlines));

                hqs = new VariableFragment(resQuery, queryname + "_" + refname
                    + "-tv");
                hqs.setIndexedArray(horizontalQueryScanlines);
                hqsIdx = new VariableFragment(resQuery, queryname + "_"
                    + refname + "-idx");
                hqsIdx.setArray(ArrayTools2.getIndexArray(
                    horizontalQueryScanlines));
            }
        }

        for (IFileFragment ff : ret) {
            final DefaultWorkflowResult dwr = new DefaultWorkflowResult(ff.getUri(), this, getWorkflowSlot(), ff);
            getWorkflow().append(dwr);
            ff.save();
        }

        return new TupleND<IFileFragment>(ret);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.GENERAL_PREPROCESSING;
    }

    /**
     * Getter.
     *
     * @param ff  file fragment
     * @param spm scans per modulation
     * @return scanlines
     */
    private List<Array> getScanlineFor(final IFileFragment ff, final int spm) {
        IVariableFragment ticVar = ff.getChild(this.totalIntensity);
        IVariableFragment scsiv = ff.getChild(this.secondColumnScanIndexVar);
        ticVar.setIndex(scsiv);
        final List<Array> scanlines = ff.getChild(this.totalIntensity).
            getIndexedArray();
        return scanlines;
    }
}
