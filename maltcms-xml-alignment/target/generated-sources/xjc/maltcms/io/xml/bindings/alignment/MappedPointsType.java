//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2013.09.03 um 03:57:56 PM CEST 
//


package maltcms.io.xml.bindings.alignment;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für mappedPointsType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="mappedPointsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://maltcms.sourceforge.net/maltcmsAlignment}resource"/>
 *         &lt;element ref="{http://maltcms.sourceforge.net/maltcmsAlignment}pointMap"/>
 *       &lt;/sequence>
 *       &lt;attribute name="isAlignmentReference" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mappedPointsType", propOrder = {
    "resource",
    "pointMap"
})
public class MappedPointsType {

    @XmlElement(required = true)
    protected ResourceType resource;
    @XmlElement(required = true)
    protected PointMapType pointMap;
    @XmlAttribute(name = "isAlignmentReference", required = true)
    protected boolean isAlignmentReference;

    /**
     * Ruft den Wert der resource-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ResourceType }
     *     
     */
    public ResourceType getResource() {
        return resource;
    }

    /**
     * Legt den Wert der resource-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ResourceType }
     *     
     */
    public void setResource(ResourceType value) {
        this.resource = value;
    }

    /**
     * Ruft den Wert der pointMap-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PointMapType }
     *     
     */
    public PointMapType getPointMap() {
        return pointMap;
    }

    /**
     * Legt den Wert der pointMap-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PointMapType }
     *     
     */
    public void setPointMap(PointMapType value) {
        this.pointMap = value;
    }

    /**
     * Ruft den Wert der isAlignmentReference-Eigenschaft ab.
     * 
     */
    public boolean isIsAlignmentReference() {
        return isAlignmentReference;
    }

    /**
     * Legt den Wert der isAlignmentReference-Eigenschaft fest.
     * 
     */
    public void setIsAlignmentReference(boolean value) {
        this.isAlignmentReference = value;
    }

}
