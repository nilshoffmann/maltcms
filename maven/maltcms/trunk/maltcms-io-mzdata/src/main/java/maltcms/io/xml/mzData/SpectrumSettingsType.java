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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Description of the parameters for the mass spectrometer for a given
 * acquisition (or list of)
 * 
 * <p>
 * Java class for spectrumSettingsType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name=&quot;spectrumSettingsType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;acqSpecification&quot; minOccurs=&quot;0&quot;&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name=&quot;acquisition&quot; maxOccurs=&quot;unbounded&quot;&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;extension base=&quot;{}paramType&quot;&gt;
 *                           &lt;attribute name=&quot;acqNumber&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; /&gt;
 *                         &lt;/extension&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name=&quot;spectrumType&quot; use=&quot;required&quot;&gt;
 *                   &lt;simpleType&gt;
 *                     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;&gt;
 *                       &lt;enumeration value=&quot;discrete&quot;/&gt;
 *                       &lt;enumeration value=&quot;continuous&quot;/&gt;
 *                     &lt;/restriction&gt;
 *                   &lt;/simpleType&gt;
 *                 &lt;/attribute&gt;
 *                 &lt;attribute name=&quot;methodOfCombination&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *                 &lt;attribute name=&quot;count&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name=&quot;spectrumInstrument&quot;&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;extension base=&quot;{}paramType&quot;&gt;
 *                 &lt;attribute name=&quot;msLevel&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; /&gt;
 *                 &lt;attribute name=&quot;mzRangeStart&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}float&quot; /&gt;
 *                 &lt;attribute name=&quot;mzRangeStop&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}float&quot; /&gt;
 *               &lt;/extension&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "spectrumSettingsType", propOrder = { "acqSpecification",
        "spectrumInstrument" })
