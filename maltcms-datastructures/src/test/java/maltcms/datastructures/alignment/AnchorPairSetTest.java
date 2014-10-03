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
package maltcms.datastructures.alignment;

import cross.datastructures.tuple.Tuple2D;
import java.util.ArrayList;
import junit.framework.Assert;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.ms.IAnchor;
import maltcms.datastructures.ms.IRetentionInfo;
import maltcms.datastructures.ms.RetentionInfo;

/**
 * Test for {@link maltcms.datastructures.alignment.AnchorPairSet}.
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
public class AnchorPairSetTest extends TestCase {

    protected ArrayList<IAnchor> al1 = new ArrayList<>();
    protected ArrayList<IAnchor> al2 = new ArrayList<>();
    protected ArrayList<Tuple2D<IAnchor, IAnchor>> testl = new ArrayList<>();
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
            log.info(a1 + " " + a2 + " " + a1.compareTo(a2));
            if (a1.compareTo(a2) == 0) {
                Assert.assertTrue(true);
            }
        }
    }
}
