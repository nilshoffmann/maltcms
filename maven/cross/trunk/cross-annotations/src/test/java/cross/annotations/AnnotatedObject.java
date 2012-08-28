/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cross.annotations;

import java.util.Arrays;
import java.util.List;
import lombok.Data;

/**
 *
 * @author hoffmann
 */
@RequiresVariables(names = {"a", "b", "c"})
@ProvidesVariables(names = {"b", "d"})
@RequiresOptionalVariables(names = {"e"})
@Data
public class AnnotatedObject {

    @Configurable(type = int.class, description = "field1 for testing", name = "myField1", value = "10")
    private int field1 = 10;
    @Configurable(description = "field2 for testing")
    private int field2 = 12;
    @Configurable(value = "field3value")
    private String field3;
    private Long field4 = 1l;

    public static final List<String> getRequiredVariables() {
        return Arrays.asList("a", "b", "c");
    }

    public static final List<String> getOptionalRequiredVariables() {
        return Arrays.asList("e");
    }

    public static final List<String> getProvidedVariables() {
        return Arrays.asList("b", "d");
    }

    public static final List<String> getRequiredConfigKeys() {
        return Arrays.asList("cross.annotations.AnnotatedObject.myField1", "cross.annotations.AnnotatedObject.field2", "cross.annotations.AnnotatedObject.field3");
    }
}
