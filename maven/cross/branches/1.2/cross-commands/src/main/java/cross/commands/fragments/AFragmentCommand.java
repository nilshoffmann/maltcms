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

package cross.commands.fragments;

import java.io.File;

import org.jdom.Element;

import cross.commands.workflow.DefaultWorkflowResult;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.commands.workflow.IWorkflow;
import cross.commands.workflow.IWorkflowElement;
import cross.commands.workflow.IWorkflowResult;
import cross.commands.workflow.WorkflowSlot;
import cross.event.EventSource;
import cross.event.IEvent;
import cross.event.IEventSource;
import cross.event.IListener;
import cross.tools.StringTools;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.Data;
import net.sf.maltcms.execution.api.ICompletionService;
import net.sf.maltcms.execution.spi.CompletionServiceFactory;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * A class providing a default implementation for configure and a concrete
 * typing of the untyped superclass {@link cross.commands.ICommand}.
 * 
 * Use objects extending this class as commands within a FileFragment-based
 * pipeline.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * @param <V>
 * 
 */
@Data
public abstract class AFragmentCommand implements IFragmentCommand {

    @Getter(AccessLevel.NONE)
    private final IEventSource<IWorkflowResult> eventSource = new EventSource<IWorkflowResult>();
    private IWorkflow workflow = null;

    public void initSubCommand(IFragmentCommand fragmentCommand) {
        fragmentCommand.setWorkflow(workflow);
        //fragmentCommand.configure(workflow.getConfiguration());
    }
    
    /**
     * @param l
     * @see 
     *      cross.event.IEventSource#addListener(cross.event.IListener<cross.event
     *      .IEvent<V>>[])
     */
    @Override
    public void addListener(final IListener<IEvent<IWorkflowResult>> l) {
        this.eventSource.addListener(l);
    }

    @Override
    public void appendXML(final Element e) {
    }

//    @Override
//    public void configure(final Configuration cfg) {
//    }

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
        final IFileFragment copy = new FileFragment(
                new File(getWorkflow().getOutputDirectory(this),
                StringTools.removeFileExt(iff.getName())
                + ".cdf"));
        copy.addSourceFile(iff);
        return copy;
    }

    /**
     * Maps a list of Files which resemble processing results of input file fragments
     * to the input file fragments in the right order.
     * @param files
     * @param inputFragments
     * @return 
     */
    public TupleND<IFileFragment> mapToInput(List<File> files, TupleND<IFileFragment> inputFragments) {
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
     * Retrieve an instance of a completion service either for execution within
     * the same local virtual machine or for external execution.
     * 
     * @param <T>
     * @param serviceObjectType
     * @param executeInVM
     * @return 
     */
    public <T extends Serializable> ICompletionService<T> createCompletionService(Class<? extends T> serviceObjectType, boolean executeInVM) {
        ICompletionService<T> ics = null;
        if(executeInVM) {
            ics = new CompletionServiceFactory<T>().createVMLocalCompletionService();
        }else{
            ics = new CompletionServiceFactory<T>().createMpaxsCompletionService();
        }
        return ics;
    }
    
    /**
     * Retrieve an instance of a completion service either for execution within
     * the same local virtual machine or for external execution, as determined by
     * @see cross.datastructures.workflow.IWorkflow.isExecuteLocal() .
     * @param <T>
     * @param serviceObjectType
     * @return 
     */
    public <T extends Serializable> ICompletionService<T> createCompletionService(Class<? extends T> serviceObjectType) {
        return createCompletionService(serviceObjectType,getWorkflow().isExecuteLocal());
    }
    
    public void addWorkflowResults(IFileFragment...fragments) {
        for(IFileFragment fragment:fragments) {
            addWorkflowResult(fragment);
        }
    }
    
    public void addWorkflowResults(TupleND<IFileFragment> fragments) {
        for(IFileFragment fragment:fragments) {
            addWorkflowResult(fragment);
        }
    }

    public void addWorkflowResult(IFileFragment fragment) {
        getWorkflow().append(new DefaultWorkflowResult(new File(fragment.getAbsolutePath()), this, getWorkflowSlot(), fragment));
    }
    
    public void addWorkflowResult(IFileFragment fragment, IFileFragment... resources) {
        getWorkflow().append(new DefaultWorkflowResult(new File(fragment.getAbsolutePath()), this, getWorkflowSlot(), resources));
    }

    public void addWorkflowResult(IFileFragment fragment, WorkflowSlot slot, IFileFragment... resources) {
        getWorkflow().append(new DefaultWorkflowResult(new File(fragment.getAbsolutePath()), this, slot, resources));
    }

    public void addWorkflowResult(IFileFragment fragment, IWorkflowElement producer, WorkflowSlot slot, IFileFragment... resources) {
        getWorkflow().append(new DefaultWorkflowResult(new File(fragment.getAbsolutePath()), producer, slot, resources));
    }

    /**
     * @param l
     * @see cross.event.IEventSource#removeListener(cross.event.IListener<cross.
     *      event.IEvent<V>>[])
     */
    @Override
    public void removeListener(final IListener<IEvent<IWorkflowResult>> l) {
        this.eventSource.removeListener(l);
    }
}
