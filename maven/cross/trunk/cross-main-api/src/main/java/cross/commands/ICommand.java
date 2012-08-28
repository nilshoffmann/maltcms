/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package cross.commands;

import cross.IConfigurable;

/**
 * Interface for objects which represent a command acting on an object of type
 * <code>IN</code> and returning objects of type
 * <code>OUT</code>.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 * @param <IN> input type.
 * @param <OUT> output type.
 */
public interface ICommand<IN, OUT> extends IConfigurable {

    /**
     * Apply a command implementation to an Object of type
     * <code>IN</code>, returning an object of type
     * <code>OUT</code>.
     *
     * @param t
     * @return
     */
    public OUT apply(IN in);
}
