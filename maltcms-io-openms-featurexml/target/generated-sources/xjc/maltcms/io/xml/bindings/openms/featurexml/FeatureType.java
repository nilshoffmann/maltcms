//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2013.09.03 um 04:00:41 PM CEST 
//


package maltcms.io.xml.bindings.openms.featurexml;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java-Klasse für featureType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="featureType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="position" maxOccurs="2" minOccurs="2">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>anySimpleType">
 *                 &lt;attribute name="dim" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="intensity" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="quality" maxOccurs="2" minOccurs="0">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>anySimpleType">
 *                 &lt;attribute name="dim" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="overallquality" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="charge" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="model" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="param" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                           &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="convexhull" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="hullpoint" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="hposition" maxOccurs="2" minOccurs="2">
 *                               &lt;complexType>
 *                                 &lt;simpleContent>
 *                                   &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>anySimpleType">
 *                                     &lt;attribute name="dim" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                                   &lt;/extension>
 *                                 &lt;/simpleContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="nr" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="subordinate" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="feature" type="{}featureType" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="PeptideIdentification" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="PeptideHit" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence minOccurs="0">
 *                             &lt;element name="userParam" type="{}userParam" maxOccurs="unbounded" minOccurs="0"/>
 *                           &lt;/sequence>
 *                           &lt;attribute name="sequence" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="charge" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *                           &lt;attribute name="score" use="required" type="{http://www.w3.org/2001/XMLSchema}float" />
 *                           &lt;attribute name="aa_before">
 *                             &lt;simpleType>
 *                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                 &lt;minLength value="0"/>
 *                                 &lt;maxLength value="1"/>
 *                               &lt;/restriction>
 *                             &lt;/simpleType>
 *                           &lt;/attribute>
 *                           &lt;attribute name="aa_after">
 *                             &lt;simpleType>
 *                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                 &lt;minLength value="0"/>
 *                                 &lt;maxLength value="1"/>
 *                               &lt;/restriction>
 *                             &lt;/simpleType>
 *                           &lt;/attribute>
 *                           &lt;attribute name="protein_refs" type="{http://www.w3.org/2001/XMLSchema}IDREFS" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="userParam" type="{}userParam" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="identification_run_ref" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *                 &lt;attribute name="score_type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="higher_score_better" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *                 &lt;attribute name="significance_threshold" type="{http://www.w3.org/2001/XMLSchema}float" />
 *                 &lt;attribute name="spectrum_reference" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *                 &lt;attribute name="RT" type="{http://www.w3.org/2001/XMLSchema}float" />
 *                 &lt;attribute name="MZ" type="{http://www.w3.org/2001/XMLSchema}float" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="userParam" type="{}userParam" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "featureType", propOrder = {
    "position",
    "intensity",
    "quality",
    "overallquality",
    "charge",
    "model",
    "convexhull",
    "subordinate",
    "peptideIdentification",
    "userParam"
})
public class FeatureType {

