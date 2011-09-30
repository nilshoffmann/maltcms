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
 * Comparator implementation for IOptionHandler implementations.
 * Compares the priority of ICliOptionHandlers to order them in ascending
 * order of priority. Lower priority means more important, e.g. -100 is more 
 * important than 100. If two IOptionHandlers have the same priority, their 
 * order is determined by the service loader and class loader implementation.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 */
public class OptionHandlerComparator implements Comparator<IOptionHandler>{

    @Override
    public int compare(IOptionHandler t, IOptionHandler t1) {
        return t1.getPriority()-t.getPriority();
        
    }
    
}
