//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2013.09.03 um 04:00:41 PM CEST 
//


package maltcms.io.xml.bindings.openms.featurexml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für MassType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="MassType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="average"/>
 *     &lt;enumeration value="monoisotopic"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "MassType")
@XmlEnum
public enum MassType {

    @XmlEnumValue("average")
    AVERAGE("average"),
    @XmlEnumValue("monoisotopic")
    MONOISOTOPIC("monoisotopic");
    private final String value;

    MassType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MassType fromValue(String v) {
        for (MassType c: MassType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
