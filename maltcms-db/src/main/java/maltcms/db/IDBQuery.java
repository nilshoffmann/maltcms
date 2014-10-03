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
package maltcms.db;

import com.db4o.query.Predicate;
import cross.datastructures.tuple.Tuple2D;
import java.util.Collection;
import maltcms.datastructures.ms.IMetabolite;

/*
 * @author Nils Hoffmann
 */
/**
 * <p>IDBQuery interface.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public interface IDBQuery<T extends Predicate<G>, G> {

    /**
     * <p>setDB.</p>
     *
     * @param dbLocation a {@link java.lang.String} object.
     */
    public abstract void setDB(String dbLocation);

    /**
     * <p>getBestHits.</p>
     *
     * @param k a int.
     * @param ssp a T object.
     * @return a {@link java.util.Collection} object.
     */
    public abstract Collection<Tuple2D<Double, IMetabolite>> getBestHits(int k,
            T ssp);
}
