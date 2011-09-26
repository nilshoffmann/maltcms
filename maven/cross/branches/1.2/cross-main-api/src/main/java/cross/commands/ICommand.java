/*
 * $license$
 *
 * $Id$
 */

package cross.commands;

import cross.IConfigurable;

/**
 * Interface for objects which represent a command acting on an object of type <code>IN</code>
 * and returning objects of type <code>OUT</code>.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 * @param <IN>
 *            input type.
 * @param <OUT>
 *            output type.
 */
public interface ICommand<IN, OUT> extends IConfigurable {

    /**
     * Apply a command implementation to an Object of type <code>IN</code>,
     * returning an object of type <code>OUT</code>.
     * @param t
     * @return
     */
    public OUT apply(IN in);
}
