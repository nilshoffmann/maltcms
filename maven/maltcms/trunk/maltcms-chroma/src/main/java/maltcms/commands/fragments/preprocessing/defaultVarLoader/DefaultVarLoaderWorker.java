package maltcms.commands.fragments.preprocessing.defaultVarLoader;

import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.ImmutableFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FragmentTools;
import java.io.File;
import java.io.Serializable;
import java.util.concurrent.Callable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author nils
 */
@Slf4j
@Data
public class DefaultVarLoaderWorker implements Callable<File>, Serializable {
    
    private File fileToLoad;
    private File fileToSave;

    @Override
    public File call() throws Exception {
        EvalTools.notNull(fileToLoad, this);
        EvalTools.notNull(fileToSave, this);
        //create a new working fragment
        IFileFragment output = new FileFragment(fileToSave);
        //add source file for data retrieval
        output.addSourceFile(new ImmutableFileFragment(fileToLoad));
        FragmentTools.loadDefaultVars(output);
        FragmentTools.loadAdditionalVars(output);
        //save working fragment
        output.save();
        return new File(output.getAbsolutePath());
    }
}