public class SpectrumSettingsType {

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
	 *         &lt;element name=&quot;acquisition&quot; maxOccurs=&quot;unbounded&quot;&gt;
	 *           &lt;complexType&gt;
	 *             &lt;complexContent&gt;
	 *               &lt;extension base=&quot;{}paramType&quot;&gt;
	 *                 &lt;attribute name=&quot;acqNumber&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; /&gt;
	 *               &lt;/extension&gt;
	 *             &lt;/complexContent&gt;
	 *           &lt;/complexType&gt;
	 *         &lt;/element&gt;
	 *       &lt;/sequence&gt;
	 *       &lt;attribute name=&quot;spectrumType&quot; use=&quot;required&quot;&gt;
	 *         &lt;simpleType&gt;
	 *           &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;&gt;
	 *             &lt;enumeration value=&quot;discrete&quot;/&gt;
	 *             &lt;enumeration value=&quot;continuous&quot;/&gt;
	 *           &lt;/restriction&gt;
	 *         &lt;/simpleType&gt;
	 *       &lt;/attribute&gt;
	 *       &lt;attribute name=&quot;methodOfCombination&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
	 *       &lt;attribute name=&quot;count&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; /&gt;
	 *     &lt;/restriction&gt;
	 *   &lt;/complexContent&gt;
	 * &lt;/complexType&gt;
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "acquisition" })
	public static class AcqSpecification {

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
		 *     &lt;extension base=&quot;{}paramType&quot;&gt;
		 *       &lt;attribute name=&quot;acqNumber&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; /&gt;
		 *     &lt;/extension&gt;
		 *   &lt;/complexContent&gt;
		 * &lt;/complexType&gt;
		 * </pre>
		 * 
		 * 
		 */
		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlType(name = "")
		public static class Acquisition extends ParamType {

			@XmlAttribute(required = true)
			protected int acqNumber;

			/**
			 * Gets the value of the acqNumber property.
			 * 
			 */
			public int getAcqNumber() {
				return this.acqNumber;
			}

			/**
			 * Sets the value of the acqNumber property.
			 * 
			 */
			public void setAcqNumber(final int value) {
				this.acqNumber = value;
			}

		}

		@XmlElement(required = true)
		protected List<SpectrumSettingsType.AcqSpecification.Acquisition> acquisition;
		@XmlAttribute(required = true)
		protected String spectrumType;
		@XmlAttribute(required = true)
		protected String methodOfCombination;

		@XmlAttribute(required = true)
		protected int count;

		/**
		 * Gets the value of the acquisition property.
		 * 
		 * <p>
		 * This accessor method returns a reference to the live list, not a
		 * snapshot. Therefore any modification you make to the returned list
		 * will be present inside the JAXB object. This is why there is not a
		 * <CODE>set</CODE> method for the acquisition property.
		 * 
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getAcquisition().add(newItem);
		 * </pre>
		 * 
		 * 
		 * <p>
		 * Objects of the following type(s) are allowed in the list
		 * {@link SpectrumSettingsType.AcqSpecification.Acquisition }
		 * 
		 * 
		 */
		public List<SpectrumSettingsType.AcqSpecification.Acquisition> getAcquisition() {
			if (this.acquisition == null) {
				this.acquisition = new ArrayList<SpectrumSettingsType.AcqSpecification.Acquisition>();
			}
			return this.acquisition;
		}

		/**
		 * Gets the value of the count property.
		 * 
		 */
		public int getCount() {
			return this.count;
		}

		/**
		 * Gets the value of the methodOfCombination property.
		 * 
		 * @return possible object is {@link String }
		 * 
		 */
		public String getMethodOfCombination() {
			return this.methodOfCombination;
		}

		/**
		 * Gets the value of the spectrumType property.
		 * 
		 * @return possible object is {@link String }
		 * 
		 */
		public String getSpectrumType() {
			return this.spectrumType;
		}

		/**
		 * Sets the value of the count property.
		 * 
		 */
		public void setCount(final int value) {
			this.count = value;
		}

		/**
		 * Sets the value of the methodOfCombination property.
		 * 
		 * @param value
		 *            allowed object is {@link String }
		 * 
		 */
		public void setMethodOfCombination(final String value) {
			this.methodOfCombination = value;
		}

		/**
		 * Sets the value of the spectrumType property.
		 * 
		 * @param value
		 *            allowed object is {@link String }
		 * 
		 */
		public void setSpectrumType(final String value) {
			this.spectrumType = value;
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
	 *     &lt;extension base=&quot;{}paramType&quot;&gt;
	 *       &lt;attribute name=&quot;msLevel&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; /&gt;
	 *       &lt;attribute name=&quot;mzRangeStart&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}float&quot; /&gt;
	 *       &lt;attribute name=&quot;mzRangeStop&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}float&quot; /&gt;
	 *     &lt;/extension&gt;
	 *   &lt;/complexContent&gt;
	 * &lt;/complexType&gt;
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "")
	public static class SpectrumInstrument extends ParamType {

		@XmlAttribute(required = true)
		protected int msLevel;
		@XmlAttribute
		protected Float mzRangeStart;
		@XmlAttribute
		protected Float mzRangeStop;

		/**
		 * Gets the value of the msLevel property.
		 * 
		 */
		public int getMsLevel() {
			return this.msLevel;
		}

		/**
		 * Gets the value of the mzRangeStart property.
		 * 
		 * @return possible object is {@link Float }
		 * 
		 */
		public Float getMzRangeStart() {
			return this.mzRangeStart;
		}

		/**
		 * Gets the value of the mzRangeStop property.
		 * 
		 * @return possible object is {@link Float }
		 * 
		 */
		public Float getMzRangeStop() {
			return this.mzRangeStop;
		}

		/**
		 * Sets the value of the msLevel property.
		 * 
		 */
		public void setMsLevel(final int value) {
			this.msLevel = value;
		}

		/**
		 * Sets the value of the mzRangeStart property.
		 * 
		 * @param value
		 *            allowed object is {@link Float }
		 * 
		 */
		public void setMzRangeStart(final Float value) {
			this.mzRangeStart = value;
		}

		/**
		 * Sets the value of the mzRangeStop property.
		 * 
		 * @param value
		 *            allowed object is {@link Float }
		 * 
		 */
		public void setMzRangeStop(final Float value) {
			this.mzRangeStop = value;
		}

	}

	protected SpectrumSettingsType.AcqSpecification acqSpecification;

	@XmlElement(required = true)
	protected SpectrumSettingsType.SpectrumInstrument spectrumInstrument;

	/**
	 * Gets the value of the acqSpecification property.
	 * 
	 * @return possible object is {@link SpectrumSettingsType.AcqSpecification }
	 * 
	 */
	public SpectrumSettingsType.AcqSpecification getAcqSpecification() {
		return this.acqSpecification;
	}

	/**
	 * Gets the value of the spectrumInstrument property.
	 * 
	 * @return possible object is
	 *         {@link SpectrumSettingsType.SpectrumInstrument }
	 * 
	 */
	public SpectrumSettingsType.SpectrumInstrument getSpectrumInstrument() {
		return this.spectrumInstrument;
	}

	/**
	 * Sets the value of the acqSpecification property.
	 * 
	 * @param value
	 *            allowed object is
	 *            {@link SpectrumSettingsType.AcqSpecification }
	 * 
	 */
	public void setAcqSpecification(
	        final SpectrumSettingsType.AcqSpecification value) {
		this.acqSpecification = value;
	}

	/**
	 * Sets the value of the spectrumInstrument property.
	 * 
	 * @param value
	 *            allowed object is
	 *            {@link SpectrumSettingsType.SpectrumInstrument }
	 * 
	 */
	public void setSpectrumInstrument(
	        final SpectrumSettingsType.SpectrumInstrument value) {
		this.spectrumInstrument = value;
	}

}
