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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


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
 *         &lt;element name="dataProcessing" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="software">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="processingAction" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="userParam" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;extension base="{}userParam">
 *                         &lt;/extension>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="completion_time" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="IdentificationRun" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="SearchParameters">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="FixedModification" maxOccurs="unbounded" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence minOccurs="0">
 *                                       &lt;element name="userParam" type="{}userParam" maxOccurs="unbounded" minOccurs="0"/>
 *                                     &lt;/sequence>
 *                                     &lt;attribute name="name" use="required">
 *                                       &lt;simpleType>
 *                                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                           &lt;minLength value="1"/>
 *                                         &lt;/restriction>
 *                                       &lt;/simpleType>
 *                                     &lt;/attribute>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                             &lt;element name="VariableModification" maxOccurs="unbounded" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence minOccurs="0">
 *                                       &lt;element name="userParam" type="{}userParam" maxOccurs="unbounded" minOccurs="0"/>
 *                                     &lt;/sequence>
 *                                     &lt;attribute name="name" use="required">
 *                                       &lt;simpleType>
 *                                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                           &lt;minLength value="1"/>
 *                                         &lt;/restriction>
 *                                       &lt;/simpleType>
 *                                     &lt;/attribute>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                             &lt;element name="userParam" type="{}userParam" maxOccurs="unbounded" minOccurs="0"/>
 *                           &lt;/sequence>
 *                           &lt;attribute name="db" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="db_version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="taxonomy" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="mass_type" use="required" type="{}MassType" />
 *                           &lt;attribute name="charges" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="enzyme" type="{}DigestionEnzyme" />
 *                           &lt;attribute name="missed_cleavages" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *                           &lt;attribute name="precursor_peak_tolerance" use="required" type="{http://www.w3.org/2001/XMLSchema}float" />
 *                           &lt;attribute name="peak_mass_tolerance" use="required" type="{http://www.w3.org/2001/XMLSchema}float" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="ProteinIdentification" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="ProteinHit" maxOccurs="unbounded" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence minOccurs="0">
 *                                       &lt;element name="userParam" type="{}userParam" maxOccurs="unbounded" minOccurs="0"/>
 *                                     &lt;/sequence>
 *                                     &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                                     &lt;attribute name="accession" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                     &lt;attribute name="score" use="required" type="{http://www.w3.org/2001/XMLSchema}float" />
 *                                     &lt;attribute name="sequence" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                             &lt;element name="userParam" type="{}userParam" maxOccurs="unbounded" minOccurs="0"/>
 *                           &lt;/sequence>
 *                           &lt;attribute name="score_type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="higher_score_better" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *                           &lt;attribute name="significance_threshold" type="{http://www.w3.org/2001/XMLSchema}float" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                 &lt;attribute name="search_engine" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="search_engine_version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="date" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="UnassignedPeptideIdentification" maxOccurs="unbounded" minOccurs="0">
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
 *         &lt;element name="featureList">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="feature" type="{}featureType" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="count" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}float" />
 *       &lt;attribute name="document_id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "dataProcessing",
    "identificationRun",
    "unassignedPeptideIdentification",
    "featureList"
})
@XmlRootElement(name = "featureMap")
public class FeatureMap {

    protected List<FeatureMap.DataProcessing> dataProcessing;
    @XmlElement(name = "IdentificationRun")
    protected List<FeatureMap.IdentificationRun> identificationRun;
    @XmlElement(name = "UnassignedPeptideIdentification")
    protected List<FeatureMap.UnassignedPeptideIdentification> unassignedPeptideIdentification;
    @XmlElement(required = true)
    protected FeatureMap.FeatureList featureList;
    @XmlAttribute(name = "version")
    protected Float version;
    @XmlAttribute(name = "document_id")
    protected String documentId;
    @XmlAttribute(name = "id")
    protected String id;

