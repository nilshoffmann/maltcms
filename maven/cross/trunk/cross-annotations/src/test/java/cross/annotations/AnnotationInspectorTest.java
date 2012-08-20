/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cross.annotations;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hoffmann
 */
public class AnnotationInspectorTest {

    private AnnotatedObject testObject = new AnnotatedObject();
    private final String prefix = "cross.annotations.AnnotatedObject.";

    public AnnotationInspectorTest() {
    }

    /**
     * Test of getOptionalRequiredVariables method, of class
     * AnnotationInspector.
     */
    @Test
    public void testGetOptionalRequiredVariables_ClassObject() {
        List<String> required = AnnotatedObject.getOptionalRequiredVariables();
        assertEquals(required, AnnotationInspector.getOptionalRequiredVariables(AnnotatedObject.class));
        assertEquals(required, AnnotationInspector.getOptionalRequiredVariables(testObject));
    }

    /**
     * Test of getRequiredVariables method, of class AnnotationInspector.
     */
    @Test
    public void testGetRequiredVariables_ClassObject() {
        List<String> required = AnnotatedObject.getRequiredVariables();
        assertEquals(required, AnnotationInspector.getRequiredVariables(AnnotatedObject.class));
        assertEquals(required, AnnotationInspector.getRequiredVariables(testObject));
    }

    /**
     * Test of getProvidedVariables method, of class AnnotationInspector.
     */
    @Test
    public void testGetProvidedVariables_ClassObject() {
        List<String> provided = AnnotatedObject.getProvidedVariables();
        assertEquals(provided, AnnotationInspector.getProvidedVariables(AnnotatedObject.class));
        assertEquals(provided, AnnotationInspector.getProvidedVariables(testObject));
    }

    /**
     * Test of getRequiredConfigKeys method, of class AnnotationInspector.
     */
    @Test
    public void testGetRequiredConfigKeys_ClassObject() {
        List<String> requiredKeys = AnnotatedObject.getRequiredConfigKeys();
        assertEquals(requiredKeys, AnnotationInspector.getRequiredConfigKeys(AnnotatedObject.class));
        assertEquals(requiredKeys, AnnotationInspector.getRequiredConfigKeys(testObject));
    }

    /**
     * Test of getDefaultValueFor method, of class AnnotationInspector.
     */
    @Test
    public void testGetDefaultValueFor() {
        String expected1 = "10";
        assertEquals(expected1, AnnotationInspector.getDefaultValueFor(AnnotatedObject.class, "field1"));
        assertEquals("",AnnotationInspector.getDefaultValueFor(AnnotatedObject.class, "field2"));
        String expected3 = "field3value";
        assertEquals(expected3, AnnotationInspector.getDefaultValueFor(AnnotatedObject.class, "field3"));
    }

    /**
     * Test of getNameFor method, of class AnnotationInspector.
     */
    @Test
    public void testGetNameFor() {
        String expected1 = "cross.annotations.AnnotatedObject.field1";
        assertEquals(expected1, AnnotationInspector.getNameFor(AnnotatedObject.class, "myField1"));
        String expected2 = "cross.annotations.AnnotatedObject.field2";
        assertEquals(expected2, AnnotationInspector.getNameFor(AnnotatedObject.class, "field2"));
        String expected3 = "cross.annotations.AnnotatedObject.field3";
        assertEquals(expected3, AnnotationInspector.getNameFor(AnnotatedObject.class, "field3"));
        String expected4 = "";
        assertEquals(expected4, AnnotationInspector.getNameFor(AnnotatedObject.class, "field4"));
    }

    /**
     * Test of getDescriptionFor method, of class AnnotationInspector.
     */
    @Test
    public void testGetDescriptionFor() {
        String expected1 = "field1 for testing";
        assertEquals(expected1, AnnotationInspector.getDescriptionFor(AnnotatedObject.class, "myField1"));
        String expected2 = "field2 for testing";
        assertEquals(expected2, AnnotationInspector.getDescriptionFor(AnnotatedObject.class, "field2"));
        String expected3 = "";
        assertEquals(expected3, AnnotationInspector.getDescriptionFor(AnnotatedObject.class, "field3"));
        String expected4 = "";
        assertEquals(expected4, AnnotationInspector.getDescriptionFor(AnnotatedObject.class, "field4"));
    }

    /**
     * Test of getTypeFor method, of class AnnotationInspector.
     */
    @Test
    public void testGetTypeFor() {
        Class<?> field1 = int.class;
        assertEquals(field1, AnnotationInspector.getTypeFor(AnnotatedObject.class, "field1"));
        Class<?> field2 = int.class;
        assertEquals(field2, AnnotationInspector.getTypeFor(AnnotatedObject.class, "field2"));
        Class<?> field3 = String.class;
        assertEquals(field3, AnnotationInspector.getTypeFor(AnnotatedObject.class, "field3"));
        assertNull(AnnotationInspector.getTypeFor(AnnotatedObject.class, "field4"));
    }
}
