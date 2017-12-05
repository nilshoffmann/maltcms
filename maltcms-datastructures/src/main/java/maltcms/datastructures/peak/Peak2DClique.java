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
package maltcms.datastructures.peak;

import cross.datastructures.fragments.IFileFragment;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>Peak2DClique class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class Peak2DClique {

    private Map<IFileFragment, Peak2D> peaks;
    private Map<String, Double> ratios;
    private String id;

    /**
     * <p>Constructor for Peak2DClique.</p>
     *
     * @param id a {@link java.lang.String} object.
     */
    public Peak2DClique(String id) {
        this.id = id;
        this.peaks = new HashMap<>();
        this.ratios = new HashMap<>();
    }

    /**
     * <p>Constructor for Peak2DClique.</p>
     *
     * @param id a {@link java.lang.String} object.
     * @param f a {@link java.util.Collection} object.
     * @param peaks a {@link java.util.List} object.
     */
    public Peak2DClique(String id, Collection<IFileFragment> f,
            List<Peak2D> peaks) {
        this(id);
        Iterator<IFileFragment> i1 = f.iterator();
        Iterator<Peak2D> i2 = peaks.iterator();
        while (i1.hasNext() && i2.hasNext()) {
            this.peaks.put(i1.next(), i2.next());
        }
    }

    /**
     * <p>add.</p>
     *
     * @param ff a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param peak a {@link maltcms.datastructures.peak.Peak2D} object.
     */
    public void add(IFileFragment ff, Peak2D peak) {
        this.peaks.put(ff, peak);
    }

    /**
     * <p>get.</p>
     *
     * @param ff a {@link cross.datastructures.fragments.IFileFragment} object.
     * @return a {@link maltcms.datastructures.peak.Peak2D} object.
     */
    public Peak2D get(IFileFragment ff) {
        return peaks.get(ff);
    }

    /**
     * <p>getAll.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Peak2D> getAll() {
        return new ArrayList<>(peaks.values());
        // final List<Peak2D> ret = new ArrayList<Peak2D>();
        // for (IFileFragment ff : f) {
        // ret.add(peaks.get(ff));
        // }
        // return ret;
    }

    /**
     * <p>addRatio.</p>
     *
     * @param class1 a {@link java.lang.String} object.
     * @param class2 a {@link java.lang.String} object.
     * @param ratio a {@link java.lang.Double} object.
     */
    public void addRatio(String class1, String class2, Double ratio) {
        this.ratios.put(class1 + "-" + class2, ratio);
    }

    /**
     * <p>getRatio.</p>
     *
     * @param class1 a {@link java.lang.String} object.
     * @param class2 a {@link java.lang.String} object.
     * @return a double.
     */
    public double getRatio(String class1, String class2) {
        if (this.ratios.containsKey(class1 + "-" + class2)) {
            return this.ratios.get(class1 + "-" + class2);
        }
        return Double.NEGATIVE_INFINITY;
    }

    /**
     * <p>getID.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getID() {
        return this.id;
    }
}
