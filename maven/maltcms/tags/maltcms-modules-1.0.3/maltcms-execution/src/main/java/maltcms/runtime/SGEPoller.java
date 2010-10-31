/**
 * 
 */
package maltcms.runtime;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.Session;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class SGEPoller implements RunnableFuture<Integer> {

	private final Session s;

	private final String jobID;

	private int lastState = Session.UNDETERMINED;

	private boolean isCancelled = false;

	private boolean isDone = false;

	public SGEPoller(final Session s1, final String jobID1) {
		this.s = s1;
		this.jobID = jobID1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#cancel(boolean)
	 */
	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {
		try {
			this.s.control(this.jobID, Session.TERMINATE);
			this.isCancelled = true;
			return true;
		} catch (final DrmaaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#get()
	 */
	@Override
	public Integer get() throws InterruptedException, ExecutionException {
		return this.lastState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public Integer get(final long timeout, final TimeUnit unit)
	        throws InterruptedException, ExecutionException, TimeoutException {
		return this.lastState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#isCancelled()
	 */
	@Override
	public boolean isCancelled() {
		return this.isCancelled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#isDone()
	 */
	@Override
	public boolean isDone() {
		return this.isDone;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.RunnableFuture#run()
	 */
	@Override
	public void run() {
		try {
			this.lastState = this.s.getJobProgramStatus(this.jobID);
			this.isDone = true;
		} catch (final DrmaaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
