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
 * $Id: AlignmentFactory.java 129 2010-06-25 11:57:02Z nilshoffmann $
 */
package maltcms.experimental.operations;

import java.awt.geom.Area;

import cross.Factory;

/**
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 */
public class AlignmentFactory {

	/**
	 * Returns a new DTW instance, configured by ObjectFactory.
	 * 
	 * @return
	 */
	public IAlignment getDTWInstance() {
		IAlignment ia = Factory.getInstance().getObjectFactory().instantiate(
		        DTW.class);
		return ia;
	}

	public IAlignment getDTWInstance(IOptimizationFunction iof,
	        TwoFeatureVectorOperation tfvo, Area constraints) {
		IAlignment ia = getDTWInstance();
		ia.setIOptimizationFunction(iof);
		ia.setPairwiseFeatureVectorOperation(tfvo);
		ia.setConstraints(constraints);
		return ia;
	}

}