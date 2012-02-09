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

import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.ImmutableFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FragmentTools;
import java.io.File;
import java.io.Serializable;
import java.util.concurrent.Callable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author nils
 */
@Slf4j
@Data
public class DefaultVarLoaderWorker implements Callable<File>, Serializable {
    
    private File fileToLoad;
    private File fileToSave;

    @Override
    public File call() throws Exception {
        EvalTools.notNull(fileToLoad, this);
        EvalTools.notNull(fileToSave, this);
        //create a new working fragment
        IFileFragment output = new FileFragment(fileToSave);
        //add source file for data retrieval
        output.addSourceFile(new ImmutableFileFragment(fileToLoad));
        FragmentTools.loadDefaultVars(output);
        FragmentTools.loadAdditionalVars(output);
        //save working fragment
        output.save();
        return new File(output.getAbsolutePath());
    }
}
