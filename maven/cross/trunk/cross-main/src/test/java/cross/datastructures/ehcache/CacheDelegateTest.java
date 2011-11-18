/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cross.datastructures.ehcache;

import java.util.Random;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author nilshoffmann
 */
public class CacheDelegateTest {

    private int narrays = 2000;
    private Integer[] indices;
    private int maxRepetitions = 20;
    private long seed = 1920712093679568761L;
    private Random r;

    public CacheDelegateTest() {
    }

    @Before
    public void setUp() {
    }

    private ICacheDelegate<Integer, double[]> createCache(String name) {
        System.out.println("Setting up cache!");
        int arraySize = 1000;
        CacheFactory<Integer, double[]> cf = new CacheFactory<Integer, double[]>();
        ICacheDelegate<Integer, double[]> delegate = cf.createDefaultCache(
                name);
        cf.getCacheFor(name).setSampledStatisticsEnabled(true);
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
}
