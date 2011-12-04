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
 * $Id: IRecurrence.java 116 2010-06-17 08:46:30Z nilshoffmann $
 */
package maltcms.commands.distances;

import maltcms.datastructures.array.IArrayD2Double;
import cross.IConfigurable;

/**
 * Interface for classes performing a recurring operation, e.g. dynamic
 * programming.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public interface IRecurrence extends IConfigurable {

    public abstract double eval(int row, int column,
            IArrayD2Double cumDistMatrix, double dij, byte[][] predecessors);

    public abstract double eval(int row, int column,
            IArrayD2Double previousRow, IArrayD2Double currentRow, double dij);

    public abstract void set(double compression_weight,
            double expansion_weight, double diagonal_weight);

    public abstract void setMinimizing(boolean b);

    public abstract double getGlobalGapPenalty();
}
