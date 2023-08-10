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

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Predicate;
import java.io.File;
import java.net.URL;
import java.util.concurrent.Callable;
import org.slf4j.LoggerFactory;



/**
 * <p>QueryCallable class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class QueryCallable<T> implements Callable<ObjectSet<T>> {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(QueryCallable.class);

    protected Predicate<T> llap;
    protected ObjectSet<T> los;
    protected String lloc;
    protected boolean done = false;
    protected ObjectContainer oc = null;

    /**
     * <p>Constructor for QueryCallable.</p>
     *
     * @param location a {@link java.lang.String} object.
     * @param ap a {@link com.db4o.query.Predicate} object.
     */
    public QueryCallable(String location, Predicate<T> ap) {
        lloc = location;
        this.log.debug("QueryCallable for {}", location);
        if (ap == null) {
            throw new IllegalArgumentException("Predicate must not be null!");
        } else {
            llap = ap;
        }
    }

    /** {@inheritDoc} */
    @Override
    public ObjectSet<T> call() throws Exception {
        if (new File(this.lloc).exists()) {
            this.log.debug("Opening DB locally as file!");
            this.oc = Db4o.openFile(this.lloc);
            return oc.query(llap);// oc.query(llap);
        } else {
            URL url = new URL(this.lloc);
            // log.info(url.getAuthority());
            // log.info(url.getHost());
            // log.info(url.getFile());
            // log.info(url.getDefaultPort());
            // log.info(url.getPath());
            // log.info(url.getPort());
            // log.info(url.getProtocol());
            // log.info(url.getQuery());
            // log.info(url.getRef());
            // log.info(url.getUserInfo());
            this.log.debug("Opening DB via Client!");
            this.oc = Db4o.openClient(url.getHost(), url.getPort(), url.
                    getUserInfo(), "default");
            return oc.query(llap);// oc.query(llap);
        }
        // ObjectContainer oc = Db4o.openFile(lloc);
        // try {
        // } finally {
        // oc.close();
        // }
    }

    /**
     * <p>terminate.</p>
     */
    public void terminate() {
        if (this.oc != null) {
            this.log.debug("Closing DB connection!");
            this.oc.close();
        }
    }
}
