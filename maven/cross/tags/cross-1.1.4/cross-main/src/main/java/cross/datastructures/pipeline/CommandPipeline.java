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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.jdom.Element;
import org.slf4j.Logger;

import cross.Factory;
import cross.Logging;
import cross.annotations.AnnotationInspector;
import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowResult;
import cross.event.IEvent;
import cross.exception.ConstraintViolationException;
import cross.exception.ResourceNotAvailableException;
import cross.datastructures.tools.EvalTools;
import cross.tools.StringTools;

/**
 * Implementation of ICommandSequence for a linear sequence of commands.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class CommandPipeline implements ICommandSequence {

	public static final String NUMBERFORMAT = "%.2f";

	protected List<IFragmentCommand> commands = null;

	protected Iterator<IFragmentCommand> iter = null;

	protected TupleND<IFileFragment> input;

	protected TupleND<IFileFragment> tmp;

	protected IFragmentCommand head = null;

	protected IWorkflow iw = null;

	private final Logger log = Logging.getLogger(this.getClass());

	private boolean checkCommandDependencies = true;

	private List<Tuple2D<String, String>> pipeline = Collections.emptyList();

	private HashMap<IFragmentCommand, String> cmdToConfig = new HashMap<IFragmentCommand, String>();

	private int cnt;

	/**
	 * Load the given commands and initialize them.
	 * 
	 * @param commands
	 */
	protected ArrayList<IFragmentCommand> addToList(
	        final TupleND<IFileFragment> inputFragments,
	        final Collection<Tuple2D<String, String>> commands)
	        throws ConstraintViolationException {
		EvalTools.notNull(commands, this);
		EvalTools.notNull(inputFragments, this);
		final ArrayList<IFragmentCommand> al = new ArrayList<IFragmentCommand>();
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
		for (final Tuple2D<String, String> s : commands) {
			this.log.debug("Adding command " + s.getFirst());
			final IFragmentCommand cmd = loadCommand(s.getFirst(), s
			        .getSecond());
			cmdToConfig.put(cmd, s.getSecond());
			// ClassSpy cs = new ClassSpy(s);
			EvalTools
			        .notNull(
			                cmd,
			                "Instantiation of AFragmentCommand failed, check to remove explicit constructors in class "
			                        + s);
			if (this.checkCommandDependencies) {
				// required variables
				final Collection<String> requiredVars = AnnotationInspector
				        .getRequiredVariables(cmd);
				// optional variables
				final Collection<String> optionalVars = AnnotationInspector
				        .getOptionalRequiredVariables(cmd);
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
			final Collection<String> createdVars = AnnotationInspector
			        .getProvidedVariables(cmd);
			for (final String var : createdVars) {
				if (!var.isEmpty() && !providedVariables.contains(var)) {
					this.log.debug("Adding new variable {}, provided by {}",
					        var, cmd.getClass().getName());
					providedVariables.add(var);
				} else {
					this.log
					        .warn(
					                "Potential name clash, variable {} already provided!",
					                var);
				}
			}
			al.add(cmd);
			EvalTools.notNull(cmd, this);
		}
		return al;
	}

	protected Collection<String> checkOptionalVariables(
	        final IFragmentCommand cmd, final Collection<String> optionalVars,
	        final HashSet<String> providedVariables) {
		if (optionalVars.size() == 0) {
			this.log.debug("No optional variables declared!");
			return optionalVars;
		}
		boolean checkOpt = true;
		for (final String var : optionalVars) {
			if (!var.isEmpty() && !providedVariables.contains(var)) {
				this.log
				        .warn(
				                "Variable {} requested as optional by {} not declared as created by previous commands!",
				                var, cmd.getClass().getName());
				checkOpt = false;
			}

		}
		if (checkOpt && (optionalVars.size() > 0)) {
			this.log
			        .debug(
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
			this.log.debug("No required variables declared!");
			return requiredVars;
		}
		boolean check = true;
		final Collection<String> failedVars = new ArrayList<String>();
		for (final String var : requiredVars) {
			this.log.debug("Checking variable {}", var);
			if (!var.isEmpty() && !providedVariables.contains(var)) {
				this.log
				        .warn(
				                "Variable {} requested by {} not declared as created by previous commands!",
				                var, cmd.getClass().getName());
				check = false;
				failedVars.add(var);
			}
		}
		if (check) {
			if (requiredVars.size() > 0) {
				this.log.debug(
				        "Command {} has access to all required variables!", cmd
				                .getClass().getName());
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
		this.checkCommandDependencies = cfg.getBoolean(this.getClass()
		        .getName()
		        + ".checkCommandDependencies", true);
		final List<?> pipeline = Factory.getInstance().getConfiguration()
		        .getList("pipeline");
		final List<?> pipelineProperties = Factory.getInstance()
		        .getConfiguration().getList("pipeline.properties");
		if ((pipeline != null) && !pipeline.isEmpty()) {// generic for elements
			// in pipeline
			final List<String> ls = StringTools.toStringList(pipeline);
			final List<String> lsp = StringTools
			        .toStringList(pipelineProperties);
			final ArrayList<Tuple2D<String, String>> al = new ArrayList<Tuple2D<String, String>>();
			for (int i = 0; i < ls.size(); i++) {
				al.add(new Tuple2D<String, String>(ls.get(i),
				        lsp.isEmpty() ? null : lsp.get(i)));
			}
			this.pipeline = al;
		} else {
			throw new IllegalArgumentException(
			        "Could not create command sequence!");
		}
	}

	@Override
	public Collection<IFragmentCommand> getCommands() {
		return this.commands;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.ucar.ma2.CommandSequence#getInput()
	 */
	public TupleND<IFileFragment> getInput() {
		return this.input;
	}

	@Override
	public IWorkflow getIWorkflow() {
		return this.iw;
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
				final String vname = Factory.getInstance().getConfiguration()
				        .getString(s);
				if ((vname != null) && !vname.isEmpty()) {
					try {
						final IVariableFragment ivf = ff.getChild(vname, true);
						this.log.debug("Retrieved var {}", ivf.getVarname());
						if (!providedVariables.contains(s)) {
							providedVariables.add(s);
						}
					} catch (final ResourceNotAvailableException rnae) {
						this.log.debug(
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
	public boolean hasNext() {
		return this.iter.hasNext();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.datastructures.pipeline.ICommandSequence#init()
	 */
	@Override
	public void init() {
		final List<IFragmentCommand> cmds = addToList(this.tmp, this.pipeline);
		Factory.getInstance().log.debug(this.pipeline.toString());
		EvalTools.inRangeI(1, Integer.MAX_VALUE, cmds.size(), this);
		setCommands(cmds);
	}

	@Override
	public void listen(final IEvent<IWorkflowResult> v) {
		this.iw.append(v.get());
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
		final IFragmentCommand clazz = Factory.getInstance().getObjectFactory()
		        .instantiate(clsname, IFragmentCommand.class,
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
	public TupleND<IFileFragment> next() {
		if (this.iter.hasNext()) {
			final IFragmentCommand cmd = this.iter.next();
			cmd.setIWorkflow(getIWorkflow());
			cmd.getIWorkflow().getOutputDirectory(cmd);
			if (cmdToConfig.containsKey(cmd)) {
				String s = cmdToConfig.get(cmd);
				if (s != null) {
					try {
						PropertiesConfiguration pc = new PropertiesConfiguration(
						        s);
						pc.save(new File(cmd.getIWorkflow().getOutputDirectory(
						        cmd), cmd.getClass().getSimpleName()
						        + ".properties"));
					} catch (ConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			// save current state of workflow
			final IWorkflow iw = getIWorkflow();
			iw.save();
			// log.info("Next ICommand: {}",cmd.getClass().getName());
			this.log
			        .info("#############################################################################");
			this.log.info("# Running {}/{}: {}", new Object[] { (this.cnt + 1),
			        this.commands.size(), cmd.getClass().getSimpleName() });
			this.log.debug("# Package: {}", cmd.getClass().getPackage()
			        .getName());
			this.log
			        .info("#############################################################################");
			// set output dir to currently active command
			getIWorkflow().getOutputDirectory(cmd);
			long start = System.nanoTime();
			this.tmp = cmd.apply(this.tmp);
			start = Math.abs(System.nanoTime() - start);
			final float seconds = ((float) start) / ((float) 1000000000);
			final StringBuilder sb = new StringBuilder();
			final Formatter formatter = new Formatter(sb);
			formatter.format(CommandPipeline.NUMBERFORMAT, (seconds));
			this.log.info("Runtime of command {}: {} sec", cmd.getClass()
			        .getName(), sb.toString());
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
	public void remove() {

	}

	@Override
	public void setCommands(final Collection<IFragmentCommand> c) {
		EvalTools.inRangeI(1, Integer.MAX_VALUE, c.size(), this);
		this.commands = new ArrayList<IFragmentCommand>(c);
		this.iter = this.commands.iterator();
		this.cnt = 0;
	}

	@Override
	public void setInput(final TupleND<IFileFragment> t) {
		this.input = t;
		this.tmp = t;
		init();
	}

	@Override
	public void setIWorkflow(final IWorkflow iw1) {
		this.iw = iw1;
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
		this.log.debug("Appending xml for CommandPipeline");
		final Element ifrge = new Element("workflowInputs");
		for (final IFileFragment ifrg : getInput()) {
			final Element ifrge0 = new Element("workflowInput");
			ifrge0.setAttribute("uri", new File(ifrg.getAbsolutePath()).toURI()
			        .toASCIIString());
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
