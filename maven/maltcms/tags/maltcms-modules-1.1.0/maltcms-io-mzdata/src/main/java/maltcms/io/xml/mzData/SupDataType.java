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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * Data type for additional data vectors (beyond m/z and intensity).
 * 
 * <p>
 * Java class for supDataType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name=&quot;supDataType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;arrayName&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;/&gt;
 *         &lt;choice&gt;
 *           &lt;element name=&quot;float&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}float&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *           &lt;element name=&quot;double&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}double&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *           &lt;element name=&quot;int&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *           &lt;element name=&quot;boolean&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}boolean&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *           &lt;element name=&quot;string&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *           &lt;element name=&quot;time&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}float&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *           &lt;element name=&quot;URI&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}anyURI&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name=&quot;id&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; /&gt;
 *       &lt;attribute name=&quot;length&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; /&gt;
 *       &lt;attribute name=&quot;indexed&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}boolean&quot; /&gt;
 *       &lt;attribute name=&quot;offset&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; default=&quot;0&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "supDataType", propOrder = { "arrayName", "_float", "_double",
        "_int", "_boolean", "string", "time", "uri" })
public class SupDataType {

	@XmlElement(required = true)
	protected String arrayName;
	@XmlElement(name = "float", type = Float.class)
	protected List<Float> _float;
	@XmlElement(name = "double", type = Double.class)
	protected List<Double> _double;
	@XmlElement(name = "int", type = Integer.class)
	protected List<Integer> _int;
	@XmlElement(name = "boolean", type = Boolean.class)
	protected List<Boolean> _boolean;
	protected List<String> string;
	@XmlElement(type = Float.class)
	protected List<Float> time;
	@XmlElement(name = "URI")
	@XmlSchemaType(name = "anyURI")
	protected List<String> uri;
	@XmlAttribute(required = true)
	protected int id;
	@XmlAttribute(required = true)
	protected int length;
	@XmlAttribute(required = true)
	protected boolean indexed;
	@XmlAttribute
	protected Integer offset;

	/**
	 * Gets the value of the arrayName property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getArrayName() {
		return this.arrayName;
	}

	/**
	 * Gets the value of the boolean property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the boolean property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getBoolean().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Boolean }
	 * 
	 * 
	 */
	public List<Boolean> getBoolean() {
		if (this._boolean == null) {
			this._boolean = new ArrayList<Boolean>();
		}
		return this._boolean;
	}

	/**
	 * Gets the value of the double property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the double property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getDouble().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Double }
	 * 
	 * 
	 */
	public List<Double> getDouble() {
		if (this._double == null) {
			this._double = new ArrayList<Double>();
		}
		return this._double;
	}

	/**
	 * Gets the value of the float property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the float property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getFloat().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Float }
	 * 
	 * 
	 */
	public List<Float> getFloat() {
		if (this._float == null) {
			this._float = new ArrayList<Float>();
		}
		return this._float;
	}

	/**
	 * Gets the value of the id property.
	 * 
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Gets the value of the int property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the int property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getInt().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Integer }
	 * 
	 * 
	 */
	public List<Integer> getInt() {
		if (this._int == null) {
			this._int = new ArrayList<Integer>();
		}
		return this._int;
	}

	/**
	 * Gets the value of the length property.
	 * 
	 */
	public int getLength() {
		return this.length;
	}

	/**
	 * Gets the value of the offset property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public int getOffset() {
		if (this.offset == null) {
			return 0;
		} else {
			return this.offset;
		}
	}

	/**
	 * Gets the value of the string property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the string property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getString().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link String }
	 * 
	 * 
	 */
	public List<String> getString() {
		if (this.string == null) {
			this.string = new ArrayList<String>();
		}
		return this.string;
	}

	/**
	 * Gets the value of the time property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the time property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getTime().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Float }
	 * 
	 * 
	 */
	public List<Float> getTime() {
		if (this.time == null) {
			this.time = new ArrayList<Float>();
		}
		return this.time;
	}

	/**
	 * Gets the value of the uri property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the uri property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getURI().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link String }
	 * 
	 * 
	 */
	public List<String> getURI() {
		if (this.uri == null) {
			this.uri = new ArrayList<String>();
		}
		return this.uri;
	}

	/**
	 * Gets the value of the indexed property.
	 * 
	 */
	public boolean isIndexed() {
		return this.indexed;
	}

	/**
	 * Sets the value of the arrayName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setArrayName(final String value) {
		this.arrayName = value;
	}

	/**
	 * Sets the value of the id property.
	 * 
	 */
	public void setId(final int value) {
		this.id = value;
	}

	/**
	 * Sets the value of the indexed property.
	 * 
	 */
	public void setIndexed(final boolean value) {
		this.indexed = value;
	}

	/**
	 * Sets the value of the length property.
	 * 
	 */
	public void setLength(final int value) {
		this.length = value;
	}

	/**
	 * Sets the value of the offset property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setOffset(final Integer value) {
		this.offset = value;
	}

}
