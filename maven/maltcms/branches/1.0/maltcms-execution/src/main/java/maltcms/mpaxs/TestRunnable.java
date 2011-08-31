/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package maltcms.mpaxs;

import java.io.Serializable;

/**
 *
 * @author nilshoffmann
 */
public class TestRunnable implements Runnable, Serializable{

    @Override
    public void run() {
        for(int i = 0;i<10; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                //Logger.getLogger(TestRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
