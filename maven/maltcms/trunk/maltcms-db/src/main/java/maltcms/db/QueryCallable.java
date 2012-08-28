/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package maltcms.db;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Callable;


import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Predicate;
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
