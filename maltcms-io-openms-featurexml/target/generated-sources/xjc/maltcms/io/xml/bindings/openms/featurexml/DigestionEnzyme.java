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
 * <p>Java-Klasse für DigestionEnzyme.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="DigestionEnzyme">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="pepsin_a"/>
 *     &lt;enumeration value="chymotrypsin"/>
 *     &lt;enumeration value="proteinase_k"/>
 *     &lt;enumeration value="trypsin"/>
 *     &lt;enumeration value="no_enzyme"/>
 *     &lt;enumeration value="unknown_enzyme"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DigestionEnzyme")
@XmlEnum
public enum DigestionEnzyme {

    @XmlEnumValue("pepsin_a")
    PEPSIN_A("pepsin_a"),
    @XmlEnumValue("chymotrypsin")
    CHYMOTRYPSIN("chymotrypsin"),
    @XmlEnumValue("proteinase_k")
    PROTEINASE_K("proteinase_k"),
    @XmlEnumValue("trypsin")
    TRYPSIN("trypsin"),
    @XmlEnumValue("no_enzyme")
    NO_ENZYME("no_enzyme"),
    @XmlEnumValue("unknown_enzyme")
    UNKNOWN_ENZYME("unknown_enzyme");
    private final String value;

    DigestionEnzyme(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DigestionEnzyme fromValue(String v) {
        for (DigestionEnzyme c: DigestionEnzyme.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
