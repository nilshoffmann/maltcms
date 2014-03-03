/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code
 * under. Certain files or entire directories may not be covered by this
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package maltcms.datastructures.filters.array;

import cross.datastructures.tools.EvalTools;
import maltcms.commands.filters.array.AArrayFilter;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.Sparse;

/**
 * Returns a list of all arrays, processed to be consistent with this objects
 * merging strategy. Merging two single arrays will lead to a single array.
 * Merging of two twin arrays, e.g. in m/z and i, where the first is the index
 * to the second, will lead to two arrays, the first with the consensus m/z
 * scale, and the second with the corresponding intensity values.
 *
 * @author Nils Hoffmann
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

    @Override
    public ArrayMerge copy() {
        return new ArrayMerge();
    }
}
