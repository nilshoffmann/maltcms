package cross.datastructures.workflow;

import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiresVariables(names = {"variable2", "variable5"})
@RequiresOptionalVariables(names = {"variable4"})
public class FragmentCommandMockC extends AFragmentCommand {

    /**
     * 
     */
    private static final long serialVersionUID = 7454449407054696377L;

    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> in) {
        log.info("Running {}",getClass().getName());
        TupleND<IFileFragment> out = createWorkFragments(in);
        for (IFileFragment frag : out) {
            frag.save();
        }
        return out;
    }

    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.FILEIO;
    }

    @Override
    public String getDescription() {
        return "This is a mock c command";
    }
}
