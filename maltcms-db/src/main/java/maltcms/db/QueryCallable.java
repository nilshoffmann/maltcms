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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QueryCallable<T> implements Callable<ObjectSet<T>> {

    protected Predicate<T> llap;
    protected ObjectSet<T> los;
    protected String lloc;
    protected boolean done = false;
    protected ObjectContainer oc = null;

    public QueryCallable(String location, Predicate<T> ap) {
        lloc = location;
        this.log.debug("QueryCallable for {}", location);
        if (ap == null) {
            throw new IllegalArgumentException("Predicate must not be null!");
        } else {
            llap = ap;
        }
    }

    public ObjectSet<T> call() throws Exception {
        if (new File(this.lloc).exists()) {
            this.log.debug("Opening DB locally as file!");
            this.oc = Db4o.openFile(this.lloc);
            return oc.query(llap);// oc.query(llap);
        } else {
            URL url = new URL(this.lloc);
            // System.out.println(url.getAuthority());
            // System.out.println(url.getHost());
            // System.out.println(url.getFile());
            // System.out.println(url.getDefaultPort());
            // System.out.println(url.getPath());
            // System.out.println(url.getPort());
            // System.out.println(url.getProtocol());
            // System.out.println(url.getQuery());
            // System.out.println(url.getRef());
            // System.out.println(url.getUserInfo());
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

    public void terminate() {
        if (this.oc != null) {
            this.log.debug("Closing DB connection!");
            this.oc.close();
        }
    }
}
