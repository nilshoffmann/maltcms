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
package maltcms.commands.fragments.io;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.workflow.IWorkflow;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import maltcms.commands.fragments.peakfinding.TICPeakFinderTest;
import maltcms.commands.fragments.preprocessing.DefaultVarLoader;
import maltcms.commands.fragments.preprocessing.DenseArrayProducer;
import maltcms.test.AFragmentCommandTest;
import maltcms.test.ExtractClassPathFiles;
import maltcms.test.ZipResourceExtractor;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author Nils Hoffmann
 */
public class ObiWarplmataExporterTest extends AFragmentCommandTest {

    @Rule
    public ExtractClassPathFiles ecpf = new ExtractClassPathFiles(tf,
            "/cdf/1D/glucoseA.cdf.gz");
    /**
     *
     */
    @Test
    public void testObiWarplmataExporter() throws IOException {
        File dataFolder = tf.newFolder("chromaTest Data ö");
        File outputBase = tf.newFolder(TICPeakFinderTest.class.getName());
        List<IFragmentCommand> commands = new ArrayList<>();
        commands.add(new DefaultVarLoader());
        commands.add(new DenseArrayProducer());
        ObiWarplmataExporter exporter = new ObiWarplmataExporter();
        commands.add(exporter);
        IWorkflow w = createWorkflow(outputBase, commands, ecpf.getFiles());
        testWorkflow(w);
    }
}
