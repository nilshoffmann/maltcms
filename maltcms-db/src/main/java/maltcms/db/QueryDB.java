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
package maltcms.db;

import com.db4o.ObjectSet;
import com.db4o.query.Predicate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import maltcms.db.predicates.metabolite.IAggregatePredicateFactory;

public class QueryDB<T> {

    protected static ExecutorService es = Executors.newFixedThreadPool(Math.min(
            1, Runtime.getRuntime().availableProcessors() - 1));
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

    public QueryDB(String dblocation, String[] args,
            IAggregatePredicateFactory<T> iapf) {
        this(dblocation);
        this.ap = iapf.digestCommandLine(args);
    }

    /**
     * Returns a QueryCallable built according to the arguments supplied to the
     * constructor of this object.
     *
     * @return
     */
    public QueryCallable<T> getCallable() {
        if (this.ap == null) {
            return new QueryCallable<>(this.dblocation, getDefaultPredicate());
        }
        return new QueryCallable<>(this.dblocation, this.ap);
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
     * Use if you want to use a different set of Predicates than defined at
     * construction time. Supplied Predicates will be passed on to
     * QueryCallable, but will not be used as new defaults.
     *
     * @param c1
     * @param c2
     * @return
     */
    public QueryCallable<T> getCallable(Predicate<T> ap) {
        return new QueryCallable<>(this.dblocation, ap);
    }

    /**
     * Submits the QueryCallable qf to the ExecutorService es and returns a
     * Future to retrieve results.
     *
     * @param qc
     * @return
     */
    public Future<ObjectSet<T>> invoke(QueryCallable<T> qc) {
        return es.submit(qc);
    }

    /**
     * Submits the QueryCallable constructed according to current settings and
     * returns Future to retrieve results.
     *
     * @return
     */
    public Future<ObjectSet<T>> invoke() {
        return es.submit(getCallable());
    }

    /**
     * Allows to set the predicate used for matching by this QueryDB object.
     *
     * @param p
     */
    public void setPredicate(Predicate<T> p) {
        this.ap = p;
    }
}
