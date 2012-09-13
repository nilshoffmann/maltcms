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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Description of a supplemental data array
 *
 * <p> Java class for supDescType complex type.
 *
 * <p> The following schema fragment specifies the expected content contained
 * within this class.
 *
 * <pre>
 * &lt;complexType name=&quot;supDescType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;supDataDesc&quot; type=&quot;{}descriptionType&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;supSourceFile&quot; type=&quot;{}sourceFileType&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name=&quot;supDataArrayRef&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "supDescType", propOrder = {"supDataDesc", "supSourceFile"})
public class SupDescType {

    protected DescriptionType supDataDesc;
    protected List<SourceFileType> supSourceFile;
    @XmlAttribute(required = true)
    protected int supDataArrayRef;

    /**
     * Gets the value of the supDataArrayRef property.
     *
     */
    public int getSupDataArrayRef() {
        return this.supDataArrayRef;
    }

    /**
     * Gets the value of the supDataDesc property.
     *
     * @return possible object is {@link DescriptionType }
     *
     */
    public DescriptionType getSupDataDesc() {
        return this.supDataDesc;
    }

    /**
     * Gets the value of the supSourceFile property.
     *
     * <p> This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the supSourceFile property.
     *
     * <p> For example, to add a new item, do as follows:
     *
     * <pre>
     * getSupSourceFile().add(newItem);
     * </pre>
     *
     *
     * <p> Objects of the following type(s) are allowed in the list
	 * {@link SourceFileType }
     *
     *
     */
    public List<SourceFileType> getSupSourceFile() {
        if (this.supSourceFile == null) {
            this.supSourceFile = new ArrayList<SourceFileType>();
        }
        return this.supSourceFile;
    }

    /**
     * Sets the value of the supDataArrayRef property.
     *
     */
    public void setSupDataArrayRef(final int value) {
        this.supDataArrayRef = value;
    }

    /**
     * Sets the value of the supDataDesc property.
     *
     * @param value allowed object is {@link DescriptionType }
     *
     */
    public void setSupDataDesc(final DescriptionType value) {
        this.supDataDesc = value;
    }
}
