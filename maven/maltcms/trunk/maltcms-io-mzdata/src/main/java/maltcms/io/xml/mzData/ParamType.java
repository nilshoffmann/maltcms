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
