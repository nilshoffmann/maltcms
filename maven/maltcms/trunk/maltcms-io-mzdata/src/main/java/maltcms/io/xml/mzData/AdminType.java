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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 'Header' information - sample description, contact details, comments
 *
 * <p> Java class for adminType complex type.
 *
 * <p> The following schema fragment specifies the expected content contained
 * within this class.
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
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "adminType", propOrder = {"sampleName", "sampleDescription",
    "sourceFile", "contact"})
public class AdminType {

    @XmlElement(required = true)
    protected String sampleName;
    protected DescriptionType sampleDescription;
    protected SourceFileType sourceFile;
    @XmlElement(required = true)
    protected List<PersonType> contact;

    /**
     * Gets the value of the contact property.
     *
     * <p> This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the contact property.
     *
     * <p> For example, to add a new item, do as follows:
     *
     * <pre>
     * getContact().add(newItem);
     * </pre>
     *
     *
     * <p> Objects of the following type(s) are allowed in the list
	 * {@link PersonType }
     *
     *
     */
    public List<PersonType> getContact() {
        if (this.contact == null) {
            this.contact = new ArrayList<PersonType>();
        }
        return this.contact;
    }

    /**
     * Gets the value of the sampleDescription property.
     *
     * @return possible object is {@link DescriptionType }
     *
     */
    public DescriptionType getSampleDescription() {
        return this.sampleDescription;
    }

    /**
     * Gets the value of the sampleName property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getSampleName() {
        return this.sampleName;
    }

    /**
     * Gets the value of the sourceFile property.
     *
     * @return possible object is {@link SourceFileType }
     *
     */
    public SourceFileType getSourceFile() {
        return this.sourceFile;
    }

    /**
     * Sets the value of the sampleDescription property.
     *
     * @param value allowed object is {@link DescriptionType }
     *
     */
    public void setSampleDescription(final DescriptionType value) {
        this.sampleDescription = value;
    }

    /**
     * Sets the value of the sampleName property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setSampleName(final String value) {
        this.sampleName = value;
    }

    /**
     * Sets the value of the sourceFile property.
     *
     * @param value allowed object is {@link SourceFileType }
     *
     */
    public void setSourceFile(final SourceFileType value) {
        this.sourceFile = value;
    }
}
