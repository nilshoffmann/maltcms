/* 
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.datastructures.cache.db4o;

import com.db4o.Db4oEmbedded;
import com.db4o.EmbeddedObjectContainer;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.ext.DatabaseClosedException;
import com.db4o.io.CachingStorage;
import com.db4o.io.FileStorage;
import com.db4o.io.Storage;
import com.db4o.ta.DeactivatingRollbackStrategy;
import com.db4o.ta.TransparentActivationSupport;
import com.db4o.ta.TransparentPersistenceSupport;
import cross.datastructures.cache.ICacheDelegate;
import cross.exception.ConstraintViolationException;
import java.io.File;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache manager for db4o-based caching. Registered as shutdown hook.
 *
 * Will automatically close any remaining connections on application exit and
 * deletes all cache files.
 *
 * @author Nils Hoffmann
 */
public class Db4oCacheManager extends Thread {

    private final File basedir;
    private final boolean deleteCachesOnExit;
    private Map<File, Db4oCacheDelegate> caches = new ConcurrentHashMap<File, Db4oCacheDelegate>();

    private Db4oCacheManager(File basedir, boolean deleteCachesOnExit) {
        super();
        Runtime.getRuntime().addShutdownHook(this);
        this.basedir = basedir;
        this.deleteCachesOnExit = deleteCachesOnExit;
    }
    private static final Map<File, Db4oCacheManager> managerMap = new ConcurrentHashMap<File, Db4oCacheManager>();

    public static Db4oCacheManager getInstance(File basedir) {
        if (!managerMap.containsKey(basedir)) {
            managerMap.put(basedir, new Db4oCacheManager(basedir, true));
        }
        return managerMap.get(basedir);
    }

    public static Db4oCacheManager getInstance(File basedir, boolean deleteCachesOnExit) {
        if (!managerMap.containsKey(basedir)) {
            managerMap.put(basedir, new Db4oCacheManager(basedir, deleteCachesOnExit));
        }
        return managerMap.get(basedir);
    }

    public <K, V> ICacheDelegate<K, V> getCache(String name) {
        return getCache(name, null);
    }

    public <K, V> ICacheDelegate<K, V> getCache(String name, Comparator<K> comparator) {
        File cachePath = new File(basedir, name);
        Db4oCacheDelegate<K, V> delegate = caches.get(cachePath);
        if (delegate == null) {
            if (cachePath.exists() && cachePath.isFile()) {
                throw new ConstraintViolationException("Cache location already exists: " + cachePath.getAbsolutePath());
            } else {
                cachePath.getParentFile().mkdirs();
            }
            EmbeddedConfiguration configuration = Db4oEmbedded.newConfiguration();
            configuration.common().add(new TransparentActivationSupport());
            configuration.common().add(new TransparentPersistenceSupport(new DeactivatingRollbackStrategy()));
            Storage fileStorage = new FileStorage();
            int npages = 10;
            int nbytesPerPage = 1024*1024;
            Storage cachingStorage = new CachingStorage(fileStorage,npages,nbytesPerPage);
            configuration.file().storage(cachingStorage);
            EmbeddedObjectContainer oc = Db4oEmbedded.openFile(configuration, cachePath.getAbsolutePath());
            delegate = new Db4oCacheDelegate<K, V>(name, oc, comparator);
            caches.put(cachePath, delegate);
        }
        return delegate;
    }

    public <K, V> void remove(ICacheDelegate<K, V> delegate) {
        File cachePath = new File(basedir, delegate.getName());
        caches.get(cachePath).close();
        cachePath.delete();
        caches.remove(cachePath);
    }

    @Override
    public void run() {
        for (File cachePath : caches.keySet()) {
            try {
                caches.get(cachePath).close();
            } catch (DatabaseClosedException dce) {
            }
            if (deleteCachesOnExit) {
                cachePath.delete();
            }
        }
        caches.clear();
    }
}
