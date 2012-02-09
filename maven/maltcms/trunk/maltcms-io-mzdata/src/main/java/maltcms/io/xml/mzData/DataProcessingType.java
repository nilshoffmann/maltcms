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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Description of the software, and the way in which it was used to generate the
 * peak list.
 * 
 * <p>
 * Java class for dataProcessingType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name=&quot;dataProcessingType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;software&quot;&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;extension base=&quot;{}softwareType&quot;&gt;
 *               &lt;/extension&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name=&quot;processingMethod&quot; type=&quot;{}paramType&quot; minOccurs=&quot;0&quot;/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dataProcessingType", propOrder = { "software",
        "processingMethod" })
public class DataProcessingType {

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 * 
	 * <p>
	 * The following schema fragment specifies the expected content contained
	 * within this class.
	 * 
	 * <pre>
	 * &lt;complexType&gt;
	 *   &lt;complexContent&gt;
	 *     &lt;extension base=&quot;{}softwareType&quot;&gt;
	 *     &lt;/extension&gt;
	 *   &lt;/complexContent&gt;
	 * &lt;/complexType&gt;
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "")
	public static class Software extends SoftwareType {

	}

	@XmlElement(required = true)
	protected DataProcessingType.Software software;

	protected ParamType processingMethod;

	/**
	 * Gets the value of the processingMethod property.
	 * 
	 * @return possible object is {@link ParamType }
	 * 
	 */
	public ParamType getProcessingMethod() {
		return this.processingMethod;
	}

	/**
	 * Gets the value of the software property.
	 * 
	 * @return possible object is {@link DataProcessingType.Software }
	 * 
	 */
	public DataProcessingType.Software getSoftware() {
		return this.software;
	}

	/**
	 * Sets the value of the processingMethod property.
	 * 
	 * @param value
	 *            allowed object is {@link ParamType }
	 * 
	 */
	public void setProcessingMethod(final ParamType value) {
		this.processingMethod = value;
	}

	/**
	 * Sets the value of the software property.
	 * 
	 * @param value
	 *            allowed object is {@link DataProcessingType.Software }
	 * 
	 */
	public void setSoftware(final DataProcessingType.Software value) {
		this.software = value;
	}

}
