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
package maltcms.io.misc;

import java.io.File;

import maltcms.io.csv.CSVWriter;

import org.jdom.Element;

import cross.Factory;
import cross.datastructures.StatsMap;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import lombok.extern.slf4j.Slf4j;

/**
 * Writes StatsMap objects to CSV Files.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Slf4j
public class StatsWriter implements IWorkflowElement {

    private IWorkflow iwf;

    /*
     * (non-Javadoc)
     * 
     * @see cross.io.misc.IXMLSerializable#appendXML(org.jdom.Element)
     */
    @Override
    public void appendXML(final Element e) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflow()
     */
    @Override
    public IWorkflow getWorkflow() {
        return this.iwf;
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
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
    @Override
    public void setWorkflow(final IWorkflow iw) {
        this.iwf = iw;

    }

    public void write(final IFileFragment ff) {

        final StatsMap sm = ff.getStats();
        write(sm);
        for (final IVariableFragment vf : ff) {
            write(vf.getStats());
        }
    }

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
                                .removeFileExt(parent.getAbsolutePath());
                        final String path = new File(parent.getAbsolutePath())
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
