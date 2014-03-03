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
 * Description of the components of the mass spectrometer used
 *
 * <p>
 * Java class for instrumentDescriptionType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained
 * within this class.
 *
 * <pre>
 * &lt;complexType name=&quot;instrumentDescriptionType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;instrumentName&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;/&gt;
 *         &lt;element name=&quot;source&quot; type=&quot;{}paramType&quot;/&gt;
 *         &lt;element name=&quot;analyzerList&quot;&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name=&quot;analyzer&quot; type=&quot;{}paramType&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name=&quot;count&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name=&quot;detector&quot; type=&quot;{}paramType&quot;/&gt;
 *         &lt;element name=&quot;additional&quot; type=&quot;{}paramType&quot; minOccurs=&quot;0&quot;/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "instrumentDescriptionType", propOrder = {"instrumentName",
    "source", "analyzerList", "detector", "additional"})
public class InstrumentDescriptionType implements Serializable {

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
     *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
     *       &lt;sequence&gt;
     *         &lt;element name=&quot;analyzer&quot; type=&quot;{}paramType&quot; maxOccurs=&quot;unbounded&quot;/&gt;
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
    @XmlType(name = "", propOrder = {"analyzer"})
    public static class AnalyzerList {

        @XmlElement(required = true)
        protected List<ParamType> analyzer;
        @XmlAttribute(required = true)
        protected int count;

        /**
         * Gets the value of the analyzer property.
         *
         * <p>
         * This accessor method returns a reference to the live list, not a
         * snapshot. Therefore any modification you make to the returned list
         * will be present inside the JAXB object. This is why there is not a
         * <CODE>set</CODE> method for the analyzer property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         *
         * <pre>
         * getAnalyzer().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ParamType }
         *
         *
         */
        public List<ParamType> getAnalyzer() {
            if (this.analyzer == null) {
                this.analyzer = new ArrayList<ParamType>();
            }
            return this.analyzer;
        }

        /**
         * Gets the value of the count property.
         *
         */
        public int getCount() {
            return this.count;
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
    protected String instrumentName;
    @XmlElement(required = true)
    protected ParamType source;
    @XmlElement(required = true)
    protected InstrumentDescriptionType.AnalyzerList analyzerList;
    @XmlElement(required = true)
    protected ParamType detector;
    protected ParamType additional;

    /**
     * Gets the value of the additional property.
     *
     * @return possible object is {@link ParamType }
     *
     */
    public ParamType getAdditional() {
        return this.additional;
    }

    /**
     * Gets the value of the analyzerList property.
     *
     * @return possible object is {@link InstrumentDescriptionType.AnalyzerList
     * }
     *
     */
    public InstrumentDescriptionType.AnalyzerList getAnalyzerList() {
        return this.analyzerList;
    }

    /**
     * Gets the value of the detector property.
     *
     * @return possible object is {@link ParamType }
     *
     */
    public ParamType getDetector() {
        return this.detector;
    }

    /**
     * Gets the value of the instrumentName property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getInstrumentName() {
        return this.instrumentName;
    }

    /**
     * Gets the value of the source property.
     *
     * @return possible object is {@link ParamType }
     *
     */
    public ParamType getSource() {
        return this.source;
    }

    /**
     * Sets the value of the additional property.
     *
     * @param value allowed object is {@link ParamType }
     *
     */
    public void setAdditional(final ParamType value) {
        this.additional = value;
    }

    /**
     * Sets the value of the analyzerList property.
     *
     * @param value allowed object is
     *              {@link InstrumentDescriptionType.AnalyzerList }
     *
     */
    public void setAnalyzerList(
        final InstrumentDescriptionType.AnalyzerList value) {
        this.analyzerList = value;
    }

    /**
     * Sets the value of the detector property.
     *
     * @param value allowed object is {@link ParamType }
     *
     */
    public void setDetector(final ParamType value) {
        this.detector = value;
    }

    /**
     * Sets the value of the instrumentName property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setInstrumentName(final String value) {
        this.instrumentName = value;
    }

    /**
     * Sets the value of the source property.
     *
     * @param value allowed object is {@link ParamType }
     *
     */
    public void setSource(final ParamType value) {
        this.source = value;
    }
}
