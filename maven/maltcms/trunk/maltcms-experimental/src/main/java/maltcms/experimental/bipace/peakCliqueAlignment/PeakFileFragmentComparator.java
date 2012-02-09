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
package maltcms.experimental.bipace.peakCliqueAlignment;

import java.util.Comparator;
import maltcms.datastructures.peak.Peak;

/**
 *
 * @author nils
 */
public class PeakFileFragmentComparator implements Comparator<Peak> {

    @Override
    public int compare(final Peak o1, final Peak o2) {
        final int i = new PeakComparator().compare(o1, o2);
        if (i == 0) {
            return o1.getAssociation().getName().compareTo(o2.getAssociation().getName());
        }
        return i;
    }
}
