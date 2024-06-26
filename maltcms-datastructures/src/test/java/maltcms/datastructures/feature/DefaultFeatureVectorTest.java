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
package maltcms.datastructures.feature;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;
import maltcms.tools.ArrayTools;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */
public class DefaultFeatureVectorTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Test of externalization of class Scan1D.
     */
    @Test
    public void testReadWriteExternal() throws Exception {
        DefaultFeatureVector dfv = new DefaultFeatureVector();
        UUID uid = dfv.getUniqueId();
        Array a = ArrayTools.randomGaussian(10, 0, 50);
        Assert.assertNotNull(a);
        Array b = ArrayTools.randomUniform(10, 0, 50);
        Assert.assertNotNull(b);
        dfv.addFeature("featureA", a);
        dfv.addFeature("featureB", b);
        File f = temporaryFolder.newFile();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(dfv);
        } catch (IOException ioex) {
            throw ioex;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            DefaultFeatureVector o = (DefaultFeatureVector) ois.readObject();
            Assert.assertEquals(dfv.getFeatureNames().size(), o.getFeatureNames().size());
            for (String feature : o.getFeatureNames()) {
                Assert.assertNotNull(o.getFeature(feature));
//                Assert.assertEquals(dfv.getFeature(feature), o.getFeature(feature));
            }
            Assert.assertEquals(uid, o.getUniqueId());
        } catch (IOException ioex) {
            throw ioex;
        }
    }

}
