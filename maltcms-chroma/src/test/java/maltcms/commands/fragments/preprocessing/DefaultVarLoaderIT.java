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
package maltcms.commands.fragments.preprocessing;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.workflow.IWorkflow;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import maltcms.io.andims.NetcdfDataSource;
import maltcms.test.AFragmentCommandTest;
import maltcms.test.ExtractClassPathFiles;
import maltcms.tools.MaltcmsTools;
import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j

public class DefaultVarLoaderIT extends AFragmentCommandTest {

    @Rule
    public ExtractClassPathFiles testFiles = new ExtractClassPathFiles(tf,
            "/cdf/1D/glucoseA.cdf.gz");

    public DefaultVarLoaderIT() {
        setLogLevelFor(MaltcmsTools.class, Level.DEBUG);
        setLogLevelFor(NetcdfDataSource.class, Level.DEBUG);
    }

    /**
     * Test of apply method, of class DenseArrayProducer.
     */
    @Test
    public void testDirectApply() throws IOException {
        List<IFragmentCommand> commands = new ArrayList<>();
        DefaultVarLoader dvl = new DefaultVarLoader();
        commands.add(dvl);
        IWorkflow w = createWorkflow(commands, testFiles.getFiles());
        testWorkflow(w);
    }
}