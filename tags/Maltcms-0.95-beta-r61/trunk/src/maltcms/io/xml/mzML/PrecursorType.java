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
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * The method of precursor ion selection and activation
 * 
 * <p>
 * Java class for PrecursorType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name=&quot;PrecursorType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;isolationWindow&quot; type=&quot;{http://psi.hupo.org/schema_revision/mzML_1.0.0}ParamGroupType&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;selectedIonList&quot; type=&quot;{http://psi.hupo.org/schema_revision/mzML_1.0.0}SelectedIonListType&quot;/&gt;
 *         &lt;element name=&quot;activation&quot; type=&quot;{http://psi.hupo.org/schema_revision/mzML_1.0.0}ParamGroupType&quot;/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name=&quot;spectrumRef&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}IDREF&quot; /&gt;
 *       &lt;attribute name=&quot;sourceFileRef&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}IDREF&quot; /&gt;
 *       &lt;attribute name=&quot;externalNativeID&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;externalSpectrumID&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PrecursorType", propOrder = { "isolationWindow",
        "selectedIonList", "activation" })
public class PrecursorType {

	protected ParamGroupType isolationWindow;
	@XmlElement(required = true)
	protected SelectedIonListType selectedIonList;
	@XmlElement(required = true)
	protected ParamGroupType activation;
	@XmlAttribute
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	protected Object spectrumRef;
	@XmlAttribute
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	protected Object sourceFileRef;
	@XmlAttribute
	protected String externalNativeID;
	@XmlAttribute
	protected String externalSpectrumID;

	/**
	 * Gets the value of the activation property.
	 * 
	 * @return possible object is {@link ParamGroupType }
	 * 
	 */
	public ParamGroupType getActivation() {
		return this.activation;
	}

	/**
	 * Gets the value of the externalNativeID property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getExternalNativeID() {
		return this.externalNativeID;
	}

	/**
	 * Gets the value of the externalSpectrumID property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getExternalSpectrumID() {
		return this.externalSpectrumID;
	}

	/**
	 * Gets the value of the isolationWindow property.
	 * 
	 * @return possible object is {@link ParamGroupType }
	 * 
	 */
	public ParamGroupType getIsolationWindow() {
		return this.isolationWindow;
	}

	/**
	 * Gets the value of the selectedIonList property.
	 * 
	 * @return possible object is {@link SelectedIonListType }
	 * 
	 */
	public SelectedIonListType getSelectedIonList() {
		return this.selectedIonList;
	}

	/**
	 * Gets the value of the sourceFileRef property.
	 * 
	 * @return possible object is {@link Object }
	 * 
	 */
	public Object getSourceFileRef() {
		return this.sourceFileRef;
	}

	/**
	 * Gets the value of the spectrumRef property.
	 * 
	 * @return possible object is {@link Object }
	 * 
	 */
	public Object getSpectrumRef() {
		return this.spectrumRef;
	}

	/**
	 * Sets the value of the activation property.
	 * 
	 * @param value
	 *            allowed object is {@link ParamGroupType }
	 * 
	 */
	public void setActivation(final ParamGroupType value) {
		this.activation = value;
	}

	/**
	 * Sets the value of the externalNativeID property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setExternalNativeID(final String value) {
		this.externalNativeID = value;
	}

	/**
	 * Sets the value of the externalSpectrumID property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setExternalSpectrumID(final String value) {
		this.externalSpectrumID = value;
	}

	/**
	 * Sets the value of the isolationWindow property.
	 * 
	 * @param value
	 *            allowed object is {@link ParamGroupType }
	 * 
	 */
	public void setIsolationWindow(final ParamGroupType value) {
		this.isolationWindow = value;
	}

	/**
	 * Sets the value of the selectedIonList property.
	 * 
	 * @param value
	 *            allowed object is {@link SelectedIonListType }
	 * 
	 */
	public void setSelectedIonList(final SelectedIonListType value) {
		this.selectedIonList = value;
	}

	/**
	 * Sets the value of the sourceFileRef property.
	 * 
	 * @param value
	 *            allowed object is {@link Object }
	 * 
	 */
	public void setSourceFileRef(final Object value) {
		this.sourceFileRef = value;
	}

	/**
	 * Sets the value of the spectrumRef property.
	 * 
	 * @param value
	 *            allowed object is {@link Object }
	 * 
	 */
	public void setSpectrumRef(final Object value) {
		this.spectrumRef = value;
	}

}