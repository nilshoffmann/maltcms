/*
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: CommandPipeline.java 116 2010-06-17 08:46:30Z nilshoffmann $
 */
/**
 * Created by Nils.Hoffmann@cebitec.uni-bielefeld.de at 28.02.2007
 */
package cross.datastructures.pipeline;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.jdom.Element;

import cross.Factory;
import cross.IConfigurable;
import cross.annotations.AnnotationInspector;
import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowResult;
import cross.event.IEvent;
import cross.exception.ConstraintViolationException;
import cross.exception.ResourceNotAvailableException;
import cross.datastructures.tools.EvalTools;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.sf.maltcms.execution.api.ExecutionFactory;
import net.sf.maltcms.execution.api.Impaxs;

/**
 * Implementation of ICommandSequence for a linear sequence of commands.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Slf4j
@Data
public final class CommandPipeline implements ICommandSequence, IConfigurable {

    /**
     * 
     */
    private static final long serialVersionUID = 7387727704189206255L;
    //accessible fields
    public static final String NUMBERFORMAT = "%.2f";
    private List<IFragmentCommand> commands = Collections.emptyList();
    private TupleND<IFileFragment> input;
    private IWorkflow workflow;
    private boolean checkCommandDependencies = true;
    @Getter
    @Setter(value = AccessLevel.NONE)
    private Impaxs executionServer;
//    private Collection<Tuple2D<String, String>> pipeline = Collections.emptyList();
//    private HashMap<IFragmentCommand, String> cmdToConfig = new HashMap<IFragmentCommand, String>();
    //private fields
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Iterator<IFragmentCommand> iter;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private TupleND<IFileFragment> tmp;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private int cnt;

    /**
     * Load the given commands and initialize them.
     * 
     * @param commands
     */
