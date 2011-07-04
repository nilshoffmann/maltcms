/**
 * 
 */
package maltcms.commands.fragments.visualization;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import maltcms.datastructures.ms.IAnchor;
import maltcms.tools.ImageTools;
import maltcms.tools.MaltcmsTools;
import maltcms.ui.charts.AChart;
import maltcms.ui.charts.PlotRunner;
import maltcms.ui.charts.XYChart;

import org.apache.commons.configuration.Configuration;
import org.jfree.chart.plot.XYPlot;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;
import cross.Factory;
import cross.Logging;
import cross.annotations.Configurable;
import cross.annotations.RequiresVariables;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import cross.datastructures.tools.EvalTools;
import cross.tools.StringTools;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
@RequiresVariables(names = { "var.total_intensity", "var.scan_acquisition_time" })
public class EICHeatmapCoplot extends TICHeatmapCoplot {

	private Logger log = Logging.getLogger(this);

	@Configurable
	private List<String> drawEICs;

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.fragments.AFragmentCommand#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Generates a stacked heatmap plot of EICs (bird's eye view) with shared time axis";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.ICommand#apply(java.lang.Object)
	 */
	@Override
	public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
		final File[] files = drawEICs(getWorkflow().getOutputDirectory(this),
		        t, Factory.getInstance().getConfiguration().getString(
		                "var.scan_acquisition_time", "scan_acquisition_time"),
		        null, "");
		for (final File file : files) {
			final DefaultWorkflowResult dwrut = new DefaultWorkflowResult(file,
			        this, getWorkflowSlot(), t.toArray(new IFileFragment[] {}));
			getWorkflow().append(dwrut);
		}
		return t;
	}

	private File[] drawEICs(final File outputDir,
	        final TupleND<IFileFragment> t, final String satVar,
	        final IFileFragment ref, final double[] eics, final double binsize,
	        final String filePrefix) {
		if (t.isEmpty()) {
			return new File[] {};
		}
		final String[] labels = new String[t.size()];
		final File[] returnFiles = new File[2 * eics.length];
		int retFileCnt = 0;
		int j = 0;
		final List<List<IAnchor>> allLabels = new ArrayList<List<IAnchor>>();
		int nanchors = 0;
		try {
			for (final IFileFragment iff : t) {
				labels[j] = t.get(j).getName();
				j++;
				for (int i = 0; i < eics.length; i++) {
					final IVariableFragment ivf = new VariableFragment(iff,
					        filePrefix + "_eic_" + eics[i]);
					log.debug("Adding eic var {}", ivf.getVarname());
					log.debug("to {}", iff.toString());
					final Array eic = MaltcmsTools.getEIC(iff, eics[i], eics[i]
					        + binsize, true, false);
					log.debug("{}", eic);
					EvalTools.notNull(eic, ImageTools.class);
					ivf.setArray(eic);
				}
				final List<IAnchor> l = MaltcmsTools.prepareAnchors(iff);
				if (l != null) {
					nanchors += l.size();
					allLabels.add(l);
				}
				// workFrag.add(workFileFrag);
			}
			// for each eic
			for (int i = 0; i < eics.length; i++) {
				// prepare arrays and domain arrays according to number of
				// FileFragments
				final Array[] arrays = new Array[t.size()];
				final Array[] domains = new Array[t.size()];
				// create label positions array for total number of anchors
				final ArrayDouble.D1 labelPos = new ArrayDouble.D1(nanchors);
				final ArrayDouble.D1 labelVal = new ArrayDouble.D1(nanchors);
				// create array for names of anchors
				final String[] labelNames = new String[nanchors];
				int offset = 0;
				// for each FileFragment
				for (j = 0; j < t.size(); j++) {
					log.debug("Trying to read {} from {}", filePrefix + "_eic_"
					        + eics[i], t.get(j).toString());
					arrays[j] = t.get(j).getChild(
					        filePrefix + "_eic_" + eics[i]).getArray();
					log.debug("{}", arrays[j]);
					domains[j] = t.get(j).getChild(satVar).getArray();
					// get the corresponding anchors
					final List<IAnchor> l = allLabels.get(j);
					// get the corresponding scan acquisition time array
					final Array sat = domains[j];
					// get the index to access elements in array
					final Index satI = sat.getIndex();
					final Index valI = arrays[j].getIndex();
					// global anchor counter
					int cnt = 0;
					// for each anchor in anchor list for FileFragment k
					for (final IAnchor ia : l) {
						// set label position at position offset+cnt
						// to value of scan_acquisition_time at the given scan
						// index
						labelPos.set(offset + cnt, sat.getDouble(satI.set(ia
						        .getScanIndex())));
						labelVal.set(offset + cnt, arrays[j].getDouble(valI
						        .set(ia.getScanIndex())));
						// set name
						labelNames[offset + cnt] = ia.getName();
						// update counter
						cnt++;
					}
					offset += l.size();

				}
				final AChart<XYPlot> xyc = new XYChart("Plot of EIC "
				        + filePrefix + " " + eics[i], labels, arrays, domains,
				        labelPos, labelVal, labelNames, satVar + " [s]",
				        "Intensity");
				final PlotRunner pr = new PlotRunner(xyc.create(),
				        "Plot of EIC " + eics[i], filePrefix + "_jfc_eic_"
				                + eics[i] + ".png", outputDir);
				pr.configure(Factory.getInstance().getConfiguration());
				final File f = pr.getFile();
				returnFiles[retFileCnt++] = f;
				Factory.getInstance().submitJob(pr);
				returnFiles[retFileCnt++] = drawTICS(outputDir,
				        new TupleND<IFileFragment>(t), filePrefix + "_eic_"
				                + eics[i], satVar, ref, filePrefix + "_eic_"
				                + eics[i] + ".png");
			}
		} catch (final ResourceNotAvailableException rnae) {
			log.warn("Could not load resource: {}", rnae);
			return new File[] {};
		}
		return returnFiles;
	}

	private File[] drawEICs(final File outputDir,
	        final TupleND<IFileFragment> t, final String satVar,
	        final IFileFragment ref, final String filePrefix) {
		if (t.isEmpty()) {
			return new File[] {};
		}
		final List<String> l = StringTools.toStringList(this.drawEICs);
		final double[] d = new double[l.size()];
		for (int i = 0; i < l.size(); i++) {
			d[i] = Double.parseDouble(l.get(i));
		}
		final double binsize = Factory.getInstance().getConfiguration()
		        .getDouble(ImageTools.class.getName() + ".eicBinSize", 1.0d);
		return drawEICs(outputDir, t, satVar, ref, d, binsize, filePrefix);
	}

	@Override
	public void configure(Configuration cfg) {
		this.drawEICs = StringTools.toStringList(cfg.getList(this.getClass()
		        .getName()
		        + ".drawEICs", Arrays.asList(new String[] { "117.0", "217.0",
		        "268.0", "313.0", "350.0" })));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
	 */
	@Override
	public WorkflowSlot getWorkflowSlot() {
		return WorkflowSlot.VISUALIZATION;
	}

}
