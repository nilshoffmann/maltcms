package maltcms.commands.fragments.alignment2;

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
 * $Id: PeakCliqueAlignment.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import maltcms.datastructures.peak.Clique;
import maltcms.datastructures.peak.Peak;
import maltcms.tools.ArrayTools;

import org.apache.commons.configuration.Configuration;

import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.NotImplementedException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.experimental.bipace.spi.Peak1DSimilarityCalculator;
import maltcms.experimental.bipace.api.PeakSimilarityCalculator;
import maltcms.experimental.bipace.peakCliqueAlignment.BBHFinder;
import maltcms.experimental.bipace.peakCliqueAlignment.CliqueFinder;
import maltcms.experimental.bipace.peakCliqueAlignment.PeakSimilarityVisualizer;
import maltcms.experimental.bipace.peakCliqueAlignment.io.AnchorExporter;
import maltcms.experimental.bipace.peakCliqueAlignment.io.LangeTautenhahnExporter;
import maltcms.experimental.bipace.peakCliqueAlignment.io.MultipleAlignmentWriter;
import maltcms.experimental.bipace.peakCliqueAlignment.io.XmlAlignmentWriter;
import maltcms.math.functions.IScalarArraySimilarity;
import maltcms.math.functions.ProductSimilarity;
import maltcms.math.functions.similarities.ArrayCorr;
import maltcms.math.functions.similarities.GaussianDifferenceSimilarity;
import net.sf.maltcms.execution.api.ICompletionService;
import org.openide.util.lookup.ServiceProvider;

