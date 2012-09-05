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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

enum ClassMember {

    CONSTRUCTOR, FIELD, METHOD, CLASS, ALL
}

/**
 * <p>ClassSpy class.</p>
 *
 * @author Nils Hoffmann
 * @version $Id$
 */
public class ClassSpy {

    /**
     * <p>main.</p>
     *
     * @param args a {@link java.lang.String} object.
     */
    public static void main(final String... args) {
    }

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

    private static void printMembers(final Member[] mbrs, final String s) {
        System.out.format("%s:%n", s);
        for (final Member mbr : mbrs) {
            if (mbr instanceof Field) {
                System.out.format("  %s%n", ((Field) mbr).toGenericString());
            } else if (mbr instanceof Constructor) {
                System.out.format("  %s%n", ((Constructor) mbr)
                        .toGenericString());
            } else if (mbr instanceof Method) {
                System.out.format("  %s%n", ((Method) mbr).toGenericString());
            }
        }
        if (mbrs.length == 0) {
            System.out.format("  -- No %s --%n", s);
        }
        System.out.format("%n");
    }

    /**
     * <p>Constructor for ClassSpy.</p>
     *
     * @param s a {@link java.lang.String} object.
     */
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
