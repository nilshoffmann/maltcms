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
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * This element holds additional data or annotation. Only controlled values are
 * allowed here.
 * 
 * <p>
 * Java class for CVParamType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name=&quot;CVParamType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;attribute name=&quot;cvRef&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}IDREF&quot; /&gt;
 *       &lt;attribute name=&quot;accession&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;value&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;name&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;unitAccession&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;unitName&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;unitCvRef&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}IDREF&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CVParamType")
public class CVParamType {

	@XmlAttribute(required = true)
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	protected Object cvRef;
	@XmlAttribute(required = true)
	protected String accession;
	@XmlAttribute
	protected String value;
	@XmlAttribute(required = true)
	protected String name;
	@XmlAttribute
	protected String unitAccession;
	@XmlAttribute
	protected String unitName;
	@XmlAttribute
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	protected Object unitCvRef;

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
	 * Gets the value of the cvRef property.
	 * 
	 * @return possible object is {@link Object }
	 * 
	 */
	public Object getCvRef() {
		return this.cvRef;
	}

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the value of the unitAccession property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getUnitAccession() {
		return this.unitAccession;
	}

	/**
	 * Gets the value of the unitCvRef property.
	 * 
	 * @return possible object is {@link Object }
	 * 
	 */
	public Object getUnitCvRef() {
		return this.unitCvRef;
	}

	/**
	 * Gets the value of the unitName property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getUnitName() {
		return this.unitName;
	}

	/**
	 * Gets the value of the value property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getValue() {
		return this.value;
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
	 * Sets the value of the cvRef property.
	 * 
	 * @param value
	 *            allowed object is {@link Object }
	 * 
	 */
	public void setCvRef(final Object value) {
		this.cvRef = value;
	}

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setName(final String value) {
		this.name = value;
	}

	/**
	 * Sets the value of the unitAccession property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setUnitAccession(final String value) {
		this.unitAccession = value;
	}

	/**
	 * Sets the value of the unitCvRef property.
	 * 
	 * @param value
	 *            allowed object is {@link Object }
	 * 
	 */
	public void setUnitCvRef(final Object value) {
		this.unitCvRef = value;
	}

	/**
	 * Sets the value of the unitName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setUnitName(final String value) {
		this.unitName = value;
	}

	/**
	 * Sets the value of the value property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setValue(final String value) {
		this.value = value;
	}

}