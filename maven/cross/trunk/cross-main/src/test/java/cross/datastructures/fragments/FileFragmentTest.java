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
package cross.datastructures.fragments;

import cross.Factory;
import cross.cache.CacheFactory;
import cross.cache.CacheType;
import cross.datastructures.tools.FileTools;
import cross.datastructures.tools.FragmentTools;
import cross.exception.ConstraintViolationException;
import cross.exception.ResourceNotAvailableException;
import cross.io.MockDatasource;
import cross.test.SetupLogging;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class FileFragmentTest {

    @Rule
    public MethodRule watchman = new TestWatchman() {
        @Override
        public void starting(FrameworkMethod method) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 80; i++) {
                sb.append("#");
            }
            sb.append("\n").append("# ").append(method.getName()).append("\n");
            for (int i = 0; i < 80; i++) {
                sb.append("#");
            }
            System.out.println(sb.toString());
        }
    };
    @Rule
    public SetupLogging logging = new SetupLogging();

    /**
     *
     */
    @Before
    public void setUp() {
        Factory.getInstance().getDataSourceFactory().setDataSources(Arrays.asList(MockDatasource.class.getCanonicalName()));
        Fragments.setDefaultFragmentCacheType(CacheType.NONE);
    }
    
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //

    @Test
    public void testUriEquality() {

        URI uri1 = URI.create("file://tmp/897123/command1/fileA.cdf");
        URI uri2 = URI.create("file://tmp/897123/command2/fileB.cdf");
        URI uri3 = URI.create("file://tmp/897123/command2/fileB.cdf");

        Assert.assertFalse(uri1.equals(uri2));
        Assert.assertEquals(uri2, uri3);
    }

    @Test
    public void testUriRelativization() {
        System.out.println("File to file");
        URI uri1 = URI.create("file://tmp/897123/command1/fileA.cdf");
        URI uri2 = URI.create("file://tmp/897123/command2/fileB.cdf");
        System.out.println("target: " + uri1);
        System.out.println("base: " + uri2);
        URI relative1 = uri2.relativize(uri1);
        URI relative12 = FileTools.getRelativeUri(uri2, uri1);
        URI relative21 = FileTools.getRelativeUri(uri1, uri2);
        Assert.assertEquals(URI.create("../command1/fileA.cdf"), relative12);
        Assert.assertEquals(URI.create("../command2/fileB.cdf"), relative21);
        Assert.assertEquals(uri1, FileTools.resolveRelativeUri(uri2, relative12));
        Assert.assertEquals(uri2, FileTools.resolveRelativeUri(uri1, relative21));
        System.out.println("relativized1: " + relative1.getPath());
        System.out.println("relativized2: " + uri1.relativize(uri2).getPath());
        System.out.println("resolved: " + uri2.relativize(uri1));
        Assert.assertEquals(uri1, uri2.resolve(relative1));

        System.out.println("Dir to dir");
        URI uri3 = URI.create("file://tmp/897123/command1/");
        URI uri4 = URI.create("file://tmp/897123/command2/");
        System.out.println("target: " + uri3);
        System.out.println("base: " + uri4);
        URI relative2 = uri4.relativize(uri3);
        System.out.println("relativized: " + relative2);
        Assert.assertEquals(uri3, uri4.resolve(relative2));
//      
        System.out.println("Dir to other dir");
        URI uri5 = URI.create("file://tmp/897123/");
        URI uri6 = URI.create("file://tmp/897123/command2/");
        System.out.println("target: " + uri5);
        System.out.println("base: " + uri6);
        URI relative3 = uri6.relativize(uri5);
        System.out.println("relativized: " + relative3);
        Assert.assertEquals(uri5, uri6.resolve(relative3));

        URI uri7 = new File("/tmp/897123/command1/file1.cdf").toURI();
        URI uri8 = new File("/tmp/897123/command2/file2.cdf").toURI();
        System.out.println("target: " + uri7);
        System.out.println("base: " + uri8);
        URI relative4 = uri8.relativize(uri7);
        System.out.println("relativized: " + relative4);
        Assert.assertEquals(uri7, uri8.resolve(relative4));

        URI uri9 = new File("/tmp/897123/command1/file1.cdf").toURI();
        URI uri10 = new File("/tmp/897123/command2").toURI();
        System.out.println("target: " + uri9);
        System.out.println("base: " + uri10);
        URI relative5 = uri10.relativize(uri9);
        System.out.println("relativized: " + relative5);
        Assert.assertEquals(uri9, uri10.resolve(relative5));

        URI relative52 = FileTools.getRelativeUri(uri9, uri10);
        Assert.assertEquals(URI.create("../command2"), relative52);
        Assert.assertEquals(uri10, FileTools.resolveRelativeUri(uri9, relative52));
        
        URI uri11 = new File("/tmp/69138/workflow/command1/file1.cdf").toURI();
        URI uri12 = new File("/tmp/69138/workflow/command1/otherOutput/oof.cdf").toURI();
        URI relative1112 = FileTools.getRelativeUri(uri11, uri12);
        Assert.assertEquals(URI.create("otherOutput/oof.cdf"),relative1112);
    }

    @Test
    public void testSourceFilesRelativization() {
        logging.setLogLevel("log4j.category.cross.datastructures.fragments", "DEBUG");
        FileFragment remote1 = new FileFragment(URI.create("http://bibiserv.techfak.uni-bielefeld.de/chroma/data/glucoseA.cdf"));
        File outBaseDir = new File(System.getProperty("java.io.tmpdir"), "testSourceFilesRelativization");
        File pathLocal1 = new File(outBaseDir, "local1.cdf");
        FileFragment local1 = new FileFragment(pathLocal1);
        File pathLocal2 = new File(outBaseDir, "local2.cdf");
        FileFragment local2 = new FileFragment(pathLocal2);
        File pathLocal3 = new File(new File(outBaseDir, "subdir"), "local3.cdf");
        FileFragment local3 = new FileFragment(pathLocal3);
        local3.addSourceFile(local1, local2);
        File pathLocal4 = new File(new File(outBaseDir.getParentFile(), "testUpDir/subdir"), "local2.cdf");
        FileFragment local4 = new FileFragment(pathLocal4);
        IFileFragment[] sourceFiles = new IFileFragment[]{remote1, local3};
        local4.addSourceFile(sourceFiles);

        ArrayChar.D2 a = (ArrayChar.D2) FragmentTools.createSourceFilesArray(local4, local4.getSourceFiles());
        System.out.println("Raw Source files from array:");
        System.out.println(a);
        Assert.assertEquals(sourceFiles[0].getUri(), URI.create(a.getString(0)));
//        Assert.assertEquals(URI.create("../../testSourceFilesRelativization/subdir/local3.cdf"), URI.create(a.getString(1)));
        Assert.assertEquals(local3.getUri(), FileTools.resolveRelativeUri(local4.getUri(), URI.create(a.getString(1))));
        ArrayList<IFileFragment> resolvedSourceFiles = new ArrayList<IFileFragment>(local4.getSourceFiles());
        for (int i = 0; i < sourceFiles.length; i++) {
            Assert.assertEquals(resolvedSourceFiles.get(i).getUri(), sourceFiles[i].getUri());
        }
        
        URI uri11 = new File("/tmp/69138/workflow/command1/file1.cdf").toURI();
        FileFragment f1 = new FileFragment(uri11);
        URI uri12 = new File("/tmp/69138/workflow/command1/otherOutput/oof.cdf").toURI();
        FileFragment f2 = new FileFragment(uri12);
        Assert.assertTrue(FragmentTools.isChild(f1, f2));
        
        URI uri13 = new File("/tmp/69138/workflow/command2/file1.cdf").toURI();
        FileFragment f3 = new FileFragment(uri13);
        URI uri14 = new File("/tmp/69138/workflow/command0/otherOutput/oof.cdf").toURI();
        FileFragment f4 = new FileFragment(uri14);
        Assert.assertFalse(FragmentTools.isChild(f3, f4));
        
        URI uri15 = new File("/tmp/69138/workflow/command2/otherOutput/oof1.cdf").toURI();
        FileFragment f5 = new FileFragment(uri15);
        f5.addSourceFile(f3);
        Assert.assertFalse(f5.getSourceFiles().isEmpty());
        Assert.assertTrue(FragmentTools.isChild(f3,f5));
        Assert.assertEquals(URI.create("otherOutput/oof1.cdf"), FragmentTools.resolve(f5, f3));
    }

    @Test
    public void testSourceFilesEquality() {
//        FileFragment.clearFragments();
        logging.setLogLevel("log4j.category.cross.datastructures.fragments", "DEBUG");
        FileFragment remote1 = new FileFragment(URI.create("http://bibiserv.techfak.uni-bielefeld.de/chroma/data/glucoseA.cdf"));
        File outBaseDir = new File(System.getProperty("java.io.tmpdir"), "testSourceFilesRelativization");
        File pathLocal1 = new File(outBaseDir, "local1.cdf");
        FileFragment local1 = new FileFragment(pathLocal1);
        File pathLocal2 = new File(outBaseDir, "local2.cdf");
        FileFragment local2 = new FileFragment(pathLocal2);
        File pathLocal3 = new File(new File(outBaseDir, "subdir"), "local3.cdf");
        FileFragment local3 = new FileFragment(pathLocal3);
        local3.addSourceFile(local1, local2);
        File pathLocal4 = new File(new File(outBaseDir.getParentFile(), "testUpDir/subdir"), "local2.cdf");
        FileFragment local4 = new FileFragment(pathLocal4);
        IFileFragment[] sourceFiles = new IFileFragment[]{remote1, local3};
        local4.addSourceFile(sourceFiles);

        Collection<IFileFragment> c = local4.getSourceFiles();
        local4.save();
        Collection<IFileFragment> cs = local4.getSourceFiles();
        //assert that we have the same number of source files
        Assert.assertEquals(c.size(), cs.size());
        //assert that we have the same source files
        Assert.assertEquals(c, cs);
    }

    @Test
    public void testBreadthFirstSearch() {
        logging.setLogLevel("log4j.category.cross.datastructures.fragments", "DEBUG");
        File outBaseDir = new File(System.getProperty("java.io.tmpdir"), "testSourceFilesBFS");

        //create two Filefragments at the same level with equally named variables
        File pathLocal1 = new File(outBaseDir, "local1.cdf");
        FileFragment local1 = new FileFragment(pathLocal1);
        local1.addChild("testVar1").setArray(Array.factory(new double[]{876, 986123.8, 21986.856, 79006.8613, 897123.123}));

        File pathLocal2 = new File(outBaseDir, "local2.cdf");
        FileFragment local2 = new FileFragment(pathLocal2);
        local2.addChild("testVar1").setArray(Array.factory(new double[]{7889, 986123.8, 21986.856, 79006.8613, 897123.123}));

        File pathLocal3 = new File(new File(outBaseDir, "subdir"), "local3.cdf");
        FileFragment local3 = new FileFragment(pathLocal3);
        local3.addSourceFile(local1, local2);
        log.info("Source files: {}", local3.getSourceFiles());

        try {
            local3.getChild("testVar1");
        } catch (ConstraintViolationException ise) {
            Assert.assertTrue("IllegalStateException", true);
        } catch (ResourceNotAvailableException rnae) {
            Assert.fail(rnae.getLocalizedMessage());
        }

    }

    @Test
    public void testBreadthFirstDifferentDepthSearch() {
//        FileFragment.clearFragments();
        logging.setLogLevel("log4j.category.cross.datastructures.fragments", "DEBUG");
        File outBaseDir = new File(System.getProperty("java.io.tmpdir"), "testSourceFilesBFS");

        File pathLocal0 = new File(outBaseDir, "local0.cdf");
        FileFragment local0 = new FileFragment(pathLocal0);
        local0.addChild("testVar1").setArray(Array.factory(new double[]{876, 986123.8, 21986.856, 79006.8613, 897123.123}));

        //create two Filefragments at the same level with equally named variables
        File pathLocal1 = new File(outBaseDir, "local1.cdf");
        FileFragment local1 = new FileFragment(pathLocal1);
        local1.addSourceFile(local0);

        File pathLocal2 = new File(outBaseDir, "local2.cdf");
        FileFragment local2 = new FileFragment(pathLocal2);
        local2.addChild("testVar1").setArray(Array.factory(new double[]{7889, 986123.8, 21986.856, 79006.8613, 897123.123}));

        File pathLocal3 = new File(new File(outBaseDir, "subdir"), "local3.cdf");
        FileFragment local3 = new FileFragment(pathLocal3);
        local3.addSourceFile(local1, local2);
        log.info("Source files: {}", local3.getSourceFiles());

        try {
            IVariableFragment ivf = local3.getChild("testVar1");
            log.info("Parent of variable: {}",ivf.getParent());
            Assert.assertEquals(local2, ivf.getParent());
        } catch (ResourceNotAvailableException rnae) {
            Assert.fail(rnae.getLocalizedMessage());
        }

    }

    @Test(expected = ConstraintViolationException.class)
    public void testBreadthFirstSameDepthSearch() {
//        FileFragment.clearFragments();
        logging.setLogLevel("log4j.category.cross.datastructures.fragments", "DEBUG");
        File outBaseDir = new File(System.getProperty("java.io.tmpdir"), "testSourceFilesBFS");

        File pathLocal0 = new File(outBaseDir, "local0.cdf");
        FileFragment local0 = new FileFragment(pathLocal0);
        local0.addChild("testVar1").setArray(Array.factory(new double[]{876, 986123.8, 21986.856, 79006.8613, 897123.123}));

        //create two Filefragments at the same level with equally named variables
        File pathLocal1 = new File(outBaseDir, "local1.cdf");
        FileFragment local1 = new FileFragment(pathLocal1);
        local1.addSourceFile(local0);

        File pathLocal2 = new File(outBaseDir, "local2.cdf");
        FileFragment local2 = new FileFragment(pathLocal2);
        local2.addSourceFile(local1);
        local2.addChild("testVar1").setArray(Array.factory(new double[]{7889, 986123.8, 21986.856, 79006.8613, 897123.123}));

        File pathLocal3 = new File(new File(outBaseDir, "subdir"), "local3.cdf");
        FileFragment local3 = new FileFragment(pathLocal3);
        local3.addSourceFile(local2);

        File pathLocal4 = new File(new File(outBaseDir, "subdir"), "local4.cdf");
        FileFragment local4 = new FileFragment(pathLocal4);
        local4.addSourceFile(local1, local3);
        log.info("Source files: {}", local4.getSourceFiles());

        try {
            IVariableFragment ivf = local4.getChild("testVar1");
        } catch (ResourceNotAvailableException rnae) {
            Assert.fail(rnae.getLocalizedMessage());
        }

    }
    
    @Test
    public void testVariableFragmentEquality() {
        IFileFragment f = createTestFragment();
        //first check that all are there
        Assert.assertNotNull(f.getChild("variable1"));
        Assert.assertNotNull(f.getChild("variable2"));
        Assert.assertNotNull(f.getChild("indexVar1"));
        Assert.assertNotNull(f.getChild("variable3"));
        try{
            f.getChild("variable3");
        }catch(ResourceNotAvailableException rnae) {
            log.info("Caught expected exception for non-existing child {}","variable3");
        }
        Assert.assertNotNull(f.getChild("variable1").getIndex());
        Assert.assertNotNull(f.getChild("variable2").getIndex());
        Assert.assertEquals(f.getChild("variable1").getIndex(),f.getChild("variable2").getIndex());
        Assert.assertEquals(f.getChild("indexVar1"),f.getChild("variable1").getIndex());
        Assert.assertNull(f.getChild("variable3").getIndex());
        Assert.assertNull(f.getChild("indexVar1").getIndex());
    }
    
    public IFileFragment createTestFragment() {
        IFileFragment f = new FileFragment();
        f.addChild("variable1").setIndex(f.addChild("indexVar1"));
        List<Array> l1 = new ArrayList<Array>();
        l1.add(Array.factory(new double[]{1.2,1.5}));
        l1.add(Array.factory(new double[]{2.2,2.6,2.87}));
        l1.add(Array.factory(new double[]{3.67}));
        f.getChild("variable1").setIndexedArray(l1);
        f.addChild("variable2").setIndex(f.getChild("indexVar1"));
        List<Array> l2 = new ArrayList<Array>();
        l2.add(Array.factory(new int[]{1,1}));
        l2.add(Array.factory(new int[]{2,2,2}));
        l2.add(Array.factory(new int[]{3}));
        f.getChild("variable2").setIndexedArray(l2);
        f.addChild("variable3").setArray(Array.factory(new double[]{2,3.3,235.32,352.3}));
        f.getChild("indexVar1").setArray(Array.factory(new int[]{2,3,1}));
        return f;
    }
}
