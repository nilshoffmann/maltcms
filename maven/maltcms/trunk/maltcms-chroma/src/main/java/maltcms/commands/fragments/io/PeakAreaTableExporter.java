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

import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * Exports aligned peak area table.
 *
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 *
 *
 */
@RequiresVariables(names={"var.tic_peaks"})
@ServiceProvider(service=AFragmentCommand.class)
public class PeakAreaTableExporter extends AFragmentCommand {

    @Override
    public String getDescription() {
        return "Creates a csv file from a multiple alignment and peak data.";
    }

    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> in) {
        //FIXME
        //alignment auslesen
        //peak_area auslesen
        return in;
    }

    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.FILEIO;
    }
    
}
