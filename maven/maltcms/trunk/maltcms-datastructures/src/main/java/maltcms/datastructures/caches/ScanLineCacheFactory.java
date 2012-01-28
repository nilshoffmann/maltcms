/*
 * Copyright (C) 2009, 2010 Mathias Wilhelm mwilhelm A T
 * TechFak.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: ScanLineCacheFactory.java 155 2010-08-18 14:43:45Z mwilhelm42 $
 */
package maltcms.datastructures.caches;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import cross.Factory;
import cross.Logging;
import cross.datastructures.fragments.IFileFragment;

/**
 * This Factory creates all {@link IScanLine} caches and hold an reference to
 * support multiple usage.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
public class ScanLineCacheFactory {

	private static final Logger log = Logging
			.getLogger(ScanLineCacheFactory.class);
	private static Map<String, IScanLine> scanlinecaches = null;
	private static boolean useSparseSLC = true;

	static {
		if (ScanLineCacheFactory.scanlinecaches == null) {
			ScanLineCacheFactory.scanlinecaches = new HashMap<String, IScanLine>();
		}
	}

	/**
	 * Creates a new {@link IScanLine} for {@link IFileFragment} and stores it
	 * in a {@link HashMap}.
	 * 
	 * The concrete instance of {@link IScanLine} depends on the configuration
	 * cross.datastructures.fragments.VariableFragment.useCachedList. If its set
	 * to true, the {@link IScanLine} will be the {@link CachedScanLineList},
	 * otherwise the {@link ScanLineCache}
	 * 
	 * @param ff
	 *            file fragment
	 * @return scanline cache for this file fragment
	 */
	private static IScanLine createScanLineCache(final IFileFragment ff) {
		IScanLine slc = null;
		final Configuration cfg = Factory.getInstance().getConfiguration();
		// log.info("Creating CachedScanLineList for {}", ff.getName());
		slc = new ScanLineCache(ff);
                log.info("Using scan line cache implementation: {}",slc.getClass().getName());
		// slc = new CachedScanLineList(ff);
		slc.configure(cfg);
		scanlinecaches.put(ff.getName(), slc);
		return slc;
	}

	/**
	 * This method will automatically create a new {@link IScanLine} for the
	 * given {@link IFileFragment} if no one is cached. Otherwise it will return
	 * the known {@link IScanLine}.
	 * 
	 * @param ff
	 *            file fragment
	 * @return scanline cache for this file fragment
	 */
	public static IScanLine getScanLineCache(final IFileFragment ff) {

		if (ScanLineCacheFactory.scanlinecaches.containsKey(ff.getName())) {
			return ScanLineCacheFactory.scanlinecaches.get(ff.getName());
                        
		} else {
			return ScanLineCacheFactory.createScanLineCache(ff);
		}
	}

	public static IScanLine getSparseScanLineCache(final IFileFragment ff) {
		if (ScanLineCacheFactory.scanlinecaches.containsKey(ff.getName())) {
			return ScanLineCacheFactory.scanlinecaches.get(ff.getName());
		}
		IScanLine slc = null;
		slc = new SparseScanLineCache(ff);
                log.info("Using scan line cache implementation: {}",slc.getClass().getName());
		scanlinecaches.put(ff.getName(), slc);
		return slc;
	}

	public static IScanLine getDefaultScanLineCache(final IFileFragment ff) {
		if (ScanLineCacheFactory.scanlinecaches.containsKey(ff.getName())) {
			return ScanLineCacheFactory.scanlinecaches.get(ff.getName());
		}
		IScanLine slc = null;
		slc = new ScanLineCache(ff);
                log.info("Using scan line cache implementation: {}",slc.getClass().getName());
		scanlinecaches.put(ff.getName(), slc);
		return slc;
	}
        
        public static IScanLine getOldScanLineCache(final IFileFragment ff) {
		if (ScanLineCacheFactory.scanlinecaches.containsKey(ff.getName())) {
			return ScanLineCacheFactory.scanlinecaches.get(ff.getName());
		}
		IScanLine slc = null;
		slc = new ScanLineCacheOld(ff);
                log.info("Using scan line cache implementation: {}",slc.getClass().getName());
		scanlinecaches.put(ff.getName(), slc);
		return slc;
	}
        
        

	public static IScanLine getCachedListScanLineCache(final IFileFragment ff) {
		if (ScanLineCacheFactory.scanlinecaches.containsKey(ff.getName())) {
			return ScanLineCacheFactory.scanlinecaches.get(ff.getName());
		}
		IScanLine slc = null;
		slc = new CachedScanLineList(ff);
                log.info("Using scan line cache implementation: {}",slc.getClass().getName());
		scanlinecaches.put(ff.getName(), slc);
		return slc;
	}

}
