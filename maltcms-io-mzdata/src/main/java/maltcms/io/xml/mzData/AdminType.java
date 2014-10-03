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
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 'Header' information - sample description, contact details, comments
 *
 * <p>
 * Java class for adminType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name=&quot;adminType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;sampleName&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;/&gt;
 *         &lt;element name=&quot;sampleDescription&quot; type=&quot;{}descriptionType&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;sourceFile&quot; type=&quot;{}sourceFileType&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;contact&quot; type=&quot;{}personType&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 * @author Nils Hoffmann
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "adminType", propOrder = {"sampleName", "sampleDescription",
    "sourceFile", "contact"})
public class AdminType implements Serializable {

    @XmlElement(required = true)
    protected String sampleName;
    protected DescriptionType sampleDescription;
    protected SourceFileType sourceFile;
    @XmlElement(required = true)
    protected List<PersonType> contact;

    /**
     * Gets the value of the contact property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the contact property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getContact().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link maltcms.io.xml.mzData.PersonType}
     *
     * @return a {@link java.util.List} object.
     */
    public List<PersonType> getContact() {
        if (this.contact == null) {
            this.contact = new ArrayList<>();
        }
        return this.contact;
    }

    /**
     * Gets the value of the sampleDescription property.
     *
     * @return possible object is {@link maltcms.io.xml.mzData.DescriptionType}
     */
    public DescriptionType getSampleDescription() {
        return this.sampleDescription;
    }

    /**
     * Gets the value of the sampleName property.
     *
     * @return possible object is {@link java.lang.String}
     */
    public String getSampleName() {
        return this.sampleName;
    }

    /**
     * Gets the value of the sourceFile property.
     *
     * @return possible object is {@link maltcms.io.xml.mzData.SourceFileType}
     */
    public SourceFileType getSourceFile() {
        return this.sourceFile;
    }

    /**
     * Sets the value of the sampleDescription property.
     *
     * @param value allowed object is {@link maltcms.io.xml.mzData.DescriptionType}
     */
    public void setSampleDescription(final DescriptionType value) {
        this.sampleDescription = value;
    }

    /**
     * Sets the value of the sampleName property.
     *
     * @param value allowed object is {@link java.lang.String}
     */
    public void setSampleName(final String value) {
        this.sampleName = value;
    }

    /**
     * Sets the value of the sourceFile property.
     *
     * @param value allowed object is {@link maltcms.io.xml.mzData.SourceFileType}
     */
    public void setSourceFile(final SourceFileType value) {
        this.sourceFile = value;
    }
}
