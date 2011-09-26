/*
 * $license$
 *
 * $Id$
 */

package cross.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for member variables, which should be made available to
 * Configuration.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Documented
@Target(value = { ElementType.FIELD })
@Retention(value = RetentionPolicy.RUNTIME)
@Inherited
public @interface Configurable {

	/**
	 * The name of the annotated object. Returns an empty String by default.
	 * 
	 * @return
	 */
	String name() default "";

	/**
	 * The value of the annotated object. Returns an empty String by default.
	 * 
	 * @return
	 */
	String value() default "";

	/**
	 * The class of the annotated object. Returns Object.class by default.
	 * 
	 * @return
	 */
	Class<?> type() default Object.class;
}
