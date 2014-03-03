/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code
 * under. Certain files or entire directories may not be covered by this
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package maltcms.io.xml.mzData;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * Information about an ontology/CV source and a short 'lookup' tag to refer to.
 *
 * <p>
 * Java class for cvLookupType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained
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
public class CvLookupType implements Serializable {

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
