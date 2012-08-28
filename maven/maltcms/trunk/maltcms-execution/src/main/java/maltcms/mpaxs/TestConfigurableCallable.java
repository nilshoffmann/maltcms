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
package maltcms.mpaxs;

import java.io.File;
import net.sf.mpaxs.api.concurrent.ConfigurableRunnable;
import net.sf.mpaxs.api.job.Progress;

/**
 *
 * @author nilshoffmann
 */
public class TestConfigurableCallable implements ConfigurableRunnable<Long> {

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
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                //Logger.getLogger(TestConfigurableCallable.class.getName()).log(Level.SEVERE, null, ex);
            }
            progress.setProgress(i * 10);
        }
        finished = System.nanoTime();
    }
}
