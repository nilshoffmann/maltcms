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
package cross.commands.fragments;

import cross.Factory;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.*;
import cross.event.EventSource;
import cross.event.IEvent;
import cross.event.IEventSource;
import cross.event.IListener;
import cross.tools.StringTools;
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
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
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

    public void initSubCommand(AFragmentCommand fragmentCommand) {
        fragmentCommand.setWorkflow(workflow);
        fragmentCommand.configure(workflow.getConfiguration());
    }

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
            log.warn("{} tasks failed with exception:\n{}", ics.getFailedTasks().size(), ex.getLocalizedMessage());
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

    @Override
    public void appendXML(final Element e) {
    }

    /**
     * As of release 1.2.2, please use the spring beans based configuration for
     * AFragmentCommand instances.
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
        final IFileFragment copy = Factory.getInstance().getFileFragmentFactory().create(new File(getWorkflow().getOutputDirectory(this),
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

    public <T extends Serializable> ICompletionService<T> createCompletionService(
            Class<? extends T> serviceObjectType) {
        return createNonBlockingCompletionService(serviceObjectType, 1000,
                TimeUnit.MILLISECONDS);
    }

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

    public void addWorkflowResults(IFileFragment... fragments) {
        for (IFileFragment fragment : fragments) {
            addWorkflowResult(fragment);
        }
    }

    public void addWorkflowResults(TupleND<IFileFragment> fragments) {
        for (IFileFragment fragment : fragments) {
            addWorkflowResult(fragment);
        }
    }

    public void addWorkflowResult(IFileFragment fragment) {
        getWorkflow().append(
                new DefaultWorkflowResult(new File(fragment.getAbsolutePath()),
                this, getWorkflowSlot(), fragment));
    }

    public void addWorkflowResult(IFileFragment fragment,
            IFileFragment... resources) {
        getWorkflow().append(
                new DefaultWorkflowResult(new File(fragment.getAbsolutePath()),
                this, getWorkflowSlot(), resources));
    }

    public void addWorkflowResult(IFileFragment fragment, WorkflowSlot slot,
            IFileFragment... resources) {
        getWorkflow().append(
                new DefaultWorkflowResult(new File(fragment.getAbsolutePath()),
                this, slot, resources));
    }

    public void addWorkflowResult(IFileFragment fragment,
            IWorkflowElement producer, WorkflowSlot slot,
            IFileFragment... resources) {
        getWorkflow().append(
                new DefaultWorkflowResult(new File(fragment.getAbsolutePath()),
                producer, slot, resources));
    }

    public void initProgress(int size) {
        EvalTools.isNull(progress,this);
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
}
