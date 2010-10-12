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

package maltcms.commands.fragments;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import maltcms.tools.MaltcmsTools;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;
import ucar.ma2.MAVector;
import annotations.ProvidesVariables;
import annotations.RequiresVariables;
import cross.Factory;
import cross.Logging;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.FileFragmentFactory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ConstraintViolationException;
import cross.tools.ArrayTools;
import cross.tools.EvalTools;
import cross.tools.FileTools;
import cross.tools.FragmentTools;

/**
 * Creates bins of fixed size (currently 1) from a given set of spectra with
 * masses and intensities. Can filter mass channels, whose intensity is then
 * removed from the chromatogram.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@ProvidesVariables(names = { "var.binned_mass_values",
        "var.binned_intensity_values", "var.binned_scan_index" })
@RequiresVariables(names = { "var.mass_values", "var.intensity_values",
        "var.scan_index", "var.scan_acquisition_time", "var.total_intensity" })
public class DenseArrayProducer extends AFragmentCommand {

	private int nthreads = 5;

	boolean normalize_scans = false;

	private String mass_values = "mass_values";

	private String intensity_values = "intensity_values";

	private String scan_index = "scan_index";

	private String total_intensity = "total_intensity";

	private String binned_intensity_values = "binned_intensity_values";

	private String binned_mass_values = "binned_mass_values";

	private String binned_scan_index = "binned_scan_index";

	private String mapping_file = "input_to_tmp_files.cdf",
	        mapping_file_var = "file_map";

	private final Logger log = Logging.getLogger(this.getClass());

	private String minvarname = "mass_range_min";

	private String maxvarname = "mass_range_max";

	private List<Double> maskedMasses = null;

	private boolean invertMaskedMasses;

	private double massBinResolution = 1.0d;

	public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
		final Collection<Tuple2D<IFileFragment, IFileFragment>> c = FragmentTools
		        .getFileMap();
		final int nt = Math.min(this.nthreads, t.size());
		// log.info("Using {} threads",nt);
		// ExecutorService es = Executors.newFixedThreadPool(nt);
		final ArrayList<IFileFragment> al = new ArrayList<IFileFragment>();
		if ((c != null) && !c.isEmpty()) {
			this.log.debug("Checking file map!");
			for (final IFileFragment f : t) {
				for (final Tuple2D<IFileFragment, IFileFragment> tuple : c) {
					if (f.getAbsolutePath().equals(
					        tuple.getFirst().getAbsolutePath())) {
						this.log
						        .debug(
						                "Found file {} with dense arrays and source {}",
						                tuple.getSecond(), f);
						al.add(tuple.getSecond());
						FragmentTools.loadDefaultVars(tuple.getSecond());
						FragmentTools.loadAdditionalVars(tuple.getSecond());
					}
				}
			}
		} else {
			this.log.debug("Creating dense arrays!");
			final ArrayList<Tuple2D<IFileFragment, IFileFragment>> map = new ArrayList<Tuple2D<IFileFragment, IFileFragment>>();
			final IFileFragment dense_array_files = FileFragmentFactory
			        .getInstance().create(
			                new File(FileTools.prependDefaultDirs(null,
			                        getIWorkflow().getStartupDate()),
			                        this.mapping_file), null);
			this.log.debug("Dense Array File Map: {}", dense_array_files);
			EvalTools.notNull(map, this);
			EvalTools.notNull(dense_array_files, this);
			this.log.debug("Looking for minimum and maximum values!");
			final Tuple2D<Double, Double> minmax = MaltcmsTools
			        .findGlobalMinMax(t, this.minvarname, this.maxvarname,
			                this.mass_values);
			EvalTools.notNull(minmax, this);
			this.log.debug("Minimum mass: {}; Maximum mass; {}", minmax
			        .getFirst(), minmax.getSecond());
			final IWorkflowElement iwe = this;
			for (final IFileFragment ff : t) {
				// Runnable r = new Runnable() {
				// @Override
				// public void run() {
				this.log.info("Loading scans for file {}", ff.getName());
				IFileFragment f = MaltcmsTools.prepareDenseArraysMZI(ff,
				        this.scan_index, this.mass_values,
				        this.intensity_values, this.binned_scan_index,
				        this.binned_mass_values, this.binned_intensity_values,
				        minmax.getFirst(), minmax.getSecond(), getIWorkflow()
				                .getStartupDate());
				// f.save();
				this.log.debug("Loaded scans for file {}, stored in {}", ff, f);
				this.log.debug("Source Files of f={} : {}", f, f
				        .getSourceFiles());
				final int bins = MaltcmsTools
				        .getNumberOfIntegerMassBins(minmax.getFirst(), minmax
				                .getSecond(), this.massBinResolution);
				// set masked masschannels to zero intensity
				if ((this.maskedMasses != null) && !this.maskedMasses.isEmpty()) {
					this.log.info("Filtering masked masses!");
					final ArrayDouble.D1 selector = new ArrayDouble.D1(bins);
					if (this.invertMaskedMasses) {
						ArrayTools.fill(selector, 1.0d);
						for (final Double integ : this.maskedMasses) {
							this.log.info("Retaining mass {} at index {}",
							        integ, MaltcmsTools.binMZ(integ, minmax
							                .getFirst(), minmax.getSecond(),
							                this.massBinResolution));

							selector.set(MaltcmsTools.binMZ(integ, minmax
							        .getFirst(), minmax.getSecond(),
							        this.massBinResolution), 0.0d);
							// - (int) (Math.floor(minmax.getFirst())), 0.0d);
						}
					} else {
						for (final Double integ : this.maskedMasses) {
							this.log.info("Filtering mass {} at index {}",
							        integ, MaltcmsTools.binMZ(integ, minmax
							                .getFirst(), minmax.getSecond(),
							                this.massBinResolution));
							selector.set(MaltcmsTools.binMZ(integ, minmax
							        .getFirst(), minmax.getSecond(),
							        this.massBinResolution), 1.0d);
							// - (int) (Math.floor(minmax.getFirst())), 1.0d);
						}
					}
					final IVariableFragment ivf = f
					        .getChild(this.binned_intensity_values);
					final IVariableFragment sidx = f
					        .getChild(this.binned_scan_index);
					final IVariableFragment total_intens = f
					        .getChild(this.total_intensity);
					final Array ta = total_intens.getArray();
					final Array tan = Array.factory(ta.getElementType(), ta
					        .getShape());
					final Index taidx = ta.getIndex();
					final Index tanidx = tan.getIndex();
					ivf.setIndex(sidx);
					final List<Array> intens = ivf.getIndexedArray();
					// Over all scans
					int scan = 0;
					final ArrayList<Array> filtered = new ArrayList<Array>(
					        intens.size());
					for (final Array a : intens) {
						final Index aidx = a.getIndex();
						EvalTools.eqI(1, a.getRank(), this);
						double accum = 0;
						for (int i = 0; i < a.getShape()[0]; i++) {
							if (selector.get(i) == 1.0d) {
								// log.info("Selector index {} = 1.0",i);
								aidx.set(i);
								accum += a.getDouble(aidx);
								a.setDouble(aidx, 0);
							}
						}
						this.log.debug("accumulated intensity = {} in scan {}",
						        accum, scan);
						taidx.set(scan);
						tanidx.set(scan);
						final double prev = ta.getDouble(taidx);
						this.log.debug("previous {}, new {}", prev, prev
						        - accum);
						tan.setDouble(tanidx, prev - accum);
						filtered.add(a);
						scan++;
					}
					ivf.setIndexedArray(filtered);
					total_intens.setArray(tan);
				}
				if (this.normalize_scans) {
					this.log.info("Normalizing scans to length 1");
					final IVariableFragment ivf = f
					        .getChild(this.binned_intensity_values);
					final IVariableFragment sidx = f
					        .getChild(this.binned_scan_index);
					ivf.setIndex(sidx);
					final List<Array> intens = ivf.getIndexedArray();
					final List<Array> normIntens = new ArrayList<Array>();
					for (final Array a : intens) {
						final MAVector ma = new MAVector(a);
						final double norm = ma.norm();
						this.log.debug("Norm: {}", norm);
						if (norm == 0.0) {
							normIntens.add(a);
						} else {
							normIntens.add(ArrayTools.mult(a, 1.0d / norm));
						}
					}
					ivf.setIndexedArray(normIntens);
				}
				// MaltcmsTools
				// .buildBinaryMassVectors(f, binary_mass_values,
				// mass_values, intensity_values, scan_index,
				// maskedMasses);
				this.log.debug("Created IFileFragment {}", f);
				// MaltcmsTools.loadDefaultVars(f);
				// FragmentTools.loadAdditionalVars(f);
				this.log.debug("Adding pair {},{}", ff, f);
				map.add(new Tuple2D<IFileFragment, IFileFragment>(ff, f));

				this.log.debug("{}", FileFragment.printFragment(f));
				f.save();
				al.add(f);
				final DefaultWorkflowResult dwr = new DefaultWorkflowResult(
				        new File(f.getAbsolutePath()), iwe,
				        WorkflowSlot.GENERAL_PREPROCESSING);
				getIWorkflow().append(dwr);
				// }

				// };
				// es.submit(r);

			}
			// es.shutdown();
			// try {
			// //Timeout after 30 Minutes
			// es.awaitTermination(30, TimeUnit.MINUTES);
			// } catch (InterruptedException e) {
			// log.error(e.getLocalizedMessage());
			// }
			final IVariableFragment fileMap = FragmentTools.createFileMap(
			        dense_array_files, this.mapping_file_var, map);
			EvalTools
			        .notNull(new Object[] { fileMap, dense_array_files }, this);
			dense_array_files.save();
			Factory.getInstance().getConfiguration().setProperty(
			        "input_to_tmp_files_location",
			        dense_array_files.getAbsolutePath());
			final ArrayList<Tuple2D<IFileFragment, IFileFragment>> tuple = new ArrayList<Tuple2D<IFileFragment, IFileFragment>>(
			        FragmentTools.getFileMap());
			for (int i = 0; i < map.size(); i++) {
				if (!map.get(i).getFirst().getAbsolutePath().equals(
				        tuple.get(i).getFirst().getAbsolutePath())
				        && !map.get(i).getSecond().getAbsolutePath().equals(
				                tuple.get(i).getSecond().getAbsolutePath())) {
					throw new ConstraintViolationException(
					        "Contents are not equal: " + map.get(i) + "!="
					                + tuple.get(i));
				}
			}

			final DefaultWorkflowResult dwr = new DefaultWorkflowResult(
			        new File(dense_array_files.getAbsolutePath()), this,
			        WorkflowSlot.GENERAL_PREPROCESSING);
			getIWorkflow().append(dwr);
		}
		final TupleND<IFileFragment> res = new TupleND<IFileFragment>(al);
		EvalTools.notNull(res, this);
		return res;
	}

	@Override
	public void configure(final Configuration cfg) {
		this.mass_values = cfg.getString("var.mass_values", "mass_values");
		this.intensity_values = cfg.getString("var.intensity_values",
		        "intensity_values");
		this.total_intensity = cfg.getString("var.total_intensity",
		        "total_intensity");
		this.minvarname = cfg.getString("var.mass_range_min", "mass_range_min");
		this.maxvarname = cfg.getString("var.mass_range_max", "mass_range_max");
		this.scan_index = cfg.getString("var.scan_index", "scan_index");
		this.binned_intensity_values = cfg.getString(
		        "var.binned_intensity_values", "binned_intensity_values");
		this.binned_mass_values = cfg.getString("var.binned_mass_values",
		        "binned_mass_values");
		this.binned_scan_index = cfg.getString("var.binned_scan_index",
		        "binned_scan_index");
		this.mapping_file = cfg.getString("input_to_tmp_files_file_name",
		        "input_to_tmp_files.cdf");
		this.mapping_file_var = cfg.getString("var.file_map", "file_map");
		this.normalize_scans = cfg.getBoolean(this.getClass().getName()
		        + ".normalizeScans", false);
		this.nthreads = cfg.getInt("maltcms.pipelinethreads", 5);
		this.maskedMasses = MaltcmsTools.parseMaskedMassesList(cfg.getList(this
		        .getClass().getName()
		        + ".maskMasses", Collections.emptyList()));
		this.invertMaskedMasses = cfg.getBoolean(this.getClass().getName()
		        + ".invertMaskedMasses", false);
		this.massBinResolution = cfg.getDouble(
		        "dense_arrays.massBinResolution", 1.0d);
	}

	@Override
	public String getDescription() {
		return "Creates a binned representation of a chromatogram.";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
	 */
	@Override
	public WorkflowSlot getWorkflowSlot() {
		return WorkflowSlot.GENERAL_PREPROCESSING;
	}
}