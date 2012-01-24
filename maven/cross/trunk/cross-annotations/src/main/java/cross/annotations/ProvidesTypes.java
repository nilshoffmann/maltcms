/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cross.annotations;

import java.lang.annotation.*;

/**
 *
 * @author nils
 */
@Target(value = { ElementType.TYPE })
@Retention(value = RetentionPolicy.RUNTIME)
@Inherited
public @interface ProvidesTypes {
    Class[] values();
}
