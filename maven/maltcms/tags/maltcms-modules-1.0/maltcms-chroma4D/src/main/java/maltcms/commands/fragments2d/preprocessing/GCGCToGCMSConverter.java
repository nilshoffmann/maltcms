package maltcms.commands.fragments2d.preprocessing;

import maltcms.datastructures.ms.Chromatogram2D;
import maltcms.datastructures.ms.Scan2D;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import ucar.ma2.Index;
import cross.Logging;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;

@ProvidesVariables(names = { "var.excluded_masses",
        "var.total_intensity_filtered" })
public class GCGCToGCMSConverter extends AFragmentCommand {

	private Logger log = Logging.getLogger(this);

	@Configurable(name = "var.total_intensity")
	private String totalIntensityVar = "total_intensity";
	@Configurable(name = "var.total_intensity_filtered")
	private String totalIntensityFilteredVar = "total_intensity_filtered";
	@Configurable(name = "var.scan_rate")
	private String scanRateVar = "scan_rate";
	@Configurable(name = "var.modulation_time")
	private String modulationTimeVar = "modulation_time";
	@Configurable(name = "var.second_column_scan_index")
	private String secondScanIndexVar = "second_column_scan_index";

	@Override
	public String getDescription() {
		return "GCxGC-MS data to GC-MS data by simple scan-wise summation.";
	}

	@Override
	public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
		TupleND<IFileFragment> ret = new TupleND<IFileFragment>();
		for (IFileFragment ff : t) {
			final int scanRate = ff.getChild(this.scanRateVar).getArray()
			        .getInt(Index.scalarIndexImmutable);
			final int modulationTime = ff.getChild(this.modulationTimeVar)
			        .getArray().getInt(Index.scalarIndexImmutable);
			final int scansPerModulation = scanRate * modulationTime;
			this.log.debug("SPM: {}", scansPerModulation);

			IFileFragment retF = new FileFragment(getIWorkflow()
			        .getOutputDirectory(this), ff.getName());
			retF.addSourceFile(ff);

			Chromatogram2D c2 = new Chromatogram2D(ff);
			for (Scan2D s : c2) {

			}

			retF.save();
			ret.add(retF);
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
	public void configure(Configuration cfg) {
		super.configure(cfg);
		this.totalIntensityVar = cfg.getString(this.getClass().getName()
		        + ".totalIntensityVar", "total_intensity");
		this.scanRateVar = cfg.getString("var.scan_rate", "scan_rate");
		this.modulationTimeVar = cfg.getString("var.modulation_time",
		        "modulation_time");
		this.secondScanIndexVar = cfg.getString("var.second_column_scan_index",
		        "second_column_scan_index");
	}

	@Override
	public WorkflowSlot getWorkflowSlot() {
		return WorkflowSlot.GENERAL_PREPROCESSING;
	}

}
