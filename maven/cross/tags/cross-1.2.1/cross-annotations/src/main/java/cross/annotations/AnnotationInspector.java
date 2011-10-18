/*
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: AnnotationInspector.java 105 2010-03-10 11:15:53Z nilshoffmann $
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
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class AnnotationInspector {

    static Logger log = LoggerFactory.getLogger(AnnotationInspector.class);

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
        final Field[] f = c.getDeclaredFields();
        for (final Field fld : f) {
            if (fld.isAnnotationPresent(Configurable.class)) {
                String key = fld.getAnnotation(Configurable.class).name();
                AnnotationInspector.log.info("Key: {}", key);
                if (key.isEmpty()) {
                    key = c.getName() + "." + fld.getName();
                }
                AnnotationInspector.log.info("Key: {}", key);
                if (key.endsWith(name)) {
                    AnnotationInspector.log.info("Key: {}", key);
                    return fld.getAnnotation(Configurable.class).value();
                }
            }
        }
        return "";
    }

    public static Class<?> getTypeFor(final Class<?> c, final String name) {
        final Field[] f = c.getDeclaredFields();
        for (final Field fld : f) {
            if (fld.isAnnotationPresent(Configurable.class)) {
                Class<?> clazz = fld.getAnnotation(Configurable.class).type();
                String key = fld.getAnnotation(Configurable.class).name();
                if (key.endsWith(name)) {
                    if (clazz == null) {
                        return String.class;
                    }
                    return clazz;
                }

            }
        }
        return null;
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
