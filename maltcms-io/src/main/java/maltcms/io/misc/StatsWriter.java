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
package maltcms.io.misc;

import cross.Factory;
import cross.datastructures.StatsMap;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tools.FileTools;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import java.io.File;

import maltcms.io.csv.CSVWriter;
import org.jdom2.Element;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Writes StatsMap objects to CSV Files.
 *
 * @author Nils Hoffmann
 * 
 */

public class StatsWriter implements IWorkflowElement {
    
    private static final org.slf4j.Logger log = getLogger(StatsWriter.class);

    private IWorkflow iwf;

    /*
     * (non-Javadoc)
     *
     * @see cross.io.misc.IXMLSerializable#appendXML(org.jdom.Element)
     */
    /** {@inheritDoc} */
    @Override
    public void appendXML(final Element e) {
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflow()
     */
    /** {@inheritDoc} */
    @Override
    public IWorkflow getWorkflow() {
        return this.iwf;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    /** {@inheritDoc} */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.STATISTICS;
    }

    /*
     * (non-Javadoc)
     *
     * @seecross.datastructures.workflow.IWorkflowElement#setWorkflow(cross.
     * datastructures.workflow.IWorkflow)
     */
    /** {@inheritDoc} */
    @Override
    public void setWorkflow(final IWorkflow iw) {
        this.iwf = iw;

    }

    /**
     * <p>write.</p>
     *
     * @param ff a {@link cross.datastructures.fragments.IFileFragment} object.
     */
    public void write(final IFileFragment ff) {

        final StatsMap sm = ff.getStats();
        write(sm);
        for (final IVariableFragment vf : ff) {
            write(vf.getStats());
        }
    }

    /**
     * <p>write.</p>
     *
     * @param sm a {@link cross.datastructures.StatsMap} object.
     */
    public void write(final StatsMap... sm) {
        if (sm != null) {
            log.info("Writing " + sm.length + " statsMaps to file!");
            // String outdir = ArrayFactory.getConfiguration().getString(
            // "output.basedir", "")
            // + "/stats";
            for (final StatsMap map : sm) {
                if (map != null) {
                    final IFragment f = map.getAssociation();
                    final CSVWriter csvw = Factory.getInstance()
                            .getObjectFactory().instantiate(CSVWriter.class);
                    csvw.setWorkflow(getWorkflow());
                    // if (f instanceof IGroupFragment) {
                    // csvw.write(f
                    // , map);
                    if (f instanceof IVariableFragment) {
                        final IFileFragment parent = ((IVariableFragment) f)
                                .getParent();
                        final String basename = StringTools
                                .removeFileExt(FileTools.getFilename(parent.getUri()));
                        final String path = new File(parent.getUri())
                                .getParent();
                        csvw.writeStatsMap(path,
                                basename + "-"
                                + ((IVariableFragment) f).getName()
                                + ".csv", map);
                    } else if (f instanceof IFileFragment) {
                        csvw.writeStatsMap((IFileFragment) f, map);
                    }
                }
            }
        } else {
            log.warn("StatsMap[] was null!");
        }
    }
}
