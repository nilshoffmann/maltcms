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

import cross.datastructures.fragments.IVariableFragment;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 *
 * @author hoffmann
 */
public class NetcdfJavaTest {

    /**
     *
     */
    @Rule
    public TemporaryFolder tf = new TemporaryFolder();

    /**
     *
     */
    @Test
    public void testWriteRead() {
        File outputFolder = tf.newFolder("testOutput");
        File testCdf = new File(outputFolder, "testCdf.cdf");
        try {
            NetcdfFileWriteable nfw = NetcdfFileWriteable.createNew(testCdf.getAbsolutePath());

            Attribute a1 = new Attribute("software", "maltcms");
            Attribute a2 = new Attribute("purpose", "testing");
            Attribute a3 = new Attribute("version", Integer.valueOf(1));
            nfw.addGlobalAttribute(a1);
            nfw.addGlobalAttribute(a2);
            nfw.addGlobalAttribute(a3);

            Map<String, Dimension> dimensions = new HashMap<String, Dimension>();
            dimensions.put("dim1", new Dimension("dim1", 15));
            dimensions.put("dim2", new Dimension("dim2", 8));
            dimensions.put("dim3", new Dimension("dim3", 10));
            Dimension dim1 = dimensions.get("dim1");
            Dimension dim2 = dimensions.get("dim2");
            Variable ivf1 = nfw.addVariable("variable1", DataType.DOUBLE, new Dimension[]{dim1, dim2});
            nfw.addDimension(nfw.getRootGroup(), dim1);
            nfw.addDimension(nfw.getRootGroup(), dim2);
//            nfw.addDimension(nfw.getRootGroup(), dimensions.get("dim1"));
//            nfw.addDimension(nfw.getRootGroup(), dimensions.get("dim2"));
//            nfw.addDimension(nfw.getRootGroup(), dimensions.get("dim3"));
            ivf1.addAttribute(new Attribute("description", "two-dimensional array"));
            ArrayDouble.D2 arr1 = new ArrayDouble.D2(dim1.getLength(), dim2.getLength());
            Dimension dim3 = dimensions.get("dim3");
            Variable ivf2 = nfw.addVariable("variable2", DataType.INT, new Dimension[]{dim3});
            nfw.addDimension(nfw.getRootGroup(), dim3);
            ArrayInt.D1 arr2 = new ArrayInt.D1(dim3.getLength());
            //unused dimension 
            Dimension dim4 = new Dimension("dim4", 214);
            nfw.addDimension(nfw.getRootGroup(), dim4);

            nfw.create();
            try {
                nfw.write(ivf1.getFullNameEscaped(), arr1);
                nfw.write(ivf2.getFullNameEscaped(), arr2);
                nfw.close();
            } catch (InvalidRangeException ex) {
                Logger.getLogger(NetcdfJavaTest.class.getName()).log(Level.SEVERE, null, ex);
                Assert.fail(ex.getLocalizedMessage());
            }

            NetcdfFile readFile = NetcdfFile.open(testCdf.getAbsolutePath());
            Attribute software = readFile.findGlobalAttribute("software");
            Attribute purpose = readFile.findGlobalAttribute("purpose");
            Attribute version = readFile.findGlobalAttribute("version");
            //check global attributes
            Assert.assertEquals(a1, software);
            Assert.assertEquals(a2, purpose);
            Assert.assertEquals(a3, version);
            //check variables
            Variable rivf1 = readFile.findVariable("variable1");
            List<Dimension> rdims1 = rivf1.getDimensions();
            List<Dimension> dims1 = ivf1.getDimensions();
            //compare dimensions
            for (int i = 0; i < dims1.size(); i++) {
                Dimension left = dims1.get(i);
                Dimension right = rdims1.get(i);
                Assert.assertEquals(left, right);
            }
            IndexIterator ii1 = arr1.getIndexIterator();
            IndexIterator rii1 = rivf1.read().getIndexIterator();
            Assert.assertEquals(arr1.getShape()[0], rivf1.read().getShape()[0]);
            Assert.assertEquals(arr1.getShape()[1], rivf1.read().getShape()[1]);
            while (ii1.hasNext() && rii1.hasNext()) {
                Assert.assertEquals(ii1.getDoubleNext(), rii1.getDoubleNext());
            }
            Assert.assertEquals(rivf1.findAttribute("description").getStringValue(), "two-dimensional array");

            //check next variable
            Variable rivf2 = readFile.findVariable("variable2");
            List<Dimension> rdims2 = rivf2.getDimensions();
            List<Dimension> dims2 = ivf2.getDimensions();
            //compare dimensions
            for (int i = 0; i < dims2.size(); i++) {
                Dimension left = dims2.get(i);
                Dimension right = rdims2.get(i);
                Assert.assertEquals(left, right);
            }
            IndexIterator ii2 = arr2.getIndexIterator();
            IndexIterator rii2 = rivf2.read().getIndexIterator();
            Assert.assertEquals(arr2.getShape()[0], rivf2.read().getShape()[0]);
            while (ii2.hasNext() && rii2.hasNext()) {
                Assert.assertEquals(ii2.getDoubleNext(), rii2.getDoubleNext());
            }

            Dimension dr4 = readFile.findDimension(dim4.getName());
            Assert.assertNotNull(dr4);
        } catch (IOException ex) {
            Logger.getLogger(NetcdfJavaTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
