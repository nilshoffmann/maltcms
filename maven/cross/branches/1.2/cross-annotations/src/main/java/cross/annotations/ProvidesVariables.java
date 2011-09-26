/*
 * 
 *
 * $Id$
 */

package cross.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which should be added to AFragmentCommand instances, declaring,
 * which variables, as given by their names, the annotated class creates.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 */
@Target(value = { ElementType.TYPE })
@Retention(value = RetentionPolicy.RUNTIME)
@Inherited
public @interface ProvidesVariables {
	String[] names();
}
