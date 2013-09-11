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
 * <p>Java-Klasse für UserParamType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="UserParamType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="int"/>
 *     &lt;enumeration value="float"/>
 *     &lt;enumeration value="string"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "UserParamType")
@XmlEnum
public enum UserParamType {

    @XmlEnumValue("int")
    INT("int"),
    @XmlEnumValue("float")
    FLOAT("float"),
    @XmlEnumValue("string")
    STRING("string");
    private final String value;

    UserParamType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static UserParamType fromValue(String v) {
        for (UserParamType c: UserParamType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
