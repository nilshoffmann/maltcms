/**
 * 
 */
package maltcms.commands.fragments.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import maltcms.io.csv.CSVReader;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import ucar.ma2.ArrayInt;
import ucar.nc2.Dimension;
import cross.Factory;
import cross.Logging;
import cross.annotations.Configurable;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
@Slf4j
@Data
public class TICPeakListImporter extends AFragmentCommand {

    @Configurable
    private List<String> filesToRead;
    @Configurable(type = int.class, value = "0")
    private int scanIndexOffset = 0;
    @Configurable(type = String.class, value = "SCAN")
    private String scanIndexColumnName = "SCAN";
    @Configurable(value = "var.tic_peaks")
    private String ticPeakVarName = "tic_peaks";

    /*
     * (non-Javadoc)
     * 
     * @see cross.commands.fragments.AFragmentCommand#getDescription()
     */
    @Override
    public String getDescription() {
        return "Imports tic peak data from csv files";
    }

    @Override
    public void configure(Configuration cfg) {
        super.configure(cfg);
        filesToRead = StringTools.toStringList(cfg.getList(this.getClass().
                getName()
                + ".filesToRead", Collections.emptyList()));
        ticPeakVarName = cfg.getString("var.tic_peaks", "tic_peaks");
        scanIndexColumnName = cfg.getString(
                getClass().getName() + ".scanIndexColumnName", "SCAN");
        scanIndexOffset = cfg.getInt(getClass().getName() + ".scanIndexOffset",
                0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.commands.ICommand#apply(java.lang.Object)
     */
    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
        TupleND<IFileFragment> retf = new TupleND<IFileFragment>();
        for (IFileFragment ff : t) {

            for (String s : this.filesToRead) {
                if (StringTools.removeFileExt(s).endsWith(
                        StringTools.removeFileExt(ff.getName()))) {
                    log.warn("Loading TIC peaks from file {}", s);
                    IFileFragment work = Factory.getInstance().
                            getFileFragmentFactory().create(
                            getWorkflow().getOutputDirectory(this),
                            ff.getName(), ff);
                    CSVReader csvr = Factory.getInstance().getObjectFactory().
                            instantiate(CSVReader.class);
                    Tuple2D<Vector<Vector<String>>, Vector<String>> table;
                    try {
                        table = csvr.read(new FileInputStream(s));
                        HashMap<String, Vector<String>> hm = csvr.getColumns(
                                table);
                        Vector<String> peakScanIndex = hm.get(
                                scanIndexColumnName);
                        ArrayInt.D1 extr = new ArrayInt.D1(peakScanIndex.size());
                        for (int i = 0; i < peakScanIndex.size(); i++) {
                            // ChemStation output seems to start with 1 based
                            // indexing,
                            // we need to correct to 0 based indexing
                            extr.set(i,
                                    Integer.parseInt(peakScanIndex.get(i)) + scanIndexOffset);
                        }
                        final IVariableFragment peaks = new VariableFragment(
                                work, this.ticPeakVarName);
                        final Dimension peak_number = new Dimension(
                                "peak_number", peakScanIndex.size(), true,
                                false, false);
                        peaks.setDimensions(new Dimension[]{peak_number});
                        peaks.setArray(extr);
                        retf.add(work);
                        work.save();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
            }

        }
        return retf;
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
}
