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
 * $Id: IOptimizationFunction.java 129 2010-06-25 11:57:02Z nilshoffmann $
 */
package maltcms.experimental.operations;

import java.awt.Point;
import java.util.List;

import maltcms.datastructures.array.IArrayD2Double;
import maltcms.datastructures.array.IFeatureVector;
import maltcms.datastructures.IFileFragmentModifier;

/**
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 */
public interface IOptimizationFunction extends IFileFragmentModifier {

	public abstract void init(List<IFeatureVector> l, List<IFeatureVector> r,
	        IArrayD2Double cumulatedScores, IArrayD2Double pwScores,
	        TwoFeatureVectorOperation tfvo);

	public abstract void apply(int... is);

	public abstract List<Point> getTrace();

	public abstract String getOptimalOperationSequenceString();

	public abstract String[] getStates();

	public abstract void setWeight(String state, double d);

	public abstract double getWeight(String state);

	public abstract double getOptimalValue();

}
