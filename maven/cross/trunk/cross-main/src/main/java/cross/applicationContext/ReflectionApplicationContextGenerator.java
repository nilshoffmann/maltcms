/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cross.applicationContext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This code is based on https://github.com/mangstadt/Spring-Application-Context-Generator
 * but uses the compiled class information via reflection and the 
 * java service provider/netbeans lookup API in order to resolve references.
 * Values are populated with their default values from the instantiated objects.
 * 
 * The Generator will accept any number of concrete or abstract fully qualified class names
 * and will introspect the associated classes. Abstract classes will be used to query for 
 * corresponding service implementations. Classes must have a default no-args constructor
 * in order for the service loader facility to work properly, so there are no constructor-arg
 * elements created at the moment.
 * 
 * @author nilshoffmann
 */
public class ReflectionApplicationContextGenerator {

    public static void main(String[] args) {
        String[] classes = args;
        String springVersion = "3.0";
        String scope = "prototype";
        //generate the application context XML
        ReflectionApplicationContextGenerator generator = new ReflectionApplicationContextGenerator(springVersion,scope);
        for (String className : classes) {
            try {
                Collection<?> serviceImplementations = Lookup.getDefault().lookupAll(Class.forName(className));
                if (serviceImplementations.isEmpty()) {
                    //standard bean, add it
                    try {
                        generator.addBean(className);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {//service provider implementation
                    for (Object obj : serviceImplementations) {
                        try {
                            generator.addBean(obj.getClass().getCanonicalName());
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        Document document = generator.getDocument();

        //output the XML
        String xmlString;
        {
            try {
                TransformerFactory transfac = TransformerFactory.newInstance();
                Transformer trans = transfac.newTransformer();
                trans.setOutputProperty(OutputKeys.INDENT, "yes");
                StringWriter sw = new StringWriter();
                StreamResult result = new StreamResult(sw);
                DOMSource domSource = new DOMSource(document);
                trans.transform(domSource, result);
                xmlString = sw.toString();
                BufferedWriter bw = null;
                try {
                    bw = new BufferedWriter(new FileWriter(new File("applicationContext.xml")));
                    bw.write(xmlString);
                    bw.close();
                } catch (IOException ex) {
                    Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    if (bw != null) {
                        try {
                            bw.close();
                        } catch (IOException ex) {
                            Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

                System.out.println(xmlString);
            } catch (TransformerException ex) {
                Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    /**
     * The list of Java primative types.
     */
    private static final List<String> primatives = Arrays.asList(new String[]{"byte", "short", "char", "int", "long", "float", "double", "boolean"});
    /**
     * The list of Java wrapper classes (includes String).
     */
    private static final List<String> wrappers = Arrays.asList(new String[]{"Byte", "Short", "Character", "Integer", "Long", "Float", "Double", "Boolean", "String"});
    /**
     * The XML document.
     */
    private final Document document;
    /**
     * The XML root element.
     */
    private final Element root;
    private LinkedHashMap<Class, Element> classToElement = new LinkedHashMap<Class, Element>();
    private String defaultScope = "prototype";

    /**
     * Constructs a new application context generator.
     * @param springVersion the Spring version
     */
    public ReflectionApplicationContextGenerator(String springVersion, String defaultScope) {
        //create the XML document
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = dbfac.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            //never thrown in my case, so ignore it
        }
        document = docBuilder.newDocument();

        //create the root element
        root = document.createElementNS("http://www.springframework.org/schema/beans", "beans");
        root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation", "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-" + springVersion + ".xsd");
        document.appendChild(root);
        this.defaultScope = defaultScope;
    }

    /**
     * Gets the generated XML document.
     * @return the XML document
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Adds a bean to the application context using a Java source file. Only
     * public classes are added.
     * @param reader the input stream to the Java source file (this is closed
     * after it is read)
     * @return this
     * @throws IOException if there's a problem reading the file
     */
    public ReflectionApplicationContextGenerator addBean(String className) throws ClassNotFoundException {
        Class<?> clazz;
        clazz = getClass().getClassLoader().loadClass(className);
        if (!classToElement.containsKey(clazz)) {
            //build a "bean" element for each class
            Element beanElement = buildBeanElement(clazz);
            if (beanElement != null) {
                root.appendChild(beanElement);
                classToElement.put(clazz, beanElement);
            } else {
                //System.err.println("Warning: Could not find public class in \"" + file + "\".");
            }
            
        }

        return this;
    }

    public static String toLowerCaseName(String s) {
        return s.substring(0, 1).toLowerCase() + s.substring(1);
    }

    /**
     * Creates the &lt;bean /&gt; element.
     * @param javaClass the Java source code
     * @return the &lt;bean /&gt; element or null if there were no public
     * classes.
     */
    private Element buildBeanElement(Class<?> javaClass) {
        System.out.println("Building bean element for " + javaClass.getName());
        //get the name of the class
        String className = javaClass.getSimpleName();

        //get the name of the package
        String packageName = javaClass.getPackage().getName();

        //create <bean /> element
        Element beanElement = document.createElement("bean");
        String classNameLower = toLowerCaseName(className);
        beanElement.setAttribute("id", classNameLower);
        String classAttr = (packageName == null) ? className : packageName + "." + className;
        beanElement.setAttribute("class", classAttr);
        beanElement.setAttribute("scope", defaultScope);

        if (javaClass.getConstructors().length == 0) {
            throw new IllegalArgumentException("Class " + className + " needs to have a public default constructor without parameters!");
        }
        //constructors are not supported

        //get all the class' properties from the public fields and setter methods.
        List<ClassProperty> properties = new ArrayList<ClassProperty>();
        Method[] publicMethods = javaClass.getMethods();
        Object obj;
        try {
            //create a new instance to retrieve possible default values
            obj = javaClass.newInstance();
            for (Method method : publicMethods) {
                if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
                    String methodBaseName = method.getName().startsWith("get")?method.getName().substring(3):method.getName().substring(2);
                    try {
                        //check for corresponding setter
                        if (javaClass.getMethod("set" + methodBaseName, method.getReturnType()) != null) {
                            String propertyName = toLowerCaseName(methodBaseName);
                            System.out.print("Handling property " + propertyName);
                            ClassProperty p = new ClassProperty();
                            Class<?> returnType = method.getReturnType();
                            if (returnType.isArray()) {
                                p.type = "Array";
                            } else {
                                p.type = returnType.getCanonicalName();
                            }
                            p.name = propertyName;

                            Object methodObject = null;
                            try {
                                methodObject = method.invoke(obj);
                            } catch (IllegalAccessException ex) {
                                Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IllegalArgumentException ex) {
                                Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (InvocationTargetException ex) {
                                Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            String value = null;
                            if (methodObject == null) {
                                value = "";
                            } else {
                                if (methodObject.getClass().isArray()) {
                                    value = methodObject.getClass().getComponentType().getCanonicalName();
                                } else {
                                    value = methodObject.toString();
                                }
                            }
                            if (value == null) {
                                value = "";
                            } else {
                                value = value.trim();
                            }
                            if (value.startsWith("\"")) {
                                //remove the quotes that surround Strings
                                value = value.substring(1, value.length() - 1);
                            } else if (value.startsWith("'")) {
                                //remove the quotes that surround characters
                                value = value.substring(1, value.length() - 1);
                            } else if (value.endsWith("d") || value.endsWith("D") || value.endsWith("f") || value.endsWith("F") || value.endsWith("l") || value.endsWith("L")) {
                                //remove the "double", "float", or "long" letters if they are there
                                value = value.substring(0, value.length() - 1);
                            }
                            p.value = value;

                            System.out.println(" value: " + p.value + " type: " + p.type);
                            properties.add(p);
                        }
                    } catch (NoSuchMethodException ex) {
                        System.out.println("Ignoring read-only property " + methodBaseName);
                        //Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SecurityException ex) {
                        Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            obj = null;
            Collections.sort(properties, new Comparator<ClassProperty>() {

                public int compare(ClassProperty t, ClassProperty t1) {
                    return t.name.compareTo(t1.name);
                }
            });

            //add all properties as <property /> elements
            for (ClassProperty p : properties) {
                Element propertyElement = document.createElement("property");
                propertyElement.setAttribute("name", p.name);
                if (p.type.startsWith("java.lang.")) {
                    String shortType = p.type.substring("java.lang.".length());
                    if (primatives.contains(shortType) || wrappers.contains(shortType)) {
                        propertyElement.setAttribute("value", p.value);
                    }
                } else if (primatives.contains(p.type) || wrappers.contains(p.type)) {
                    propertyElement.setAttribute("value", p.value);
                } else if ("Array".equals(p.type)
                        || "List".equals(p.type) || "java.util.List".equals(p.type)) {
                    Element listElement = document.createElement("list");
                    propertyElement.appendChild(listElement);
                } else if ("Set".equals(p.type) || "java.util.Set".equals(p.type)) {
                    Element listElement = document.createElement("set");
                    propertyElement.appendChild(listElement);
                } else if ("Map".equals(p.type) || "java.util.Map".equals(p.type)) {
                    Element listElement = document.createElement("map");
                    propertyElement.appendChild(listElement);
                } else if ("Properties".equals(p.type) || "java.util.Properties".equals(p.type)) {
                    Element listElement = document.createElement("props");
                    propertyElement.appendChild(listElement);
                } else {
                    Class<?> c;
                    System.out.println("Inspecting referenced bean " + p.name + " of type " + p.type);
                    try {
                        c = getClass().getClassLoader().loadClass(p.type);
                        if (c.isInterface() || Modifier.isAbstract(c.getModifiers())) {
                            //retrieve all ServiceProvider implementations of given type
                            Collection<?> objects = Lookup.getDefault().lookupAll(c);
                            if (!objects.isEmpty()) {
                                Object first = objects.iterator().next();
//                                for (Object object : objects) {
//                                    if (first == null) {
//                                        first = object;
                                        try {
                                            addBean(first.getClass().getCanonicalName());
                                            propertyElement.setAttribute("ref", toLowerCaseName(first.getClass().getSimpleName()));
                                        } catch (Exception e) {
                                            propertyElement.setAttribute("ref", "null");
                                        }
                                        
//                                    }
//                                }

                            } else {
                                System.out.println("Could not retrieve ServiceProviders for type " + p.type);
                                propertyElement.setAttribute("ref", "null");
                            }
                        } else {
                            try {
                                addBean(c.getCanonicalName());
                                propertyElement.setAttribute("ref", toLowerCaseName(c.getSimpleName()));
                            } catch (Exception e) {
                                propertyElement.setAttribute("ref", "null");
                            }

                        }
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
                        propertyElement.setAttribute("value", "null");
                    }
                }
                beanElement.appendChild(propertyElement);
            }
        } catch (InstantiationException ex) {
            Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }


        return beanElement;
    }

    private class ClassProperty {

        public String name;
        public String type;
        public String value;
    }
}
