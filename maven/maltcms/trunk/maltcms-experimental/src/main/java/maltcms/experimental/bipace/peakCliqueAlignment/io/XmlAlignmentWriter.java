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
package maltcms.experimental.bipace.peakCliqueAlignment.io;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.alignment.AlignmentFactory;
import maltcms.datastructures.peak.Peak;
import maltcms.io.xml.bindings.alignment.Alignment;
import org.jdom.Element;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Slf4j
@Data
public class XmlAlignmentWriter implements IWorkflowElement {
    private IWorkflow workflow;

    public void saveToXMLAlignment(final TupleND<IFileFragment> tuple,
            final List<List<Peak>> ll) {
        AlignmentFactory af = new AlignmentFactory();
        Alignment a = af.createNewAlignment(this.getClass().getName(), false);
        HashMap<IFileFragment, List<Integer>> fragmentToScanIndexMap = new HashMap<IFileFragment, List<Integer>>();
        for (final List<Peak> l : ll) {
            log.debug("Adding {} peaks: {}", l.size(), l);
            HashMap<IFileFragment, Peak> fragToPeak = new HashMap<IFileFragment, Peak>();
            for (final Peak p : l) {
                fragToPeak.put(p.getAssociation(), p);
            }
            for (final IFileFragment iff : tuple) {
                int scanIndex = -1;
                if (fragToPeak.containsKey(iff)) {
                    Peak p = fragToPeak.get(iff);
                    scanIndex = p.getScanIndex();
                }

                List<Integer> scans = null;
                if (fragmentToScanIndexMap.containsKey(iff)) {
                    scans = fragmentToScanIndexMap.get(iff);
                } else {
                    scans = new ArrayList<Integer>();
                    fragmentToScanIndexMap.put(iff, scans);
                }

                scans.add(scanIndex);
            }
        }

        for (IFileFragment iff : fragmentToScanIndexMap.keySet()) {
            af.addScanIndexMap(a, new File(iff.getAbsolutePath()).toURI(),
                    fragmentToScanIndexMap.get(iff), false);
        }
        File out = new File(getWorkflow().getOutputDirectory(this),
                "peakCliqueAssignment.maltcmsAlignment.xml");
        af.save(a, out);
        DefaultWorkflowResult dwr = new DefaultWorkflowResult(out, this,
                WorkflowSlot.ALIGNMENT, tuple.toArray(new IFileFragment[]{}));
        getWorkflow().append(dwr);
    }
    
    
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.ALIGNMENT;
    }

    @Override
    public void appendXML(Element e) {
        
    }
    
}
