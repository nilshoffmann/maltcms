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
package maltcms.io.hdf5.mz5;

import cross.Factory;
import cross.cache.CacheType;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.Fragments;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.ImmutableFileFragment;
import cross.test.LogMethodName;
import cross.test.SetupLogging;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;
import maltcms.io.andims.NetcdfDataSource;
import maltcms.test.ExtractClassPathFiles;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class Mz5Test {

    @Rule
    public SetupLogging sl = new SetupLogging();
    @Rule
    public LogMethodName lmn = new LogMethodName();
    @Rule
    public TemporaryFolder tf = new TemporaryFolder();
    @Rule
    public ExtractClassPathFiles ecpf = new ExtractClassPathFiles(tf, "/mz5/small_raw.mz5.gz", "/mz5/tiny_pwiz.mz5.gz");

    MZ5DataSource getDataSource() {
        return new MZ5DataSource();
    }

    /**
     *
     */
    @Before
    public void setUp() {
        Factory.getInstance().getDataSourceFactory().setDataSources(Arrays.asList(NetcdfDataSource.class.getCanonicalName(), MZ5DataSource.class.getCanonicalName()));
        Fragments.setDefaultFragmentCacheType(CacheType.NONE);
        sl.setLogLevel(Mz5Test.class, "INFO");
    }

    /**
     * Test of canRead method, of class NetcdfDataSource.
     */
    @Test
    public void testCanRead() {
        for (File f : ecpf.getFiles()) {
            Assert.assertEquals(1, getDataSource().canRead(new FileFragment(f)));
            Assert.assertEquals(1, getDataSource().
                canRead(new ImmutableFileFragment(new FileFragment(f.toURI()))));
        }
    }

    /**
     * Test of readAll method, of class NetcdfDataSource.
     */
    @Test
    public void testReadAll() throws Exception {
//		sl.setLogLevel("cross.datastructures.fragments",
//				"DEBUG");
//		sl.setLogLevel("maltcms.io.andims", "DEBUG");
//		sl.setLogLevel("maltcms.io.xml.mzML", "DEBUG");
        for (File f : ecpf.getFiles()) {
            List<Array> l = getDataSource().readAll(new FileFragment(f));
            Assert.assertTrue(l.size() > 0);
            try {
                List<Array> l2 = getDataSource().
                    readAll(new ImmutableFileFragment(f));
                Assert.assertEquals(l.size(), l2.size());
            } catch (UnsupportedOperationException uoe) {
                Assert.fail(uoe.getLocalizedMessage());
            }
        }
//		sl.setLogLevel("cross.datastructures.fragments",
//				"INFO");
//		sl.setLogLevel("maltcms.io.andims", "INFO");
//		sl.setLogLevel("maltcms.io.xml.mzML", "INFO");
    }

    /**
     * Test of readAll method, of class NetcdfDataSource.
     */
    @Test
    public void testReadStructureAll() throws Exception {
//		sl.setLogLevel("cross.datastructures.fragments",
//				"DEBUG");
//		sl.setLogLevel("maltcms.io.andims", "DEBUG");
//		sl.setLogLevel("maltcms.io.xml.mzML", "DEBUG");
        for (File f : ecpf.getFiles()) {
            IFileFragment allFragment = new FileFragment(f);
            List<IVariableFragment> l = getDataSource().readStructure(allFragment);
            Assert.assertTrue(l.size() > 0);
            FileFragment testFragment = new FileFragment(f);
            for (IVariableFragment ivf : l) {
                try {
                    IVariableFragment ivf2 = testFragment.addChild(ivf.getName());
                    ivf2 = getDataSource().readStructure(ivf2);
                    log.info("Checking variable {}", ivf2.getName());
                    Assert.assertEquals(ivf.getName(), ivf2.getName());
                    Assert.assertTrue(Arrays.deepEquals(ivf.getDimensions(), ivf2.getDimensions()));
                } catch (UnsupportedOperationException uoe) {
                    Assert.fail(uoe.getLocalizedMessage());
                }
            }
            log.info("Fragment: {}", FileFragment.printFragment(allFragment));
        }
        sl.setLogLevel("cross.datastructures.fragments",
            "INFO");
        sl.setLogLevel("maltcms.io.andims", "INFO");
        sl.setLogLevel("maltcms.io.xml.mzML", "INFO");
    }

    /**
     * Test of readIndexed method, of class NetcdfDataSource.
     */
    @Test
    public void testReadIndexed() throws Exception {
//        FileFragment.clearFragments();
        sl.setLogLevel("cross.datastructures.fragments",
            "DEBUG");
        sl.setLogLevel("maltcms.io.andims", "DEBUG");
        sl.setLogLevel("maltcms.io.hdf5.mz5", "DEBUG");
        for (File f : ecpf.getFiles()) {
            IFileFragment ff = new FileFragment(f);
            IVariableFragment si = ff.getChild("SpectrumIndex");
            Assert.assertNotNull(si.getArray());
            IVariableFragment iv = ff.getChild("SpectrumIntensity");
            iv.setIndex(si);
            List<Array> l = getDataSource().readIndexed(iv);
            Assert.assertTrue(l.size() > 0);
            int shapeSum = 0;
            for (Array a : l) {
                log.info("Shape: {}", a.getShape());
                shapeSum += a.getShape()[0];
            }
            Assert.assertEquals(iv.getDimensions()[0].getLength(), shapeSum);
            ff = new ImmutableFileFragment(f);
            si = ff.getChild("SpectrumIndex");
            Assert.assertNotNull(si.getArray());
            iv = ff.getChild("SpectrumIntensity");
            iv.setIndex(si);
            l = getDataSource().readIndexed(iv);
            Assert.assertTrue(l.size() > 0);
            iv.setIndex(null);
            ff.removeChild(si);
            ff.removeChild(iv);
//            FileFragment.clearFragments();
            ff = new FileFragment(f);
            si = ff.getChild("SpectrumIndex");
            Assert.assertNotNull(si.getArray());

        }
        sl.setLogLevel("cross.datastructures.fragments",
            "INFO");
        sl.setLogLevel("maltcms.io.andims", "INFO");
        sl.setLogLevel("maltcms.io.hdf5.mz5", "INFO");
    }
}
