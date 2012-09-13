/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
package maltcms.io.andims;

import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.ImmutableFileFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.exception.ResourceNotAvailableException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;
import maltcms.test.ExtractHelper;
import maltcms.test.SetupLogging;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayDouble.D3;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayInt.D1;
import ucar.ma2.ArrayInt.D2;
import ucar.ma2.IndexIterator;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

/**
 *
 * @author hoffmann
 */
@Slf4j
public class NetcdfDataSourceTest {

    /**
     *
     */
    @Rule
    public SetupLogging sl = new SetupLogging();
    /**
     *
     */
    @Rule
    public TemporaryFolder tf = new TemporaryFolder();
    File[] files1D;
    File[] files2D;

    @Before
    public void extractTestData() {
        if (files1D == null || files2D == null) {
            try {
                //files1D = ExtractHelper.extractAllForType(tf.newFolder(ExtractHelper.FType.CDF_1D.name()), ExtractHelper.FType.CDF_1D);
                files1D = ExtractHelper.extractForType(tf.newFolder(), ExtractHelper.FType.CDF_1D, "/cdf/1D/glucoseA.cdf.gz");
                files2D = new File[0];//ExtractHelper.extractAllForType(tf.newFolder(ExtractHelper.FType.CDF_2D.name()), ExtractHelper.FType.CDF_2D);
            } catch (IOException ex) {
                Logger.getLogger(NetcdfDataSourceTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    NetcdfDataSource getDataSource() {
        return new NetcdfDataSource();
    }

    /**
     * Test of canRead method, of class NetcdfDataSource.
     */
    @Test
    public void testCanRead() {
        for (File f : files1D) {
            Assert.assertEquals(1, getDataSource().canRead(new FileFragment(f)));
            Assert.assertEquals(1, getDataSource().canRead(new ImmutableFileFragment(f)));
        }
        for (File f : files2D) {
            Assert.assertEquals(1, getDataSource().canRead(new FileFragment(f)));
            Assert.assertEquals(1, getDataSource().canRead(new ImmutableFileFragment(f)));
        }
    }

    /**
     * Test of readAll method, of class NetcdfDataSource.
     */
    @Test
    public void testReadAll() throws Exception {
        for (File f : files1D) {
            List<Array> l = getDataSource().readAll(new FileFragment(f));
            Assert.assertTrue(l.size() > 0);
            try {
                l = getDataSource().readAll(new ImmutableFileFragment(f));
                Assert.fail();
            } catch (UnsupportedOperationException uoe) {
            }
        }
        for (File f : files2D) {
            List<Array> l = getDataSource().readAll(new FileFragment(f));
            Assert.assertTrue(l.size() > 0);
            try {
                l = getDataSource().readAll(new ImmutableFileFragment(f));
                Assert.fail();
            } catch (UnsupportedOperationException uoe) {
            }
        }
    }

    /**
     * Test of readIndexed method, of class NetcdfDataSource.
     */
    @Test
    public void testReadIndexed() throws Exception {
        testReadIndexed2();
    }

    /**
     * Test of readIndexed2 method, of class NetcdfDataSource.
     */
    @Test
    public void testReadIndexed2() throws Exception {
        for (File f : files1D) {
            IFileFragment ff = new FileFragment(f);
            IVariableFragment si = ff.getChild("scan_index");
            IVariableFragment iv = ff.getChild("intensity_values");
            iv.setIndex(si);
            List<Array> l = getDataSource().readIndexed(iv);
            Assert.assertTrue(l.size() > 0);

            ff = new ImmutableFileFragment(f);
            si = ff.getChild("scan_index");
            iv = ff.getChild("intensity_values");
            iv.setIndex(si);
            l = getDataSource().readIndexed(iv);
            Assert.assertTrue(l.size() > 0);
        }
        for (File f : files2D) {
            IFileFragment ff = new FileFragment(f);
            IVariableFragment si = ff.getChild("scan_index");
            IVariableFragment iv = ff.getChild("intensity_values");
            iv.setIndex(si);
            List<Array> l = getDataSource().readIndexed(iv);
            Assert.assertTrue(l.size() > 0);

            ff = new ImmutableFileFragment(f);
            si = ff.getChild("scan_index");
            iv = ff.getChild("intensity_values");
            iv.setIndex(si);
            l = getDataSource().readIndexed(iv);
            Assert.assertTrue(l.size() > 0);
        }
    }

    /**
     * Test of readSingle method, of class NetcdfDataSource.
     */
    @Test
    public void testReadSingle() throws Exception {
        for (File f : files1D) {
            IFileFragment ff = new FileFragment(f);
            IVariableFragment si = ff.getChild("scan_index");
            Array a = getDataSource().readSingle(si);
            Assert.assertNotNull(a);
            Assert.assertTrue(a.getShape()[0] > 0);

            ff = new ImmutableFileFragment(f);
            si = ff.getChild("scan_index");
            a = getDataSource().readSingle(si);
            Assert.assertNotNull(a);
            Assert.assertTrue(a.getShape()[0] > 0);
        }
        for (File f : files2D) {
            IFileFragment ff = new FileFragment(f);
            IVariableFragment si = ff.getChild("scan_index");
            Array a = getDataSource().readSingle(si);
            Assert.assertNotNull(a);
            Assert.assertTrue(a.getShape()[0] > 0);

            ff = new ImmutableFileFragment(f);
            si = ff.getChild("scan_index");
            a = getDataSource().readSingle(si);
            Assert.assertNotNull(a);
            Assert.assertTrue(a.getShape()[0] > 0);
        }
    }

    /**
     * Test of readStructure method, of class NetcdfDataSource.
     */
    @Test
    public void testReadStructure_IVariableFragment() throws Exception {
        for (File f : files1D) {
            IFileFragment ff = new FileFragment(f);
            IVariableFragment si = new VariableFragment(ff, "scan_index");
            si = getDataSource().readStructure(si);
            Assert.assertNotNull(si.getDataType());
            Assert.assertNotNull(si.getDimensions());
            Assert.assertNotNull(si.getRange());
            Assert.assertFalse(si.hasArray());

            si = ff.getChild("scan_index", true);
            Assert.assertNotNull(si.getDataType());
            Assert.assertNotNull(si.getDimensions());
            Assert.assertNotNull(si.getRange());
            Assert.assertFalse(si.hasArray());

            ff = new ImmutableFileFragment(f);
            si = ff.getChild("scan_index", true);
            Assert.assertNotNull(si.getDataType());
            Assert.assertNotNull(si.getDimensions());
            Assert.assertNotNull(si.getRange());
            //ImmutableVariableFragment2 always returns true for hasArray
            Assert.assertTrue(si.hasArray());
        }
        for (File f : files2D) {
            IFileFragment ff = new FileFragment(f);
            IVariableFragment si = new VariableFragment(ff, "scan_index");
            si = getDataSource().readStructure(si);
            Assert.assertNotNull(si.getDataType());
            Assert.assertNotNull(si.getDimensions());
            Assert.assertNotNull(si.getRange());
            Assert.assertFalse(si.hasArray());

            si = ff.getChild("scan_index", true);
            Assert.assertNotNull(si.getDataType());
            Assert.assertNotNull(si.getDimensions());
            Assert.assertNotNull(si.getRange());
            Assert.assertFalse(si.hasArray());

            ff = new ImmutableFileFragment(f);
            si = ff.getChild("scan_index", true);
            Assert.assertNotNull(si.getDataType());
            Assert.assertNotNull(si.getDimensions());
            Assert.assertNotNull(si.getRange());
            //ImmutableVariableFragment2 always returns true for hasArray
            Assert.assertTrue(si.hasArray());
        }
    }

    /**
     * Test of supportedFormats method, of class NetcdfDataSource.
     */
    @Test
    public void testSupportedFormats() {
        String[] fileEnding = new String[]{"nc", "nc.gz", "nc.z", "nc.zip", "nc.gzip", "nc.bz2", "cdf", "cdf.gz", "cdf.z", "cdf.zip", "cdf.gzip", "cdf.bz2"};
        Assert.assertEquals(new NetcdfDataSource().supportedFormats(), Arrays.asList(fileEnding));
    }

    /**
     * Test of write method, of class NetcdfDataSource.
     */
    @Test
    public void testWrite() {
        File outputFolder = tf.newFolder("testOutput");
        File testCdf = new File(outputFolder, "testCdf.cdf");
        FileFragment ff = new FileFragment(testCdf);
        Attribute a1 = new Attribute("software", "maltcms");
        Attribute a2 = new Attribute("purpose", "testing");
        Attribute a3 = new Attribute("version", Integer.valueOf(1));
        ff.setAttributes(a1, a2, a3);
        Dimension dim1 = new Dimension("dim1", 15);
        Dimension dim2 = new Dimension("dim2", 8);
        Dimension dim3 = new Dimension("dim3", 10);
        Dimension dim5 = new Dimension("dim5", 24);
        Dimension dim6 = new Dimension("dim6", 240);
        VariableFragment ivf1 = new VariableFragment(ff, "variable1");
        ivf1.setDimensions(new Dimension[]{dim1, dim2, dim3});
        ivf1.setAttributes(new Attribute("description", "three-dimensional array"));
//        ArrayDouble.D3 arr1 = new ArrayDouble.D3(dim1.getLength(),dim2.getLength(),dim3.getLength());
        ArrayDouble.D3 arr1 = new ArrayDouble.D3(15, 8, 10);
        ivf1.setArray(arr1);
        VariableFragment ivf2 = new VariableFragment(ff, "variable2");
        ivf2.setDimensions(new Dimension[]{dim3});
        ArrayInt.D1 arr2 = new ArrayInt.D1(dim3.getLength());
        ivf2.setArray(arr2);
        //unused dimension 
        Dimension dim4 = new Dimension("dim4", 214);
        ff.addDimensions(dim4);

        VariableFragment ivf3 = new VariableFragment(ff, "variable3");
        ArrayInt.D2 arr3 = new ArrayInt.D2(25, 17);
        ivf3.setArray(arr3);
        
        VariableFragment ivf4 = new VariableFragment(ff, "variable4");
        ivf4.setDimensions(new Dimension[]{dim5});
        List<Array> arrays = new ArrayList<Array>();
        ArrayInt.D1 index = new ArrayInt.D1(24);
        int offset = 0;
        for(int i = 0;i<24;i++) {
            index.set(i,offset);
            Array a = new ArrayDouble.D1(10);
            arrays.add(a);
            offset+=10;
        }
        ivf4.setArray(index);
        VariableFragment ivf5 = new VariableFragment(ff, "variable5");
        ivf5.setDimensions(new Dimension[]{dim6});
        ivf5.setIndex(ivf4);
        ivf5.setIndexedArray(arrays);
        
        VariableFragment ivf6 = new VariableFragment(ff, "variable6");
        ivf6.setDimensions(new Dimension[]{dim6});
        ivf6.setIndex(ivf4);
        List<Array> arrays2 = new ArrayList<Array>();
        for(int i = 0;i<24;i++) {
            Array a = new ArrayDouble.D1(10);
            arrays2.add(a);
        }
        ivf6.setIndexedArray(arrays2);

        System.out.println("Defined dimensions: " + ff.getDimensions());

        boolean b = getDataSource().write(ff);
        Assert.assertTrue(b);
        testDirectRead(testCdf, dim4, ivf1, arr1, ivf2, arr2, arr3);
        testIndirectRead(testCdf, dim4, ivf1, arr1, ivf2, arr2, arr3);
        
    }

    public void testDirectRead(File testCdf, Dimension dim4, VariableFragment ivf1, D3 arr1, VariableFragment ivf2, D1 arr2, D2 arr3) throws ResourceNotAvailableException {
        //read in the created file
        IFileFragment readFragment = new FileFragment(testCdf);
        try {
            //initialize fragment with content
            getDataSource().readStructure(readFragment);
        } catch (IOException ex) {
            Logger.getLogger(NetcdfDataSourceTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        Assert.assertFalse(readFragment.getDimensions().contains(dim4));
        System.out.println("Stored dimensions: " + readFragment.getDimensions());
        System.out.println("Stored variables: ");
        for (IVariableFragment v : readFragment) {
            System.out.println("Variable: " + v.toString());
            System.out.println("\tDataType: " + v.getDataType());
            System.out.println("\tDimensions: " + Arrays.toString(v.getDimensions()));
        }
        //check global attributes
        Assert.assertEquals(readFragment.getAttribute("software").getStringValue(), "maltcms");
        Assert.assertEquals(readFragment.getAttribute("purpose").getStringValue(), "testing");
        Assert.assertEquals(readFragment.getAttribute("version").getNumericValue(), Integer.valueOf(1));
        //check variables
        IVariableFragment rivf1 = readFragment.getChild("variable1");
        Dimension[] rdims1 = rivf1.getDimensions();
        Dimension[] dims1 = ivf1.getDimensions();
        //compare dimensions
        for (int i = 0; i < dims1.length; i++) {
            Dimension left = dims1[i];
            Dimension right = rdims1[i];
            Assert.assertEquals(left, right);
        }
        IndexIterator ii1 = arr1.getIndexIterator();
        IndexIterator rii1 = rivf1.getArray().getIndexIterator();
        log.info("Original shape: {}", Arrays.toString(arr1.getShape()));
        log.info("Restored shape: {}", Arrays.toString(rivf1.getArray().getShape()));
        Assert.assertEquals(arr1.getShape()[0], rivf1.getArray().getShape()[0]);
        Assert.assertEquals(arr1.getShape()[1], rivf1.getArray().getShape()[1]);
        while (ii1.hasNext() && rii1.hasNext()) {
            Assert.assertEquals(ii1.getDoubleNext(), rii1.getDoubleNext());
        }
        Assert.assertEquals(rivf1.getAttribute("description").getStringValue(), "three-dimensional array");

        //check next variable
        IVariableFragment rivf2 = readFragment.getChild("variable2");
        Dimension[] rdims2 = rivf2.getDimensions();
        Dimension[] dims2 = ivf2.getDimensions();
        //compare dimensions
        for (int i = 0; i < dims2.length; i++) {
            Dimension left = dims2[i];
            Dimension right = rdims2[i];
            Assert.assertEquals(left, right);
        }
        IndexIterator ii2 = arr2.getIndexIterator();
        IndexIterator rii2 = rivf2.getArray().getIndexIterator();
        Assert.assertEquals(arr2.getShape()[0], rivf2.getArray().getShape()[0]);
        while (ii2.hasNext() && rii2.hasNext()) {
            Assert.assertEquals(ii2.getDoubleNext(), rii2.getDoubleNext());
        }

        IVariableFragment rivf3 = readFragment.getChild("variable3");
        Array ria3 = rivf3.getArray();
        Assert.assertEquals(arr3.getShape()[0], ria3.getShape()[0]);
        Assert.assertEquals(arr3.getShape()[1], ria3.getShape()[1]);

        for (Dimension dim : readFragment.getDimensions()) {
            Assert.assertNotSame(dim.getName(), dim4.getName());
        }
        
        IVariableFragment rivf4 = readFragment.getChild("variable4");
        IVariableFragment rivf5 = readFragment.getChild("variable5",true);
        rivf5.setIndex(rivf4);
        List<Array> rivf5l = rivf5.getIndexedArray();
        Assert.assertNotNull(rivf5l);
        Assert.assertEquals(24, rivf5l.size());
        IVariableFragment rivf6 = readFragment.getChild("variable6",true);
        rivf6.setIndex(rivf4);
        List<Array> rivf6l = rivf6.getIndexedArray();
        Assert.assertNotNull(rivf6l);
        Assert.assertEquals(24, rivf6l.size());
        
        
        //test indirect read
    }
    
    public void testIndirectRead(File testCdf, Dimension dim4, VariableFragment ivf1, D3 arr1, VariableFragment ivf2, D1 arr2, D2 arr3) throws ResourceNotAvailableException {
        File outputFolder = tf.newFolder("testOutput/referenceTest");
        File testCdf2 = new File(outputFolder, "testCdf2.cdf");
        //read in the created file
        IFileFragment readFragment = new FileFragment(testCdf2);
        readFragment.addSourceFile(new ImmutableFileFragment(testCdf));
        try {
            //initialize fragment with content
            getDataSource().readStructure(readFragment);
        } catch (IOException ex) {
            Logger.getLogger(NetcdfDataSourceTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        Assert.assertFalse(readFragment.getDimensions().contains(dim4));
        System.out.println("Stored dimensions: " + readFragment.getDimensions());
        System.out.println("Stored variables: ");
        for (IVariableFragment v : readFragment) {
            System.out.println("Variable: " + v.toString());
            System.out.println("\tDataType: " + v.getDataType());
            System.out.println("\tDimensions: " + Arrays.toString(v.getDimensions()));
        }
//        //check global attributes
//        Assert.assertEquals(readFragment.getAttribute("software").getStringValue(), "maltcms");
//        Assert.assertEquals(readFragment.getAttribute("purpose").getStringValue(), "testing");
//        Assert.assertEquals(readFragment.getAttribute("version").getNumericValue(), Integer.valueOf(1));
        //check variables
        IVariableFragment rivf1 = readFragment.getChild("variable1");
        Dimension[] rdims1 = rivf1.getDimensions();
        Dimension[] dims1 = ivf1.getDimensions();
        //compare dimensions
        for (int i = 0; i < dims1.length; i++) {
            Dimension left = dims1[i];
            Dimension right = rdims1[i];
            Assert.assertEquals(left, right);
        }
        IndexIterator ii1 = arr1.getIndexIterator();
        IndexIterator rii1 = rivf1.getArray().getIndexIterator();
        log.info("Original shape: {}", Arrays.toString(arr1.getShape()));
        log.info("Restored shape: {}", Arrays.toString(rivf1.getArray().getShape()));
        Assert.assertEquals(arr1.getShape()[0], rivf1.getArray().getShape()[0]);
        Assert.assertEquals(arr1.getShape()[1], rivf1.getArray().getShape()[1]);
        while (ii1.hasNext() && rii1.hasNext()) {
            Assert.assertEquals(ii1.getDoubleNext(), rii1.getDoubleNext());
        }
        Assert.assertEquals(rivf1.getAttribute("description").getStringValue(), "three-dimensional array");

        //check next variable
        IVariableFragment rivf2 = readFragment.getChild("variable2");
        Dimension[] rdims2 = rivf2.getDimensions();
        Dimension[] dims2 = ivf2.getDimensions();
        //compare dimensions
        for (int i = 0; i < dims2.length; i++) {
            Dimension left = dims2[i];
            Dimension right = rdims2[i];
            Assert.assertEquals(left, right);
        }
        IndexIterator ii2 = arr2.getIndexIterator();
        IndexIterator rii2 = rivf2.getArray().getIndexIterator();
        Assert.assertEquals(arr2.getShape()[0], rivf2.getArray().getShape()[0]);
        while (ii2.hasNext() && rii2.hasNext()) {
            Assert.assertEquals(ii2.getDoubleNext(), rii2.getDoubleNext());
        }

        IVariableFragment rivf3 = readFragment.getChild("variable3");
        Array ria3 = rivf3.getArray();
        Assert.assertEquals(arr3.getShape()[0], ria3.getShape()[0]);
        Assert.assertEquals(arr3.getShape()[1], ria3.getShape()[1]);

        for (Dimension dim : readFragment.getDimensions()) {
            Assert.assertNotSame(dim.getName(), dim4.getName());
        }
        
        IVariableFragment rivf4 = readFragment.getChild("variable4");
        IVariableFragment rivf5 = readFragment.getChild("variable5",true);
        rivf5.setIndex(rivf4);
        List<Array> rivf5l = rivf5.getIndexedArray();
        Assert.assertNotNull(rivf5l);
        Assert.assertEquals(24, rivf5l.size());
        IVariableFragment rivf6 = readFragment.getChild("variable6",true);
        rivf6.setIndex(rivf4);
        List<Array> rivf6l = rivf6.getIndexedArray();
        Assert.assertNotNull(rivf6l);
        Assert.assertEquals(24, rivf6l.size());
        
        //test indirect read
    }
}
