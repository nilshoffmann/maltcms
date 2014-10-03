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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * Extension of binary data group for m/z and intensity values
 *
 * <p>
 * Java class for peakListBinaryType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name=&quot;peakListBinaryType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;group ref=&quot;{}binaryDataGroup&quot;/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 * @author hoffmann
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "peakListBinaryType", propOrder = {"data"})
public class PeakListBinaryType implements Serializable {

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
     *   &lt;simpleContent&gt;
     *     &lt;extension base=&quot;&lt;http://www.w3.org/2001/XMLSchema&gt;base64Binary&quot;&gt;
     *       &lt;attribute name=&quot;precision&quot; use=&quot;required&quot;&gt;
     *         &lt;simpleType&gt;
     *           &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;&gt;
     *             &lt;enumeration value=&quot;32&quot;/&gt;
     *             &lt;enumeration value=&quot;64&quot;/&gt;
     *           &lt;/restriction&gt;
     *         &lt;/simpleType&gt;
     *       &lt;/attribute&gt;
     *       &lt;attribute name=&quot;endian&quot; use=&quot;required&quot;&gt;
     *         &lt;simpleType&gt;
     *           &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;&gt;
     *             &lt;enumeration value=&quot;big&quot;/&gt;
     *             &lt;enumeration value=&quot;little&quot;/&gt;
     *           &lt;/restriction&gt;
     *         &lt;/simpleType&gt;
     *       &lt;/attribute&gt;
     *       &lt;attribute name=&quot;length&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; /&gt;
     *     &lt;/extension&gt;
     *   &lt;/simpleContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {"value"})
    public static class Data {

        @XmlValue
        protected byte[] value;
        @XmlAttribute(required = true)
        protected String precision;
        @XmlAttribute(required = true)
        protected String endian;
        @XmlAttribute(required = true)
        protected int length;

        /**
         * Gets the value of the endian property.
         *
         * @return possible object is {@link String }
         *
         */
        public String getEndian() {
            return this.endian;
        }

        /**
         * Gets the value of the length property.
         *
         */
        public int getLength() {
            return this.length;
        }

        /**
         * Gets the value of the precision property.
         *
         * @return possible object is {@link String }
         *
         */
        public String getPrecision() {
            return this.precision;
        }

        /**
         * Gets the value of the value property.
         *
         * @return possible object is byte[]
         */
        public byte[] getValue() {
            return this.value;
        }

        /**
         * Sets the value of the endian property.
         *
         * @param value allowed object is {@link String }
         *
         */
        public void setEndian(final String value) {
            this.endian = value;
        }

        /**
         * Sets the value of the length property.
         *
         */
        public void setLength(final int value) {
            this.length = value;
        }

        /**
         * Sets the value of the precision property.
         *
         * @param value allowed object is {@link String }
         *
         */
        public void setPrecision(final String value) {
            this.precision = value;
        }

        /**
         * Sets the value of the value property.
         *
         * @param value allowed object is byte[]
         */
        public void setValue(final byte[] value) {
            this.value = (value);
        }
    }
    @XmlElement(required = true)
    protected PeakListBinaryType.Data data;

    /**
     * Gets the value of the data property.
     *
     * @return possible object is {@link maltcms.io.xml.mzData.PeakListBinaryType.Data}
     */
    public PeakListBinaryType.Data getData() {
        return this.data;
    }

    /**
     * Sets the value of the data property.
     *
     * @param value allowed object is {@link maltcms.io.xml.mzData.PeakListBinaryType.Data}
     */
    public void setData(final PeakListBinaryType.Data value) {
        this.data = value;
    }
}
