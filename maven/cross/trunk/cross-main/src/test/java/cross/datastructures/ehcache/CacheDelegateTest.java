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

import cross.cache.CacheType;
import cross.datastructures.cache.VariableFragmentArrayCache;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.Fragments;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.test.SetupLogging;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
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
@Slf4j
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
	@Rule
	public SetupLogging logging = new SetupLogging();

	/**
	 *
	 */
	@Before
	public void setUp() {
		logging.setLogLevel("log4j.category.net.sf.ehcache", "INFO");

	}

	/**
	 *
	 */
	@Test
	public void cachedVariableFragment() throws IOException {
		logging.setLogLevel("log4j.category.net.sf.ehcache", "DEBUG");

		Fragments.setDefaultFragmentCacheType(CacheType.EHCACHE);
		FileFragment ff = new FileFragment(tf.newFolder("cachedVariableFragmentTest"), "testfrag.cdf");
		Ehcache cache = CacheManager.getInstance().getEhcache(ff.getCache().getName());
		CacheConfiguration config = cache.getCacheConfiguration();
		log.info("Storing cache on disk at {}", config.getDiskStorePath());
		log.info("Using disk store size of {}", cache.getDiskStoreSize());
		log.info("Overflowing to disk: {}", config.isOverflowToDisk());
		for (int j = 0; j < 100; j++) {
			VariableFragment vf1 = new VariableFragment(ff, "a" + j);
			vf1.setArray(new ArrayDouble.D2(10, 39));
			VariableFragment vfIndex = new VariableFragment(ff, "index" + j);
			vfIndex.setArray(new ArrayInt.D1(20));
			VariableFragment vf2 = new VariableFragment(ff, "b" + j, vfIndex);
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
			log.info("In memory: {}; On disk: {}", cache.getSize(), cache.getDiskStoreSize());
		}
		for (IVariableFragment var : ff) {
			Assert.assertNotNull(var.getArray());
			log.info(var.getName() + ": " + var.getArray());
		}
		logging.setLogLevel("log4j.category.net.sf.ehcache", "INFO");
	}

	/**
	 *
	 */
	@Test
	public void customCachedVariableFragment() throws IOException {
		logging.setLogLevel("log4j.category.net.sf.ehcache", "DEBUG");
		Fragments.setDefaultFragmentCacheType(CacheType.EHCACHE);
		FileFragment ff = new FileFragment(tf.newFolder("cachedVariableFragmentTest"), "testfrag.cdf");
		CacheManager manager = CacheManager.create();
		CacheConfiguration config = new CacheConfiguration(ff.getName() + "-variable-fragment-cache", 100);
		config.setDiskStorePath(tf.newFolder("cacheLocation").getAbsolutePath());
		config.setMaxElementsInMemory(100);
		config.setMaxElementsOnDisk(1000);
		config.setOverflowToDisk(true);
		config.setDiskSpoolBufferSizeMB(10);
		config.setEternal(false);
		Ehcache cache = new Cache(config);
		manager.addCache(cache);

		log.info("Storing cache on disk at {}", config.getDiskStorePath());
		log.info("Using disk store size of {}", cache.getDiskStoreSize());
		log.info("Overflowing to disk: {}", config.isOverflowToDisk());
		ff.setCache(new VariableFragmentArrayCache(cache));
		for (int j = 0; j < 100; j++) {
			VariableFragment vf1 = new VariableFragment(ff, "a" + j);
			vf1.setArray(new ArrayDouble.D2(10, 39));
			VariableFragment vfIndex = new VariableFragment(ff, "index" + j);
			vfIndex.setArray(new ArrayInt.D1(20));
			VariableFragment vf2 = new VariableFragment(ff, "b" + j, vfIndex);
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
			log.info("In memory: {}; On disk: {}", cache.getSize(), cache.getDiskStoreSize());
		}
		for (IVariableFragment var : ff) {
			Assert.assertNotNull(var.getArray());
			log.info(var.getName() + ": " + var.getArray());
		}
		logging.setLogLevel("log4j.category.net.sf.ehcache", "INFO");
	}
}
