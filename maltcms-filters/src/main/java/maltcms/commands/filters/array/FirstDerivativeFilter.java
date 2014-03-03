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
package maltcms.commands.filters.array;

import lombok.Data;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;

/**
 * Calculate first derivative on array stored in a new array.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Data
@ServiceProvider(service = AArrayFilter.class)
public class FirstDerivativeFilter extends AArrayFilter {

    public FirstDerivativeFilter() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.ucar.ma2.ArrayFilter#filter(maltcms.ucar.ma2.Array)
     */
    @Override
    public Array apply(final Array a) {
        final Array arr = super.apply(a);
        Double last = 0.0d, current = 0.0d, next = 0.0d, derivative = 0.0d;
        final IndexIterator ii = arr.getIndexIterator();
        final Array der = Array.factory(arr.getElementType(), arr.getShape());
        final IndexIterator derii = der.getIndexIterator();
        long cnt = 0;
        while (ii.hasNext() && derii.hasNext()) {
            next = ii.getDoubleNext();
            if (cnt == 0) {
                last = next;
            } else if (cnt == 1) {
                current = next;
            } else if (cnt == arr.getSize() - 1) {// set value of last
                // derivative to the
                // previous value
                derii.setDoubleNext(derivative);
                continue;// exit the loop
            } else {
                derivative = (current - last + (next - last)) / 2.0d / 2.0d;// calculate
                // approximate
                // first
                // derivative
                derii.setDoubleNext(derivative);
                last = current;
                current = next;
            }
            cnt++;
        }
        der.setDouble(der.getIndex().set(0),
            arr.getDouble(arr.getIndex().set(1)));// set value of first element to that of the
        // second element
        return der;
    }

    @Override
    public FirstDerivativeFilter copy() {
        return new FirstDerivativeFilter();
    }
}
