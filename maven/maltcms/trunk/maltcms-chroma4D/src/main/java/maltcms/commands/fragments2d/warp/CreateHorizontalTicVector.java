/*
 * Copyright (C) 2009, 2010 Mathias Wilhelm mwilhelm A T
 * TechFak.Uni-Bielefeld.DE
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
 * $Id: CreateHorizontalTicVector.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */
package maltcms.commands.fragments2d.warp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import maltcms.commands.fragments2d.testing.Visualization2D;
import maltcms.tools.ArrayTools2;
import maltcms.tools.MaltcmsTools;
import maltcms.tools.PathTools;
import ucar.ma2.Array;
import ucar.ma2.Index;
import cross.Factory;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.lookup.ServiceProvider;

/**
 * Creates all reference-query horizontal tic scanlines vectors.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
@Slf4j
@Data
@RequiresVariables(names = { "var.total_intensity", "var.modulation_time",
        "var.scan_rate", "var.second_column_scan_index" })
@RequiresOptionalVariables(names = { "" })
@ProvidesVariables(names = { "" })
@ServiceProvider(service=AFragmentCommand.class)
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
	@Configurable(name = "var.second_column_scan_index", value = "second_column_scan_index")
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

		final IFileFragment pwHorizontalAlignmentFragment = MaltcmsTools
		        .getPairwiseDistanceFragment(t, "-horizontal");

		final ArrayList<IFileFragment> ret = new ArrayList<IFileFragment>();
		IFileFragment fret;
		for (IFileFragment ff : t) {
			fret = Factory.getInstance().getFileFragmentFactory().create(
			        new File(getWorkflow().getOutputDirectory(this), ff
			                .getName()));
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

				modulationi = ref.getChild(this.modulationVar).getArray()
				        .getDouble(idx);
				modulationj = query.getChild(this.modulationVar).getArray()
				        .getDouble(idx);
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

				horizontalRefScanlines = ArrayTools2.transpose(scanlines
				        .getFirst());
				horizontalQueryScanlines = ArrayTools2.transpose(scanlines
				        .getSecond());

				refname = StringTools.removeFileExt(ref.getName());
				queryname = StringTools.removeFileExt(query.getName());

				hrs = new VariableFragment(resRef, refname + "_" + queryname
				        + "-tv");
				hrs.setIndexedArray(horizontalRefScanlines);
				hrsIdx = new VariableFragment(resRef, refname + "_" + queryname
				        + "-idx");
				hrsIdx.setArray(ArrayTools2
				        .getIndexArray(horizontalRefScanlines));

				hqs = new VariableFragment(resQuery, queryname + "_" + refname
				        + "-tv");
				hqs.setIndexedArray(horizontalQueryScanlines);
				hqsIdx = new VariableFragment(resQuery, queryname + "_"
				        + refname + "-idx");
				hqsIdx.setArray(ArrayTools2
				        .getIndexArray(horizontalQueryScanlines));
			}
		}

		for (IFileFragment ff : ret) {
			final DefaultWorkflowResult dwr = new DefaultWorkflowResult(
			        new File(ff.getAbsolutePath()), this, getWorkflowSlot(), ff);
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
	 * @param ff
	 *            file fragment
	 * @param spm
	 *            scans per modulation
	 * @return scanlines
	 */
	private List<Array> getScanlineFor(final IFileFragment ff, final int spm) {
		ff.getChild(this.totalIntensity).setIndex(
		        ff.getChild(this.secondColumnScanIndexVar));
		final List<Array> scanlines = ff.getChild(this.totalIntensity)
		        .getIndexedArray();
		return scanlines;
	}

}
