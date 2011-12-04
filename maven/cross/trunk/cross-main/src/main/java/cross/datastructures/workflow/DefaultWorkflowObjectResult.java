/*
 * $license$
 *
 * $Id$
 */
package cross.datastructures.workflow;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.io.xml.IXMLSerializable;
import java.io.File;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Slf4j
@Data
public class DefaultWorkflowObjectResult<T> implements IWorkflowObjectResult<T>{

    private T object = null;
    private WorkflowSlot workflowSlot = null;
    private IWorkflowElement workflowElement = null;
    private IFileFragment[] resources = null;

    public DefaultWorkflowObjectResult() {
        
    }
    
    public DefaultWorkflowObjectResult(final T t, final IWorkflowElement iwe1,
            final WorkflowSlot ws1, IFileFragment... resources) {
        this();
        EvalTools.notNull(new Object[]{t, iwe1, ws1, resources}, this);
        this.object = t;
        this.workflowElement = iwe1;
        this.workflowSlot = ws1;
        this.resources = resources;
    }

    @Override
    public void appendXML(Element e) {
        final Element iwr = new Element("workflowElementResult");
        iwr.setAttribute("class", getClass().getCanonicalName());
        iwr.setAttribute("slot", getWorkflowSlot().name());
        iwr.setAttribute("generator", getWorkflowElement().getClass().getCanonicalName());

        final Element resources = iwr.addContent("resources");
        for (IFileFragment f : this.resources) {
            final Element res = iwr.addContent("resource");
            res.setAttribute("uri", new File(f.getAbsolutePath()).toURI().toString());
            resources.addContent(res);
        }
        iwr.addContent(resources);
        if(object instanceof IXMLSerializable) {
            final Element objectElement = iwr.addContent("object");
            ((IXMLSerializable)object).appendXML(objectElement);
        }else{
            log.warn("Object is does not implement cross.io.xml.IXMLSerializable!");
        }
        e.addContent(iwr);
    }
    
}
