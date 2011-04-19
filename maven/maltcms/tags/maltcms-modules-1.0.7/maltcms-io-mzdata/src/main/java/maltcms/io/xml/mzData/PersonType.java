/*
 * $Id: PersonType.java 43 2009-10-16 17:22:55Z nilshoffmann $
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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Data type for operator identification information.
 * 
 * <p>
 * Java class for personType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name=&quot;personType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;name&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;/&gt;
 *         &lt;element name=&quot;institution&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;/&gt;
 *         &lt;element name=&quot;contactInfo&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "personType", propOrder = { "name", "institution",
        "contactInfo" })
public class PersonType {

	@XmlElement(required = true)
	protected String name;
	@XmlElement(required = true)
	protected String institution;
	protected String contactInfo;

	/**
	 * Gets the value of the contactInfo property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getContactInfo() {
		return this.contactInfo;
	}

	/**
	 * Gets the value of the institution property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getInstitution() {
		return this.institution;
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
	 * Sets the value of the contactInfo property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setContactInfo(final String value) {
		this.contactInfo = value;
	}

	/**
	 * Sets the value of the institution property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setInstitution(final String value) {
		this.institution = value;
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

}
