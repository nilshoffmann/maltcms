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
package maltcms.db.connection;

import com.db4o.Db4o;
import com.db4o.ObjectServer;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>DB4oServer class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@Slf4j
public class DB4oServer implements Runnable {

    private String address, port;
    private boolean shutdown = false;

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(final String[] args) {
        Thread t = new Thread(new DB4oServer(args[0], args[1]));
        t.start();
    }

    /**
     * <p>shutdown.</p>
     */
    public void shutdown() {
        this.shutdown = true;
    }

    /**
     * <p>Constructor for DB4oServer.</p>
     *
     * @param address a {@link java.lang.String} object.
     * @param port a {@link java.lang.String} object.
     */
    public DB4oServer(String address, String port) {
        this.address = address;
        this.port = port;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    /** {@inheritDoc} */
    @Override
    public void run() {
        log.info("Starting db4o server for " + this.address + " on port " + this.port);
//    	Configuration cfg = Db4o.newConfiguration();
//    	cfg.clientServer().batchMessages(true);
//    	cfg.clientServer().maxBatchQueueSize(10);
//    	cfg.clientServer().prefetchObjectCount(100);
//    	cfg.clientServer().timeoutClientSocket(1000);
//    	cfg.optimizeNativeQueries(true);
        ObjectServer os = Db4o.openServer(this.address, Integer.parseInt(this.port));
        os.grantAccess("default", "default");
        int clients = 0;
        while (!shutdown) {
            int cl = os.ext().clientCount();
            if (cl != clients) {
                log.info("Active clients: " + cl);
                clients = cl;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
   
                log.warn(e.getLocalizedMessage());
            }
        }
        os.close();
    }
}
