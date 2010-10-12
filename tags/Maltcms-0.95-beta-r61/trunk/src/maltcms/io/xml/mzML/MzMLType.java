/*
 * $Id$
 */

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB)
// Reference Implementation, vhudson-jaxb-ri-2.1-646
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source
// schema.
// Generated on: 2008.10.14 at 08:55:16 AM CEST
//

package maltcms.io.xml.mzML;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This is the root element for the Proteomics Standards Initiative (PSI) mzML
 * schema, which is intended to capture the use of a mass spectrometer, the data
 * generated, and the initial processing of that data (to the level of the peak
 * list).
 * 
 * <p>
 * Java class for mzMLType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name=&quot;mzMLType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;cvList&quot; type=&quot;{http://psi.hupo.org/schema_revision/mzML_1.0.0}CVListType&quot;/&gt;
 *         &lt;element name=&quot;fileDescription&quot; type=&quot;{http://psi.hupo.org/schema_revision/mzML_1.0.0}FileDescriptionType&quot;/&gt;
 *         &lt;element name=&quot;referenceableParamGroupList&quot; type=&quot;{http://psi.hupo.org/schema_revision/mzML_1.0.0}ReferenceableParamGroupListType&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;sampleList&quot; type=&quot;{http://psi.hupo.org/schema_revision/mzML_1.0.0}SampleListType&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;instrumentConfigurationList&quot; type=&quot;{http://psi.hupo.org/schema_revision/mzML_1.0.0}InstrumentConfigurationListType&quot;/&gt;
 *         &lt;element name=&quot;softwareList&quot; type=&quot;{http://psi.hupo.org/schema_revision/mzML_1.0.0}SoftwareListType&quot;/&gt;
 *         &lt;element name=&quot;dataProcessingList&quot; type=&quot;{http://psi.hupo.org/schema_revision/mzML_1.0.0}DataProcessingListType&quot;/&gt;
 *         &lt;element name=&quot;acquisitionSettingsList&quot; type=&quot;{http://psi.hupo.org/schema_revision/mzML_1.0.0}AcquisitionSettingsListType&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;run&quot; type=&quot;{http://psi.hupo.org/schema_revision/mzML_1.0.0}RunType&quot;/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name=&quot;accession&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;version&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;id&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mzMLType", propOrder = { "cvList", "fileDescription",
        "referenceableParamGroupList", "sampleList",
        "instrumentConfigurationList", "softwareList", "dataProcessingList",
        "acquisitionSettingsList", "run" })
public class MzMLType {

	@XmlElement(required = true)
	protected CVListType cvList;
	@XmlElement(required = true)
	protected FileDescriptionType fileDescription;
	protected ReferenceableParamGroupListType referenceableParamGroupList;
	protected SampleListType sampleList;
	@XmlElement(required = true)
	protected InstrumentConfigurationListType instrumentConfigurationList;
	@XmlElement(required = true)
	protected SoftwareListType softwareList;
	@XmlElement(required = true)
	protected DataProcessingListType dataProcessingList;
	protected AcquisitionSettingsListType acquisitionSettingsList;
	@XmlElement(required = true)
	protected RunType run;
	@XmlAttribute
	protected String accession;
	@XmlAttribute(required = true)
	protected String version;
	@XmlAttribute
	protected String id;

	/**
	 * Gets the value of the accession property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getAccession() {
		return this.accession;
	}

	/**
	 * Gets the value of the acquisitionSettingsList property.
	 * 
	 * @return possible object is {@link AcquisitionSettingsListType }
	 * 
	 */
	public AcquisitionSettingsListType getAcquisitionSettingsList() {
		return this.acquisitionSettingsList;
	}

	/**
	 * Gets the value of the cvList property.
	 * 
	 * @return possible object is {@link CVListType }
	 * 
	 */
	public CVListType getCvList() {
		return this.cvList;
	}

	/**
	 * Gets the value of the dataProcessingList property.
	 * 
	 * @return possible object is {@link DataProcessingListType }
	 * 
	 */
	public DataProcessingListType getDataProcessingList() {
		return this.dataProcessingList;
	}

	/**
	 * Gets the value of the fileDescription property.
	 * 
	 * @return possible object is {@link FileDescriptionType }
	 * 
	 */
	public FileDescriptionType getFileDescription() {
		return this.fileDescription;
	}

	/**
	 * Gets the value of the id property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Gets the value of the instrumentConfigurationList property.
	 * 
	 * @return possible object is {@link InstrumentConfigurationListType }
	 * 
	 */
	public InstrumentConfigurationListType getInstrumentConfigurationList() {
		return this.instrumentConfigurationList;
	}

	/**
	 * Gets the value of the referenceableParamGroupList property.
	 * 
	 * @return possible object is {@link ReferenceableParamGroupListType }
	 * 
	 */
	public ReferenceableParamGroupListType getReferenceableParamGroupList() {
		return this.referenceableParamGroupList;
	}

	/**
	 * Gets the value of the run property.
	 * 
	 * @return possible object is {@link RunType }
	 * 
	 */
	public RunType getRun() {
		return this.run;
	}

	/**
	 * Gets the value of the sampleList property.
	 * 
	 * @return possible object is {@link SampleListType }
	 * 
	 */
	public SampleListType getSampleList() {
		return this.sampleList;
	}

	/**
	 * Gets the value of the softwareList property.
	 * 
	 * @return possible object is {@link SoftwareListType }
	 * 
	 */
	public SoftwareListType getSoftwareList() {
		return this.softwareList;
	}

	/**
	 * Gets the value of the version property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getVersion() {
		return this.version;
	}

	/**
	 * Sets the value of the accession property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setAccession(final String value) {
		this.accession = value;
	}

	/**
	 * Sets the value of the acquisitionSettingsList property.
	 * 
	 * @param value
	 *            allowed object is {@link AcquisitionSettingsListType }
	 * 
	 */
	public void setAcquisitionSettingsList(
	        final AcquisitionSettingsListType value) {
		this.acquisitionSettingsList = value;
	}

	/**
	 * Sets the value of the cvList property.
	 * 
	 * @param value
	 *            allowed object is {@link CVListType }
	 * 
	 */
	public void setCvList(final CVListType value) {
		this.cvList = value;
	}

	/**
	 * Sets the value of the dataProcessingList property.
	 * 
	 * @param value
	 *            allowed object is {@link DataProcessingListType }
	 * 
	 */
	public void setDataProcessingList(final DataProcessingListType value) {
		this.dataProcessingList = value;
	}

	/**
	 * Sets the value of the fileDescription property.
	 * 
	 * @param value
	 *            allowed object is {@link FileDescriptionType }
	 * 
	 */
	public void setFileDescription(final FileDescriptionType value) {
		this.fileDescription = value;
	}

	/**
	 * Sets the value of the id property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setId(final String value) {
		this.id = value;
	}

	/**
	 * Sets the value of the instrumentConfigurationList property.
	 * 
	 * @param value
	 *            allowed object is {@link InstrumentConfigurationListType }
	 * 
	 */
	public void setInstrumentConfigurationList(
	        final InstrumentConfigurationListType value) {
		this.instrumentConfigurationList = value;
	}

	/**
	 * Sets the value of the referenceableParamGroupList property.
	 * 
	 * @param value
	 *            allowed object is {@link ReferenceableParamGroupListType }
	 * 
	 */
	public void setReferenceableParamGroupList(
	        final ReferenceableParamGroupListType value) {
		this.referenceableParamGroupList = value;
	}

	/**
	 * Sets the value of the run property.
	 * 
	 * @param value
	 *            allowed object is {@link RunType }
	 * 
	 */
	public void setRun(final RunType value) {
		this.run = value;
	}

	/**
	 * Sets the value of the sampleList property.
	 * 
	 * @param value
	 *            allowed object is {@link SampleListType }
	 * 
	 */
	public void setSampleList(final SampleListType value) {
		this.sampleList = value;
	}

	/**
	 * Sets the value of the softwareList property.
	 * 
	 * @param value
	 *            allowed object is {@link SoftwareListType }
	 * 
	 */
	public void setSoftwareList(final SoftwareListType value) {
		this.softwareList = value;
	}

	/**
	 * Sets the value of the version property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setVersion(final String value) {
		this.version = value;
	}

}