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
package maltcms.commands.distances.dtw;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import cross.annotations.Configurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import lombok.Data;
import maltcms.commands.distances.PairwiseFeatureSequenceSimilarity;
import org.openide.util.lookup.ServiceProvider;

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
