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

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.IWorkflowObjectResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.datastructures.tools.EvalTools;
import cross.io.xml.IXMLSerializable;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
@Data
public class DefaultWorkflowObjectResult<T> implements IWorkflowObjectResult<T> {

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

        final Element resources = new Element("resources");
        for (IFileFragment f : this.resources) {
            final Element res = new Element("resource");
            res.setAttribute("uri", f.getUri().normalize().toString());
            resources.addContent(res);
        }
        if (object instanceof IXMLSerializable) {
            final Element objectElement = new Element("object");
            ((IXMLSerializable) object).appendXML(objectElement);
            iwr.addContent(objectElement);
        } else {
            log.warn("Object is does not implement cross.io.xml.IXMLSerializable!");
        }
        iwr.addContent(resources);
        e.addContent(iwr);
    }
}
