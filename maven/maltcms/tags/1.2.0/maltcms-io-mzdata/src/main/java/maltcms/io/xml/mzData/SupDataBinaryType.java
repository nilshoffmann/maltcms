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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Extension of binary data group for supplemental data
 * 
 * <p>
 * Java class for supDataBinaryType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name=&quot;supDataBinaryType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;arrayName&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;/&gt;
 *         &lt;group ref=&quot;{}binaryDataGroup&quot;/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name=&quot;id&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "supDataBinaryType", propOrder = { "arrayName", "data" })
public class SupDataBinaryType {

	@XmlElement(required = true)
	protected String arrayName;
	@XmlElement(required = true)
	protected maltcms.io.xml.mzData.PeakListBinaryType.Data data;
	@XmlAttribute(required = true)
	protected int id;

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
	 * Gets the value of the data property.
	 * 
	 * @return possible object is
	 *         {@link maltcms.io.xml.mzData.PeakListBinaryType.Data }
	 * 
	 */
	public maltcms.io.xml.mzData.PeakListBinaryType.Data getData() {
		return this.data;
	}

	/**
	 * Gets the value of the id property.
	 * 
	 */
	public int getId() {
		return this.id;
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
	 * Sets the value of the data property.
	 * 
	 * @param value
	 *            allowed object is
	 *            {@link maltcms.io.xml.mzData.PeakListBinaryType.Data }
	 * 
	 */
	public void setData(
	        final maltcms.io.xml.mzData.PeakListBinaryType.Data value) {
		this.data = value;
	}

	/**
	 * Sets the value of the id property.
	 * 
	 */
	public void setId(final int value) {
		this.id = value;
	}

}
