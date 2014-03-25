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
package maltcms.commands.fragments.alignment;

import com.carrotsearch.hppc.LongObjectMap;
import com.carrotsearch.hppc.LongObjectOpenHashMap;
import cross.Factory;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import cross.tools.StringTools;
import java.io.File;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.BBHResult;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.Clique;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.CliqueFinder;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.CliqueTable;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.IBipacePeak;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.IWorkerFactory;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.OneWayPeakAnova;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.PairwiseSimilarityResult;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.PeakComparator;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.PeakEdge;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.PeakListWriter;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.WorkerFactory;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.XmlAlignmentWriter;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.peakFactory.IPeakFactory;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.peakFactory.IPeakFactoryImpl;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.peakFactory.Peak1DMSFactory;
import maltcms.datastructures.caches.ScanLineCacheFactory;
import maltcms.io.csv.CSVWriter;
import maltcms.tools.ArrayTools;
import maltcms.tools.MaltcmsTools;
import maltcms.ui.charts.PlotRunner;
import net.sf.mpaxs.api.ICompletionService;
import org.apache.commons.configuration.Configuration;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;

/**
 * For every peak in each chromatogram, its bi-directional best hits are
 * determined, and all bi-directional best hits are merged. If they apply, they
 * cover a set of k peaks in k chromatograms.
 *
 * @author Nils Hoffmann
 *
 *
 */
@RequiresVariables(names = {
    "var.scan_acquisition_time", "var.mass_values", "var.intensity_values",
    "var.scan_index"})
@RequiresOptionalVariables(names = {"var.binned_mass_values",
    "var.binned_intensity_values", "var.binned_scan_index", "var.tic_peaks", "var.eic_peaks",
    "var.first_column_elution_time", "var.second_column_elution_time", "var.peak_area", "var.peak_index_list"})
