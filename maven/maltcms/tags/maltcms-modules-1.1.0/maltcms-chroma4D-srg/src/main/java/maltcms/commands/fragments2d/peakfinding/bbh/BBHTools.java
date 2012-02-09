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
package maltcms.commands.fragments2d.peakfinding.bbh;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import maltcms.commands.fragments2d.peakfinding.output.IPeakExporter;
import maltcms.datastructures.peak.Peak2D;
import maltcms.datastructures.peak.Peak2DClique;
import cross.datastructures.fragments.IFileFragment;

public class BBHTools {

    public static List<Peak2DClique> getPeak2DCliqueList(
            Collection<IFileFragment> f, List<List<Point>> bidiBestHitList,
            List<List<Peak2D>> peaklist) {
        final List<Peak2DClique> peakCliques = new ArrayList<Peak2DClique>();

        boolean complete = false;
        // for (List<Point> lp : bidiBestHitList) {
        for (int i = 0; i < bidiBestHitList.size(); i++) {
            complete = true;
            List<Peak2D> peaks = new ArrayList<Peak2D>();
            for (Point p : bidiBestHitList.get(i)) {
                if (p.x == -1) {
                    complete = false;
                } else {
                    peaks.add(peaklist.get(p.y).get(p.x));
                }
            }
            if (complete) {
                peakCliques.add(new Peak2DClique("" + i, f, peaks));
            }
        }

        return peakCliques;
    }

    /**
     * Creates a filtered BBH List.
     * 
     * This method will change the y component of the Point of the BBH list.
     * This is needed to assure the correctness of the export by
     * {@link IPeakExporter} (method exportDetailedBBH)
     * 
     * @param i
     *            ith chromatogram
     * @param j
     *            jth chromatogram
     * @param bidiBestHits
     *            full BBH list
     * @return BBH list containing only the peaks of the ith and jth
     *         chromatogram
     */
    public static List<List<Point>> getBidiBestList(final int i, final int j,
            final List<List<Point>> bidiBestHits) {
        final List<List<Point>> index = new ArrayList<List<Point>>();
        int c = 0;
        for (List<Point> list : bidiBestHits) {
            index.add(new ArrayList<Point>());
            for (int k = 0; k < list.size(); k++) {
                if (k == i) {
                    index.get(c).add(new Point(list.get(k).x, 0));
                } else if (k == j) {
                    index.get(c).add(new Point(list.get(k).x, 1));
                }
            }
            c++;
        }
        return index;
    }

    /**
     * This Method will call all methods of {@link IPeakExporter}. Furthermore
     * it creates all pairwise BBH information.
     * 
     * @param bidiBestHitList
     *            bidirection best hit list
     * @param peaklist
     *            peaklist
     * @param bbh
     *            bidirectional best hits class
     * @param peakExporter
     *            peak exporter
     * @param chromatogramNames
     *            names of the chromatograms
     */
    public static void exportBBHInformation(List<List<Point>> bidiBestHitList,
            List<List<Peak2D>> peaklist, IBidirectionalBestHit bbh,
            IPeakExporter peakExporter, List<String> chromatogramNames) {

        // this.log.info("Exporting whole detailed BBH information");
        peakExporter.exportDetailedBBHInformation(bidiBestHitList, peaklist,
                bbh, chromatogramNames, null, "detailedBBHInformation.csv");
        // this.log.info("Exporting peak occurence map");
        peakExporter.exportPeakOccurrenceMap(bidiBestHitList, peaklist, bbh,
                chromatogramNames, "peakOccurenceMap.csv");

        // Workaround to export pairwise detailed bbh information
        List<String> filenamest;
        List<List<Point>> bidiBestHitListt;
        List<List<Peak2D>> peaklists;
        // List<Reliability> relis;
        // List<Reliability> reliTmp;
        // QualityControl qc = new QualityControl();
        for (int i = 0; i < peaklist.size(); i++) {
            for (int j = i + 1; j < peaklist.size(); j++) {
                // this.log
                // .info(
                // "Exporting pairwise detailed BBH information for {}, {}",
                // chromatogramNames.get(i), chromatogramNames
                // .get(j));
                peaklists = new ArrayList<List<Peak2D>>();
                peaklists.add(peaklist.get(i));
                peaklists.add(peaklist.get(j));
                filenamest = new ArrayList<String>();
                filenamest.add(chromatogramNames.get(i));
                filenamest.add(chromatogramNames.get(j));

                bidiBestHitListt = getBidiBestList(i, j, bidiBestHitList);

                // qc.calc(bbh.getPeakLists().get(i), bbh.getPeakLists()
                // .get(j), bidiBestHitListt);

                peakExporter.exportDetailedBBHInformation(bidiBestHitListt,
                        peaklists, bbh, filenamest, null, "pwBBH_"
                        + chromatogramNames.get(i) + "-"
                        + chromatogramNames.get(j) + ".csv");
            }
        }

        // peakExporter.exportBBHInformation(bidiBestHitList, bbh,
        // chromatogramNames, relis);
        peakExporter.exportBBHInformation(bidiBestHitList, peaklist, bbh,
                chromatogramNames, null);
        peakExporter.exportBBHMultipleAlignmentRT(bidiBestHitList, peaklist, bbh,
                chromatogramNames, null);

        // return bidiBestHitList;
    }

