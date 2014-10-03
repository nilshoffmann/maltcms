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
package maltcms.io.xml.mzData;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the maltcms.io.xml.mzData package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups.
 * ArrayFactory methods for each of these are provided in this class.
 *
 * @author hoffmann
 * 
 */
@XmlRegistry
public class ObjectFactory {

    /**
     * Create a new ObjectFactory that can be used to create new instances of
     * schema derived classes for package: maltcms.io.xml.mzData
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.AdminType}
     *
     * @return a {@link maltcms.io.xml.mzData.AdminType} object.
     */
    public AdminType createAdminType() {
        return new AdminType();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.CvLookupType}
     *
     * @return a {@link maltcms.io.xml.mzData.CvLookupType} object.
     */
    public CvLookupType createCvLookupType() {
        return new CvLookupType();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.CvParamType}
     *
     * @return a {@link maltcms.io.xml.mzData.CvParamType} object.
     */
    public CvParamType createCvParamType() {
        return new CvParamType();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.DataProcessingType}
     *
     * @return a {@link maltcms.io.xml.mzData.DataProcessingType} object.
     */
    public DataProcessingType createDataProcessingType() {
        return new DataProcessingType();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.DataProcessingType.Software}
     *
     * @return a {@link maltcms.io.xml.mzData.DataProcessingType.Software} object.
     */
    public DataProcessingType.Software createDataProcessingTypeSoftware() {
        return new DataProcessingType.Software();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.DescriptionType}
     *
     * @return a {@link maltcms.io.xml.mzData.DescriptionType} object.
     */
    public DescriptionType createDescriptionType() {
        return new DescriptionType();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.InstrumentDescriptionType}
     *
     * @return a {@link maltcms.io.xml.mzData.InstrumentDescriptionType} object.
     */
    public InstrumentDescriptionType createInstrumentDescriptionType() {
        return new InstrumentDescriptionType();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.InstrumentDescriptionType.AnalyzerList}
     *
     * @return a {@link maltcms.io.xml.mzData.InstrumentDescriptionType.AnalyzerList} object.
     */
    public InstrumentDescriptionType.AnalyzerList createInstrumentDescriptionTypeAnalyzerList() {
        return new InstrumentDescriptionType.AnalyzerList();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.MzData}
     *
     * @return a {@link maltcms.io.xml.mzData.MzData} object.
     */
    public MzData createMzData() {
        return new MzData();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.MzData.Description}
     *
     * @return a {@link maltcms.io.xml.mzData.MzData.Description} object.
     */
    public MzData.Description createMzDataDescription() {
        return new MzData.Description();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.MzData.SpectrumList}
     *
     * @return a {@link maltcms.io.xml.mzData.MzData.SpectrumList} object.
     */
    public MzData.SpectrumList createMzDataSpectrumList() {
        return new MzData.SpectrumList();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.MzData.SpectrumList.Spectrum}
     *
     * @return a {@link maltcms.io.xml.mzData.MzData.SpectrumList.Spectrum} object.
     */
    public MzData.SpectrumList.Spectrum createMzDataSpectrumListSpectrum() {
        return new MzData.SpectrumList.Spectrum();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.ParamType}
     *
     * @return a {@link maltcms.io.xml.mzData.ParamType} object.
     */
    public ParamType createParamType() {
        return new ParamType();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.PeakListBinaryType}
     *
     * @return a {@link maltcms.io.xml.mzData.PeakListBinaryType} object.
     */
    public PeakListBinaryType createPeakListBinaryType() {
        return new PeakListBinaryType();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.PeakListBinaryType.Data}
     *
     * @return a {@link maltcms.io.xml.mzData.PeakListBinaryType.Data} object.
     */
    public PeakListBinaryType.Data createPeakListBinaryTypeData() {
        return new PeakListBinaryType.Data();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.PersonType}
     *
     * @return a {@link maltcms.io.xml.mzData.PersonType} object.
     */
    public PersonType createPersonType() {
        return new PersonType();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.PrecursorType}
     *
     * @return a {@link maltcms.io.xml.mzData.PrecursorType} object.
     */
    public PrecursorType createPrecursorType() {
        return new PrecursorType();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.SoftwareType}
     *
     * @return a {@link maltcms.io.xml.mzData.SoftwareType} object.
     */
    public SoftwareType createSoftwareType() {
        return new SoftwareType();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.SourceFileType}
     *
     * @return a {@link maltcms.io.xml.mzData.SourceFileType} object.
     */
    public SourceFileType createSourceFileType() {
        return new SourceFileType();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.SpectrumDescType}
     *
     * @return a {@link maltcms.io.xml.mzData.SpectrumDescType} object.
     */
    public SpectrumDescType createSpectrumDescType() {
        return new SpectrumDescType();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.SpectrumDescType.PrecursorList}
     *
     * @return a {@link maltcms.io.xml.mzData.SpectrumDescType.PrecursorList} object.
     */
    public SpectrumDescType.PrecursorList createSpectrumDescTypePrecursorList() {
        return new SpectrumDescType.PrecursorList();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.SpectrumSettingsType}
     *
     * @return a {@link maltcms.io.xml.mzData.SpectrumSettingsType} object.
     */
    public SpectrumSettingsType createSpectrumSettingsType() {
        return new SpectrumSettingsType();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.SpectrumSettingsType.AcqSpecification}
     *
     * @return a {@link maltcms.io.xml.mzData.SpectrumSettingsType.AcqSpecification} object.
     */
    public SpectrumSettingsType.AcqSpecification createSpectrumSettingsTypeAcqSpecification() {
        return new SpectrumSettingsType.AcqSpecification();
    }

    /**
     * Create an instance of
     * {@link maltcms.io.xml.mzData.SpectrumSettingsType.AcqSpecification.Acquisition}
     *
     * @return a {@link maltcms.io.xml.mzData.SpectrumSettingsType.AcqSpecification.Acquisition} object.
     */
    public SpectrumSettingsType.AcqSpecification.Acquisition createSpectrumSettingsTypeAcqSpecificationAcquisition() {
        return new SpectrumSettingsType.AcqSpecification.Acquisition();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.SpectrumSettingsType.SpectrumInstrument}
     *
     * @return a {@link maltcms.io.xml.mzData.SpectrumSettingsType.SpectrumInstrument} object.
     */
    public SpectrumSettingsType.SpectrumInstrument createSpectrumSettingsTypeSpectrumInstrument() {
        return new SpectrumSettingsType.SpectrumInstrument();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.SpectrumType}
     *
     * @return a {@link maltcms.io.xml.mzData.SpectrumType} object.
     */
    public SpectrumType createSpectrumType() {
        return new SpectrumType();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.SupDataBinaryType}
     *
     * @return a {@link maltcms.io.xml.mzData.SupDataBinaryType} object.
     */
    public SupDataBinaryType createSupDataBinaryType() {
        return new SupDataBinaryType();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.SupDataType}
     *
     * @return a {@link maltcms.io.xml.mzData.SupDataType} object.
     */
    public SupDataType createSupDataType() {
        return new SupDataType();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.SupDescType}
     *
     * @return a {@link maltcms.io.xml.mzData.SupDescType} object.
     */
    public SupDescType createSupDescType() {
        return new SupDescType();
    }

    /**
     * Create an instance of {@link maltcms.io.xml.mzData.UserParamType}
     *
     * @return a {@link maltcms.io.xml.mzData.UserParamType} object.
     */
    public UserParamType createUserParamType() {
        return new UserParamType();
    }
}
