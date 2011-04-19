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
import cross.Logging;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
@RequiresVariables(names = { "pairwise_distance" })
public class ScoreDistributionVisualizer extends AFragmentCommand {

	private final Logger log = Logging.getLogger(this);

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.ICommand#apply(java.lang.Object)
	 */
	@Override
	public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
		for (final IFileFragment iff : t) {
			try {
				final IVariableFragment pwd = iff.getChild("pairwise_distance");
				final Array arr = pwd.getArray();
				final double[] dbl = (double[]) arr.copyTo1DJavaArray();
				final Histogram1D h = new Histogram1D(iff.getName(), dbl);
				final hep.aida.ref.Converter c = new hep.aida.ref.Converter();
				final String s = c.toString(h);
				final File f = new File(
				        getIWorkflow().getOutputDirectory(this), "histogram_"
				                + iff.getName());
				try {
					final BufferedWriter sw = new BufferedWriter(
					        new FileWriter(f));
					sw.write(s);
					sw.flush();
					sw.close();
				} catch (final FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (final IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				final DefaultWorkflowResult dwr = new DefaultWorkflowResult(f,
				        this, WorkflowSlot.STATISTICS, iff);
				getIWorkflow().append(dwr);
			} catch (final ResourceNotAvailableException rnae) {
				this.log.warn("Could not load variable {} from file {}",
				        "pairwise_distance", iff);
			}
		}
		return t;
	}

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
	 * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
	 */
	@Override
	public WorkflowSlot getWorkflowSlot() {
		return WorkflowSlot.VISUALIZATION;
	}

}
