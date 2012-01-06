/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
