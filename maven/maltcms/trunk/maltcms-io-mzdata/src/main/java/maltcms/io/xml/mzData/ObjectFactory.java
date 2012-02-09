/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
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
 */
@XmlRegistry
public class ObjectFactory {

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package: maltcms.io.xml.mzData
	 * 
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link AdminType }
	 * 
	 */
	public AdminType createAdminType() {
		return new AdminType();
	}

	/**
	 * Create an instance of {@link CvLookupType }
	 * 
	 */
	public CvLookupType createCvLookupType() {
		return new CvLookupType();
	}

	/**
	 * Create an instance of {@link CvParamType }
	 * 
	 */
	public CvParamType createCvParamType() {
		return new CvParamType();
	}

	/**
	 * Create an instance of {@link DataProcessingType }
	 * 
	 */
	public DataProcessingType createDataProcessingType() {
		return new DataProcessingType();
	}

	/**
	 * Create an instance of {@link DataProcessingType.Software }
	 * 
	 */
	public DataProcessingType.Software createDataProcessingTypeSoftware() {
		return new DataProcessingType.Software();
	}

	/**
	 * Create an instance of {@link DescriptionType }
	 * 
	 */
	public DescriptionType createDescriptionType() {
		return new DescriptionType();
	}

	/**
	 * Create an instance of {@link InstrumentDescriptionType }
	 * 
	 */
	public InstrumentDescriptionType createInstrumentDescriptionType() {
		return new InstrumentDescriptionType();
	}

	/**
	 * Create an instance of {@link InstrumentDescriptionType.AnalyzerList }
	 * 
	 */
	public InstrumentDescriptionType.AnalyzerList createInstrumentDescriptionTypeAnalyzerList() {
		return new InstrumentDescriptionType.AnalyzerList();
	}

	/**
	 * Create an instance of {@link MzData }
	 * 
	 */
	public MzData createMzData() {
		return new MzData();
	}

	/**
	 * Create an instance of {@link MzData.Description }
	 * 
	 */
	public MzData.Description createMzDataDescription() {
		return new MzData.Description();
	}

	/**
	 * Create an instance of {@link MzData.SpectrumList }
	 * 
	 */
	public MzData.SpectrumList createMzDataSpectrumList() {
		return new MzData.SpectrumList();
	}

	/**
	 * Create an instance of {@link MzData.SpectrumList.Spectrum }
	 * 
	 */
	public MzData.SpectrumList.Spectrum createMzDataSpectrumListSpectrum() {
		return new MzData.SpectrumList.Spectrum();
	}

	/**
	 * Create an instance of {@link ParamType }
	 * 
	 */
	public ParamType createParamType() {
		return new ParamType();
	}

	/**
	 * Create an instance of {@link PeakListBinaryType }
	 * 
	 */
	public PeakListBinaryType createPeakListBinaryType() {
		return new PeakListBinaryType();
	}

	/**
	 * Create an instance of {@link PeakListBinaryType.Data }
	 * 
	 */
	public PeakListBinaryType.Data createPeakListBinaryTypeData() {
		return new PeakListBinaryType.Data();
	}

	/**
	 * Create an instance of {@link PersonType }
	 * 
	 */
	public PersonType createPersonType() {
		return new PersonType();
	}

	/**
	 * Create an instance of {@link PrecursorType }
	 * 
	 */
	public PrecursorType createPrecursorType() {
		return new PrecursorType();
	}

	/**
	 * Create an instance of {@link SoftwareType }
	 * 
	 */
	public SoftwareType createSoftwareType() {
		return new SoftwareType();
	}

	/**
	 * Create an instance of {@link SourceFileType }
	 * 
	 */
	public SourceFileType createSourceFileType() {
		return new SourceFileType();
	}

	/**
	 * Create an instance of {@link SpectrumDescType }
	 * 
	 */
	public SpectrumDescType createSpectrumDescType() {
		return new SpectrumDescType();
	}

	/**
	 * Create an instance of {@link SpectrumDescType.PrecursorList }
	 * 
	 */
	public SpectrumDescType.PrecursorList createSpectrumDescTypePrecursorList() {
		return new SpectrumDescType.PrecursorList();
	}

	/**
	 * Create an instance of {@link SpectrumSettingsType }
	 * 
	 */
	public SpectrumSettingsType createSpectrumSettingsType() {
		return new SpectrumSettingsType();
	}

	/**
	 * Create an instance of {@link SpectrumSettingsType.AcqSpecification }
	 * 
	 */
	public SpectrumSettingsType.AcqSpecification createSpectrumSettingsTypeAcqSpecification() {
		return new SpectrumSettingsType.AcqSpecification();
	}

	/**
	 * Create an instance of
	 * {@link SpectrumSettingsType.AcqSpecification.Acquisition }
	 * 
	 */
	public SpectrumSettingsType.AcqSpecification.Acquisition createSpectrumSettingsTypeAcqSpecificationAcquisition() {
		return new SpectrumSettingsType.AcqSpecification.Acquisition();
	}

	/**
	 * Create an instance of {@link SpectrumSettingsType.SpectrumInstrument }
	 * 
	 */
	public SpectrumSettingsType.SpectrumInstrument createSpectrumSettingsTypeSpectrumInstrument() {
		return new SpectrumSettingsType.SpectrumInstrument();
	}

	/**
	 * Create an instance of {@link SpectrumType }
	 * 
	 */
	public SpectrumType createSpectrumType() {
		return new SpectrumType();
	}

	/**
	 * Create an instance of {@link SupDataBinaryType }
	 * 
	 */
	public SupDataBinaryType createSupDataBinaryType() {
		return new SupDataBinaryType();
	}

	/**
	 * Create an instance of {@link SupDataType }
	 * 
	 */
	public SupDataType createSupDataType() {
		return new SupDataType();
	}

	/**
	 * Create an instance of {@link SupDescType }
	 * 
	 */
	public SupDescType createSupDescType() {
		return new SupDescType();
	}

	/**
	 * Create an instance of {@link UserParamType }
	 * 
	 */
	public UserParamType createUserParamType() {
		return new UserParamType();
	}

}
