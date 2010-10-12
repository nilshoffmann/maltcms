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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * List and descriptions of samples.
 * 
 * <p>
 * Java class for SampleListType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name=&quot;SampleListType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;sample&quot; type=&quot;{http://psi.hupo.org/schema_revision/mzML_1.0.0}SampleType&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name=&quot;count&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}nonNegativeInteger&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SampleListType", propOrder = { "sample" })
public class SampleListType {

	@XmlElement(required = true)
	protected List<SampleType> sample;
	@XmlAttribute(required = true)
	@XmlSchemaType(name = "nonNegativeInteger")
	protected BigInteger count;

	/**
	 * Gets the value of the count property.
	 * 
	 * @return possible object is {@link BigInteger }
	 * 
	 */
	public BigInteger getCount() {
		return this.count;
	}

	/**
	 * Gets the value of the sample property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the sample property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getSample().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link SampleType }
	 * 
	 * 
	 */
	public List<SampleType> getSample() {
		if (this.sample == null) {
			this.sample = new ArrayList<SampleType>();
		}
		return this.sample;
	}

	/**
	 * Sets the value of the count property.
	 * 
	 * @param value
	 *            allowed object is {@link BigInteger }
	 * 
	 */
	public void setCount(final BigInteger value) {
		this.count = value;
	}

}