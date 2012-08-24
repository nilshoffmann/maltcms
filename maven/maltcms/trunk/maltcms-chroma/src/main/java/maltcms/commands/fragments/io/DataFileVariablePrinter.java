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
package maltcms.commands.fragments.io;


import cross.Factory;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import java.io.IOException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.lookup.ServiceProvider;

/**
 * Prints available variables for provided file fragments.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class DataFileVariablePrinter extends AFragmentCommand {

    private final String description = "Prints available variables for provided file fragments";
    private final WorkflowSlot workflowSlot = WorkflowSlot.FILEIO;

    /*
     * (non-Javadoc)
     * 
     * @see cross.commands.ICommand#apply(java.lang.Object)
     */
    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
        for(IFileFragment f:t) {
            try {
                Factory.getInstance().getDataSourceFactory().getDataSourceFor(f).
                        readStructure(f);
            } catch (final IOException e) {
                throw new RuntimeException(e.fillInStackTrace());
            }
            log.info("{}",FileFragment.printFragment(f));
        }
        return t;
    }
}