    /**
     * Gets the value of the dataProcessing property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dataProcessing property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDataProcessing().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FeatureMap.DataProcessing }
     * 
     * 
     */
    public List<FeatureMap.DataProcessing> getDataProcessing() {
        if (dataProcessing == null) {
            dataProcessing = new ArrayList<FeatureMap.DataProcessing>();
        }
        return this.dataProcessing;
    }

    /**
     * Gets the value of the identificationRun property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the identificationRun property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIdentificationRun().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FeatureMap.IdentificationRun }
     * 
     * 
     */
    public List<FeatureMap.IdentificationRun> getIdentificationRun() {
        if (identificationRun == null) {
            identificationRun = new ArrayList<FeatureMap.IdentificationRun>();
        }
        return this.identificationRun;
    }

    /**
     * Gets the value of the unassignedPeptideIdentification property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the unassignedPeptideIdentification property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUnassignedPeptideIdentification().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FeatureMap.UnassignedPeptideIdentification }
     * 
     * 
     */
    public List<FeatureMap.UnassignedPeptideIdentification> getUnassignedPeptideIdentification() {
        if (unassignedPeptideIdentification == null) {
            unassignedPeptideIdentification = new ArrayList<FeatureMap.UnassignedPeptideIdentification>();
        }
        return this.unassignedPeptideIdentification;
    }

    /**
     * Ruft den Wert der featureList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FeatureMap.FeatureList }
     *     
     */
    public FeatureMap.FeatureList getFeatureList() {
        return featureList;
    }

    /**
     * Legt den Wert der featureList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FeatureMap.FeatureList }
     *     
     */
    public void setFeatureList(FeatureMap.FeatureList value) {
        this.featureList = value;
    }

    /**
     * Ruft den Wert der version-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getVersion() {
        return version;
    }

    /**
     * Legt den Wert der version-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setVersion(Float value) {
        this.version = value;
    }

    /**
     * Ruft den Wert der documentId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocumentId() {
        return documentId;
    }

    /**
     * Legt den Wert der documentId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocumentId(String value) {
        this.documentId = value;
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
     *         &lt;element name="software">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="processingAction" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="userParam" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;extension base="{}userParam">
     *               &lt;/extension>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="completion_time" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "software",
        "processingAction",
        "userParam"
    })
    public static class DataProcessing {

        @XmlElement(required = true)
        protected FeatureMap.DataProcessing.Software software;
        protected List<FeatureMap.DataProcessing.ProcessingAction> processingAction;
        protected List<FeatureMap.DataProcessing.UserParam> userParam;
        @XmlAttribute(name = "completion_time", required = true)
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar completionTime;

        /**
         * Ruft den Wert der software-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link FeatureMap.DataProcessing.Software }
         *     
         */
        public FeatureMap.DataProcessing.Software getSoftware() {
            return software;
        }

        /**
         * Legt den Wert der software-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link FeatureMap.DataProcessing.Software }
         *     
         */
        public void setSoftware(FeatureMap.DataProcessing.Software value) {
            this.software = value;
        }

