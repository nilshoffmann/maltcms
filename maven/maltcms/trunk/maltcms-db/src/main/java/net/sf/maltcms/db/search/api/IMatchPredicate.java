/*
 * $license$
 *
 * $Id$
 */
package net.sf.maltcms.db.search.api;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public interface IMatchPredicate<T> {

    public boolean match(T t);
}
