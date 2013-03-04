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

import java.lang.annotation.*;

/**
 * <p>Marker annotation for member variables, which should be made available to
 * Configuration.</p>
 *
 * @author Nils Hoffmann
 * @version $Id$
 */
@Documented
@Target(value = {ElementType.FIELD})
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
     * @see AnnotationInspector will now retrieve the type information directly
     * from the annotated field.
     *
     * @return
     */
    @Deprecated
    Class<?> type() default Object.class;

    /**
     * The description of the annotated object. Returns an empty String by
     * default.
     *
     * @return
     */
    String description() default "";
}
