/*
 * $license$
 *
 * $Id$
 */
package maltcms.experimental.bipace.peakCliqueAlignment.io;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.MathTools;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.peak.Peak;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.jdom.Element;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Slf4j
@Data
public class LangeTautenhahnExporter implements IWorkflowElement {

    private String massValues = "mass_values";
    private String intensityValues = "intensity_values";
    private String scanIndex = "scan_index";
    private int minCliqueSize = 2;
    private double intensityStdDeviationFactor = 1.0;
    private IWorkflow workflow;

    public void saveToLangeTautenhahnFormat(
            final HashMap<String, Integer> columnMap, final List<List<Peak>> ll) {
        log.info("Saving data to [intensity rt m/z] format.");
        // filename intensity rt m/z
        // final List<List<String>> rows = new
        // ArrayList<List<String>>(ll.size());
        File outputFile = new File(getWorkflow().getOutputDirectory(this).
                getAbsolutePath(), "peakCliqueAssignment_matched_features.csv");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
            final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
                    Locale.US);
            df.applyPattern("0.000");
            for (final List<Peak> l : ll) {

                // check peak group for common masses within bounds
                // round mz to second digit after comma -> mass binning
                // for each peak, map its mz values to intensity
                // store Peak and original mz with mass binned mz as key
                final TreeMap<Double, LinkedHashMap<Peak, double[]>> keyToPeakMz = new TreeMap<Double, LinkedHashMap<Peak, double[]>>();
                for (final Peak p : l) {
                    final IFileFragment iff = p.getAssociation();
                    final IVariableFragment sindex = iff.getChild(this.scanIndex);
                    final IVariableFragment masses = iff.getChild(
                            this.massValues);
                    masses.setIndex(sindex);
                    final IVariableFragment intensities = iff.getChild(
                            this.intensityValues);
                    intensities.setIndex(sindex);
                    EvalTools.notNull(iff, this);

                    Array massA = masses.getArray();
                    IndexIterator iter = massA.getIndexIterator();
                    Array intensA = intensities.getArray();
                    IndexIterator iiter = intensA.getIndexIterator();
                    // double mz = MaltcmsTools.getMaxMass(massA, intensA);
                    // double intens =
                    // MaltcmsTools.getMaxMassIntensity(intensA);
                    // map mzKey to peak to mz
                    LinkedHashMap<Peak, double[]> peakToMz;
                    while (iter.hasNext() && iiter.hasNext()) {
                        double mz = iter.getDoubleNext();
                        double intens = iiter.getDoubleNext();
                        double rmz = mz * 100.0;
                        rmz = Math.round(rmz);
                        rmz = rmz / 100.0;

                        if (keyToPeakMz.containsKey(rmz)) {
                            peakToMz = keyToPeakMz.get(rmz);
                        } else {
                            peakToMz = new LinkedHashMap<Peak, double[]>();
                            keyToPeakMz.put(rmz, peakToMz);
                        }
                        peakToMz.put(p, new double[]{mz, intens});
                    }
                }
                int minNumberOfCommonFeatures = this.minCliqueSize == -1 ? columnMap.
                        size() : this.minCliqueSize;
                // check bins and remove features, which do not occur often
                // enough
                HashSet<Double> keysToRemove = new HashSet<Double>();
                StandardDeviation sd = new StandardDeviation();
                for (Double d : keyToPeakMz.keySet()) {
                    LinkedHashMap<Peak, double[]> lhm = keyToPeakMz.get(d);
                    if (lhm.keySet().size() >= minNumberOfCommonFeatures) {
                        int validFeatures = 0;
                        double[] intensities = new double[lhm.keySet().size()];
                        int i = 0;
                        for (Peak p : lhm.keySet()) {
                            intensities[i++] = lhm.get(p)[1];
                        }

                        double median = MathTools.median(intensities);
                        double stddev = sd.evaluate(intensities);
                        log.info("Median: {}, stddev: {}", median, stddev);
                        for (int j = 0; j < intensities.length; j++) {
                            double v = Math.pow(intensities[j] - median, 2.0);
                            // if (v < 1.0 * stddev) {
                            validFeatures++;
                            // }
                        }
                        if (validFeatures < minNumberOfCommonFeatures) {
                            keysToRemove.add(d);
                            log.info(
                                    "Skipping mass {}, not enough well behaved features within bounds: {},{}!",
                                    new Object[]{
                                        d,
                                        median
                                        - this.intensityStdDeviationFactor
                                        * stddev,
                                        median
                                        + this.intensityStdDeviationFactor});
                        }

                    } else {// remove if not enough features
                        keysToRemove.add(d);
                        log.info("Skipping mass {}, not enough features!", d);
                    }
                }
                for (Double d : keysToRemove) {
                    keyToPeakMz.remove(d);
                }
                log.info("Found {} common features!",
                        keyToPeakMz.keySet().size());

                // build feature line for each mz group

                // line[0] = l.get(0).getAssociation().getName();
                log.debug("Adding {} peaks: {}", l.size(), l);

                for (double d : keyToPeakMz.keySet()) {
                    LinkedHashMap<Peak, double[]> peakToMz = keyToPeakMz.get(d);

                    final String[] line = new String[columnMap.size() * 3];
                    for (Peak p : peakToMz.keySet()) {
                        IFileFragment iff = p.getAssociation();
                        final int pos = columnMap.get(iff.getName()).intValue() * 3;
                        log.debug("Insert position for {}: {}",
                                iff.getName(), pos);
                        if (pos >= 0) {
                            double[] tpl = peakToMz.get(p);
                            double mz = tpl[0];
                            double intensity = tpl[1];
                            line[pos] = intensity + "";
                            line[pos + 1] = p.getScanAcquisitionTime() + "";
                            line[pos + 2] = mz + "";
                            log.debug("SAT: {}", line[pos + 2]);
                        }
                    }
                    // final List<String> v = Arrays.asList(line);
                    StringBuilder sb = new StringBuilder();
                    for (String s : line) {
                        sb.append(s + "\t");
                    }
                    sb.append("\n");
                    bw.write(sb.toString());

                    // log.debug("Adding row {}", v);
                }
                // for (final Peak p : l) {
                // final IFileFragment iff = p.getAssociation();
                // final IVariableFragment sindex = iff
                // .getChild(this.scanIndex);
                // final IVariableFragment masses = iff
                // .getChild(this.massValues);
                // masses.setIndex(sindex);
                // final IVariableFragment intensities = iff
                // .getChild(this.intensityValues);
                // intensities.setIndex(sindex);
                // EvalTools.notNull(iff, this);
                // final int pos = columnMap.get(iff.getName()).intValue() * 3;
                // log.debug("Insert position for {}: {}", iff.getName(),
                // pos);
                // if (pos >= 0) {
                // // line[pos] = iff.getName();
                // log.debug("Reading scan {}", p.getScanIndex());
                // final Array mza = masses.getIndexedArray().get(
                // p.getScanIndex());
                // final Array intena = intensities.getIndexedArray().get(
                // p.getScanIndex());
                // // List<Tuple2D<Double, Double>> pm = MaltcmsTools
                // // .getPeakingMasses(iff, p.getScanIndex(), p
                // // .getScanIndex() - 1, 3, 1.0d);
                // // log.info("Found peaking masses for scan {}:{}",
                // // p
                // // .getScanIndex(), pm);
                // double peakMass = MaltcmsTools.getMaxMass(mza, intena);
                // double peakIntens = MaltcmsTools
                // .getMaxMassIntensity(intena);
                // // double peakMass = pm.get(pm.size() - 1).getSecond();
                // // double peakIntens = pm.get(pm.size() - 1).getFirst();
                // line[pos] = peakIntens + "";
                // line[pos + 1] = p.getScanAcquisitionTime() + "";
                // line[pos + 2] = peakMass + "";
                // log.debug("SAT: {}", line[pos + 2]);
                // }
                // }
                // StringBuilder sb = new StringBuilder();
                // for (String s : line) {
                // sb.append(s + " ");
                // }
                // sb.append("\n");
                // bw.write(sb.toString());
            }
            bw.flush();
            bw.close();
            DefaultWorkflowResult dwr = new DefaultWorkflowResult(outputFile,
                    this, WorkflowSlot.ALIGNMENT);
            getWorkflow().append(dwr);
        } catch (IOException ioe) {
            log.error("{}", ioe.getLocalizedMessage());
        }
    }

    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.FILEIO;
    }

    @Override
    public void appendXML(Element e) {
    }
}
