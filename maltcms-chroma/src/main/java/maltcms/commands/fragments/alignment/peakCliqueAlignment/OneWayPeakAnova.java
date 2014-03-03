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
package maltcms.commands.fragments.alignment.peakCliqueAlignment;

import cross.Factory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.MathTools;
import cross.tools.StringTools;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Vector;
import lombok.extern.slf4j.Slf4j;
import maltcms.io.csv.CSVReader;
import maltcms.tools.ArrayTools;
import maltcms.ui.charts.PlotRunner;
import net.sf.mpaxs.api.ICompletionService;
import net.sf.mpaxs.spi.concurrent.CompletionServiceFactory;
import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.inference.OneWayAnova;
import org.apache.commons.math.stat.inference.TestUtils;
import org.jdom.Element;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYBarDataset;

/**
 * @author Nils Hoffmann
 *
 *
 */
@Slf4j
public class OneWayPeakAnova implements IWorkflowElement {

    private IWorkflow workflow;

    private HashMap<String, HashSet<IBipacePeak>> getClasses(Clique<IBipacePeak> c,
        HashMap<IFileFragment, String> fileToClass) {
        HashMap<String, HashSet<IBipacePeak>> classToPeak = new HashMap<String, HashSet<IBipacePeak>>();
        for (IBipacePeak p : c.getPeakList()) {
            final String classAssoc = fileToClass.get(p.getAssociation());
            HashSet<IBipacePeak> hs = null;
            if (classToPeak.containsKey(classAssoc)) {
                hs = classToPeak.get(classAssoc);
            } else {
                hs = new HashSet<IBipacePeak>();
                classToPeak.put(classAssoc, hs);
            }
            hs.add(p);
        }
        return classToPeak;
    }

    private double[] getIntensityValuesForPeaks(Collection<IBipacePeak> s) {
        if (s == null || s.isEmpty() || s.size() < 2) {
            return new double[]{};
        }
        double[] d = new double[s.size()];
        int i = 0;
        for (IBipacePeak p : s) {
            d[i++] = ArrayTools.integrate(p.getMsIntensities());
        }
        return d;
    }

    private double calcMeanForPeaks(Collection<IBipacePeak> s) {
        if (s == null || s.isEmpty()) {
            return 0.0d;
        }
        double[] values = new double[s.size()];
        int i = 0;
        for (IBipacePeak p : s) {
            values[i++] = ArrayTools.integrate(p.getMsIntensities());
        }
        return MathTools.average(values, 0, values.length - 1);
    }

