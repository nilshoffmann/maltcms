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
package maltcms.commands.fragments.preprocessing.defaultVarLoader;

import cross.Factory;
import cross.datastructures.fragments.*;
import cross.datastructures.tools.EvalTools;
import cross.io.misc.FragmentStringParser;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Worker implementation to load default and additional variables for an
 * individual
 *
 * @see IFileFragment.
 *
 * Uses
 * @see FragmentStringParser to decode String representation of
 * @param fileToLoad.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Slf4j
@Data
public class DefaultVarLoaderWorker implements Callable<File>, Serializable {

    private String fileToLoad;
    private String fileToSave;
    private List<String> defaultVariables = Collections.emptyList();
    private List<String> additionalVariables = Collections.emptyList();

    @Override
    public File call() throws Exception {
        EvalTools.notNull(fileToLoad, this);
        EvalTools.notNull(fileToSave, this);
        //create a new working fragment
        IFileFragment input = new FragmentStringParser().parse(fileToLoad);
        IFileFragment output = new FileFragment(new File(fileToSave));
        //add source file for data retrieval
        loadDefaultVariables(input, output);
        loadAdditionalVariables(input, output);
        //add source file afterwards to avoid accidental pull-in of variables
        output.addSourceFile(new ImmutableFileFragment(input));
        //save working fragment
        output.save();
        return new File(output.getAbsolutePath());
    }

    private IVariableFragment createVariable(IFileFragment output, IVariableFragment sourceVar) {
        IVariableFragment targetVar = output.hasChildren(sourceVar.getName()) ? output.getChild(sourceVar.getName()) : VariableFragment.createCompatible(output, sourceVar);
        if (sourceVar.getIndex() != null) {
            IVariableFragment sourceIndexVar = sourceVar.getIndex();
            IVariableFragment targetIndexVar = null;
            if (output.hasChild(sourceIndexVar.getName())) {
                targetIndexVar = output.getChild(sourceIndexVar.getName());
            } else {
                targetIndexVar = createVariable(output, sourceIndexVar);
            }
            targetVar.setIndex(targetIndexVar);
            targetVar.setIndexedArray(sourceVar.getIndexedArray());
        } else {
            targetVar.setArray(sourceVar.getArray());
        }
        return targetVar;
    }

    public void loadDefaultVariables(IFileFragment input, IFileFragment output) {
        for (String var : defaultVariables) {
            if (!var.equals("") && !var.trim().isEmpty()) {
                log.debug("Loading var {}", var);
                createVariable(output, input.getChild(var));
            }
        }
    }

    public void loadAdditionalVariables(IFileFragment input, IFileFragment output) {
        for (String var : additionalVariables) {
            if (var.equals("*")) { // load all available Variables
                log.debug("Loading all available vars!");
                final ArrayList<IVariableFragment> al;
                try {
                    al = Factory.getInstance().getDataSourceFactory().getDataSourceFor(input).readStructure(input);
                    for (final IVariableFragment vf : al) {
                        createVariable(output, vf);
                    }
                } catch (IOException ex) {
                    log.warn("{}", ex.getLocalizedMessage());
                }

            } else if (!var.equals("") && !var.trim().isEmpty()) {
                log.debug("Loading var {}", var);
                createVariable(output, input.getChild(var));
            }
        }
    }
}
