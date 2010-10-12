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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Description of the acquisition settings of the instrument prior to the start
 * of the run.
 * 
 * <p>
 * Java class for AcquisitionSettingsType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name=&quot;AcquisitionSettingsType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base=&quot;{http://psi.hupo.org/schema_revision/mzML_1.0.0}ParamGroupType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;sourceFileRefList&quot; type=&quot;{http://psi.hupo.org/schema_revision/mzML_1.0.0}SourceFileRefListType&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;targetList&quot; type=&quot;{http://psi.hupo.org/schema_revision/mzML_1.0.0}TargetListType&quot; minOccurs=&quot;0&quot;/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name=&quot;id&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}ID&quot; /&gt;
 *       &lt;attribute name=&quot;instrumentConfigurationRef&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}IDREF&quot; /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AcquisitionSettingsType", propOrder = { "sourceFileRefList",
        "targetList" })
public class AcquisitionSettingsType extends ParamGroupType {

	protected SourceFileRefListType sourceFileRefList;
	protected TargetListType targetList;
	@XmlAttribute(required = true)
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlID
	@XmlSchemaType(name = "ID")
	protected String id;
	@XmlAttribute(required = true)
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	protected Object instrumentConfigurationRef;

	/**
	 * Gets the value of the id property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Gets the value of the instrumentConfigurationRef property.
	 * 
	 * @return possible object is {@link Object }
	 * 
	 */
	public Object getInstrumentConfigurationRef() {
		return this.instrumentConfigurationRef;
	}

	/**
	 * Gets the value of the sourceFileRefList property.
	 * 
	 * @return possible object is {@link SourceFileRefListType }
	 * 
	 */
	public SourceFileRefListType getSourceFileRefList() {
		return this.sourceFileRefList;
	}

	/**
	 * Gets the value of the targetList property.
	 * 
	 * @return possible object is {@link TargetListType }
	 * 
	 */
	public TargetListType getTargetList() {
		return this.targetList;
	}

	/**
	 * Sets the value of the id property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setId(final String value) {
		this.id = value;
	}

	/**
	 * Sets the value of the instrumentConfigurationRef property.
	 * 
	 * @param value
	 *            allowed object is {@link Object }
	 * 
	 */
	public void setInstrumentConfigurationRef(final Object value) {
		this.instrumentConfigurationRef = value;
	}

	/**
	 * Sets the value of the sourceFileRefList property.
	 * 
	 * @param value
	 *            allowed object is {@link SourceFileRefListType }
	 * 
	 */
	public void setSourceFileRefList(final SourceFileRefListType value) {
		this.sourceFileRefList = value;
	}

	/**
	 * Sets the value of the targetList property.
	 * 
	 * @param value
	 *            allowed object is {@link TargetListType }
	 * 
	 */
	public void setTargetList(final TargetListType value) {
		this.targetList = value;
	}

}
