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

/**
 * The method of precursor ion selection and activation
 *
 * <p> Java class for precursorType complex type.
 *
 * <p> The following schema fragment specifies the expected content contained
 * within this class.
 *
 * <pre>
 * &lt;complexType name=&quot;precursorType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;ionSelection&quot; type=&quot;{}paramType&quot;/&gt;
 *         &lt;element name=&quot;activation&quot; type=&quot;{}paramType&quot;/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name=&quot;msLevel&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; /&gt;
 *       &lt;attribute name=&quot;spectrumRef&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "precursorType", propOrder = {"ionSelection", "activation"})
public class PrecursorType {

    @XmlElement(required = true)
    protected ParamType ionSelection;
    @XmlElement(required = true)
    protected ParamType activation;
    @XmlAttribute(required = true)
    protected int msLevel;
    @XmlAttribute(required = true)
    protected int spectrumRef;

    /**
     * Gets the value of the activation property.
     *
     * @return possible object is {@link ParamType }
     *
     */
    public ParamType getActivation() {
        return this.activation;
    }

    /**
     * Gets the value of the ionSelection property.
     *
     * @return possible object is {@link ParamType }
     *
     */
    public ParamType getIonSelection() {
        return this.ionSelection;
    }

    /**
     * Gets the value of the msLevel property.
     *
     */
    public int getMsLevel() {
        return this.msLevel;
    }

    /**
     * Gets the value of the spectrumRef property.
     *
     */
    public int getSpectrumRef() {
        return this.spectrumRef;
    }

    /**
     * Sets the value of the activation property.
     *
     * @param value allowed object is {@link ParamType }
     *
     */
    public void setActivation(final ParamType value) {
        this.activation = value;
    }

    /**
     * Sets the value of the ionSelection property.
     *
     * @param value allowed object is {@link ParamType }
     *
     */
    public void setIonSelection(final ParamType value) {
        this.ionSelection = value;
    }

    /**
     * Sets the value of the msLevel property.
     *
     */
    public void setMsLevel(final int value) {
        this.msLevel = value;
    }

    /**
     * Sets the value of the spectrumRef property.
     *
     */
    public void setSpectrumRef(final int value) {
        this.spectrumRef = value;
    }
}
