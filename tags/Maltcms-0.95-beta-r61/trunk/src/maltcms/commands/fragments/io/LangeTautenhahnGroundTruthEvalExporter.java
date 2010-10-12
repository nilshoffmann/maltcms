/**
 * 
 */
package maltcms.commands.fragments.io;

import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.NotImplementedException;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class LangeTautenhahnGroundTruthEvalExporter extends AFragmentCommand {

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.fragments.AFragmentCommand#getDescription()
	 */
	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.ICommand#apply(java.lang.Object)
	 */
	@Override
	public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
		// One consensus feature per file
		// File Score Intensity RT MZ
		throw new NotImplementedException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
	 */
	@Override
	public WorkflowSlot getWorkflowSlot() {
		// TODO Auto-generated method stub
		return null;
	}

}
