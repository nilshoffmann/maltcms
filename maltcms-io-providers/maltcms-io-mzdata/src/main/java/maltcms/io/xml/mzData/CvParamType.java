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
import javax.xml.bind.annotation.XmlType;

/**
 * Parameters from a controlled vocbulary.
 *
 * <p>
 * Java class for cvParamType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name=&quot;cvParamType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;attribute name=&quot;cvLabel&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;accession&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;name&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;value&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 * @author Nils Hoffmann
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cvParamType")
public class CvParamType implements Serializable {

    @XmlAttribute(required = true)
    protected String cvLabel;
    @XmlAttribute(required = true)
    protected String accession;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute
    protected String value;

    /**
     * Gets the value of the accession property.
     *
     * @return possible object is {@link java.lang.String}
     */
    public String getAccession() {
        return this.accession;
    }

    /**
     * Gets the value of the cvLabel property.
     *
     * @return possible object is {@link java.lang.String}
     */
    public String getCvLabel() {
        return this.cvLabel;
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
     * Gets the value of the value property.
     *
     * @return possible object is {@link java.lang.String}
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the value of the accession property.
     *
     * @param value allowed object is {@link java.lang.String}
     */
    public void setAccession(final String value) {
        this.accession = value;
    }

    /**
     * Sets the value of the cvLabel property.
     *
     * @param value allowed object is {@link java.lang.String}
     */
    public void setCvLabel(final String value) {
        this.cvLabel = value;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link java.lang.String}
     */
    public void setName(final String value) {
        this.name = value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is {@link java.lang.String}
     */
    public void setValue(final String value) {
        this.value = value;
    }
}
