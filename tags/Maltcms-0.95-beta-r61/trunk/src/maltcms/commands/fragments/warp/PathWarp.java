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

/**
 * 
 */
package maltcms.commands.fragments.warp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import maltcms.datastructures.alignment.AnchorPairSet;
import maltcms.datastructures.ms.IAnchor;
import maltcms.datastructures.warp.MZIWarpInput;
import maltcms.tools.MaltcmsTools;

import org.apache.commons.configuration.Configuration;
import org.mozilla.javascript.edu.emory.mathcs.backport.java.util.Arrays;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.MAMath;
import ucar.nc2.Dimension;
import annotations.Configurable;
import cross.Factory;
import cross.Logging;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragmentFactory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import cross.tools.ArrayTools;
import cross.tools.EvalTools;
import cross.tools.FileTools;
import cross.tools.FragmentTools;

/**
 * Instead of warping one time series to the time scale of another one, warp
 * both to their common time, given by the steps of the alignment map.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class PathWarp extends AFragmentCommand {

	private final Logger log = Logging.getLogger(this.getClass());
	@Configurable(name = "var.total_intensity")
	private String total_intensity = "total_intensity";
	@Configurable(name = "var.anchors.retention_scans")
	private String riname = "retention_scans";
	private String rinamename = "retention_index_names";
	private String indexVar = "scan_index";
	private String massValuesVar = "mass_values";
	private String intensValuesVar = "intensity_values";
	private boolean average = false;

	public IFileFragment apply(final MZIWarpInput m, final IFileFragment tf) {
		processIndexedArrays(m, tf);
		processArrays(m, tf);
		int qlen = m.getQueryFileFragment().getChild(this.total_intensity)
		        .getArray().getShape()[0];
		int rlen = m.getReferenceFileFragment().getChild(this.total_intensity)
		        .getArray().getShape()[0];
		processRIs(m, tf, rlen, qlen);
		// tf.addSourceFile(m.getReferenceFileFragment());
		// tf.addSourceFile(m.getQueryFileFragment());
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
			final MZIWarpInput mwi = new MZIWarpInput(ff, getIWorkflow());
			final IFileFragment tf = FileFragmentFactory.getInstance().create(
			        new File(FileTools.prependDefaultDirs(this.getClass(),
			                getIWorkflow().getStartupDate()), ff.getName()),
			        this.getClass());
			final IFileFragment f = apply(mwi, tf);
			this.log.debug("Path AWarp saving to {}", tf.getName());
			final DefaultWorkflowResult dwr = new DefaultWorkflowResult(
			        new File(f.getAbsolutePath()), this, getWorkflowSlot());
			getIWorkflow().append(dwr);
			f.save();
			ret.add(f);
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
		this.average = cfg.getBoolean(this.getClass().getName()
		        + ".averageCompressions", false);
	}

	@Override
	public String getDescription() {
		return "Warps binned mass spectra according to a given alignment map. Merges aligned mass spectra to mean mass spectra.";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
	 */
	@Override
	public WorkflowSlot getWorkflowSlot() {
		return WorkflowSlot.WARPING;
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
			vf.setDimensions(new Dimension[] { new Dimension(srcd.getName(), m
			        .getPath().size(), srcd.isShared(), srcd.isUnlimited(),
			        srcd.isVariableLength()) });
			final Array warped = Array.factory(r.getElementType(),
			        new int[] { m.getPath().size() });
			EvalTools.eqI(r.getRank(), 1, this);
			EvalTools.eqI(warped.getRank(), 1, this);
			final Index warpedIndex = warped.getIndex();
			final Index qi = q.getIndex();
			final Index ri = r.getIndex();
			int idx = 0;
			for (final Tuple2DI t : m.getPath()) {
				// this.log.debug("Processing {},{} with sizes {}, {}",
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
			this.log.warn("Could not process " + varname);
		}
	}

	public void processArrays(final MZIWarpInput m, final IFileFragment tf) {
		final ArrayList<String> al = FragmentTools.getDefaultVars();
		for (final String s : al) {
			if (!s.equals(Factory.getInstance().getConfiguration().getString(
			        "var.binned_mass_values"))
			        && !s.equals(Factory.getInstance().getConfiguration()
			                .getString("var.binned_intensity_values"))
			        && !s.equals(Factory.getInstance().getConfiguration()
			                .getString("var.binned_scan_index"))
			        && !s.equals(Factory.getInstance().getConfiguration()
			                .getString("var.scan_index"))
			        && !s.equals(Factory.getInstance().getConfiguration()
			                .getString("var.mass_values"))
			        && !s.equals(Factory.getInstance().getConfiguration()
			                .getString("var.intensity_values"))) {
				processArray(m, tf, s);

			}
		}
	}

	// public void processIndexedArrays(final MZIWarpInput m,
	// final IFileFragment tf) {
	//
	// final IVariableFragment lhssi = m.getReferenceFileFragment().getChild(
	// "binned_scan_index");
	// final IVariableFragment lhsm = m.getReferenceFileFragment().getChild(
	// "binned_mass_values");
	// final IVariableFragment lhsi = m.getReferenceFileFragment().getChild(
	// "binned_intensity_values");
	//
	// final IVariableFragment rhssi = m.getQueryFileFragment().getChild(
	// "binned_scan_index");
	// final IVariableFragment rhsm = m.getQueryFileFragment().getChild(
	// "binned_mass_values");
	// final IVariableFragment rhsi = m.getQueryFileFragment().getChild(
	// "binned_intensity_values");
	//
	// lhsm.setIndex(lhssi);
	// lhsi.setIndex(lhssi);
	//
	// rhsm.setIndex(rhssi);
	// rhsi.setIndex(rhssi);
	//
	// final List<Array> lhs = lhsi.getIndexedArray();
	// final List<Array> rhs = rhsi.getIndexedArray();
	// final ArrayList<Array> target = new ArrayList<Array>(m.getPath().size());
	// final ArrayList<Array> targetMasses = new ArrayList<Array>(m.getPath()
	// .size());
	// final List<Array> masses = lhsm.getIndexedArray();
	// this.log.debug("Path length = {}", target.size());
	// for (final Tuple2DI t : m.getPath()) {
	// // if (t.getFirst() < lhs.size() && t.getSecond() < rhs.size()) {
	// EvalTools.eqI(lhs.get(t.getFirst()).getRank(), rhs.get(
	// t.getSecond()).getRank(), this);
	// // this.log.debug("Processing {},{} with sizes {}, {}", new
	// // Object[]
	// // {
	// // t.getFirst(), t.getSecond(), lhs.size(), rhs.size() });
	// Array a = ArrayTools.sum(lhs.get(t.getFirst()), rhs.get(t
	// .getSecond()));
	// a = ArrayTools.mult(a, 0.5d);
	// target.add(a);// rhs.get(t.getSecond()).copy());
	//
	// targetMasses.add(masses.get(t.getFirst()));
	// // } else {
	// // this.log.warn("Skipping illegal mapping {}", t);
	// // }
	// }
	//
	// //
	//
	// MaltcmsTools.createFlattenedArrays(tf, targetMasses, target);
	// }

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
		log.debug("lhs scans: " + lhsI.size() + " rhs scans: " + rhsI.size());
		log
		        .debug("lhs points: "
		                + Arrays.toString(lhsi.getArray().getShape())
		                + " rhs points: "
		                + Arrays.toString(rhsi.getArray().getShape()));
		Tuple2D<List<Array>, List<Array>> t = ArrayTools.merge2(lhsM, lhsI,
		        rhsM, rhsI, m.getPath(), average);
		List<Array> mergedMasses = t.getFirst();
		List<Array> mergedIntens = t.getSecond();
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

		List<Array> warpedMasses = new ArrayList<Array>();
		for (Array a : mergedMasses) {
			ArrayDouble.D1 ad = new ArrayDouble.D1(a.getShape()[0]);
			MAMath.copyDouble(ad, a);
			warpedMasses.add(ad);
		}
		ivf1.setIndexedArray(warpedMasses);
		List<Array> warpedIntensities = new ArrayList<Array>();
		for (Array a : mergedIntens) {
			ArrayDouble.D1 ad = new ArrayDouble.D1(a.getShape()[0]);
			MAMath.copyDouble(ad, a);
			warpedIntensities.add(ad);
		}
		ivf2.setIndexedArray(warpedIntensities);
		// }
	}

	public void processRIs(final MZIWarpInput m, final IFileFragment tf,
	        final int refScanNo, final int queryScanNo) {

		try {
			Tuple2D<List<IAnchor>, List<IAnchor>> t = MaltcmsTools.getAnchors(m
			        .getReferenceFileFragment(), m.getQueryFileFragment());
			AnchorPairSet aps = new AnchorPairSet(t.getFirst(), t.getSecond(),
			        refScanNo, queryScanNo);
			final ArrayList<Integer> ri = new ArrayList<Integer>();
			final ArrayList<String> riName = new ArrayList<String>();
			int pairindex = 0;
			List<Tuple2D<Integer, Integer>> cscans = aps
			        .getCorrespondingScans();
			for (final Tuple2DI pair : m.getPath()) {
				Tuple2D<Integer, Integer> ps = cscans.get(pairindex);
				if (ps.getFirst() == pair.getFirst()
				        && ps.getSecond() == pair.getSecond()) {
					String name = null;
					for (IAnchor ia : t.getFirst()) {
						if (ia.getScanIndex() == ps.getFirst()) {
							name = ia.getName();
							riName.add(name);
						}
					}
					ri.add(Integer.valueOf(pairindex));
					pairindex++;
				}
			}
			ArrayInt.D1 target = new ArrayInt.D1(ri.size());
			int i = 0;
			for (Integer itg : ri) {
				target.set(i++, itg.intValue());
			}
			ArrayChar.D2 names = ArrayTools.createStringArray(riName.size(),
			        128);
			i = 0;
			for (String s : riName) {
				names.setString(i++, s);
			}
			final IVariableFragment rif = new VariableFragment(tf, this.riname);
			rif.setArray(target);
			final IVariableFragment rin = new VariableFragment(tf,
			        this.rinamename);
			rin.setArray(names);
		} catch (final ResourceNotAvailableException e) {
			this.log.warn("Could not process " + this.riname);
		}

	}
}