/**
 * For every peak in each chromatogram, its bi-directional best hits are
 * determined, and all bi-directional best hits are merged. If they apply, they
 * cover a set of k peaks in k chromatograms.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
@RequiresVariables(names = {"var.binned_mass_values",
    "var.binned_intensity_values", "var.binned_scan_index",
    "var.scan_acquisition_time", "var.mass_values", "var.intensity_values",
    "var.scan_index"})
@RequiresOptionalVariables(names = {"var.tic_peaks"})
@ProvidesVariables(names = {"var.anchors.retention_index_names",
    "var.anchors.retention_times", "var.anchors.retention_indices",
    "var.anchors.retention_scans"})
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class PeakCliqueAlignment extends AFragmentCommand {

    private IScalarArraySimilarity similarityFunction;
    @Configurable(name = "var.tic_peaks")
    private String ticPeaks = "tic_peaks";
    @Configurable(name = "var.mass_values")
    private String massValues = "mass_values";
    @Configurable(name = "var.scan_index")
    private String scanIndex = "scan_index";
    @Configurable(name = "var.intensity_values")
    private String intensityValues = "intensity_values";
    @Configurable
    private boolean useUserSuppliedAnchors = false;
    @Configurable
    private int minCliqueSize = -1;
    @Configurable(name = "var.retention_index_names")
    private String anchorNames = "retention_index_names";
    @Configurable(name = "var.retention_times")
    private String anchorTimes = "retention_times";
    @Configurable(name = "var.retention_indices")
    private String anchorRetentionIndex = "retention_indices";
    @Configurable(name = "var.retention_scans")
    private String anchorScanIndex = "retention_scans";
    // private boolean keepOnlyBiDiBestHitsForAll = true;
    @Configurable(name = "var.binned_intensity_values")
    private String binnedIntensities = "binned_intensity_values";
    @Configurable(name = "var.binned_scan_index")
    private String binnedScanIndex = "binned_scan_index";
    @Configurable(name = "var.scan_acquisition_time")
    private String scanAcquisitionTime = "scan_acquisition_time";
    @Configurable
    private boolean savePeakSimilarities = false;
    @Configurable
    private boolean exportAlignedFeatures = false;
    @Configurable
    private double maxRTDifference = 60.0d;
    @Configurable
    private double intensityStdDeviationFactor = 1.0d;
    @Configurable
    private boolean saveXMLAlignment = true;
    @Configurable
    private int maxBBHErrors = 0;
    @Configurable
    private boolean savePlots = false;

    public PeakCliqueAlignment() {
        similarityFunction = new ProductSimilarity();
        GaussianDifferenceSimilarity gds = new GaussianDifferenceSimilarity();
        similarityFunction.setScalarSimilarities(gds);
        ArrayCorr ac = new ArrayCorr();
        similarityFunction.setArraySimilarities(ac);
    }

    @Override
    public String toString() {
        return getClass().getName();
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
        if (t.size() < 2) {
            log.warn("At least two files required for peak clique assignment!");
        } else {
            final TupleND<IFileFragment> tret = identifyPeaks(t);
            for (final IFileFragment iff : tret) {
                log.debug("{}", iff);
            }
            return tret;
        }
        return t;
    }

    /**
     * @param al
     * @param fragmentToPeaks
     * TODO: Paralellize like in PairwiseDistanceCalculator
     */
    private void calculatePeakSimilarities(final TupleND<IFileFragment> al,
            final HashMap<String, List<Peak>> fragmentToPeaks) {
//        int n = 0;
//        for (String key : fragmentToPeaks.keySet()) {
//            n += fragmentToPeaks.get(key).size();
//        }
//        log.info(
//                "Calculating {} pairwise peak similarities for {} peaks!",
//                ((long) n * (long) n), n);
//        log.info("Using {} as pairwise peak similarity!",
//                this.similarityFunction.getClass().getName());
//
//        // Loop over all pairs of FileFragments
//        long elements = (long) n * (long) n;
//        double percentDone = 0;
//        final long parts = 10;
//        long partCnt = 0;
//        long elemCnt = 0;
//        
//        // k^{2}, could be reduced to k^{2}-k, if we only use pairwise distinct
//        // file fragments
//        for (IFileFragment f1 : al) {
//            for (IFileFragment f2 : al) {
//                // calculate similarity between peaks
//                final List<Peak> lhsPeaks = fragmentToPeaks.get(f1.getName());
//                final List<Peak> rhsPeaks = fragmentToPeaks.get(f2.getName());
//                log.debug("Comparing {} and {}", f1.getName(),
//                        f2.getName());
//                PeakSimilarityCalculator psc = new Peak1DSimilarityCalculator();
//                psc.setSimilarityFunction(similarityFunction);
//                
//                // all-against-all peak list comparison
//                // l^{2} for l=max(|lhsPeaks|,|rhsPeaks|)
//                for (final Peak p1 : lhsPeaks) {
//                    for (final Peak p2 : rhsPeaks) {
//                        // skip peaks, which are too far apart
//                        double rt1 = p1.getScanAcquisitionTime();
//                        double rt2 = p2.getScanAcquisitionTime();
//                        // cutoff to limit calculation work
//                        // this has a better effect, than applying the limit
//                        // within the similarity function only
//                        // of course, this limit should be larger
//                        // than the limit within the similarity function
//                        if (Math.abs(rt1 - rt2) < this.maxRTDifference) {
//                            // the similarity is symmetric:
//                            // sim(a,b) = sim(b,a)
//                            final Double d = getSimilarity(p1, p2);
//                            p1.addSimilarity(p2, d);
//                            p2.addSimilarity(p1, d);
//                        }
//                        elemCnt++;
//                    }
//                }
//                // update progress
//                percentDone = ArrayTools.calcPercentDone(elements, elemCnt);
//                partCnt = ArrayTools.printPercentDone(percentDone, parts,
//                        partCnt, log);
//            }
//        }
//
//        if (this.savePeakSimilarities) {
//            PeakSimilarityVisualizer psv = new PeakSimilarityVisualizer();
//            psv.setWorkflow(getWorkflow());
//            psv.visualizePeakSimilarities(
//                    fragmentToPeaks, 256, "beforeBIDI");
//        }
        throw new NotImplementedException();
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
        this.minCliqueSize = cfg.getInt(this.getClass().getName()
                + ".minCliqueSize", -1);
//        final String aldist = "maltcms.commands.distances.ArrayLp";
//        this.costFunction = Factory.getInstance().getObjectFactory().instantiate(
//                cfg.getString(this.getClass().getName()
//                + ".costFunction", aldist),
//                IArrayDoubleComp.class);
        this.useUserSuppliedAnchors = cfg.getBoolean(this.getClass().getName()
                + ".useUserSuppliedAnchors", false);
        this.savePeakSimilarities = cfg.getBoolean(this.getClass().getName()
                + ".savePeakSimilarities", false);
        this.exportAlignedFeatures = cfg.getBoolean(this.getClass().getName()
                + ".exportAlignedFeatures", false);
        this.maxRTDifference = cfg.getDouble(this.getClass().getName()
                + ".maxRTDifference", 60.0d);
        this.intensityStdDeviationFactor = cfg.getDouble(
                this.getClass().getName() + ".intensityStdDeviationFactor", 5);
        this.saveXMLAlignment = cfg.getBoolean(
                this.getClass().getName() + ".saveXMLAlignment", true);
        this.maxBBHErrors = cfg.getInt(
                this.getClass().getName() + ".maxBBHErrors", 0);
        this.savePlots = cfg.getBoolean(this.getClass().getName() + ".savePlots",
                false);
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.commands.fragments.AFragmentCommand#getDescription()
     */
    @Override
    public String getDescription() {
        return "Assigns peak candidates as pairs and groups them into cliques of size k";
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.PEAKMATCHING;
    }

    private TupleND<IFileFragment> identifyPeaks(
            final TupleND<IFileFragment> originalFragments) {
        log.debug("Matching peaks");
        final HashMap<String, List<Peak>> fragmentToPeaks = new HashMap<String, List<Peak>>();
        final HashMap<String, Integer> columnMap = new HashMap<String, Integer>(
                originalFragments.size());
        final TupleND<IFileFragment> t = createWorkFragments(originalFragments);
        initializePeaks(t, fragmentToPeaks,
                columnMap);
        log.info("Calculating all-against-all peak similarities");
        calculatePeakSimilarities(t, fragmentToPeaks);

        log.info("Searching for bidirectional best hits");
        BBHFinder bbhf = new BBHFinder();
        final List<Peak> unmatchedPeaks = bbhf.findBiDiBestHits(t,
                fragmentToPeaks);
        final List<List<Peak>> ll = new ArrayList<List<Peak>>();
        CliqueFinder cf = new CliqueFinder();
        cf.setWorkflow(getWorkflow());
        cf.setMaxBBHErrors(maxBBHErrors);
        cf.setMinCliqueSize(minCliqueSize);
        cf.setSavePlots(savePlots);

        final HashMap<Peak, Clique> peakToClique = new HashMap<Peak, Clique>();
        List<Clique> cliques = cf.findCliques(t, fragmentToPeaks, ll,
                peakToClique);

        //reinspect
        bbhf.removePeakSimilaritiesWhichHaveNoBestHits(t, fragmentToPeaks);
        TupleND<IFileFragment> ret = saveResults(fragmentToPeaks, columnMap, ll,
                t, cliques, originalFragments, peakToClique);

        for (IFileFragment iff : originalFragments) {
            iff.clearArrays();
        }
        return ret;
    }

    private void initializePeaks(final TupleND<IFileFragment> t,
            final HashMap<String, List<Peak>> fragmentToPeaks,
            final HashMap<String, Integer> columnMap) {
        Peak1DProvider peakProvider = new Peak1DProvider();
        peakProvider.setUseUserSuppliedAnchors(useUserSuppliedAnchors);
        peakProvider.setKeepBestSimilaritiesOnly(savePeakSimilarities);
        peakProvider.setAnchorNames(anchorNames);
        peakProvider.setAnchorRetentionIndex(anchorRetentionIndex);
        peakProvider.setAnchorScanIndex(anchorScanIndex);
        peakProvider.setAnchorTimes(anchorTimes);
        peakProvider.setBinnedIntensities(binnedIntensities);
        peakProvider.setBinnedScanIndex(binnedScanIndex);
        peakProvider.setIntensityValues(intensityValues);
        peakProvider.setMassValues(massValues);
        peakProvider.setScanAcquisitionTime(scanAcquisitionTime);
        peakProvider.setScanIndex(scanIndex);
        peakProvider.setTicPeaks(ticPeaks);
        peakProvider.initializePeaks(t, fragmentToPeaks,
                columnMap);
    }

    private TupleND<IFileFragment> saveResults(
            final HashMap<String, List<Peak>> fragmentToPeaks,
            final HashMap<String, Integer> columnMap,
            final List<List<Peak>> ll,
            final TupleND<IFileFragment> t,
            List<Clique> cliques,
            final TupleND<IFileFragment> originalFragments,
            final HashMap<Peak, Clique> peakToClique) {
        if (this.savePeakSimilarities) {
            PeakSimilarityVisualizer psv =
                    new PeakSimilarityVisualizer();
            psv.setWorkflow(getWorkflow());
            psv.visualizePeakSimilarities(
                    fragmentToPeaks, 2, "afterBIDI");
        }
        if (this.exportAlignedFeatures) {
            LangeTautenhahnExporter lte = new LangeTautenhahnExporter();
            lte.setWorkflow(getWorkflow());
            lte.setMassValues(massValues);
            lte.setIntensityValues(intensityValues);
            lte.setScanIndex(scanIndex);
            lte.setIntensityStdDeviationFactor(intensityStdDeviationFactor);
            lte.setMinCliqueSize(minCliqueSize);
            lte.saveToLangeTautenhahnFormat(columnMap, ll);
        }
        log.info("Saving peak match tables!");
        MultipleAlignmentWriter maw = new MultipleAlignmentWriter();
        maw.setWorkflow(getWorkflow());
        maw.savePeakMatchTable(columnMap, ll);
        maw.savePeakMatchRTTable(columnMap, ll);
        if (saveXMLAlignment) {
            log.info("Saving alignment to xml!");
            XmlAlignmentWriter xaw = new XmlAlignmentWriter();
            xaw.setWorkflow(getWorkflow());
            xaw.saveToXMLAlignment(t, ll);
        }
        log.info("Adding anchor variables");
        TupleND<IFileFragment> ret = null;
        if (cliques.isEmpty()) {
            ret = originalFragments;
        } else {
            AnchorExporter ae = new AnchorExporter();
            ae.setWorkflow(getWorkflow());
            ae.addAnchors(ll, t, cliques, peakToClique);
        }
        return ret;
    }
}
