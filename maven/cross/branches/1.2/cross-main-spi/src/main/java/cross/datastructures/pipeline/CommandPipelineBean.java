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

package cross.datastructures.pipeline;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowResult;
import cross.event.IEvent;
import java.util.Collection;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;


/**
 *
 * @author nils
 */
@Slf4j
@Data
public class CommandPipelineBean implements ICommandSequence {

    private Collection<IFragmentCommand> commands;
    
    private TupleND<IFileFragment> input;
    
    private IWorkflow workflow;
    
    private boolean checkCommandDependencies;

    @Override
    public boolean hasNext() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init() {
        
    }

    @Override
    public TupleND<IFileFragment> next() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void listen(IEvent<IWorkflowResult> v) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void appendXML(Element e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
