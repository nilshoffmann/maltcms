/**
 * 
 */
package maltcms.commands.fragments.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import maltcms.io.csv.CSVWriter;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import cross.Logging;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
@RequiresVariables(names = { "var.binned_mass_values",
        "var.binned_intensity_values", "var.binned_scan_index",
        "var.scan_acquisition_time" })
public class ObiWarplmataExporter extends AFragmentCommand {

	private String scanAcquisitionTimeVariableName;
	private String binnedIntensitiesVariableName;
	private String binnedScanIndexVariableName;
	private String binnedMassesVariableName;

	private final Logger log = Logging.getLogger(this);

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.ICommand#apply(java.lang.Object)
	 */
	@Override
	public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
		final CSVWriter csvw = new CSVWriter();
		csvw.setWorkflow(getWorkflow());
		csvw.setFieldSeparator(" ");
		for (final IFileFragment iff : t) {
			this.log.info("Exporting file {}", iff.getName());
			final Array sat = iff
			        .getChild(this.scanAcquisitionTimeVariableName).getArray();
			final IVariableFragment bidx = iff
			        .getChild(this.binnedScanIndexVariableName);
			final IVariableFragment ms = iff
			        .getChild(this.binnedMassesVariableName);
			final IVariableFragment ins = iff
			        .getChild(this.binnedIntensitiesVariableName);
			ms.setIndex(bidx);
			ins.setIndex(bidx);
			this.log.info("Creating header...");
			final int nscans = sat.getShape()[0];
			final List<String> times = new ArrayList<String>(nscans);
			final IndexIterator sati = sat.getIndexIterator();
			while (sati.hasNext()) {
				times.add("" + sati.getDoubleNext());
			}
			final int nbins = ms.getIndexedArray().get(0).getShape()[0];
			final List<String> bins = new ArrayList<String>(nbins);
			final IndexIterator msi = ms.getIndexedArray().get(0)
			        .getIndexIterator();
			while (msi.hasNext()) {
				bins.add("" + msi.getDoubleNext());
			}
			final List<List<String>> lines = new ArrayList<List<String>>();
			lines.add(Arrays.asList(nscans + ""));
			lines.add(times);
			lines.add(Arrays.asList(nbins + ""));
			lines.add(bins);
			this.log.info("Setting data...");
			for (final Array a : ins.getIndexedArray()) {
				final List<String> v = new ArrayList<String>(a.getShape()[0]);
				final IndexIterator ii = a.getIndexIterator();
				while (ii.hasNext()) {
					v.add(ii.getDoubleNext() + "");
				}
				lines.add(v);
			}
			final File path = getWorkflow().getOutputDirectory(this);
			this.log.info("Writing data...");
			csvw.writeTableByRows(path.getAbsolutePath(), StringTools
			        .removeFileExt(iff.getName())
			        + ".lmata", lines, WorkflowSlot.FILEIO);
			lines.clear();
		}
		return t;
	}

	@Override
	public void configure(final Configuration cfg) {
		super.configure(cfg);
		this.scanAcquisitionTimeVariableName = cfg.getString(
		        "var.scan_acquisition_time", "scan_acquisition_time");
		this.binnedMassesVariableName = cfg.getString("var.binned_mass_values",
		        "binned_mass_values");
		this.binnedIntensitiesVariableName = cfg.getString(
		        "var.binned_intensity_values", "binned_intensity_values");
		this.binnedScanIndexVariableName = cfg.getString(
		        "var.binned_scan_index", "binned_scan_index");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.fragments.AFragmentCommand#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Creates compatible lmata matrix files for use with Obi-Warp (http://obi-warp.sourceforge.net/)";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
	 */
	@Override
	public WorkflowSlot getWorkflowSlot() {
		return WorkflowSlot.FILECONVERSION;
	}

}
