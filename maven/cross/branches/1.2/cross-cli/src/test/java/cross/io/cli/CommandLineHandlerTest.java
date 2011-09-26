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
import junit.framework.Assert;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author nilshoffmann
 */
public class CommandLineHandlerTest {

    private CommandLineHandler clh;
    private SecurityManager securityManager;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BasicConfigurator.configure();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        securityManager = System.getSecurityManager();
        System.setSecurityManager(new CustomSecurityManager());
        clh = CommandLineHandler.getInstance();
    }

    @After
    public void tearDown() throws Exception {
        System.setSecurityManager(securityManager);
    }

    @Test
    public void handleCommandLine() throws Exception {
        try {
            String[] commandLine = new String[]{"--list-cli-handlers"};
            clh.handleCommandLine(commandLine);
        } catch (ExitException ee) {
            System.out.println(ee.getLocalizedMessage());
            Assert.assertEquals(1, ee.status);
        }
    }
}
