/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
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
 * $Id$
 */
package maltcms.commands.fragments.alignment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import maltcms.commands.fragments.warp.ChromatogramWarp;
import maltcms.datastructures.fragments.MultipleAlignment;
import maltcms.tools.MaltcmsTools;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayChar.StringIterator;
import cross.Factory;
import cross.Logging;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragmentFactory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ConstraintViolationException;
import cross.exception.ResourceNotAvailableException;
import cross.io.csv.CSVWriter;
import cross.tools.FileTools;
import cross.tools.FragmentTools;
import cross.tools.ImageTools;
import cross.tools.StringTools;

/**
 * Implementation of the center star approximation for multiple alignment.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class CenterStarAlignment extends AFragmentCommand {

	private String pairwiseDistanceMatrixVariableName;
	private String pairwiseDistanceNamesVariableName;
	private String minimizingArrayCompVariableName;
	private boolean minimizeDist;
	private boolean drawTICs = false;
	private boolean drawEICs = false;
	private boolean alignToFirst = false;

	private final Logger log = Logging.getLogger(this);

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.ICommand#apply(java.lang.Object)
	 */
	@Override
	public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {

		if (this.drawTICs) {
			final File utics = ImageTools.drawTICS(this.getClass(), t, Factory
			        .getInstance().getConfiguration().getString(
			                "var.total_intensity", "total_intensity"), Factory
			        .getInstance().getConfiguration().getString(
			                "var.scan_acquisition_time",
			                "scan_acquisition_time"), null,
			        "unaligned-tics.png", getIWorkflow().getStartupDate());
			DefaultWorkflowResult dwrut = new DefaultWorkflowResult(utics,
			        this, WorkflowSlot.VISUALIZATION);
			getIWorkflow().append(dwrut);
		}
		if (this.drawEICs) {
			final List<IFileFragment> unalignedEICFragments = MaltcmsTools
			        .prepareEICFragments(t, this.getClass(), getIWorkflow()
			                .getStartupDate());
			File[] files = ImageTools.drawEICs(
			        this.getClass(),
			        // new TupleND<IFileFragment>(unalignedEICFragments),
			        // Factory
			        t, Factory.getInstance().getConfiguration().getString(
			                "var.scan_acquisition_time",
			                "scan_acquisition_time"), null, "unaligned",
			        getIWorkflow().getStartupDate());
			for (final File file : files) {
				DefaultWorkflowResult dwrut = new DefaultWorkflowResult(file,
				        this, WorkflowSlot.VISUALIZATION);
				getIWorkflow().append(dwrut);
			}
		}
		final IFileFragment pwd = MaltcmsTools.getPairwiseDistanceFragment();
		final ArrayDouble.D2 pwdist = (ArrayDouble.D2) pwd.getChild(
		        this.pairwiseDistanceMatrixVariableName).getArray();
		final ArrayChar.D2 names1 = (ArrayChar.D2) pwd.getChild(
		        this.pairwiseDistanceNamesVariableName).getArray();
		final ArrayInt.D0 minimizeDistA = (ArrayInt.D0) pwd.getChild(
		        this.minimizingArrayCompVariableName).getArray();
		if (minimizeDistA.get() == 0) {
			this.minimizeDist = false;
		} else {
			this.minimizeDist = true;
		}
		// Second step:
		// find sequence minimizing distance to all other sequences
		final int center = findCenterSequence(pwdist, names1);

		final IFileFragment centerSeq = FileFragmentFactory.getInstance()
		        .getFragment(names1.getString(center));
		this.log.info("Center sequence is: {}", centerSeq);
		final TupleND<IFileFragment> alignments = getAlignmentsFromFragment(pwd);
		final MultipleAlignment ma = new MultipleAlignment(centerSeq);
		for (final IFileFragment iff : alignments) {
			ma.addColumn(iff);
		}
		final List<IFileFragment> warpedFiles = saveAlignment(this.getClass(),
		        t, alignments, Factory.getInstance().getConfiguration()
		                .getString("var.total_intensity", "total_intensity"),
		        centerSeq);
		// Draw aligned TICs
		// File atics = ImageTools.drawAlignedTICS(this.getClass(), t,
		// alignments,
		// Factory.getInstance().getConfiguration().getString(
		// "var.total_intensity",
		// "total_intensity"),centerSeq);
		// DefaultWorkflowResult dwrat = new
		// DefaultWorkflowResult(atics,this,WorkflowSlot.VISUALIZATION);
		// getIWorkflow().append(dwrat);
		if (this.drawTICs) {
			final File atics = ImageTools.drawTICS(this.getClass(),
			        new TupleND<IFileFragment>(warpedFiles), Factory
			                .getInstance().getConfiguration().getString(
			                        "var.total_intensity", "total_intensity"),
			        Factory.getInstance().getConfiguration().getString(
			                "var.scan_acquisition_time",
			                "scan_acquisition_time"), warpedFiles.get(0),
			        "aligned-tics.png", getIWorkflow().getStartupDate());
			DefaultWorkflowResult dwrat = new DefaultWorkflowResult(atics,
			        this, WorkflowSlot.VISUALIZATION);
			getIWorkflow().append(dwrat);
		}

		if (this.drawEICs) {
			File[] files = ImageTools
			        .drawEICs(this.getClass(), new TupleND<IFileFragment>(
			                warpedFiles), Factory.getInstance()
			                .getConfiguration().getString(
			                        "var.scan_acquisition_time",
			                        "scan_acquisition_time"), warpedFiles
			                .get(0), "aligned", getIWorkflow().getStartupDate());
			for (final File file : files) {
				DefaultWorkflowResult dwrat = new DefaultWorkflowResult(file,
				        this, WorkflowSlot.VISUALIZATION);
				getIWorkflow().append(dwrat);
			}
		}
		for (final IFileFragment iff : warpedFiles) {
			iff.clearArrays();
		}
		return t;
	}

	@Override
	public void configure(final Configuration cfg) {
		this.pairwiseDistanceMatrixVariableName = cfg.getString(
		        "var.pairwise_distance_matrix", "pairwise_distance_matrix");
		this.pairwiseDistanceNamesVariableName = cfg.getString(
		        "var.pairwise_distance_names", "pairwise_distance_names");
		this.minimizingArrayCompVariableName = cfg.getString(
		        "var.minimizing_array_comp", "minimizing_array_comp");
		this.drawTICs = cfg.getBoolean(
		        "cross.tools.ImageTools.createTICCharts", false);
		this.drawEICs = cfg.getBoolean(
		        "cross.tools.ImageTools.createEICCharts", false);
		this.alignToFirst = cfg.getBoolean(this.getClass().getName()
		        + ".alignToFirst", false);
	}

	private int findCenterSequence(final ArrayDouble.D2 a,
	        final ArrayChar.D2 names) {
		if (this.alignToFirst) {
			return 0;
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
			this.log.info("Sum of values for {}={}", names.getString(i),
			        sums[i]);
		}

		int optIndex = -1;
		double optVal = this.minimizeDist ? Double.POSITIVE_INFINITY
		        : Double.NEGATIVE_INFINITY;
		for (int i = 0; i < rows; i++) {
			if (this.minimizeDist) {
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
		return optIndex;
	}

	private TupleND<IFileFragment> getAlignmentsFromFragment(
	        final IFileFragment pwd) {
		final TupleND<IFileFragment> tpl = new TupleND<IFileFragment>();
		try {
			final ArrayChar.D2 aliNames = (ArrayChar.D2) pwd.getChild(
			        "pairwise_distance_alignment_names").getArray();
			final StringIterator si = aliNames.getStringIterator();
			while (si.hasNext()) {
				final IFileFragment f = FileFragmentFactory.getInstance()
				        .create(new File(si.next()));
				tpl.add(f);
			}
		} catch (final ResourceNotAvailableException rne) {
			this.log.warn("{}", rne.getLocalizedMessage());
		}
		return tpl;
	}

	@Override
	public String getDescription() {
		return "Creates a multiple alignment by selecting a reference chromatogram based on highest overall similarity or lowest overall distance of reference to other chromatograms.";
	}

	/**
	 * @return the minimizingArrayCompVariableName
	 */
	public String getMinimizingArrayCompVariableName() {
		return this.minimizingArrayCompVariableName;
	}

	private IFileFragment getOriginalFileFor(final IFileFragment iff) {
		final List<IFileFragment> l = Factory.getInitialFiles();
		final String iffname = StringTools.removeFileExt(iff.getName());
		for (final IFileFragment f : l) {
			final String fname = StringTools.removeFileExt(f.getName());

			if (iffname.equals(fname)) {
				return f;
			}
		}
		throw new ConstraintViolationException(
		        "Could not find original file, returning argument!");
		// return iff;
	}

	/**
	 * @return the pairwiseDistanceMatrixVariableName
	 */
	public String getPairwiseDistanceMatrixVariableName() {
		return this.pairwiseDistanceMatrixVariableName;
	}

	/**
	 * @return the pairwiseDistanceNamesVariableName
	 */
	public String getPairwiseDistanceNamesVariableName() {
		return this.pairwiseDistanceNamesVariableName;
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
	 * @return the minimizeDist
	 */
	public boolean isMinimizeDist() {
		return this.minimizeDist;
	}

	private List<IFileFragment> saveAlignment(final Class<?> creator,
	        final TupleND<IFileFragment> t,
	        final TupleND<IFileFragment> alignment, final String ticvar,
	        final IFileFragment top) {
		// Set representative name
		final String refname = top.getName();
		// List to hold alignments, where representative is LHS
		final ArrayList<IFileFragment> repOnLHS = new ArrayList<IFileFragment>();
		// List to hold alignment, where representative is RHS
		final ArrayList<IFileFragment> repOnRHS = new ArrayList<IFileFragment>();
		// List of warped files
		final List<IFileFragment> warped = new ArrayList<IFileFragment>();
		// warped.add(top);
		final ChromatogramWarp cw = Factory.getInstance().instantiate(
		        ChromatogramWarp.class);
		final IFileFragment centerCopy = cw.copyReference(
		        getOriginalFileFor(top), getIWorkflow());
		centerCopy.addSourceFile(top);
		DefaultWorkflowResult dwr = new DefaultWorkflowResult(new File(
		        centerCopy.getAbsolutePath()), this, WorkflowSlot.WARPING);
		getIWorkflow().append(dwr);
		warped.add(centerCopy);

		for (final IFileFragment iff : alignment) {
			this.log.debug("Processing {}", iff.getAbsolutePath());
			final IFileFragment ref = FragmentTools.getLHSFile(iff);
			if (ref.getName().equals(refname)) {
				repOnLHS.add(iff);
			} else if (refname.equals(FragmentTools.getRHSFile(iff).getName())) {
				repOnRHS.add(iff);
			}
		}
		final ArrayList<Tuple2D<String, Array>> a = new ArrayList<Tuple2D<String, Array>>(
		        repOnLHS.size() + repOnRHS.size() + 1);
		this.log.debug(
		        "#alignments to reference: {}, #alignments to query: {}",
		        repOnLHS.size(), repOnRHS.size());
		String topName = StringTools.removeFileExt(top.getName());
		if (alignment.size() > 1) {// only add a star if we have more than one
			// alignment
			topName = topName + " *";
		}
		a.add(new Tuple2D<String, Array>(topName, top.getChild(ticvar)
		        .getArray()));
		final Vector<Vector<String>> table = new Vector<Vector<String>>();
		final Array topa = top.getChild(ticvar).getArray();
		final Vector<String> topCol = new Vector<String>(topa.getShape()[0] + 1);
		topCol.add(StringTools.removeFileExt(top.getName()));
		for (int i = 0; i < topa.getShape()[0]; i++) {
			topCol.add(i + "");
		}
		table.add(topCol);
		// top/representative is on lhs side of alignment
		for (int i = 0; i < repOnLHS.size(); i++) {
			final List<Tuple2DI> al = MaltcmsTools.getWarpPath(repOnLHS.get(i));
			final IFileFragment ifwarped = cw.warp(top,
			        getOriginalFileFor(FragmentTools
			                .getRHSFile(repOnLHS.get(i))), FragmentTools
			                .getRHSFile(repOnLHS.get(i)), al, true,
			        getIWorkflow());
			dwr = new DefaultWorkflowResult(
			        new File(ifwarped.getAbsolutePath()), this,
			        WorkflowSlot.WARPING);
			getIWorkflow().append(dwr);
			warped.add(ifwarped);
			final String name = StringTools.removeFileExt(FragmentTools
			        .getRHSFile(repOnLHS.get(i)).getName());
			final Vector<String> column = new Vector<String>();
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
				this.log.debug("value={}", value);
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
		// Alignments where rep is on rhs
		// so retrieve warp path and lhs array and warp to rhs array via path

		for (int i = 0; i < repOnRHS.size(); i++) {
			final List<Tuple2DI> al = MaltcmsTools.getWarpPath(repOnRHS.get(i));
			final IFileFragment ifwarped = cw.warp(top,
			        getOriginalFileFor(FragmentTools
			                .getLHSFile(repOnRHS.get(i))), FragmentTools
			                .getLHSFile(repOnRHS.get(i)), al, false,
			        getIWorkflow());
			dwr = new DefaultWorkflowResult(
			        new File(ifwarped.getAbsolutePath()), this,
			        WorkflowSlot.WARPING);
			getIWorkflow().append(dwr);
			warped.add(ifwarped);
			final String name = StringTools.removeFileExt(FragmentTools
			        .getLHSFile(repOnRHS.get(i)).getName());
			final Vector<String> column = new Vector<String>();
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
				this.log.debug("value={}", value);
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
		final CSVWriter csv = Factory.getInstance()
		        .instantiate(CSVWriter.class);
		csv.setIWorkflow(getIWorkflow());
		csv.writeTableByCols(FileTools.prependDefaultDirs(creator,
		        getIWorkflow().getStartupDate()).getAbsolutePath(),
		        "multiple-alignment.csv", table, WorkflowSlot.ALIGNMENT);
		return warped;
	}

	/**
	 * @param minimizeDist
	 *            the minimizeDist to set
	 */
	public void setMinimizeDist(final boolean minimizeDist) {
		this.minimizeDist = minimizeDist;
	}

	/**
	 * @param minimizingArrayCompVariableName
	 *            the minimizingArrayCompVariableName to set
	 */
	public void setMinimizingArrayCompVariableName(
	        final String minimizingArrayCompVariableName) {
		this.minimizingArrayCompVariableName = minimizingArrayCompVariableName;
	}

	/**
	 * @param pairwiseDistanceMatrixVariableName
	 *            the pairwiseDistanceMatrixVariableName to set
	 */
	public void setPairwiseDistanceMatrixVariableName(
	        final String pairwiseDistanceMatrixVariableName) {
		this.pairwiseDistanceMatrixVariableName = pairwiseDistanceMatrixVariableName;
	}

	/**
	 * @param pairwiseDistanceNamesVariableName
	 *            the pairwiseDistanceNamesVariableName to set
	 */
	public void setPairwiseDistanceNamesVariableName(
	        final String pairwiseDistanceNamesVariableName) {
		this.pairwiseDistanceNamesVariableName = pairwiseDistanceNamesVariableName;
	}

}
