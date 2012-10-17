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
package cross.main.commands.fragments;

import cross.main.datastructures.workflow.DefaultWorkflowResult;
import cross.main.datastructures.workflow.DefaultWorkflowProgressResult;
import cross.commands.fragments.IFragmentCommand;
import cross.main.Factory;
import cross.main.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.main.datastructures.tools.EvalTools;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.IWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.event.EventSource;
import cross.event.IEvent;
import cross.event.IEventSource;
import cross.event.IListener;
import cross.exception.MappingNotAvailableException;
import cross.tools.StringTools;
import cross.main.vocabulary.CvResolver;
import cross.vocabulary.ICvResolver;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sf.mpaxs.api.ICompletionService;
import net.sf.mpaxs.spi.concurrent.CompletionServiceFactory;
import org.apache.commons.configuration.Configuration;
import org.jdom.Element;

/**
 * A class providing a default implementation for configure and a concrete
 * typing of the untyped superclass {@link cross.commands.ICommand}.
 *
 * Use objects extending this class as commands within a FileFragment-based
 * pipeline.
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
@Data
public abstract class AFragmentCommand implements IFragmentCommand {

    private static final long serialVersionUID = -4551167359317007776L;
    @Getter(AccessLevel.NONE)
    private final IEventSource<IWorkflowResult> eventSource = new EventSource<IWorkflowResult>();
    private IWorkflow workflow = null;
    private DefaultWorkflowProgressResult progress = null;
    private ICvResolver cvResolver = new CvResolver();

    /**
     *
     * @param fragmentCommand
     */
    public void initSubCommand(AFragmentCommand fragmentCommand) {
        fragmentCommand.setWorkflow(workflow);
        fragmentCommand.configure(workflow.getConfiguration());
    }

    /**
     *
     * @param ics
     * @param t
     * @return
     */
    public TupleND<IFileFragment> postProcess(ICompletionService<File> ics,
            final TupleND<IFileFragment> t) {
        TupleND<IFileFragment> ret = new TupleND<IFileFragment>();
        try {
            List<File> results = ics.call();
            // expect at least one result
            EvalTools.gt(0, results.size(), this);
            // map input to results
            ret = mapToInput(results, t);
            // append results to workflow for bookkeeping
            addWorkflowResults(ret);
        } catch (Exception ex) {
            log.error("Caught exception while executing workers: ", ex);
            throw new RuntimeException(ex);
        }
        return ret;
    }

    /**
     * @param l
     * @see
     * cross.event.IEventSource#addListener(cross.event.IListener<cross.event
     * .IEvent<V>>[])
     */
    @Override
    public void addListener(final IListener<IEvent<IWorkflowResult>> l) {
        this.eventSource.addListener(l);
    }

    /**
     *
     * @param e
     */
    @Override
    public void appendXML(final Element e) {
    }

    /**
     * As of release 1.2.2, please use the spring beans based configuration for
     * AFragmentCommand instances. Only configuration of variable name mappings
     * should be performed using configure.
     *
     * @param cfg
     * @deprecated
     */
    @Override
    @Deprecated
    public void configure(final Configuration cfg) {
    }

    /**
     * @param e
     * @see cross.event.IEventSource#fireEvent(cross.event.IEvent)
     */
    @Override
    public void fireEvent(final IEvent<IWorkflowResult> e) {
        this.eventSource.fireEvent(e);
    }

    /**
     *
     * @return
     */
    public abstract String getDescription();

    /**
     * Utility method to create mutable FileFragments from a given tuple of
     * FileFragments.
     *
     * @param t
     * @return
     */
    public TupleND<IFileFragment> createWorkFragments(TupleND<IFileFragment> t) {
        TupleND<IFileFragment> wt = new TupleND<IFileFragment>();
        for (IFileFragment iff : t) {
            wt.add(createWorkFragment(iff));
        }
        return wt;
    }

    /**
     * Utility method to create a mutable FileFragment to work on.
     *
     * @param iff
     * @return
     */
    public IFileFragment createWorkFragment(IFileFragment iff) {
        final IFileFragment copy = new FileFragment(new File(getWorkflow().getOutputDirectory(this),
                StringTools.removeFileExt(iff.getName()) + ".cdf"));
        copy.addSourceFile(iff);
        return copy;
    }

    /**
     * Maps a list of Files which resemble processing results of input file
     * fragments to the input file fragments in the right order.
     *
     * @param files
     * @param inputFragments
     * @return
     */
    public TupleND<IFileFragment> mapToInput(List<File> files,
            TupleND<IFileFragment> inputFragments) {
        HashMap<String, File> names = new LinkedHashMap<String, File>();
        for (File f : files) {
            names.put(StringTools.removeFileExt(f.getName()), f);
        }
        TupleND<IFileFragment> retFragments = new TupleND<IFileFragment>();
        for (IFileFragment fragment : inputFragments) {
            retFragments.add(new FileFragment(names.get(StringTools.removeFileExt(fragment.getName()))));
        }
        return retFragments;
    }

    /**
     *
     * @param <T>
     * @param serviceObjectType
     * @return
     */
    public <T extends Serializable> ICompletionService<T> createCompletionService(
            Class<? extends T> serviceObjectType) {
        return createNonBlockingCompletionService(serviceObjectType, 1000,
                TimeUnit.MILLISECONDS);
    }

    /**
     *
     * @param <T>
     * @param serviceObjectType
     * @return
     */
    public <T extends Serializable> ICompletionService<T> createBlockingCompletionService(
            Class<? extends T> serviceObjectType) {
        ICompletionService<T> ics = null;
        CompletionServiceFactory<T> csf = new CompletionServiceFactory<T>();
        csf.setBlockingWait(true);
        if (getWorkflow().isExecuteLocal()) {
            log.info("Creating local completion service!");
            csf.setMaxThreads(Factory.getInstance().getConfiguration().getInt("cross.Factory.maxthreads", 1));
            ics = csf.newLocalCompletionService();
        } else {
            log.info("Creating mpaxs completion service!");
            ics = new CompletionServiceFactory<T>().newDistributedCompletionService();
        }
        return ics;
    }

    /**
     *
     * @param <T>
     * @param serviceObjectType
     * @param timeOut
     * @param timeUnit
     * @return
     */
    public <T extends Serializable> ICompletionService<T> createNonBlockingCompletionService(
            Class<? extends T> serviceObjectType, long timeOut,
            TimeUnit timeUnit) {
        ICompletionService<T> ics = null;
        CompletionServiceFactory<T> csf = new CompletionServiceFactory<T>();
        csf.setTimeOut(timeOut);
        csf.setTimeUnit(timeUnit);
        if (getWorkflow().isExecuteLocal()) {
            log.info("Creating local completion service!");
            csf.setMaxThreads(Factory.getInstance().getConfiguration().getInt("cross.Factory.maxthreads", 1));
            ics = csf.newLocalCompletionService();
        } else {
            log.info("Creating mpaxs completion service!");
            ics = new CompletionServiceFactory<T>().newDistributedCompletionService();
        }
        return ics;
    }

    /**
     *
     * @param fragments
     */
    public void addWorkflowResults(IFileFragment... fragments) {
        for (IFileFragment fragment : fragments) {
            addWorkflowResult(fragment);
        }
    }

    /**
     *
     * @param fragments
     */
    public void addWorkflowResults(TupleND<IFileFragment> fragments) {
        for (IFileFragment fragment : fragments) {
            addWorkflowResult(fragment);
        }
    }

    /**
     *
     * @param fragment
     */
    public void addWorkflowResult(IFileFragment fragment) {
        getWorkflow().append(
                new DefaultWorkflowResult(new File(fragment.getAbsolutePath()),
                this, getWorkflowSlot(), fragment));
    }

    /**
     *
     * @param fragment
     * @param resources
     */
    public void addWorkflowResult(IFileFragment fragment,
            IFileFragment... resources) {
        getWorkflow().append(
                new DefaultWorkflowResult(new File(fragment.getAbsolutePath()),
                this, getWorkflowSlot(), resources));
    }

    /**
     *
     * @param fragment
     * @param slot
     * @param resources
     */
    public void addWorkflowResult(IFileFragment fragment, WorkflowSlot slot,
            IFileFragment... resources) {
        getWorkflow().append(
                new DefaultWorkflowResult(new File(fragment.getAbsolutePath()),
                this, slot, resources));
    }

    /**
     *
     * @param fragment
     * @param producer
     * @param slot
     * @param resources
     */
    public void addWorkflowResult(IFileFragment fragment,
            IWorkflowElement producer, WorkflowSlot slot,
            IFileFragment... resources) {
        getWorkflow().append(
                new DefaultWorkflowResult(new File(fragment.getAbsolutePath()),
                producer, slot, resources));
    }

    /**
     *
     * @param size
     */
    public void initProgress(int size) {
        EvalTools.isNull(progress, this);
        setProgress(
                new DefaultWorkflowProgressResult(
                size, this, getWorkflowSlot()));
    }

    /**
     * @param l
     * @see cross.event.IEventSource#removeListener(cross.event.IListener<cross.
     * event.IEvent<V>>[])
     */
    @Override
    public void removeListener(final IListener<IEvent<IWorkflowResult>> l) {
        this.eventSource.removeListener(l);
    }
    
    public String resolve(String varname) {
        try{
            String resolved = cvResolver.translate(varname);
            return resolved;
        }catch(MappingNotAvailableException mnae) {
            log.warn("Could not map variable: "+varname,mnae);
            return varname;
        }
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return getClass().getName();
    }
}
