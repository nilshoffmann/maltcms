/**
 * 
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

	private ExecutorService es = null;//Executors.newFixedThreadPool(this.maxThreads);

	private int maxThreads = 1;

	public enum ExecutorType {
		SINGLETON, CACHED, FIXED
	};

	private ExecutorType type = ExecutorType.FIXED;

	/**
	 * @param nthreads
	 */
	public ExecutorsManager(int nthreads) {
		this.maxThreads = nthreads;
		this.es = Executors.newFixedThreadPool(this.maxThreads);
	}

	/**
	 * 
	 * @param et
	 */
	public ExecutorsManager(ExecutorType et) {
		if (this.type.equals(ExecutorType.SINGLETON)) {
			this.es = Executors.newSingleThreadExecutor();
		} else if (this.type.equals(ExecutorType.CACHED)) {
			this.es = Executors.newCachedThreadPool();
		} else {
			this.es = Executors.newFixedThreadPool(this.maxThreads);
		}
	}

	public boolean awaitTermination(long timeout, TimeUnit unit)
	        throws InterruptedException {
		return es.awaitTermination(timeout, unit);
	}

	public void execute(Runnable command) {
		es.execute(command);
	}

	public <T> List<Future<T>> invokeAll(
	        Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
	        throws InterruptedException {
		return es.invokeAll(tasks, timeout, unit);
	}

	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
	        throws InterruptedException {
		return es.invokeAll(tasks);
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
	        long timeout, TimeUnit unit) throws InterruptedException,
	        ExecutionException, TimeoutException {
		return es.invokeAny(tasks, timeout, unit);
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
	        throws InterruptedException, ExecutionException {
		return es.invokeAny(tasks);
	}

	public boolean isShutdown() {
		return es.isShutdown();
	}

	public boolean isTerminated() {
		return es.isTerminated();
	}

	public void shutdown() {
		es.shutdown();
	}

	public List<Runnable> shutdownNow() {
		return es.shutdownNow();
	}

	public <T> Future<T> submit(Callable<T> task) {
		return es.submit(task);
	}

	public <T> Future<T> submit(Runnable task, T result) {
		return es.submit(task, result);
	}

	public Future<?> submit(Runnable task) {
		return es.submit(task);
	}

}
