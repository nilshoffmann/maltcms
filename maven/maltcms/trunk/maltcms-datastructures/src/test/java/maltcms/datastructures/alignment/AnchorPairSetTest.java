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
package maltcms.datastructures.alignment;

import java.util.ArrayList;

import junit.framework.Assert;
import junit.framework.TestCase;
import maltcms.datastructures.ms.IAnchor;
import maltcms.datastructures.ms.IRetentionInfo;
import maltcms.datastructures.ms.RetentionInfo;
import cross.datastructures.tuple.Tuple2D;

/**
 * Test for {@link maltcms.datastructures.alignment.AnchorPairSet}.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
public class AnchorPairSetTest extends TestCase {

    protected ArrayList<IAnchor> al1 = new ArrayList<IAnchor>();
    protected ArrayList<IAnchor> al2 = new ArrayList<IAnchor>();
    protected ArrayList<Tuple2D<IAnchor, IAnchor>> testl = new ArrayList<Tuple2D<IAnchor, IAnchor>>();
    private int width, height;

    public AnchorPairSetTest(final String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final IRetentionInfo a1 = new RetentionInfo();
        a1.setName("RI1");
        a1.setRetentionIndex(1200.0d);
        a1.setScanIndex(500);
        final IRetentionInfo a2 = new RetentionInfo();
        a2.setName("RI2");
        a2.setRetentionIndex(1600.0d);
        a2.setScanIndex(1000);
        final IRetentionInfo a3 = new RetentionInfo();
        a3.setName("RI3");
        a3.setRetentionIndex(1800.0d);
        a3.setScanIndex(1600);
        final IRetentionInfo a4 = new RetentionInfo();
        a4.setName("RI4");
        a4.setRetentionIndex(2000.0d);
        a4.setScanIndex(2500);

        final IRetentionInfo b1 = new RetentionInfo();
        b1.setName("RI1");
        b1.setRetentionIndex(1200.0d);
        b1.setScanIndex(300);
        final IRetentionInfo b2 = new RetentionInfo();
        b2.setName("RI2");
        b2.setRetentionIndex(1400.0d);
        b2.setScanIndex(850);
        final IRetentionInfo b3 = new RetentionInfo();
        b3.setName("RI3");
        b3.setRetentionIndex(1800.0d);
        b3.setScanIndex(1500);
        final IRetentionInfo b4 = new RetentionInfo();
        b4.setName("RI4");
        b4.setRetentionIndex(2000.0d);
        b4.setScanIndex(1900);
        final IRetentionInfo b5 = new RetentionInfo();
        b5.setName("RI5");
        b5.setRetentionIndex(2400.0d);
        b5.setScanIndex(2200);

        this.testl.add(new Tuple2D<IAnchor, IAnchor>(a1, b1));
        this.testl.add(new Tuple2D<IAnchor, IAnchor>(a3, b3));
        this.testl.add(new Tuple2D<IAnchor, IAnchor>(a4, b4));

        this.al1.add(a1);
        this.al1.add(a2);
        this.al1.add(a3);
        this.al1.add(a4);

        this.al2.add(b1);
        this.al2.add(b2);
        this.al2.add(b3);
        this.al2.add(b4);
        this.al2.add(b5);
        this.width = 3000;
        this.height = 2500;
    }

    public void testAnchorPairSet() {
        Assert.assertNotNull(new AnchorPairSet(this.al1, this.al2, this.width,
                this.height, 5));
    }

    public void testPrepare() {
        final AnchorPairSet aps = new AnchorPairSet(this.al1, this.al2,
                this.width, this.height, 5);
        for (final Tuple2D<IAnchor, IAnchor> t : aps) {
            final IAnchor a1 = t.getFirst();
            final IAnchor a2 = t.getSecond();
            System.out.println(a1 + " " + a2 + " " + a1.compareTo(a2));
            if (a1.compareTo(a2) == 0) {
                Assert.assertTrue(true);
            }
        }
    }
}
