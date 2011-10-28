/*
 * $license$
 *
 * $Id$
 */
package maltcms.experimental.bipace.peakCliqueAlignment.io;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.peak.Peak;
import maltcms.io.csv.CSVWriter;
import org.jdom.Element;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Slf4j
@Data
public class MultipleAlignmentWriter implements IWorkflowElement {
    
    private IWorkflow workflow;
    
    public void savePeakMatchTable(final HashMap<String, Integer> columnMap,
            final List<List<Peak>> ll) {
        final List<List<String>> rows = new ArrayList<List<String>>(ll.size());
        List<String> headers = null;
        final String[] headerLine = new String[columnMap.size()];
        for (int i = 0; i < headerLine.length; i++) {
            headerLine[i] = "";
        }
        headers = Arrays.asList(headerLine);
        for (final String s : columnMap.keySet()) {
            headers.set(columnMap.get(s), StringTools.removeFileExt(s));
        }
        log.debug("Adding row {}", headers);
        rows.add(headers);
        for (final List<Peak> l : ll) {
            final String[] line = new String[columnMap.size()];
            for (int i = 0; i < line.length; i++) {
                line[i] = "-";
            }
            log.debug("Adding {} peaks: {}", l.size(), l);
            for (final Peak p : l) {
                final IFileFragment iff = p.getAssociation();
                EvalTools.notNull(iff, this);
                final int pos = columnMap.get(iff.getName()).intValue();
                log.debug("Insert position for {}: {}", iff.getName(), pos);
                if (pos >= 0) {
                    if (line[pos].equals("-")) {
                        line[pos] = p.getScanIndex() + "";
                    } else {
                        log.warn("Array position {} already used!", pos);
                    }
                }
            }
            final List<String> v = Arrays.asList(line);
            rows.add(v);
            log.debug("Adding row {}", v);
        }

        final CSVWriter csvw = new CSVWriter();
        csvw.setWorkflow(getWorkflow());
        csvw.writeTableByRows(getWorkflow().getOutputDirectory(this).
                getAbsolutePath(), "multiple-alignment.csv", rows,
                WorkflowSlot.ALIGNMENT);
    }
    
    /**
     * @param columnMap
     * @param ll
     */
    public void savePeakMatchRTTable(final HashMap<String, Integer> columnMap,
            final List<List<Peak>> ll) {
        final List<List<String>> rows = new ArrayList<List<String>>(ll.size());
        List<String> headers = null;
        final String[] headerLine = new String[columnMap.size()];
        for (int i = 0; i < headerLine.length; i++) {
            headerLine[i] = "";
        }
        headers = Arrays.asList(headerLine);
        for (final String s : columnMap.keySet()) {
            headers.set(columnMap.get(s), StringTools.removeFileExt(s));
        }
        log.debug("Adding row {}", headers);
        rows.add(headers);
        final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
                Locale.US);
        // this is a fix, default rounding convention is HALF_EVEN,
        // which allows less error to accumulate, but is seldomly used
        // outside of java...
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.applyPattern("0.000");
        for (final List<Peak> l : ll) {
            final String[] line = new String[columnMap.size()];
            for (int i = 0; i < line.length; i++) {
                line[i] = "-";
            }
            log.debug("Adding {} peaks", l.size());
            for (final Peak p : l) {
                final IFileFragment iff = p.getAssociation();
                EvalTools.notNull(iff, this);
                final int pos = columnMap.get(iff.getName()).intValue();
                log.debug("Insert position for {}: {}", iff.getName(), pos);
                if (pos >= 0) {
                    if (line[pos].equals("-")) {
                        final double sat = p.getScanAcquisitionTime() / 60.0d;
                        line[pos] = df.format(sat) + "";
                    } else {
                        log.warn("Array position {} already used!", pos);
                    }
                }
            }
            final List<String> v = Arrays.asList(line);
            rows.add(v);
            log.debug("Adding row {}", v);
        }

        final CSVWriter csvw = new CSVWriter();
        csvw.setWorkflow(getWorkflow());
        csvw.writeTableByRows(getWorkflow().getOutputDirectory(this).
                getAbsolutePath(), "multiple-alignmentRT.csv", rows,
                WorkflowSlot.ALIGNMENT);
    }


    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.ALIGNMENT;
    }

    @Override
    public void appendXML(Element e) {
        
    }
    
    
}
