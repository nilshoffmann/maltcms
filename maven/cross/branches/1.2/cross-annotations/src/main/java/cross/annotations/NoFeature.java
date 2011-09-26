/*
 * $license$
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
 * Marker annotation which should be added to methods indicating, that this
 * method should not be considered when searching for feature type methods. Used
 * by {@link maltcms.db.predicates.PublicMemberGetters}.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 */
@Target(value = { ElementType.METHOD })
@Retention(value = RetentionPolicy.RUNTIME)
@Inherited
public @interface NoFeature {
}