    @XmlElement(required = true)
    protected List<FeatureType.Position> position;
    protected double intensity;
    protected List<FeatureType.Quality> quality;
    protected Double overallquality;
    protected Double charge;
    protected FeatureType.Model model;
    protected List<FeatureType.Convexhull> convexhull;
    protected FeatureType.Subordinate subordinate;
    @XmlElement(name = "PeptideIdentification")
    protected List<FeatureType.PeptideIdentification> peptideIdentification;
    protected List<UserParam> userParam;
    @XmlAttribute(name = "id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;

    /**
     * Gets the value of the position property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the position property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPosition().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FeatureType.Position }
     * 
     * 
     */
    public List<FeatureType.Position> getPosition() {
        if (position == null) {
            position = new ArrayList<FeatureType.Position>();
        }
        return this.position;
    }

    /**
     * Ruft den Wert der intensity-Eigenschaft ab.
     * 
     */
    public double getIntensity() {
        return intensity;
    }

    /**
     * Legt den Wert der intensity-Eigenschaft fest.
     * 
     */
    public void setIntensity(double value) {
        this.intensity = value;
    }

    /**
     * Gets the value of the quality property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the quality property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getQuality().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FeatureType.Quality }
     * 
     * 
     */
    public List<FeatureType.Quality> getQuality() {
        if (quality == null) {
            quality = new ArrayList<FeatureType.Quality>();
        }
        return this.quality;
    }

    /**
     * Ruft den Wert der overallquality-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getOverallquality() {
        return overallquality;
    }

    /**
     * Legt den Wert der overallquality-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setOverallquality(Double value) {
        this.overallquality = value;
    }

    /**
     * Ruft den Wert der charge-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getCharge() {
        return charge;
    }

    /**
     * Legt den Wert der charge-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setCharge(Double value) {
        this.charge = value;
    }

    /**
     * Ruft den Wert der model-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FeatureType.Model }
     *     
     */
    public FeatureType.Model getModel() {
        return model;
    }

    /**
     * Legt den Wert der model-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FeatureType.Model }
     *     
     */
    public void setModel(FeatureType.Model value) {
        this.model = value;
    }

    /**
     * Gets the value of the convexhull property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the convexhull property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConvexhull().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FeatureType.Convexhull }
     * 
     * 
     */
    public List<FeatureType.Convexhull> getConvexhull() {
        if (convexhull == null) {
            convexhull = new ArrayList<FeatureType.Convexhull>();
        }
        return this.convexhull;
    }

    /**
     * Ruft den Wert der subordinate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FeatureType.Subordinate }
     *     
     */
    public FeatureType.Subordinate getSubordinate() {
        return subordinate;
    }

    /**
     * Legt den Wert der subordinate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FeatureType.Subordinate }
     *     
     */
    public void setSubordinate(FeatureType.Subordinate value) {
        this.subordinate = value;
    }

    /**
     * Gets the value of the peptideIdentification property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the peptideIdentification property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPeptideIdentification().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FeatureType.PeptideIdentification }
     * 
     * 
     */
    public List<FeatureType.PeptideIdentification> getPeptideIdentification() {
        if (peptideIdentification == null) {
            peptideIdentification = new ArrayList<FeatureType.PeptideIdentification>();
        }
        return this.peptideIdentification;
    }

    /**
     * Gets the value of the userParam property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the userParam property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUserParam().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link UserParam }
     * 
     * 
     */
    public List<UserParam> getUserParam() {
        if (userParam == null) {
            userParam = new ArrayList<UserParam>();
        }
        return this.userParam;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }


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
     *         &lt;element name="hullpoint" maxOccurs="unbounded">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="hposition" maxOccurs="2" minOccurs="2">
     *                     &lt;complexType>
     *                       &lt;simpleContent>
     *                         &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>anySimpleType">
     *                           &lt;attribute name="dim" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *                         &lt;/extension>
     *                       &lt;/simpleContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="nr" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "hullpoint"
    })
    public static class Convexhull {

        @XmlElement(required = true)
        protected List<FeatureType.Convexhull.Hullpoint> hullpoint;
        @XmlAttribute(name = "nr")
        @XmlSchemaType(name = "anySimpleType")
        protected String nr;

        /**
         * Gets the value of the hullpoint property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the hullpoint property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getHullpoint().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link FeatureType.Convexhull.Hullpoint }
         * 
         * 
         */
        public List<FeatureType.Convexhull.Hullpoint> getHullpoint() {
            if (hullpoint == null) {
                hullpoint = new ArrayList<FeatureType.Convexhull.Hullpoint>();
            }
            return this.hullpoint;
        }

        /**
         * Ruft den Wert der nr-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getNr() {
            return nr;
        }

        /**
         * Legt den Wert der nr-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setNr(String value) {
            this.nr = value;
        }


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
         *         &lt;element name="hposition" maxOccurs="2" minOccurs="2">
         *           &lt;complexType>
         *             &lt;simpleContent>
         *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>anySimpleType">
         *                 &lt;attribute name="dim" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
         *               &lt;/extension>
         *             &lt;/simpleContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "hposition"
        })
        public static class Hullpoint {

            @XmlElement(required = true)
            protected List<FeatureType.Convexhull.Hullpoint.Hposition> hposition;

            /**
             * Gets the value of the hposition property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the hposition property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getHposition().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link FeatureType.Convexhull.Hullpoint.Hposition }
             * 
             * 
             */
            public List<FeatureType.Convexhull.Hullpoint.Hposition> getHposition() {
                if (hposition == null) {
                    hposition = new ArrayList<FeatureType.Convexhull.Hullpoint.Hposition>();
                }
                return this.hposition;
            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;simpleContent>
             *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>anySimpleType">
             *       &lt;attribute name="dim" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
             *     &lt;/extension>
             *   &lt;/simpleContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "value"
            })
            public static class Hposition {

                @XmlValue
                @XmlSchemaType(name = "anySimpleType")
                protected Object value;
                @XmlAttribute(name = "dim", required = true)
                @XmlSchemaType(name = "anySimpleType")
                protected String dim;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Object }
                 *     
                 */
                public Object getValue() {
                    return value;
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Object }
                 *     
                 */
                public void setValue(Object value) {
                    this.value = value;
                }

                /**
                 * Ruft den Wert der dim-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getDim() {
                    return dim;
                }

                /**
                 * Legt den Wert der dim-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setDim(String value) {
                    this.dim = value;
                }

            }

        }

    }


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
     *         &lt;element name="param" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *                 &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "param"
    })
    public static class Model {

        protected List<FeatureType.Model.Param> param;
        @XmlAttribute(name = "name", required = true)
        @XmlSchemaType(name = "anySimpleType")
        protected String name;

        /**
         * Gets the value of the param property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the param property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getParam().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link FeatureType.Model.Param }
         * 
         * 
         */
        public List<FeatureType.Model.Param> getParam() {
            if (param == null) {
                param = new ArrayList<FeatureType.Model.Param>();
            }
            return this.param;
        }

        /**
         * Ruft den Wert der name-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getName() {
            return name;
        }

        /**
         * Legt den Wert der name-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setName(String value) {
            this.name = value;
        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
         *       &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Param {

            @XmlAttribute(name = "name", required = true)
            @XmlSchemaType(name = "anySimpleType")
            protected String name;
            @XmlAttribute(name = "value", required = true)
            @XmlSchemaType(name = "anySimpleType")
            protected String value;

            /**
             * Ruft den Wert der name-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getName() {
                return name;
            }

            /**
             * Legt den Wert der name-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setName(String value) {
                this.name = value;
            }

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setValue(String value) {
                this.value = value;
            }

        }

    }


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
     *         &lt;element name="PeptideHit" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence minOccurs="0">
     *                   &lt;element name="userParam" type="{}userParam" maxOccurs="unbounded" minOccurs="0"/>
     *                 &lt;/sequence>
     *                 &lt;attribute name="sequence" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="charge" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
     *                 &lt;attribute name="score" use="required" type="{http://www.w3.org/2001/XMLSchema}float" />
     *                 &lt;attribute name="aa_before">
     *                   &lt;simpleType>
     *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                       &lt;minLength value="0"/>
     *                       &lt;maxLength value="1"/>
     *                     &lt;/restriction>
     *                   &lt;/simpleType>
     *                 &lt;/attribute>
     *                 &lt;attribute name="aa_after">
     *                   &lt;simpleType>
     *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                       &lt;minLength value="0"/>
     *                       &lt;maxLength value="1"/>
     *                     &lt;/restriction>
     *                   &lt;/simpleType>
     *                 &lt;/attribute>
     *                 &lt;attribute name="protein_refs" type="{http://www.w3.org/2001/XMLSchema}IDREFS" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="userParam" type="{}userParam" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *       &lt;attribute name="identification_run_ref" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
     *       &lt;attribute name="score_type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="higher_score_better" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
     *       &lt;attribute name="significance_threshold" type="{http://www.w3.org/2001/XMLSchema}float" />
     *       &lt;attribute name="spectrum_reference" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
     *       &lt;attribute name="RT" type="{http://www.w3.org/2001/XMLSchema}float" />
     *       &lt;attribute name="MZ" type="{http://www.w3.org/2001/XMLSchema}float" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "peptideHit",
        "userParam"
    })
    public static class PeptideIdentification {

        @XmlElement(name = "PeptideHit")
        protected List<FeatureType.PeptideIdentification.PeptideHit> peptideHit;
        protected List<UserParam> userParam;
        @XmlAttribute(name = "identification_run_ref", required = true)
        @XmlIDREF
        @XmlSchemaType(name = "IDREF")
        protected Object identificationRunRef;
        @XmlAttribute(name = "score_type", required = true)
        protected String scoreType;
        @XmlAttribute(name = "higher_score_better", required = true)
        protected boolean higherScoreBetter;
        @XmlAttribute(name = "significance_threshold")
        protected Float significanceThreshold;
        @XmlAttribute(name = "spectrum_reference")
        @XmlSchemaType(name = "unsignedInt")
        protected Long spectrumReference;
        @XmlAttribute(name = "RT")
        protected Float rt;
        @XmlAttribute(name = "MZ")
        protected Float mz;

        /**
         * Gets the value of the peptideHit property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the peptideHit property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getPeptideHit().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link FeatureType.PeptideIdentification.PeptideHit }
         * 
         * 
         */
        public List<FeatureType.PeptideIdentification.PeptideHit> getPeptideHit() {
            if (peptideHit == null) {
                peptideHit = new ArrayList<FeatureType.PeptideIdentification.PeptideHit>();
            }
            return this.peptideHit;
        }

        /**
         * Gets the value of the userParam property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the userParam property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getUserParam().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link UserParam }
         * 
         * 
         */
        public List<UserParam> getUserParam() {
            if (userParam == null) {
                userParam = new ArrayList<UserParam>();
            }
            return this.userParam;
        }

        /**
         * Ruft den Wert der identificationRunRef-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Object }
         *     
         */
        public Object getIdentificationRunRef() {
            return identificationRunRef;
        }

        /**
         * Legt den Wert der identificationRunRef-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Object }
         *     
         */
        public void setIdentificationRunRef(Object value) {
            this.identificationRunRef = value;
        }

        /**
         * Ruft den Wert der scoreType-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getScoreType() {
            return scoreType;
        }

        /**
         * Legt den Wert der scoreType-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setScoreType(String value) {
            this.scoreType = value;
        }

        /**
         * Ruft den Wert der higherScoreBetter-Eigenschaft ab.
         * 
         */
        public boolean isHigherScoreBetter() {
            return higherScoreBetter;
        }

        /**
         * Legt den Wert der higherScoreBetter-Eigenschaft fest.
         * 
         */
        public void setHigherScoreBetter(boolean value) {
            this.higherScoreBetter = value;
        }

        /**
         * Ruft den Wert der significanceThreshold-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Float }
         *     
         */
        public Float getSignificanceThreshold() {
            return significanceThreshold;
        }

        /**
         * Legt den Wert der significanceThreshold-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Float }
         *     
         */
        public void setSignificanceThreshold(Float value) {
            this.significanceThreshold = value;
        }

        /**
         * Ruft den Wert der spectrumReference-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Long }
         *     
         */
        public Long getSpectrumReference() {
            return spectrumReference;
        }

        /**
         * Legt den Wert der spectrumReference-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Long }
         *     
         */
        public void setSpectrumReference(Long value) {
            this.spectrumReference = value;
        }

        /**
         * Ruft den Wert der rt-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Float }
         *     
         */
        public Float getRT() {
            return rt;
        }

        /**
         * Legt den Wert der rt-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Float }
         *     
         */
        public void setRT(Float value) {
            this.rt = value;
        }

        /**
         * Ruft den Wert der mz-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Float }
         *     
         */
        public Float getMZ() {
            return mz;
        }

        /**
         * Legt den Wert der mz-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Float }
         *     
         */
        public void setMZ(Float value) {
            this.mz = value;
        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence minOccurs="0">
         *         &lt;element name="userParam" type="{}userParam" maxOccurs="unbounded" minOccurs="0"/>
         *       &lt;/sequence>
         *       &lt;attribute name="sequence" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="charge" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
         *       &lt;attribute name="score" use="required" type="{http://www.w3.org/2001/XMLSchema}float" />
         *       &lt;attribute name="aa_before">
         *         &lt;simpleType>
         *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
         *             &lt;minLength value="0"/>
         *             &lt;maxLength value="1"/>
         *           &lt;/restriction>
         *         &lt;/simpleType>
         *       &lt;/attribute>
         *       &lt;attribute name="aa_after">
         *         &lt;simpleType>
         *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
         *             &lt;minLength value="0"/>
         *             &lt;maxLength value="1"/>
         *           &lt;/restriction>
         *         &lt;/simpleType>
         *       &lt;/attribute>
         *       &lt;attribute name="protein_refs" type="{http://www.w3.org/2001/XMLSchema}IDREFS" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "userParam"
        })
        public static class PeptideHit {

            protected List<UserParam> userParam;
            @XmlAttribute(name = "sequence", required = true)
            protected String sequence;
            @XmlAttribute(name = "charge", required = true)
            protected BigInteger charge;
            @XmlAttribute(name = "score", required = true)
            protected float score;
            @XmlAttribute(name = "aa_before")
            protected String aaBefore;
            @XmlAttribute(name = "aa_after")
            protected String aaAfter;
            @XmlAttribute(name = "protein_refs")
            @XmlIDREF
            @XmlSchemaType(name = "IDREFS")
            protected List<Object> proteinRefs;

            /**
             * Gets the value of the userParam property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the userParam property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getUserParam().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link UserParam }
             * 
             * 
             */
            public List<UserParam> getUserParam() {
                if (userParam == null) {
                    userParam = new ArrayList<UserParam>();
                }
                return this.userParam;
            }

            /**
             * Ruft den Wert der sequence-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getSequence() {
                return sequence;
            }

            /**
             * Legt den Wert der sequence-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setSequence(String value) {
                this.sequence = value;
            }

            /**
             * Ruft den Wert der charge-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link BigInteger }
             *     
             */
            public BigInteger getCharge() {
                return charge;
            }

            /**
             * Legt den Wert der charge-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *     
             */
            public void setCharge(BigInteger value) {
                this.charge = value;
            }

            /**
             * Ruft den Wert der score-Eigenschaft ab.
             * 
             */
            public float getScore() {
                return score;
            }

            /**
             * Legt den Wert der score-Eigenschaft fest.
             * 
             */
            public void setScore(float value) {
                this.score = value;
            }

            /**
             * Ruft den Wert der aaBefore-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getAaBefore() {
                return aaBefore;
            }

            /**
             * Legt den Wert der aaBefore-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setAaBefore(String value) {
                this.aaBefore = value;
            }

            /**
             * Ruft den Wert der aaAfter-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getAaAfter() {
                return aaAfter;
            }

            /**
             * Legt den Wert der aaAfter-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setAaAfter(String value) {
                this.aaAfter = value;
            }

            /**
             * Gets the value of the proteinRefs property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the proteinRefs property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getProteinRefs().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link Object }
             * 
             * 
             */
            public List<Object> getProteinRefs() {
                if (proteinRefs == null) {
                    proteinRefs = new ArrayList<Object>();
                }
                return this.proteinRefs;
            }

        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>anySimpleType">
     *       &lt;attribute name="dim" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *     &lt;/extension>
     *   &lt;/simpleContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Position {

        @XmlValue
        @XmlSchemaType(name = "anySimpleType")
        protected Object value;
        @XmlAttribute(name = "dim", required = true)
        @XmlSchemaType(name = "anySimpleType")
        protected String dim;

        /**
         * Ruft den Wert der value-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Object }
         *     
         */
        public Object getValue() {
            return value;
        }

        /**
         * Legt den Wert der value-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Object }
         *     
         */
        public void setValue(Object value) {
            this.value = value;
        }

        /**
         * Ruft den Wert der dim-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDim() {
            return dim;
        }

        /**
         * Legt den Wert der dim-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDim(String value) {
            this.dim = value;
        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>anySimpleType">
     *       &lt;attribute name="dim" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *     &lt;/extension>
     *   &lt;/simpleContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Quality {

        @XmlValue
        @XmlSchemaType(name = "anySimpleType")
        protected Object value;
        @XmlAttribute(name = "dim", required = true)
        @XmlSchemaType(name = "anySimpleType")
        protected String dim;

        /**
         * Ruft den Wert der value-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Object }
         *     
         */
        public Object getValue() {
            return value;
        }

        /**
         * Legt den Wert der value-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Object }
         *     
         */
        public void setValue(Object value) {
            this.value = value;
        }

        /**
         * Ruft den Wert der dim-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDim() {
            return dim;
        }

        /**
         * Legt den Wert der dim-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDim(String value) {
            this.dim = value;
        }

    }


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
     *         &lt;element name="feature" type="{}featureType" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "feature"
    })
    public static class Subordinate {

        protected List<FeatureType> feature;

        /**
         * Gets the value of the feature property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the feature property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getFeature().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link FeatureType }
         * 
         * 
         */
        public List<FeatureType> getFeature() {
            if (feature == null) {
                feature = new ArrayList<FeatureType>();
            }
            return this.feature;
        }

    }

}
