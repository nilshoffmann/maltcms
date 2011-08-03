/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package maltcms.mpaxs;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.maltcms.execution.spi.CompletionServiceFactory;
import net.sf.maltcms.execution.spi.ComputeServerFactory;
import net.sf.maltcms.execution.api.ICompletionService;
import net.sf.maltcms.execution.api.ConfigurationKeys;
import net.sf.maltcms.execution.api.ExecutionType;
import net.sf.maltcms.execution.api.Impaxs;
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
                createMpaxsCompletionService();
        
        mcs.submit(new TestCallable());
        mcs.submit(new TestCallable());
        mcs.submit(new TestCallable());
        mcs.submit(new TestCallable());
        mcs.submit(new TestCallable());
        
        try {
            System.out.println("MCS1 Results (RMI execution): "+ mcs.call());
        } catch (Exception ex) {
            Logger.getLogger(ImpaxsExecution.class.getName()).
                    log(Level.SEVERE, null, ex);
        }

        final ICompletionService<String> mcs2 = new CompletionServiceFactory<String>().
                createVMLocalCompletionService();
        mcs2.submit(new TestCallable());
        mcs2.submit(new TestCallable());
        mcs2.submit(new TestCallable());
        mcs2.submit(new TestCallable());
        mcs2.submit(new TestCallable());
        
        try {
            System.out.println("MCS2 Results (local execution): "+mcs2.call());
        } catch (Exception ex) {
            Logger.getLogger(ImpaxsExecution.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
        impxs.stopMasterServer();
        System.exit(0);
    }

}