    public void calcFisherRatios(Collection<Clique<IBipacePeak>> c,
        Collection<IFileFragment> f, String groupFileLocation) {
        if (groupFileLocation.isEmpty()) {
            log.warn("No group file given!");
            return;
        }
        // Read group information
        CSVReader csvr = new CSVReader();
        Tuple2D<Vector<Vector<String>>, Vector<String>> v = csvr
            .read(groupFileLocation);
        HashMap<String, IFileFragment> fileToShortFile = new HashMap<String, IFileFragment>();
        for (IFileFragment frag : f) {
            fileToShortFile
                .put(StringTools.removeFileExt(frag.getName()), frag);
        }

        // map from file to group/class
        HashMap<IFileFragment, String> fileToClass = new HashMap<IFileFragment, String>();
        for (Vector<String> line : v.getFirst()) {
            if (line.size() > 1 && !line.isEmpty()) {
                log.debug("line: {}", line);
                fileToClass.put(fileToShortFile.get(line.get(0)), line.get(1));
            }
        }

        double[][] fRatio = new double[2][c.size()];
        double[][] pValue = new double[2][c.size()];
        int i = 0;
        for (Clique<IBipacePeak> clq : c) {
            log.debug("RTMean of clique: {}", clq.getCliqueRTMean());
            fRatio[0][i] = clq.getCliqueRTMean();
            pValue[0][i] = clq.getCliqueRTMean();
            double[] vals = calcFisherRatio(fileToClass, clq);
            fRatio[1][i] = vals[0];
            pValue[1][i] = vals[1];
            i++;
        }
        DefaultXYDataset fxyds = new DefaultXYDataset();
        fxyds.addSeries("fisher ratios", fRatio);

        XYBarDataset dscdRT = new XYBarDataset(fxyds, 10.0);
        JFreeChart jfc = ChartFactory.createXYBarChart(
            "Fisher ratios for peak cliques", "clique median RT", false,
            "Fisher ratio", dscdRT, PlotOrientation.VERTICAL, true, true,
            true);
        customizeBarChart(jfc);
        PlotRunner pr = new PlotRunner(jfc.getXYPlot(), "Clique fisher ratios",
            "cliquesFisherRatios.png", getWorkflow().getOutputDirectory(
                this));
        pr.configure(Factory.getInstance().getConfiguration());
        final File file = pr.getFile();
        final DefaultWorkflowResult dwr = new DefaultWorkflowResult(file, this,
            WorkflowSlot.VISUALIZATION, f.toArray(new IFileFragment[]{}));
        getWorkflow().append(dwr);
        ICompletionService<JFreeChart> ics = new CompletionServiceFactory<JFreeChart>().newLocalCompletionService();
        ics.submit(pr);

        DefaultXYDataset pxyds = new DefaultXYDataset();
        pxyds.addSeries("p-values", pValue);
        XYBarDataset dscdRT2 = new XYBarDataset(pxyds, 10.0);
        JFreeChart jfc2 = ChartFactory.createXYBarChart(
            "p-values for peak cliques", "clique median RT", false,
            "p-value", dscdRT2, PlotOrientation.VERTICAL, true, true, true);
        customizeBarChart(jfc2);
        PlotRunner pr2 = new PlotRunner(jfc2.getXYPlot(), "Clique p-values",
            "cliquesPValues.png", getWorkflow().getOutputDirectory(this));
        pr2.configure(Factory.getInstance().getConfiguration());
        final File file2 = pr2.getFile();
        final DefaultWorkflowResult dwr2 = new DefaultWorkflowResult(file2,
            this, WorkflowSlot.VISUALIZATION, f
            .toArray(new IFileFragment[]{}));
        getWorkflow().append(dwr2);
        ics.submit(pr2);
        try {
            ics.call();
        } catch (Exception ex) {
            log.warn("Caught exception while waiting for results!", ex);
        }
    }

    private void customizeBarChart(JFreeChart jfc) {
        XYBarRenderer xybr = (XYBarRenderer) jfc.getXYPlot().getRenderer();
        xybr.setDrawBarOutline(false);
        xybr.setShadowVisible(false);
        xybr.setBarPainter(new StandardXYBarPainter());
    }

