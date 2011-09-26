/*
 * 
 *
 * $Id$
 */

package cross.datastructures.threads;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Proxy for different ExecutorService implementations.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class ExecutorsManager implements ExecutorService {

    public enum ExecutorType {

        SINGLETON, CACHED, FIXED
    }
    private ExecutorService es = null;// Executors.newFixedThreadPool(this.maxThreads);
    private int maxThreads = 1;
    ;

	private final ExecutorType type = ExecutorType.FIXED;

    /**
     * 
     * @param et
     */
    public ExecutorsManager(final ExecutorType et) {
        if (this.type.equals(ExecutorType.SINGLETON)) {
            this.es = Executors.newSingleThreadExecutor();
        } else if (this.type.equals(ExecutorType.CACHED)) {
            this.es = Executors.newCachedThreadPool();
        } else {
            this.es = Executors.newFixedThreadPool(this.maxThreads);
        }
    }

    /**
     * @param nthreads
     */
    public ExecutorsManager(final int nthreads) {
        this.maxThreads = nthreads;
        this.es = Executors.newFixedThreadPool(this.maxThreads);
    }

    public boolean awaitTermination(final long timeout, final TimeUnit unit)
            throws InterruptedException {
        return this.es.awaitTermination(timeout, unit);
    }

    public void execute(final Runnable command) {
        this.es.execute(command);
    }

    public <T> List<Future<T>> invokeAll(
            final Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
        return this.es.invokeAll(tasks);
    }

    public <T> List<Future<T>> invokeAll(
            final Collection<? extends Callable<T>> tasks, final long timeout,
            final TimeUnit unit) throws InterruptedException {
        return this.es.invokeAll(tasks, timeout, unit);
    }

    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        return this.es.invokeAny(tasks);
    }

    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks,
            final long timeout, final TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return this.es.invokeAny(tasks, timeout, unit);
    }

    public boolean isShutdown() {
        return this.es.isShutdown();
    }

    public boolean isTerminated() {
        return this.es.isTerminated();
    }

    public void shutdown() {
        this.es.shutdown();
    }

    public List<Runnable> shutdownNow() {
        return this.es.shutdownNow();
    }

    public <T> Future<T> submit(final Callable<T> task) {
        return this.es.submit(task);
    }

    public Future<?> submit(final Runnable task) {
        return this.es.submit(task);
    }

    public <T> Future<T> submit(final Runnable task, final T result) {
        return this.es.submit(task, result);
    }
}