    public static List<MissingPeak2D> getMissingPeaks(
            List<List<Point>> bidiBestHitList, List<List<Peak2D>> peaklist,
            int scansPerModulation) {
        final List<MissingPeak2D> missing = new ArrayList<MissingPeak2D>();
        final int chromatogramCount = bidiBestHitList.get(0).size();
        final int peakCount = bidiBestHitList.size();
        int meanFirstScanIndex, meanSecondScanIndex, maxFirstDelta, maxSecondDelta;
        int dx, dy;
        int c = 0;
        Point p1, p2;
        Peak2D pe1, pe2;
        MissingPeak2D missingPeak;

        List<Integer> dxl = new ArrayList<Integer>(), dyl = new ArrayList<Integer>();
        for (int i = 0; i < peakCount; i++) {
            meanFirstScanIndex = 0;
            meanSecondScanIndex = 0;
            maxFirstDelta = 0;
            maxSecondDelta = 0;
            c = 0;
            missingPeak = new MissingPeak2D();
            for (int j1 = 0; j1 < chromatogramCount; j1++) {
                p1 = bidiBestHitList.get(i).get(j1);
                if (p1.x != -1) {
                    pe1 = peaklist.get(p1.y).get(p1.x);
                    meanFirstScanIndex += pe1.getPeakArea().getSeedPoint().x;
                    meanSecondScanIndex += pe1.getPeakArea().getSeedPoint().y;
                    missingPeak.addMS(pe1.getPeakArea().getSeedMS());
                    c++;
                    for (int j2 = j1 + 1; j2 < chromatogramCount; j2++) {
                        p2 = bidiBestHitList.get(i).get(j2);
                        if (p2.x != -1) {
                            pe2 = peaklist.get(p2.y).get(p2.x);
                            dx = Math.abs(pe1.getPeakArea().getSeedPoint().x
                                    - pe2.getPeakArea().getSeedPoint().x);
                            dxl.add(dx);
                            maxFirstDelta = Math.max(maxFirstDelta, dx);
                            dy = Math.abs(pe1.getPeakArea().getSeedPoint().y
                                    - pe2.getPeakArea().getSeedPoint().y);
                            dyl.add(dy);
                            maxSecondDelta = Math.max(maxSecondDelta, dy);
                        }
                    }
                } else {
                    missingPeak.addMissingChromatogram(j1);
                }
            }
            if (missingPeak.getMissingChromatogramList().size() > 0) {
                meanFirstScanIndex /= c;
                meanSecondScanIndex /= c;
                missingPeak.setMeanFirstScanIndex(meanFirstScanIndex);
                missingPeak.setMeanSecondScanIndex(meanSecondScanIndex);
                missingPeak.setMaxFirstDelta(
                        (int) ((double) maxFirstDelta * 1.25)); //adding 25% as additional offset
                missingPeak.setMaxSecondDelta(
                        (int) ((double) maxSecondDelta * 1.25)); // adding 25% as additional offset
                missingPeak.setAverageCount(c);
                missing.add(missingPeak);
            }
        }

        return missing;
    }
}