    private double[] calcFisherRatio(
        HashMap<IFileFragment, String> fileToClass, Clique<IBipacePeak> c) {
        // double cliqueMean = calcMeanForPeaks(c.getPeakList());
        LinkedHashSet<String> classes = new LinkedHashSet<String>();
        for (String cls : fileToClass.values()) {
            classes.add(cls);
        }
        log.debug("Classes: {}", classes);
        // double[] clsMeans = new double[classes.size()];
        HashMap<String, HashSet<IBipacePeak>> classToPeaks = getClasses(c, fileToClass);
        log.debug("Class to Peaks: {}", classToPeaks);
        Collection<double[]> groupValues = new ArrayList<double[]>();
        // int i = 0;
        for (String cls : classes) {
            // log.debug("Calculating mean for class {}", cls);
            // clsMeans[i++] = calcMeanForPeaks(classToPeaks.get(cls));
            double[] gvals = getIntensityValuesForPeaks(classToPeaks.get(cls));
            if (gvals.length > 0) {
                groupValues.add(gvals);
            } else {
                log.warn("Skipping group " + cls + " for clique at "
                    + c.getCliqueRTMean()
                    + "! Not enough peaks, need at least two!");
            }
        }
        // List<double[]> clsValues = new ArrayList<double[]>();
        // for (String cls : classes) {
        // clsValues.add(getIntensityValuesForPeaks(classToPeaks.get(cls)));
        // }
        //
        // double N = fileToClass.keySet().size();
        // double k = classes.size();
        //
        // // average within class variation
        // double interClsVariation = calculateInterClassVariation(cliqueMean,
        // classes, clsMeans, classToPeaks);
        // double sigmaSqCls = interClsVariation / (k - 1);
        //
        // double sigmaSqErr = calculateIntraClassVariation(classes,
        // classToPeaks,
        // cliqueMean)
        // / (N - k);
        // double fisherRatio = sigmaSqCls / sigmaSqErr;
        // System.out.println("Fisher ratio for clique: " + fisherRatio);
        double alpha = 0.01;
        OneWayAnova owa = TestUtils.getOneWayAnova();
        // FDistribution fdist = new FDistributionImpl(k - 1, N - k);
        double fisherRatio = Double.NaN;
        double pvalue = Double.NaN;
        try {
            // double pval = 1.0d - fdist.cumulativeProbability(fisherRatio);
            // System.out.println("p-value for clique: " + pval);
            // if (pval < alpha) {
            // System.out
            // .println("Rejected Null hypothesis at significance level "
            // + alpha);
            // } else {
            // System.out
            // .println("Accepted Null hypothesis, group means are not significantly different at level "
            // + alpha);
            // }
            fisherRatio = owa.anovaFValue(groupValues);
            System.out.println("F-ratio: " + fisherRatio);
            pvalue = owa.anovaPValue(groupValues);
            System.out.println("p-value: " + pvalue);
            if (owa.anovaTest(groupValues, alpha)) {
                System.out
                    .println("Null hypthesis rejected, group variances exceed given significance level of "
                        + alpha);
            } else {
                pvalue = Double.NaN;
                fisherRatio = Double.NaN;
            }
        } catch (MathException me) {
            log.warn(me.getLocalizedMessage());
        }
        return new double[]{fisherRatio, pvalue};
    }

    /**
     * @param classes
     * @param classToPeaks
     * @param cliqueMean
     * @return
     */
    private double calculateIntraClassVariation(LinkedHashSet<String> classes,
        HashMap<String, HashSet<IBipacePeak>> classToPeaks, double cliqueMean) {
        double intraClsVariation = 0;
        for (String cls : classes) {
            // double intraClassMean = calcMeanForPeaks(classToPeaks.get(cls));
            if (classToPeaks.containsKey(cls)) {
                for (IBipacePeak p : classToPeaks.get(cls)) {
                    intraClsVariation += Math.pow(ArrayTools.integrate(p
                        .getMsIntensities())
                        - cliqueMean, 2);
                }
            }
        }
        log.debug("Intra class variance: {}", intraClsVariation);
        return intraClsVariation;
    }

    /**
     * @param cliqueMean
     * @param classes
     * @param clsMeans
     * @param classToPeaks
     * @return
     */
    private double calculateInterClassVariation(double cliqueMean,
        LinkedHashSet<String> classes, double[] clsMeans,
        HashMap<String, HashSet<IBipacePeak>> classToPeaks) {
        int i;
        double sigmaSqCls = 0;
        i = 0;
        for (String cls : classes) {
            if (classToPeaks.containsKey(cls)) {
                sigmaSqCls += (Math.pow((clsMeans[i] - cliqueMean), 2) * classToPeaks
                    .get(cls).size());
            }
            i++;
        }
        log.debug("Inter class variance: {}", sigmaSqCls);
        return sigmaSqCls;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflow()
     */
    @Override
    public IWorkflow getWorkflow() {
        return this.workflow;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.STATISTICS;
    }

    /*
     * (non-Javadoc)
     *
     * @seecross.datastructures.workflow.IWorkflowElement#setWorkflow(cross.
     * datastructures.workflow.IWorkflow)
     */
    @Override
    public void setWorkflow(IWorkflow iw) {
        this.workflow = iw;

    }

    /*
     * (non-Javadoc)
     *
     * @see cross.io.xml.IXMLSerializable#appendXML(org.jdom.Element)
     */
    @Override
    public void appendXML(Element e) {
        // TODO Auto-generated method stub
    }
}
