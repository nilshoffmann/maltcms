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
package cross.datastructures.ehcache;

import cross.datastructures.cache.ICacheDelegate;
import cross.datastructures.cache.CacheFactory;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.VariableFragment;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import junit.framework.Assert;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;

/**
 *
 * @author Nils Hoffmann
 */
public class CacheDelegateTest {

    private int narrays = 50;
    private Integer[] indices;
    private int maxRepetitions = 20;
    private long seed = 1920712093679568761L;
    private Random r;
    /**
     *
     */
    @Rule
    public TemporaryFolder tf = new TemporaryFolder();

    /**
     *
     */
    public CacheDelegateTest() {
    }

    /**
     *
     */
    @Before
    public void setUp() {
    }

    private ICacheDelegate<Integer, Array> createDb4oCache(String name, boolean sorted) {
        System.out.println("Setting up cache!");
        int arraySize = 1000;
        ICacheDelegate<Integer, Array> delegate;
        if (sorted) {
            delegate = CacheFactory.createDb4oDefaultCache(tf.newFolder("db4ocache"),
                    name);
        } else {
            delegate = CacheFactory.createDb4oSortedCache(tf.newFolder("db4ocache"), name, new Comparator<Integer>() {
                @Override
                public int compare(Integer t, Integer t1) {
                    return t.intValue() - t1.intValue();
                }
            });
        }
        //cf.getCacheFor(name).setSampledStatisticsEnabled(true);
        indices = new Integer[narrays];
        for (int i = 0; i < narrays; i++) {
            double[] a = new double[arraySize];
            for (int j = 0; j < arraySize; j++) {
                a[j] = Math.random() * 1023890213;
            }
            indices[i] = Integer.valueOf(i);
            delegate.put(indices[i], Array.factory(a));

        }
        System.out.println("Finished creating cache!");
        return delegate;
    }

    private ICacheDelegate<Integer, double[]> createCache(String name) {
        System.out.println("Setting up cache!");
        int arraySize = 1000;
        ICacheDelegate<Integer, double[]> delegate = CacheFactory.createDefaultCache(
                name);
        CacheFactory.getCacheFor(name).setSampledStatisticsEnabled(true);
        indices = new Integer[narrays];
        for (int i = 0; i < narrays; i++) {
            double[] a = new double[arraySize];
            for (int j = 0; j < arraySize; j++) {
                a[j] = Math.random() * 1023890213;
            }
            indices[i] = Integer.valueOf(i);
            delegate.put(indices[i], a);

        }
        System.out.println("Finished creating cache!");
        return delegate;
    }

    /**
     *
     */
    @Test
    public void cachedVariableFragment() {
        FileFragment ff = new FileFragment();
        VariableFragment vf1 = new VariableFragment(ff, "a");
        vf1.setArray(new ArrayDouble.D2(10, 39));
        VariableFragment vfIndex = new VariableFragment(ff, "index");
        vfIndex.setArray(new ArrayInt.D1(20));
        VariableFragment vf2 = new VariableFragment(ff, "b", vfIndex);
        List<Array> l = new ArrayList<Array>();
        Array indexArray = vfIndex.getArray();
        int offset = 0;
        for (int i = 0; i < 20; i++) {
            l.add(new ArrayDouble.D1(10));
            indexArray.setInt(i, offset);
            offset += 10;
        }
        vf2.setIndexedArray(l);
        Assert.assertNotNull(vf1.getArray());
        Assert.assertNotNull(vf2.getIndexedArray());
        Assert.assertEquals(20, vf2.getIndexedArray().size());
        Assert.assertNotNull(vfIndex.getArray());
    }

    /**
     *
     */
    @Test
    public void cachedSequentialReadLru() {
        ICacheDelegate<Integer, double[]> delegate = createCache(
                "cachedSequentialRead1");
        CacheFactory.getCacheFor(delegate.getName()).getCacheConfiguration().
                setMemoryStoreEvictionPolicyFromObject(
                MemoryStoreEvictionPolicy.LRU);
        //simulate sequential access
        for (int j = 0; j < maxRepetitions; j++) {
            for (int i = 0; i < narrays; i++) {
                delegate.get(indices[i]);
            }
        }

        System.out.println("Statistics for sequential access: "
                + CacheFactory.getCacheFor(delegate.getName()).getStatistics());
        CacheFactory.getCacheFor(delegate.getName()).getCacheManager().
                removeCache(delegate.getName());
    }

