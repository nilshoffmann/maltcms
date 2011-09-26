/**
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 *
 * This file is part of Cross/Maltcms.
 *
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 */
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
