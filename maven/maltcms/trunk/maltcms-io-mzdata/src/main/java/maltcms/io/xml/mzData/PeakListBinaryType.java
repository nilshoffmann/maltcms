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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * Extension of binary data group for m/z and intensity values
 *
 * <p> Java class for peakListBinaryType complex type.
 *
 * <p> The following schema fragment specifies the expected content contained
 * within this class.
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
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "peakListBinaryType", propOrder = {"data"})
public class PeakListBinaryType {

    /**
     * <p> Java class for anonymous complex type.
     *
     * <p> The following schema fragment specifies the expected content
     * contained within this class.
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
     * @return possible object is {@link PeakListBinaryType.Data }
     *
     */
    public PeakListBinaryType.Data getData() {
        return this.data;
    }

    /**
     * Sets the value of the data property.
     *
     * @param value allowed object is {@link PeakListBinaryType.Data }
     *
     */
    public void setData(final PeakListBinaryType.Data value) {
        this.data = value;
    }
}
