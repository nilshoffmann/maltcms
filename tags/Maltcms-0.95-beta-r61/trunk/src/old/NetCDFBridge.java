/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
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
 * $Id$
 */

package old;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDatasetCache;
import cross.Logging;
import cross.datastructures.fragments.FileFragmentFactory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;

/**
 * @deprecated
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Deprecated
public class NetCDFBridge implements Runnable {

	static String FILELOC1 = "file:///vol/meltdb/data/RT-Drift/ATCC13032_20050131.cdf";

	static String FILELOC2 = "file:///vol/meltdb/data/RT-Drift/ATCC13032_20050523.cdf";

	static long MAX_ELEMENTS = (long) Math.pow(2, 5);

	static Logger log = null;

	public static void main(final String[] args) {
		NetcdfDatasetCache.init();
		final NetCDFBridge ncb = NetCDFBridge.getInstance();
		final ConcurrentHashMap<String, Range> hs = new ConcurrentHashMap<String, Range>();
		final String s1 = "mass_values";
		final String s2 = "intensity_values";
		// hs.put(s2);
		// hs.add("mass_values");
		// hs.add("intensity_values");
		Logging.getInstance().logger.info("List Variables: {}", hs);
		NetcdfDataset nds;
		try {
			nds = NetcdfDatasetCache.acquire(NetCDFBridge.FILELOC1, null);
			final Variable v = nds.findVariable("scan_index");
			// long items = nds.findVariable("intensity_values").getSize();
			if ((v != null) && !v.isScalar()) {
				Array a;
				try {
					a = v.read();
					final IndexIterator it = a.getIndexIterator();
					final long size = a.getSize();
					final int rank = a.getRank();
					Logging.getInstance().logger.info("Size: {} rank: {}",
					        size, rank);
					final long start = 0;
					// size=20;
					int i1 = -1;
					int i2 = -1;
					final ArrayList<Tuple2D<IFileFragment, IFileFragment>> list = new ArrayList<Tuple2D<IFileFragment, IFileFragment>>();
					for (long i = start; i < size; i++) {
						if (i1 == -1) {
							if (it.hasNext()) {
								i1 = it.getIntNext();
							}
						}
						if (it.hasNext()) {
							i2 = it.getIntNext();
						}
						if ((i1 >= 0) && (i2 >= 0)) {
							if (i1 == i2) {
								i = size;
								continue;
							}
							// NetCDFBridge.log.log(Level.INFO,"i1: "+i1);
							// NetCDFBridge.log.log(Level.INFO,"i2: "+i2);
							Range r = null;
							try {
								r = new Range(i1, i2);
								Logging.getInstance().logger.info(
								        "Range: {}:{}", r.first(), r.last());
							} catch (final InvalidRangeException e) {

							}
							String range = "";
							if (r != null) {
								range = "[" + r.first() + ":" + r.last() + "]";
							}
							final IFileFragment di1 = FileFragmentFactory
							        .getInstance().fromString(
							                NetCDFBridge.FILELOC1 + ">" + s1
							                        + range);
							final IFileFragment di2 = FileFragmentFactory
							        .getInstance().fromString(
							                NetCDFBridge.FILELOC1 + ">" + s2
							                        + range);

							list.add(new Tuple2D<IFileFragment, IFileFragment>(
							        di1, di2));
						}

						i1 = i2;
					}
					final PISConsumer<Double> specVis = new SpectrumVisualizer(
					        list);
					ncb.submit(specVis);
					try {
						Thread.sleep(1000);
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		} catch (final IOException e1) {
			e1.printStackTrace();
		}
		NetcdfDatasetCache.exit();
	}

	Thread t = null;

	HashMap<String, PipedOutputStream> attrToPOS = null;

	HashSet<PISConsumer<Double>> finished = null;

	LinkedBlockingQueue<PISConsumer<Double>> cons = null;

	ExecutorService tp = null;

	static NetCDFBridge nb = null;

	public static NetCDFBridge getInstance() {
		if (NetCDFBridge.nb == null) {
			NetCDFBridge.nb = new NetCDFBridge();
		}
		return NetCDFBridge.nb;
	}

	/** Creates a new instance of NetCDFBridge */
	private NetCDFBridge() {
		this.cons = new LinkedBlockingQueue<PISConsumer<Double>>();
		this.finished = new HashSet<PISConsumer<Double>>();
		this.tp = Executors.newFixedThreadPool(20);
		this.tp.execute(this);
	}

	public void printAttributes(final Variable vds) {
		final Iterator<?> iter = vds.getAttributes().iterator();
		while (iter.hasNext()) {
			final Attribute ads = (Attribute) iter.next();
			Logging.getInstance().logger.info("Name: {} DataType: {}", ads
			        .getName(), ads.getDataType().toString());
		}
	}

	public synchronized void readNetcdfDataset(
	        final PISConsumer<Double> consumer, final Logger log1) {

		final Runnable r1 = new Runnable() {

			public void run() {
				final NetcdfArrayPISProvider<Double> provider = new NetcdfArrayPISProvider<Double>();
				provider.provide(consumer);
			}

		};
		NetCDFBridge.this.tp.execute(r1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (true) {
			// log.log(Level.INFO,"NetCDFBridge Running!");
			if (!this.cons.isEmpty()) {
				synchronized (this.cons) {
					// log.log(Level.INFO,"Number of Consumers before:
					// "+this.cons.size());
					while (this.cons.size() > 0) {
						final PISConsumer<Double> con = this.cons.poll();
						readNetcdfDataset(con, NetCDFBridge.log);
					}
					// log.log(Level.INFO,"Number of Consumers after:
					// "+this.cons.size());
				}
			} else {
				try {
					Thread.sleep(2000);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public synchronized void submit(final PISConsumer<Double> consumer) {
		try {
			this.cons.put(consumer);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

}
