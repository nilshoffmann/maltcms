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

package maltcms.datastructures.fragments;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayDouble.D2;
import cross.IConfigurable;
import cross.datastructures.fragments.FileFragmentFactory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.io.IFileFragmentProvider;
import cross.tools.ArrayTools;
import cross.tools.EvalTools;
import cross.tools.FileTools;

public class PairwiseDistances implements IFileFragmentProvider, IConfigurable {

	/**
	 * Static factory method which reconstructs a PairwiseDistances object from
	 * the given FileFragment.
	 * 
	 * @param pwd
	 *            the FileFragment from which to construct PairwiseDistances
	 * @return
	 */
	public static PairwiseDistances fromFileFragment(final IFileFragment pwdFrag) {
		final PairwiseDistances pwd = new PairwiseDistances();
		pwd.pwDistFileFragment = pwdFrag;
		return pwd;
	}

	private IFileFragment pwDistFileFragment;

	private ArrayDouble.D2 pairwiseDistances;

	private String pwDistMatrixVariableName = "pairwise_distance_matrix";

	private String pwDistVariableName = "pairwise_distance_names";

	private String pwDistAlignmentsVarName = "pairwise_distance_alignment_names";

	private String name = "pairwise_distances.cdf";

	private boolean minimize;

	private String minArrayComp;

	private ucar.ma2.ArrayChar.D2 names;

	private TupleND<IFileFragment> alignments;

	private IWorkflow iw;

	@Override
	public void configure(final Configuration cfg) {
		this.pwDistMatrixVariableName = cfg.getString(
		        "var.pairwise_distance_matrix", "pairwise_distance_matrix");
		this.pwDistVariableName = cfg.getString("var.pairwise_distance_names",
		        "pairwise_distance_names");
		this.pwDistAlignmentsVarName = cfg.getString(
		        "var.pairwise_distance_alignment_names",
		        "pairwise_distance_alignment_names");
		this.name = cfg.getString("pairwise_distances_file_name",
		        "pairwise_distances.cdf");

	}

	public TupleND<IFileFragment> getAlignments() {
		return this.alignments;
	}

	public IWorkflow getIWorkflow() {
		return this.iw;
	}

	/**
	 * @return the minArrayComp
	 */
	public String getMinArrayComp() {
		return this.minArrayComp;
	}

	/**
	 * @return the pairwiseDistances
	 */
	public ArrayDouble.D2 getPairwiseDistances() {
		return this.pairwiseDistances;
	}

	public String getPwDistAlignmentsVarName() {
		return this.pwDistAlignmentsVarName;
	}

	/**
	 * @return the pwDistMatrixVariableName
	 */
	public String getPwDistMatrixVariableName() {
		return this.pwDistMatrixVariableName;
	}

	/**
	 * @return the pwDistVariableName
	 */
	public String getPwDistVariableName() {
		return this.pwDistVariableName;
	}

	/**
	 * @return the minimize
	 */
	public boolean isMinimize() {
		return this.minimize;
	}

	@Override
	public IFileFragment provideFileFragment() {
		if (this.pwDistFileFragment == null) {
			this.pwDistFileFragment = FileFragmentFactory.getInstance().create(
			        FileTools.prependDefaultDirs(this.name, this.getClass(),
			                this.iw.getStartupDate()), this.getClass());
			final IVariableFragment pwd = new VariableFragment(
			        this.pwDistFileFragment, this.pwDistMatrixVariableName);
			pwd.setArray(this.pairwiseDistances);
			final IVariableFragment na = new VariableFragment(
			        this.pwDistFileFragment, this.pwDistVariableName);
			na.setArray(this.names);
			final IVariableFragment minimizing = new VariableFragment(
			        this.pwDistFileFragment, this.minArrayComp);
			final ArrayInt.D0 ab = new ArrayInt.D0();
			ab.set(this.minimize ? 1 : 0);
			minimizing.setArray(ab);
			final IVariableFragment alignments = new VariableFragment(
			        this.pwDistFileFragment, this.pwDistAlignmentsVarName);
			int maxlength = 128;
			for (final IFileFragment iff : this.alignments) {
				if (iff.getAbsolutePath().length() > maxlength) {
					maxlength = iff.getAbsolutePath().length();
				}
			}
			final ArrayChar.D2 anames = ArrayTools.createStringArray(
			        this.alignments.getSize(), maxlength);
			int i = 0;
			for (final IFileFragment iff : this.alignments) {
				anames.setString(i++, iff.getAbsolutePath());
			}
			alignments.setArray(anames);
		}
		return this.pwDistFileFragment;
	}

	public void setAlignments(final TupleND<IFileFragment> t) {
		this.alignments = t;
	}

	public void setIsMinimizing(final boolean minimizingLocalDistance) {
		this.minimize = minimizingLocalDistance;
	}

	public void setIWorkflow(final IWorkflow iw1) {
		this.iw = iw1;
	}

	/**
	 * @param minArrayComp
	 *            the minArrayComp to set
	 */
	public void setMinArrayComp(final String minArrayComp) {
		this.minArrayComp = minArrayComp;
	}

	/**
	 * @param minimize
	 *            the minimize to set
	 */
	public void setMinimize(final boolean minimize) {
		this.minimize = minimize;
	}

	public void setMinimizingArrayComp(final String minArrayComp1) {
		EvalTools.notNull(minArrayComp1, this);
		this.minArrayComp = minArrayComp1;
	}

	public void setName(final String name1) {
		this.name = name1;
	}

	public void setNames(final ucar.ma2.ArrayChar.D2 names1) {
		EvalTools.notNull(names1, this);
		this.names = names1;
	}

	public void setPairwiseDistances(final D2 pairwiseDistances1) {
		EvalTools.notNull(pairwiseDistances1, this);
		this.pairwiseDistances = pairwiseDistances1;
	}

	public void setPwDistAlignmentsVarName(final String pwDistAlignmentsVarName) {
		this.pwDistAlignmentsVarName = pwDistAlignmentsVarName;
	}

	/**
	 * @param pwDistMatrixVariableName
	 *            the pwDistMatrixVariableName to set
	 */
	public void setPwDistMatrixVariableName(
	        final String pwDistMatrixVariableName) {
		this.pwDistMatrixVariableName = pwDistMatrixVariableName;
	}

	/**
	 * @param pwDistVariableName
	 *            the pwDistVariableName to set
	 */
	public void setPwDistVariableName(final String pwDistVariableName) {
		this.pwDistVariableName = pwDistVariableName;
	}

}
