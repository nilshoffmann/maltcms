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
 * $Id: Experiment2D.java 115 2010-04-23 15:42:15Z nilshoffmann $
 */

package maltcms.datastructures.ms;

import cross.datastructures.fragments.IFileFragment;

/**
 * Concrete Implementation containing a 2-dimensional chromatogram, e.g. from
 * GCxGC-MS.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class Experiment2D extends Experiment1D implements IExperiment2D {

	/**
     * 
     */
	public Experiment2D() {
		super();
	}

	/**
	 * @param ff1
	 */
	public Experiment2D(IFileFragment ff1) {
		super(ff1);
	}

	private IChromatogram2D ic2d = null;

	@Override
	public IChromatogram2D getChromatogram2D() {
		return this.ic2d;
	}

	@Override
	public void setChromatogram2D(final IChromatogram2D ic) {
		this.ic2d = ic;
		setChromatogram(new Chromatogram1D(ic.getParent()));
	}

}
