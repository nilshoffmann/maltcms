/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
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
package maltcms.commands.fragments.preprocessing.defaultVarLoader;

import cross.Factory;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.ImmutableFileFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tools.EvalTools;
import cross.io.misc.FragmentStringParser;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import lombok.Data;
import org.slf4j.LoggerFactory;


/**
 * Worker implementation to load default and additional variables for an
 * individual
 *
 * @see IFileFragment.
 *
 * Uses
 * @see FragmentStringParser to decode String representation of
 * @param fileToLoad.
 * @author Nils Hoffmann
 * 
 */

@Data
public class DefaultVarLoaderWorker implements Callable<File>, Serializable {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DefaultVarLoaderWorker.class);

    private URI fileToLoad;
    private URI fileToSave;
    private List<String> defaultVariables = Collections.emptyList();
    private List<String> additionalVariables = Collections.emptyList();

    /** {@inheritDoc} */
    @Override
    public File call() throws Exception {
        EvalTools.notNull(fileToLoad, this);
        EvalTools.notNull(fileToSave, this);
        //create a new working fragment
        IFileFragment input = new FileFragment(fileToLoad);
        IFileFragment output = new FileFragment(fileToSave);
        //add source file for data retrieval
        loadDefaultVariables(input, output);
        loadAdditionalVariables(input, output);
        //add source file afterwards to avoid accidental pull-in of variables
        output.addSourceFile(new ImmutableFileFragment(input));
        //save working fragment
        output.save();
        File result = new File(output.getUri());
        return result;
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

    /**
     * <p>loadDefaultVariables.</p>
     *
     * @param input a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param output a {@link cross.datastructures.fragments.IFileFragment} object.
     */
    public void loadDefaultVariables(IFileFragment input, IFileFragment output) {
        for (String var : defaultVariables) {
            if (!var.equals("") && !var.trim().isEmpty()) {
                log.debug("Loading var {}", var);
                createVariable(output, input.getChild(var));
            }
        }
    }

    /**
     * <p>loadAdditionalVariables.</p>
     *
     * @param input a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param output a {@link cross.datastructures.fragments.IFileFragment} object.
     */
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
