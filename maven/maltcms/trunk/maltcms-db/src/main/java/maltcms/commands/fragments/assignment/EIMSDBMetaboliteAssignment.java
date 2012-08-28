/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package maltcms.commands.fragments.assignment;

/**
 *
 */
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import maltcms.datastructures.ms.IMetabolite;
import maltcms.db.MetaboliteQueryDB;
import maltcms.db.QueryCallable;
import maltcms.io.csv.CSVWriter;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.ArrayInt;

import com.db4o.ObjectSet;

import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.ms.ChromatogramFactory;
import maltcms.datastructures.ms.IChromatogram1D;
import maltcms.datastructures.ms.IScan1D;
import maltcms.db.predicates.metabolite.MetaboliteSimilarity;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 *
 *
 */
@RequiresVariables(names = {"var.mass_values", "var.intensity_values",
    "var.scan_index", "var.scan_acquisition_time", "var.tic_peaks"})
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class EIMSDBMetaboliteAssignment extends AFragmentCommand {

    private List<String> dblocation;
    private double threshold = 0.9;
    private int maxk = 5;

    /*
     * (non-Javadoc)
     * 
     * @see cross.commands.fragments.AFragmentCommand#getDescription()
     */
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
    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
        ChromatogramFactory cf = new ChromatogramFactory();
        for (IFileFragment iff : t) {
            IChromatogram1D chrom = cf.createChromatogram1D(iff);
            List<String> header = Arrays.asList(new String[]{"ScanNumber",
                        "RetentionTime", "Score", "RI", "MW", "Formula", "ID",
                        "SOURCE"});
            CSVWriter csvw = new CSVWriter();
            csvw.setWorkflow(getWorkflow());
            PrintWriter pw = csvw.createPrintWriter(getWorkflow().
                    getOutputDirectory(this).getAbsolutePath(), StringTools.
                    removeFileExt(iff.getName())
                    + "_peak_assignment.csv", header,
                    WorkflowSlot.IDENTIFICATION);
            ArrayInt.D1 peaks = (ArrayInt.D1) iff.getChild("tic_peaks").getArray();
            for (String dbloc : this.dblocation) {
                for (int i = 0; i < peaks.getShape()[0]; i++) {
                    int scan = (int) peaks.get(i);
                    this.log.info("Scan {}", scan);
                    IScan1D scan1D = chrom.getScan(scan);
                    MetaboliteSimilarity ms = new MetaboliteSimilarity(scan1D,
                            threshold, maxk, false);
                    MetaboliteQueryDB mqdb = new MetaboliteQueryDB(dbloc, ms);
                    QueryCallable<IMetabolite> qc = mqdb.getCallable();
                    ObjectSet<IMetabolite> osRes = null;
                    try {
                        osRes = qc.call();
                        log.info("Received {} hits from ObjectSet!",
                                osRes.size());
                        qc.terminate();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    List<Tuple2D<Double, IMetabolite>> l = ms.getMatches();
                    System.out.println("Adding top " + l.size() + " hits!");
                    for (Tuple2D<Double, IMetabolite> tuple2D : l) {
                        IMetabolite im = tuple2D.getSecond();
                        List<String> row = Arrays.asList(
                                new String[]{
                                    "" + (scan),
                                    "" + scan1D.getScanAcquisitionTime(),
                                    "" + (int) (tuple2D.getFirst() * 1000.0),
                                    "" + im.getRetentionIndex(), "" + im.getMW(),
                                    "" + im.getFormula(), im.getID(), "" + dbloc});
                        csvw.writeLine(pw, row);
                    }
                    pw.flush();
                }
            }
            pw.close();
        }
        return t;
    }

    @Override
    public void configure(Configuration cfg) {
        super.configure(cfg);
        this.dblocation = StringTools.toStringList(cfg.getList("metabolite.db"));
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
