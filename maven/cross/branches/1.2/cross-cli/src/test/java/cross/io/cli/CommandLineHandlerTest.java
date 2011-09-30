/**
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 *
 * This file is part of Cross/Maltcms.
 *
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * 
 *
 * $Id$
 */
package cross.io.cli;

import cross.io.cli.CustomSecurityManager.ExitException;
import java.io.File;
import static junit.framework.Assert.*;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openide.util.Lookup;

/**
 *
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 */
public class CommandLineHandlerTest {

    private static SecurityManager securityManager;
    private static String userHome;
    @Rule
    public static TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() throws Exception {
        //setup logging
        BasicConfigurator.configure();
        //setup custom security manager to intercept System.exit
        securityManager = System.getSecurityManager();
        System.setSecurityManager(new CustomSecurityManager());
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.setSecurityManager(securityManager);

    }

    @Before
    public void setUp() throws Exception {
        System.out.println("Root of temp folder is: " + folder.getRoot().
                getAbsolutePath());
        userHome = System.getProperty("user.home");
        System.out.println("Old user home is: " + userHome);
    }

    @After
    public void tearDown() throws Exception {
        System.setProperty("user.home", userHome);
    }

    @Test
    public void listCliHandlers() throws Exception {
        try {
            String[] commandLine = new String[]{"--list-cli-handlers"};
            Processor clh = Lookup.getDefault().lookup(IProcessorFactory.class).create();
            clh.processCommandLine(commandLine);
        } catch (ExitException ee) {
            System.out.println(ee.getLocalizedMessage());
            assertEquals(1, ee.status);
        }
    }

    @Test
    public void mock1() throws Exception {
        String[] commandLine = new String[]{"--make-clean"};
        Processor clh = Lookup.getDefault().lookup(IProcessorFactory.class).create();
        clh.processCommandLine(commandLine);

    }

    @Test
    public void testTempDir() throws Exception {

        System.setProperty("user.home", folder.getRoot().getAbsolutePath());
        File testFolder = folder.newFolder("testFolder");
        File testFile = new File(testFolder, "myTestFile.txt");
        testFile.createNewFile();
        assertTrue(testFile.exists());

    }
}
