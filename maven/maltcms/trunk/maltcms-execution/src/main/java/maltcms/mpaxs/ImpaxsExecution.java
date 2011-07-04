/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package maltcms.mpaxs;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import net.sf.maltcms.execution.api.concurrent.ConfigurableCallable;
import net.sf.maltcms.execution.api.concurrent.ConfigurableRunnable;
import net.sf.maltcms.execution.api.concurrent.DefaultCallable;
import net.sf.maltcms.execution.api.concurrent.DefaultRunnable;
import net.sf.maltcms.execution.api.ExecutionFactory;
import net.sf.maltcms.execution.api.computeHost.IRemoteHost;
import net.sf.maltcms.execution.api.job.IJob;
import net.sf.maltcms.execution.api.event.IJobEventListener;
import net.sf.maltcms.execution.api.Impaxs;
import net.sf.maltcms.execution.api.job.Job;
import net.sf.maltcms.execution.api.job.Status;

/**
 *
 * @author nilshoffmann
 */
public class ImpaxsExecution<V> implements IJobEventListener {

    private Impaxs imp = null;
    private Set<IJob> finishedJobs = new LinkedHashSet<IJob>();
    private Set<IJob> submittedJobs = new LinkedHashSet<IJob>();
    private boolean isShutdown = false;
    private int numberOfSubmittedJobs = 0;
    private int numberOfFinishedJobs = 0;

    public ImpaxsExecution() {
        final JFrame jf = new JFrame("MasterServer");
        imp = ExecutionFactory.getDefaultComputeServer();
        imp.startMasterServer(jf);
        imp.addJobEventListener(this);
        System.out.println("AuthToken is: "+imp.getAuthenticationToken());
        Runnable r = new Runnable() {

            @Override
            public void run() {
                jf.setVisible(true);
            }
        };
        SwingUtilities.invokeLater(r);
    }

    public static void main(String[] args) {
//        Impaxs imp = null;
        final ImpaxsExecution imp = new ImpaxsExecution();

        ExecutorService es = Executors.newFixedThreadPool(1);

        Runnable submitter = new Runnable() {

            @Override
            public void run() {
                ConfigurableRunnable cr = new TestConfigurableCallable();
                ConfigurableCallable cc = new ConfigurableCallable(cr);
                IJob job = new Job(cc);
                imp.submitJob(job);
//                imp.submitJob(new Job(cc));
//                imp.submitJob(new Job(cc));
                IJob job2 = new Job(new DefaultRunnable(new TestRunnable(), Boolean.TRUE));
                imp.submitJob(job2);
                IJob job3 = new Job(new DefaultCallable(new TestCallable()));
                imp.submitJob(job3);
            }
        };
        es.submit(submitter);

//        Runnable host = new Runnable() {
//
//            @Override
//            public void run() {
//                IRemoteHost host = ExecutionFactory.getDefaultComputeHost();
//                host.setAuthenticationToken(imp.getImpaxs().getAuthenticationToken());
//                host.startComputeHost();
//            }
//        };
//        es.submit(host);
        es.shutdown();

    }

    public Impaxs getImpaxs() {
        return imp;
    }

    public void submitJob(IJob job) {
        imp.submitJob(job);
        submittedJobs.add(job);
        numberOfSubmittedJobs++;
    }

    @Override
    public void jobChanged(IJob job) {
        if (job.getStatus().equals(Status.DONE)) {
            numberOfFinishedJobs++;
            try {
                //            finishedJobs.add(job);
                //            submittedJobs.remove(job);
                System.out.println("Result of job " + job.getId() + ": " + job.getClassToExecute().get());
            } catch (InterruptedException ex) {
                Logger.getLogger(ImpaxsExecution.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(ImpaxsExecution.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (numberOfSubmittedJobs == numberOfFinishedJobs && numberOfSubmittedJobs > 0) {
            System.out.println("Finished execution of jobs!");
            imp.stopMasterServer();
            //System.exit(0);
        }
    }
}
