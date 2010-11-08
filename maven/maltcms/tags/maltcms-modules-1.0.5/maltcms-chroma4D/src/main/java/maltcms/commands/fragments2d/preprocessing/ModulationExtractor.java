/*
 * Copyright (C) 2008-2010 Nils Hoffmann Nils.Hoffmann A T
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
 * $Id: ModulationExtractor.java 140 2010-07-12 11:13:35Z nilshoffmann $
 */
package maltcms.commands.fragments2d.preprocessing;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import cross.Logging;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;

/**
 * 
 * ModulationExtractor allows to subset the modulations contained in a GCxGC-MS
 * file, by setting the global scan_index variable and the
 * second_column_scan_index variables to their appropriate values. Therefor,
 * downstream commands should use those variables in order to read individual
 * modulations or use the Chromatogram2D class or one of the IScanLine
 * implementations.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 */
@RequiresVariables(names = { "var.total_intensity", "var.modulation_time",
        "var.scan_rate", "var.scan_duration", "var.second_column_time",
        "var.second_column_scan_index", "var.total_intensity_1d",
        "var.scan_acquisition_time", "var.scan_acquisition_time_1d",
        "var.scan_index" })
@ProvidesVariables(names = { "var.scan_index", "var.second_column_scan_index" })
public class ModulationExtractor extends AFragmentCommand {

	@Configurable(name = "var.total_intensity", value = "total_intensity")
	private String totalIntensityVar = "total_intensity";
	@Configurable(name = "var.modulation_time", value = "modulation_time")
	private String modulationTimeVar = "modulation_time";
	@Configurable(name = "var.scan_rate", value = "scan_rate")
	private String scanRateVar = "scan_rate";
	@Configurable(name = "var.scan_duration", value = "scan_duration")
	private String scanDurationVar = "scan_duration";
	@Configurable(name = "var.second_column_time", value = "second_column_time")
	private String secondColumnTimeVar = "second_column_time";
	@Configurable(name = "var.second_column_scan_index", value = "second_column_scan_index")
	private String secondColumnScanIndexVar = "second_column_scan_index";
	@Configurable(name = "var.total_intensity_1d", value = "total_intensity_1d")
	private String totalIntensity1dVar = "total_intensity_1d";
	@Configurable(name = "var.scan_aquisition", value = "scan_aquisition")
	private String scanAcquisitionTimeVar = "scan_aquisition";
	@Configurable(name = "var.scan_acquisition_1d", value = "scan_acquisition_1d")
	private String scanAcquisitionTime1dVar = "scan_acquisition_1d";
	@Configurable(name = "var.scan_index")
	private String scanIndexVar = "scan_index";

	@Configurable(value = "-1", type = Integer.class)
	private int startModulation = -1;
	@Configurable(value = "-1", type = Integer.class)
	private int endModulation = -1;

	private Logger log = Logging.getLogger(this);

	@Override
	public void configure(Configuration cfg) {
		super.configure(cfg);

		this.totalIntensityVar = cfg.getString("var.total_intensity",
		        "total_intensity");
		this.modulationTimeVar = cfg.getString("var.modulation_time",
		        "modulation_time");
		this.scanRateVar = cfg.getString("var.scan_rate", "scan_rate");
		this.scanDurationVar = cfg.getString("var.scan_duration",
		        "scan_duration");
		this.secondColumnTimeVar = cfg.getString("var.second_column_time",
		        "second_column_time");
		this.secondColumnScanIndexVar = cfg.getString(
		        "var.second_column_scan_index", "second_column_scan_index");
		this.totalIntensity1dVar = cfg.getString("var.total_intensity_1d",
		        "total_intensity_1d");
		this.scanAcquisitionTimeVar = cfg.getString(
		        "var.scan_acquisition_time", "scan_acquisition_time");
		this.scanAcquisitionTime1dVar = cfg.getString(
		        "var.scan_acquisition_time_1d", "scan_acquisition_time_1d");
		this.scanIndexVar = cfg.getString("var.scan_index", "scan_index");

		this.startModulation = cfg.getInt(getClass().getName()
		        + ".startModulation", -1);
		this.endModulation = cfg.getInt(
		        getClass().getName() + ".endModulation", -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.fragments.AFragmentCommand#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Allows definition of a start and end modulation period to be extracted from a raw GCxGC-MS chromatogram.";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.ICommand#apply(java.lang.Object)
	 */
	@Override
	public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
		final TupleND<IFileFragment> res = new TupleND<IFileFragment>();
		for (IFileFragment ff : t) {
			final IFileFragment work = createWorkFragment(ff);
			final double srv = ff.getChild(this.scanRateVar).getArray()
			        .getDouble(Index.scalarIndexImmutable);
			final double modT = ff.getChild(this.modulationTimeVar).getArray()
			        .getDouble(Index.scalarIndexImmutable);
			final int globalNumberOfModulations = ff.getChild(
			        this.secondColumnScanIndexVar).getArray().getShape()[0];
			log.info("File contains {} modulations", globalNumberOfModulations);
			final int globalStartIndex = (int) (Math.max(this.startModulation,
			        0)
			        * (int) srv * (int) modT);
			log.info("Scan rate is {}", srv);
			log.info("Modulation time is {}", modT);
			int globalEndIndex = (int) (this.endModulation);
			if (this.endModulation == -1) {
				globalEndIndex = (int) (globalNumberOfModulations * (int) srv * (int) modT) - 1;
			}

			int startMod = Math.max(this.startModulation, 0);
			int endMod = this.endModulation == -1 ? globalNumberOfModulations - 1
			        : Math.min(this.endModulation,
			                globalNumberOfModulations - 1);
			log.info("Reading from modulation: {} to {}", startMod, endMod);
			log.info("Reading from global index {} to {}.", globalStartIndex,
			        globalEndIndex);
			final int numberOfModulations = endMod - startMod;
			log.info("Reading {} modulations", numberOfModulations);

			try {
				log.debug("start mod: {}, end mod: {}", startMod, endMod);
				final Range modRange = new Range(startMod, endMod);
				final IVariableFragment origModulationIndex = ff
				        .getChild(this.secondColumnScanIndexVar);
				// origModulationIndex.setRange(new Range[] { modRange });
				final Array sia = origModulationIndex.getArray().section(
				        Arrays.asList(modRange));
				// log.info("{}", sia);
				final VariableFragment nScanIndex = new VariableFragment(work,
				        this.secondColumnScanIndexVar);
				nScanIndex.setArray(sia);
				DefaultWorkflowResult dwr = new DefaultWorkflowResult(new File(
				        work.getAbsolutePath()), this, getWorkflowSlot(), work);
				getIWorkflow().append(dwr);
			} catch (InvalidRangeException e) {
				log.warn("{}", e.getLocalizedMessage());
			}

			try {
				final Range r = new Range(globalStartIndex, globalEndIndex);
				final IVariableFragment origScanIndex = ff
				        .getChild(this.scanIndexVar);
				origScanIndex.setRange(new Range[] { r });
				final Array sia = origScanIndex.getArray();
				final VariableFragment nScanIndex = new VariableFragment(work,
				        this.scanIndexVar);
				nScanIndex.setArray(sia);
				DefaultWorkflowResult dwr = new DefaultWorkflowResult(new File(
				        work.getAbsolutePath()), this, getWorkflowSlot(), work);
				getIWorkflow().append(dwr);
			} catch (InvalidRangeException ire) {
				log.warn("{}", ire.getLocalizedMessage());
			}
			work.save();
			res.add(work);
		}
		return res;
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
