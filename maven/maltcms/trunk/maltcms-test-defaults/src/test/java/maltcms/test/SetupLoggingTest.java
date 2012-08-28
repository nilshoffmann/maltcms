/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package maltcms.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;

/**
 *
 * @author nils
 */
@Slf4j
public class SetupLoggingTest {

    @Rule
    public SetupLogging sl = new SetupLogging();

    public SetupLoggingTest() {
    }

    @Test
    public void testSomeMethod() {
        System.out.println("Logging configuration: ");
        System.out.println(sl.getConfig());
        log.info("Testing logging output!");
    }
}
