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
package maltcms.commands.fragments.peakfinding.io;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import java.awt.Color;
import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments.peakfinding.TICPeakFinder;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.WorkflowResult;
import maltcms.datastructures.peak.MaltcmsAnnotationFactory;
import maltcms.datastructures.peak.Peak1D;
import maltcms.datastructures.peak.normalization.IPeakNormalizer;
import maltcms.io.csv.CSVWriter;
import maltcms.io.xml.bindings.annotation.MaltcmsAnnotation;
import maltcms.tools.ArrayTools;
import maltcms.ui.charts.AChart;
import maltcms.ui.charts.CombinedDomainXYChart;
import maltcms.ui.charts.PlotRunner;
import maltcms.ui.charts.XYChart;
import net.sf.mpaxs.api.ICompletionService;
import net.sf.mpaxs.spi.concurrent.CompletionServiceFactory;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.nc2.Dimension;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
@Data
public class Peak1DUtilities implements Serializable {

    public void addTicResults(final IFileFragment ff,
            final List<Peak1D> peaklist, final List<IPeakNormalizer> peakNormalizers, Array filteredSignal, String filteredValuesVariableName) {
        if (filteredSignal != null && filteredValuesVariableName != null) {
            final Dimension scan_number = new Dimension("scan_number", filteredSignal.getShape()[0]);
            final IVariableFragment mai = new VariableFragment(ff,
                    filteredValuesVariableName);
            mai.setDimensions(new Dimension[]{scan_number});
            mai.setArray(filteredSignal);
        }
        Peak1D.append(ff, peakNormalizers, peaklist, "tic_peaks");
    }

    public void addEicResults(final IFileFragment ff,
            final List<Peak1D> peaklist, final List<IPeakNormalizer> peakNormalizers) {
        Peak1D.append(ff, peakNormalizers, peaklist, "eic_peaks");
    }

    /**
     * <p>
     * saveXMLPeakAnnotations.</p>
     *
     * @param output a {@link java.net.URI} object, pointing to a result file.
     * @param l a {@link java.util.List} object.
     * @param iff a {@link cross.datastructures.fragments.IFileFragment} object,
     * containing the raw data on which peakfinding was performed.
     * @return a
     * {@link maltcms.commands.fragments.peakfinding.ticPeakFinder.WorkflowResult}
     * object.
     */
    public WorkflowResult saveXMLPeakAnnotations(URI output, final List<Peak1D> l,
            final IFileFragment iff) {
        File outputDirectory = new File(output).getParentFile();
        return saveXMLPeakAnnotations(outputDirectory, l, iff);
    }

    /**
     * <p>
     * saveXMLPeakAnnotations.</p>
     *
     * @param outputDirectory a {@link java.io.File} object, pointing to the
     * result output directory.
     * @param l a {@link java.util.List} object.
     * @param iff a {@link cross.datastructures.fragments.IFileFragment} object,
     * containing the raw data on which peakfinding was performed.
     * @return a
     * {@link maltcms.commands.fragments.peakfinding.ticPeakFinder.WorkflowResult}
     * object.
     */
    public WorkflowResult saveXMLPeakAnnotations(File outputDirectory, final List<Peak1D> l,
            final IFileFragment iff) {
        MaltcmsAnnotationFactory maf = new MaltcmsAnnotationFactory();
        File matFile = new File(outputDirectory,
                StringTools.removeFileExt(iff.getName())
                + ".maltcmsAnnotation.xml");
        MaltcmsAnnotation ma = maf.createNewMaltcmsAnnotationType(iff.getUri());
        for (Peak1D p : l) {
            maf.addPeakAnnotation(ma, this.getClass().getName(), p);
        }
        maf.save(ma, matFile);
        WorkflowResult result = new WorkflowResult(matFile.toURI(), Peak1DUtilities.class.getCanonicalName(), WorkflowSlot.PEAKFINDING, new URI[]{iff.getUri()});
        return result;
    }

    /**
     * <p>
     * saveCSVPeakAnnotations.</p>
     *
     * @param outputDirectory the directory in which to store result files
     * @param l peak list
     * @param iff the source file fragment
     * @return a
     * {@link maltcms.commands.fragments.peakfinding.ticPeakFinder.WorkflowResult}
     * object.
     */
    public WorkflowResult saveCSVPeakAnnotations(final File outputDirectory, final List<Peak1D> l, final IFileFragment iff) {
        final List<List<String>> rows = new ArrayList<>(l.size());
        List<String> headers = null;
        final String[] headerLine = new String[]{"APEX", "START", "STOP",
            "RT_APEX", "RT_START", "RT_STOP", "AREA", "AREA_NORMALIZED", "AREA_NORMALIZED_PERCENT", "NORMALIZATION_METHODS", "MW", "INTENSITY", "SNR"};
        headers = Arrays.asList(headerLine);
        log.debug("Adding row {}", headers);
        rows.add(headers);
        for (final Peak1D pb : l) {
            final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
                    Locale.US);
            df.applyPattern("0.0000");
            log.debug("Adding {} peaks", l.size());
            final String[] line = new String[]{pb.getApexIndex() + "",
                pb.getStartIndex() + "", pb.getStopIndex() + "",
                df.format(pb.getApexTime()), df.format(pb.getStartTime()),
                df.format(pb.getStopTime()), pb.getArea() + "", pb.getNormalizedArea() + "",
                (pb.getNormalizationMethods().length == 0) ? "" : pb.getNormalizedArea() * 100.0 + "",
                Arrays.toString(pb.getNormalizationMethods()),
                "" + pb.getMw(), "" + pb.getApexIntensity(), "" + pb.getSnr()};
            final List<String> v = Arrays.asList(line);
            rows.add(v);
            log.debug("Adding row {}", v);
        }

