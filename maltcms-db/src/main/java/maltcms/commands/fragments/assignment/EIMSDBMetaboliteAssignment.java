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
package maltcms.commands.fragments.assignment;

/**
 *
 */
import com.db4o.ObjectSet;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.ms.ChromatogramFactory;
import maltcms.datastructures.ms.IChromatogram1D;
import maltcms.datastructures.ms.IMetabolite;
import maltcms.datastructures.ms.IScan1D;
import maltcms.db.MetaboliteQueryDB;
import maltcms.db.QueryCallable;
import maltcms.db.predicates.metabolite.MetaboliteSimilarity;
import maltcms.io.csv.CSVWriter;
import maltcms.math.functions.IArraySimilarity;
import maltcms.math.functions.similarities.ArrayCos;
import org.apache.commons.configuration.Configuration;
import ucar.ma2.Array;

/**
 * <p>EIMSDBMetaboliteAssignment class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@RequiresVariables(names = {"var.mass_values", "var.intensity_values",
    "var.scan_index", "var.scan_acquisition_time"})
@RequiresOptionalVariables(names = {"var.tic_peaks", "var.peak_index_list", "var.eic_peaks"})
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class EIMSDBMetaboliteAssignment extends AFragmentCommand {

    private List<String> dblocation;
    private double threshold = 0.9;
    private int maxk = 5;
    private IArraySimilarity similarityFunction = new ArrayCos();
    private double massResolution = 1.0d;
    private String peakIndexVariable = "var.tic_peaks";

    /*
     * (non-Javadoc)
     *
     * @see cross.commands.fragments.AFragmentCommand#getDescription()
     */
    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Putative identification of EI-MS peaks for each chromatogram against the given databases.";
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.commands.ICommand#apply(java.lang.Object)FIXME this is all
     * preliminary
     */
    /** {@inheritDoc} */
    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
        ChromatogramFactory cf = new ChromatogramFactory();
        for (IFileFragment iff : t) {
            IChromatogram1D chrom = cf.createChromatogram1D(iff);
            List<String> header = Arrays.asList(new String[]{"ScanNumber",
                "RetentionTime", "Score", "RI", "MW", "Name", "Formula", "ID", "Native-ID",
                "SOURCE", "SimilarityFunction", "SourceVariable"});
            CSVWriter csvw = new CSVWriter();
            csvw.setWorkflow(getWorkflow());
            try (PrintWriter pw = csvw.createPrintWriter(getWorkflow().
                    getOutputDirectory(this).getAbsolutePath(), StringTools.
                    removeFileExt(iff.getName())
                    + "_peak_assignment.csv", header,
                    WorkflowSlot.IDENTIFICATION)) {
                Array peaks = iff.getChild(resolve(peakIndexVariable)).getArray();
                for (String dbloc : this.dblocation) {
                    for (int i = 0; i < peaks.getShape()[0]; i++) {
                        int scan = (int) peaks.getInt(i);
                        this.log.info("Scan {}", scan);
                        IScan1D scan1D = chrom.getScan(scan);
                        MetaboliteSimilarity ms = new MetaboliteSimilarity(scan1D,
                                threshold, maxk, false);
                        ms.setSimilarityFunction(similarityFunction.copy());
                        ms.setResolution(massResolution);
                        MetaboliteQueryDB mqdb = new MetaboliteQueryDB(dbloc, ms);
                        QueryCallable<IMetabolite> qc = mqdb.getCallable();
                        ObjectSet<IMetabolite> osRes = null;
                        try {
                            osRes = qc.call();
                            log.info("Received {} hits from ObjectSet!",
                                    osRes.size());
                            qc.terminate();
                        } catch (InterruptedException e) {
                            log.warn(e.getLocalizedMessage());
                        } catch (ExecutionException e) {
                            log.warn(e.getLocalizedMessage());
                        } catch (Exception e) {
                            log.warn(e.getLocalizedMessage());
                        }
                        List<Tuple2D<Double, IMetabolite>> l = ms.getMatches();
                        log.info("Adding top {} hits!", l.size());
                        for (Tuple2D<Double, IMetabolite> tuple2D : l) {
                            IMetabolite im = tuple2D.getSecond();
                            List<String> row = Arrays.asList(
                                    new String[]{
                                        "" + (scan),
                                        "" + scan1D.getScanAcquisitionTime(),
                                        "" + (int) (tuple2D.getFirst() * 1000.0),
                                        "" + im.getRetentionIndex(), "" + im.getMW(),
                                        "" + im.getFormula(), "" + im.getName(), im.getID(),
                                        "" + im.getLink(), "" + dbloc,
                                        similarityFunction.getClass().getSimpleName().substring("Array".length()),
                                        "" + resolve(peakIndexVariable)});
                            csvw.writeLine(pw, row);
                        }
                        if (i % 100 == 0) {
                            pw.flush();
                        }
                    }
                }
            }
        }
        return t;
    }

    /** {@inheritDoc} */
    @Override
    public void configure(Configuration cfg) {
        super.configure(cfg);
//		this.dblocation = StringTools.toStringList(cfg.getList("metabolite.db"));
//		this.threshold = cfg.getDouble(
//			this.getClass().getName() + ".threshold", 0.9);
//		this.maxk = cfg.getInt(this.getClass().getName() + ".maxk", 5);
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    /** {@inheritDoc} */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.IDENTIFICATION;
    }
}
