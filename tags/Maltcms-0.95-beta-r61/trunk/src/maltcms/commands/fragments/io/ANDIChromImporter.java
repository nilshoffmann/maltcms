/**
 * 
 */
package maltcms.commands.fragments.io;

import java.util.ArrayList;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;
import ucar.ma2.MAMath;
import annotations.Configurable;
import annotations.ProvidesVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragmentFactory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.FileTools;

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
@ProvidesVariables(names = { "var.scan_acquisition_time" })
public class ANDIChromImporter extends AFragmentCommand {

	@Configurable(name = "ordinate_values")
	private String ticVarName = "ordinate_values";
	@Configurable(name = "scan_acquisition_time")
	private String satVarName = "scan_acquisition_time";
	@Configurable(name = "actual_sampling_interval")
	private String asiVarName = "actual_sampling_interval";

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.fragments.AFragmentCommand#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Generates variable scan_acquisition_time from data";
	}

	@Override
	public void configure(Configuration cfg) {
		super.configure(cfg);
		this.ticVarName = cfg.getString("var.total_intensity",
		        "ordinate_values");
		this.satVarName = cfg.getString("var.scan_acquisition_time",
		        "scan_acquisition_time");
		this.asiVarName = cfg.getString("var.actual_sampling_interval",
		        "actual_sampling_interval");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.ICommand#apply(java.lang.Object)
	 */
	@Override
	public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
		ArrayList<IFileFragment> ret = new ArrayList<IFileFragment>();
		for (IFileFragment iff : t) {
			IFileFragment fret = FileFragmentFactory.getInstance().create(
			        FileTools.prependDefaultDirs(iff.getName(),
			                this.getClass(), getIWorkflow().getStartupDate()));
			Array a = iff.getChild(this.ticVarName).getArray();
			Array sa = iff.getChild(this.asiVarName).getArray();
			ArrayDouble.D1 sat = new ArrayDouble.D1(a.getShape()[0]);
			Index sai = sa.getIndex();
			double asi = sa.getDouble(sai.set(0));
			for (int i = 0; i < sat.getShape()[0]; i++) {
				sat.set(i, ((double) i) * asi);
			}
			fret.addSourceFile(iff);
			VariableFragment vf = new VariableFragment(fret,
			        this.satVarName);
			vf.setArray(sat);
			fret.save();
			ret.add(fret);
		}
		return new TupleND<IFileFragment>(ret);
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
