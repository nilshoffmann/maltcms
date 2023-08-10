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
package maltcms.datastructures.caches;

import cross.datastructures.fragments.IFileFragment;
import org.slf4j.LoggerFactory;


/**
 * This Factory creates all {@link maltcms.datastructures.caches.IScanLine} caches. Each IScanLine instance is
 * created new every time any of the <code>get*</code> methods is invoked.
 *
 * Previously created instances are not cached and returned due to memory leak
 * issues.
 *
 * @author Mathias Wilhelm
 * 
 */

public class ScanLineCacheFactory {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ScanLineCacheFactory.class);

//    private static Map<String, IScanLine> scanlinecaches = null;
//	private static boolean useSparseSLC = true;
    private static double minMass = 0;
    private static double maxMass = 1000;
    private static double massResolution = 1.0;

    /**
     * <p>Setter for the field <code>minMass</code>.</p>
     *
     * @param minMass a double.
     * @since 1.3.2
     */
    public static void setMinMass(double minMass) {
        ScanLineCacheFactory.minMass = minMass;
    }

    /**
     * <p>Setter for the field <code>maxMass</code>.</p>
     *
     * @param maxMass a double.
     * @since 1.3.2
     */
    public static void setMaxMass(double maxMass) {
        ScanLineCacheFactory.maxMass = maxMass;
    }

    /**
     * <p>Setter for the field <code>massResolution</code>.</p>
     *
     * @param massResolution a double.
     * @since 1.3.2
     */
    public static void setMassResolution(double massResolution) {
        ScanLineCacheFactory.massResolution = massResolution;
    }
    /**
     * This method will automatically create a new {@link maltcms.datastructures.caches.IScanLine} for the
     * given {@link cross.datastructures.fragments.IFileFragment} if no one is cached. Otherwise it will return
     * the known {@link maltcms.datastructures.caches.IScanLine}.
     *
     * @param ff file fragment
     * @return scanline cache for this file fragment
     */
    public static IScanLine getScanLineCache(final IFileFragment ff) {
        return ScanLineCacheFactory.getSparseScanLineCache(ff);
    }

    /**
     * <p>getSparseScanLineCache.</p>
     *
     * @param ff a {@link cross.datastructures.fragments.IFileFragment} object.
     * @return a {@link maltcms.datastructures.caches.IScanLine} object.
     */
    public static IScanLine getSparseScanLineCache(final IFileFragment ff) {
        IScanLine slc = null;
        slc = new SparseScanLineCache(ff, minMass, maxMass, massResolution);
        log.info("Using scan line cache implementation: {}", slc.getClass().getName());
        return slc;
    }
}
