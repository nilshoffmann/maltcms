/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
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
 * $Id$
 */

/**
 * Created by Nils.Hoffmann@cebitec.uni-bielefeld.de at 28.02.2007
 */
package cross.datastructures.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import annotations.AnnotationInspector;
import cross.Factory;
import cross.Logging;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowResult;
import cross.event.IEvent;
import cross.exception.ConstraintViolationException;
import cross.exception.ResourceNotAvailableException;
import cross.tools.EvalTools;
import cross.tools.StringTools;

/**
 * Implementation of ICommandSequence for a linear sequence of commands.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class CommandPipeline implements ICommandSequence {

	public static final String NUMBERFORMAT = "%.2f";

	protected List<AFragmentCommand> commands = null;

	protected Iterator<AFragmentCommand> iter = null;

	protected TupleND<IFileFragment> input;

	protected TupleND<IFileFragment> tmp;

	protected AFragmentCommand head = null;

	protected IWorkflow iw = null;

	private final Logger log = Logging.getLogger(this.getClass());

	private boolean checkCommandDependencies = true;

	private List<String> pipeline = Collections.emptyList();

	private int cnt;

	/**
	 * Load the given commands and initialize them.
	 * 
	 * @param commands
	 */
	protected ArrayList<AFragmentCommand> addToList(
	        final TupleND<IFileFragment> inputFragments,
	        final Collection<String> commands)
	        throws ConstraintViolationException {
		EvalTools.notNull(commands, this);
		EvalTools.notNull(inputFragments, this);
		final ArrayList<AFragmentCommand> al = new ArrayList<AFragmentCommand>();
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
		HashSet<String> providedVariables = new HashSet<String>();
		for (final String s : commands) {
			this.log.debug("Adding command " + s);
			final AFragmentCommand cmd = loadCommand(s);
			// ClassSpy cs = new ClassSpy(s);
			EvalTools
			        .notNull(
			                cmd,
			                "Instantiation of AFragmentCommand failed, check to remove explicit constructors in class "
			                        + s);
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
			if (this.checkCommandDependencies) {
				// The following method throws a RuntimeException, when its
				// constraints are not met, e.g. requiredVariables are not
				// present, leading to a termination
				checkRequiredVariables(cmd, requiredVars, providedVariables);
				checkOptionalVariables(cmd, optionalVars, providedVariables);
			}

			// provided variables
			final Collection<String> createdVars = AnnotationInspector
			        .getProvidedVariables(cmd);
			for (String var : createdVars) {
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

	/**
	 * @param inputFragments
	 * @param providedVariables
	 * @return
	 */
	private void getPersistentVariables(
	        final TupleND<IFileFragment> inputFragments,
	        final Collection<String> requiredVariables,
	        final HashSet<String> providedVariables) {

		for (IFileFragment ff : inputFragments) {
			for (String s : requiredVariables) {
				// resolve the variables name
				String vname = Factory.getInstance().getConfiguration()
				        .getString(s);
				if(!vname.isEmpty()) {
					try {
						IVariableFragment ivf = ff.getChild(vname, true);
						this.log.debug("Retrieved var {}", ivf.getVarname());
						if (!providedVariables.contains(s)) {
							providedVariables.add(s);
						}
					} catch (ResourceNotAvailableException rnae) {
						this.log.debug("Could not find variable {} as child of {}",
						        vname,ff.getAbsolutePath());
					}
				}
			}
		}
	}

	protected Collection<String> checkOptionalVariables(
	        final AFragmentCommand cmd, final Collection<String> optionalVars,
	        final HashSet<String> providedVariables) {
		if (optionalVars.size() == 0) {
			this.log.debug("No optional variables declared!");
			return optionalVars;
		}
		boolean checkOpt = true;
		for (String var : optionalVars) {
			if (!var.isEmpty() && !providedVariables.contains(var)) {
				this.log
				        .warn(
				                "Variable {} requested as optional by {} not declared as created by previous commands!",
				                var, cmd.getClass().getName());
				checkOpt = false;
			}

		}
		if (checkOpt && optionalVars.size() > 0) {
			this.log
			        .debug(
			                "Command {} has access to all optional requested variables!",
			                cmd.getClass().getName());
		}
		return optionalVars;
	}

	protected Collection<String> checkRequiredVariables(
	        final AFragmentCommand cmd, final Collection<String> requiredVars,
	        final HashSet<String> providedVariables)
	        throws ConstraintViolationException {
		if (requiredVars.size() == 0) {
			this.log.debug("No required variables declared!");
			return requiredVars;
		}
		boolean check = true;
		final Collection<String> failedVars = new ArrayList<String>();
		for (String var : requiredVars) {
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
		List<?> pipeline = Factory.getInstance().getConfiguration().getList(
		        "pipeline");
		if ((pipeline != null) && !pipeline.isEmpty()) {// generic for elements
			// in pipeline
			final List<String> ls = StringTools.toStringList(pipeline);
			this.pipeline = ls;
		} else {
			throw new IllegalArgumentException(
			        "Could not create command sequence!");
		}
	}

	@Override
	public Collection<AFragmentCommand> getCommands() {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.ucar.ma2.CommandSequence#hasNext()
	 */
	public boolean hasNext() {
		return this.iter.hasNext();
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
	protected AFragmentCommand loadCommand(final String clsname) {
		EvalTools.notNull(clsname, this);
		final AFragmentCommand clazz = Factory.getInstance().instantiate(
		        clsname, AFragmentCommand.class);
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
			final AFragmentCommand cmd = this.iter.next();
			cmd.setIWorkflow(getIWorkflow());
			// log.info("Next ICommand: {}",cmd.getClass().getName());
			this.log
			        .info("#############################################################################");
			this.log.info("# Running {}/{}: {}", new Object[] { (this.cnt + 1),
			        this.commands.size(), cmd.getClass().getSimpleName() });
			this.log.debug("# Package: {}", cmd.getClass().getPackage()
			        .getName());
			this.log
			        .info("#############################################################################");
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
	public void setCommands(final Collection<AFragmentCommand> c) {
		EvalTools.inRangeI(1, Integer.MAX_VALUE, c.size(), this);
		this.commands = new ArrayList<AFragmentCommand>(c);
		this.iter = this.commands.iterator();
		this.cnt = 0;
	}

	public void setPipeline(final Collection<String> s) {
		this.pipeline = new ArrayList<String>(s);
	}

	@Override
	public void setInput(final TupleND<IFileFragment> t) {
		// this.input = t;
		this.tmp = t;
		init();
	}

	@Override
	public void setIWorkflow(final IWorkflow iw1) {
		this.iw = iw1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.datastructures.pipeline.ICommandSequence#init()
	 */
	@Override
	public void init() {
		List<AFragmentCommand> cmds = addToList(this.tmp, this.pipeline);
		Factory.getInstance().log.debug(this.pipeline.toString());
		EvalTools.inRangeI(1, Integer.MAX_VALUE, cmds.size(), this);
		setCommands(cmds);
	}
}
