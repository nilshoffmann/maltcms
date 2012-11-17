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
package maltcms.commands.fragments.io;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import maltcms.io.csv.CSVReader;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import cross.Factory;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresOptionalVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.datastructures.tools.EvalTools;
import java.util.Collections;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.lookup.ServiceProvider;

/**
 * Class reading retention indices/anchors/identified compounds from files with
 * specialized tab-separated structure. Should be reworked to blend in with the
 * other IO-Provider Classes and should thus implement IDataSource.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@RequiresOptionalVariables(names = {"var.scan_acquisition_time"})
@ProvidesVariables(names = {"var.anchors.retention_index_names",
    "var.anchors.retention_scans"})
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class CSVAnchorReader extends AFragmentCommand {

    @Override
    public String toString() {
        return getClass().getName();
    }
    private final String omit = "-";
    @Configurable(name = "csvri.retention.time")
    private boolean time = false;
    @Configurable(name = "csvri.retention.index")
    private boolean index = true;
    @Configurable(name = "csvri.retention.scan")
    private boolean scan = false;
    @Configurable
    private List<String> location = Collections.emptyList();
    @Configurable(name = "csvri.filemarker")
    private String fileDesignation = ">";
    @Configurable
    private String basedir = "";
    @Configurable
    private int max_names_length = Integer.MIN_VALUE;
    @Configurable(name = "var.retention_index_names")
    private String anchorNamesVariableName = "retention_index_names";
    @Configurable(name = "var.retention_times")
    private String anchorTimesVariableName = "retention_times";
    @Configurable(name = "var.retention_indices")
    private String anchorRetentionIndexVariableName = "retention_indices";
    @Configurable(name = "var.retention_scans")
    private String anchorScanIndexVariableName = "retention_scans";
    @Configurable(name = "var.scan_acquisition_time")
    private String satVariableName = "scan_acquisition_time";

    /**
     * Simply return the same FileFragments as were received. apply will add the
     * necessary fragments automatically to those fragments in the tuple which
     * have an associated RI file.
     */
    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        // EvalTools.notNull(this.location);
        if (!this.location.isEmpty()) {
            log.info("Using anchors!");
            return applyCSVReader(this.location);
            // apply(this.location);
        } else {
            log.info("Not using anchors!");
        }
        return t;
    }

    protected TupleND<IFileFragment> applyCSVReader(final List<String> u) {
        if (u != null) {
            log.debug("Setting locations to parameter u");
            this.location = u;
        }

        if (this.location.size() == 1) {
            final String filename = u.get(0);
            if (filename.equals("*.txt") || filename.equals("*.tsv")) {
                final File f = new File(this.basedir);
                log.debug("Trying to load anchors from {}", f.
                        getAbsolutePath());
                if (f.isDirectory()) {
                    final String[] files = f.list(new FilenameFilter() {
                        @Override
                        public boolean accept(final File dir, final String name) {
                            if (name.endsWith(".txt") || name.endsWith(".tsv")) {
                                return true;
                            }
                            return false;
                        }
                    });
                    this.location = Arrays.asList(files);
                }
            }
        }

        final ArrayList<IFileFragment> retF = new ArrayList<IFileFragment>();
        for (final String s : this.location) {
            File f = new File(s);
            if (!f.exists()) {
                f = new File(this.basedir, s);
            }
            log.debug("Reading anchors from file {}", s);
            final CSVReader csvr = new CSVReader();
            final HashMap<String, Vector<String>> cols = csvr.getColumns(csvr.
                    read(f.getAbsolutePath()));
            final Vector<String> skippedLines = csvr.getSkippedLines();
            EvalTools.eqI(skippedLines.size(), 1, this);
            final String associatedToFile = skippedLines.get(0).substring(
                    this.fileDesignation.length());
            log.debug("Associated to file: {}", associatedToFile);
            final File targetFile = new File(associatedToFile);
            final String inputBasedir = Factory.getInstance().getConfiguration().
                    getString("input.basedir", "");
            File g = null;
            if (targetFile.isAbsolute()) {
                log.info("File association is absolute");
                g = targetFile;
            } else {
                // Check if file is in same directory
                g = new File(f.getParentFile(), associatedToFile);
                if (!g.exists() && !inputBasedir.isEmpty()) {
                    // locate file based on filename and input files
                    final List<IFileFragment> l = Factory.getInstance().
                            getInputDataFactory().getInitialFiles();
                    int collision = 0;
                    for (final IFileFragment iff : l) {
                        if (iff.getName().equals(associatedToFile)) {
                            collision++;
                            g = new File(iff.getUri());
                        }
                    }
                    if (collision > 1) {
                        log.warn(
                                "Found {} files with the same name, can not associate!",
                                collision);
                        g = null;
                    }
                }
            }
            if (g != null) {
                log.info("Associated file is {}", g.getAbsolutePath());
                final String name = g.getAbsolutePath();
                log.debug("Full path: {}", name);
                // create new working fragment
                final IFileFragment parentFragment = new FileFragment(
                        new File(getWorkflow().getOutputDirectory(this), g.
                        getName()));
                // add associatedToFile fragment as source file, create if
                // non-existant
                parentFragment.addSourceFile(new FileFragment(g));
                if (this.scan) {
                    createScan(cols.get("Scan"), parentFragment);
                }
                if (this.index) {
                    createIndex(cols.get("RI"), parentFragment);
                }
                if (this.time) {
                    createTime(cols.get("RT"), parentFragment);
                }
                createName(cols.get("Name"), parentFragment);
                parentFragment.save();
                final DefaultWorkflowResult dwr = new DefaultWorkflowResult(
                        parentFragment.getUri(), this,
                        WorkflowSlot.FILEIO, parentFragment);
                getWorkflow().append(dwr);
                retF.add(parentFragment);
            } else {
                log.warn("No association possible, skipping file: {}",
                        associatedToFile);
                g = null;
            }
        }
        return new TupleND<IFileFragment>(retF);
    }

    private void augmentRT(final IFileFragment parent) {
        final IVariableFragment rt = parent.getChild(
                this.anchorTimesVariableName);
        Array rta = rt.getArray();
        final IVariableFragment rs = parent.getChild(
                this.anchorScanIndexVariableName);
        final Array rsa = rs.getArray();
        if (rta.getShape()[0] != rsa.getShape()[0]) {
            rta = new ArrayDouble.D1(rsa.getShape()[0]);
        }
        final IVariableFragment sat = parent.getChild(this.satVariableName);
        final Array a = sat.getArray();
        final Index ia = a.getIndex();
        final IndexIterator irta = rta.getIndexIterator();
        final IndexIterator irsa = rsa.getIndexIterator();
        while (irsa.hasNext() && irta.hasNext()) {
            // increase time iter
            if (irta.getDoubleNext() == -1.0d) {
                // set current time value to value in scan_acquisition_time at
                // index irsa.getIntNext
                irta.setDoubleCurrent(a.getDouble(ia.set(irsa.getIntNext())));
            }
        }
        rt.setArray(rta);
        parent.removeChild(sat);
    }

    @Override
    public void configure(final Configuration cfg) {
        if (cfg.containsKey("csvri.retention.time")) {
            this.time = cfg.getBoolean("csvri.retention.time");
        }
        if (cfg.containsKey("csvri.retention.index")) {
            this.index = cfg.getBoolean("csvri.retention.index");
        }
        if (cfg.containsKey("csvri.retention.scan")) {
            this.scan = cfg.getBoolean("csvri.retention.scan");
        }
        if (cfg.containsKey("anchors.location")) {
            final List<?> l = cfg.getList("anchors.location");
            final List<String> loc = new ArrayList<String>();
            for (final Object o : l) {
                if (o instanceof String) {
                    loc.add((String) o);
                } else {
                    log.warn("Property Object is not an instance of String! {}", o);
                }
            }
            this.location = loc;
        }
        if (cfg.containsKey("input.basedir")) {
            this.basedir = cfg.getString("input.basedir", "");
        }
        if (cfg.containsKey("csvri.filemarker")) {
            this.fileDesignation = cfg.getString("csvri.filemarker");
        }

        this.anchorNamesVariableName = cfg.getString(
                "var.anchors.retention_index_names", "retention_index_names");
        this.anchorTimesVariableName = cfg.getString(
                "var.anchors.retention_times", "retention_times");
        this.anchorRetentionIndexVariableName = cfg.getString(
                "var.anchors.retention_indices", "retention_indices");
        this.anchorScanIndexVariableName = cfg.getString(
                "var.anchors.retention_scans", "retention_scans");
        this.satVariableName = cfg.getString("var.scan_acquisition_time",
                "scan_acquisition_time");

    }

    /**
     * @param indices
     * @param parentFragment
     * @return
     */
    private IVariableFragment createIndex(final List<String> indices,
            final IFileFragment parentFragment) {
        final Array a = Array.factory(DataType.INT,
                new int[]{indices.size()});
        final IndexIterator ii = a.getIndexIterator();
        final Iterator<String> indi = indices.iterator();
        while (ii.hasNext() && indi.hasNext()) {
            final String idx = indi.next();
            if (idx.equals(this.omit) || (Double.parseDouble(idx) == -1.0d)) {
                log.debug("Skipping entry, does not contain a number!");
                ii.setIntNext(-1);
            } else {
                ii.setIntNext(Integer.parseInt(idx));
            }
        }
        final IVariableFragment riindices = new VariableFragment(
                parentFragment, this.anchorRetentionIndexVariableName);
        riindices.setArray(a);
        // riindices.setProtect(true);
        log.debug(riindices.toString());
        return riindices;
    }

    /**
     * @param names
     * @param parentFragment
     * @return
     */
    private IVariableFragment createName(final List<String> names,
            final IFileFragment parentFragment) {
        int maxlength = 0;
        for (final String s : names) {
            maxlength = Math.max(maxlength, s.length());
        }
        this.max_names_length = Math.max(this.max_names_length, maxlength);
        final ArrayChar.D2 c = new ArrayChar.D2(names.size(),
                this.max_names_length);
        final Iterator<String> nit = names.iterator();
        int row = 0;
        while (nit.hasNext()) {
            c.setString(row++, nit.next());
        }
        final IVariableFragment rinames = new VariableFragment(parentFragment,
                this.anchorNamesVariableName);
        rinames.setArray(c);
        // rinames.setProtect(true);
        log.debug(rinames.toString());
        return rinames;
    }

    /**
     * @param scans
     * @param parentFragment
     * @return
     */
    private IVariableFragment createScan(final List<String> scans,
            final IFileFragment parentFragment) {
        final Array d = Array.factory(DataType.INT, new int[]{scans.size()});
        final IndexIterator ii = d.getIndexIterator();
        final Iterator<String> timi = scans.iterator();
        while (ii.hasNext() && timi.hasNext()) {
            final String scn = timi.next();
            if (scn.equals(this.omit) || (Integer.parseInt(scn) == -1)) {
                log.debug("Skipping entry, does not contain a number!");
                ii.setIntNext(-1);
            } else {
                ii.setIntNext(Integer.parseInt(scn));
            }
            // ii.setIntNext(Integer.parseInt(timi.next()));
        }
        final IVariableFragment riscans = new VariableFragment(parentFragment,
                this.anchorScanIndexVariableName);
        riscans.setArray(d);
        // riscans.setProtect(true);
        log.debug(riscans.toString());
        return riscans;
    }

    /**
     * @param times
     * @param parentFragment
     * @return
     */
    private IVariableFragment createTime(final List<String> times,
            final IFileFragment parentFragment) {
        final Array b = Array.factory(DataType.DOUBLE,
                new int[]{times.size()});
        final IndexIterator ii = b.getIndexIterator();
        final Iterator<String> timi = times.iterator();
        while (ii.hasNext() && timi.hasNext()) {
            final String tme = timi.next();
            if (tme.equals(this.omit) || (Double.parseDouble(tme) == 0)) {
                log.debug("Skipping entry, does not contain a number!");
                ii.setDoubleNext(-1.0d);
            } else {
                ii.setDoubleNext(Double.parseDouble(tme));
            }
            // ii.setDoubleNext(Double.parseDouble(timi.next()));
        }
        final IVariableFragment ritimes = new VariableFragment(parentFragment,
                this.anchorTimesVariableName);
        ritimes.setArray(b);
        // ritimes.setProtect(true);
        log.debug(ritimes.toString());
        augmentRT(parentFragment);
        return ritimes;
    }

    /**
     * @return the anchorNamesVariableName
     */
    public String getAnchorNamesVariableName() {
        return this.anchorNamesVariableName;
    }

    /**
     * @return the anchorRetentionIndexVariableName
     */
    public String getAnchorRetentionIndexVariableName() {
        return this.anchorRetentionIndexVariableName;
    }

    /**
     * @return the anchorScanIndexVariableName
     */
    public String getAnchorScanIndexVariableName() {
        return this.anchorScanIndexVariableName;
    }

    /**
     * @return the anchorTimesVariableName
     */
    public String getAnchorTimesVariableName() {
        return this.anchorTimesVariableName;
    }

    /**
     * @return the basedir
     */
    public String getBasedir() {
        return this.basedir;
    }

    @Override
    public String getDescription() {
        return "Reads anchors stored in csv/tsv compatible format.";
    }

    /**
     * @return the fileDesignation
     */
    public String getFileDesignation() {
        return this.fileDesignation;
    }

    /**
     * @return the location
     */
    public List<String> getLocation() {
        return this.location;
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
     * @param anchorNamesVariableName the anchorNamesVariableName to set
     */
    public void setAnchorNamesVariableName(final String anchorNamesVariableName) {
        this.anchorNamesVariableName = anchorNamesVariableName;
    }

    /**
     * @param anchorRetentionIndexVariableName the
     * anchorRetentionIndexVariableName to set
     */
    public void setAnchorRetentionIndexVariableName(
            final String anchorRetentionIndexVariableName) {
        this.anchorRetentionIndexVariableName = anchorRetentionIndexVariableName;
    }

    /**
     * @param anchorScanIndexVariableName the anchorScanIndexVariableName to set
     */
    public void setAnchorScanIndexVariableName(
            final String anchorScanIndexVariableName) {
        this.anchorScanIndexVariableName = anchorScanIndexVariableName;
    }

    /**
     * @param anchorTimesVariableName the anchorTimesVariableName to set
     */
    public void setAnchorTimesVariableName(final String anchorTimesVariableName) {
        this.anchorTimesVariableName = anchorTimesVariableName;
    }

    /**
     * @param basedir the basedir to set
     */
    public void setBasedir(final String basedir) {
        this.basedir = basedir;
    }

    /**
     * @param fileDesignation the fileDesignation to set
     */
    public void setFileDesignation(final String fileDesignation) {
        this.fileDesignation = fileDesignation;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(final List<String> location) {
        this.location = location;
    }
}
