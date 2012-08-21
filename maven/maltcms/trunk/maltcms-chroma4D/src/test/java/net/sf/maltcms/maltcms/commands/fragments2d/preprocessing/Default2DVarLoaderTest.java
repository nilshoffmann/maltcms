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
package net.sf.maltcms.maltcms.commands.fragments2d.preprocessing;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.workflow.IWorkflow;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import junit.framework.Assert;
import maltcms.commands.fragments2d.preprocessing.Default2DVarLoader;
import cross.io.misc.ZipResourceExtractor;
import maltcms.test.AFragmentCommandTest;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author nilshoffmann
 */
public class Default2DVarLoaderTest extends AFragmentCommandTest {

    @Rule
    public TemporaryFolder tf = new TemporaryFolder();

    @Test
    public void testPeakFinder() {
        File dataFolder = tf.newFolder("chroma4DTestData");
        File outputBase = tf.newFolder("chroma4DTestOut");
        File inputFile = ZipResourceExtractor.extract(
                "/cdf/2D/090306_37_FAME_Standard_1.cdf.gz", dataFolder);


        Default2DVarLoader d2vl = new Default2DVarLoader();
        d2vl.setEstimateModulationTime(false);
        d2vl.setModulationTime(5.0d);

        List<IFragmentCommand> l = new LinkedList<IFragmentCommand>();
        l.add(d2vl);

        IWorkflow w = createWorkflow(outputBase, l, Arrays.asList(inputFile));
//        System.out.println(
//                "Running " + w.getCommandSequence().getCommands().size() + " commands on " + w.
//                getCommandSequence().
//                getInput().size() + " input files.");
        try {
            w.call();
            w.save();
        } catch (Exception ex) {
            Assert.fail(ex.getLocalizedMessage());
        }
    }

    @After
    public void cleanUp() {
//        tf.delete();
    }

    public static void main(String[] args) {
        Default2DVarLoaderTest test = new Default2DVarLoaderTest();
        test.testPeakFinder();
    }
}
