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
 * $Id: MZIDynamicTimeWarp.java 116 2010-06-17 08:46:30Z nilshoffmann $
 */

package maltcms.commands.distances.dtw;

import java.util.List;

import maltcms.tools.MaltcmsTools;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import cross.annotations.Configurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tools.EvalTools;

/**
 * Implementation of Pairwise Dynamic-Time-Warping for time-series data with an
 * evenly gridded array of mass over charge (mz) vs intensity for each
 * time-point (scan).
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class MZIDynamicTimeWarp extends ADynamicTimeWarp {

	@Configurable
	private int numberOfEICsToSelect = 0;

	@Configurable
	private boolean useSparseArrays = false;

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
		this.numberOfEICsToSelect = cfg.getInt(this.getClass().getName()
		        + ".numberOfEICsToSelect", 0);
		this.useSparseArrays = cfg.getBoolean(this.getClass().getName()
		        + ".useSparseArrays", false);
	}

	@Override
	public Tuple2D<List<Array>, List<Array>> createTuple(
	        final Tuple2D<IFileFragment, IFileFragment> t) {
		List<Array> intens1 = null;
		List<Array> intens2 = null;
		if (this.useSparseArrays) {
			final Tuple2D<Double, Double> tple = MaltcmsTools
			        .getMinMaxMassRange(t.getFirst(), t.getSecond());
			synchronized (t.getFirst()) {
				intens1 = MaltcmsTools.prepareSparseMZI(t.getFirst(),
				        this.scan_index, this.mass_values,
				        this.intensity_values, tple.getFirst(), tple
				                .getSecond());
			}
			synchronized (t.getSecond()) {
				intens2 = MaltcmsTools.prepareSparseMZI(t.getSecond(),
				        this.scan_index, this.mass_values,
				        this.intensity_values, tple.getFirst(), tple
				                .getSecond());
			}
		} else {
			synchronized (t.getFirst()) {
				final IVariableFragment index1 = t.getFirst().getChild(
				        "binned_" + this.scan_index);
				if (t.getFirst().getChild("binned_" + this.mass_values)
				        .getIndex() == null) {
					t.getFirst().getChild("binned_" + this.mass_values)
					        .setIndex(index1);
					t.getFirst().getChild("binned_" + this.intensity_values)
					        .setIndex(index1);
				}
				intens1 = t.getFirst().getChild(
				        "binned_" + this.intensity_values).getIndexedArray();
				EvalTools.notNull(intens1, this);
			}
			synchronized (t.getSecond()) {
				final IVariableFragment index2 = t.getSecond().getChild(
				        "binned_" + this.scan_index);
				if (t.getSecond().getChild("binned_" + this.mass_values)
				        .getIndex() == null) {
					t.getSecond().getChild("binned_" + this.mass_values)
					        .setIndex(index2);
					t.getSecond().getChild("binned_" + this.intensity_values)
					        .setIndex(index2);
				}
				intens2 = t.getSecond().getChild(
				        "binned_" + this.intensity_values).getIndexedArray();
				EvalTools.notNull(intens2, this);
			}
		}

		Tuple2D<List<Array>, List<Array>> tuple = null;
		// If set to a number > 0, try to select as many eics according to their
		// rank by variance (decreasing)
		if (this.numberOfEICsToSelect > 0) {
			this.log.info("Using {} eics with highest variance",
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
			tuple = new Tuple2D<List<Array>, List<Array>>(MaltcmsTools
			        .copyEics(intens1, pairedEics), MaltcmsTools.copyEics(
			        intens2, pairedEics));

		} else {// if set to 0, simply use all eics
			this.log.info("Using all eics");
			tuple = new Tuple2D<List<Array>, List<Array>>(intens1, intens2);
		}

		this.ref_num_scans = intens1.size();
		this.query_num_scans = intens2.size();
		return tuple;
	}

}
