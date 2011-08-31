package maltcms.statistics;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import maltcms.datastructures.peak.Peak2DClique;
import maltcms.io.csv.CSVReader;
import maltcms.io.csv.CSVWriter;
import maltcms.ui.charts.PlotRunner;

import org.jdom.Element;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYBarDataset;
import org.slf4j.Logger;

import cross.Factory;
import cross.Logging;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;

public class LogDeltaEvaluation implements IWorkflowElement {

    private final Logger log = Logging.getLogger(this);
    private IWorkflow workflow;
    private boolean logNaturalis = true;
    private List<Peak2DClique> peakCliques;

    public void calcRatios(List<Peak2DClique> peaks, Collection<IFileFragment> f) {
        this.peakCliques = peaks;
        final String groupFileLocation = Factory.getInstance().getConfiguration().
                getString("groupFileLocation", "");
        if (groupFileLocation.isEmpty()) {
            this.log.warn("No group file given!");
            return;
        }
        // Read group information
        final CSVReader csvr = new CSVReader();
        final Tuple2D<Vector<Vector<String>>, Vector<String>> v = csvr.read(
                groupFileLocation);
        final HashMap<String, IFileFragment> fileToShortFile = new HashMap<String, IFileFragment>();
        for (IFileFragment frag : f) {
            fileToShortFile.put(StringTools.removeFileExt(frag.getName()), frag);
        }

        // map from file to group/class
        final HashMap<IFileFragment, String> fileToClass = new HashMap<IFileFragment, String>();
        final HashMap<String, List<IFileFragment>> classToFiles = new HashMap<String, List<IFileFragment>>();
        for (Vector<String> line : v.getFirst()) {
            if (line.size() > 1 && !line.isEmpty()) {
                this.log.debug("line: {}", line);
                if (fileToShortFile.keySet().contains(
                        StringTools.removeFileExt(line.get(0)))) {
                    fileToClass.put(fileToShortFile.get(line.get(0)),
                            line.get(1));
                    if (!classToFiles.containsKey(line.get(1))) {
                        // classes.add(line.get(1));
                        classToFiles.put(line.get(1),
                                new ArrayList<IFileFragment>());
                    }
                    classToFiles.get(line.get(1)).add(
                            fileToShortFile.get(line.get(0)));
                }
            }
        }

        List<String> classes = new ArrayList<String>();
        for (String c : classToFiles.keySet()) {
            classes.add(c);
        }

        double mean1, mean2, r;
        int cc1, cc2;
        String c1, c2;
        for (int i = 0; i < classes.size(); i++) {
            for (int j = i + 1; j < classes.size(); j++) {
                c1 = classes.get(i);
                c2 = classes.get(j);
                if (!c1.equals(c2)) {
                    for (Peak2DClique pc : this.peakCliques) {
                        mean1 = 0.0d;
                        mean2 = 0.0d;
                        cc1 = 0;
                        cc2 = 0;
                        for (IFileFragment ff : classToFiles.get(c1)) {
                            try {
                                mean1 += pc.get(ff).getPeakArea().
                                        getAreaIntensity();
                                cc1++;
                            } catch (NullPointerException e) {
                                this.log.info("NULLPOINTER in c1: " + c1 + "-"
                                        + c2);
                            }
                        }
                        for (IFileFragment ff : classToFiles.get(c2)) {
                            try {
                                mean2 += pc.get(ff).getPeakArea().
                                        getAreaIntensity();
                                cc2++;
                            } catch (NullPointerException e) {
                                this.log.info("NULLPOINTER in c2: " + c1 + "-"
                                        + c2);
                            }
                        }
                        if (this.logNaturalis) {
                            r = Math.log(mean1 / cc1) - Math.log(mean2 / cc2);
                        } else {
                            r = Math.log10(mean1 / cc1)
                                    - Math.log10(mean2 / cc2);
                        }
                        pc.addRatio(c1, c2, r);
                    }
                }
            }
        }

        exportRatios(classes, f);
    }

    private void exportRatios(List<String> classes, Collection<IFileFragment> t) {
        List<List<String>> metaTable = new ArrayList<List<String>>();
        List<String> header = new ArrayList<String>();
        header.add("Peak");
        int classCombinations = 0;
        String c1, c2;
        for (int i = 0; i < classes.size(); i++) {
            for (int j = i + 1; j < classes.size(); j++) {
                c1 = classes.get(i);
                c2 = classes.get(j);
                if (!c1.equals(c2)) {
                    header.add(c1 + "-" + c2);
                    classCombinations++;
                }
            }
        }
        metaTable.add(header);
        int i = 0;
        for (Peak2DClique pc : this.peakCliques) {
            List<String> v = new ArrayList<String>();
            v.add(pc.getID());
            metaTable.add(v);
        }

        int c = 0;
        for (int j1 = 0; j1 < classes.size(); j1++) {
            for (int j2 = j1 + 1; j2 < classes.size(); j2++) {
                c1 = classes.get(j1);
                c2 = classes.get(j2);
                if (!c1.equals(c2)) {
                    i = 1;
                    double[][] values = new double[2][this.peakCliques.size()];
                    for (Peak2DClique pc : this.peakCliques) {
                        metaTable.get(i).add(pc.getRatio(c1, c2) + "");
                        values[1][i - 1] = pc.getRatio(c1, c2);
                        values[0][i - 1] = i - 1;
                        i++;
                    }
                    DefaultXYDataset fxyds = new DefaultXYDataset();
                    XYBarDataset dscdRT = new XYBarDataset(fxyds, 1.0);
                    fxyds.addSeries(c1 + " - " + c2, values);
                    JFreeChart jfc = ChartFactory.createXYBarChart(
                            "Log deltas for peak cliques " + c1 + " and " + c2,
                            "Clique number", false, "Log Deltas for " + c1
                            + " and " + c2, dscdRT,
                            PlotOrientation.VERTICAL, true, true, true);
                    customizeBarChart(jfc);
                    PlotRunner pr = new PlotRunner(jfc.getXYPlot(),
                            "Log Deltas", "cliqueslogDelta" + c1 + "-" + c2,
                            getWorkflow().getOutputDirectory(this));
                    pr.configure(Factory.getInstance().getConfiguration());
                    final File file = pr.getFile();
                    final DefaultWorkflowResult dwr = new DefaultWorkflowResult(
                            file, this, WorkflowSlot.VISUALIZATION,
                            t.toArray(new IFileFragment[]{}));
                    getWorkflow().append(dwr);
                    Factory.getInstance().submitJob(pr);
                    c++;
                }
            }
        }

        final CSVWriter csvw = new CSVWriter();
        csvw.setWorkflow(this.workflow);
        csvw.writeTableByRows(this.workflow.getOutputDirectory(this).
                getAbsolutePath(), "classDifferences.csv", metaTable,
                WorkflowSlot.PEAKFINDING);
    }

    private void customizeBarChart(JFreeChart jfc) {
        XYBarRenderer xybr = (XYBarRenderer) jfc.getXYPlot().getRenderer();
        xybr.setDrawBarOutline(false);
        xybr.setShadowVisible(false);
        xybr.setBarPainter(new StandardXYBarPainter());
    }

    @Override
    public IWorkflow getWorkflow() {
        return this.workflow;
    }

    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.STATISTICS;
    }

    @Override
    public void setWorkflow(IWorkflow iw) {
        this.workflow = iw;

    }

    @Override
    public void appendXML(Element e) {
    }
}
