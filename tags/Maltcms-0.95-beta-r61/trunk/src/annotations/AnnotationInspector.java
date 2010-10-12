/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
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
 * $Id$
 */

package annotations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;

import cross.Logging;

/**
 * Class which provides static utility methods to test for presence of specific
 * annotations.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class AnnotationInspector {

	static Logger log = Logging.getLogger(AnnotationInspector.class);

	public static Collection<String> getRequiredVariables(final Object o) {
		final Collection<String> coll = new ArrayList<String>();
		AnnotationInspector.inspectTypeRequiredVariables(o.getClass(), coll);
		final Class<?> clazz = o.getClass().getSuperclass();
		if (clazz != null) {
			AnnotationInspector.inspectTypeRequiredVariables(clazz, coll);
		}
		return coll;
	}

	private static void inspectTypeRequiredVariables(final Class<?> c,
	        final Collection<String> coll) {
		if (c.isAnnotationPresent(RequiresVariables.class)) {
			String[] s = c.getAnnotation(RequiresVariables.class).names();
			coll.addAll(java.util.Arrays.asList(s));
		}
	}

	private static void inspectTypeProvidedVariables(final Class<?> c,
	        final Collection<String> coll) {
		if (c.isAnnotationPresent(ProvidesVariables.class)) {
			String[] s = c.getAnnotation(ProvidesVariables.class).names();
			coll.addAll(java.util.Arrays.asList(s));
		}
	}

	private static void inspectTypeOptionalRequiredVariables(final Class<?> c,
	        final Collection<String> coll) {
		if (c.isAnnotationPresent(RequiresOptionalVariables.class)) {
			String[] s = c.getAnnotation(RequiresOptionalVariables.class)
			        .names();
			coll.addAll(java.util.Arrays.asList(s));
		}
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

	public static Collection<String> getRequiredConfigKeys(final Object o) {
		final Collection<String> coll = new ArrayList<String>();
		AnnotationInspector.inspectFields(o.getClass(), coll);
		final Class<?> clazz = o.getClass().getSuperclass();
		if (clazz != null) {
			AnnotationInspector.inspectFields(clazz, coll);
		}
		return coll;
	}

	private static void inspectFields(final Class<?> c,
	        final Collection<String> coll) {
		final Field[] f = c.getDeclaredFields();
		for (final Field fld : f) {
			if (fld.isAnnotationPresent(Configurable.class)) {
				String name = fld.getAnnotation(Configurable.class).name();
				if (name.contains(".")) { //$NON-NLS-1$
					// globally qualified, leave as is
					AnnotationInspector.log.debug(Messages
					        .getString("AnnotationInspector.1"), //$NON-NLS-1$
					        name);
				} else {
					// locally qualified name, prepend package name
					AnnotationInspector.log.debug(Messages
					        .getString("AnnotationInspector.2"), //$NON-NLS-1$
					        name, c.getName() + "." + name); //$NON-NLS-1$
					name = c.getName() + "." + name; //$NON-NLS-1$

				}
				AnnotationInspector.log.debug(Messages
				        .getString("AnnotationInspector.5"), //$NON-NLS-1$
				        c.getName(), name);
				coll.add(name);
			}
		}
	}

}
