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
 * $Id: FastBidirectionalBestHit.java 129 2010-06-25 11:57:02Z nilshoffmann $
 */
package maltcms.commands.fragments2d.peakfinding.bbh;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import maltcms.commands.distances.ArrayCos;
import maltcms.commands.distances.IArrayDoubleComp;
import maltcms.datastructures.peak.Peak2D;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import cross.Factory;
import cross.Logging;
import cross.annotations.Configurable;

/**
 * Will compute a list of bidirectional best hits.
 * 
 * FIXME: Es gibt fälle, bei denen ein bbh mit score 0.88 gefunden wird, obwohl
 * es nicht die gleichen peaks sind. Das sind nah beieinander liegende peaks,
 * welche in den anderen chromatogrammen nicht vorkommen. irgendwie muss das
 * fixiert werden
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
public class FastBidirectionalBestHit implements IBidirectionalBestHit {

	private final Logger log = Logging.getLogger(this);

	@Configurable(value = "maltcms.commands.distances.ArrayCos", type = IArrayDoubleComp.class)
	private String distClass = "maltcms.commands.distances.ArrayCos";
	@Configurable(value = "true", type = boolean.class)
	private boolean useMeanMS = true;
	@Configurable(value = "0.9d", type = double.class)
	private Double threshold = 0.9d;
	@Configurable(value = "25.0d", type = double.class)
	private double maxRetDiff = 500.0d;

	private IArrayDoubleComp dist = new ArrayCos();
	private List<Map<Integer, Boolean>> doneList;
	private int counter = 0;
	private int fcounter = 0;

	/**
	 * Default constructor. Sets up all needed variables.
	 */
	public FastBidirectionalBestHit() {
		this.doneList = new ArrayList<Map<Integer, Boolean>>();
	}

	/**
	 * Adds a peak list to a internal peak list.
	 * 
	 * @param peakList
	 *            peak list
	 */
	public void addPeakLists(final List<Peak2D> peakList) {
		this.doneList.add(new HashMap<Integer, Boolean>());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final Configuration cfg) {
		this.threshold = cfg.getDouble(
				this.getClass().getName() + ".threshold", 0.9d);
		this.useMeanMS = cfg.getBoolean(this.getClass().getName()
				+ ".useMeanMS", true);
		this.distClass = cfg.getString(
				this.getClass().getName() + ".distClass",
				"maltcms.commands.distances.ArrayCos");
		this.dist = Factory.getInstance().getObjectFactory().instantiate(
				this.distClass, IArrayDoubleComp.class);
		this.maxRetDiff = cfg.getDouble(this.getClass().getName()
				+ ".maxRetDiff", 500.0d);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double sim(Peak2D p, Peak2D np) {
		double sim, diffrt1, diffrt2;
		diffrt1 = Math.abs(p.getFirstRetTime() - np.getFirstRetTime());
		diffrt2 = Math.abs(p.getSecondRetTime() - np.getSecondRetTime());
		if (this.useMeanMS) {
			sim = this.dist.apply(0, 0, diffrt1, diffrt2, p.getPeakArea()
					.getMeanMS(), np.getPeakArea().getMeanMS());
		} else {
			sim = this.dist.apply(0, 0, diffrt1, diffrt2, p.getPeakArea()
					.getSeedMS(), np.getPeakArea().getSeedMS());
		}
		return sim;
	}

	/**
	 * Will find the best hit in list of peak p.
	 * 
	 * @param p
	 *            peak
	 * @param list
	 *            list
	 * @return <code>-1</code> if no one was found
	 */
	private int findBidiBestHist(final Peak2D p, final List<Peak2D> list) {
		int maxI = -1;
		double max;
		if (this.dist.minimize()) {
			max = Double.MAX_VALUE;
		} else {
			max = Double.MIN_VALUE;
		}
		double sim;
		Peak2D np;
                double diff;
		for (int i = 0; i < list.size(); i++) {
                    np = list.get(i);
                    //                               200  -> 1000 - 200  =  800
                    // p = 1000, maxdiff = 500, np = 500  -> 1000 - 500  =  500
                    // p = 1000, maxdiff = 500, np = 1500 -> 1000 - 1500 = -500
                    //                               1800 -> 1000 - 1800 = -800
                    diff = p.getFirstRetTime() - np.getFirstRetTime();
                    if (Math.abs(diff) < this.maxRetDiff) {
                            this.counter++;
                            sim = sim(p, np);
                            if (this.dist.minimize()) {
                                    if (sim < max) {
                                            maxI = i;
                                            max = sim;
                                    }
                            } else {
                                    if (sim > max) {
                                            maxI = i;
                                            max = sim;
                                    }
                            }
                    } else {
                        if (diff < -this.maxRetDiff) {
                            this.fcounter += list.size()-i;
                            return maxI;
                        }
                        this.fcounter++;
                    }
		}
		if (this.threshold != 0) {
			if (this.dist.minimize()) {
				if (max > this.threshold) {
					return -1;
				}
			} else {
				if (max < this.threshold) {
					return -1;
				}
			}
		}
		return maxI;
	}

	/**
	 * Getter.
	 * 
	 * @return a list of all bidirectional best hits. List contains the indices
	 *         of peak in the peaklist.
	 */
	public List<List<Point>> getBidiBestHitList(
			final List<List<Peak2D>> peaklists) {
            System.out.println(this.maxRetDiff);
		this.log.info("Dist: {}", this.dist.getClass().getName());
		this.log.info("Threshold: {}", this.threshold);
		this.log.info("Use mean MS: {}", this.useMeanMS);

		this.doneList = new ArrayList<Map<Integer, Boolean>>(peaklists.size());
		for (int i = 0; i < peaklists.size(); i++) {
			this.doneList.add(new HashMap<Integer, Boolean>(peaklists.get(i).size()));
		}

		this.log.info("peaklistsize {}:", peaklists.size());
		for (List<Peak2D> l : peaklists) {
			this.log.info("	{}", l.size());
		}

		final List<List<Point>> indexList = new ArrayList<List<Point>>();
		List<Point> bidibestlist = new ArrayList<Point>();
		int ii;
		// Runtime runtime = Runtime.getRuntime();
		int r, l;
		// int c = 0;
		int bidibestr, bidibestl;
		for (int h = 0; h < peaklists.size() - 1; h++) {
			for (int i = 0; i < peaklists.get(h).size(); i++) {
				// this.log.info("free memory is: {}", runtime.freeMemory());

				if (!this.doneList.get(h).containsKey(i)) {
					r = h + 1;
					l = h;
					for (int j = 0; j < h; j++) {
						bidibestlist.add(new Point(-1, j));
					}
					bidibestlist.add(new Point(i, l));
					ii = i;
					while (true) {
						bidibestr = findBidiBestHist(peaklists.get(l).get(ii),
								peaklists.get(r));
						if (bidibestr != -1 && !this.doneList.get(r).containsKey(bidibestr)) {
							bidibestl = findBidiBestHist(peaklists.get(r).get(
									bidibestr), peaklists.get(l));
							if (bidibestl == ii) {
                                                                //TODO check whether group is still a consistent bidibest hit group
                                                                boolean consistent = true;
                                                                for (Point tp : bidibestlist) {
                                                                    int tmpm;
                                                                    // what if peak bidibestr in chromatogram r has a bidibest hit in chromatogram tp.y, but one of the first ones does not have it?
                                                                    if (tp.x != -1 && tp.y != l) {
                                                                        //check tp.x peaklist against bidibestr in r
                                                                        tmpm = findBidiBestHist(peaklists.get(r).get(bidibestr), peaklists.get(tp.y));
                                                                        if (tmpm != tp.x) {
                                                                            consistent = false;
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                                if (consistent) {
                                                                    bidibestlist.add(new Point(bidibestr, r));
                                                                    this.doneList.get(l).put(ii, true);
                                                                    this.doneList.get(r).put(bidibestr, true);
                                                                    l = r;
                                                                    r++;
                                                                    ii = bidibestr;
                                                                } else {
//                                                                    System.out.println("would result in an inconsistent group! will not add peak to group!");
                                                                    bidibestlist.add(new Point(-1, r));
                                                                    r++;
                                                                }
							} else {
                                                            // is not a bidibest hit
								bidibestlist.add(new Point(-1, r));
								r++;
							}
						} else {
                                                        // does not have a hit, or hit is already in a peak clique
							bidibestlist.add(new Point(-1, r));
							r++;
						}
						if (r == peaklists.size()) {
							break;
						}
					}
					indexList.add(bidibestlist);
					bidibestlist = new ArrayList<Point>();
				}
			}
		}

		for (Map<Integer, Boolean> li : this.doneList) {
			System.out.println("Donelistsize: " + li.keySet().size());
		}

		final int lastIndex = peaklists.size() - 1;
		for (int i = 0; i < peaklists.get(lastIndex).size(); i++) {
			if (!this.doneList.get(lastIndex).containsKey(i)) {
				bidibestlist = new ArrayList<Point>();
				for (int j = 0; j < lastIndex; j++) {
					bidibestlist.add(new Point(-1, j));
				}
				bidibestlist.add(new Point(i, lastIndex));
				indexList.add(bidibestlist);
			}
		}

		this.log.info("Did: {}", this.counter);
		this.log.info("Skipped: {}", this.fcounter);

		return indexList;
	}

	public void clear() {
		this.dist = Factory.getInstance().getObjectFactory().instantiate(
				this.distClass, IArrayDoubleComp.class);
		// this.peaklists = new ArrayList<List<Peak2D>>();
		this.doneList = new ArrayList<Map<Integer, Boolean>>();
		this.counter = 0;
		this.fcounter = 0;
	}

}
