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
package maltcms.mpaxs;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.mpaxs.spi.concurrent.CompletionServiceFactory;
import net.sf.mpaxs.spi.concurrent.ComputeServerFactory;
import net.sf.mpaxs.api.ICompletionService;
import net.sf.mpaxs.api.ConfigurationKeys;
import net.sf.mpaxs.api.ExecutionType;
import net.sf.mpaxs.api.Impaxs;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 *
 * @author nilshoffmann
 */
public class ImpaxsExecution {

    public static void main(String[] args) {
        PropertiesConfiguration pc = new PropertiesConfiguration();
        pc.setProperty(ConfigurationKeys.KEY_EXECUTION_MODE, ExecutionType.DRMAA);
        Impaxs impxs = ComputeServerFactory.getComputeServer();
        impxs.startMasterServer(pc);
        final ICompletionService<String> mcs = new CompletionServiceFactory<String>().
                newDistributedCompletionService();

        mcs.submit(new TestCallable());
        mcs.submit(new TestCallable());
        mcs.submit(new TestCallable());
        mcs.submit(new TestCallable());
        mcs.submit(new TestCallable());

        try {
            System.out.println("MCS1 Results (RMI execution): " + mcs.call());
        } catch (Exception ex) {
            Logger.getLogger(ImpaxsExecution.class.getName()).
                    log(Level.SEVERE, null, ex);
        }

        final ICompletionService<String> mcs2 = new CompletionServiceFactory<String>().
                newLocalCompletionService();
        mcs2.submit(new TestCallable());
        mcs2.submit(new TestCallable());
        mcs2.submit(new TestCallable());
        mcs2.submit(new TestCallable());
        mcs2.submit(new TestCallable());

        try {
            System.out.println("MCS2 Results (local execution): " + mcs2.call());
        } catch (Exception ex) {
            Logger.getLogger(ImpaxsExecution.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
        impxs.stopMasterServer();
        System.exit(0);
    }
}
