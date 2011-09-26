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

package cross;

import org.apache.commons.configuration.Configuration;

/**
 *
 * @author nilshoffmann
 */
public interface IObjectFactory extends IConfigurable {

    <T> void configureType(final T t);

    /**
     * Create a new Instance of c, configure automatically, if c is an instance
     * of IConfigurable
     *
     * @param <T>
     * @param c
     * @return
     */
    <T> T instantiate(final Class<T> c);

    /**
     * Instantiate a class, given by a classname and the class of Type T.
     *
     * @param <T>
     * @param classname
     * @param cls
     * @return
     */
    <T> T instantiate(final String classname, final Class<T> cls);

    /**
     * Instantiate a class, given a classname and the class of Type t and
     * configure with configuration from configurationFile.
     *
     * @param <T>
     * @param classname
     * @param cls
     * @param configurationFile
     * @return
     */
    <T> T instantiate(final String classname, final Class<T> cls, final String configurationFile);

    /**
     * Instantiate a class, given a classname and the class of Type t and
     * configure with configuration from config.
     *
     * @param <T>
     * @param classname
     * @param cls
     * @param config
     * @return
     */
    <T> T instantiate(final String classname, final Class<T> cls, final Configuration config);

}