@ProvidesVariables(names = {"var.anchors.retention_index_names",
    "var.anchors.retention_times", "var.anchors.retention_indices",
    "var.anchors.retention_scans"})
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class PeakCliqueAlignment extends AFragmentCommand {

//    private IScalarArraySimilarity similarityFunction;
    @Configurable(name = "var.tic_peaks")
    private String ticPeaks = "tic_peaks";
    @Configurable(name = "var.eic_peaks")
    private String eicPeaks = "eic_peaks";
    @Configurable(name = "var.peak_index_list")
    private String peakIndexList = "peak_index_list";
    @Configurable(name = "var.mass_values")
    private String massValues = "mass_values";
    @Configurable(name = "var.scan_index")
    private String scanIndex = "scan_index";
    @Configurable(name = "var.intensity_values")
    private String intensityValues = "intensity_values";
    @Configurable(name = "var.retention_index_names")
    private String anchorNames = "retention_index_names";
    @Configurable(name = "var.retention_times")
    private String anchorTimes = "retention_times";
    @Configurable(name = "var.retention_indices")
    private String anchorRetentionIndex = "retention_indices";
    @Configurable(name = "var.retention_scans")
    private String anchorScanIndex = "retention_scans";
    @Configurable(name = "var.binned_intensity_values")
    private String binnedIntensities = "binned_intensity_values";
    @Configurable(name = "var.binned_scan_index")
    private String binnedScanIndex = "binned_scan_index";
    @Configurable(name = "var.scan_acquisition_time")
    private String scanAcquisitionTime = "scan_acquisition_time";
    @Configurable(name = "var.peak_area")
    private String peakAreaVariable = "peak_area";
    @Configurable
    private boolean useUserSuppliedAnchors = false;
    @Configurable
    private int minCliqueSize = -1;
    @Configurable
    private boolean savePeakSimilarities = false;
    @Configurable
    private boolean savePeakMatchRTTable = true;
    @Configurable
    private boolean savePeakMatchAreaTable = true;
    @Configurable
    private boolean savePeakMatchAreaPercentTable = true;
    @Configurable
    private boolean saveXMLAlignment = true;
    @Deprecated
    @Configurable
    private int maxBBHErrors = 0;
    @Configurable
    private double minBbhFraction = 1.0d;
    @Configurable
    private boolean savePlots = false;
    @Configurable
    private boolean saveUnmatchedPeaks = false;
    @Configurable
    private boolean saveIncompatiblePeaks = false;
    @Configurable
    private boolean saveUnassignedPeaks = false;
    @Configurable
    private boolean useSparseArrays = false;
    @Configurable
    private boolean use2DRetentionTimes = false;
    @Configurable
    private IWorkerFactory workerFactory = new WorkerFactory();
    @Configurable
    private IPeakFactory peakFactory = new Peak1DMSFactory();
    @Configurable
    private double rtNormalizationFactor = 1 / 60.0d;
    @Configurable
    private String rtOutputFormat = "0.000";
    @Deprecated
    @Configurable
    private boolean postProcessCliques = false;

    @Override
    public String toString() {
        return getClass().getName();
    }

    /**
     *
     * @param al
     * @param newFragments
     * @param cliques
     * @return
     */
    private TupleND<IFileFragment> addAnchors(HashMap<IBipacePeak, Clique<IBipacePeak>> peakToClique, final List<List<IBipacePeak>> al,
            final TupleND<IFileFragment> newFragments,
            final List<Clique<IBipacePeak>> cliques,
            final Map<String, Integer> nameToIdMap,
            final LongObjectMap<PeakEdge> peakEdgeMap) {

        final String ri_names = this.anchorNames;
        final String ri_times = this.anchorTimes;
        final String ri_indices = this.anchorRetentionIndex;
        final String ri_scans = this.anchorScanIndex;

        findCenter(newFragments, cliques, nameToIdMap, peakEdgeMap);

        // Create Variables and arrays
        final HashMap<String, IFileFragment> hm = new HashMap<>();
        log.info("Preparing files!");
        for (final IFileFragment ff : newFragments) {
            log.debug("Source files of fragment {}: {}",
                    ff.getUri(), ff.getSourceFiles());
            hm.put(ff.getName(), ff);
            final int size = getNumberOfPeaksWithinCliques(ff, cliques);
            if (size > 0) {
                final ArrayInt.D1 anchors = new ArrayInt.D1(size);
                ArrayTools.fillArray(anchors, Integer.valueOf(-1));
                final IVariableFragment ri = ff.hasChild(ri_scans) ? ff.getChild(
                        ri_scans) : new VariableFragment(ff, ri_scans);
                final ArrayDouble.D1 anchorTimes = new ArrayDouble.D1(size);
                ArrayTools.fillArray(anchorTimes, Double.valueOf(-1));
                final IVariableFragment riTimes = ff.hasChild(ri_times) ? ff.getChild(ri_times) : new VariableFragment(ff, ri_times);
                final ArrayChar.D2 names = cross.datastructures.tools.ArrayTools.createStringArray(size, 256);
                final IVariableFragment riNames = ff.hasChild(ri_names) ? ff.getChild(ri_names) : new VariableFragment(ff, ri_names);
                final ArrayDouble.D1 rindexA = new ArrayDouble.D1(size);
                final IVariableFragment rindex = ff.hasChild(ri_indices) ? ff.getChild(ri_indices)
                        : new VariableFragment(ff, ri_indices);
                ri.setArray(anchors);
                riNames.setArray(names);
                riTimes.setArray(anchorTimes);
                rindex.setArray(rindexA);
            }
        }

        // Set Anchors
        int id = 0;
        int[] nextIndex = new int[newFragments.size()];
        HashMap<String, Integer> placeMap = new HashMap<>();
        int cnt = 0;
        // fill placeMap with unique id aka slot in cliqueNumbers for each
        // FileFragment
        for (IFileFragment f : newFragments) {
            placeMap.put(f.getName(), cnt++);
        }

        log.info("Setting anchors!");
        for (final Clique<IBipacePeak> c : cliques) {
            // Matched IPeaks
            for (final IBipacePeak p : c.getPeakList()) {
                final IFileFragment association = hm.get(p.getAssociation());
                int slot = placeMap.get(association.getName());
                if (peakToClique.containsKey(p)) {
                    id = nextIndex[slot];
                    long cid = peakToClique.get(p).getID();
                    log.debug(
                            "Adding anchor at scan index {} and rt {} with id {} to file {}",
                            new Object[]{p.getScanIndex(),
                                p.getScanAcquisitionTime(), cid,
                                association.getName()});
                    String name = "A" + cid;
                    if (!(p.getName().equals(""))) {
                        name = name + "_" + p.getName();

                    }
                    // log.info("Adding anchor with name {}", name);
                    ((ArrayChar.D2) association.getChild(ri_names).getArray()).setString(id, name);
                    ((ArrayInt.D1) association.getChild(ri_scans).getArray()).set(id, p.getScanIndex());
                    ((ArrayDouble.D1) association.getChild(ri_times).getArray()).set(id, p.getScanAcquisitionTime());
                    nextIndex[slot]++;
                }

            }
            id++;
        }

        // cleanup
        for (final IFileFragment ff : newFragments) {
            // make sure, that work array data is removed
            if (ff.hasChild(this.ticPeaks)) {
                ff.removeChild(ff.getChild(this.ticPeaks));
            }
            if (ff.hasChild(this.eicPeaks)) {
                ff.removeChild(ff.getChild(this.eicPeaks));
            }
            if (ff.hasChild(this.peakIndexList)) {
                ff.removeChild(ff.getChild(this.peakIndexList));
            }
            if (ff.hasChild(this.binnedIntensities)) {
                ff.removeChild(ff.getChild(this.binnedIntensities));
            }
            if (ff.hasChild(this.binnedScanIndex)) {
                ff.removeChild(ff.getChild(this.binnedScanIndex));
            }
            if (ff.hasChild(this.scanIndex)) {
                ff.removeChild(ff.getChild(this.scanIndex));
            }
            if (ff.hasChild(this.massValues)) {
                ff.removeChild(ff.getChild(this.massValues));
            }
            if (ff.hasChild(this.intensityValues)) {
                ff.removeChild(ff.getChild(this.intensityValues));
            }
            ff.getChild(this.scanAcquisitionTime).getArray();
            final DefaultWorkflowResult dwr = new DefaultWorkflowResult(
                    ff.getUri(), this, getWorkflowSlot(), ff);
            getWorkflow().append(dwr);
            ff.save();
        }
        return newFragments;
    }

    private int getNumberOfPeaksWithinCliques(IFileFragment iff, List<Clique<IBipacePeak>> l) {
        int npeaks = 0;
        for (Clique<IBipacePeak> c : l) {
            for (IBipacePeak p : c.getPeakList()) {
                if (p.getAssociation().equals(iff.getName())) {
                    npeaks++;
                }
            }
        }
        return npeaks;
    }

    private double getCommonScore(IFileFragment a, IFileFragment b,
            List<Clique<IBipacePeak>> commonCliques, Map<String, Integer> nameToIdMap, LongObjectMap<PeakEdge> peakEdgeMap) {
        double score = 0;
        for (Clique<IBipacePeak> c : commonCliques) {
            double v = 0;
            v = c.getSimilarityForPeaks(nameToIdMap.get(a.getName()), nameToIdMap.get(b.getName()), peakEdgeMap);
            if (Double.isNaN(v) || Double.isInfinite(v)) {
                v = 0;
            }
            score += v;
        }
        return score;
    }

    /**
     * @param newFragments
     * @param cliques
     */
    private void findCenter(final TupleND<IFileFragment> newFragments,
            final List<Clique<IBipacePeak>> cliques, final Map<String, Integer> nameToIdMap, final LongObjectMap<PeakEdge> peakEdgeMap) {
        // cliqueNumbers -> number of cliques per FileFragment
        // FileFragments with highest number of cliques are favorites
        double[] cliqueNumbers = new double[newFragments.size()];
        double[] cliqueSize = new double[newFragments.size()];
        HashMap<String, Integer> placeMap = new HashMap<>();
        int cnt = 0;
        // fill placeMap with unique id aka slot in cliqueNumbers for each
        // FileFragment
        for (IFileFragment f : newFragments) {
            placeMap.put(f.getName(), cnt++);
        }
        // for all peaks, increment number of cliques for FileFragment
        // associated to each peak
        int npeaks = 0;
        for (Clique<IBipacePeak> c : cliques) {
            for (IBipacePeak p : c.getPeakList()) {
                cliqueNumbers[placeMap.get(p.getAssociation())]++;
                cliqueSize[placeMap.get(p.getAssociation())] += c.getPeakList().size();
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
        CliqueTable ct = new CliqueTable(newFragments, cliques);
        log.info("Calculating fragment scores");
        ArrayChar.D2 fragmentNames = new ArrayChar.D2(newFragments.size(), 1024);
        for (int i = 0; i < newFragments.size(); i++) {
            fragmentNames.setString(i, newFragments.get(i).getName());
            for (int j = 0; j < newFragments.size(); j++) {
                List<Clique<IBipacePeak>> commonCliques = ct.getCommonCliques(
                        newFragments.get(i), newFragments.get(j), cliques);
                if (!commonCliques.isEmpty()) {
                    fragmentScores.set(
                            i,
                            j,
                            getCommonScore(newFragments.get(i),
                                    newFragments.get(j), commonCliques, nameToIdMap, peakEdgeMap)
                            / ((double) commonCliques.size()));
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

        boolean minimize = false;//costFunction.minimize();
        for (int j = 0; j < newFragments.size(); j++) {
            log.info("File: {}, value: {}", newFragments.get(j).getName(),
                    (minimize ? -fragScores[j] : fragScores[j]));
        }

        int optIndex = 0;

        double optVal = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < fragScores.length; i++) {
            if (fragScores[i] > optVal) {
                optVal = Math.max(optVal, fragScores[i]);
                optIndex = i;
            }
//            }
        }

        final CSVWriter csvw = Factory.getInstance().getObjectFactory().
                instantiate(CSVWriter.class);
        csvw.setWorkflow(getWorkflow());
        List<List<String>> tble = new ArrayList<>();
        tble.add(Arrays.asList(newFragments.get(optIndex).getName()));
        csvw.writeTableByRows(getWorkflow().getOutputDirectory(this).
                getAbsolutePath(), "center-star.csv", tble,
                WorkflowSlot.CLUSTERING);

        log.info("{} with value {} is the center!",
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

    /*
     * (non-Javadoc)
     *
     * @see cross.commands.ICommand#apply(java.lang.Object)
     */
    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        for (final IFileFragment iff : t) {
            log.debug("{}", iff);
        }
        Tuple2D<Double, Double> massRange = MaltcmsTools.getMinMaxMassRange(t);
        ScanLineCacheFactory.setMinMass(massRange.getFirst());
        ScanLineCacheFactory.setMaxMass(massRange.getSecond());
        if (t.size() < 2) {
            log.warn("At least two files are required for peak clique alignment!");
        } else {
            List<IFileFragment> l = new ArrayList<>(t);
            Collections.sort(l, new Comparator<IFileFragment>() {
                @Override
                public int compare(IFileFragment o1, IFileFragment o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            final TupleND<IFileFragment> tret = matchPeaks(new TupleND<>(l));
            for (final IFileFragment iff : tret) {
                log.debug("{}", iff);
            }
            return tret;
        }
        return t;
    }

    /**
     * @param al the list of FileFragments
     * @param nameToFragment String name to FileFragment map
     * @param fragmentToPeaks the map of FileFragment names to IPeak lists
     * @param n the number of peaks
     * @param peakEdgeMap the map in which to store global peak edges
     * @return the number of unmatched peaks
     */
    private int calculatePeakSimilarities(final TupleND<IFileFragment> al, Map<String, IFileFragment> nameToFragment,
            final Map<String, List<IBipacePeak>> fragmentToPeaks, final int n, final LongObjectMap<PeakEdge> peakEdgeMap) {
        log.info(
                "Calculating {} pairwise peak similarities for {} peaks!",
                ((long) n * (long) n), n);
//        log.info("Using {} as pairwise peak similarity!",
//                this.similarityFunction.getClass().getName());
        // Loop over all pairs of FileFragments
        ICompletionService<PairwiseSimilarityResult> ics = createCompletionService(PairwiseSimilarityResult.class);
        File outputDirectory = new File(getWorkflow().getOutputDirectory(this), "PeakSimilarityVisualizer");
        outputDirectory.mkdirs();
        workerFactory.setSavePeakSimilarities(savePeakSimilarities);
        List<Callable<PairwiseSimilarityResult>> workers = workerFactory.create(outputDirectory, al, fragmentToPeaks);
        log.info("Running {} pairwise similarity tasks!", workers.size());
        for (Callable<PairwiseSimilarityResult> worker : workers) {
            ics.submit(worker);
        }
        final Set<IBipacePeak> unmatchedPeaks = new LinkedHashSet<>();
        try {
            for (IFileFragment f : al) {
                unmatchedPeaks.addAll(fragmentToPeaks.get(f.getName()));
            }
            List<PairwiseSimilarityResult> bbhPeaksList = ics.call();
            ListIterator<PairwiseSimilarityResult> resultIterator = bbhPeaksList.listIterator();
            while (resultIterator.hasNext()) {
                PairwiseSimilarityResult upl = resultIterator.next();
                resultIterator.remove();
                for (IBipacePeak p : upl.getBbhPeakedgeSet()) {
                    unmatchedPeaks.remove(p);
                }
                long[] edgeKeys = upl.getPeakEdgeKeys();
                PeakEdge[] edges = upl.getPeakEdgeValues();
                for (int i = 0; i < edgeKeys.length; i++) {
                    peakEdgeMap.put(edgeKeys[i], edges[i]);
                }
            }
//            for (PairwiseSimilarityResult upl : bbhPeaksList) {
//                for (IBipacePeak p : upl.getBbhPeakedgeSet()) {
//                    unmatchedPeaks.remove(p);
//                }
//                long[] edgeKeys = upl.getPeakEdgeKeys();
//                PeakEdge[] edges = upl.getPeakEdgeValues();
//                for (int i = 0; i < edgeKeys.length; i++) {
//                    peakEdgeMap.put(edgeKeys[i], edges[i]);
//                }
//            }
        } catch (Exception ex) {
            log.error("Caught exception while executing workers: ", ex);
            throw new RuntimeException(ex);
        }
        log.info("Found {}/{} unmatched peaks!", unmatchedPeaks.size(), n);
        log.info("Continuing with {} matched peaks!", (n - unmatchedPeaks.size()));
        //add unmatched peaks file to workflow results
        if (saveUnmatchedPeaks) {
            PeakListWriter writer = new PeakListWriter();
            File output = writer.savePeakList(getWorkflow().getOutputDirectory(this), nameToFragment, unmatchedPeaks, "unmatchedPeaks.msp", "UNMATCHED");
            getWorkflow().append(new DefaultWorkflowResult(output, this, WorkflowSlot.FILEIO, nameToFragment.values().toArray(new IFileFragment[nameToFragment.size()])));
        }
        return unmatchedPeaks.size();
    }

    /**
     * FIXME add support for 1D only chromatograms -> see TICDynamicTimeWarp for
     * that
     *
     * @param al
     * @return
     */
    private HashMap<String, List<IBipacePeak>> checkUserSuppliedAnchors(
            final TupleND<IFileFragment> al, final Map<String, Integer> fragmentToId, final double massBinResolution, final Tuple2D<Double, Double> minMaxMassRange, final int size) {
        // Check for already defined peaks
        final HashMap<String, List<IBipacePeak>> definedAnchors = new HashMap<>();
        for (final IFileFragment t : al) {
            List<IBipacePeak> anchors = checkUserSuppliedAnchors(t, fragmentToId.get(t.getName()), massBinResolution, minMaxMassRange, size);
            if (!anchors.isEmpty()) {
                log.info("Using {} user-supplied anchors for file {}!", anchors.size(), t.getName());
                definedAnchors.put(t.getName(), anchors);
            }
        }
        return definedAnchors;
    }

    public List<IBipacePeak> checkUserSuppliedAnchors(final IFileFragment t, final int associationId, final double massBinResolution, final Tuple2D<Double, Double> minMaxMassRange, final int size) {
        IVariableFragment anames = null;
        IVariableFragment ascans = null;
        List<IBipacePeak> peaks = new ArrayList<>();
        IPeakFactoryImpl peakFactoryImpl = peakFactory.createInstance(t, minMaxMassRange, size, massBinResolution, useSparseArrays, associationId);
        try {
            anames = t.getChild(this.anchorNames);
            ascans = t.getChild(this.anchorScanIndex);
            final ArrayChar.D2 peakNames = (ArrayChar.D2) anames.getArray();
            final Array peakScans = ascans.getArray();
            final Index peakScansI = peakScans.getIndex();

//			IVariableFragment biv = t.getChild(this.binnedIntensities);
//			IVariableFragment bsi = t.getChild(this.binnedScanIndex);
//			biv.setIndex(bsi);
            log.info("Checking user supplied anchors for: {}", t);
            final Array scan_acquisition_time = t.getChild(
                    this.scanAcquisitionTime).getArray();
            final Index sat1 = scan_acquisition_time.getIndex();
//			final List<Array> bintens = biv.getIndexedArray();
            for (int i = 0; i < peakScans.getShape()[0]; i++) {
                final String name = peakNames.getString(i);
                final int scan = peakScans.getInt(peakScansI.set(i));
//				final double sat = scan_acquisition_time.getDouble(sat1.set(
//						scan));
                log.debug("{}", t.getName());
                final IBipacePeak p = peakFactoryImpl.create(scan, scan);
//				final IBipacePeak p = new PeakNG(scan, bintens.get(scan),
//						sat, t.getName(), associationId, this.savePeakSimilarities);
                p.setName(name);
                log.debug(
                        "Adding user supplied anchor {} with name {}", p,
                        p.getName());
                peaks.add(p);
            }
            return peaks;
        } catch (final ResourceNotAvailableException rne) {
            log.debug("Could not find any user-defined anchors!");
            return Collections.emptyList();
        }
    }

    @Override
    public void configure(final Configuration cfg) {
        super.configure(cfg);

        this.scanAcquisitionTime = cfg.getString(
                "var.scan_acquisition_time", "scan_acquisition_time");
        this.binnedIntensities = cfg.getString(
                "var.binned_intensity_values", "binned_intensity_values");
        this.binnedScanIndex = cfg.getString(
                "var.binned_scan_index", "binned_scan_index");
        this.anchorNames = cfg.getString(
                "var.anchors.retention_index_names", "retention_index_names");
        this.anchorTimes = cfg.getString(
                "var.anchors.retention_times", "retention_times");
        this.anchorRetentionIndex = cfg.getString(
                "var.anchors.retention_indices", "retention_indices");
        this.anchorScanIndex = cfg.getString(
                "var.anchors.retention_scans", "retention_scans");
        this.massValues = cfg.getString("var.mass_values",
                "mass_values");
        this.scanIndex = cfg.getString("var.scan_index",
                "scan_index");
        this.intensityValues = cfg.getString(
                "var.intensity_values", "intensity_values");
        this.ticPeaks = cfg.getString("var.tic_peaks", "tic_peaks");
        this.eicPeaks = cfg.getString("var.eic_peaks", "eic_peaks");
        this.peakIndexList = cfg.getString("var.peak_index_list", "peak_index_list");
        //this.peakAreaVariable = cfg.getString("var.peak_area", "peak_area");
    }

    /**
     *
     * @param al
     * @param fragmentToPeaks
     */
    public void saveSimilarityMatrix(final TupleND<IFileFragment> al,
            final HashMap<String, List<IBipacePeak>> fragmentToPeaks, final LongObjectOpenHashMap<PeakEdge> edgeMap) {
        for (final IFileFragment iff1 : al) {
            for (final IFileFragment iff2 : al) {
                final List<IBipacePeak> lhsPeaks = fragmentToPeaks.get(iff1.getName());
                final List<IBipacePeak> rhsPeaks = fragmentToPeaks.get(iff2.getName());
                double score = 0;
                int bidihits = 0;
                log.debug("lhsPeaks: {}", lhsPeaks.size());
                log.debug("rhsPeaks: {}", rhsPeaks.size());
                for (final IBipacePeak plhs : lhsPeaks) {
                    for (final IBipacePeak prhs : rhsPeaks) {
                        double d = plhs.getSimilarity(edgeMap, prhs);
                        if (d != Double.NEGATIVE_INFINITY
                                && d != Double.POSITIVE_INFINITY) {
                            score += d;
                            bidihits++;
                        }
                    }
                }
                score /= ((double) (bidihits));
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.commands.fragments.AFragmentCommand#getDescription()
     */
    /**
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "Assigns peak candidates as pairs and groups them into cliques of size k";
    }

//    public double getSimilarity(final Peak a, final Peak b) {
//        final double v = this.similarityFunction.apply(new double[]{
//                    a.getScanAcquisitionTime()}, new double[]{b.getScanAcquisitionTime()},
//                a.getMSIntensities(), b.getMSIntensities());
//        return v;
//    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    /**
     *
     * @return
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.PEAKMATCHING;
    }

    private TupleND<IFileFragment> matchPeaks(
            final TupleND<IFileFragment> originalFragments) {
        log.debug("Matching peaks");
        final Map<String, IFileFragment> nameToFragment = new ConcurrentHashMap<>();
        final Map<String, Integer> nameToIndex = new ConcurrentHashMap<>();
        final Map<String, List<IBipacePeak>> fragmentToPeaks = new ConcurrentHashMap<>();
        final ArrayList<IFileFragment> al2 = new ArrayList<>();
        int i = 0;
        for (final IFileFragment iff : originalFragments) {
            final IFileFragment iff2 = new FileFragment(new File(getWorkflow().
                    getOutputDirectory(this),
                    iff.getName()));
            iff2.addSourceFile(iff);
            nameToFragment.put(iff2.getName(), iff2);
            log.debug("Created work file {}", iff2);
            al2.add(iff2);
            nameToIndex.put(iff2.getName(), i++);
        }
        final TupleND<IFileFragment> t = new TupleND<>(al2);
        initializePeaks(originalFragments, fragmentToPeaks, nameToIndex);
        log.debug("Calculating all-against-all peak similarities");
        int n = 0;
        for (String key : fragmentToPeaks.keySet()) {
            n += fragmentToPeaks.get(key).size();
        }
        log.info("Searching for bidirectional best hits");
        final long startT = System.currentTimeMillis();
        final LongObjectMap<PeakEdge> peakEdgeMap = new LongObjectOpenHashMap<>();
        final int unmatchedPeaks = calculatePeakSimilarities(t, nameToFragment, fragmentToPeaks, n, peakEdgeMap);
        log.info("Found bidi best hits in {} milliseconds",
                System.currentTimeMillis() - startT);

        final long startT2 = System.currentTimeMillis();
        final List<List<IBipacePeak>> cliqueList = new ArrayList<>();

        BBHResult result;
        if (this.minCliqueSize == -1 || this.minCliqueSize == t.size()) {
            log.info("Combining bidirectional best hits if present in all files");
            CliqueFinder cliqueFinder = new CliqueFinder(saveIncompatiblePeaks, saveUnassignedPeaks, minBbhFraction, this);
            result = cliqueFinder.combineBiDiBestHits(t, nameToFragment, nameToIndex, fragmentToPeaks, t.size(), n - unmatchedPeaks, peakEdgeMap);
        } else {
            if (this.minCliqueSize > t.size()) {
                log.info("Resetting minimum group size to: {}, was: {}",
                        t.size(), this.minCliqueSize);
                this.minCliqueSize = t.size();
            }
            log.info("Combining bidirectional best hits, minimum group size: {}",
                    this.minCliqueSize);
            CliqueFinder cliqueFinder = new CliqueFinder(saveIncompatiblePeaks, saveUnassignedPeaks, minBbhFraction, this);
            result = cliqueFinder.combineBiDiBestHits(t, nameToFragment, nameToIndex, fragmentToPeaks,
                    this.minCliqueSize, n - unmatchedPeaks, peakEdgeMap);
        }
        if (savePlots) {
            String groupFileLocation = Factory.getInstance().getConfiguration().
                    getString("groupFileLocation", "");
            OneWayPeakAnova owa = new OneWayPeakAnova();
            owa.setWorkflow(getWorkflow());
            owa.calcFisherRatios(result.getCliques(), t, groupFileLocation);
            saveCliquePlots(result.getCliques(), t);
        }
        // add all remaining cliques to cliqueList
        log.info("Minimum clique size: {}", minCliqueSize);
        final List<Clique<IBipacePeak>> cliques = result.getCliques();
        ListIterator<Clique<IBipacePeak>> li = cliques.listIterator();
        Set<IBipacePeak> peakCliqueSet = new HashSet<>();
        int requiredMinimumCliqueSize = minCliqueSize;
        if (requiredMinimumCliqueSize == -1) {
            requiredMinimumCliqueSize = originalFragments.getSize();
        }
        int peaksInCliques = 0;
        while (li.hasNext()) {
            Clique<IBipacePeak> c = li.next();
            peaksInCliques += c.getPeakList().size();
            peakCliqueSet.addAll(c.getPeakList());
            try {
                log.debug("Clique {}", c);
            } catch (NullPointerException npe) {
                log.debug("Clique empty?: {}", c.getPeakList());
            }
            if (c.getPeakList().size() >= requiredMinimumCliqueSize) {
                cliqueList.add(c.getPeakList());
            } else {
                li.remove();
            }
            //clear ms array data for gc
            for (IBipacePeak peak : c.getPeakList()) {
                peak.setMsIntensities(null);
            }
        }

        log.info("Found {} cliques covering {} peaks of {} total peaks in {} milliseconds", cliques.size(), peaksInCliques, n,
                System.currentTimeMillis() - startT2);
        log.info("Percentage of peaks covered in cliques: {}%", 100.0f * (peaksInCliques / (float) n));
        log.info("Saving peak match index table!");
        savePeakMatchTable(nameToIndex, cliqueList);
        if (savePeakMatchRTTable) {
            log.info("Saving peak match rt table!");
            savePeakMatchRTTable(nameToIndex, cliqueList);
        }
        if (savePeakMatchAreaTable) {
            log.info("Saving peak match area table!");
            savePeakMatchAreaTable(nameToIndex, cliqueList, nameToFragment);
        }
        if (savePeakMatchAreaPercentTable) {
            log.info("Saving peak match area percent table!");
            savePeakMatchAreaPercentTable(nameToIndex, cliqueList, nameToFragment);
        }

        saveToXMLAlignment(t, cliqueList);
        log.info("Adding anchor variables");
        final TupleND<IFileFragment> ret = cliques.isEmpty() ? originalFragments
                : addAnchors(result.getPeakToClique(), cliqueList, t, cliques, nameToIndex, peakEdgeMap);
        for (IFileFragment iff : originalFragments) {
            iff.clearArrays();
        }
        return ret;
    }

    /**
     * Initialize peaks / anchors provided from previous commands.
     *
     * @param originalFileFragments
     * @param fragmentToPeaks
     * @param nameToIndex
     */
    private void initializePeaks(
            final TupleND<IFileFragment> originalFileFragments,
            final Map<String, List<IBipacePeak>> fragmentToPeaks,
            final Map<String, Integer> nameToIndex) {
//		int column = 0;
        // int maxPeaks = Integer.MIN_VALUE;
        int npeaks = 0;
        HashMap<String, List<IBipacePeak>> definedAnchors = new HashMap<>();
        final double massBinResolution = Factory.getInstance().getConfiguration().getDouble("dense_arrays.massBinResolution",
                1.0d);
        Tuple2D<Double, Double> minMaxMassRange = MaltcmsTools.getMinMaxMassRange(originalFileFragments);
        final int size = MaltcmsTools.getNumberOfIntegerMassBins(minMaxMassRange.getFirst(),
                minMaxMassRange.getSecond(), massBinResolution);
        if (this.useUserSuppliedAnchors) {
            log.debug("Checking for user-supplied anchors!");
            definedAnchors = checkUserSuppliedAnchors(originalFileFragments, nameToIndex, massBinResolution, minMaxMassRange, size);
//			if(!definedAnchors.isEmpty()) {
//				for (final IFileFragment iff : originalFileFragments) {
//					definedAnchors.put(iff.getName(), new ArrayList<IBipacePeak>(0));
//				}
//			}
        }
        log.debug("{}", definedAnchors.toString());
        if (useSparseArrays) {
            log.info("Using sparse arrays!");
        }
        List<IFileFragment> fragments = new ArrayList<>(originalFileFragments);
        Collections.sort(fragments, new Comparator<IFileFragment>() {
            @Override
            public int compare(IFileFragment o1, IFileFragment o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        // Insert Peaks into HashMap
        for (final IFileFragment t : fragments) {
            // if we have a valid variable defined,
            // use those peak indices
            Array peakCandidates1;
            try {
                IVariableFragment peakCandidates = t.getChild(this.eicPeaks);
                peakCandidates1 = peakCandidates.getArray();
                log.debug("EIC Peaks for file {}: {}", t.getUri(),
                        peakCandidates1);
            } catch (ResourceNotAvailableException rnae) {
                try {
                    IVariableFragment peakCandidates = t.getChild(this.ticPeaks);
                    peakCandidates1 = peakCandidates.getArray();
                    log.debug("TIC Peaks for file {}: {}", t.getUri(),
                            peakCandidates1);
                } catch (ResourceNotAvailableException rnae2) {
                    try {
                        IVariableFragment peakCandidates = t.getChild(this.peakIndexList);
                        peakCandidates1 = peakCandidates.getArray();
                        log.debug("TIC2D Peaks for file {}: {}", t.getUri(),
                                peakCandidates1);
                    } catch (ResourceNotAvailableException rnae3) {
                        // otherwise, create an index array for all scans!!!
                        Array sidx = t.getChild(this.binnedScanIndex).getArray();
                        peakCandidates1 = ArrayTools.indexArray(sidx.getShape()[0], 0);
                    }
                }
            }

            EvalTools.notNull(peakCandidates1, this);
            List<IBipacePeak> peaks = null;
            Set<Integer> peakIndices = new LinkedHashSet<>();
            if (fragmentToPeaks.containsKey(t.getName())) {
                peaks = fragmentToPeaks.get(t.getName());
            } else {
                peaks = new ArrayList<>();
            }
            log.debug("Adding peaks for {}", t.getName());
            int fragmentIndex = nameToIndex.get(t.getName());
            IPeakFactoryImpl pfi = peakFactory.createInstance(t, minMaxMassRange, size, massBinResolution, useSparseArrays, fragmentIndex);

            for (int i = 0; i < peakCandidates1.getShape()[0]; i++) {
                final int pc1i = peakCandidates1.getInt(i);
                if (peakIndices.contains(pc1i)) {
                    log.debug("Skipping already present peak at scan index {}!", pc1i);
                } else {
                    IBipacePeak p = null;
                    p = pfi.create(i, pc1i);
                    peaks.add(p);
                    npeaks++;
                }
            }

            final List<IBipacePeak> userDefinedAnchors = definedAnchors.get(t.getName());
            if ((userDefinedAnchors != null) && !userDefinedAnchors.isEmpty() && this.useUserSuppliedAnchors) {
                log.info("Using user-defined anchors for {}", t.getName());
                for (final IBipacePeak p : userDefinedAnchors) {

                    final int n = Collections.binarySearch(peaks, p,
                            new PeakComparator());
                    if (n >= 0) {// if found in list, remove and add to anchors
                        final IBipacePeak q = peaks.get(n);
                        q.setName(p.getName());
                        log.debug("{} with name {} annotated by user!", p,
                                p.getName());
                    } else {// else add at proposed insert position
                        log.debug("Adding peak at position {}", n);
                        peaks.add(((-1) * n) - 1, p);
                    }
                }
            }

            fragmentToPeaks.put(t.getName(), peaks);
//			columnMap.put(t.getName(), column++);
            // clearing space
            t.clearArrays();
        }
        log.debug("{} peaks present", npeaks);
    }

    /**
     * @param columnMap
     * @param cliqueList
     */
    private void savePeakMatchAreaTable(final Map<String, Integer> columnMap,
            final List<List<IBipacePeak>> ll, final Map<String, IFileFragment> nameToFragment) {
        final List<List<String>> rows = new ArrayList<>(ll.size());
        List<String> headers = null;
        final String[] headerLine = new String[columnMap.size()];
        for (int i = 0; i < headerLine.length; i++) {
            headerLine[i] = "";
        }
        headers = Arrays.asList(headerLine);
        for (final String s : columnMap.keySet()) {
            headers.set(columnMap.get(s), StringTools.removeFileExt(s));
        }
        log.debug("Adding row {}", headers);
        rows.add(headers);
        int rowCounter = 0;
        for (final List<IBipacePeak> l : ll) {
            log.debug("Adding row {}", rowCounter);
            final String[] line = new String[columnMap.size()];
            for (int i = 0; i < line.length; i++) {
                line[i] = "-";
            }
            log.debug("Adding {} peaks", l.size());
            for (final IBipacePeak p : l) {
                final String iff = p.getAssociation();
                log.debug("Adding peak {} from {}", p, iff);
                EvalTools.notNull(iff, this);
                IFileFragment fragment = nameToFragment.get(iff);
                EvalTools.notNull(fragment, this);
                final int pos = columnMap.get(iff);
                try {
                    Array peakAreas = fragment.getChild(peakAreaVariable).getArray();
                    log.debug("PeakAreas for {}: {}", fragment.getName(), peakAreas.getShape()[0]);
                    log.debug("Insert position for {}: {}", iff, pos);
                    if (pos >= 0) {
                        if (line[pos].equals("-")) {
                            if (p.getPeakIndex() != -1) {
                                line[pos] = peakAreas.getDouble(p.getPeakIndex()) + "";
                            } else {
                                line[pos] = "NaN";
                            }
                        } else {
                            log.warn("Array position {} already used!", pos);
                        }
                    }
                } catch (ResourceNotAvailableException rnae) {
                    log.debug("Could not find {} as starting from {}!", peakAreaVariable, fragment.getName());
                }
            }
            final List<String> v = Arrays.asList(line);
            rows.add(v);
            log.debug("Adding row {}", v);
            rowCounter++;
        }

        final CSVWriter csvw = new CSVWriter();
        csvw.setWorkflow(getWorkflow());
        csvw.writeTableByRows(getWorkflow().getOutputDirectory(this).
                getAbsolutePath(), "multiple-alignmentArea.csv", rows,
                WorkflowSlot.ALIGNMENT);
    }

    /**
     * @param columnMap
     * @param cliqueList
     */
    private void savePeakMatchAreaPercentTable(final Map<String, Integer> columnMap,
            final List<List<IBipacePeak>> ll, final Map<String, IFileFragment> nameToFragment) {
        final List<List<String>> rows = new ArrayList<>(ll.size());
        List<String> headers = null;
        final String[] headerLine = new String[columnMap.size()];
        for (int i = 0; i < headerLine.length; i++) {
            headerLine[i] = "";
        }
        headers = Arrays.asList(headerLine);
        for (final String s : columnMap.keySet()) {
            headers.set(columnMap.get(s), StringTools.removeFileExt(s));
        }
        log.debug("Adding row {}", headers);
        rows.add(headers);
        int rowCounter = 0;
        for (final List<IBipacePeak> l : ll) {
            log.debug("Adding row {}", rowCounter);
            final String[] line = new String[columnMap.size()];
            for (int i = 0; i < line.length; i++) {
                line[i] = "-";
            }
            log.debug("Adding {} peaks", l.size());
            for (final IBipacePeak p : l) {
                final String iff = p.getAssociation();
                log.debug("Adding peak {} from {}", p, iff);
                EvalTools.notNull(iff, this);
                IFileFragment fragment = nameToFragment.get(iff);
                EvalTools.notNull(fragment, this);
                final int pos = columnMap.get(iff);
                try {
                    Array peakAreas = fragment.getChild(peakAreaVariable).getArray();
                    log.debug("PeakAreas for {}: {}", fragment.getName(), peakAreas.getShape()[0]);
                    log.debug("Insert position for {}: {}", iff, pos);
                    if (pos >= 0) {
                        if (line[pos].equals("-")) {
                            if (p.getPeakIndex() != -1) {
                                line[pos] = peakAreas.getDouble(p.getPeakIndex()) * 100.0 + "";
                            } else {
                                line[pos] = "NaN";
                            }
                        } else {
                            log.warn("Array position {} already used!", pos);
                        }
                    }
                } catch (ResourceNotAvailableException rnae) {
                    log.debug("Could not find {} as starting from {}!", peakAreaVariable, fragment.getName());
                }
            }
            final List<String> v = Arrays.asList(line);
            rows.add(v);
            log.debug("Adding row {}", v);
            rowCounter++;
        }

        final CSVWriter csvw = new CSVWriter();
        csvw.setWorkflow(getWorkflow());
        csvw.writeTableByRows(getWorkflow().getOutputDirectory(this).
                getAbsolutePath(), "multiple-alignmentAreaPercent.csv", rows,
                WorkflowSlot.ALIGNMENT);
    }

    /**
     * @param columnMap
     * @param cliqueList
     */
    private void savePeakMatchRTTable(final Map<String, Integer> columnMap,
            final List<List<IBipacePeak>> ll) {
        final List<List<String>> rows = new ArrayList<>(ll.size());
        List<String> headers = null;
        final String[] headerLine = new String[columnMap.size()];
        for (int i = 0; i < headerLine.length; i++) {
            headerLine[i] = "";
        }
        headers = Arrays.asList(headerLine);
        for (final String s : columnMap.keySet()) {
            headers.set(columnMap.get(s), StringTools.removeFileExt(s));
        }
        log.debug("Adding row {}", headers);
        rows.add(headers);
        final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
                Locale.US);
        // this is a fix, default rounding convention is HALF_EVEN,
        // which allows less error to accumulate, but is seldomly used
        // outside of java...
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.applyPattern(rtOutputFormat);
        for (final List<IBipacePeak> l : ll) {
            final String[] line = new String[columnMap.size()];
            for (int i = 0; i < line.length; i++) {
                line[i] = "-";
            }
            log.debug("Adding {} peaks", l.size());
            for (final IBipacePeak p : l) {
                final String iff = p.getAssociation();
                EvalTools.notNull(iff, this);
                final int pos = columnMap.get(iff);
                log.debug("Insert position for {}: {}", iff, pos);
                if (pos >= 0) {
                    if (line[pos].equals("-")) {
                        final double sat = p.getScanAcquisitionTime() * rtNormalizationFactor;
                        line[pos] = df.format(sat) + "";
                    } else {
                        log.warn("Array position {} already used!", pos);
                    }
                }
            }
            final List<String> v = Arrays.asList(line);
            rows.add(v);
            log.debug("Adding row {}", v);
        }

        final CSVWriter csvw = new CSVWriter();
        csvw.setWorkflow(getWorkflow());
        csvw.writeTableByRows(getWorkflow().getOutputDirectory(this).
                getAbsolutePath(), "multiple-alignmentRT.csv", rows,
                WorkflowSlot.ALIGNMENT);
    }

    /**
     * @param columnMap
     * @param cliqueList
     */
    private void savePeakMatchTable(final Map<String, Integer> columnMap,
            final List<List<IBipacePeak>> ll) {
        final CSVWriter csvw = new CSVWriter();
        csvw.setWorkflow(getWorkflow());
        List<String> headers = null;
        final String[] headerLine = new String[columnMap.size()];
        for (int i = 0; i < headerLine.length; i++) {
            headerLine[i] = "";
        }
        headers = Arrays.asList(headerLine);
        for (final String s : columnMap.keySet()) {
            headers.set(columnMap.get(s), StringTools.removeFileExt(s));
        }
        log.debug("Adding row {}", headers);
        try (PrintWriter writer = csvw.createPrintWriter(getWorkflow().getOutputDirectory(this).
                getAbsolutePath(), "multiple-alignment.csv", headers, WorkflowSlot.ALIGNMENT)) {
            for (final List<IBipacePeak> l : ll) {
                final String[] line = new String[columnMap.size()];
                for (int i = 0; i < line.length; i++) {
                    line[i] = "-";
                }
                log.debug("Adding {} peaks: {}", l.size(), l);
                for (final IBipacePeak p : l) {
                    final String iff = p.getAssociation();
                    EvalTools.notNull(iff, this);
                    final int pos = columnMap.get(iff);
                    log.debug("Insert position for {}: {}", iff, pos);
                    if (pos >= 0) {
                        if (line[pos].equals("-")) {
                            line[pos] = p.getScanIndex() + "";
                        } else {
                            log.warn("Array position {} already used!", pos);
                        }
                    }
                }
                final List<String> v = Arrays.asList(line);
                log.debug("Adding row {}", v);
                csvw.writeLine(writer, v);
            }
        }
    }

    private void saveToXMLAlignment(final TupleND<IFileFragment> tuple,
            final List<List<IBipacePeak>> ll) {
        if (saveXMLAlignment) {
            log.info("Saving alignment to xml!");
            File out = new File(getWorkflow().getOutputDirectory(this),
                    "peakCliqueAssignment.maltcmsAlignment.xml");
            XmlAlignmentWriter xmlWriter = new XmlAlignmentWriter();
            xmlWriter.saveToXMLAlignment(out, tuple, ll);
            DefaultWorkflowResult dwr = new DefaultWorkflowResult(out, this,
                    WorkflowSlot.ALIGNMENT, tuple.toArray(new IFileFragment[tuple.size()]));
            getWorkflow().append(dwr);
        }
    }

    private void saveCliquePlots(final List<Clique<IBipacePeak>> l, final TupleND<IFileFragment> al) {
        DefaultBoxAndWhiskerCategoryDataset dscdRT = new DefaultBoxAndWhiskerCategoryDataset();
        for (Clique<IBipacePeak> c : l) {
            dscdRT.add(c.createRTBoxAndWhisker(), "", c.getCliqueRTMean());
        }
        JFreeChart jfc = ChartFactory.createBoxAndWhiskerChart("Cliques",
                "clique mean RT", "RT diff to centroid", dscdRT, true);
        jfc.getCategoryPlot().setOrientation(PlotOrientation.HORIZONTAL);
        PlotRunner pr = new PlotRunner(jfc.getCategoryPlot(),
                "Clique RT diff to centroid", "cliquesRTdiffToCentroid.png",
                getWorkflow().getOutputDirectory(this));
        pr.configure(Factory.getInstance().getConfiguration());
        pr.setImgwidth(800);
        pr.setImgheight(25 * l.size());
        final File f = pr.getFile();
        final DefaultWorkflowResult dwr = new DefaultWorkflowResult(f, this,
                WorkflowSlot.VISUALIZATION,
                al.toArray(new IFileFragment[]{}));
        getWorkflow().append(dwr);
        Factory.getInstance().submitJob(pr);

        DefaultBoxAndWhiskerCategoryDataset dscdTIC = new DefaultBoxAndWhiskerCategoryDataset();
        for (Clique<IBipacePeak> c : l) {
            dscdTIC.add(c.createApexTicBoxAndWhisker(), "", c.getCliqueRTMean());
        }
        JFreeChart jfc2 = ChartFactory.createBoxAndWhiskerChart("Cliques",
                "clique mean RT", "log(apex TIC centroid)-log(apex TIC)",
                dscdTIC, true);
        jfc2.getCategoryPlot().setOrientation(PlotOrientation.HORIZONTAL);
        PlotRunner pr2 = new PlotRunner(jfc2.getCategoryPlot(),
                "Clique log apex TIC centroid diff to log apex TIC",
                "cliquesLogApexTICCentroidDiffToLogApexTIC.png",
                getWorkflow().getOutputDirectory(this));
        pr2.configure(Factory.getInstance().getConfiguration());
        pr2.setImgwidth(800);
        pr2.setImgheight(25 * l.size());
        final File g = pr.getFile();
        final DefaultWorkflowResult dwr2 = new DefaultWorkflowResult(g, this,
                WorkflowSlot.VISUALIZATION,
                al.toArray(new IFileFragment[]{}));
        getWorkflow().append(dwr2);
        Factory.getInstance().submitJob(pr2);
    }
}
