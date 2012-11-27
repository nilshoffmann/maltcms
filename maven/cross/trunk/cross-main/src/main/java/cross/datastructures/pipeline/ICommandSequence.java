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

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowResult;
import cross.event.IEvent;
import cross.event.IListener;
import cross.io.xml.IXMLSerializable;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract sequence of commands on FileFragment objects.
 *
 * @author Nils Hoffmann
 *
 */
public interface ICommandSequence extends Iterator<TupleND<IFileFragment>>,
        IListener<IEvent<IWorkflowResult>>, IXMLSerializable {

    public abstract List<IFragmentCommand> getCommands();

    /**
     * Return input to this ICommandSequence.
     *
     * @return
     */
    public abstract TupleND<IFileFragment> getInput();

    public abstract IWorkflow getWorkflow();

    /**
     * Do we have any unprocessed Commands left?
     */
    @Override
    public abstract boolean hasNext();

    public abstract boolean validate();

    /**
     * Apply the next ICommand and return results.
     */
    @Override
    public abstract TupleND<IFileFragment> next();

    @Override
    public abstract void remove();

    public abstract void setCommands(List<IFragmentCommand> c);

    public abstract void setInput(TupleND<IFileFragment> t);

    public abstract void setWorkflow(IWorkflow iw);

    public abstract boolean isCheckCommandDependencies();

    public abstract void setCheckCommandDependencies(boolean checkCommandDependencies);
    
    /**
     * If <code>true</code>, will throw a {@link ConstraintViolationException}
     * if any {@link IFileFragment} has unsaved changes, as determined by calling
     * <code>f.isModified()</code>. Otherwise, a warning is logged. 
     * 
     * @param b
     */
    public abstract void setThrowExceptionOnUnsavedModification(boolean b);
    
    public abstract boolean isThrowExceptionOnUnsavedModification();
}
