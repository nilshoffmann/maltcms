/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package maltcms.commands.fragments.io;

import cross.annotations.Configurable;
import java.io.File;
import java.util.ArrayList;

import maltcms.io.csv.CSVWriter;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.lookup.ServiceProvider;

/**
 * Writes 1D-variables to csv files, one entry per row.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Data
@Slf4j
@ServiceProvider(service = AFragmentCommand.class)
public class VariableDataExporter extends AFragmentCommand {

    private final String description = "Exports one-dimensional variables to csv format ";
    private final WorkflowSlot workflowSlot = WorkflowSlot.FILEIO;
    @Configurable
    private ArrayList<String> varNames = new ArrayList<String>(0);

    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        final CSVWriter csvw = new CSVWriter();
        csvw.setWorkflow(getWorkflow());
        for (final IFileFragment iff : t) {
            final File path = getWorkflow().getOutputDirectory(this);
            for (final String s : this.varNames) {
                final Array a = iff.getChild(s).getArray();
                csvw.writeArray(path.getAbsolutePath(), StringTools.
                        removeFileExt(iff.getName())
                        + "_" + s, a);
            }
        }
        return t;
    }
}