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
package maltcms.datastructures.ms;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class TreatmentGroup2D implements ITreatmentGroup<IChromatogram2D> {

	List<IChromatogram2D> l = new ArrayList<IChromatogram2D>();

	private String name = "";

	/*
	 * (non-Javadoc)
	 * 
	 * @seemaltcms.datastructures.ms.ITreatmentGroup#addChromatogram(maltcms.
	 * datastructures.ms.IChromatogram)
	 */
	@Override
	public void addChromatogram(IChromatogram2D t) {
		this.l.add(t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.ms.ITreatmentGroup#getChromatograms()
	 */
	@Override
	public List<IChromatogram2D> getChromatograms() {
		return this.l;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.ms.ITreatmentGroup#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.ms.ITreatmentGroup#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

}
