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

import cross.datastructures.fragments.IFileFragment;
import lombok.extern.slf4j.Slf4j;

/**
 * This Factory creates all {@link IScanLine} caches. Each IScanLine instance
 * is created new every time any of the <code>get*</code> methods is invoked.
 *
 * Previously created instances are not cached and returned due to memory leak
 * issues.
 *
 * @author Mathias Wilhelm
 */
@Slf4j
public class ScanLineCacheFactory {

//    private static Map<String, IScanLine> scanlinecaches = null;
//	private static boolean useSparseSLC = true;
	private static double minMass = 0;
	private static double maxMass = 1000;
	private static double massResolution = 1.0;

	public static void setMinMass(double minMass) {
		ScanLineCacheFactory.minMass = minMass;
	}

	public static void setMaxMass(double maxMass) {
		ScanLineCacheFactory.maxMass = maxMass;
	}

	public static void setMassResolution(double massResolution) {
		ScanLineCacheFactory.massResolution = massResolution;
	}

//    static {
//        if (ScanLineCacheFactory.scanlinecaches == null) {
//            ScanLineCacheFactory.scanlinecaches = new HashMap<String, IScanLine>();
//        }
//    }
	/**
	 * Creates a new {@link IScanLine} for {@link IFileFragment} and stores it
	 * in a {@link HashMap}.
	 *
	 * The concrete instance of {@link IScanLine} depends on the configuration
	 * cross.datastructures.fragments.VariableFragment.useCachedList. If its set
	 * to true, the {@link IScanLine} will be the {@link CachedScanLineList},
	 * otherwise the {@link ScanLineCache}
	 *
	 * @param ff file fragment
	 * @return scanline cache for this file fragment
	 */
//	private static IScanLine createScanLineCache(final IFileFragment ff) {
//		IScanLine slc = null;
//		final Configuration cfg = Factory.getInstance().getConfiguration();
//		// log.info("Creating CachedScanLineList for {}", ff.getName());
//		slc = new ScanLineCache(ff, minMass, maxMass, massResolution);
//		log.info("Using scan line cache implementation: {}", slc.getClass().getName());
//		// slc = new CachedScanLineList(ff);
//		slc.configure(cfg);
////        scanlinecaches.put(ff.getName(), slc);
//		return slc;
//	}
	/**
	 * This method will automatically create a new {@link IScanLine} for the
	 * given {@link IFileFragment} if no one is cached. Otherwise it will return
	 * the known {@link IScanLine}.
	 *
	 * @param ff file fragment
	 * @return scanline cache for this file fragment
	 */
	public static IScanLine getScanLineCache(final IFileFragment ff) {

//        if (ScanLineCacheFactory.scanlinecaches.containsKey(ff.getName())) {
//            return ScanLineCacheFactory.scanlinecaches.get(ff.getName());
//
//        } else {
//		return ScanLineCacheFactory.createScanLineCache(ff);
//        }
		return ScanLineCacheFactory.getSparseScanLineCache(ff);
	}

	public static IScanLine getSparseScanLineCache(final IFileFragment ff) {
//        if (ScanLineCacheFactory.scanlinecaches.containsKey(ff.getName())) {
//            return ScanLineCacheFactory.scanlinecaches.get(ff.getName());
//        }
		IScanLine slc = null;
		slc = new SparseScanLineCache(ff, minMass, maxMass, massResolution);
		log.info("Using scan line cache implementation: {}", slc.getClass().getName());
//        scanlinecaches.put(ff.getName(), slc);
		return slc;
	}
//
//	public static IScanLine getDefaultScanLineCache(final IFileFragment ff) {
////        if (ScanLineCacheFactory.scanlinecaches.containsKey(ff.getName())) {
////            return ScanLineCacheFactory.scanlinecaches.get(ff.getName());
////        }
//		IScanLine slc = null;
//		slc = new ScanLineCache(ff, minMass, maxMass, massResolution);
//		log.info("Using scan line cache implementation: {}", slc.getClass().getName());
////        scanlinecaches.put(ff.getName(), slc);
//		return slc;
//	}

//	public static IScanLine getOldScanLineCache(final IFileFragment ff) {
////        if (ScanLineCacheFactory.scanlinecaches.containsKey(ff.getName())) {
////            return ScanLineCacheFactory.scanlinecaches.get(ff.getName());
////        }
//		IScanLine slc = null;
//		slc = new ScanLineCacheOld(ff);
//		log.info("Using scan line cache implementation: {}", slc.getClass().getName());
////        scanlinecaches.put(ff.getName(), slc);
//		return slc;
//	}
//	public static IScanLine getCachedListScanLineCache(final IFileFragment ff) {
////        if (ScanLineCacheFactory.scanlinecaches.containsKey(ff.getName())) {
////            return ScanLineCacheFactory.scanlinecaches.get(ff.getName());
////        }
//		IScanLine slc = null;
//		slc = new CachedScanLineList(ff);
//		log.info("Using scan line cache implementation: {}", slc.getClass().getName());
////        scanlinecaches.put(ff.getName(), slc);
//		return slc;
//	}
}
