//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2013.09.18 um 06:01:26 PM CEST 
//


package maltcms.io.xml.bindings.alignment;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://maltcms.sourceforge.net/maltcmsAlignment}mappedPoints" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="generator" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="isCompleteMap" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="numberOfMaps" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "mappedPoints"
})
@XmlRootElement(name = "alignment")
public class Alignment {

    @XmlElement(required = true)
    protected List<MappedPointsType> mappedPoints;
    @XmlAttribute(name = "type", required = true)
    protected String type;
    @XmlAttribute(name = "generator", required = true)
    protected String generator;
    @XmlAttribute(name = "isCompleteMap", required = true)
    protected boolean isCompleteMap;
    @XmlAttribute(name = "numberOfMaps", required = true)
    protected BigInteger numberOfMaps;

    /**
     * Gets the value of the mappedPoints property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mappedPoints property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMappedPoints().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MappedPointsType }
     * 
     * 
     */
    public List<MappedPointsType> getMappedPoints() {
        if (mappedPoints == null) {
            mappedPoints = new ArrayList<MappedPointsType>();
        }
        return this.mappedPoints;
    }

    /**
     * Ruft den Wert der type-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Legt den Wert der type-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Ruft den Wert der generator-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGenerator() {
        return generator;
    }

    /**
     * Legt den Wert der generator-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGenerator(String value) {
        this.generator = value;
    }

    /**
     * Ruft den Wert der isCompleteMap-Eigenschaft ab.
     * 
     */
    public boolean isIsCompleteMap() {
        return isCompleteMap;
    }

    /**
     * Legt den Wert der isCompleteMap-Eigenschaft fest.
     * 
     */
    public void setIsCompleteMap(boolean value) {
        this.isCompleteMap = value;
    }

    /**
     * Ruft den Wert der numberOfMaps-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNumberOfMaps() {
        return numberOfMaps;
    }

    /**
     * Legt den Wert der numberOfMaps-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNumberOfMaps(BigInteger value) {
        this.numberOfMaps = value;
    }

}
