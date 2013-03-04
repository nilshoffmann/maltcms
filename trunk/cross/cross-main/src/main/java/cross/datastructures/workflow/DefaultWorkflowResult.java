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
import cross.datastructures.tools.EvalTools;
import cross.exception.ResourceNotAvailableException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;

/**
 * Default implementation of an
 * <code>IWorkflowResult</code>.
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
public class DefaultWorkflowResult implements IWorkflowFileResult {

    private URI file = null;
    private WorkflowSlot workflowSlot = null;
    private IWorkflowElement workflowElement = null;
    private IFileFragment[] resources = null;

    public DefaultWorkflowResult() {
    }

    public DefaultWorkflowResult(final URI f1, final IWorkflowElement iwe1,
            final WorkflowSlot ws1, IFileFragment... resources) {
        EvalTools.notNull(new Object[]{f1, iwe1, ws1, resources}, this);
        this.file = f1;
        this.workflowElement = iwe1;
        this.workflowSlot = ws1;
        this.resources = resources;
    }

    public DefaultWorkflowResult(final File f1, final IWorkflowElement iwe1,
            final WorkflowSlot ws1, IFileFragment... resources) {
        this(f1.toURI(), iwe1, ws1, resources);
    }

    @Override
    public File getFile() {
        File f = new File(this.file);
        if (f.isFile()) {
            return f;
        }
        throw new ResourceNotAvailableException("Local file is a URI resource: " + this.file);
    }

    @Override
    public IWorkflowElement getWorkflowElement() {
        return this.workflowElement;
    }

    @Override
    public WorkflowSlot getWorkflowSlot() {
        return this.workflowSlot;
    }

    @Override
    public void setFile(final File iff1) {
        EvalTools.notNull(iff1, this);
        this.file = iff1.toURI();
    }

    @Override
    public void setWorkflowElement(final IWorkflowElement iwe1) {
        EvalTools.notNull(iwe1, this);
        this.workflowElement = iwe1;
    }

    @Override
    public void setWorkflowSlot(final WorkflowSlot ws1) {
        EvalTools.notNull(ws1, this);
        this.workflowSlot = ws1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.datastructures.workflow.IWorkflowFileResult#getResources()
     */
    @Override
    public IFileFragment[] getResources() {
        return this.resources;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * cross.datastructures.workflow.IWorkflowFileResult#setResources(cross.
     * datastructures.fragments.IFileFragment[])
     */
    @Override
    public void setResources(IFileFragment... resources) {
        this.resources = resources;
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.io.xml.IXMLSerializable#appendXML(org.jdom.Element)
     */
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
        iwr.addContent(resources);
        iwr.setAttribute("file", getFile().getAbsolutePath());
        iwr.setAttribute("file-uri", this.file.normalize().toString());
        e.addContent(iwr);
    }
}
