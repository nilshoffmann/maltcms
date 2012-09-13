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
package maltcms.commands.fragments2d.peakfinding;

import maltcms.test.AFragmentCommandTest;

/**
 *
 * @author nilshoffmann
 */
public class CwtPeakFinderTest extends AFragmentCommandTest {
//    @Rule
//    public TemporaryFolder tf = new TemporaryFolder();
//
//    @Test
//    public void testPeakFinder() {
//
//        File inputFile = ZipResourceExtractor.extract(
//                "/cdf/2D/090306_37_FAME_Standard_1.cdf.gz", tf.newFolder(
//                "cdf2D"));
//
//        Default2DVarLoader d2vl = new Default2DVarLoader();
//        d2vl.setEstimateModulationTime(true);
//
//        CwtPeakFinder cpf = new CwtPeakFinder();
//
//        List<IFragmentCommand> l = new LinkedList<IFragmentCommand>();
//        l.add(d2vl);
//        l.add(cpf);
//        IWorkflow w = new DefaultWorkflow();
//        TupleND<IFileFragment> tmp = new TupleND<IFileFragment>(new FileFragment(inputFile.
//                getAbsoluteFile()));
//        CommandPipeline cp = new CommandPipeline();
//        cp.setCommands(l);
//        cp.setInput(tmp);
//        w.setCommandSequence(cp);
//        System.out.println(
//                "Running " + w.getCommandSequence().getCommands().size() + " commands on " + cp.
//                getInput().size() + " input files.");
//
//        for (IFragmentCommand cmd : l) {
//            cmd.setWorkflow(w);
//            tmp = cmd.apply(tmp);
//        }
////        w.save();
//    }
//
//    @After
//    public void cleanUp() {
//        tf.delete();
//    }
//
//    public static void main(String[] args) {
//        CwtPeakFinderTest test = new CwtPeakFinderTest();
//        test.testPeakFinder();
//    }
}
