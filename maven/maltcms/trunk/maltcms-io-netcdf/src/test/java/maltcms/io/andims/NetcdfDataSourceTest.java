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

import cross.Factory;
import cross.datastructures.cache.CacheFactory;
import cross.datastructures.cache.CacheType;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.ImmutableFileFragment;
import cross.datastructures.fragments.ImmutableVariableFragment2;
import cross.datastructures.fragments.VariableFragment;
import cross.exception.ResourceNotAvailableException;
import cross.test.LogMethodName;
import maltcms.test.ExtractClassPathFiles;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;
import cross.test.SetupLogging;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.IndexIterator;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class NetcdfDataSourceTest {

    @Rule
    public SetupLogging sl = new SetupLogging();
    @Rule
    public LogMethodName lmn = new LogMethodName();
    @Rule
    public TemporaryFolder tf = new TemporaryFolder();
    @Rule
    public ExtractClassPathFiles ecpf = new ExtractClassPathFiles(tf, "/cdf/1D/glucoseA.cdf.gz");

    NetcdfDataSource getDataSource() {
        return new NetcdfDataSource();
    }

    /**
     *
     */
    @Before
    public void setUp() {
        Factory.getInstance().getDataSourceFactory().setDataSources(Arrays.asList(NetcdfDataSource.class.getCanonicalName()));
        CacheFactory.setDefaultFragmentCacheType(CacheType.NONE);
    }

    /**
     * Test of canRead method, of class NetcdfDataSource.
     */
    @Test
    public void testCanRead() {
//        FileFragment.clearFragments();
        for (File f : ecpf.getFiles()) {
            Assert.assertEquals(1, getDataSource().canRead(new FileFragment(f)));
//            FileFragment.clearFragments();
            Assert.assertEquals(1, getDataSource().
                    canRead(new ImmutableFileFragment(new FileFragment(f.toURI()))));
        }
//        FileFragment.clearFragments();
    }

    /**
     * Test of readAll method, of class NetcdfDataSource.
     */
    @Test
    public void testReadAll() throws Exception {
//        FileFragment.clearFragments();
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
//        FileFragment.clearFragments();
    }

    /**
     * Test of readIndexed method, of class NetcdfDataSource.
     */
    @Test
    public void testReadIndexed() throws Exception {
//        FileFragment.clearFragments();
        for (File f : ecpf.getFiles()) {
            IFileFragment ff = new FileFragment(f);
            IVariableFragment si = ff.getChild("scan_index");
            Assert.assertNotNull(si.getArray());
            IVariableFragment iv = ff.getChild("intensity_values");
            iv.setIndex(si);
            List<Array> l = getDataSource().readIndexed(iv);
            Assert.assertTrue(l.size() > 0);

            ff = new ImmutableFileFragment(f);
            si = ff.getChild("scan_index");
            Assert.assertNotNull(si.getArray());
            iv = ff.getChild("intensity_values");
            iv.setIndex(si);
            l = getDataSource().readIndexed(iv);
            Assert.assertTrue(l.size() > 0);
            iv.setIndex(null);
            ff.removeChild(si);
            ff.removeChild(iv);
//            FileFragment.clearFragments();
            ff = new FileFragment(f);
            si = ff.getChild("scan_index");
            Assert.assertNotNull(si.getArray());

        }
//        FileFragment.clearFragments();
    }

    /**
     * Test of readSingle method, of class NetcdfDataSource.
     */
    @Test
    public void testReadSingle() throws Exception {
//        FileFragment.clearFragments();
        for (File f : ecpf.getFiles()) {
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
//        FileFragment.clearFragments();
    }

    /**
     * Test of readStructure method, of class NetcdfDataSource.
     */
    @Test
    public void testReadStructure_IVariableFragment() throws Exception {
//        FileFragment.clearFragments();
        for (File f : ecpf.getFiles()) {
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
            //remove VariableFragment
            ff.removeChild(si);
            ff = new ImmutableFileFragment(f);
            //retrieve ImmutableVariableFragment2 from disk
            si = ff.getChild("scan_index", true);
            Assert.assertNotNull(si.getDataType());
            Assert.assertNotNull(si.getDimensions());
            Assert.assertNotNull(si.getRange());
            //Immutable variable always has an array
            Assert.assertTrue(si.hasArray());
            Assert.assertNotNull(si.getArray());
        }
//        FileFragment.clearFragments();
    }

    /**
     * Test of supportedFormats method, of class NetcdfDataSource.
     */
    @Test
    public void testSupportedFormats() {
        String[] fileEnding = new String[]{"nc", "nc.gz", "nc.z", "nc.zip", "nc.gzip", "nc.bz2", "cdf", "cdf.gz", "cdf.z", "cdf.zip", "cdf.gzip", "cdf.bz2"};
        Assert.assertEquals(new NetcdfDataSource().supportedFormats(), Arrays.
                asList(fileEnding));
    }

    public List<Dimension> copyDims(Dimension... dim) {
        List<Dimension> copy = new LinkedList<Dimension>();
        for (Dimension dimension : dim) {
            copy.add(new Dimension(dimension.getName(), dimension));
        }
        return copy;
    }

    public List<Attribute> copyAttributes(Attribute... attr) {
        List<Attribute> copy = new LinkedList<Attribute>();
        for (Attribute attribute : attr) {
            copy.add(new Attribute(attribute.getName(), attribute));
        }
        return copy;
    }

    public IFileFragment createTestFragment(URI uri, List<String> variableNames, Map<String, String> indexedVariableNames, List<Attribute> attributes, Map<String, List<Attribute>> variableAttributes, Map<String, List<Dimension>> usedDimensions, List<Dimension> unusedDimensions, Map<String, List<Array>> variableToArray) {
        FileFragment ff = new FileFragment(uri);
        Attribute a1 = new Attribute("software", "maltcms");
        Attribute a2 = new Attribute("purpose", "testing");
        Attribute a3 = new Attribute("version", Integer.valueOf(1));
        ff.setAttributes(a1, a2, a3);
        attributes.addAll(copyAttributes(a1, a2, a3));
        Dimension dim1 = new Dimension("dim1", 15);
        Dimension dim2 = new Dimension("dim2", 8);
        Dimension dim3 = new Dimension("dim3", 10);
        Dimension dim5 = new Dimension("dim5", 24);
        Dimension dim6 = new Dimension("dim6", 240);

        //variable1
        VariableFragment ivf1 = new VariableFragment(ff, "variable1");
        ivf1.setDimensions(new Dimension[]{dim1, dim2, dim3});
        ivf1.setAttributes(new Attribute("description",
                "three-dimensional array"));
        ArrayDouble.D3 arr1 = new ArrayDouble.D3(15, 8, 10);
        ivf1.setArray(arr1);
        usedDimensions.put("variable1", copyDims(dim1, dim2, dim3));
        variableAttributes.put("variable1", ivf1.getAttributes());
        variableNames.add("variable1");

        //variable2
        VariableFragment ivf2 = new VariableFragment(ff, "variable2");
        ivf2.setDimensions(new Dimension[]{dim3});
        ArrayInt.D1 arr2 = new ArrayInt.D1(dim3.getLength());
        ivf2.setArray(arr2);
        usedDimensions.put("variable2", copyDims(dim3));
        variableNames.add("variable2");

        //unused dimension 
        Dimension dim4 = new Dimension("dim4", 214);
        ff.addDimensions(dim4);
        unusedDimensions.addAll(copyDims(dim4));

        //variable3 - no explicit dimension
        VariableFragment ivf3 = new VariableFragment(ff, "variable3");
        ArrayInt.D2 arr3 = new ArrayInt.D2(25, 17);
        ivf3.setArray(arr3);
        variableNames.add("variable3");

        //variable4 - index variable
        VariableFragment ivf4 = new VariableFragment(ff, "variable4");
        ivf4.setDimensions(new Dimension[]{dim5});
        List<Array> arrays = new ArrayList<Array>();
        ArrayInt.D1 index = new ArrayInt.D1(24);
        int offset = 0;
        for (int i = 0; i < 24; i++) {
            index.set(i, offset);
            Array a = new ArrayDouble.D1(10);
            arrays.add(a);
            offset += 10;
        }
        ivf4.setArray(index);
        usedDimensions.put("variable4", copyDims(dim5));
        variableNames.add("variable4");

        //variable5 - indexed variable
        VariableFragment ivf5 = new VariableFragment(ff, "variable5");
        ivf5.setDimensions(new Dimension[]{dim6});
        ivf5.setIndex(ivf4);
        ivf5.setIndexedArray(arrays);
        usedDimensions.put("variable5", copyDims(dim6));
        variableNames.add("variable5");
        indexedVariableNames.put("variable5", "variable4");

        VariableFragment ivf6 = new VariableFragment(ff, "variable6");
        ivf6.setDimensions(new Dimension[]{dim6});
        ivf6.setIndex(ivf4);
        List<Array> arrays2 = new ArrayList<Array>();
        for (int i = 0; i < 24; i++) {
            Array a = new ArrayDouble.D1(10);
            arrays2.add(a);
        }
        ivf6.setIndexedArray(arrays2);
        usedDimensions.put("variable6", copyDims(dim6));
        variableNames.add("variable6");
        indexedVariableNames.put("variable6", "variable4");

        variableToArray.put("variable1", Arrays.asList(arr1.copy()));
        variableToArray.put("variable2", Arrays.asList(arr2.copy()));
        variableToArray.put("variable3", Arrays.asList(arr3.copy()));
        variableToArray.put("variable4", Arrays.asList(index.copy()));
        variableToArray.put("variable5", arrays);
        variableToArray.put("variable6", arrays2);

        System.out.println("Defined dimensions: " + ff.getDimensions());

        return ff;
    }

    /**
     * Test of write method, of class NetcdfDataSource.
     */
    @Test
    public void testWriteRead() throws IOException {
//        FileFragment.clearFragments();
        sl.setLogLevel("cross.datastructures.fragments",
                "DEBUG");
        sl.setLogLevel("maltcms.io.andims", "DEBUG");
        List<Attribute> attributes = new LinkedList<Attribute>();
        List<String> variableNames = new LinkedList<String>();
        Map<String, String> indexedVariableNames = new HashMap<String, String>();
        Map<String, List<Dimension>> usedDimensions = new HashMap<String, List<Dimension>>();
        Map<String, List<Attribute>> variableAttributes = new HashMap<String, List<Attribute>>();
        List<Dimension> unusedDimensions = new LinkedList<Dimension>();
        Map<String, List<Array>> variableToArray = new HashMap<String, List<Array>>();

        File outputFolder = tf.newFolder();
        File testCdf = new File(outputFolder, "testWriteRead.cdf").getAbsoluteFile();
        URI testCdfUri = testCdf.toURI();
        IFileFragment ff = createTestFragment(testCdfUri, variableNames, indexedVariableNames, attributes, variableAttributes,
                usedDimensions, unusedDimensions, variableToArray);
        Assert.assertEquals(testCdf, new File(ff.getUri()));
        Assert.assertEquals(testCdfUri, ff.getUri());
        boolean b = ff.save();
        Assert.assertTrue(b);
//        FileFragment.clearFragments();
        IVariableFragment variable1 = Factory.getInstance().getDataSourceFactory().getDataSourceFor(ff).readStructure(new ImmutableVariableFragment2(ff, "variable1"));
        Assert.assertNotNull(variable1.getArray());
        testDirectRead(testCdfUri, variableNames, indexedVariableNames, attributes, variableAttributes, usedDimensions, unusedDimensions,
                variableToArray);
//        FileFragment.clearFragments();
//        testIndirectRead(testCdfUri, variableNames, indexedVariableNames, attributes, variableAttributes, usedDimensions, unusedDimensions,
//                variableToArray);
//        FileFragment.clearFragments();
        sl.setLogLevel("cross.datastructures.fragments",
                "OFF");
        sl.setLogLevel("maltcms.io.andims", "INFO");
    }

    public void dimensionsEqual(Dimension[] d1, Dimension[] d2) {
        Assert.assertEquals(d1.length, d2.length);
        //compare dimensions
        for (int i = 0; i < d1.length; i++) {
            Dimension left = d1[i];
            Dimension right = d2[i];
            Assert.assertEquals(left.getName(), right.getName());
            Assert.assertEquals(left.getLength(), right.getLength());
        }
    }

    public void variablesEqual(IVariableFragment v1, IVariableFragment v2) {
    }

    public void arraysEqual(Array a, Array b) {
        Assert.assertEquals(a.getShape().length, b.getShape().length);
        for (int i = 0; i < a.getShape().length; i++) {
            Assert.assertEquals(a.getShape()[i], b.getShape()[i]);
        }
        Assert.assertEquals(a.getElementType(), b.getElementType());
        IndexIterator ii1 = a.getIndexIterator();
        IndexIterator rii1 = b.getIndexIterator();
        log.info("Original shape: {}", Arrays.toString(a.getShape()));
        log.info("Restored shape: {}", Arrays.toString(b.
                getShape()));
        while (ii1.hasNext() && rii1.hasNext()) {
            Assert.assertEquals(ii1.getDoubleNext(), rii1.getDoubleNext());
        }
    }

    public void testDirectRead(URI testCdf, List<String> variableNames, Map<String, String> indexedVariableNames, List<Attribute> attributes, Map<String, List<Attribute>> variableAttributes, Map<String, List<Dimension>> usedDimensions, List<Dimension> unusedDimensions, Map<String, List<Array>> variableToArray) throws ResourceNotAvailableException {
        System.out.
                println("###################################################");
        System.out.println("# Testing direct read on file " + testCdf);
        sl.setLogLevel("cross.datastructures.fragments",
                "DEBUG");
        sl.setLogLevel("maltcms.io.andims", "DEBUG");
        //read in the created file
        IFileFragment readFragment = new FileFragment(testCdf);
        readFragment.readStructure();
        for (Dimension unusedDim : unusedDimensions) {
            Assert.assertFalse(readFragment.getDimensions().contains(unusedDim));
        }
        System.out.println("Stored dimensions: " + readFragment.getDimensions());
        System.out.println("Stored variables: ");
        for (IVariableFragment v : readFragment) {
            System.out.println("Variable: " + v.toString());
            System.out.println("\tDataType: " + v.getDataType());
            System.out.println("\tDimensions: " + Arrays.toString(v.
                    getDimensions()));
        }
        System.out.println("Global attributes: ");
        for (Attribute attribute : readFragment.getAttributes()) {
            System.out.println("Attribute: " + attribute.getName() + "=" + attribute.getStringValue());
        }
        //check global attributes
        for (Attribute attribute : attributes) {
            Assert.assertEquals(attribute, readFragment.getAttribute(attribute.getName()));
        }

        //check variables
        for (String variableName : variableNames) {
            IVariableFragment variableFragment = readFragment.getChild(variableName, true);
            if (indexedVariableNames.containsKey(variableName)) {
                //handle indexed variable data
                IVariableFragment indexFragment = readFragment.getChild(indexedVariableNames.get(variableName));
                variableFragment.setIndex(indexFragment);
                if (variableToArray.containsKey(variableName)) {
                    List<Array> arrays = variableFragment.getIndexedArray();
                    for (int i = 0; i < arrays.size(); i++) {
                        arraysEqual(arrays.get(i), variableToArray.get(variableName).get(i));
                    }
                }
            } else {
                //handle plain variable data
                if (variableToArray.containsKey(variableName)) {
                    arraysEqual(variableFragment.getArray(), variableToArray.get(variableName).get(0));
                }
            }
            if (usedDimensions.containsKey(variableName)) {
                dimensionsEqual(variableFragment.getDimensions(), usedDimensions.get(variableName).toArray(new Dimension[]{}));
            }
            if (variableAttributes.containsKey(variableName)) {
                for (Attribute variableAttribute : variableAttributes.get(variableName)) {
                    Assert.assertEquals(variableFragment.getAttribute(variableAttribute.getName()), variableAttribute);
                }
            }
        }

        System.out.println("###################################################");
        sl.setLogLevel("cross.datastructures.fragments",
                "OFF");
        sl.setLogLevel("maltcms.io.andims", "INFO");
        //test direct read
    }

    public void testIndirectRead(URI testCdf, List<String> variableNames, Map<String, String> indexedVariableNames, List<Attribute> attributes, Map<String, List<Attribute>> variableAttributes, Map<String, List<Dimension>> usedDimensions, List<Dimension> unusedDimensions, Map<String, List<Array>> variableToArray) throws ResourceNotAvailableException {
        System.out.
                println("###################################################");
        System.out.println("# Testing indirect read on file " + testCdf);
        sl.setLogLevel("cross.datastructures.fragments",
                "DEBUG");
        sl.setLogLevel("maltcms.io.andims", "DEBUG");
        File outputFolder;
        try {
            outputFolder = tf.newFolder();
            File testCdf2 = new File(outputFolder, "indirectRead1.cdf");
            //read in the created file
            IFileFragment readFragment = new FileFragment(testCdf2);
            //set source file explicitly
            readFragment.addSourceFile(new FileFragment(testCdf));
            //check variables
            for (String variableName : variableNames) {
                IVariableFragment variableFragment = readFragment.getChild(variableName, true);
                if (indexedVariableNames.containsKey(variableName)) {
                    //handle indexed variable data
                    IVariableFragment indexFragment = readFragment.getChild(indexedVariableNames.get(variableName));
                    variableFragment.setIndex(indexFragment);
                    if (variableToArray.containsKey(variableName)) {
                        List<Array> arrays = variableFragment.getIndexedArray();
                        for (int i = 0; i < arrays.size(); i++) {
                            arraysEqual(arrays.get(i), variableToArray.get(variableName).get(i));
                        }
                    }

                    //make it fail for now
                    IVariableFragment f1 = readFragment.getChild(variableName, true);
                    Assert.assertSame(variableFragment, f1);
                    readFragment.getChild(variableName, true).setIndex(readFragment.getChild(indexedVariableNames.get(variableName)));
                    if (variableToArray.containsKey(variableName)) {
                        List<Array> arrays = readFragment.getChild(variableName, true).getIndexedArray();
                        for (int i = 0; i < arrays.size(); i++) {
                            arraysEqual(arrays.get(i), variableToArray.get(variableName).get(i));
                        }
                    }

                } else {
                    //handle plain variable data
                    if (variableToArray.containsKey(variableName)) {
                        arraysEqual(variableFragment.getArray(), variableToArray.get(variableName).get(0));
                    }
                }
                if (usedDimensions.containsKey(variableName)) {
                    dimensionsEqual(variableFragment.getDimensions(), usedDimensions.get(variableName).toArray(new Dimension[]{}));
                }
                if (variableAttributes.containsKey(variableName)) {
                    for (Attribute variableAttribute : variableAttributes.get(variableName)) {
                        Assert.assertEquals(variableFragment.getAttribute(variableAttribute.getName()), variableAttribute);
                    }
                }
            }
            System.out.println(
                    "###################################################");
        } catch (IOException ex) {
            log.error("Caught exception", ex);
            Assert.fail(ex.getLocalizedMessage());
        }
        sl.setLogLevel("cross.datastructures.fragments",
                "OFF");
        sl.setLogLevel("maltcms.io.andims", "INFO");
        //test indirect read
    }

    /**
     * Test of write method, of class NetcdfDataSource.
     */
    @Test
    public void testMultiChainedReadWrite() {
//        FileFragment.clearFragments();
        sl.setLogLevel("cross.datastructures.fragments",
                "DEBUG");
        sl.setLogLevel("maltcms.io.andims", "DEBUG");
        List<IFileFragment> sources = new LinkedList<IFileFragment>();
        for (File f : ecpf.getFiles()) {
            IFileFragment ff = new FileFragment(f);
            sources.add(ff);
        }
        IFileFragment[] fragments = new IFileFragment[sources.size()];
        for (int i = 0; i < sources.size(); i++) {
            fragments[i] = sources.get(i);
        }
        try {
            File folder = tf.newFolder();
            //create hierarchy of five levels
            for (int j = 0; j < 5; j++) {
                File level = new File(folder, j + "");
                //create as many fragments as there were given
                for (int i = 0; i < fragments.length; i++) {
                    FileFragment work = new FileFragment(level, i + ".cdf");
                    work.addSourceFile(fragments[i]);
                    //create a shadowing variable
                    IVariableFragment shadow = new VariableFragment(work,
                            "shadow-" + i);
                    shadow.setArray(Array.factory(new int[]{j}));
                    //create unique variable
                    IVariableFragment unique = new VariableFragment(work,
                            "unique-" + j);
                    unique.setArray(Array.factory(new int[]{i, j}));
                    System.out.println(work.toString());
                    work.save();
                    work.clearArrays();
                    work.clearDimensions();
                    fragments[i] = work;
                }
            }
            for (int i = 0; i < fragments.length; i++) {
                IFileFragment work = fragments[i];
                IVariableFragment shadow = work.getChild("shadow-" + i);
                Array shadowA = shadow.getArray();
                Assert.assertNotNull(shadowA);
                Assert.assertEquals(shadowA.getInt(0), 4);
                System.out.println("shadow-" + i + " = " + shadowA);
                for (int j = 0; j < 5; j++) {
                    IVariableFragment unique = work.getChild("unique-" + j);
                    Array a = unique.getArray();
                    Assert.assertNotNull(a);
                    Assert.assertEquals(a.getInt(0), i);
                    Assert.assertEquals(a.getInt(1), j);
                    System.out.println("unique-" + j + " = " + a);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(NetcdfDataSourceIntegrationTest.class.getName()).
                    log(Level.SEVERE, null, ex);
            Assert.fail(ex.getLocalizedMessage());
        }
//        FileFragment.clearFragments();
        sl.setLogLevel("cross.datastructures.fragments",
                "OFF");
        sl.setLogLevel("maltcms.io.andims", "INFO");
    }
}
