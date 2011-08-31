/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cross.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author nils
 */
@Target(value = { ElementType.TYPE })
@Retention(value = RetentionPolicy.RUNTIME)
@Inherited
public @interface RequiresTypes {
    Class[] values();
}
