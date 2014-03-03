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
package maltcms.commands.distances.dtw;

import cross.annotations.Configurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import maltcms.commands.distances.PairwiseFeatureSequenceSimilarity;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;

/**
 * Dynamic Time Warp on univariate Time Series, e.g. the total ion current
 * (TIC).
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Data
@ServiceProvider(service = PairwiseFeatureSequenceSimilarity.class)
public class TICDynamicTimeWarp extends ADynamicTimeWarp {

    @Configurable
    private String arrayVariable = "total_intensity";

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.commands.distances.dtw.ADynamicTimeWarp#configure(org.apache.
     * commons.configuration.Configuration)
     */
    /**
     *
     * @param cfg
     */
    @Override
    public void configure(final Configuration cfg) {
        super.configure(cfg);
        this.arrayVariable = cfg.getString("var.total_intensity", "total_intensity");
    }

    /**
     *
     * @param t
     * @return
     */
    @Override
    public Tuple2D<List<Array>, List<Array>> createTuple(
        final Tuple2D<IFileFragment, IFileFragment> t) {
        Array ticRef = null;
        synchronized (t.getFirst()) {
            ticRef = t.getFirst().getChild(this.arrayVariable).getArray();
        }
        Array queryRef = null;
        synchronized (t.getSecond()) {
            queryRef = t.getSecond().getChild(this.arrayVariable).getArray();
        }
        final List<Array> ref = new ArrayList<Array>();
        final List<Array> query = new ArrayList<Array>();
        IndexIterator iref = ticRef.getIndexIterator();
        final Index idx = Index.scalarIndexImmutable;
        while (iref.hasNext()) {
            final Array a = Array.factory(ticRef.getElementType(),
                new int[]{1});
            a.setObject(idx, iref.next());
            ref.add(a);
        }
        iref = queryRef.getIndexIterator();
        while (iref.hasNext()) {
            final Array a = Array.factory(queryRef.getElementType(),
                new int[]{1});
            a.setObject(idx, iref.next());
            query.add(a);
        }
        // ref.add(ticRef);
        // query.add(queryRef);
        final Tuple2D<List<Array>, List<Array>> tuple = new Tuple2D<List<Array>, List<Array>>(
            ref, query);
        this.ref_num_scans = ticRef.getShape()[0];
        this.query_num_scans = queryRef.getShape()[0];
        return tuple;
    }
}