        final CSVWriter csvw = new CSVWriter();
        File peakAreasFile = csvw.writeTableByRows(outputDirectory.
                getAbsolutePath(), StringTools.removeFileExt(iff.getName())
                + "_peakAreas.csv", rows, WorkflowSlot.ALIGNMENT);
        return new WorkflowResult(peakAreasFile.toURI(), TICPeakFinder.class.getCanonicalName(), WorkflowSlot.PEAKFINDING, new URI[]{iff.getUri()});
    }

    /**
     * <p>
     * visualize.</p>
     *
     * @param outputDirectory the output directory.
     * @param f the input file fragment.
     * @param xValues a {@link ucar.ma2.Array} object.
     * @param yValues a {@link ucar.ma2.Array} object.
     * @param filteredYValues a {@link ucar.ma2.Array} object.
     * @param snr an array of double.
     * @param peaks a {@link ucar.ma2.ArrayInt.D1} object.
     * @param peakThreshold a double.
     * @param baselineEstimator a
     * {@link org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction}
     * object.
     * @param systemProperties a {@link java.util.Properties} object. Contains the
     * system properties including keys for charting.
     * @return a collection of {@link WorkflowResult}s.
     */
    public Collection<WorkflowResult> visualize(final File outputDirectory, final IFileFragment f, final Array xValues, final Array yValues,
            final Array filteredYValues,
            final double[] snr, final ArrayInt.D1 peaks,
            final double peakThreshold, PolynomialSplineFunction baselineEstimator, Properties systemProperties) {
        String x_label = "time [s]";
        final ArrayDouble.D1 posx = new ArrayDouble.D1(peaks.getShape()[0]);
        final ArrayDouble.D1 posy = new ArrayDouble.D1(peaks.getShape()[0]);
        final Array snrEstimate = Array.factory(snr);
        final Array threshold = new ArrayDouble.D1(snr.length);
        final Array baseline = new ArrayDouble.D1(snr.length);
        for (int i = 0; i < snr.length; i++) {
            try {
                baseline.setDouble(i, baselineEstimator.value(xValues.getDouble(i)));
            } catch (ArgumentOutsideDomainException ex) {
                Logger.getLogger(TICPeakFinder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        ArrayTools.fill(threshold, peakThreshold);
        final Index satIdx = xValues.getIndex();
        final Index intensIdx = yValues.getIndex();
        for (int i = 0; i < peaks.getShape()[0]; i++) {
            posx.set(i, xValues.getDouble(satIdx.set(peaks.get(i))));
            posy.set(i, yValues.getInt(intensIdx.set(peaks.get(i))));
        }
        final AChart<XYPlot> tc1 = new XYChart("SNR plot",
                new String[]{"Signal-to-noise ratio", "Threshold"},
                new Array[]{snrEstimate, threshold}, new Array[]{xValues}, posx, posy,
                new String[]{}, x_label, "snr (db)");
        tc1.setSeriesColors(new Color[]{Color.RED, Color.BLUE});
        final AChart<XYPlot> tc2 = new XYChart("TICPeakFinder results for "
                + f.getName(), new String[]{"Total Ion Count (TIC)", "Filtered TIC",
                    "Estimated Baseline"},
                new Array[]{yValues, filteredYValues, baseline}, new Array[]{
                    xValues}, posx,
                posy, new String[]{}, x_label, "counts");
        // final AChart<XYPlot> tc3 = new XYChart("Peak candidates",
        // new String[] { "Peak candidates" }, new Array[] { peaks },
        // new Array[] { domain }, x_label, "peak");
        // final AChart<XYPlot> tc4 = new
        // XYChart("Value of median within window",
        // new String[] { "Value of median within window" },
        // new Array[] { deviation }, new Array[] { domain }, x_label,
        // "counts");
        final ArrayList<XYPlot> al = new ArrayList<>();
        al.add(tc1.create());
        // final XYPlot pk = tc3.create();
        final XYBarRenderer xyb = new XYBarRenderer();
        xyb.setShadowVisible(false);
        // pk.setRenderer(xyb);
        // al.add(pk);
        al.add(tc2.create());
        // al.add(tc4.create());
        final CombinedDomainXYChart cdt = new CombinedDomainXYChart("TIC-Peak",
                x_label, false, al);
        final PlotRunner pr = new PlotRunner(cdt.create(),
                "TIC and Peak information for " + f.getName(),
                "combinedTICandPeakChart-" + StringTools.removeFileExt(f.getName()) + ".png", outputDirectory);
        pr.configure(ConfigurationConverter.getConfiguration(systemProperties));
        ICompletionService<JFreeChart> ics = new CompletionServiceFactory<JFreeChart>().newLocalCompletionService();
        ics.submit(pr);
        Collection<WorkflowResult> results = new ArrayList<>();
        try {
            ics.call();
            results.add(new WorkflowResult(pr.getFile().toURI(), TICPeakFinder.class.getCanonicalName(), WorkflowSlot.VISUALIZATION, new URI[]{f.getUri()}));
        } catch (Exception ex) {
            log.warn("{}", ex);
        }
        return results;
    }

}
