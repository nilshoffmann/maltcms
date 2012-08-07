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
package cross.datastructures.workflow;

import java.util.Arrays;
import java.util.Map;
import lombok.Data;
import org.jdom.Element;

/**
 *
 * @author nilshoffmann
 */
@Data
public class DefaultWorkflowStatisticsResult implements IWorkflowResult {

    private WorkflowSlot workflowSlot;
    private IWorkflowElement workflowElement;
    private Map<String,Object> stats;

    @Override
    public void appendXML(Element elmnt) {
        if (stats != null) {
            final Element iwr = new Element("workflowElementResult");
            iwr.setAttribute("class", getClass().getCanonicalName());
            iwr.setAttribute("slot", getWorkflowSlot().name());
            iwr.setAttribute("generator", getWorkflowElement().getClass().getCanonicalName());

            final Element resources = new Element("statistics");
            for(String key:stats.keySet()) {
                final Element res = new Element("item");
                res.setAttribute("name", key);
                String value = "";
                if(stats.get(key).getClass().isArray()) {
                    value = Arrays.deepToString((Object[])stats.get(key));
                }else{
                    value = stats.get(key).toString();
                }
                res.setAttribute("value",value);
                resources.addContent(res);
            }
            iwr.addContent(resources);
            elmnt.addContent(iwr);
        }
    }
}
