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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * Information about an ontology/CV source and a short 'lookup' tag to refer to.
 *
 * <p> Java class for cvLookupType complex type.
 *
 * <p> The following schema fragment specifies the expected content contained
 * within this class.
 *
 * <pre>
 * &lt;complexType name=&quot;cvLookupType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;attribute name=&quot;cvLabel&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;fullName&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;version&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;address&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}anyURI&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cvLookupType")
public class CvLookupType {

    @XmlAttribute(required = true)
    protected String cvLabel;
    @XmlAttribute
    protected String fullName;
    @XmlAttribute(required = true)
    protected String version;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String address;

    /**
     * Gets the value of the address property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * Gets the value of the cvLabel property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getCvLabel() {
        return this.cvLabel;
    }

    /**
     * Gets the value of the fullName property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getFullName() {
        return this.fullName;
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
     * Sets the value of the address property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setAddress(final String value) {
        this.address = value;
    }

    /**
     * Sets the value of the cvLabel property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setCvLabel(final String value) {
        this.cvLabel = value;
    }

    /**
     * Sets the value of the fullName property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setFullName(final String value) {
        this.fullName = value;
    }

    /**
     * Sets the value of the version property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setVersion(final String value) {
        this.version = value;
    }
}
