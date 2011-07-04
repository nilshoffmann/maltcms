/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package maltcms.mpaxs;

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 *
 * @author nilshoffmann
 */
public class TestCallable implements Callable<Long>, Serializable {

    @Override
    public Long call() throws Exception {
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
//                Logger.getLogger(TestCallable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return System.nanoTime();
    }
}
