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
 * $Id: IRetentionInfo2D.java 110 2010-03-25 15:21:19Z nilshoffmann $
 */
package maltcms.datastructures.ms;


/**
 * Interface adding retention index 2d and time 2d  info.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public interface IRetentionInfo2D {

	public abstract double getRetentionIndex2D();

	public abstract double getRetentionTime2D();

	public abstract String getRetentionTimeUnit2D();

	public abstract void setRetentionIndex2D(double d);

	public abstract void setRetentionTime2D(double d);

	public abstract void setRetentionTimeUnit2D(String s);
	
}