//    protected ArrayList<IFragmentCommand> addToList(
//            final TupleND<IFileFragment> inputFragments,
//            final Collection<Tuple2D<String, String>> commands)
//            throws ConstraintViolationException {
//        EvalTools.notNull(commands, this);
//        EvalTools.notNull(inputFragments, this);
//        final ArrayList<IFragmentCommand> al = new ArrayList<IFragmentCommand>();
//        // TODO add support for pipeline constraint checking
//        // prerequisites for a correct pipeline:
//        // a: input files must provide initially created variables, which are
//        // required by first command
//        // or first command does not require any variables -> mimick by
//        // DefaultVarLoader
//        // currently, smallest initial set is total_intensity, mass_values,
//        // intensity_values,scan_index
//        // scan_acquisition_time
//        // b: later commands in the pipeline may require variables which are
//        // created further
//        // upstream and not by their immediate predecessor
//        // c: downstream commands can only be executed, if all required
//        // variables are provided
//        // upstream
//        // d: optional variables may be requested, but do not lead to
//        // termination, if they
//        // are not provided
//        final HashSet<String> providedVariables = new HashSet<String>();
//        for (final Tuple2D<String, String> s : commands) {
//            log.debug("Adding command " + s.getFirst());
//            final IFragmentCommand cmd = loadCommand(s.getFirst(), s.getSecond());
////            cmdToConfig.put(cmd, s.getSecond());
//            // ClassSpy cs = new ClassSpy(s);
//            EvalTools.notNull(
//                    cmd,
//                    "Instantiation of AFragmentCommand failed, check to remove explicit constructors in class "
//                    + s);
//            if (this.checkCommandDependencies) {
//                // required variables
//                final Collection<String> requiredVars = AnnotationInspector.getRequiredVariables(cmd);
//                // optional variables
//                final Collection<String> optionalVars = AnnotationInspector.getOptionalRequiredVariables(cmd);
//                // get variables provided from the past
//                getPersistentVariables(inputFragments, requiredVars,
//                        providedVariables);
//                getPersistentVariables(inputFragments, optionalVars,
//                        providedVariables);
//                // check dependencies
//                // The following method throws a RuntimeException, when its
//                // constraints are not met, e.g. requiredVariables are not
//                // present, leading to a termination
//                checkRequiredVariables(cmd, requiredVars, providedVariables);
//                checkOptionalVariables(cmd, optionalVars, providedVariables);
//            }
//
//            // provided variables
//            final Collection<String> createdVars = AnnotationInspector.getProvidedVariables(cmd);
//            for (final String var : createdVars) {
//                if (!var.isEmpty() && !providedVariables.contains(var)) {
//                    log.debug("Adding new variable {}, provided by {}",
//                            var, cmd.getClass().getName());
//                    providedVariables.add(var);
//                } else {
//                    log.warn(
//                            "Potential name clash, variable {} already provided!",
//                            var);
//                }
//            }
//            al.add(cmd);
//            EvalTools.notNull(cmd, this);
//        }
//        return al;
//    }
    protected Collection<String> checkOptionalVariables(
            final IFragmentCommand cmd, final Collection<String> optionalVars,
            final HashSet<String> providedVariables) {
        if (optionalVars.size() == 0) {
            log.debug("No optional variables declared!");
            return optionalVars;
        }
        boolean checkOpt = true;
        for (final String var : optionalVars) {
            if (!var.isEmpty() && !providedVariables.contains(var)) {
                log.warn(
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

    protected Collection<String> checkRequiredVariables(
            final IFragmentCommand cmd, final Collection<String> requiredVars,
            final HashSet<String> providedVariables)
            throws ConstraintViolationException {
        if (requiredVars.size() == 0) {
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
                        "Command {} has access to all required variables!", cmd.
                        getClass().getName());
            }
            return requiredVars;
        } else {
            throw new ConstraintViolationException("Command "
                    + cmd.getClass().getName()
                    + " requires non-existing variables: "
                    + failedVars.toString());
        }
    }

    @Override
    public void configure(final Configuration cfg) {
        log.warn(
                "CommandPipeline does not support configuration via configure anylonger. Please use a Spring xml file!");
//        this.checkCommandDependencies = cfg.getBoolean(this.getClass().getName()
//                + ".checkCommandDependencies", true);
//        final List<?> pipeline = Factory.getInstance().getConfiguration().getList("pipeline");
//        final List<?> pipelineProperties = Factory.getInstance().getConfiguration().getList("pipeline.properties");
//        if ((pipeline != null) && !pipeline.isEmpty()) {// generic for elements
//            // in pipeline
//            final List<String> ls = StringTools.toStringList(pipeline);
//            final List<String> lsp = StringTools.toStringList(pipelineProperties);
//            final ArrayList<Tuple2D<String, String>> al = new ArrayList<Tuple2D<String, String>>();
//            for (int i = 0; i < ls.size(); i++) {
//                al.add(new Tuple2D<String, String>(ls.get(i),
//                        lsp.isEmpty() ? null : lsp.get(i)));
//            }
//            this.pipeline = al;
//        } else {
//            throw new IllegalArgumentException(
//                    "Could not create command sequence!");
//        }
    }

    /**
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


    /*
     * (non-Javadoc)
     * 
     * @see maltcms.ucar.ma2.CommandSequence#hasNext()
     */
    @Override
    public boolean hasNext() {
        return this.iter.hasNext();
    }

    @Override
    public boolean validate() {
        if (this.checkCommandDependencies) {
            try {
                checkCommandDependencies(input, commands);
                return true;
            } catch (ConstraintViolationException cve) {
                log.warn("Pipeline validation failed: " + cve.
                        getLocalizedMessage());
                return false;
            }
        }
        return true;
    }

    protected void checkCommandDependencies(
            TupleND<IFileFragment> inputFragments,
            List<IFragmentCommand> commands) {
        final HashSet<String> providedVariables = new HashSet<String>();
        for (IFragmentCommand cmd : commands) {
            if (this.checkCommandDependencies) {
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

            // provided variables
            final Collection<String> createdVars = AnnotationInspector.
                    getProvidedVariables(cmd);
            for (final String var : createdVars) {
                if (!var.isEmpty() && !providedVariables.contains(var)) {
                    log.debug("Adding new variable {}, provided by {}",
                            var, cmd.getClass().getName());
                    providedVariables.add(var);
                } else {
                    log.warn(
                            "Variable {} is shadowed!",
                            var);
                }
            }
        }
    }

    @Override
    public void listen(final IEvent<IWorkflowResult> v) {
        this.workflow.append(v.get());
    }

    /**
     * Load and configure a given command.
     * 
     * @param clsname
     * @return
     */
    protected IFragmentCommand loadCommand(final String clsname,
            final String propertiesFileName) {
        EvalTools.notNull(clsname, this);
        final IFragmentCommand clazz = Factory.getInstance().getObjectFactory().
                instantiate(clsname, IFragmentCommand.class,
                propertiesFileName);
        clazz.addListener(this);
        EvalTools.notNull(clazz, "Could not load class " + clsname
                + ". Check package and classname for possible typos!", this);
        return clazz;
    }

    /*
     * (non-Javadoc)
     * 
     * @see maltcms.ucar.ma2.CommandSequence#next()
     */
    @Override
    public TupleND<IFileFragment> next() {
        try {
            if (this.executionServer == null && !getWorkflow().isExecuteLocal()) {
                log.info("Launching execution infrastructure!");
                executionServer = ExecutionFactory.getDefaultComputeServer();
                executionServer.startMasterServer();
            }
            if (this.iter.hasNext()) {
                final IFragmentCommand cmd = this.iter.next();
                cmd.setWorkflow(getWorkflow());
                cmd.getWorkflow().getOutputDirectory(cmd);
                // save current state of workflow
                final IWorkflow iw = getWorkflow();
                iw.save();
                // log.info("Next ICommand: {}",cmd.getClass().getName());
                log.info(
                        "#############################################################################");
                log.info("# Running {}/{}: {}",
                        new Object[]{(this.cnt + 1),
                            this.commands.size(), cmd.getClass().getSimpleName()});
                log.debug("# Package: {}", cmd.getClass().getPackage().getName());
                log.info(
                        "#############################################################################");
                // set output dir to currently active command
                getWorkflow().getOutputDirectory(cmd);
                long start = System.nanoTime();
                this.tmp = cmd.apply(this.tmp);
                start = Math.abs(System.nanoTime() - start);
                final float seconds = ((float) start) / ((float) 1000000000);
                final StringBuilder sb = new StringBuilder();
                final Formatter formatter = new Formatter(sb);
                formatter.format(CommandPipeline.NUMBERFORMAT, (seconds));
                log.info("Runtime of command {}: {} sec",
                        cmd.getClass().getName(),
                        sb.toString());
                this.cnt++;
                //shutdown master server if execution has finished
                if (this.cnt == this.commands.size()) {
                    shutdownMasterServer();
                }
                System.gc();

            }
            //shutdown master server in case of any uncaught exceptions
        } catch (Exception e) {
        	log.error("Caught exception while executing pipeline: ",e);
            shutdownMasterServer();
            throw new RuntimeException(e);
        }
        return this.tmp;
    }

    protected void shutdownMasterServer() {
        if (executionServer != null) {
            try {
                executionServer.stopMasterServer();
            } catch (Exception e) {
                log.warn(
                        "Exception occured while shutting down MasterServer!",
                        e);
            } finally {
                try {
                    executionServer.stopMasterServer();
                } catch (Exception e) {
                    log.warn(
                            "Exception occured while shutting down MasterServer!",
                            e);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see maltcms.ucar.ma2.CommandSequence#remove()
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCommands(List<IFragmentCommand> c) {
        EvalTools.inRangeI(1, Integer.MAX_VALUE, c.size(), this);
        this.commands = new ArrayList<IFragmentCommand>(c);
        this.iter = this.commands.iterator();
        this.cnt = 0;
    }

    @Override
    public void setInput(TupleND<IFileFragment> t) {
        EvalTools.geq(1, t.getSize(), this);
        this.input = t;
        this.tmp = t;
    }

    @Override
    public void setWorkflow(IWorkflow iw1) {
        this.workflow = iw1;
    }

    /**
     * Set a pipeline directly. Every tuple in the collection consists of the
     * String of the AFragmentCommand to run and the properties file location
     * used to configure that AFragmentCommand.
     * 
     * @param s
     */
//    public void setPipeline(final Collection<Tuple2D<String, String>> s) {
//        this.pipeline = new ArrayList<Tuple2D<String, String>>(s);
//    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.io.xml.IXMLSerializable#appendXML(org.jdom.Element)
     */
    @Override
    public void appendXML(Element e) {
        log.debug("Appending xml for CommandPipeline");
        final Element ifrge = new Element("workflowInputs");
        for (final IFileFragment ifrg : getInput()) {
            final Element ifrge0 = new Element("workflowInput");
            ifrge0.setAttribute("uri", new File(ifrg.getAbsolutePath()).toURI().
                    toASCIIString());
            ifrge.addContent(ifrge0);
        }
        e.addContent(ifrge);
        
        final Element ofrge = new Element("workflowOutputs");
        for (final IFileFragment ofrg : tmp) {
            final Element ofrge0 = new Element("workflowOutput");
            ofrge0.setAttribute("uri", new File(ofrg.getAbsolutePath()).toURI().
                    toASCIIString());
            ofrge.addContent(ofrge0);
        }
        e.addContent(ofrge);
        
        final Element cmds = new Element("workflowCommands");
        for (final IFragmentCommand wr : getCommands()) {
            final Element iwr = new Element("workflowCommand");
            iwr.setAttribute("class", wr.getClass().getCanonicalName());
            cmds.addContent(iwr);
        }
        e.addContent(cmds);
    }
}
