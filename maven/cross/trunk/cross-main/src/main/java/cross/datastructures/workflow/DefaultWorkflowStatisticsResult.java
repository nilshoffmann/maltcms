/* 
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.datastructures.workflow;

import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.IWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import java.util.Arrays;
import java.util.Map;
import lombok.Data;
import org.jdom.Element;

/**
 *
 * @author Nils Hoffmann
 */
@Data
public class DefaultWorkflowStatisticsResult implements IWorkflowResult {

    private WorkflowSlot workflowSlot;
    private IWorkflowElement workflowElement;
    private Map<String, Object> stats;

    @Override
    public void appendXML(Element elmnt) {
        if (stats != null) {
            final Element iwr = new Element("workflowElementResult");
            iwr.setAttribute("class", getClass().getCanonicalName());
            iwr.setAttribute("slot", getWorkflowSlot().name());
            iwr.setAttribute("generator", getWorkflowElement().getClass().getCanonicalName());

            final Element resources = new Element("statistics");
            for (String key : stats.keySet()) {
                final Element res = new Element("item");
                res.setAttribute("name", key);
                String value = "";
                if (stats.get(key).getClass().isArray()) {
                    value = Arrays.deepToString((Object[]) stats.get(key));
                } else {
                    value = stats.get(key).toString();
                }
                res.setAttribute("value", value);
                resources.addContent(res);
            }
            iwr.addContent(resources);
            elmnt.addContent(iwr);
        }
    }
}
