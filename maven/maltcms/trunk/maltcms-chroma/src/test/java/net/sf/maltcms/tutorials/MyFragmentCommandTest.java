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
package net.sf.maltcms.tutorials;

import cross.applicationContext.DefaultApplicationContextFactory;
import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.tools.FragmentTools;
import cross.datastructures.workflow.IWorkflow;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import maltcms.commands.filters.array.AArrayFilter;
import maltcms.commands.filters.array.LogFilter;
import maltcms.commands.filters.array.SqrtFilter;
import maltcms.test.AFragmentCommandTest;
import maltcms.test.ExtractClassPathFiles;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author Nils Hoffmann
 */
public class MyFragmentCommandTest extends AFragmentCommandTest {
    
    @Rule
    public ExtractClassPathFiles ecpf = new ExtractClassPathFiles(tf,
            "/cdf/1D/glucoseA.cdf.gz", "/cdf/1D/glucoseB.cdf.gz");
    
    /**
     * Test of getDescription method, of class MyFragmentCommand.
     */
    @Test
    public void testProgrammaticWorkflow() {
        File outputBase = tf.newFolder("chromaTestOut");
        List<IFragmentCommand> commands = new ArrayList<IFragmentCommand>();
        MyFragmentCommand cmd = new MyFragmentCommand();
        cmd.setFilter(Arrays.asList(new AArrayFilter[]{new SqrtFilter()}));
        commands.add(cmd);
        //add a transparent fragment command
        commands.add(new SameInSameOutFragmentCommand());
        //add a second filter fragment command that retrieves data already 
        //written by cmd
        MyFragmentCommand cmd2 = new MyFragmentCommand();
        cmd2.setFilter(Arrays.asList(new AArrayFilter[]{new LogFilter()}));
        commands.add(cmd2);
        commands.add(new DecoratingFragmentCommand());
        commands.add(new SameInSameOutFragmentCommand());
        commands.add(new TransparentFragmentCommand());
        IWorkflow w = createWorkflow(outputBase, commands, ecpf.getFiles());
        testWorkflow(w);
    }

    /**
     * Test of apply method, of class MyFragmentCommand.
     */
    @Test
    public void testApplicationContextWorkflow() {
        File outputBase = tf.newFolder("chromaTestOut");
        DefaultApplicationContextFactory dacf = new DefaultApplicationContextFactory(Arrays.asList("/cfg/pipelines/xml/workflowDefaults.xml","/cfg/pipelines/xml/myFragmentCommand.xml"), new PropertiesConfiguration());
        ApplicationContext ac = dacf.createClassPathApplicationContext();
        IWorkflow w = ac.getBean(IWorkflow.class);
        w.getCommandSequence().setInput(FragmentTools.immutable(ecpf.getFiles()));
        w.setOutputDirectory(outputBase);
        testWorkflow(w);
    }
}
