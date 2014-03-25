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
package net.sf.maltcms.evaluation.api.classification;

import cross.datastructures.combinations.ChoiceFactory;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Nils Hoffmann
 */
public class ChoiceFactoryTest {

    public List<Object[]> createData() {
        Object[] a = new String[]{"a", "b"};
        Object[] b = new Double[]{0.35, 976.213, 8612.2};
        Object[] c = new Integer[]{1, 2, 3};
        return Arrays.asList(a, b, c);
    }

    /**
     * Test of getKPartiteChoices method, of class Combinatorics.
     */
    @Test
    public void testGetKPartiteChoices() {
        List<Object[]> data = ChoiceFactory.getKPartiteChoices(createData());
        for (Object[] obj : data) {
            System.out.println(Arrays.toString(obj));
        }
        List<Object[]> reference = new LinkedList<>();
        reference.add(new Object[]{"a", 0.35, 1});
        reference.add(new Object[]{"a", 0.35, 2});
        reference.add(new Object[]{"a", 0.35, 3});
        reference.add(new Object[]{"a", 976.213, 1});
        reference.add(new Object[]{"a", 976.213, 2});
        reference.add(new Object[]{"a", 976.213, 3});
        reference.add(new Object[]{"a", 8612.2, 1});
        reference.add(new Object[]{"a", 8612.2, 2});
        reference.add(new Object[]{"a", 8612.2, 3});
        reference.add(new Object[]{"b", 0.35, 1});
        reference.add(new Object[]{"b", 0.35, 2});
        reference.add(new Object[]{"b", 0.35, 3});
        reference.add(new Object[]{"b", 976.213, 1});
        reference.add(new Object[]{"b", 976.213, 2});
        reference.add(new Object[]{"b", 976.213, 3});
        reference.add(new Object[]{"b", 8612.2, 1});
        reference.add(new Object[]{"b", 8612.2, 2});
        reference.add(new Object[]{"b", 8612.2, 3});

        Assert.assertEquals(reference.size(), data.size());
        for (int i = 0; i < data.size(); i++) {
            Object[] a = reference.get(i);
            Object[] b = data.get(i);
            Assert.assertEquals(a.length, b.length);
            Assert.assertEquals(a[0], b[0]);
            Assert.assertEquals((Double) a[1], (Double) b[1], 0.0d);
            Assert.assertEquals(a[2], b[2]);
        }
    }
}
