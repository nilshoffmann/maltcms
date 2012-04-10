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
package net.sf.maltcms.maltcms.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;
import maltcms.commands.filters.array.MovingAverageFilter;
import maltcms.commands.filters.array.TopHatFilter;
import maltcms.commands.fragments.peakfinding.TICPeakFinder;
import maltcms.commands.fragments.preprocessing.DefaultVarLoader;
import maltcms.commands.fragments.preprocessing.DenseArrayProducer;
import maltcms.io.ZipResourceExtractor;
import maltcms.test.AFragmentCommandTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.workflow.IWorkflow;

/**
 *
 * @author nilshoffmann
 */
public class BiPaceTest extends AFragmentCommandTest {
    
    @Rule
    public TemporaryFolder tf = new TemporaryFolder();

    @Test
    public void testChromA() {
        File dataFolder = tf.newFolder("chromaTestData");
        File inputFile1 = ZipResourceExtractor.extract(
                "/cdf/1D/glucoseA.cdf.gz", dataFolder);
        File inputFile2 = ZipResourceExtractor.extract(
                "/cdf/1D/glucoseB.cdf.gz", dataFolder);
        File outputBase = tf.newFolder("chromaTestOut");
        List<IFragmentCommand> commands = new ArrayList<IFragmentCommand>();
        commands.add(new DefaultVarLoader());
        commands.add(new DenseArrayProducer());
        TICPeakFinder tpf = new TICPeakFinder();
        MovingAverageFilter maf = new MovingAverageFilter();
        maf.setWindow(10);
        TopHatFilter thf = new TopHatFilter();
        thf.setWindow(50);
        tpf.setFilter(Arrays.asList(maf, thf));
//        tpf.setSnrWindow(10);
        tpf.setPeakThreshold(0.0d);
        commands.add(tpf);
//        commands.add(new PeakCliqueAlignment());
        IWorkflow w = createWorkflow(outputBase, commands, Arrays.asList(
                inputFile1, inputFile2));
        try {

            w.call();
            w.save();
        } catch (Exception ex) {
            Assert.fail(ex.getLocalizedMessage());
        }

    }
}
