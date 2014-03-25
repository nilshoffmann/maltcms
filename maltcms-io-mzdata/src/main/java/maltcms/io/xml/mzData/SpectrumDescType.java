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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Description of the process of performing an acquisition
 *
 * <p>
 * Java class for spectrumDescType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name=&quot;spectrumDescType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;spectrumSettings&quot; type=&quot;{}spectrumSettingsType&quot;/&gt;
 *         &lt;element name=&quot;precursorList&quot; minOccurs=&quot;0&quot;&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name=&quot;precursor&quot; type=&quot;{}precursorType&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name=&quot;count&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name=&quot;comments&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "spectrumDescType", propOrder = {"spectrumSettings",
    "precursorList", "comments"})
public class SpectrumDescType implements Serializable {

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
     *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
     *       &lt;sequence&gt;
     *         &lt;element name=&quot;precursor&quot; type=&quot;{}precursorType&quot; maxOccurs=&quot;unbounded&quot;/&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name=&quot;count&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {"precursor"})
    public static class PrecursorList {

        @XmlElement(required = true)
        protected List<PrecursorType> precursor;
        @XmlAttribute(required = true)
        protected int count;

        /**
         * Gets the value of the count property.
         *
         */
        public int getCount() {
            return this.count;
        }

        /**
         * Gets the value of the precursor property.
         *
         * <p>
         * This accessor method returns a reference to the live list, not a
         * snapshot. Therefore any modification you make to the returned list
         * will be present inside the JAXB object. This is why there is not a
         * <CODE>set</CODE> method for the precursor property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         *
         * <pre>
         * getPrecursor().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link PrecursorType }
         *
         *
         */
        public List<PrecursorType> getPrecursor() {
            if (this.precursor == null) {
                this.precursor = new ArrayList<>();
            }
            return this.precursor;
        }

        /**
         * Sets the value of the count property.
         *
         */
        public void setCount(final int value) {
            this.count = value;
        }
    }
    @XmlElement(required = true)
    protected SpectrumSettingsType spectrumSettings;
    protected SpectrumDescType.PrecursorList precursorList;
    protected List<String> comments;

    /**
     * Gets the value of the comments property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the comments property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getComments().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link String
     * }
     *
     *
     */
    public List<String> getComments() {
        if (this.comments == null) {
            this.comments = new ArrayList<>();
        }
        return this.comments;
    }

    /**
     * Gets the value of the precursorList property.
     *
     * @return possible object is {@link SpectrumDescType.PrecursorList }
     *
     */
    public SpectrumDescType.PrecursorList getPrecursorList() {
        return this.precursorList;
    }

    /**
     * Gets the value of the spectrumSettings property.
     *
     * @return possible object is {@link SpectrumSettingsType }
     *
     */
    public SpectrumSettingsType getSpectrumSettings() {
        return this.spectrumSettings;
    }

    /**
     * Sets the value of the precursorList property.
     *
     * @param value allowed object is {@link SpectrumDescType.PrecursorList }
     *
     */
    public void setPrecursorList(final SpectrumDescType.PrecursorList value) {
        this.precursorList = value;
    }

    /**
     * Sets the value of the spectrumSettings property.
     *
     * @param value allowed object is {@link SpectrumSettingsType }
     *
     */
    public void setSpectrumSettings(final SpectrumSettingsType value) {
        this.spectrumSettings = value;
    }
}
