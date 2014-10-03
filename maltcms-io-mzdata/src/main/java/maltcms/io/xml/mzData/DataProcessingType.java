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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Description of the software, and the way in which it was used to generate the
 * peak list.
 *
 * <p>
 * Java class for dataProcessingType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name=&quot;dataProcessingType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;software&quot;&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;extension base=&quot;{}softwareType&quot;&gt;
 *               &lt;/extension&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name=&quot;processingMethod&quot; type=&quot;{}paramType&quot; minOccurs=&quot;0&quot;/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 * @author hoffmann
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dataProcessingType", propOrder = {"software",
    "processingMethod"})
public class DataProcessingType implements Serializable {

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
     *     &lt;extension base=&quot;{}softwareType&quot;&gt;
     *     &lt;/extension&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Software extends SoftwareType {
    }
    @XmlElement(required = true)
    protected DataProcessingType.Software software;
    protected ParamType processingMethod;

    /**
     * Gets the value of the processingMethod property.
     *
     * @return possible object is {@link maltcms.io.xml.mzData.ParamType}
     */
    public ParamType getProcessingMethod() {
        return this.processingMethod;
    }

    /**
     * Gets the value of the software property.
     *
     * @return possible object is {@link maltcms.io.xml.mzData.DataProcessingType.Software}
     */
    public DataProcessingType.Software getSoftware() {
        return this.software;
    }

    /**
     * Sets the value of the processingMethod property.
     *
     * @param value allowed object is {@link maltcms.io.xml.mzData.ParamType}
     */
    public void setProcessingMethod(final ParamType value) {
        this.processingMethod = value;
    }

    /**
     * Sets the value of the software property.
     *
     * @param value allowed object is {@link maltcms.io.xml.mzData.DataProcessingType.Software}
     */
    public void setSoftware(final DataProcessingType.Software value) {
        this.software = value;
    }
}
