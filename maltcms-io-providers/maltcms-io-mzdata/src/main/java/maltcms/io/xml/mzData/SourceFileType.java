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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * Description of the source file, including location and type.
 *
 * <p>
 * Java class for sourceFileType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name=&quot;sourceFileType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;nameOfFile&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;/&gt;
 *         &lt;element name=&quot;pathToFile&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}anyURI&quot;/&gt;
 *         &lt;element name=&quot;fileType&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 * @author Nils Hoffmann
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sourceFileType", propOrder = {"nameOfFile", "pathToFile",
    "fileType"})
public class SourceFileType implements Serializable {

    @XmlElement(required = true)
    protected String nameOfFile;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String pathToFile;
    protected String fileType;

    /**
     * Gets the value of the fileType property.
     *
     * @return possible object is {@link java.lang.String}
     */
    public String getFileType() {
        return this.fileType;
    }

    /**
     * Gets the value of the nameOfFile property.
     *
     * @return possible object is {@link java.lang.String}
     */
    public String getNameOfFile() {
        return this.nameOfFile;
    }

    /**
     * Gets the value of the pathToFile property.
     *
     * @return possible object is {@link java.lang.String}
     */
    public String getPathToFile() {
        return this.pathToFile;
    }

    /**
     * Sets the value of the fileType property.
     *
     * @param value allowed object is {@link java.lang.String}
     */
    public void setFileType(final String value) {
        this.fileType = value;
    }

    /**
     * Sets the value of the nameOfFile property.
     *
     * @param value allowed object is {@link java.lang.String}
     */
    public void setNameOfFile(final String value) {
        this.nameOfFile = value;
    }

    /**
     * Sets the value of the pathToFile property.
     *
     * @param value allowed object is {@link java.lang.String}
     */
    public void setPathToFile(final String value) {
        this.pathToFile = value;
    }
}
