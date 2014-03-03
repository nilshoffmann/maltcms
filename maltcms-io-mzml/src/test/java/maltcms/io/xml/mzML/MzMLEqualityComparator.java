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
package maltcms.io.xml.mzML;

import java.io.File;
import java.util.List;
import junit.framework.Assert;
import uk.ac.ebi.jmzml.model.mzml.CV;
import uk.ac.ebi.jmzml.model.mzml.CVList;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.model.mzml.Chromatogram;
import uk.ac.ebi.jmzml.model.mzml.ChromatogramList;
import uk.ac.ebi.jmzml.model.mzml.DataProcessing;
import uk.ac.ebi.jmzml.model.mzml.DataProcessingList;
import uk.ac.ebi.jmzml.model.mzml.MzML;
import uk.ac.ebi.jmzml.model.mzml.ProcessingMethod;
import uk.ac.ebi.jmzml.model.mzml.ReferenceableParamGroup;
import uk.ac.ebi.jmzml.model.mzml.ReferenceableParamGroupList;
import uk.ac.ebi.jmzml.model.mzml.ReferenceableParamGroupRef;
import uk.ac.ebi.jmzml.model.mzml.Run;
import uk.ac.ebi.jmzml.model.mzml.Software;
import uk.ac.ebi.jmzml.model.mzml.SoftwareList;
import uk.ac.ebi.jmzml.model.mzml.UserParam;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;

/**
 * Provides comparison methods for mzml files. Testing consists of unmarshalling
 * mzMLFile1, then unmarshalling mzMLFile2 and comparing both unmarshalled MzML
 * objects.
 *
 * @author Nils Hoffmann
 */
public class MzMLEqualityComparator {

    public boolean equals(File mzMLFile1, File mzMLFile2) {
        MzMLUnmarshaller um1;
        um1 = new MzMLUnmarshaller(mzMLFile1);
        MzML m = um1.unmarshall();
        MzMLUnmarshaller um2 = new MzMLUnmarshaller(mzMLFile2);
        MzML rm = um2.unmarshall();
        Assert.assertTrue(equalsMzML(m, rm));
        return true;
    }

    protected boolean equalsMzML(MzML m1, MzML m2) {
        Assert.assertEquals(m1.getAccession(), m2.getAccession());
        Assert.assertEquals(m1.getId(), m2.getId());
        Assert.assertEquals(m1.getVersion(), m2.getVersion());
        Assert.assertTrue(equalsCVList(m1.getCvList(), m2.getCvList()));
        Assert.assertTrue(equalsDataProcessingList(m1.getDataProcessingList(), m2.getDataProcessingList()));
        //TODO implement comparison of remaining parts
        //m1.getFileDescription()
        //m1.getInstrumentConfigurationList()
        Assert.assertTrue(equalsReferenceableParamGroupList(m1.getReferenceableParamGroupList(), m2.getReferenceableParamGroupList()));
        Assert.assertTrue(equalsRun(m1.getRun(), m2.getRun()));
        //m1.getSampleList()
        //m1.getScanSettingsList()
        Assert.assertTrue(equalsSoftwareList(m1.getSoftwareList(), m2.getSoftwareList()));
        return true;
    }

