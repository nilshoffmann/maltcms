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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;cvLookup&quot; type=&quot;{}cvLookupType&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;description&quot;&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name=&quot;admin&quot; type=&quot;{}adminType&quot;/&gt;
 *                   &lt;element name=&quot;instrument&quot; type=&quot;{}instrumentDescriptionType&quot;/&gt;
 *                   &lt;element name=&quot;dataProcessing&quot; type=&quot;{}dataProcessingType&quot;/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name=&quot;spectrumList&quot;&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name=&quot;spectrum&quot; maxOccurs=&quot;unbounded&quot;&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;extension base=&quot;{}spectrumType&quot;&gt;
 *                         &lt;/extension&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name=&quot;count&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name=&quot;version&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; fixed=&quot;1.05&quot; /&gt;
 *       &lt;attribute name=&quot;accessionNumber&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 * @author Nils Hoffmann
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"cvLookup", "description", "spectrumList"})
@XmlRootElement(name = "mzData")
public class MzData implements Serializable {

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
     *         &lt;element name=&quot;admin&quot; type=&quot;{}adminType&quot;/&gt;
     *         &lt;element name=&quot;instrument&quot; type=&quot;{}instrumentDescriptionType&quot;/&gt;
     *         &lt;element name=&quot;dataProcessing&quot; type=&quot;{}dataProcessingType&quot;/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {"admin", "instrument", "dataProcessing"})
    public static class Description {

        @XmlElement(required = true)
        protected AdminType admin;
        @XmlElement(required = true)
        protected InstrumentDescriptionType instrument;
        @XmlElement(required = true)
        protected DataProcessingType dataProcessing;

        /**
         * Gets the value of the admin property.
         *
         * @return possible object is {@link AdminType }
         *
         */
        public AdminType getAdmin() {
            return this.admin;
        }

        /**
         * Gets the value of the dataProcessing property.
         *
         * @return possible object is {@link DataProcessingType }
         *
         */
        public DataProcessingType getDataProcessing() {
            return this.dataProcessing;
        }

        /**
         * Gets the value of the instrument property.
         *
         * @return possible object is {@link InstrumentDescriptionType }
         *
         */
        public InstrumentDescriptionType getInstrument() {
            return this.instrument;
        }

        /**
         * Sets the value of the admin property.
         *
         * @param value allowed object is {@link AdminType }
         *
         */
        public void setAdmin(final AdminType value) {
            this.admin = value;
        }

        /**
         * Sets the value of the dataProcessing property.
         *
         * @param value allowed object is {@link DataProcessingType }
         *
         */
        public void setDataProcessing(final DataProcessingType value) {
            this.dataProcessing = value;
        }

        /**
         * Sets the value of the instrument property.
         *
         * @param value allowed object is {@link InstrumentDescriptionType }
         *
         */
        public void setInstrument(final InstrumentDescriptionType value) {
            this.instrument = value;
        }
    }

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
     *         &lt;element name=&quot;spectrum&quot; maxOccurs=&quot;unbounded&quot;&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;extension base=&quot;{}spectrumType&quot;&gt;
     *               &lt;/extension&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
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
    @XmlType(name = "", propOrder = {"spectrum"})
    public static class SpectrumList {

        /**
         * <p>
         * Java class for anonymous complex type.
         *
         * <p>
         * The following schema fragment specifies the expected content
         * contained within this class.
         *
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;extension base=&quot;{}spectrumType&quot;&gt;
         *     &lt;/extension&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         *
         *
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Spectrum extends SpectrumType {
        }
        @XmlElement(required = true)
        protected List<MzData.SpectrumList.Spectrum> spectrum;
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
         * Gets the value of the spectrum property.
         *
         * <p>
         * This accessor method returns a reference to the live list, not a
         * snapshot. Therefore any modification you make to the returned list
         * will be present inside the JAXB object. This is why there is not a
         * <CODE>set</CODE> method for the spectrum property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         *
         * <pre>
         * getSpectrum().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link MzData.SpectrumList.Spectrum }
         *
         *
         */
        public List<MzData.SpectrumList.Spectrum> getSpectrum() {
            if (this.spectrum == null) {
                this.spectrum = new ArrayList<>();
            }
            return this.spectrum;
        }

        /**
         * Sets the value of the count property.
         *
         */
        public void setCount(final int value) {
            this.count = value;
        }
    }
    protected List<CvLookupType> cvLookup;
    @XmlElement(required = true)
    protected MzData.Description description;
    @XmlElement(required = true)
    protected MzData.SpectrumList spectrumList;
    @XmlAttribute(required = true)
    protected String version;
    @XmlAttribute(required = true)
    protected String accessionNumber;

    /**
     * Gets the value of the accessionNumber property.
     *
     * @return possible object is {@link java.lang.String}
     */
    public String getAccessionNumber() {
        return this.accessionNumber;
    }

    /**
     * Gets the value of the cvLookup property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the cvLookup property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getCvLookup().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link maltcms.io.xml.mzData.CvLookupType}
     *
     * @return a {@link java.util.List} object.
     */
    public List<CvLookupType> getCvLookup() {
        if (this.cvLookup == null) {
            this.cvLookup = new ArrayList<>();
        }
        return this.cvLookup;
    }

    /**
     * Gets the value of the description property.
     *
     * @return possible object is {@link maltcms.io.xml.mzData.MzData.Description}
     */
    public MzData.Description getDescription() {
        return this.description;
    }

    /**
     * Gets the value of the spectrumList property.
     *
     * @return possible object is {@link maltcms.io.xml.mzData.MzData.SpectrumList}
     */
    public MzData.SpectrumList getSpectrumList() {
        return this.spectrumList;
    }

    /**
     * Gets the value of the version property.
     *
     * @return possible object is {@link java.lang.String}
     */
    public String getVersion() {
        if (this.version == null) {
            return "1.05";
        } else {
            return this.version;
        }
    }

    /**
     * Sets the value of the accessionNumber property.
     *
     * @param value allowed object is {@link java.lang.String}
     */
    public void setAccessionNumber(final String value) {
        this.accessionNumber = value;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is {@link maltcms.io.xml.mzData.MzData.Description}
     */
    public void setDescription(final MzData.Description value) {
        this.description = value;
    }

    /**
     * Sets the value of the spectrumList property.
     *
     * @param value allowed object is {@link maltcms.io.xml.mzData.MzData.SpectrumList}
     */
    public void setSpectrumList(final MzData.SpectrumList value) {
        this.spectrumList = value;
    }

    /**
     * Sets the value of the version property.
     *
     * @param value allowed object is {@link java.lang.String}
     */
    public void setVersion(final String value) {
        this.version = value;
    }
}
