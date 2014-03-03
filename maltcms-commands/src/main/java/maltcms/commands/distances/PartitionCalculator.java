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
package maltcms.commands.distances;

import cross.Factory;
import cross.IConfigurable;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.List;
import java.util.concurrent.Callable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.array.IArrayD2Double;
import org.apache.commons.configuration.Configuration;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;

@Slf4j
@Data
public class PartitionCalculator implements Callable<Integer>, IConfigurable {

    /**
     *
     */
    private static final long serialVersionUID = -7473548606374882072L;
    private Rectangle shape = null;
    private IArrayD2Double pa = null;
    private IDtwSimilarityFunction costFunction = null;
    private final ArrayDouble.D1 satRef, satQuery;
    private final List<Array> ref, query;

    public PartitionCalculator(final Rectangle shape1,
        final IArrayD2Double pa1, final ArrayDouble.D1 satRef1,
        final ArrayDouble.D1 satQuery1, final List<Array> ref1,
        final List<Array> query1) {
        this.shape = shape1;
        this.pa = pa1;
        this.satRef = satRef1;
        this.satQuery = satQuery1;
        this.ref = ref1;
        this.query = query1;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public Integer call() throws Exception {
        final Area a = this.pa.getShape();
        if (a.intersects(this.shape)) {
            log.debug("Bounds before intersection: {}", this.shape);
            final Area b = new Area(this.shape);
            b.intersect(a);
            final Rectangle r = b.getBounds();
            log.debug("Bounds after intersection: {}", r);
            int counter = 0;
            for (int i = r.y; i < r.y + r.height; i++) {
                final int[] bounds = this.pa.getColumnBounds(i);
                for (int j = bounds[0]; j < bounds[0] + bounds[1]; j++) {
                    this.pa.set(i, j, this.costFunction.apply(i, j,
                        this.satRef.get(i), this.satQuery.get(j),
                        this.ref.get(i), this.query.get(j)));
                    counter++;
                }
            }
            return new Integer(counter);
        } else {
            log.debug(
                "Job outside of defined bounds on PartitionedArray for rectangle {}",
                this.shape);
        }

        return new Integer(0);
    }

    /*
     * (non-Javadoc)
     *
     * @seecross.IConfigurable#configure(org.apache.commons.configuration.
     * Configuration)
     */
    @Override
    public void configure(final Configuration cfg) {
        final String aldist = "maltcms.commands.distances.ArrayLp";
        this.costFunction = Factory
            .getInstance()
            .getObjectFactory()
            .instantiate(
                cfg.getString("alignment.algorithm.distance", aldist),
                IDtwSimilarityFunction.class);
        log.debug("Using {}", this.costFunction.getClass().getName());

    }
}
