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
package maltcms.io.xml.mzML;

import cross.Factory;
import cross.cache.CacheType;
import cross.datastructures.fragments.Fragments;
import cross.test.LogMethodName;
import cross.test.SetupLogging;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;
import maltcms.test.ExtractClassPathFiles;
import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import uk.ac.ebi.jmzml.model.mzml.MzML;
import uk.ac.ebi.jmzml.xml.io.MzMLMarshaller;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class MZMLTest {

	@Rule
	public SetupLogging sl = new SetupLogging();
	@Rule
	public LogMethodName lmn = new LogMethodName();
	@Rule
	public TemporaryFolder tf = new TemporaryFolder();
	@Rule
	public ExtractClassPathFiles ecpf = new ExtractClassPathFiles(tf, "/mzML/small.pwiz.1.1.mzML.gz", "/mzML/tiny.pwiz.1.1.mzML.gz", "/mzML/MzMLFile_PDA.mzML.xml.gz");

	MZMLDataSource getDataSource() {
		return new MZMLDataSource();
	}

	/**
	 *
	 */
	@Before
	public void setUp() {
		Factory.getInstance().getDataSourceFactory().setDataSources(Arrays.asList(MZMLDataSource.class.getCanonicalName()));
		Fragments.setDefaultFragmentCacheType(CacheType.NONE);
	}

	/**
	 * Test round trip behaviour of jmzml library.
	 */
	@Test
	public void testRoundTripReadWriteRead() {
		for (File f : ecpf.getFiles()) {
			MzMLUnmarshaller um1;
			um1 = new MzMLUnmarshaller(f);
			MzML m = um1.unmarshall();
			File testFile = null;
			try {
				testFile = tf.newFile();
				MzMLMarshaller marshaller = new MzMLMarshaller();
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(testFile));
				marshaller.marshall(m, bos);
				Assert.assertTrue(new MzMLEqualityComparator().equals(f, testFile));
			} catch (IOException ex) {
				log.error("Failed to create test file:", ex);
			}
		}
	}
}