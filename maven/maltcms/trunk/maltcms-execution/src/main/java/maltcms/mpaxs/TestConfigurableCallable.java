/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package maltcms.mpaxs;

import java.io.File;
import net.sf.maltcms.execution.api.concurrent.ConfigurableRunnable;
import net.sf.maltcms.execution.api.job.Progress;

/**
 *
 * @author nilshoffmann
 */
public class TestConfigurableCallable implements ConfigurableRunnable<Long>{

    private Progress progress = new Progress();
    private long finished = -1l;

    @Override
    public Long get() {
        return finished;
    }

    @Override
    public void configure(File pathToConfig) {

    }

    @Override
    public Progress getProgress() {
        return progress;
    }

    @Override
    public void run() {
        progress.setMessage("Starting computation");
        for(int i = 0;i<10; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                //Logger.getLogger(TestConfigurableCallable.class.getName()).log(Level.SEVERE, null, ex);
            }
            progress.setProgress(i*10);
        }
        finished = System.nanoTime();
    }

}
