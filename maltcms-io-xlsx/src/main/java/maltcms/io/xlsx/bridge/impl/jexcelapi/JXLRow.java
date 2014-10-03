/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
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
package maltcms.io.xlsx.bridge.impl.jexcelapi;

import java.util.Iterator;
import jxl.Cell;
import lombok.Data;
import maltcms.io.xlsx.bridge.ICell;
import maltcms.io.xlsx.bridge.IRow;

/**
 * <p>JXLRow class.</p>
 *
 * @author Nils Hoffmann
 * @version $Id: $Id
 */
@Data
public class JXLRow implements IRow {

    private final Cell[] row;

    /** {@inheritDoc} */
    @Override
    public ICell getCell(int i) {
        return new JXLCell(row[i]);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<ICell> iterator() {
        return new Iterator<ICell>() {

            int idx = 0;

            @Override
            public boolean hasNext() {
                return idx < row.length;
            }

            @Override
            public ICell next() {
                return getCell(idx++);
            }

            @Override
            public void remove() {
            }
        };
    }

}
