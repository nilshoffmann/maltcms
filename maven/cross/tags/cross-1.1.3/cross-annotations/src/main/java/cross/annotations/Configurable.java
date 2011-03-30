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
 * $Id: Configurable.java 116 2010-06-17 08:46:30Z nilshoffmann $
 */

/**
 * 
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