    /**
     *
     */
    @Test
    public void cachedSequentialReadLfu() {
        ICacheDelegate<Integer, double[]> delegate = createCache(
                "cachedSequentialRead2");
        CacheFactory.getCacheFor(delegate.getName()).getCacheConfiguration().
                setMemoryStoreEvictionPolicyFromObject(
                MemoryStoreEvictionPolicy.LFU);
        //simulate sequential access
        for (int j = 0; j < maxRepetitions; j++) {
            for (int i = 0; i < narrays; i++) {
                delegate.get(indices[i]);
            }
        }

        System.out.println("Statistics for sequential access lfu: "
                + CacheFactory.getCacheFor(delegate.getName()).getStatistics());
        CacheFactory.getCacheFor(delegate.getName()).getCacheManager().
                removeCache(delegate.getName());
    }

    /**
     *
     */
    @Test
    public void cachedSequentialReadFifo() {
        ICacheDelegate<Integer, double[]> delegate = createCache(
                "cachedSequentialRead3");
        CacheFactory.getCacheFor(delegate.getName()).getCacheConfiguration().
                setMemoryStoreEvictionPolicyFromObject(
                MemoryStoreEvictionPolicy.FIFO);
        //simulate sequential access
        for (int j = 0; j < maxRepetitions; j++) {
            for (int i = 0; i < narrays; i++) {
                delegate.get(indices[i]);
            }
        }

        System.out.println("Statistics for sequential access fifo: "
                + CacheFactory.getCacheFor(delegate.getName()).getStatistics());
        CacheFactory.getCacheFor(delegate.getName()).getCacheManager().
                removeCache(delegate.getName());
    }

    /**
     *
     */
    @Test
    public void cachedRandomReadLru() {
        ICacheDelegate<Integer, double[]> delegate = createCache(
                "cachedRandomRead1");
        CacheFactory.getCacheFor(delegate.getName()).getCacheConfiguration().
                setMemoryStoreEvictionPolicyFromObject(
                MemoryStoreEvictionPolicy.LRU);
        r = new Random(seed);
        //simulate random access
        for (int j = 0; j < maxRepetitions; j++) {
            for (int i = 0; i < narrays; i++) {
                delegate.get(indices[r.nextInt(narrays)]);
            }
        }
        System.out.println("Statistics for random access lru: "
                + CacheFactory.getCacheFor(delegate.getName()).getStatistics());
        CacheFactory.getCacheFor(delegate.getName()).getCacheManager().
                removeCache(delegate.getName());
    }

    /**
     *
     */
    @Test
    public void cachedRandomReadLfu() {
        ICacheDelegate<Integer, double[]> delegate = createCache(
                "cachedRandomRead2");
        CacheFactory.getCacheFor(delegate.getName()).getCacheConfiguration().
                setMemoryStoreEvictionPolicyFromObject(
                MemoryStoreEvictionPolicy.LFU);
        r = new Random(seed);
        //simulate random access
        for (int j = 0; j < maxRepetitions; j++) {
            for (int i = 0; i < narrays; i++) {
                delegate.get(indices[r.nextInt(narrays)]);
            }
        }
        System.out.println("Statistics for random access lfu: "
                + CacheFactory.getCacheFor(delegate.getName()).getStatistics());
        CacheFactory.getCacheFor(delegate.getName()).getCacheManager().
                removeCache(delegate.getName());
    }

    /**
     *
     */
    @Test
    public void cachedRandomReadFifo() {
        ICacheDelegate<Integer, double[]> delegate = createCache(
                "cachedRandomRead3");
        CacheFactory.getCacheFor(delegate.getName()).getCacheConfiguration().
                setMemoryStoreEvictionPolicyFromObject(
                MemoryStoreEvictionPolicy.FIFO);
        r = new Random(seed);
        //simulate random access
        for (int j = 0; j < maxRepetitions; j++) {
            for (int i = 0; i < narrays; i++) {
                delegate.get(indices[r.nextInt(narrays)]);
            }
        }
        System.out.println("Statistics for random access fifo: "
                + CacheFactory.getCacheFor(delegate.getName()).getStatistics());
        CacheFactory.getCacheFor(delegate.getName()).getCacheManager().
                removeCache(delegate.getName());
    }

    /**
     *
     */
    @Test
    public void db4oSequential() {
        ICacheDelegate<Integer, Array> delegate = createDb4oCache(
                "cachedDb4oSequential", true);
        //simulate sequential access
        // for (int j = 0; j < maxRepetitions; j++) {
        for (int i = 0; i < narrays; i++) {
            delegate.get(i);
        }
        //}
        delegate.close();
        System.out.println("Statistics for db4o sequential access: ");
    }

    /**
     *
     */
    @Test
    public void db4oRandomAccess() {
        ICacheDelegate<Integer, Array> delegate = createDb4oCache(
                "cachedDb4oRandomAccess", false);
        //simulate random access
        r = new Random(seed);
        // for (int j = 0; j < maxRepetitions; j++) {
        for (int i = 0; i < narrays; i++) {
            delegate.get(indices[r.nextInt(narrays)]);
        }
        // }
        delegate.close();
        System.out.println("Statistics for db4o sequential access: ");
    }
}
