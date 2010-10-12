/*
 * $Id$
 */

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB)
// Reference Implementation, vhudson-jaxb-ri-2.1-646
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source
// schema.
// Generated on: 2008.10.14 at 08:55:16 AM CEST
//

package maltcms.io.xml.mzML;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Information pertaining to the entire mzML file (i.e. not specific to any part
 * of the data set) is stored here.
 * 
 * <p>
 * Java class for FileDescriptionType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name=&quot;FileDescriptionType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;fileContent&quot; type=&quot;{http://psi.hupo.org/schema_revision/mzML_1.0.0}ParamGroupType&quot;/&gt;
 *         &lt;element name=&quot;sourceFileList&quot; type=&quot;{http://psi.hupo.org/schema_revision/mzML_1.0.0}SourceFileListType&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;contact&quot; type=&quot;{http://psi.hupo.org/schema_revision/mzML_1.0.0}ParamGroupType&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FileDescriptionType", propOrder = { "fileContent",
        "sourceFileList", "contact" })
public class FileDescriptionType {

	@XmlElement(required = true)
	protected ParamGroupType fileContent;
	protected SourceFileListType sourceFileList;
	protected List<ParamGroupType> contact;

	/**
	 * Gets the value of the contact property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the contact property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getContact().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ParamGroupType }
	 * 
	 * 
	 */
	public List<ParamGroupType> getContact() {
		if (this.contact == null) {
			this.contact = new ArrayList<ParamGroupType>();
		}
		return this.contact;
	}

	/**
	 * Gets the value of the fileContent property.
	 * 
	 * @return possible object is {@link ParamGroupType }
	 * 
	 */
	public ParamGroupType getFileContent() {
		return this.fileContent;
	}

	/**
	 * Gets the value of the sourceFileList property.
	 * 
	 * @return possible object is {@link SourceFileListType }
	 * 
	 */
	public SourceFileListType getSourceFileList() {
		return this.sourceFileList;
	}

	/**
	 * Sets the value of the fileContent property.
	 * 
	 * @param value
	 *            allowed object is {@link ParamGroupType }
	 * 
	 */
	public void setFileContent(final ParamGroupType value) {
		this.fileContent = value;
	}

	/**
	 * Sets the value of the sourceFileList property.
	 * 
	 * @param value
	 *            allowed object is {@link SourceFileListType }
	 * 
	 */
	public void setSourceFileList(final SourceFileListType value) {
		this.sourceFileList = value;
	}

}