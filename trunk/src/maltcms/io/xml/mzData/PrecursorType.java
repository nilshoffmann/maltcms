/*
 * $Id$
 */

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB)
// Reference Implementation, vhudson-jaxb-ri-2.1-646
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source
// schema.
// Generated on: 2008.10.14 at 08:55:46 AM CEST
//

package maltcms.io.xml.mzData;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The method of precursor ion selection and activation
 * 
 * <p>
 * Java class for precursorType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name=&quot;precursorType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;ionSelection&quot; type=&quot;{}paramType&quot;/&gt;
 *         &lt;element name=&quot;activation&quot; type=&quot;{}paramType&quot;/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name=&quot;msLevel&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; /&gt;
 *       &lt;attribute name=&quot;spectrumRef&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "precursorType", propOrder = { "ionSelection", "activation" })
public class PrecursorType {

	@XmlElement(required = true)
	protected ParamType ionSelection;
	@XmlElement(required = true)
	protected ParamType activation;
	@XmlAttribute(required = true)
	protected int msLevel;
	@XmlAttribute(required = true)
	protected int spectrumRef;

	/**
	 * Gets the value of the activation property.
	 * 
	 * @return possible object is {@link ParamType }
	 * 
	 */
	public ParamType getActivation() {
		return this.activation;
	}

	/**
	 * Gets the value of the ionSelection property.
	 * 
	 * @return possible object is {@link ParamType }
	 * 
	 */
	public ParamType getIonSelection() {
		return this.ionSelection;
	}

	/**
	 * Gets the value of the msLevel property.
	 * 
	 */
	public int getMsLevel() {
		return this.msLevel;
	}

	/**
	 * Gets the value of the spectrumRef property.
	 * 
	 */
	public int getSpectrumRef() {
		return this.spectrumRef;
	}

	/**
	 * Sets the value of the activation property.
	 * 
	 * @param value
	 *            allowed object is {@link ParamType }
	 * 
	 */
	public void setActivation(final ParamType value) {
		this.activation = value;
	}

	/**
	 * Sets the value of the ionSelection property.
	 * 
	 * @param value
	 *            allowed object is {@link ParamType }
	 * 
	 */
	public void setIonSelection(final ParamType value) {
		this.ionSelection = value;
	}

	/**
	 * Sets the value of the msLevel property.
	 * 
	 */
	public void setMsLevel(final int value) {
		this.msLevel = value;
	}

	/**
	 * Sets the value of the spectrumRef property.
	 * 
	 */
	public void setSpectrumRef(final int value) {
		this.spectrumRef = value;
	}

}
