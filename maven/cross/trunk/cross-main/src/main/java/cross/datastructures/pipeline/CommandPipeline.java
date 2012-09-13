/* 
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.datastructures.pipeline;

import cross.Factory;
import cross.IConfigurable;
import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.ImmutableFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowStatisticsResult;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.event.IEvent;
import cross.exception.ConstraintViolationException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.sf.mpaxs.api.ExecutionFactory;
import net.sf.mpaxs.api.Impaxs;
import org.apache.commons.configuration.Configuration;
import org.jdom.Element;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of ICommandSequence for a linear sequence of commands.
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
@Data
@ServiceProvider(service = ICommandSequence.class)
public final class CommandPipeline implements ICommandSequence, IConfigurable {

    /**
     *
     */
    private static final long serialVersionUID = 7387727704189206255L;
    /**
     * accessible fields with generated getters and setters
     */
    public static final String NUMBERFORMAT = "%.2f";
    private List<IFragmentCommand> commands = Collections.emptyList();
    private TupleND<IFileFragment> input;
    private IWorkflow workflow;
    private boolean checkCommandDependencies = true;
    private ICommandSequenceValidator validator = new DefaultCommandSequenceValidator();
    /**
     * Private fields
     */
    //execution server instance
    @Getter
    @Setter(value = AccessLevel.NONE)
    private Impaxs executionServer;
    //iterator for fragment commands
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Iterator<IFragmentCommand> iter;
    //intermediate results
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private TupleND<IFileFragment> tmp;
    //counter of processed fragment commands
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private int cnt;

    @Override
    public void configure(final Configuration cfg) {
        log.debug(
                "CommandPipeline does not support configuration via configure anylonger. Please use a Spring xml file!");
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
            boolean valid = false;
            try {
                valid = validator.isValid(this);
                //checkCommandDependencies(input, commands);
                return valid;
            } catch (ConstraintViolationException cve) {
                log.warn("Pipeline validation failed: " + cve.
                        getLocalizedMessage());
                return valid;
            }
        }
        return true;
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
    @Deprecated
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
                //clear arrays to allow for gc
                for(IFileFragment f:this.tmp) {
                    f.clearArrays();
                }
                start = Math.abs(System.nanoTime() - start);
                final float seconds = ((float) start) / ((float) 1000000000);
                final StringBuilder sb = new StringBuilder();
                final Formatter formatter = new Formatter(sb);
                formatter.format(CommandPipeline.NUMBERFORMAT, (seconds));
                log.info("Runtime of command {}: {} sec",
                        cmd.getClass().getSimpleName(),
                        sb.toString());
                Map<String, Object> statsMap = new HashMap<String, Object>();
                statsMap.put("RUNTIME_MILLISECONDS", Double.valueOf(start / 1000000.f));
                DefaultWorkflowStatisticsResult dwsr = new DefaultWorkflowStatisticsResult();
                dwsr.setWorkflowElement(cmd);
                dwsr.setWorkflowSlot(WorkflowSlot.STATISTICS);
                dwsr.setStats(statsMap);
                workflow.append(dwsr);
                this.cnt++;
                //shutdown master server if execution has finished
                if (this.cnt == this.commands.size()) {
                    shutdownMasterServer();
                }
                System.gc();

            }
            //shutdown master server in case of any uncaught exceptions
        } catch (Exception e) {
            log.error("Caught exception while executing pipeline: ", e);
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

    /*
     * (non-Javadoc)
     * 
     * @see cross.io.xml.IXMLSerializable#appendXML(org.jdom.Element)
     */
    /**
     * Appends workflowInputs, workflowOutputs and workflowCommands elements to
     * given Element parameter.
     *
     * @param e
     */
    @Override
    public void appendXML(Element e) {
        log.debug("Appending xml for CommandPipeline");
        final Element ifrge = new Element("workflowInputs");
        for (final IFileFragment ifrg : getInput()) {
            final Element ifrge0 = new Element("workflowInput");
            try {
                ifrge0.setAttribute("uri", new File(ifrg.getAbsolutePath()).getCanonicalFile().toURI().normalize().
                        toASCIIString());
                ifrge.addContent(ifrge0);
            } catch (IOException ex) {
                log.warn("{}", ex);
            }

        }
        e.addContent(ifrge);

        final Element ofrge = new Element("workflowOutputs");
        for (final IFileFragment ofrg : tmp) {
            final Element ofrge0 = new Element("workflowOutput");
            try {
                ofrge0.setAttribute("uri", new File(ofrg.getAbsolutePath()).getCanonicalFile().toURI().normalize().
                        toASCIIString());
                ofrge.addContent(ofrge0);
            } catch (IOException ex) {
                log.warn("{}", ex);
            }

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
