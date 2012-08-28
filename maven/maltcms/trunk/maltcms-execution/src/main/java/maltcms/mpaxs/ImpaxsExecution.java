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
