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

package cross.io.cli;

import java.util.Comparator;

/**
 *
 * Comparator implementation for ICliOptionHandler implementations.
 * Compares the priority of ICliOptionHandlers to order them in ascending
 * order of priority. Lower priority means more important, e.g. -100 is more 
 * important than 100.
 * 
 * @author nilshoffmann
 */
public class CliOptionHandlerComparator implements Comparator<ICliOptionHandler>{

    @Override
    public int compare(ICliOptionHandler t, ICliOptionHandler t1) {
        return t.getPriority()-t1.getPriority();
    }
    
}
