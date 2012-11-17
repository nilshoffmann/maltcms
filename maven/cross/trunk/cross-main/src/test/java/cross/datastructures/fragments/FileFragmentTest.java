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
import cross.datastructures.tools.FileTools;
import cross.io.MockDatasource;
import cross.test.SetupLogging;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import ucar.ma2.ArrayChar;

/**
 *
 * @author Nils Hoffmann
 */
public class FileFragmentTest {

    @Rule
    public SetupLogging logging = new SetupLogging();

    /**
     *
     */
    public FileFragmentTest() {
        Factory.getInstance().getDataSourceFactory().setDataSources(Arrays.asList(MockDatasource.class.getCanonicalName()));
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

        ArrayChar.D2 a = (ArrayChar.D2) local4.getChild("source_files").getArray();
        System.out.println("Raw Source files from array:");
        Assert.assertEquals(sourceFiles[0].getUri(), URI.create(a.getString(0)));
        Assert.assertEquals(URI.create("../../testSourceFilesRelativization/subdir/local3.cdf"), URI.create(a.getString(1)));
        Assert.assertEquals(local3.getUri(), FileTools.resolveRelativeUri(local4.getUri(), URI.create(a.getString(1))));
        ArrayList<IFileFragment> resolvedSourceFiles = new ArrayList<IFileFragment>(local4.getSourceFiles());
        for (int i = 0; i < sourceFiles.length; i++) {
            Assert.assertEquals(resolvedSourceFiles.get(i).getUri(), sourceFiles[i].getUri());
        }
    }
}
