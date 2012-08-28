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
package maltcms.experimental.bipace.peakCliqueAlignment;

import cross.Factory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.peak.Peak;
import maltcms.experimental.bipace.datastructures.api.Clique;
import maltcms.io.csv.CSVWriter;
import org.jdom.Element;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Slf4j
@Data
public class CenterFinder<T extends Peak> implements IWorkflowElement {

    private IWorkflow workflow;

    public int getNumberOfPeaksWithinCliques(IFileFragment iff, List<Clique<T>> l) {
        int npeaks = 0;
        for (Clique<T> c : l) {
            for (T p : c.getPeakList()) {
                if (p.getAssociation().equals(iff.getName())) {
                    npeaks++;
                }
            }
        }
        return npeaks;
    }

    @Deprecated
    private List<Clique<T>> getCommonCliques(IFileFragment a, IFileFragment b,
            List<Clique<T>> l) {
        List<Clique<T>> commonCliques = new ArrayList<Clique<T>>();
        log.debug("Retrieving common cliques");
        for (Clique<T> c : l) {
            for (Peak p : c.getPeakList()) {
                if (p.getAssociation().equals(a.getName())
                        || p.getAssociation().equals(b.getName())) {
                    commonCliques.add(c);
                }
            }
        }
        return commonCliques;
    }

    public double getCommonScore(IFileFragment a, IFileFragment b,
            List<Clique<T>> commonCliques) {
        double score = 0;
        for (Clique<T> c : commonCliques) {
            double v = 0;
            v = c.getSimilarityForPeaks(a, b);
            if (Double.isNaN(v) || Double.isInfinite(v)) {
                v = 0;
            }
            score += v;
//            for (Peak p : c.getPeakList()) {
//                for (Peak q : c.getPeakList()) {
//                    if (p.getAssociation().getName().equals(a.getName())
//                            && q.getAssociation().getName().equals(b.getName())) {
//                        double v = p.getSimilarity(q);//getSimilarity(p, q);
//                        log.debug("Similarity of {} and {} = {}", new Object[]{
//                                    p, q, v});
//                        score += v;
//                    }
//                }
//            }
        }
        return score;
    }

    /**
     * @param newFragments
     * @param cliques
     */
    public void findCenter(final TupleND<IFileFragment> newFragments,
            final List<Clique<T>> cliques) {
        // cliqueNumbers -> number of cliques per FileFragment
        // FileFragments with highest number of cliques are favorites
        double[] cliqueNumbers = new double[newFragments.size()];
        double[] cliqueSize = new double[newFragments.size()];
        HashMap<String, Integer> placeMap = new HashMap<String, Integer>();
        int cnt = 0;
        // fill placeMap with unique id aka slot in cliqueNumbers for each
        // FileFragment
        for (IFileFragment f : newFragments) {
            placeMap.put(f.getName(), cnt++);
        }
        // for all peaks, increment number of cliques for FileFragment
        // associated to each peak
        int npeaks = 0;
        for (Clique<T> c : cliques) {
            for (Peak p : c.getPeakList()) {
                cliqueNumbers[placeMap.get(p.getAssociation())]++;
                cliqueSize[placeMap.get(p.getAssociation())] += c.
                        getPeakList().size();
                npeaks++;
            }

        }

        // which FileFragment is the best reference for alignment?
        // the one, which participates in the highest number of cliques
        // or the one, which has the overall highest average similarity to all
        // others
        double[] fragScores = new double[newFragments.size()];
        // double sumFragScores = 0;
        ArrayDouble.D2 fragmentScores = new ArrayDouble.D2(newFragments.size(),
                newFragments.size());
        CliqueTable<T> ct = new CliqueTable<T>(newFragments, cliques);
        log.info("Calculating fragment scores");
        ArrayChar.D2 fragmentNames = new ArrayChar.D2(newFragments.size(), 1024);
        for (int i = 0; i < newFragments.size(); i++) {
            fragmentNames.setString(i, newFragments.get(i).getName());
            for (int j = 0; j < newFragments.size(); j++) {
//                    List<Clique> commonCliques = getCommonCliques(
//                            newFragments.get(i), newFragments.get(j), cliques);
                List<Clique<T>> commonCliques = ct.getCommonCliques(
                        newFragments.get(i), newFragments.get(j), cliques);
                if (!commonCliques.isEmpty()) {
                    fragmentScores.set(
                            i,
                            j,
                            getCommonScore(newFragments.get(i),
                            newFragments.get(j), commonCliques)
                            / ((double) commonCliques.size()));
                    // FIXME this should not be necessary, in some cases,
                    // similarities are
                    // removed further upstream
                    fragScores[i] += fragmentScores.get(i, j);
                } else {
                    log.debug("Common cliques list is empty!");
                }

            }
        }
        saveToCSV(fragmentScores, fragmentNames);
        // log number of cliques
        cnt = 0;
        for (IFileFragment iff : newFragments) {
            log.info("FileFragment " + iff.getName() + " is member of "
                    + cliqueNumbers[cnt] + " cliques with average cliqueSize: "
                    + cliqueSize[cnt] / cliqueNumbers[cnt]);
            cnt++;
        }

        // // normalize scores by number of cliques for each FileFragment
        // for (IFileFragment f : newFragments) {
        // fragScores[placeMap.get(f.getName())] /= cliqueNumbers[placeMap
        // .get(f.getName())];
        // }

        // normalize scores by total score
        // for (IFileFragment f : newFragments) {
        // fragScores[placeMap.get(f.getName())] /= sumFragScores;
        // }
        boolean minimize = false;//costFunction.minimize();
        for (int j = 0; j < newFragments.size(); j++) {
            log.info("File: {}, value: {}", newFragments.get(j).getName(),
                    (minimize ? -fragScores[j] : fragScores[j]));
        }

        int optIndex = 0;

        double optVal = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < fragScores.length; i++) {
//            if (minimize) {
//                if (fragScores[i] < optVal) {
//                    optVal = Math.min(optVal, -fragScores[i]);
//                    optIndex = i;
//                }
//            } else {
            if (fragScores[i] > optVal) {
                optVal = Math.max(optVal, fragScores[i]);
                optIndex = i;
            }
//            }
        }

        final CSVWriter csvw = Factory.getInstance().getObjectFactory().
                instantiate(CSVWriter.class);
        csvw.setWorkflow(getWorkflow());
        List<List<String>> tble = new ArrayList<List<String>>();
        tble.add(Arrays.asList(newFragments.get(optIndex).getName()));
        csvw.writeTableByRows(getWorkflow().getOutputDirectory(this).
                getAbsolutePath(), "center-star.csv", tble,
                WorkflowSlot.CLUSTERING);

        log.info("{} with value {} is the center star!",
                newFragments.get(optIndex).getName(),
                (minimize ? -optVal : optVal));
    }

    public void saveToCSV(final ArrayDouble.D2 distances,
            final ArrayChar.D2 names) {
        final CSVWriter csvw = Factory.getInstance().getObjectFactory().
                instantiate(CSVWriter.class);
        csvw.setWorkflow(getWorkflow());
        csvw.writeArray2DwithLabels(getWorkflow().getOutputDirectory(this).
                getAbsolutePath(), "pairwise_distances.csv", distances, names,
                this.getClass(), WorkflowSlot.STATISTICS, getWorkflow().
                getStartupDate());
    }

    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.CLUSTERING;
    }

    @Override
    public void appendXML(Element e) {
    }
}
