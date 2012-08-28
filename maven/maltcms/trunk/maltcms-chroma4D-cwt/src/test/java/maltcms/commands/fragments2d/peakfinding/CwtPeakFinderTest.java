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
