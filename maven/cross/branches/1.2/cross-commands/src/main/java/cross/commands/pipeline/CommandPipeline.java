/**
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
 */
/*
 * 
 *
 * $Id$
 */
package cross.commands.pipeline;

import cross.commands.pipeline.IPipelineValidator;
import cross.commands.pipeline.ICommandSequence;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.jdom.Element;

import cross.commands.fragments.IFragmentCommand;
import cross.commands.workflow.DefaultWorkflow;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.commands.workflow.IWorkflow;
import cross.commands.workflow.IWorkflowResult;
import cross.event.IEvent;
import cross.tools.EvalTools;
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
public class CommandPipeline implements ICommandSequence {

    public static final String NUMBERFORMAT = "%.2f";
    private List<IFragmentCommand> commands = Collections.emptyList();
    private Iterator<IFragmentCommand> fragmentIterator = commands.iterator();
    private TupleND<IFileFragment> input = new TupleND<IFileFragment>();
    private TupleND<IFileFragment> tmp = input;
    private IFragmentCommand head = null;
    private IWorkflow workflow = new DefaultWorkflow();
    private boolean checkCommandDependencies = true;
    private Collection<Tuple2D<String, String>> pipeline = Collections.emptyList();
    private HashMap<IFragmentCommand, String> cmdToConfig = new HashMap<IFragmentCommand, String>();
    private int cnt = 0;
    @Getter
    @Setter(value = AccessLevel.NONE)
    private Impaxs executionServer = null;
    private IPipelineValidator validator = new PipelineValidator();

//    @Override
//    public void configure(final Configuration cfg) {
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
//    }
    @Override
    public Collection<IFragmentCommand> getCommands() {
        return this.commands;
    }

    /*
     * (non-Javadoc)
     * 
     * @see maltcms.ucar.ma2.CommandSequence#getInput()
     */
    @Override
    public TupleND<IFileFragment> getInput() {
        return this.input;
    }

    @Override
    public IWorkflow getWorkflow() {
        return this.workflow;
    }

    /*
     * (non-Javadoc)
     * 
     * @see maltcms.ucar.ma2.CommandSequence#hasNext()
     */
    @Override
    public boolean hasNext() {
        return this.fragmentIterator.hasNext();
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.datastructures.pipeline.ICommandSequence#init()
     */
//    @Override
//    public void init() {
////        final List<IFragmentCommand> cmds = addToList(this.tmp, this.pipeline);
////        log.debug(this.pipeline.toString());
////        EvalTools.inRangeI(1, Integer.MAX_VALUE, cmds.size(), this);
////        setCommands(cmds);
//    }
    @Override
    public void listen(final IEvent<IWorkflowResult> v) {
        this.workflow.append(v.get());
    }

//    /**
//     * Load and configure a given command.
//     * 
//     * @param clsname
//     * @return
//     */
//    protected IFragmentCommand loadCommand(final String clsname,
//            final String propertiesFileName) {
//        EvalTools.notNull(clsname, this);
//        final IFragmentCommand clazz = Factory.getInstance().getObjectFactory().
//                instantiate(clsname, IFragmentCommand.class,
//                propertiesFileName);
//        clazz.addListener(this);
//        EvalTools.notNull(clazz, "Could not load class " + clsname
//                + ". Check package and classname for possible typos!", this);
//        return clazz;
//    }

    /*
     * (non-Javadoc)
     * 
     * @see maltcms.ucar.ma2.CommandSequence#next()
     */
    @Override
    public TupleND<IFileFragment> next() {
        if (this.fragmentIterator.hasNext()) {
            final IFragmentCommand cmd = this.fragmentIterator.next();
            cmd.setWorkflow(getWorkflow());
            cmd.getWorkflow().getOutputDirectory(cmd);
            if (cmdToConfig.containsKey(cmd)) {
                String s = cmdToConfig.get(cmd);
                if (s != null) {
                    try {
                        PropertiesConfiguration pc = new PropertiesConfiguration(
                                s);
                        pc.save(new File(cmd.getWorkflow().getOutputDirectory(
                                cmd), cmd.getClass().getSimpleName()
                                + ".properties"));
                    } catch (ConfigurationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            // save current state of workflow
            final IWorkflow iw = getWorkflow();
            iw.save();
            // log.info("Next ICommand: {}",cmd.getClass().getName());
            log.info(
                    "#############################################################################");
            log.info("# Running {}/{}: {}", new Object[]{(this.cnt + 1),
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
            log.info("Runtime of command {}: {} sec", cmd.getClass().getName(),
                    sb.toString());
            this.cnt++;
            System.gc();
        }
        return this.tmp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see maltcms.ucar.ma2.CommandSequence#remove()
     */
    @Override
    public void remove() {
    }

    @Override
    public void setCommands(final Collection<IFragmentCommand> c) {
        EvalTools.inRangeI(1, Integer.MAX_VALUE, c.size(), this);
        this.commands = new ArrayList<IFragmentCommand>(c);
        this.fragmentIterator = this.commands.iterator();
        this.cnt = 0;
    }

    @Override
    public void setInput(final TupleND<IFileFragment> t) {
        this.input = t;
        this.tmp = t;
//        init();
    }

    @Override
    public void setWorkflow(final IWorkflow iw1) {
        this.workflow = iw1;
    }

    /**
     * Set a pipeline directly. Every tuple in the collection consists of the
     * String of the AFragmentCommand to run and the properties file location
     * used to configure that AFragmentCommand.
     * 
     * @param s
     */
    public void setPipeline(final Collection<Tuple2D<String, String>> s) {
        this.pipeline = new ArrayList<Tuple2D<String, String>>(s);
    }

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
        final Element cmds = new Element("workflowCommands");
        for (final IFragmentCommand wr : getCommands()) {
            final Element iwr = new Element("workflowCommand");
            iwr.setAttribute("class", wr.getClass().getCanonicalName());
            cmds.addContent(iwr);
        }
        e.addContent(cmds);
    }
}