        /**
         * Gets the value of the processingAction property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the processingAction property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getProcessingAction().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link FeatureMap.DataProcessing.ProcessingAction }
         * 
         * 
         */
        public List<FeatureMap.DataProcessing.ProcessingAction> getProcessingAction() {
            if (processingAction == null) {
                processingAction = new ArrayList<FeatureMap.DataProcessing.ProcessingAction>();
            }
            return this.processingAction;
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
         * {@link FeatureMap.DataProcessing.UserParam }
         * 
         * 
         */
        public List<FeatureMap.DataProcessing.UserParam> getUserParam() {
            if (userParam == null) {
                userParam = new ArrayList<FeatureMap.DataProcessing.UserParam>();
            }
            return this.userParam;
        }

        /**
         * Ruft den Wert der completionTime-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getCompletionTime() {
            return completionTime;
        }

        /**
         * Legt den Wert der completionTime-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setCompletionTime(XMLGregorianCalendar value) {
            this.completionTime = value;
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
         *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class ProcessingAction {

            @XmlAttribute(name = "name", required = true)
            protected String name;

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
         *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Software {

            @XmlAttribute(name = "name", required = true)
            protected String name;
            @XmlAttribute(name = "version", required = true)
            protected String version;

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
             * Ruft den Wert der version-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getVersion() {
                return version;
            }

            /**
             * Legt den Wert der version-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setVersion(String value) {
                this.version = value;
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
         *     &lt;extension base="{}userParam">
         *     &lt;/extension>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class UserParam
            extends maltcms.io.xml.bindings.openms.featurexml.UserParam
        {


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
     *       &lt;attribute name="count" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
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
    public static class FeatureList {

        protected List<FeatureType> feature;
        @XmlAttribute(name = "count", required = true)
        protected BigInteger count;

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

        /**
         * Ruft den Wert der count-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getCount() {
            return count;
        }

        /**
         * Legt den Wert der count-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setCount(BigInteger value) {
            this.count = value;
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
     *         &lt;element name="SearchParameters">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="FixedModification" maxOccurs="unbounded" minOccurs="0">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence minOccurs="0">
     *                             &lt;element name="userParam" type="{}userParam" maxOccurs="unbounded" minOccurs="0"/>
     *                           &lt;/sequence>
     *                           &lt;attribute name="name" use="required">
     *                             &lt;simpleType>
     *                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                                 &lt;minLength value="1"/>
     *                               &lt;/restriction>
     *                             &lt;/simpleType>
     *                           &lt;/attribute>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                   &lt;element name="VariableModification" maxOccurs="unbounded" minOccurs="0">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence minOccurs="0">
     *                             &lt;element name="userParam" type="{}userParam" maxOccurs="unbounded" minOccurs="0"/>
     *                           &lt;/sequence>
     *                           &lt;attribute name="name" use="required">
     *                             &lt;simpleType>
     *                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                                 &lt;minLength value="1"/>
     *                               &lt;/restriction>
     *                             &lt;/simpleType>
     *                           &lt;/attribute>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                   &lt;element name="userParam" type="{}userParam" maxOccurs="unbounded" minOccurs="0"/>
     *                 &lt;/sequence>
     *                 &lt;attribute name="db" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="db_version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="taxonomy" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="mass_type" use="required" type="{}MassType" />
     *                 &lt;attribute name="charges" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="enzyme" type="{}DigestionEnzyme" />
     *                 &lt;attribute name="missed_cleavages" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
     *                 &lt;attribute name="precursor_peak_tolerance" use="required" type="{http://www.w3.org/2001/XMLSchema}float" />
     *                 &lt;attribute name="peak_mass_tolerance" use="required" type="{http://www.w3.org/2001/XMLSchema}float" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="ProteinIdentification" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="ProteinHit" maxOccurs="unbounded" minOccurs="0">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence minOccurs="0">
     *                             &lt;element name="userParam" type="{}userParam" maxOccurs="unbounded" minOccurs="0"/>
     *                           &lt;/sequence>
     *                           &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" />
     *                           &lt;attribute name="accession" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                           &lt;attribute name="score" use="required" type="{http://www.w3.org/2001/XMLSchema}float" />
     *                           &lt;attribute name="sequence" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                   &lt;element name="userParam" type="{}userParam" maxOccurs="unbounded" minOccurs="0"/>
     *                 &lt;/sequence>
     *                 &lt;attribute name="score_type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="higher_score_better" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
     *                 &lt;attribute name="significance_threshold" type="{http://www.w3.org/2001/XMLSchema}float" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" />
     *       &lt;attribute name="search_engine" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="search_engine_version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="date" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "searchParameters",
        "proteinIdentification"
    })
    public static class IdentificationRun {

        @XmlElement(name = "SearchParameters", required = true)
        protected FeatureMap.IdentificationRun.SearchParameters searchParameters;
        @XmlElement(name = "ProteinIdentification")
        protected FeatureMap.IdentificationRun.ProteinIdentification proteinIdentification;
        @XmlAttribute(name = "id", required = true)
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAttribute(name = "search_engine", required = true)
        protected String searchEngine;
        @XmlAttribute(name = "search_engine_version", required = true)
        protected String searchEngineVersion;
        @XmlAttribute(name = "date", required = true)
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar date;

        /**
         * Ruft den Wert der searchParameters-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link FeatureMap.IdentificationRun.SearchParameters }
         *     
         */
        public FeatureMap.IdentificationRun.SearchParameters getSearchParameters() {
            return searchParameters;
        }

        /**
         * Legt den Wert der searchParameters-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link FeatureMap.IdentificationRun.SearchParameters }
         *     
         */
        public void setSearchParameters(FeatureMap.IdentificationRun.SearchParameters value) {
            this.searchParameters = value;
        }

        /**
         * Ruft den Wert der proteinIdentification-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link FeatureMap.IdentificationRun.ProteinIdentification }
         *     
         */
        public FeatureMap.IdentificationRun.ProteinIdentification getProteinIdentification() {
            return proteinIdentification;
        }

        /**
         * Legt den Wert der proteinIdentification-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link FeatureMap.IdentificationRun.ProteinIdentification }
         *     
         */
        public void setProteinIdentification(FeatureMap.IdentificationRun.ProteinIdentification value) {
            this.proteinIdentification = value;
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
         * Ruft den Wert der searchEngine-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSearchEngine() {
            return searchEngine;
        }

        /**
         * Legt den Wert der searchEngine-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSearchEngine(String value) {
            this.searchEngine = value;
        }

        /**
         * Ruft den Wert der searchEngineVersion-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSearchEngineVersion() {
            return searchEngineVersion;
        }

        /**
         * Legt den Wert der searchEngineVersion-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSearchEngineVersion(String value) {
            this.searchEngineVersion = value;
        }

        /**
         * Ruft den Wert der date-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getDate() {
            return date;
        }

        /**
         * Legt den Wert der date-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setDate(XMLGregorianCalendar value) {
            this.date = value;
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
         *         &lt;element name="ProteinHit" maxOccurs="unbounded" minOccurs="0">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence minOccurs="0">
         *                   &lt;element name="userParam" type="{}userParam" maxOccurs="unbounded" minOccurs="0"/>
         *                 &lt;/sequence>
         *                 &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" />
         *                 &lt;attribute name="accession" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *                 &lt;attribute name="score" use="required" type="{http://www.w3.org/2001/XMLSchema}float" />
         *                 &lt;attribute name="sequence" type="{http://www.w3.org/2001/XMLSchema}string" />
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *         &lt;element name="userParam" type="{}userParam" maxOccurs="unbounded" minOccurs="0"/>
         *       &lt;/sequence>
         *       &lt;attribute name="score_type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="higher_score_better" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
         *       &lt;attribute name="significance_threshold" type="{http://www.w3.org/2001/XMLSchema}float" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "proteinHit",
            "userParam"
        })
        public static class ProteinIdentification {

            @XmlElement(name = "ProteinHit")
            protected List<FeatureMap.IdentificationRun.ProteinIdentification.ProteinHit> proteinHit;
            protected List<maltcms.io.xml.bindings.openms.featurexml.UserParam> userParam;
            @XmlAttribute(name = "score_type", required = true)
            protected String scoreType;
            @XmlAttribute(name = "higher_score_better", required = true)
            protected boolean higherScoreBetter;
            @XmlAttribute(name = "significance_threshold")
            protected Float significanceThreshold;

            /**
             * Gets the value of the proteinHit property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the proteinHit property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getProteinHit().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link FeatureMap.IdentificationRun.ProteinIdentification.ProteinHit }
             * 
             * 
             */
            public List<FeatureMap.IdentificationRun.ProteinIdentification.ProteinHit> getProteinHit() {
                if (proteinHit == null) {
                    proteinHit = new ArrayList<FeatureMap.IdentificationRun.ProteinIdentification.ProteinHit>();
                }
                return this.proteinHit;
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
             * {@link maltcms.io.xml.bindings.openms.featurexml.UserParam }
             * 
             * 
             */
            public List<maltcms.io.xml.bindings.openms.featurexml.UserParam> getUserParam() {
                if (userParam == null) {
                    userParam = new ArrayList<maltcms.io.xml.bindings.openms.featurexml.UserParam>();
                }
                return this.userParam;
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
             *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" />
             *       &lt;attribute name="accession" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
             *       &lt;attribute name="score" use="required" type="{http://www.w3.org/2001/XMLSchema}float" />
             *       &lt;attribute name="sequence" type="{http://www.w3.org/2001/XMLSchema}string" />
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
            public static class ProteinHit {

                protected List<maltcms.io.xml.bindings.openms.featurexml.UserParam> userParam;
                @XmlAttribute(name = "id", required = true)
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlID
                @XmlSchemaType(name = "ID")
                protected String id;
                @XmlAttribute(name = "accession", required = true)
                protected String accession;
                @XmlAttribute(name = "score", required = true)
                protected float score;
                @XmlAttribute(name = "sequence")
                protected String sequence;

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
                 * {@link maltcms.io.xml.bindings.openms.featurexml.UserParam }
                 * 
                 * 
                 */
                public List<maltcms.io.xml.bindings.openms.featurexml.UserParam> getUserParam() {
                    if (userParam == null) {
                        userParam = new ArrayList<maltcms.io.xml.bindings.openms.featurexml.UserParam>();
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
                 * Ruft den Wert der accession-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getAccession() {
                    return accession;
                }

                /**
                 * Legt den Wert der accession-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setAccession(String value) {
                    this.accession = value;
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
         *         &lt;element name="FixedModification" maxOccurs="unbounded" minOccurs="0">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence minOccurs="0">
         *                   &lt;element name="userParam" type="{}userParam" maxOccurs="unbounded" minOccurs="0"/>
         *                 &lt;/sequence>
         *                 &lt;attribute name="name" use="required">
         *                   &lt;simpleType>
         *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
         *                       &lt;minLength value="1"/>
         *                     &lt;/restriction>
         *                   &lt;/simpleType>
         *                 &lt;/attribute>
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *         &lt;element name="VariableModification" maxOccurs="unbounded" minOccurs="0">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence minOccurs="0">
         *                   &lt;element name="userParam" type="{}userParam" maxOccurs="unbounded" minOccurs="0"/>
         *                 &lt;/sequence>
         *                 &lt;attribute name="name" use="required">
         *                   &lt;simpleType>
         *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
         *                       &lt;minLength value="1"/>
         *                     &lt;/restriction>
         *                   &lt;/simpleType>
         *                 &lt;/attribute>
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *         &lt;element name="userParam" type="{}userParam" maxOccurs="unbounded" minOccurs="0"/>
         *       &lt;/sequence>
         *       &lt;attribute name="db" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="db_version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="taxonomy" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="mass_type" use="required" type="{}MassType" />
         *       &lt;attribute name="charges" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="enzyme" type="{}DigestionEnzyme" />
         *       &lt;attribute name="missed_cleavages" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
         *       &lt;attribute name="precursor_peak_tolerance" use="required" type="{http://www.w3.org/2001/XMLSchema}float" />
         *       &lt;attribute name="peak_mass_tolerance" use="required" type="{http://www.w3.org/2001/XMLSchema}float" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "fixedModification",
            "variableModification",
            "userParam"
        })
        public static class SearchParameters {

            @XmlElement(name = "FixedModification")
            protected List<FeatureMap.IdentificationRun.SearchParameters.FixedModification> fixedModification;
            @XmlElement(name = "VariableModification")
            protected List<FeatureMap.IdentificationRun.SearchParameters.VariableModification> variableModification;
            protected List<maltcms.io.xml.bindings.openms.featurexml.UserParam> userParam;
            @XmlAttribute(name = "db", required = true)
            protected String db;
            @XmlAttribute(name = "db_version", required = true)
            protected String dbVersion;
            @XmlAttribute(name = "taxonomy")
            protected String taxonomy;
            @XmlAttribute(name = "mass_type", required = true)
            protected MassType massType;
            @XmlAttribute(name = "charges", required = true)
            protected String charges;
            @XmlAttribute(name = "enzyme")
            protected DigestionEnzyme enzyme;
            @XmlAttribute(name = "missed_cleavages")
            @XmlSchemaType(name = "unsignedInt")
            protected Long missedCleavages;
            @XmlAttribute(name = "precursor_peak_tolerance", required = true)
            protected float precursorPeakTolerance;
            @XmlAttribute(name = "peak_mass_tolerance", required = true)
            protected float peakMassTolerance;

            /**
             * Gets the value of the fixedModification property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the fixedModification property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getFixedModification().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link FeatureMap.IdentificationRun.SearchParameters.FixedModification }
             * 
             * 
             */
            public List<FeatureMap.IdentificationRun.SearchParameters.FixedModification> getFixedModification() {
                if (fixedModification == null) {
                    fixedModification = new ArrayList<FeatureMap.IdentificationRun.SearchParameters.FixedModification>();
                }
                return this.fixedModification;
            }

            /**
             * Gets the value of the variableModification property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the variableModification property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getVariableModification().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link FeatureMap.IdentificationRun.SearchParameters.VariableModification }
             * 
             * 
             */
            public List<FeatureMap.IdentificationRun.SearchParameters.VariableModification> getVariableModification() {
                if (variableModification == null) {
                    variableModification = new ArrayList<FeatureMap.IdentificationRun.SearchParameters.VariableModification>();
                }
                return this.variableModification;
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
             * {@link maltcms.io.xml.bindings.openms.featurexml.UserParam }
             * 
             * 
             */
            public List<maltcms.io.xml.bindings.openms.featurexml.UserParam> getUserParam() {
                if (userParam == null) {
                    userParam = new ArrayList<maltcms.io.xml.bindings.openms.featurexml.UserParam>();
                }
                return this.userParam;
            }

            /**
             * Ruft den Wert der db-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getDb() {
                return db;
            }

            /**
             * Legt den Wert der db-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setDb(String value) {
                this.db = value;
            }

            /**
             * Ruft den Wert der dbVersion-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getDbVersion() {
                return dbVersion;
            }

            /**
             * Legt den Wert der dbVersion-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setDbVersion(String value) {
                this.dbVersion = value;
            }

            /**
             * Ruft den Wert der taxonomy-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getTaxonomy() {
                return taxonomy;
            }

            /**
             * Legt den Wert der taxonomy-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setTaxonomy(String value) {
                this.taxonomy = value;
            }

            /**
             * Ruft den Wert der massType-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link MassType }
             *     
             */
            public MassType getMassType() {
                return massType;
            }

            /**
             * Legt den Wert der massType-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link MassType }
             *     
             */
            public void setMassType(MassType value) {
                this.massType = value;
            }

            /**
             * Ruft den Wert der charges-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getCharges() {
                return charges;
            }

            /**
             * Legt den Wert der charges-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setCharges(String value) {
                this.charges = value;
            }

            /**
             * Ruft den Wert der enzyme-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link DigestionEnzyme }
             *     
             */
            public DigestionEnzyme getEnzyme() {
                return enzyme;
            }

            /**
             * Legt den Wert der enzyme-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link DigestionEnzyme }
             *     
             */
            public void setEnzyme(DigestionEnzyme value) {
                this.enzyme = value;
            }

            /**
             * Ruft den Wert der missedCleavages-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link Long }
             *     
             */
            public Long getMissedCleavages() {
                return missedCleavages;
            }

            /**
             * Legt den Wert der missedCleavages-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link Long }
             *     
             */
            public void setMissedCleavages(Long value) {
                this.missedCleavages = value;
            }

            /**
             * Ruft den Wert der precursorPeakTolerance-Eigenschaft ab.
             * 
             */
            public float getPrecursorPeakTolerance() {
                return precursorPeakTolerance;
            }

            /**
             * Legt den Wert der precursorPeakTolerance-Eigenschaft fest.
             * 
             */
            public void setPrecursorPeakTolerance(float value) {
                this.precursorPeakTolerance = value;
            }

            /**
             * Ruft den Wert der peakMassTolerance-Eigenschaft ab.
             * 
             */
            public float getPeakMassTolerance() {
                return peakMassTolerance;
            }

            /**
             * Legt den Wert der peakMassTolerance-Eigenschaft fest.
             * 
             */
            public void setPeakMassTolerance(float value) {
                this.peakMassTolerance = value;
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
             *       &lt;attribute name="name" use="required">
             *         &lt;simpleType>
             *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
             *             &lt;minLength value="1"/>
             *           &lt;/restriction>
             *         &lt;/simpleType>
             *       &lt;/attribute>
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
            public static class FixedModification {

                protected List<maltcms.io.xml.bindings.openms.featurexml.UserParam> userParam;
                @XmlAttribute(name = "name", required = true)
                protected String name;

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
                 * {@link maltcms.io.xml.bindings.openms.featurexml.UserParam }
                 * 
                 * 
                 */
                public List<maltcms.io.xml.bindings.openms.featurexml.UserParam> getUserParam() {
                    if (userParam == null) {
                        userParam = new ArrayList<maltcms.io.xml.bindings.openms.featurexml.UserParam>();
                    }
                    return this.userParam;
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
             *       &lt;attribute name="name" use="required">
             *         &lt;simpleType>
             *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
             *             &lt;minLength value="1"/>
             *           &lt;/restriction>
             *         &lt;/simpleType>
             *       &lt;/attribute>
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
            public static class VariableModification {

                protected List<maltcms.io.xml.bindings.openms.featurexml.UserParam> userParam;
                @XmlAttribute(name = "name", required = true)
                protected String name;

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
                 * {@link maltcms.io.xml.bindings.openms.featurexml.UserParam }
                 * 
                 * 
                 */
                public List<maltcms.io.xml.bindings.openms.featurexml.UserParam> getUserParam() {
                    if (userParam == null) {
                        userParam = new ArrayList<maltcms.io.xml.bindings.openms.featurexml.UserParam>();
                    }
                    return this.userParam;
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
    public static class UnassignedPeptideIdentification {

        @XmlElement(name = "PeptideHit")
        protected List<FeatureMap.UnassignedPeptideIdentification.PeptideHit> peptideHit;
        protected List<maltcms.io.xml.bindings.openms.featurexml.UserParam> userParam;
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
         * {@link FeatureMap.UnassignedPeptideIdentification.PeptideHit }
         * 
         * 
         */
        public List<FeatureMap.UnassignedPeptideIdentification.PeptideHit> getPeptideHit() {
            if (peptideHit == null) {
                peptideHit = new ArrayList<FeatureMap.UnassignedPeptideIdentification.PeptideHit>();
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
         * {@link maltcms.io.xml.bindings.openms.featurexml.UserParam }
         * 
         * 
         */
        public List<maltcms.io.xml.bindings.openms.featurexml.UserParam> getUserParam() {
            if (userParam == null) {
                userParam = new ArrayList<maltcms.io.xml.bindings.openms.featurexml.UserParam>();
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

            protected List<maltcms.io.xml.bindings.openms.featurexml.UserParam> userParam;
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
             * {@link maltcms.io.xml.bindings.openms.featurexml.UserParam }
             * 
             * 
             */
            public List<maltcms.io.xml.bindings.openms.featurexml.UserParam> getUserParam() {
                if (userParam == null) {
                    userParam = new ArrayList<maltcms.io.xml.bindings.openms.featurexml.UserParam>();
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

}
