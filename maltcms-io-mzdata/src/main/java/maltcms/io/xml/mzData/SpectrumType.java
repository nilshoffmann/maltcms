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
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * The structure tha captures the generation of a peak list (including the
 * underlying acquisitions)
 *
 * <p>
 * Java class for spectrumType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name=&quot;spectrumType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;spectrumDesc&quot; type=&quot;{}spectrumDescType&quot;/&gt;
 *         &lt;element name=&quot;supDesc&quot; type=&quot;{}supDescType&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;mzArrayBinary&quot; type=&quot;{}peakListBinaryType&quot;/&gt;
 *         &lt;element name=&quot;intenArrayBinary&quot; type=&quot;{}peakListBinaryType&quot;/&gt;
 *         &lt;choice maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;&gt;
 *           &lt;element name=&quot;supDataArrayBinary&quot; type=&quot;{}supDataBinaryType&quot;/&gt;
 *           &lt;element name=&quot;supDataArray&quot; type=&quot;{}supDataType&quot;/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name=&quot;id&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "spectrumType", propOrder = {"spectrumDesc", "supDesc",
    "mzArrayBinary", "intenArrayBinary", "supDataArrayBinaryOrSupDataArray"})
@XmlSeeAlso({maltcms.io.xml.mzData.MzData.SpectrumList.Spectrum.class})
public class SpectrumType implements Serializable {

    @XmlElement(required = true)
    protected SpectrumDescType spectrumDesc;
    protected List<SupDescType> supDesc;
    @XmlElement(required = true)
    protected PeakListBinaryType mzArrayBinary;
    @XmlElement(required = true)
    protected PeakListBinaryType intenArrayBinary;
    @XmlElements({
        @XmlElement(name = "supDataArray", type = SupDataType.class),
        @XmlElement(name = "supDataArrayBinary", type = SupDataBinaryType.class)})
    protected List<Object> supDataArrayBinaryOrSupDataArray;
    @XmlAttribute(required = true)
    protected int id;

    /**
     * Gets the value of the id property.
     *
     */
    public int getId() {
        return this.id;
    }

    /**
     * Gets the value of the intenArrayBinary property.
     *
     * @return possible object is {@link PeakListBinaryType }
     *
     */
    public PeakListBinaryType getIntenArrayBinary() {
        return this.intenArrayBinary;
    }

    /**
     * Gets the value of the mzArrayBinary property.
     *
     * @return possible object is {@link PeakListBinaryType }
     *
     */
    public PeakListBinaryType getMzArrayBinary() {
        return this.mzArrayBinary;
    }

    /**
     * Gets the value of the spectrumDesc property.
     *
     * @return possible object is {@link SpectrumDescType }
     *
     */
    public SpectrumDescType getSpectrumDesc() {
        return this.spectrumDesc;
    }

    /**
     * Gets the value of the supDataArrayBinaryOrSupDataArray property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the supDataArrayBinaryOrSupDataArray
     * property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getSupDataArrayBinaryOrSupDataArray().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SupDataType } {@link SupDataBinaryType }
     *
     *
     */
    public List<Object> getSupDataArrayBinaryOrSupDataArray() {
        if (this.supDataArrayBinaryOrSupDataArray == null) {
            this.supDataArrayBinaryOrSupDataArray = new ArrayList<>();
        }
        return this.supDataArrayBinaryOrSupDataArray;
    }

    /**
     * Gets the value of the supDesc property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the supDesc property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getSupDesc().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SupDescType }
     *
     *
     */
    public List<SupDescType> getSupDesc() {
        if (this.supDesc == null) {
            this.supDesc = new ArrayList<>();
        }
        return this.supDesc;
    }

    /**
     * Sets the value of the id property.
     *
     */
    public void setId(final int value) {
        this.id = value;
    }

    /**
     * Sets the value of the intenArrayBinary property.
     *
     * @param value allowed object is {@link PeakListBinaryType }
     *
     */
    public void setIntenArrayBinary(final PeakListBinaryType value) {
        this.intenArrayBinary = value;
    }

    /**
     * Sets the value of the mzArrayBinary property.
     *
     * @param value allowed object is {@link PeakListBinaryType }
     *
     */
    public void setMzArrayBinary(final PeakListBinaryType value) {
        this.mzArrayBinary = value;
    }

    /**
     * Sets the value of the spectrumDesc property.
     *
     * @param value allowed object is {@link SpectrumDescType }
     *
     */
    public void setSpectrumDesc(final SpectrumDescType value) {
        this.spectrumDesc = value;
    }
}
