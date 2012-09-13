/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
package maltcms.experimental.bipace.datastructures.api;

import lombok.Data;
import maltcms.datastructures.peak.Peak;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
public abstract class CliqueStatistics<T extends Peak> {

    private double[] cliqueMean;
    private double[] cliqueVariance;
    private T centroid;
    private Clique<T> clique;

    public abstract void selectCentroid();

    public abstract double getDistanceToCentroid(T p);

    public abstract void update();

    public abstract double[] getCliqueVariance();

    public abstract double[] getCliqueMean();

    public abstract String[] getFeatureNames();

    public int compareDraw(T t1, T t2) {
        double p1 = getDistanceToCentroid(t1);
        double q2 = getDistanceToCentroid(t2);
        if (p1 < q2) {
            return -1;
        } else if (p1 > q2) {
            return 1;
        }
        return 0;
    }

    public int compareCliques(Clique<T> c1, Clique<T> c2) {
        double[] mean1 = c1.getCliqueStatistics().getCliqueMean();
        double[] mean2 = c2.getCliqueStatistics().getCliqueMean();
        for (int j = 0; j < mean1.length; j++) {
            if (mean1[j] < mean2[j]) {
                return -1;
            } else if (mean1[j] > mean2[j]) {
                return 1;
            }
        }
        return 0;
    }
//    String groupFileLocation = Factory.getInstance().getConfiguration().
//                getString("groupFileLocation", "");
//
////        OneWayPeakAnova owa = new OneWayPeakAnova();
////        owa.setWorkflow(getWorkflow());
////        owa.calcFisherRatios(l, al, groupFileLocation);
//
//        if (this.savePlots) {
//
//            DefaultBoxAndWhiskerCategoryDataset dscdRT = new DefaultBoxAndWhiskerCategoryDataset();
//            for (Clique<T> c : l) {
//                dscdRT.add(c.createRTBoxAndWhisker(), "", c.getCliqueRTMean());
//            }
//            JFreeChart jfc = ChartFactory.createBoxAndWhiskerChart("Cliques",
//                    "clique mean RT", "RT diff to centroid", dscdRT, true);
//            jfc.getCategoryPlot().setOrientation(PlotOrientation.HORIZONTAL);
//            PlotRunner pr = new PlotRunner(jfc.getCategoryPlot(),
//                    "Clique RT diff to centroid", "cliquesRTdiffToCentroid.png",
//                    getWorkflow().getOutputDirectory(this));
//            pr.configure(Factory.getInstance().getConfiguration());
//            final File f = pr.getFile();
//            final DefaultWorkflowResult dwr = new DefaultWorkflowResult(f, this,
//                    WorkflowSlot.VISUALIZATION,
//                    al.toArray(new IFileFragment[]{}));
//            getWorkflow().append(dwr);
//            Factory.getInstance().submitJob(pr);
//
//            DefaultBoxAndWhiskerCategoryDataset dscdTIC = new DefaultBoxAndWhiskerCategoryDataset();
//            for (Clique<T> c : l) {
//                dscdTIC.add(c.createApexTicBoxAndWhisker(), "", c.
//                        getCliqueRTMean());
//            }
//            JFreeChart jfc2 = ChartFactory.createBoxAndWhiskerChart("Cliques",
//                    "clique mean RT", "log(apex TIC centroid)-log(apex TIC)",
//                    dscdTIC, true);
//            jfc2.getCategoryPlot().setOrientation(PlotOrientation.HORIZONTAL);
//            PlotRunner pr2 = new PlotRunner(jfc2.getCategoryPlot(),
//                    "Clique log apex TIC centroid diff to log apex TIC",
//                    "cliquesLogApexTICCentroidDiffToLogApexTIC.png",
//                    getWorkflow().getOutputDirectory(this));
//            pr.configure(Factory.getInstance().getConfiguration());
//            final File g = pr.getFile();
//            final DefaultWorkflowResult dwr2 = new DefaultWorkflowResult(g, this,
//                    WorkflowSlot.VISUALIZATION,
//                    al.toArray(new IFileFragment[]{}));
//            getWorkflow().append(dwr2);
//            Factory.getInstance().submitJob(pr2);
//        }
}
