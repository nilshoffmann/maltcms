//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2013.09.18 um 06:01:26 PM CEST 
//


package maltcms.io.xml.bindings.alignment;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the maltcms.io.xml.bindings.alignment package. 
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

    private final static QName _PointMap_QNAME = new QName("http://maltcms.sourceforge.net/maltcmsAlignment", "pointMap");
    private final static QName _Resource_QNAME = new QName("http://maltcms.sourceforge.net/maltcmsAlignment", "resource");
    private final static QName _MappedPoints_QNAME = new QName("http://maltcms.sourceforge.net/maltcmsAlignment", "mappedPoints");
    private final static QName _Point_QNAME = new QName("http://maltcms.sourceforge.net/maltcmsAlignment", "point");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: maltcms.io.xml.bindings.alignment
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PointType }
     * 
     */
    public PointType createPointType() {
        return new PointType();
    }

    /**
     * Create an instance of {@link Alignment }
     * 
     */
    public Alignment createAlignment() {
        return new Alignment();
    }

    /**
     * Create an instance of {@link MappedPointsType }
     * 
     */
    public MappedPointsType createMappedPointsType() {
        return new MappedPointsType();
    }

    /**
     * Create an instance of {@link ResourceType }
     * 
     */
    public ResourceType createResourceType() {
        return new ResourceType();
    }

    /**
     * Create an instance of {@link PointMapType }
     * 
     */
    public PointMapType createPointMapType() {
        return new PointMapType();
    }

    /**
     * Create an instance of {@link PointType.Dimension }
     * 
     */
    public PointType.Dimension createPointTypeDimension() {
        return new PointType.Dimension();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PointMapType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://maltcms.sourceforge.net/maltcmsAlignment", name = "pointMap")
    public JAXBElement<PointMapType> createPointMap(PointMapType value) {
        return new JAXBElement<PointMapType>(_PointMap_QNAME, PointMapType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResourceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://maltcms.sourceforge.net/maltcmsAlignment", name = "resource")
    public JAXBElement<ResourceType> createResource(ResourceType value) {
        return new JAXBElement<ResourceType>(_Resource_QNAME, ResourceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MappedPointsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://maltcms.sourceforge.net/maltcmsAlignment", name = "mappedPoints")
    public JAXBElement<MappedPointsType> createMappedPoints(MappedPointsType value) {
        return new JAXBElement<MappedPointsType>(_MappedPoints_QNAME, MappedPointsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PointType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://maltcms.sourceforge.net/maltcmsAlignment", name = "point")
    public JAXBElement<PointType> createPoint(PointType value) {
        return new JAXBElement<PointType>(_Point_QNAME, PointType.class, null, value);
    }

}
