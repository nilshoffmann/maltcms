package maltcms.db;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import maltcms.db.predicates.metabolite.IAggregatePredicateFactory;

import com.db4o.ObjectSet;
import com.db4o.query.Predicate;

public class QueryDB<T>{

	protected static ExecutorService es = Executors.newFixedThreadPool(Math.min(1,Runtime.getRuntime().availableProcessors()-1));
	
	protected String dblocation = null;
	
	protected ObjectSet<T> qres = null;
	
	protected Predicate<T> ap = null;
	
	protected Runnable r = null;
	
	public QueryDB(final String dblocation) {
		this.dblocation = dblocation;
	}
	
	public QueryDB(String dblocation, Predicate<T> p) {
		this(dblocation);
		this.ap = p;
	}
	
	public QueryDB(String dblocation, String[] args, IAggregatePredicateFactory<T> iapf) {
		this(dblocation);
		this.ap = iapf.digestCommandLine(args);
	}
	
	/**
	 * Returns a QueryCallable built according to the arguments supplied to the 
	 * constructor of this object.
	 * @return
	 */
	public QueryCallable<T> getCallable() {
		if(this.ap == null) {
			return new QueryCallable<T>(this.dblocation,getDefaultPredicate());
		}
		return new QueryCallable<T>(this.dblocation,this.ap);
	}
	
	public Predicate<T> getDefaultPredicate() {
		Predicate<T> matchAll = new Predicate<T>() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = -8580415202887162014L;

			@Override
			public boolean match(T arg0) {
				return true;
			}
		
		};
		return matchAll;
	}
	
	/**
	 * Use if you want to use a different set of Predicates than defined at construction time.
	 * Supplied Predicates will be passed on to QueryCallable, but will not be used as new defaults.
	 * @param c1
	 * @param c2
	 * @return
	 */
	public QueryCallable<T> getCallable(Predicate<T> ap) {
		return new QueryCallable<T>(this.dblocation,ap);
	}
	
	/**
	 * Submits the QueryCallable qf to the ExecutorService es and 
	 * returns a Future to retrieve results.
	 * @param qc
	 * @return
	 */
	public Future<ObjectSet<T>> invoke(QueryCallable<T> qc) {
		return es.submit(qc);
	}
	
	/**
	 * Submits the QueryCallable constructed according to current
	 * settings and returns Future to retrieve results.
	 * @return
	 */
	public Future<ObjectSet<T>> invoke() {
		return es.submit(getCallable());
	}	
	
	/**
	 * Allows to set the predicate used for matching by this QueryDB object.
	 * @param p
	 */
	public void setPredicate(Predicate<T> p) {
		this.ap = p;
	}
	
}
