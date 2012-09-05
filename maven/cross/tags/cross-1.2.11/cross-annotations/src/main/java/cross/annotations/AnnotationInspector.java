/* 
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.annotations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which provides static utility methods to test for presence of specific
 * cross.annotations.
 *
 * @author Nils Hoffmann
 */
public final class AnnotationInspector {

    static Logger log = LoggerFactory.getLogger(AnnotationInspector.class);

    private AnnotationInspector() {
    }

    public static Collection<String> getOptionalRequiredVariables(
            final Class<?> c) {
        final Collection<String> coll = new ArrayList<String>();
        AnnotationInspector.inspectTypeOptionalRequiredVariables(c, coll);
        AnnotationInspector.inspectTypeOptionalRequiredVariables(c.getSuperclass(), coll);
        return coll;
    }

    public static Collection<String> getRequiredVariables(final Class<?> c) {
        final Collection<String> coll = new ArrayList<String>();
        AnnotationInspector.inspectTypeRequiredVariables(c, coll);
        AnnotationInspector.inspectTypeRequiredVariables(c.getSuperclass(),
                coll);
        return coll;
    }

    public static Collection<String> getProvidedVariables(final Class<?> c) {
        final Collection<String> coll = new ArrayList<String>();
        AnnotationInspector.inspectTypeProvidedVariables(c, coll);
        final Class<?> clazz = c.getSuperclass();
        if (clazz != null) {
            AnnotationInspector.inspectTypeProvidedVariables(clazz, coll);
        }
        return coll;
    }

    public static Collection<String> getOptionalRequiredVariables(final Object o) {
        final Collection<String> coll = new ArrayList<String>();
        AnnotationInspector.inspectTypeOptionalRequiredVariables(o.getClass(),
                coll);
        final Class<?> clazz = o.getClass().getSuperclass();
        if (clazz != null) {
            AnnotationInspector.inspectTypeOptionalRequiredVariables(clazz,
                    coll);
        }
        return coll;
    }

    public static Collection<String> getProvidedVariables(final Object o) {
        final Collection<String> coll = new ArrayList<String>();
        AnnotationInspector.inspectTypeProvidedVariables(o.getClass(), coll);
        final Class<?> clazz = o.getClass().getSuperclass();
        if (clazz != null) {
            AnnotationInspector.inspectTypeProvidedVariables(clazz, coll);
        }
        return coll;
    }

    public static Collection<String> getRequiredConfigKeys(final Class<?> c) {
        final Collection<String> coll = new ArrayList<String>();
        final Class<?> clazz = c;
        if (clazz != null) {
            AnnotationInspector.inspectFields(clazz, coll);
        }
        return coll;
    }

    public static Collection<String> getRequiredConfigKeys(final Object o) {
        final Collection<String> coll = new ArrayList<String>();
        AnnotationInspector.inspectFields(o.getClass(), coll);
        final Class<?> clazz = o.getClass().getSuperclass();
        if (clazz != null) {
            AnnotationInspector.inspectFields(clazz, coll);
        }
        return coll;
    }

    public static Collection<String> getRequiredVariables(final Object o) {
        final Collection<String> coll = new ArrayList<String>();
        AnnotationInspector.inspectTypeRequiredVariables(o.getClass(), coll);
        final Class<?> clazz = o.getClass().getSuperclass();
        if (clazz != null) {
            AnnotationInspector.inspectTypeRequiredVariables(clazz, coll);
        }
        return coll;
    }

    public static String getDefaultValueFor(final Class<?> c, final String name) {
        Field fld = getFieldForName(c, name);
        if (fld == null) {
            return "";
        }
        String dval = fld.getAnnotation(Configurable.class).value();
        AnnotationInspector.log.info("Returning default value: {}", dval);
        return dval;

    }

    public static String getNameFor(final Class<?> c, final String name) {
        Field fld = getFieldForName(c, name);
        if (fld == null) {
            return "";
        }
        return getNameFor(c, fld);
    }

