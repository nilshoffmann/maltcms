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
 * @author hoffmann
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "personType", propOrder = {"name", "institution",
    "contactInfo"})
public class PersonType implements Serializable {

    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String institution;
    protected String contactInfo;

    /**
     * Gets the value of the contactInfo property.
     *
     * @return possible object is {@link java.lang.String}
     */
    public String getContactInfo() {
        return this.contactInfo;
    }

    /**
     * Gets the value of the institution property.
     *
     * @return possible object is {@link java.lang.String}
     */
    public String getInstitution() {
        return this.institution;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link java.lang.String}
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the value of the contactInfo property.
     *
     * @param value allowed object is {@link java.lang.String}
     */
    public void setContactInfo(final String value) {
        this.contactInfo = value;
    }

    /**
     * Sets the value of the institution property.
     *
     * @param value allowed object is {@link java.lang.String}
     */
    public void setInstitution(final String value) {
        this.institution = value;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link java.lang.String}
     */
    public void setName(final String value) {
        this.name = value;
    }
}
