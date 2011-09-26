/*
 * 
 *
 * $Id$
 */

package cross.datastructures.pipeline;

import cross.annotations.AnnotationInspector;
import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.exception.ConstraintViolationException;
import cross.exception.ResourceNotAvailableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author nilshoffmann
 */
@Data
@Slf4j
public class PipelineValidator implements IPipelineValidator{

    private List<IFragmentCommand> fragmentCommands;
    private TupleND<IFileFragment> inputFragments;

    @Override
    public void validate() throws ConstraintViolationException {
        // TODO add support for pipeline constraint checking
        // prerequisites for a correct pipeline:
        // a: input files must provide initially created variables, which are
        // required by first command
        // or first command does not require any variables -> mimick by
        // DefaultVarLoader
        // currently, smallest initial set is total_intensity, mass_values,
        // intensity_values,scan_index
        // scan_acquisition_time
        // b: later commands in the pipeline may require variables which are
        // created further
        // upstream and not by their immediate predecessor
        // c: downstream commands can only be executed, if all required
        // variables are provided
        // upstream
        // d: optional variables may be requested, but do not lead to
        // termination, if they
        // are not provided
        final HashSet<String> providedVariables = new HashSet<String>();
        for (IFragmentCommand cmd : fragmentCommands) {
            // required variables
            final Collection<String> requiredVars = AnnotationInspector.
                    getRequiredVariables(cmd);
            // optional variables
            final Collection<String> optionalVars = AnnotationInspector.
                    getOptionalRequiredVariables(cmd);
            // get variables provided from the past
            getPersistentVariables(inputFragments, requiredVars,
                    providedVariables);
            getPersistentVariables(inputFragments, optionalVars,
                    providedVariables);
            // check dependencies
            // The following method throws a RuntimeException, when its
            // constraints are not met, e.g. requiredVariables are not
            // present, leading to a termination
            checkRequiredVariables(cmd, requiredVars, providedVariables);
            checkOptionalVariables(cmd, optionalVars, providedVariables);
        }
    }
    
    /**
     * 
     * @param inputFragments
     * @param requiredVariables
     * @param providedVariables 
     */
    protected void getPersistentVariables(
            final TupleND<IFileFragment> inputFragments,
            final Collection<String> requiredVariables,
            final HashSet<String> providedVariables) {

        for (final IFileFragment ff : inputFragments) {
            for (final String s : requiredVariables) {
                // resolve the variables name
                final String vname = cross.Factory.getInstance().
                        getConfiguration().getString(s);
                if ((vname != null) && !vname.isEmpty()) {
                    try {
                        final IVariableFragment ivf = ff.getChild(vname, true);
                        log.debug("Retrieved var {}", ivf.getVarname());
                        if (!providedVariables.contains(s)) {
                            providedVariables.add(s);
                        }
                    } catch (final ResourceNotAvailableException rnae) {
                        log.debug(
                                "Could not find variable {} as child of {}",
                                vname, ff.getAbsolutePath());
                    }
                }
            }
        }
    }

    /**
     * 
     * @param command
     * @param optionalVariables
     * @param providedVariables
     * @return 
     */
    protected Collection<String> checkOptionalVariables(final IFragmentCommand command,
            final Collection<String> optionalVariables,
            final HashSet<String> providedVariables) {
        if (optionalVariables.isEmpty()) {
            log.debug("No optional variables declared!");
            return optionalVariables;
        }
        boolean checkOpt = true;
        for (final String var : optionalVariables) {
            if (!var.isEmpty() && !providedVariables.contains(var)) {
                log.warn(
                        "Variable {} requested as optional by {} not declared as created by previous commands!",
                        var, command.getClass().getName());
                checkOpt = false;
            }
        }
        if (checkOpt && (optionalVariables.size() > 0)) {
            log.debug(
                    "Command {} has access to all optional requested variables!",
                    command.getClass().getName());
        }
        return optionalVariables;
    }

    protected Collection<String> checkRequiredVariables(final IFragmentCommand command,
            final Collection<String> requiredVariables,
            final HashSet<String> providedVariables) throws ConstraintViolationException {
        if (requiredVariables.isEmpty()) {
            log.debug("No required variables declared!");
            return requiredVariables;
        }
        boolean check = true;
        final Collection<String> failedVars = new ArrayList<String>();
        for (final String var : requiredVariables) {
            log.debug("Checking variable {}", var);
            if (!var.isEmpty() && !providedVariables.contains(var)) {
                log.warn(
                        "Variable {} requested by {} not declared as created by previous commands!",
                        var, command.getClass().getName());
                check = false;
                failedVars.add(var);
            }
        }
        if (check) {
            if (requiredVariables.size() > 0) {
                log.debug("Command {} has access to all required variables!",
                        command.getClass().getName());
            }
            return requiredVariables;
        } else {
            throw new ConstraintViolationException("Command " + command.getClass().
                    getName() + " requires non-existing variables: " + failedVars.
                    toString());
        }
    }
}
