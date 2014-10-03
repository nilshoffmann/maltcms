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

/**
 * The method of precursor ion selection and activation
 *
 * <p>
 * Java class for precursorType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
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
 * @author hoffmann
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "precursorType", propOrder = {"ionSelection", "activation"})
public class PrecursorType implements Serializable {

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
     * @return possible object is {@link maltcms.io.xml.mzData.ParamType}
     */
    public ParamType getActivation() {
        return this.activation;
    }

    /**
     * Gets the value of the ionSelection property.
     *
     * @return possible object is {@link maltcms.io.xml.mzData.ParamType}
     */
    public ParamType getIonSelection() {
        return this.ionSelection;
    }

    /**
     * Gets the value of the msLevel property.
     *
     * @return a int.
     */
    public int getMsLevel() {
        return this.msLevel;
    }

    /**
     * Gets the value of the spectrumRef property.
     *
     * @return a int.
     */
    public int getSpectrumRef() {
        return this.spectrumRef;
    }

    /**
     * Sets the value of the activation property.
     *
     * @param value allowed object is {@link maltcms.io.xml.mzData.ParamType}
     */
    public void setActivation(final ParamType value) {
        this.activation = value;
    }

    /**
     * Sets the value of the ionSelection property.
     *
     * @param value allowed object is {@link maltcms.io.xml.mzData.ParamType}
     */
    public void setIonSelection(final ParamType value) {
        this.ionSelection = value;
    }

    /**
     * Sets the value of the msLevel property.
     *
     * @param value a int.
     */
    public void setMsLevel(final int value) {
        this.msLevel = value;
    }

    /**
     * Sets the value of the spectrumRef property.
     *
     * @param value a int.
     */
    public void setSpectrumRef(final int value) {
        this.spectrumRef = value;
    }
}
