/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package maltcms.mpaxs;

import java.io.File;
import java.net.URI;
import net.sf.maltcms.execution.api.concurrent.ConfigurableRunnable;
import net.sf.maltcms.execution.api.job.Progress;

/**
 *
 * @author nilshoffmann
 */
public class MaltcmsConfigurableRunnable implements ConfigurableRunnable<URI> {

    @Override
    public URI get() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void configure(File pathToConfig) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Progress getProgress() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
