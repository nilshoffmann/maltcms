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
package maltcms.io.andims;

import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.ImmutableFileFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.exception.ResourceNotAvailableException;
import cross.test.IntegrationTest;
import cross.test.LogMethodName;
import cross.test.SetupLogging;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ucar.ma2.Array;
import ucar.nc2.Dimension;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j

public class NetcdfDataSourceIT implements IntegrationTest {

    @Rule
    public SetupLogging sl = new SetupLogging();
    @Rule
    public LogMethodName lmn = new LogMethodName();
    @Rule
    public TemporaryFolder tf = new TemporaryFolder();
    IFileFragment remoteFileFragment = null;

    @Before
    public void extractTestData() {
        if (remoteFileFragment == null) {
            remoteFileFragment = new FileFragment(URI.create("http://bibiserv.techfak.uni-bielefeld.de/chroma/data/glucoseA.cdf"));
            remoteFileFragment.readStructure();
        }
    }

    /**
     * Test of indirectly reading data from a remote file fragment via a local
     * chain of file fragments.
     */
    @Test
    public void testIndirectRemoteChainedRead() {
        try {
            FileFragment localProxy = new FileFragment(tf.newFolder(), "localProxy.cdf");
            localProxy.addSourceFile(remoteFileFragment);
            Array total_intensityArray = remoteFileFragment.getChild("total_intensity", true).getArray();
            VariableFragment tic = new VariableFragment(localProxy, "total_intensity");
            tic.setArray(total_intensityArray);
            tic.setDimensions(remoteFileFragment.getChild("total_intensity").getDimensions());
            localProxy.save();
            FileFragment localProxy2 = new FileFragment(tf.newFolder(), "localProxy2.cdf");
            localProxy2.addSourceFile(new FileFragment(localProxy.getUri()));
            localProxy2.save();
            testIndirectRemoteRead(localProxy2.getUri());
        } catch (IOException | ResourceNotAvailableException ex) {
            Logger.getLogger(NetcdfDataSourceIT.class.getName()).log(Level.SEVERE, null, ex);
            Assert.fail(ex.getLocalizedMessage());
        }
    }

    public IFileFragment testDirectRemoteRead(URI testCdf) throws ResourceNotAvailableException {
        log.info("Tesing direct remote read!");
        //read in the created file
        IFileFragment readFragment = new FileFragment(testCdf);
        try {
            //initialize fragment with content
            new NetcdfDataSource().readStructure(readFragment);
        } catch (IOException ex) {
            Logger.getLogger(NetcdfDataSourceTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        //check global attributes
        Assert.assertEquals(readFragment.getAttribute("netcdf_revision").getStringValue(), "2.3.2");
        Assert.assertEquals(readFragment.getAttribute("number_of_scans").getNumericValue(), 5398);
        //check variables
        IVariableFragment rivf1 = readFragment.getChild("total_intensity");
        Dimension[] rdims1 = rivf1.getDimensions();
        Dimension[] dims1 = new Dimension[]{new Dimension("scan_number", 5398)};
        //compare dimensions
        for (int i = 0; i < dims1.length; i++) {
            Dimension left = dims1[i];
            Dimension right = rdims1[i];
            Assert.assertEquals(left, right);
        }

        IVariableFragment scanIndex = readFragment.getChild("scan_index");
        IVariableFragment massValues = readFragment.getChild("mass_values");
        massValues.setIndex(scanIndex);
        List<Array> masses = massValues.getIndexedArray();
        for (int i = 0; i < 10; i++) {
            Array a = masses.get(i);
            log.info("Masses: {}", a);
        }
        return readFragment;
    }

    public IFileFragment testIndirectRemoteRead(URI testCdf) throws ResourceNotAvailableException {
        log.info("Testing indirect remote read!");
        IFileFragment readFragment = new ImmutableFileFragment(new FileFragment(testCdf));
        IVariableFragment sourceFiles = readFragment.getChild("source_files");
        log.info("SourceFiles for " + testCdf.toString() + ": " + sourceFiles.getArray());
        //check variables
        IVariableFragment rivf1 = readFragment.getChild("total_intensity");
        Dimension[] rdims1 = rivf1.getDimensions();
        Dimension[] dims1 = new Dimension[]{new Dimension("scan_number", 5398)};
        //compare dimensions
        for (int i = 0; i < dims1.length; i++) {
            Dimension left = dims1[i];
            Dimension right = rdims1[i];
            Assert.assertEquals(left, right);
        }

        IVariableFragment scanIndex = readFragment.getChild("scan_index");
        IVariableFragment massValues = readFragment.getChild("mass_values");
        massValues.setIndex(scanIndex);
        List<Array> masses = massValues.getIndexedArray();
        for (int i = 0; i < 10; i++) {
            Array a = masses.get(i);
            log.info("Masses: {}", a);
        }
        return readFragment;
    }
}
