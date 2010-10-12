package cross.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

enum ClassMember {
	CONSTRUCTOR, FIELD, METHOD, CLASS, ALL
}

public class ClassSpy {

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
