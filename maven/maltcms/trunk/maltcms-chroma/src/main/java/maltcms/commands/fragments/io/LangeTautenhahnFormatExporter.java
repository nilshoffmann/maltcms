/**
 * 
 */
package maltcms.commands.fragments.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import maltcms.datastructures.peak.Clique;
import maltcms.datastructures.peak.Peak;
import maltcms.io.csv.CSVWriter;
import maltcms.statistics.OneWayPeakAnova;
import maltcms.tools.MaltcmsTools;
import maltcms.ui.charts.PlotRunner;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.Index;
import cross.Factory;
import cross.Logging;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
@Slf4j
@Data
public class LangeTautenhahnFormatExporter extends AFragmentCommand {

    /*
     * (non-Javadoc)
     * 
     * @see cross.commands.fragments.AFragmentCommand#getDescription()
     */
    @Override
    public String getDescription() {
        return "Creates a data table compatible to that featured in Lange et al. Critical assessment of alignment procedures for LC-MS proteomics and metabolomics measurements. BMC Bioinformatics (2008) vol. 9 pp. 375";
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.ICommand#apply(java.lang.Object)
	 */
	@Override
	public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
		buildCliques(t);
		// intensity rt m/z
		final List<List<String>> rows = new ArrayList<List<String>>(t.size());
		final IFileFragment ref = t.get(0);
		final IVariableFragment refSindex = ref.getChild("scan_index");
		final Array scanIndex = refSindex.getArray();
		for (int i = 0; i < scanIndex.getShape()[0]; i++) {
			final int si = i;
			final List<String> line = new ArrayList<String>(t.size() * 3);
			int j = 0;
			boolean skipline = false;
			for (final IFileFragment alf : t) {
				final IVariableFragment sindex = alf.getChild("scan_index");
				final IVariableFragment sat = alf
				        .getChild("scan_acquisition_time");
				final IVariableFragment masses = alf.getChild("mass_values");
				masses.setIndex(sindex);
				final IVariableFragment intensities = alf
				        .getChild("intensity_values");
				intensities.setIndex(sindex);
				final Array satArray = sat.getArray();
				final Index satIndex = satArray.getIndex();
				final int pos = j * 3;
				if (pos >= 0) {
					// line[pos] = iff.getName();
					log.debug("Reading scan {}", si);
					Array massesA = masses.getIndexedArray().get(si);
					Array intensA = intensities.getIndexedArray().get(si);
					if (intensA.getShape()[0] != 0) {
						line
						        .add(MaltcmsTools.getMaxMassIntensity(intensA)
						                + "");
						line.add(satArray.getDouble(satIndex.set(si)) + "");
						line
						        .add(MaltcmsTools.getMaxMass(massesA, intensA)
						                + "");
					} else {
						log
						        .warn(
						                "Skipping alignment of empty array after filtering masked indices in file {}",
						                alf.getName());
						skipline = true;
					}
				}
				j++;
			}
			if (!skipline) {
				log.debug("Adding row {}", line);
				rows.add(line);
			} else {
				log.warn("Skipping row {}", line);
			}

        }

        final CSVWriter csvw = new CSVWriter();
        csvw.setWorkflow(getWorkflow());
        csvw.writeTableByRows(getWorkflow().getOutputDirectory(this).
                getAbsolutePath(), "matched_features.csv", rows,
                WorkflowSlot.ALIGNMENT);

        return t;
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.FILEIO;
    }

    /**
     * @param al
     * @param fragmentToPeaks
     * @param ll
     * @param minCliqueSize
     * @return a list of clique objects
     */
    private List<Clique> combineBiDiBestHits(final TupleND<IFileFragment> al) {
        List<Clique> l = buildCliques(al);
        OneWayPeakAnova owa = new OneWayPeakAnova();
        owa.setWorkflow(getWorkflow());
        String groupFileLocation = Factory.getInstance().getConfiguration().
                getString("groupFileLocation", "");
        owa.calcFisherRatios(l, al, groupFileLocation);

        DefaultBoxAndWhiskerCategoryDataset dscdRT = new DefaultBoxAndWhiskerCategoryDataset();
        for (Clique c : l) {
            dscdRT.add(c.createRTBoxAndWhisker(), "", c.getCliqueRTMean());
        }
        JFreeChart jfc = ChartFactory.createBoxAndWhiskerChart("Cliques",
                "clique mean RT", "RT diff to centroid", dscdRT, true);
        jfc.getCategoryPlot().setOrientation(PlotOrientation.HORIZONTAL);
        PlotRunner pr = new PlotRunner(jfc.getCategoryPlot(),
                "Clique RT diff to centroid", "cliquesRTdiffToCentroid.png",
                getWorkflow().getOutputDirectory(this));
        pr.configure(Factory.getInstance().getConfiguration());
        final File f = pr.getFile();
        final DefaultWorkflowResult dwr = new DefaultWorkflowResult(f, this,
                WorkflowSlot.VISUALIZATION, al.toArray(new IFileFragment[]{}));
        getWorkflow().append(dwr);
        Factory.getInstance().submitJob(pr);

        DefaultBoxAndWhiskerCategoryDataset dscdTIC = new DefaultBoxAndWhiskerCategoryDataset();
        for (Clique c : l) {
            dscdTIC.add(c.createApexTicBoxAndWhisker(), "", c.getCliqueRTMean());
        }
        JFreeChart jfc2 = ChartFactory.createBoxAndWhiskerChart("Cliques",
                "clique mean RT", "log(apex TIC centroid)-log(apex TIC)",
                dscdTIC, true);
        jfc2.getCategoryPlot().setOrientation(PlotOrientation.HORIZONTAL);
        PlotRunner pr2 = new PlotRunner(jfc2.getCategoryPlot(),
                "Clique log apex TIC centroid diff to log apex TIC",
                "cliquesLogApexTICCentroidDiffToLogApexTIC.png", getWorkflow().
                getOutputDirectory(this));
        pr.configure(Factory.getInstance().getConfiguration());
        final File g = pr.getFile();
        final DefaultWorkflowResult dwr2 = new DefaultWorkflowResult(g, this,
                WorkflowSlot.VISUALIZATION, al.toArray(new IFileFragment[]{}));
        getWorkflow().append(dwr2);
        Factory.getInstance().submitJob(pr2);

		log.info("Found {} cliques", l.size());
		return l;
	}

	private List<Clique> buildCliques(final TupleND<IFileFragment> al) {
		IFileFragment ref = al.get(0);
		int scans = ref.getChild("scan_index").getArray().getShape()[0];
		List<Clique> cl = new ArrayList<Clique>();
		for (int i = 0; i < scans; i++) {
			List<Peak> row = new ArrayList<Peak>();
			for (IFileFragment iff : al) {
				Peak p = new Peak("", iff, i, MaltcmsTools.getBinnedMS(iff, i)
				        .getSecond(), MaltcmsTools.getScanAcquisitionTime(iff,
				        i));
				row.add(p);
			}
			for (Peak p : row) {
				for (Peak q : row) {
					if (p != q) {
						p.addSimilarity(q, 1.0d);
						q.addSimilarity(p, 1.0d);
					}
				}
			}
			Clique c = new Clique();
			for (Peak p : row) {
				if (!c.addPeak(p)) {
					log.warn("Adding of peak {} to clique {} failed!", p,
					        c);
				}
			}
			cl.add(c);
		}
		return cl;
	}

}
