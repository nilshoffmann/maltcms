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
