/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * Structure allowing the use of controlled or uncontrolled vocabulary
 *
 * <p> Java class for paramType complex type.
 *
 * <p> The following schema fragment specifies the expected content contained
 * within this class.
 *
 * <pre>
 * &lt;complexType name=&quot;paramType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;choice maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;&gt;
 *         &lt;element name=&quot;cvParam&quot; type=&quot;{}cvParamType&quot;/&gt;
 *         &lt;element name=&quot;userParam&quot; type=&quot;{}userParamType&quot;/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "paramType", propOrder = {"cvParamOrUserParam"})
@XmlSeeAlso({
    maltcms.io.xml.mzData.SpectrumSettingsType.AcqSpecification.Acquisition.class,
    maltcms.io.xml.mzData.SpectrumSettingsType.SpectrumInstrument.class,
    DescriptionType.class})
public class ParamType {

    @XmlElements({
        @XmlElement(name = "cvParam", type = CvParamType.class),
        @XmlElement(name = "userParam", type = UserParamType.class)})
    protected List<Object> cvParamOrUserParam;

    /**
     * Gets the value of the cvParamOrUserParam property.
     *
     * <p> This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the cvParamOrUserParam property.
     *
     * <p> For example, to add a new item, do as follows:
     *
     * <pre>
     * getCvParamOrUserParam().add(newItem);
     * </pre>
     *
     *
     * <p> Objects of the following type(s) are allowed in the list
	 * {@link CvParamType } {@link UserParamType }
     *
     *
     */
    public List<Object> getCvParamOrUserParam() {
        if (this.cvParamOrUserParam == null) {
            this.cvParamOrUserParam = new ArrayList<Object>();
        }
        return this.cvParamOrUserParam;
    }
}
