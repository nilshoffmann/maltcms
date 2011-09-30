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

import java.util.Collection;
import java.util.Iterator;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.commands.workflow.IWorkflow;
import cross.commands.workflow.IWorkflowResult;
import cross.datastructures.xml.IXMLSerializable;
import cross.event.IEvent;
import cross.event.IListener;

/**
 * Abstract sequence of commands on FileFragment objects.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public interface ICommandSequence extends Iterator<TupleND<IFileFragment>>,
        IListener<IEvent<IWorkflowResult>>, IXMLSerializable {

    public abstract Collection<IFragmentCommand> getCommands();

    public abstract void setCommands(Collection<IFragmentCommand> c);

    /**
     * Return input to this ICommandSequence.
     *
     * @return
     */
    public abstract TupleND<IFileFragment> getInput();

    public abstract void setInput(TupleND<IFileFragment> t);

    public abstract IWorkflow getWorkflow();

    public abstract void setWorkflow(IWorkflow iw);

    /**
     * Do we have any unprocessed Commands left?
     */
    @Override
    public abstract boolean hasNext();

    /**
     * Apply the next ICommand and return results.
     */
    @Override
    public abstract TupleND<IFileFragment> next();

    @Override
    public abstract void remove();

    public abstract boolean isCheckCommandDependencies();

    public abstract void setCheckCommandDependencies(
            boolean checkCommandDependencies);
}
