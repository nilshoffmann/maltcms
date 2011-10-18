/**
 * 
 */
package maltcms.commands.fragments.io;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import cross.Factory;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.lookup.ServiceProvider;

/**
 * 
 * Currently only generates scan_acquisition_time for plotting and alignment
 * purposes. Mapping of ordinate_values is performed by setting
 * var.total_intensity=ordinate_values.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
@ProvidesVariables(names = {"var.scan_acquisition_time"})
@Slf4j
@Data
@ServiceProvider(service=AFragmentCommand.class)
public class ANDIChromImporter extends AFragmentCommand {

	@Configurable(name = "var.ordinate_values")
	private String ordinateValuesVariable = "ordinate_values";
	@Configurable(name = "var.scan_acquisition_time")
	private String scanAcquisitionTimeVariable = "scan_acquisition_time";
	@Configurable(name = "var.actual_sampling_interval")
	private String actualSamplingIntervalVariable = "actual_sampling_interval";
	@Configurable(name = "var.actual_delay_time")
	private String actualDelayTimeVariable = "actual_delay_time";

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.ICommand#apply(java.lang.Object)
	 */
	@Override
	public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
		final ArrayList<IFileFragment> ret = new ArrayList<IFileFragment>();
		for (final IFileFragment iff : t) {
			final IFileFragment fret = Factory.getInstance()
			        .getFileFragmentFactory().create(
			                new File(getWorkflow().getOutputDirectory(this),
			                        iff.getName()));
			final Array a = iff.getChild(this.ordinateValuesVariable).getArray();
			final Array sa = iff.getChild(this.actualSamplingIntervalVariable).getArray();
			final ArrayDouble.D1 sat = new ArrayDouble.D1(a.getShape()[0]);
			final Array adt = iff.getChild(this.actualDelayTimeVariable).getArray();
			final double rtStart = adt.getDouble(0);
			final double asi = sa.getDouble(0);
			for (int i = 0; i < sat.getShape()[0]; i++) {
				sat.set(i, rtStart + ((i) * asi));
			}
			fret.addSourceFile(iff);
			final VariableFragment vf = new VariableFragment(fret,
			        this.scanAcquisitionTimeVariable);
			vf.setArray(sat);
			fret.save();
			ret.add(fret);
		}
		return new TupleND<IFileFragment>(ret);
	}

	@Override
	public void configure(final Configuration cfg) {
		super.configure(cfg);
		this.ordinateValuesVariable = cfg.getString(getClass().getName()
		        + ".ordinate_values", "ordinate_values");
		this.scanAcquisitionTimeVariable = cfg.getString(getClass().getName()
		        + ".scan_acquisition_time", "scan_acquisition_time");
		this.actualSamplingIntervalVariable = cfg.getString(getClass().getName()
		        + ".actual_sampling_interval", "actual_sampling_interval");
		this.actualDelayTimeVariable = cfg.getString(getClass().getName()
		        + ".actual_delay_time", "actual_delay_time");
	}

    /*
     * (non-Javadoc)
     * 
     * @see cross.commands.fragments.AFragmentCommand#getDescription()
     */
    @Override
    public String getDescription() {
        return "Maps data in ANDIChrom format to Maltcms/ANDIMS compatible naming scheme.";
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
