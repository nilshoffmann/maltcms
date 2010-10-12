/**
 * 
 */
package maltcms.commands.fragments.assignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import maltcms.datastructures.ms.IMetabolite;
import maltcms.db.MetaboliteQueryDB;
import maltcms.db.QueryCallable;
import maltcms.db.predicates.metabolite.MScanSimilarityPredicate;
import maltcms.db.similarities.MetaboliteSimilarity;
import maltcms.io.csv.CSVWriter;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;

import com.db4o.ObjectSet;

import cross.Logging;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
@RequiresVariables(names = { "var.mass_values", "var.intensity_values",
        "var.scan_index", "var.scan_acquisition_time", "var.tic_peaks" })
public class EIMSDBMetaboliteAssignment extends AFragmentCommand {

	private String dblocation;
	private double threshold = 0.9;

	private Logger log = Logging.getLogger(this);
	private int maxk = 5;

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.fragments.AFragmentCommand#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Tries to automatically assign peaks to metabolites in a database.";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.ICommand#apply(java.lang.Object)FIXME this is all
	 * preliminary
	 */
	@Override
	public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
		int k = this.maxk;
		for (IFileFragment iff : t) {
			IVariableFragment index = iff.getChild("scan_index");
			IVariableFragment mv = iff.getChild("mass_values");
			mv.setIndex(index);
			IVariableFragment iv = iff.getChild("intensity_values");
			IVariableFragment si = iff.getChild("scan_acquisition_time");
			Array sia = si.getArray();
			Index siai = sia.getIndex();
			iv.setIndex(index);
			List<Array> ints = iv.getIndexedArray();
			List<Array> masses = mv.getIndexedArray();
			ArrayList<List<String>> table = new ArrayList<List<String>>();
			List<String> header = Arrays.asList(new String[] { "ScanNumber",
			        "RetentionTime", "Score", "RI", "Formula", "ID" });
			table.add(header);
			ArrayInt.D1 peaks = (ArrayInt.D1) iff.getChild("tic_peaks")
			        .getArray();
			for (int i = 0; i < peaks.getShape()[0]; i++) {
				MScanSimilarityPredicate ssp = null;
				int scan = (int) peaks.get(i);
				this.log.info("Scan {}", scan);
				Tuple2D<Array, Array> query = new Tuple2D<Array, Array>(masses
				        .get(scan), ints.get(scan));
				ssp = new MScanSimilarityPredicate(query);
				ssp.setThreshold(this.threshold);
				MetaboliteQueryDB mqdb = new MetaboliteQueryDB(this.dblocation,
				        ssp);
				QueryCallable<IMetabolite> qc = mqdb.getCallable();
				ObjectSet<IMetabolite> osRes = null;
				try {
					osRes = qc.call();
					log.info("Received {} hits from ObjectSet!", osRes.size());
					// qc.terminate();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				List<Tuple2D<Double, IMetabolite>> l = new ArrayList<Tuple2D<Double, IMetabolite>>();// ((MScanSimilarityPredicate)ssp).getSimilaritiesAboveThreshold();
				MetaboliteSimilarity ms = new MetaboliteSimilarity();
				for (IMetabolite im : osRes) {
					l.add(new Tuple2D<Double, IMetabolite>(ms.get(query, im),
					        im));
				}
				qc.terminate();
				Comparator<Tuple2D<Double, IMetabolite>> comp = new Comparator<Tuple2D<Double, IMetabolite>>() {

					@Override
					public int compare(Tuple2D<Double, IMetabolite> o1,
					        Tuple2D<Double, IMetabolite> o2) {
						return o1.getFirst().compareTo(o2.getFirst());
					}
				};
				Collections.sort(l, Collections.reverseOrder(comp));
				List<Tuple2D<Double, IMetabolite>> hits = l.subList(0, Math
				        .min(l.size(), k));
				System.out.println("Adding top " + hits.size() + " hits!");
				for (Tuple2D<Double, IMetabolite> tuple2D : hits) {
					IMetabolite im = tuple2D.getSecond();
					List<String> row = Arrays.asList(new String[] {
					        "" + (scan), "" + sia.getDouble(siai.set(scan)),
					        "" + (int) (tuple2D.getFirst() * 1000.0),
					        "" + im.getRetentionIndex(), "" + im.getFormula(),
					        im.getID(), });
					table.add(row);
				}
			}
			if (table.size() > 1) {
				CSVWriter csvw = new CSVWriter();
				csvw.setIWorkflow(getIWorkflow());
				csvw.writeTableByRows(getIWorkflow().getOutputDirectory(this)
				        .getAbsolutePath(), StringTools.removeFileExt(iff
				        .getName())
				        + "_peak_assignment.csv", table,
				        WorkflowSlot.IDENTIFICATION);
			} else {
				log.warn("No matches found!");
			}
		}
		return t;
	}

	@Override
	public void configure(Configuration cfg) {
		super.configure(cfg);
		this.dblocation = cfg.getString("metabolite.db");
		this.threshold = cfg.getDouble(
		        this.getClass().getName() + ".threshold", 0.9);
		this.maxk = cfg.getInt(this.getClass().getName() + ".maxk", 5);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
	 */
	@Override
	public WorkflowSlot getWorkflowSlot() {
		return WorkflowSlot.IDENTIFICATION;
	}

}
