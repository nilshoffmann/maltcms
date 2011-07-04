/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package maltcms.mpaxs;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.sf.maltcms.execution.api.ExecutionFactory;
import net.sf.maltcms.execution.api.Impaxs;
import net.sf.maltcms.execution.api.event.IJobEventListener;
import net.sf.maltcms.execution.api.job.IJob;

/**
 *
 * @author nilshoffmann
 */
public class JobExecutor implements ExecutorService, IJobEventListener {

    private Impaxs imp = null;
    private boolean isShutdown = false;
    private ConcurrentLinkedQueue<IJob> pendingJobs = new ConcurrentLinkedQueue<IJob>();

    public JobExecutor() {
        imp = ExecutionFactory.getDefaultComputeServer();
        imp.startMasterServer();
        imp.addJobEventListener(this);
    }

    @Override
    public void shutdown() {
        imp.stopMasterServer();
    }

    @Override
    public List<Runnable> shutdownNow() {
        List<Runnable> l = new LinkedList<Runnable>();
        return l;
    }

    @Override
    public boolean isShutdown() {
        return isShutdown;
    }

    @Override
    public boolean isTerminated() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean awaitTermination(long l, TimeUnit tu) throws InterruptedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> Future<T> submit(Callable<T> clbl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> Future<T> submit(Runnable r, T t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<?> submit(Runnable r) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> clctn) throws InterruptedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> clctn, long l, TimeUnit tu) throws InterruptedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> clctn) throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> clctn, long l, TimeUnit tu) throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void execute(Runnable r) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void jobChanged(IJob job) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
