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
package cross.datastructures.tuple;

import java.io.Serializable;

/**
 * Class representing a tuple of two elements with Types T and U.
 *
 * @author Nils Hoffmann
 *
 */
public class Tuple2D<T, U> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5350596008036216528L;
    protected T first;
    protected U second;

    public Tuple2D(final T first1, final U second1) {
        this.first = first1;
        this.second = second1;
    }

    public T getFirst() {
        return this.first;
    }

    public U getSecond() {
        return this.second;
    }

    public void setFirst(final T t) {
        this.first = t;
    }

    public void setSecond(final U u) {
        this.second = u;
    }

    @Override
    public String toString() {
        return "( " + this.first.toString() + "; " + this.second.toString()
                + " )";
    }
}
