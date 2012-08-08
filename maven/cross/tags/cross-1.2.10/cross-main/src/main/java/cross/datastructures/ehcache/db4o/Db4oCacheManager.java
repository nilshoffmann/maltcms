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
package cross.datastructures.ehcache.db4o;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ext.DatabaseClosedException;
import cross.datastructures.ehcache.ICacheDelegate;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cache manager for db4o-based caching. Registered as shutdown hook.
 * 
 * Will automatically close any remaining connections on application exit
 * and delete all cache files.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class Db4oCacheManager extends Thread {

    private final File basedir;
    
    private Map<File,Db4oCacheDelegate> caches = Collections.synchronizedMap(new LinkedHashMap<File,Db4oCacheDelegate>());
    
    public Db4oCacheManager(File basedir) {
        super();
        Runtime.getRuntime().addShutdownHook(this);
        this.basedir = basedir;
    }
    
    public <K,V> ICacheDelegate<K,V> getCache(String name) {
        return getCache(name, null);
    }
    
    public <K,V> ICacheDelegate<K,V> getCache(String name, Comparator<K> comparator) {
        File cachePath = new File(basedir,name);
        Db4oCacheDelegate<K,V> delegate = caches.get(cachePath);
        if(delegate==null) {
            ObjectContainer oc = Db4oEmbedded.openFile(cachePath.getAbsolutePath());
            delegate = new Db4oCacheDelegate<K, V>(name, oc, comparator);
            caches.put(cachePath, delegate);
        }
        return delegate;
    }
    
    public <K,V> void remove(ICacheDelegate<K,V> delegate) {
        File cachePath = new File(basedir,delegate.getName());
        caches.get(cachePath).close();
        caches.remove(cachePath);
    }
    
    @Override
    public void run() {
        for(File cachePath:caches.keySet()) {
            try {
                caches.get(cachePath).close();
            } catch(DatabaseClosedException dce) {
                
            }
            File fileToDelete = cachePath;
            fileToDelete.deleteOnExit();
        }
        caches.clear();
    }
    
}
