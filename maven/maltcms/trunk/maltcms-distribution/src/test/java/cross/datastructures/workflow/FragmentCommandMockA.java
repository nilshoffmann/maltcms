package cross.datastructures.workflow;

import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayShort;
import cross.annotations.ProvidesVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.TupleND;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ProvidesVariables(names = {"variable1", "variable2"})
public class FragmentCommandMockA extends AFragmentCommand {

    /**
     * 
     */
    private static final long serialVersionUID = 7454449407054696377L;

    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> in) {
        log.info("Running {}",getClass().getName());
        TupleND<IFileFragment> out = createWorkFragments(in);
        for (IFileFragment ff : out) {
            VariableFragment vf1 = new VariableFragment(ff, "variable1");
            vf1.setArray(new ArrayInt.D1(10));
            VariableFragment vf2 = new VariableFragment(ff, "variable2");
            vf2.setArray(new ArrayShort.D2(100, 10));
            ff.save();
        }
        return out;
    }

    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.FILEIO;
    }

    @Override
    public String getDescription() {
        return "This is a mock a command";
    }
}
