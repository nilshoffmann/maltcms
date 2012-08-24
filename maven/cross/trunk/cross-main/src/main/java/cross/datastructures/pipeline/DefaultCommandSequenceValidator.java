/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package cross.datastructures.pipeline;

import cross.Factory;
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
 * Default implementation for command sequence validation.
 * 
 * Set @see checkInheritedVariables(false) if you do NOT want to 
 * check variables from previous, referenced pipeline result FileFragments. 
 * This will however defeat the purpose of pipeline validation.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
@Slf4j
class DefaultCommandSequenceValidator implements ICommandSequenceValidator {

    private boolean checkInheritedVariables = true;

    @Override
    public boolean isValid(ICommandSequence commandSequence) throws ConstraintViolationException {
        try {
            checkCommandDependencies(commandSequence.getInput(), commandSequence.getCommands());
            return true;
        } catch (ConstraintViolationException cve) {
            log.warn("Pipeline validation failed: " + cve.getLocalizedMessage());
            return false;
        }
    }

    public void checkCommandDependencies(
            TupleND<IFileFragment> inputFragments,
            List<IFragmentCommand> commands) {
        final HashSet<String> providedVariables = new HashSet<String>();
        for (IFragmentCommand cmd : commands) {
            if (this.checkInheritedVariables) {
                // required variables
                final Collection<String> requiredVars = AnnotationInspector.getRequiredVariables(cmd);
                // optional variables
                final Collection<String> optionalVars = AnnotationInspector.getOptionalRequiredVariables(cmd);
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

            // provided variables
            final Collection<String> createdVars = AnnotationInspector.getProvidedVariables(cmd);
            for (final String var : createdVars) {
                if (!var.isEmpty() && !providedVariables.contains(var)) {
                    log.debug("Adding new variable {}, provided by {}",
                            var, cmd.getClass().getName());
                    providedVariables.add(var);
                } else {
                    log.debug(
                            "Variable {} is shadowed!",
                            var);
                }
            }
        }
    }

    /**
     * Check required optional variables against provided variables for given
     * cmd.
     *
     * @param cmd
     * @param optionalVars
     * @param providedVariables
     * @return
     */
    protected Collection<String> checkOptionalVariables(
            final IFragmentCommand cmd, final Collection<String> optionalVars,
            final HashSet<String> providedVariables) {
        if (optionalVars.isEmpty()) {
            log.debug("No optional variables declared!");
            return optionalVars;
        }
        boolean checkOpt = true;
        for (final String var : optionalVars) {
            if (!var.isEmpty() && !providedVariables.contains(var)) {
                log.debug(
                        "Variable {} requested as optional by {} not declared as created by previous commands!",
                        var, cmd.getClass().getName());
                checkOpt = false;
            }

        }
        if (checkOpt && (optionalVars.size() > 0)) {
            log.debug(
                    "Command {} has access to all optional requested variables!",
                    cmd.getClass().getName());
        }
        return optionalVars;
    }

    /**
     * Check required variables against provided variables against given cmd.
     *
     * @param cmd
     * @param requiredVars
     * @param providedVariables
     * @return
     * @throws ConstraintViolationException
     */
    protected Collection<String> checkRequiredVariables(
            final IFragmentCommand cmd, final Collection<String> requiredVars,
            final HashSet<String> providedVariables)
            throws ConstraintViolationException {
        if (requiredVars.isEmpty()) {
            log.debug("No required variables declared!");
            return requiredVars;
        }
        boolean check = true;
        final Collection<String> failedVars = new ArrayList<String>();
        for (final String var : requiredVars) {
            log.debug("Checking variable {}", var);
            if (!var.isEmpty() && !providedVariables.contains(var)) {
                log.warn(
                        "Variable {} requested by {} not declared as created by previous commands!",
                        var, cmd.getClass().getName());
                check = false;
                failedVars.add(var);
            }
        }
        if (check) {
            if (requiredVars.size() > 0) {
                log.debug(
                        "Command {} has access to all required variables!", cmd.getClass().getName());
            }
            return requiredVars;
        } else {
            throw new ConstraintViolationException("Command "
                    + cmd.getClass().getName()
                    + " requires non-existing variables: "
                    + failedVars.toString());
        }
    }

    /**
     * Retrieve persistent variables, stored in previous pipeline result file
     * fragments.
     *
     * @param inputFragments
     * @param providedVariables
     * @return
     */
    private void getPersistentVariables(
            final TupleND<IFileFragment> inputFragments,
            final Collection<String> requiredVariables,
            final HashSet<String> providedVariables) {

        for (final IFileFragment ff : inputFragments) {
            for (final String s : requiredVariables) {
                // resolve the variables name
                final String vname = Factory.getInstance().getConfiguration().
                        getString(s);
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
}
