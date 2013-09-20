//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2013.09.18 um 05:51:36 PM CEST 
//


package maltcms.io.xml.bindings.annotation;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the maltcms.io.xml.bindings.annotation package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Annotations_QNAME = new QName("http://maltcms.sourceforge.net/maltcmsAnnotation", "annotations");
    private final static QName _Attribute_QNAME = new QName("http://maltcms.sourceforge.net/maltcmsAnnotation", "attribute");
    private final static QName _Resource_QNAME = new QName("http://maltcms.sourceforge.net/maltcmsAnnotation", "resource");
    private final static QName _Annotation_QNAME = new QName("http://maltcms.sourceforge.net/maltcmsAnnotation", "annotation");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: maltcms.io.xml.bindings.annotation
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link AnnotationType }
     * 
     */
    public AnnotationType createAnnotationType() {
        return new AnnotationType();
    }

    /**
     * Create an instance of {@link ResourceType }
     * 
     */
    public ResourceType createResourceType() {
        return new ResourceType();
    }

    /**
     * Create an instance of {@link AttributeType }
     * 
     */
    public AttributeType createAttributeType() {
        return new AttributeType();
    }

    /**
     * Create an instance of {@link AnnotationsType }
     * 
     */
    public AnnotationsType createAnnotationsType() {
        return new AnnotationsType();
    }

    /**
     * Create an instance of {@link MaltcmsAnnotation }
     * 
     */
    public MaltcmsAnnotation createMaltcmsAnnotation() {
        return new MaltcmsAnnotation();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AnnotationsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://maltcms.sourceforge.net/maltcmsAnnotation", name = "annotations")
    public JAXBElement<AnnotationsType> createAnnotations(AnnotationsType value) {
        return new JAXBElement<AnnotationsType>(_Annotations_QNAME, AnnotationsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AttributeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://maltcms.sourceforge.net/maltcmsAnnotation", name = "attribute")
    public JAXBElement<AttributeType> createAttribute(AttributeType value) {
        return new JAXBElement<AttributeType>(_Attribute_QNAME, AttributeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResourceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://maltcms.sourceforge.net/maltcmsAnnotation", name = "resource")
    public JAXBElement<ResourceType> createResource(ResourceType value) {
        return new JAXBElement<ResourceType>(_Resource_QNAME, ResourceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AnnotationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://maltcms.sourceforge.net/maltcmsAnnotation", name = "annotation")
    public JAXBElement<AnnotationType> createAnnotation(AnnotationType value) {
        return new JAXBElement<AnnotationType>(_Annotation_QNAME, AnnotationType.class, null, value);
    }

}
