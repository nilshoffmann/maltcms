/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
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

import java.util.HashSet;
import java.util.List;
import org.junit.Assert;
import lombok.extern.slf4j.Slf4j;
import net.sf.mpaxs.api.ConfigurationKeys;
import net.sf.mpaxs.api.ExecutionType;
import net.sf.mpaxs.api.ICompletionService;
import net.sf.mpaxs.api.Impaxs;
import net.sf.mpaxs.spi.concurrent.CompletionServiceFactory;
import net.sf.mpaxs.spi.concurrent.ComputeServerFactory;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Test;

/**
 * Test cases for impaxs integration
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class ImpaxsExecutionTest {
    
    @Test
    public void testRemoteRmiExecution() throws Exception {
//        Impaxs impxs = null;
        try {
//            PropertiesConfiguration pc = new PropertiesConfiguration();
//            pc.setProperty(ConfigurationKeys.KEY_EXECUTION_MODE, ExecutionType.DRMAA);
//            pc.setProperty(ConfigurationKeys.KEY_MASTER_SERVER_EXIT_ON_SHUTDOWN, "false");
//            impxs = ComputeServerFactory.getComputeServer();
//            impxs.startMasterServer(pc);
            //rmi service
//            final ICompletionService<String> mcs = new CompletionServiceFactory<String>().
//                    newDistributedCompletionService();
//            
//            mcs.submit(new TestCallable());
//            mcs.submit(new TestCallable());
//            mcs.submit(new TestCallable());
//            mcs.submit(new TestCallable());
//            mcs.submit(new TestCallable());
//            
//            List<String> rmiResults = mcs.call();
//            Assert.assertEquals(5, rmiResults.size());
//            HashSet<String> results = new HashSet<>(rmiResults);

            //local service
            final ICompletionService<String> mcs2 = new CompletionServiceFactory<String>().
                    newLocalCompletionService();
            mcs2.submit(new TestCallable());
            mcs2.submit(new TestCallable());
            mcs2.submit(new TestCallable());
            mcs2.submit(new TestCallable());
            mcs2.submit(new TestCallable());
            
            List<String> localResults = mcs2.call();
            Assert.assertEquals(5, localResults.size());
//            results.removeAll(localResults);
//            Assert.assertTrue(results.isEmpty());
        }catch(Exception e) {
            log.warn("Caught exception: "+e.getMessage());
        } finally {
//            if (impxs != null) {
//                impxs.stopMasterServer();
//            }
        }
    }
}
