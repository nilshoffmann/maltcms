/**
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
 */
/*
 * 
 *
 * $Id$
 */

package cross.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

enum ClassMember {
	CONSTRUCTOR, FIELD, METHOD, CLASS, ALL
}

public class ClassSpy {

	private static void printClasses(final Class<?> c) {
		System.out.format("Classes:%n");
		final Class<?>[] clss = c.getClasses();
		for (final Class<?> cls : clss) {
			System.out.format("  %s%n", cls.getCanonicalName());
		}
		if (clss.length == 0) {
			System.out
					.format("  -- No member interfaces, classes, or enums --%n");
		}
		System.out.format("%n");
	}

	@SuppressWarnings("rawtypes")
	private static void printMembers(final Member[] mbrs, final String s) {
		System.out.format("%s:%n", s);
		for (final Member mbr : mbrs) {
			if (mbr instanceof Field) {
				System.out.format("  %s%n", ((Field) mbr).toGenericString());
			} else if (mbr instanceof Constructor) {
				System.out.format("  %s%n",
						((Constructor) mbr).toGenericString());
			} else if (mbr instanceof Method) {
				System.out.format("  %s%n", ((Method) mbr).toGenericString());
			}
		}
		if (mbrs.length == 0) {
			System.out.format("  -- No %s --%n", s);
		}
		System.out.format("%n");
	}

	public ClassSpy(final String s) {
		try {
			final Class<?> c = Class.forName(s);
			System.out.format("Class:%n  %s%n%n", c.getCanonicalName());

			final Package p = c.getPackage();
			System.out.format("Package:%n  %s%n%n", (p != null ? p.getName()
					: "-- No Package --"));

			ClassSpy.printMembers(c.getDeclaredConstructors(), "Constuctors");
			ClassSpy.printMembers(c.getDeclaredFields(), "Fields");
			ClassSpy.printMembers(c.getDeclaredMethods(), "Methods");
			ClassSpy.printClasses(c);

			// production code should handle these exceptions more gracefully
		} catch (final ClassNotFoundException x) {
			x.printStackTrace();
		}
	}
}
