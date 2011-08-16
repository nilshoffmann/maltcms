/*
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: DefaultWorkflowResult.java 116 2010-06-17 08:46:30Z nilshoffmann $
 */
package cross.datastructures.workflow;

import java.io.File;

import org.jdom.Element;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;

/**
 * Default implementation of an <code>IWorkflowResult</code>.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 */
public class DefaultWorkflowResult implements IWorkflowFileResult {

    private File file = null;
    private WorkflowSlot workflowSlot = null;
    private IWorkflowElement workflowElement = null;
    private IFileFragment[] resources = null;

    public DefaultWorkflowResult() {
    }

    public DefaultWorkflowResult(final File f1, final IWorkflowElement iwe1,
            final WorkflowSlot ws1, IFileFragment... resources) {
        EvalTools.notNull(new Object[]{f1, iwe1, ws1, resources}, this);
        this.file = f1;
        this.workflowElement = iwe1;
        this.workflowSlot = ws1;
        this.resources = resources;
    }

    @Override
    public File getFile() {
        return this.file;
    }

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
        this.file = iff1;
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

        final Element resources = iwr.addContent("resources");
        for (IFileFragment f : this.resources) {
            final Element res = iwr.addContent("resource");
            res.setAttribute("uri", new File(f.getAbsolutePath()).toURI().toString());
            resources.addContent(res);
        }
        iwr.addContent(resources);
        iwr.setAttribute("file", getFile().getAbsolutePath());
        e.addContent(iwr);

    }
}
