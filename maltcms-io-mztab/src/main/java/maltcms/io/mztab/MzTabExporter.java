package maltcms.io.mztab;

import cross.annotations.AnnotationInspector;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.pipeline.ICommandSequence;
import cross.datastructures.pipeline.ResultAwareCommandPipeline;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.PublicMemberGetters;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.mpaxs.api.ICompletionService;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
@Data
@RequiresVariables(names = {"var.peak_signal_to_noise",
    "var.peak_detection_channel",
    "var.peak_type",
    "var.peak_start_index",
    "var.peak_end_index",
    "var.peak_source_file",
    "var.peak_area",
    "var.peak_area_normalized",
    "var.peak_area_percent",
    "var.peak_area_normalization_methods"})
public class MzTabExporter extends AFragmentCommand {

    private String description = "Exports peak data to mzTab format.";
    private final WorkflowSlot workflowSlot = WorkflowSlot.FILECONVERSION;

    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> in) {
        try {
            TupleND<IFileFragment> t = createWorkFragments(in);
            ICompletionService<URI> ics = createCompletionService(URI.class);
            List<URI> inputFiles = new ArrayList<>(in.size());
            for (int i = 0; i < in.size(); i++) {
                inputFiles.add(in.get(i).getUri());
            }
            String mzTabId = UUID.randomUUID().toString();
            URI outputUri = new File(getWorkflow().getOutputDirectory(), mzTabId + ".mztab").toURI();
//            getWorkflow().ics.submit(new MzTabExporterWorker(inputFiles, outputUri, mzTabId));
            return mapToInputUri(ics.call(), in);
        } catch (Exception ex) {
            log.error("Caught exception while waiting for results.", ex);
            return new TupleND<>();
        }
    }

    List<String> getPipelineParameterString() {
        ICommandSequence ics = getWorkflow().getCommandSequence();
        List<String> parameters = new LinkedList<>();
        for (IFragmentCommand cmd : ics.getCommands()) {
            Collection<String> fieldNames = AnnotationInspector.getRequiredConfigFieldNames(cmd.getClass());
            PublicMemberGetters<IFragmentCommand> pmg = new PublicMemberGetters<>(cmd);
            StringBuilder sb = new StringBuilder();
            for (String fieldName : fieldNames) {
                Method m = pmg.getMethodForFieldName(fieldName);
                if (m != null) {
                    try {
                        Object o = m.invoke(cmd);
//                        sb.append("parameter: "+fieldName)
//                        hcb.append(o);
//                        log.debug("Accessing field {} of {}. Current hash={}", new Object[]{fieldName, cmd.getClass().getName(), hcb.toHashCode()});
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        Logger.getLogger(ResultAwareCommandPipeline.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        return parameters;
    }

}
