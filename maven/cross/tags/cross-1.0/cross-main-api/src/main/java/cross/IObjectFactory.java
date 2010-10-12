/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