    public static String getNameFor(final Class<?> c, Field fld) {
        return normalizeName(c, fld);
    }

    public static String getDescriptionFor(final Class<?> c, final String name) {
        Field fld = getFieldForName(c, name);
        if (fld == null) {
            return "";
        }
        String descr = fld.getAnnotation(Configurable.class).description();
        AnnotationInspector.log.info("Returning description: {}", descr);
        return descr;
    }

    public static Class<?> getTypeFor(final Class<?> c, final String name) {
        Field fld = getFieldForName(c, name);
        if (fld == null) {
            return null;
        }
        AnnotationInspector.log.info("Returning type of field: {}", fld.getType());
        return fld.getType();
    }

    private static Field getFieldForName(Class<?> c, String name) {
        final Field[] f = c.getDeclaredFields();
        for (final Field fld : f) {
            if (fld.isAnnotationPresent(Configurable.class)) {
                String internalName = fld.getName();
                if (internalName.endsWith(name)) {
                    AnnotationInspector.log.info("Returning field: {}", normalizeName(c, fld));
                    return fld;
                }
                String externalName = fld.getAnnotation(Configurable.class).name();
                if (externalName.isEmpty()) {
                    externalName = normalizeName(c, fld);
                }
                if (externalName.endsWith(name)) {
                    AnnotationInspector.log.info("Returning field: {}", externalName);
                    return fld;
                }
            }
        }

        return null;
    }

    private static String normalizeName(Class<?> c, Field f) {
        String name = c.getName() + "." + f.getName();
        AnnotationInspector.log.info("Returning normalized name: {}", name);
        return name;
    }

    private static void inspectFields(final Class<?> c,
            final Collection<String> coll) {
        final Field[] f = c.getDeclaredFields();
        for (final Field fld : f) {
            if (fld.isAnnotationPresent(Configurable.class)) {
                String name = fld.getAnnotation(Configurable.class).name();
                if (name.contains(".")) {
                    // globally qualified, leave as is
                    AnnotationInspector.log.debug(
                            "Name appears to be globally qualified with dot(s) in {}",
                            name);
                } else if (!name.isEmpty()) {
                    // locally qualified name, prepend package name
                    AnnotationInspector.log.debug(
                            "{} is locally qualified without dot(s), prepending package and class name: {}",
                            name, c.getName() + "." + name);
                    name = c.getName() + "." + name;

                } else {

                    AnnotationInspector.log.debug("No name set, trying to access field name!");
                    AnnotationInspector.log.debug(
                            "{} has no name set, setting to field name and prepending package and class name: {}",
                            name, c.getName() + "." + fld.getName());
                    name = c.getName() + "." + fld.getName();
                }
                AnnotationInspector.log.debug(
                        "Annotation Configurable is present on {} > {}", c.getName(), name);
                coll.add(name);
            }
        }
    }

    private static void inspectTypeOptionalRequiredVariables(final Class<?> c,
            final Collection<String> coll) {
        if (c.isAnnotationPresent(RequiresOptionalVariables.class)) {
            final String[] s = c.getAnnotation(RequiresOptionalVariables.class).names();
            coll.addAll(java.util.Arrays.asList(s));
        }
    }

    private static void inspectTypeProvidedVariables(final Class<?> c,
            final Collection<String> coll) {
        if (c.isAnnotationPresent(ProvidesVariables.class)) {
            final String[] s = c.getAnnotation(ProvidesVariables.class).names();
            coll.addAll(java.util.Arrays.asList(s));
        }
    }

    private static void inspectTypeRequiredVariables(final Class<?> c,
            final Collection<String> coll) {
        if (c.isAnnotationPresent(RequiresVariables.class)) {
            final String[] s = c.getAnnotation(RequiresVariables.class).names();
            coll.addAll(java.util.Arrays.asList(s));
        }
    }
}
