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
import javax.xml.bind.annotation.XmlType;

/**
 * Description of the parameters for the mass spectrometer for a given
 * acquisition (or list of acquisitions).
 * 
 * <p>
 * Java class for SpectrumDescriptionType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name=&quot;SpectrumDescriptionType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base=&quot;{http://psi.hupo.org/schema_revision/mzML_1.0.0}ParamGroupType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;acquisitionList&quot; type=&quot;{http://psi.hupo.org/schema_revision/mzML_1.0.0}AcquisitionListType&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;precursorList&quot; type=&quot;{http://psi.hupo.org/schema_revision/mzML_1.0.0}PrecursorListType&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;scan&quot; type=&quot;{http://psi.hupo.org/schema_revision/mzML_1.0.0}ScanType&quot; minOccurs=&quot;0&quot;/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpectrumDescriptionType", propOrder = { "acquisitionList",
        "precursorList", "scan" })
public class SpectrumDescriptionType extends ParamGroupType {

	protected AcquisitionListType acquisitionList;
	protected PrecursorListType precursorList;
	protected ScanType scan;

	/**
	 * Gets the value of the acquisitionList property.
	 * 
	 * @return possible object is {@link AcquisitionListType }
	 * 
	 */
	public AcquisitionListType getAcquisitionList() {
		return this.acquisitionList;
	}

	/**
	 * Gets the value of the precursorList property.
	 * 
	 * @return possible object is {@link PrecursorListType }
	 * 
	 */
	public PrecursorListType getPrecursorList() {
		return this.precursorList;
	}

	/**
	 * Gets the value of the scan property.
	 * 
	 * @return possible object is {@link ScanType }
	 * 
	 */
	public ScanType getScan() {
		return this.scan;
	}

	/**
	 * Sets the value of the acquisitionList property.
	 * 
	 * @param value
	 *            allowed object is {@link AcquisitionListType }
	 * 
	 */
	public void setAcquisitionList(final AcquisitionListType value) {
		this.acquisitionList = value;
	}

	/**
	 * Sets the value of the precursorList property.
	 * 
	 * @param value
	 *            allowed object is {@link PrecursorListType }
	 * 
	 */
	public void setPrecursorList(final PrecursorListType value) {
		this.precursorList = value;
	}

	/**
	 * Sets the value of the scan property.
	 * 
	 * @param value
	 *            allowed object is {@link ScanType }
	 * 
	 */
	public void setScan(final ScanType value) {
		this.scan = value;
	}

}