    protected boolean equalsRun(Run c1, Run c2) {
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1 == null ^ c2 == null) {
            return false;
        }
        Assert.assertEquals(c1.getId(), c2.getId());
        Assert.assertTrue(equalsUserParamList(c1.getUserParam(), c2.getUserParam()));
        Assert.assertTrue(equalsCVParamList(c1.getCvParam(), c2.getCvParam()));
        Assert.assertTrue(equalsReferenceableParamGroupRefList(c1.getReferenceableParamGroupRef(), c2.getReferenceableParamGroupRef()));
        Assert.assertEquals(c1.getDefaultInstrumentConfigurationRef(), c2.getDefaultInstrumentConfigurationRef());
        Assert.assertEquals(c1.getDefaultSourceFileRef(), c2.getDefaultSourceFileRef());
        Assert.assertEquals(c1.getSampleRef(), c2.getSampleRef());
//		Assert.assertEquals(c1.getStartTimeStamp(), c2.getStartTimeStamp());
        Assert.assertTrue(equalsChromatogramList(c1.getChromatogramList(), c2.getChromatogramList()));
        //TODO
        //c1.getDefaultInstrumentConfiguration()
        //c1.getDefaultSourceFile()
        //c1.getSample()
        //c1.getSpectrumList()
        return true;
    }

    protected boolean equalsChromatogramList(ChromatogramList dpl1, ChromatogramList dpl2) {
        if (dpl1 == null && dpl2 == null) {
            return true;
        }
        if (dpl1 == null ^ dpl2 == null) {
            return false;
        }
        Assert.assertEquals(dpl1.getCount(), dpl2.getCount());
        Assert.assertEquals(dpl1.getDefaultDataProcessingRef(), dpl2.getDefaultDataProcessingRef());
        DataProcessing cv1 = dpl1.getDefaultDataProcessing();
        DataProcessing cv2 = dpl2.getDefaultDataProcessing();
        Assert.assertTrue(equalsDataProcessing(cv1, cv2));
        for (int i = 0; i < dpl1.getCount(); i++) {
            Chromatogram chrom1 = dpl1.getChromatogram().get(i);
            Chromatogram chrom2 = dpl2.getChromatogram().get(i);
        }
        return true;
    }

    protected boolean equalsChromatogram(Chromatogram c1, Chromatogram c2) {
        //TODO
        //c1.getBinaryDataArrayList()
        //c1.getDefaultArrayLength();
        //c1.getPrecursor()
        //c1.getProduct()
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1 == null ^ c2 == null) {
            return false;
        }
        Assert.assertEquals(c1.getId(), c2.getId());
        Assert.assertEquals(c1.getDataProcessingRef(), c2.getDataProcessingRef());
        Assert.assertEquals(c1.getReferenceableParamGroupRef(), c2.getReferenceableParamGroupRef());
        Assert.assertEquals(c1.getIndex(), c2.getIndex());
        Assert.assertTrue(equalsUserParamList(c1.getUserParam(), c2.getUserParam()));
        Assert.assertTrue(equalsCVParamList(c1.getCvParam(), c2.getCvParam()));
        Assert.assertTrue(equalsReferenceableParamGroupRefList(c1.getReferenceableParamGroupRef(), c2.getReferenceableParamGroupRef()));
        Assert.assertTrue(equalsDataProcessing(c1.getDataProcessing(), c2.getDataProcessing()));
        return true;
    }

    protected boolean equalsDataProcessingList(DataProcessingList dpl1, DataProcessingList dpl2) {
        if (dpl1 == null && dpl2 == null) {
            return true;
        }
        if (dpl1 == null ^ dpl2 == null) {
            return false;
        }
        Assert.assertEquals(dpl1.getCount(), dpl2.getCount());
        for (int i = 0; i < dpl1.getCount(); i++) {
            DataProcessing cv1 = dpl1.getDataProcessing().get(i);
            DataProcessing cv2 = dpl2.getDataProcessing().get(i);
            Assert.assertTrue(equalsDataProcessing(cv1, cv2));
        }
        return true;
    }

    protected boolean equalsDataProcessing(DataProcessing c1, DataProcessing c2) {
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1 == null ^ c2 == null) {
            return false;
        }
        Assert.assertEquals(c1.getId(), c2.getId());
        Assert.assertTrue(equalsProcessingMethodList(c1.getProcessingMethod(), c2.getProcessingMethod()));
        return true;
    }

    protected boolean equalsProcessingMethodList(List<ProcessingMethod> l1, List<ProcessingMethod> l2) {
        if (l1 == null && l2 == null) {
            return true;
        }
        if (l1 == null ^ l2 == null) {
            return false;
        }
        Assert.assertEquals(l1.size(), l2.size());
        for (int i = 0; i < l1.size(); i++) {
            ProcessingMethod pm1 = l1.get(i);
            ProcessingMethod pm2 = l2.get(i);
            Assert.assertTrue(equalsCVParamList(pm1.getCvParam(), pm2.getCvParam()));
            Assert.assertTrue(equalsUserParamList(pm1.getUserParam(), pm2.getUserParam()));
            Assert.assertTrue(equalsReferenceableParamGroupRefList(pm1.getReferenceableParamGroupRef(), pm2.getReferenceableParamGroupRef()));
            Assert.assertEquals(pm1.getSoftwareRef(), pm2.getSoftwareRef());
            Assert.assertTrue(equalsSoftware(pm1.getSoftware(), pm2.getSoftware()));
        }
        return true;
    }

    protected boolean equalsSoftwareList(SoftwareList c1, SoftwareList c2) {
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1 == null ^ c2 == null) {
            return false;
        }
        Assert.assertEquals(c1.getCount(), c2.getCount());
        List<Software> l1 = c1.getSoftware();
        List<Software> l2 = c2.getSoftware();
        Assert.assertEquals(l1.size(), l2.size());
        for (int i = 0; i < l1.size(); i++) {
            Software r1 = l1.get(i);
            Software r2 = l2.get(i);
            Assert.assertTrue(equalsSoftware(r1, r2));
        }
        return true;
    }

    protected boolean equalsSoftware(Software c1, Software c2) {
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1 == null ^ c2 == null) {
            return false;
        }
        Assert.assertEquals(c1.getId(), c2.getId());
        Assert.assertEquals(c1.getVersion(), c2.getVersion());
        Assert.assertTrue(equalsUserParamList(c1.getUserParam(), c2.getUserParam()));
        Assert.assertTrue(equalsCVParamList(c1.getCvParam(), c2.getCvParam()));
        Assert.assertTrue(equalsReferenceableParamGroupRefList(c1.getReferenceableParamGroupRef(), c2.getReferenceableParamGroupRef()));
        return true;
    }

    protected boolean equalsReferenceableParamGroupRefList(List<ReferenceableParamGroupRef> c1, List<ReferenceableParamGroupRef> c2) {
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1 == null ^ c2 == null) {
            return false;
        }
        Assert.assertEquals(c1.size(), c2.size());
        for (int i = 0; i < c1.size(); i++) {
            ReferenceableParamGroupRef r1 = c1.get(i);
            ReferenceableParamGroupRef r2 = c2.get(i);
            Assert.assertEquals(r1.getRef(), r2.getRef());
            Assert.assertTrue(equalsReferenceableParamGroup(r1.getReferenceableParamGroup(), r2.getReferenceableParamGroup()));
        }
        return true;
    }

    protected boolean equalsReferenceableParamGroupList(ReferenceableParamGroupList c1, ReferenceableParamGroupList c2) {
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1 == null ^ c2 == null) {
            return false;
        }
        Assert.assertEquals(c1.getCount(), c2.getCount());
        List<ReferenceableParamGroup> l1 = c1.getReferenceableParamGroup();
        List<ReferenceableParamGroup> l2 = c2.getReferenceableParamGroup();
        Assert.assertEquals(l1.size(), l2.size());
        for (int i = 0; i < l1.size(); i++) {
            ReferenceableParamGroup r1 = l1.get(i);
            ReferenceableParamGroup r2 = l2.get(i);
            Assert.assertTrue(equalsReferenceableParamGroup(r1, r2));
        }
        return true;
    }

    protected boolean equalsReferenceableParamGroup(ReferenceableParamGroup c1, ReferenceableParamGroup c2) {
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1 == null ^ c2 == null) {
            return false;
        }

        Assert.assertTrue(equalsCVParamList(c1.getCvParam(), c2.getCvParam()));
        Assert.assertEquals(c1.getId(), c2.getId());
        Assert.assertTrue(equalsUserParamList(c1.getUserParam(), c2.getUserParam()));
        return true;
    }

    protected boolean equalsCVList(CVList c1, CVList c2) {
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1 == null ^ c2 == null) {
            return false;
        }
        Assert.assertEquals(c1.getCount(), c2.getCount());
        for (int i = 0; i < c1.getCount(); i++) {
            CV cv1 = c1.getCv().get(i);
            CV cv2 = c2.getCv().get(i);
            if (cv1 != null && cv2 != null) {
                Assert.assertTrue(equalsCV(cv1, cv2));
            } else if (cv1 == null && cv2 == null) {
            } else {
                Assert.fail("One CV was non-null while the other was not!: " + cv1 + " " + cv2);
            }
        }
        return true;
    }

    protected boolean equalsCV(CV cv1, CV cv2) {
        if (cv1 == null && cv2 == null) {
            return true;
        }
        if (cv1 == null ^ cv2 == null) {
            return false;
        }
        Assert.assertEquals(cv1.getFullName(), cv2.getFullName());
        Assert.assertEquals(cv1.getId(), cv2.getId());
        Assert.assertEquals(cv1.getURI(), cv2.getURI());
        Assert.assertEquals(cv1.getVersion(), cv2.getVersion());
        return true;
    }

    protected boolean equalsUserParam(UserParam cv1, UserParam cv2) {
        if (cv1 == null && cv2 == null) {
            return true;
        }
        if (cv1 == null ^ cv2 == null) {
            return false;
        }
        Assert.assertEquals(cv1.getName(), cv2.getName());
        Assert.assertEquals(cv1.getType(), cv2.getType());
        Assert.assertEquals(cv1.getUnitAccession(), cv2.getUnitAccession());
        Assert.assertTrue(equalsCV(cv1.getUnitCv(), cv2.getUnitCv()));
        Assert.assertEquals(cv1.getUnitCvRef(), cv2.getUnitCvRef());
        Assert.assertEquals(cv1.getUnitName(), cv2.getUnitName());
        Assert.assertEquals(cv1.getValue(), cv2.getValue());
        return true;
    }

    protected boolean equalsCVParam(CVParam cv1, CVParam cv2) {
        if (cv1 == null && cv2 == null) {
            return true;
        }
        if (cv1 == null ^ cv2 == null) {
            return false;
        }
        Assert.assertEquals(cv1.getName(), cv2.getName());
        Assert.assertTrue(equalsCV(cv1.getCv(), cv2.getCv()));
        Assert.assertEquals(cv1.getUnitAccession(), cv2.getUnitAccession());
        Assert.assertTrue(equalsCV(cv1.getUnitCv(), cv2.getUnitCv()));
        Assert.assertEquals(cv1.getUnitCvRef(), cv2.getUnitCvRef());
        Assert.assertEquals(cv1.getUnitName(), cv2.getUnitName());
        Assert.assertEquals(cv1.getValue(), cv2.getValue());
        return true;
    }

    protected boolean equalsCVParamList(List<CVParam> c1, List<CVParam> c2) {
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1 == null ^ c2 == null) {
            return false;
        }
        Assert.assertEquals(c1.size(), c2.size());
        for (int i = 0; i < c1.size(); i++) {
            CVParam cv1 = c1.get(i);
            CVParam cv2 = c2.get(i);
            Assert.assertTrue(equalsCVParam(cv1, cv2));
        }
        return true;
    }

    protected boolean equalsUserParamList(List<UserParam> c1, List<UserParam> c2) {
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1 == null ^ c2 == null) {
            return false;
        }
        Assert.assertEquals(c1.size(), c2.size());
        for (int i = 0; i < c1.size(); i++) {
            UserParam cv1 = c1.get(i);
            UserParam cv2 = c2.get(i);
            Assert.assertTrue(equalsUserParam(cv1, cv2));
        }
        return true;
    }
}
