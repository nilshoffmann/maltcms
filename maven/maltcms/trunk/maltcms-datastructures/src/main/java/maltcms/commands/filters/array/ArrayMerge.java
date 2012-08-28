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
package maltcms.commands.filters.array;

import maltcms.datastructures.array.Sparse;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import cross.datastructures.tools.EvalTools;
import maltcms.commands.filters.array.AArrayFilter;

/**
 * Returns a list of all arrays, processed to be consistent with this objects
 * merging strategy. Merging two single arrays will lead to a single array.
 * Merging of two twin arrays, e.g. in m/z and i, where the first is the index
 * to the second, will lead to two arrays, the first with the consensus m/z
 * scale, and the second with the corresponding intensity values.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class ArrayMerge extends AArrayFilter {

    public ArrayMerge() {
        super();
    }

    @Override
    public Array[] apply(final Array[] a) {
        final Array[] b = super.apply(a);
        EvalTools.inRangeI(2, 2, b.length, this);
        final IndexIterator ii1 = b[0].getIndexIterator();
        final IndexIterator ii2 = b[1].getIndexIterator();
        Array ret = null;
        if ((b[0] instanceof Sparse) && (b[1] instanceof Sparse)) {
            ret = new Sparse(((Sparse) b[0]).getNumKeys(), ((Sparse) b[0])
                    .getMinIndex(), ((Sparse) b[0]).getMaxIndex());
        } else {
            ret = Array.factory(b[0].getElementType(), b[0].getShape());
        }
        final IndexIterator iir = ret.getIndexIterator();
        while (ii1.hasNext() && ii2.hasNext() && iir.hasNext()) {
            iir
                    .setDoubleNext((ii1.getDoubleNext() + ii2.getDoubleNext()) / 2.0d);
        }
        return new Array[]{ret};
    }
}
