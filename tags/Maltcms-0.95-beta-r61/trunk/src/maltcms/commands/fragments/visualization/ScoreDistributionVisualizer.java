/**
 * 
 */
package maltcms.commands.fragments.visualization;

import hep.aida.ref.Histogram1D;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;

import ucar.ma2.Array;
import annotations.RequiresVariables;
import cross.Logging;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import cross.tools.FileTools;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
@RequiresVariables(names = { "pairwise_distance" })
public class ScoreDistributionVisualizer extends AFragmentCommand {

	private Logger log = Logging.getLogger(this);

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.fragments.AFragmentCommand#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Generates a histogram plot of score distributions from variable pairwise_distance";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.ICommand#apply(java.lang.Object)
	 */
	@Override
	public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
		for (IFileFragment iff : t) {
			try {
				IVariableFragment pwd = iff.getChild("pairwise_distance");
				Array arr = pwd.getArray();
				double[] dbl = (double[]) arr.copyTo1DJavaArray();
				Histogram1D h = new Histogram1D(iff.getName(), dbl);
				hep.aida.ref.Converter c = new hep.aida.ref.Converter();
				String s = c.toString(h);
				File f = FileTools.prependDefaultDirs("histogram_"
				        + iff.getName(), this.getClass(), getIWorkflow()
				        .getStartupDate());
				try {
					BufferedWriter sw = new BufferedWriter(new FileWriter(f));
					sw.write(s);
					sw.flush();
					sw.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				DefaultWorkflowResult dwr = new DefaultWorkflowResult(f, this,
				        WorkflowSlot.STATISTICS);
				getIWorkflow().append(dwr);
			} catch (ResourceNotAvailableException rnae) {
				log.warn("Could not load variable {} from file {}",
				        "pairwise_distance", iff);
			}
		}
		return t;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
	 */
	@Override
	public WorkflowSlot getWorkflowSlot() {
		return WorkflowSlot.VISUALIZATION;
	}

}
