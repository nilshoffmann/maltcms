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
package maltcms.datastructures.caches;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import maltcms.tools.ArrayTools2;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import cross.datastructures.fragments.CachedList;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.Tuple2D;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link IScanLine} with {@link CachedList}.
 *
 * @author Mathias Wilhelm
 */
@Slf4j
public class CachedScanLineList extends AScanLineCache {

    private String intensityValuesVar = "intensity_values";
    private String scanIndexVar = "scan_index";
    private String massValuesVar = "mass_values";
    private boolean useCache = false;
    private boolean prefetch = false;
    private int cacheSize = 1000;
    private final IVariableFragment intensity, mass;
    private List<Array> intensities, masses;

    /**
     * Default constructor.
     *
     * @param iff1 file fragment
     */
    protected CachedScanLineList(final IFileFragment iff1) {
        super(iff1);

        System.out.println("Initing CachedScanLineList");
        intensity = iff1.getChild(this.intensityValuesVar, true);
        intensity.setIndex(iff1.getChild(this.scanIndexVar, true));
        mass = iff1.getChild(this.massValuesVar, true);
        mass.setIndex(iff1.getChild(this.scanIndexVar));
        System.out.println("Done initing CachedScanLineList");
        if (this.useCache && !this.prefetch) {
            this.log.error("!!!	Attention:");
            this.log
                    .error("		Not using the prefetching slows down the caching!");
            this.log
                    .error("		It is recommended to active the prefetching with a cache size of ~4000");
        }

        // try {
        // this.intensities = intensity.getIndexedArray();
        // this.masses = mass.getIndexedArray();
        // } catch (final OutOfMemoryError e) {
        // this.log.error("!!!	Attention:");
        // if (!this.useCache) {
        // this.log
        // .error("		useCachedList is turned off. Either you increase the heap (-mx x[G]) or you turn on the caching (see io.properties)");
        // } else {
        // this.log
        // .error("		useCachedList is turned on. Either you increase the heap (-mx x[G]) or you turn decrease the cache size (see io.properties)");
        // }
        // throw e;
        // }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Array getMassSpectra(final int x, final int y) {
        final Tuple2D<Array, Array> ms = getSparseMassSpectra(x, y);
        return ArrayTools2.normalize(ms.getFirst(), ms.getSecond(),
                getBinsSize(), this.log);
    }

    private Tuple2D<Array, Array> getSparseMassSpectra(final int x,
            final int y, final boolean spread) {
        final int i = this.getScansPerModulation() * x + y;
        if (spread) {
            int nx = x
                    - (int) (((double) this.cacheSize / (double) this
                    .getScansPerModulation()) / 2);
            if (nx < 0) {
                nx = 0;
            }
            if (this.masses == null || this.intensities == null) {
                try {
                    this.intensities = this.intensity.getIndexedArray();
                    this.masses = mass.getIndexedArray();
                } catch (final OutOfMemoryError e) {
                    this.log.error("!!!	Attention:");
                    if (!this.useCache) {
                        this.log
                                .error("		useCachedList is turned off. Either you increase the heap (-mx x[G]) or you turn on the caching (see io.properties)");
                    } else {
                        this.log
                                .error("		useCachedList is turned on. Either you increase the heap (-mx x[G]) or you turn decrease the cache size (see io.properties)");
                    }
                    throw e;
                }
            }
            this.masses.get((this.getScansPerModulation() * nx));
            this.intensities.get((this.getScansPerModulation() * nx));
        }
        return new Tuple2D<Array, Array>(this.masses.get(i), this.intensities
                .get(i));
    }

    @Override
    public Tuple2D<Array, Array> getSparseMassSpectra(int x, int y) {
        return getSparseMassSpectra(x, y, true);
    }

    @Override
    public Tuple2D<Array, Array> getSparseMassSpectra(Point p) {
        return getSparseMassSpectra(p.x, p.y);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Array> getScanlineMS(final int x) {
        final List<Array> sl = new ArrayList<Array>();
        Tuple2D<Array, Array> ms;
        for (int i = 0; i < this.getScansPerModulation(); i++) {
            ms = getSparseMassSpectra(x, i);
            sl.add(ArrayTools2.normalize(ms.getFirst(), ms.getSecond(),
                    getBinsSize(), this.log));
        }
        return sl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Tuple2D<Array, Array>> getScanlineSparseMS(int x) {
        final List<Tuple2D<Array, Array>> sl = new ArrayList<Tuple2D<Array, Array>>();
        for (int i = 0; i < this.getScansPerModulation(); i++) {
            sl.add(getSparseMassSpectra(x, i, false));
        }
        return sl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final Configuration cfg) {
        super.configure(cfg);
        this.massValuesVar = cfg.getString("var.mass_values", "mass_values");
        this.intensityValuesVar = cfg.getString("var.intensity_values",
                "intensity_values");
        this.scanIndexVar = cfg.getString("var.scan_index", "scan_index");

        this.useCache = cfg.getBoolean(FileFragment.class.getName()
                + ".useCachedList", false);
        this.prefetch = cfg.getBoolean(CachedList.class.getName()
                + ".prefetchOnMiss", false);
        this.cacheSize = cfg.getInt(CachedList.class.getName() + ".cacheSize",
                1000);
    }
}