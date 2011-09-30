/**
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
 */
/*
 * 
 *
 * $Id$
 */

package cross.commands.workflow;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import cross.commands.fragments.AFragmentCommand;
import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.commands.pipeline.ICommandSequence;
import cross.event.AEvent;
import cross.event.EventSource;
import cross.event.IEvent;
import cross.event.IEventSource;
import cross.event.IListener;
import cross.io.misc.BinaryFileBase64Wrapper;
import cross.io.misc.DefaultConfigurableFileFilter;
import cross.datastructures.xml.IXMLSerializable;
import cross.exception.NotImplementedException;
import cross.io.FileTools;
import cross.tools.StringTools;
import java.io.FileFilter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * A default implementation of {@link cross.datastructures.workflow.IWorkflow}.
 * Is a source of <code>IEvent<IWorkflowResult></code> events.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Data
@Slf4j
public class DefaultWorkflow implements IWorkflow, IXMLSerializable {

    private ArrayList<IWorkflowResult> workflowResults = new ArrayList<IWorkflowResult>();
    private IEventSource<IWorkflowResult> eventSource = new EventSource<IWorkflowResult>();
    private ICommandSequence commandSequence = null;
    private String name = "defaultWorkflow";
    private IFragmentCommand activeCommand = null;
    private String xslPathPrefix;
    private FileFilter fileFilter = new DefaultConfigurableFileFilter();
    private boolean saveHTML = false;
    private boolean saveTEXT = false;
    private boolean saveInFragmentCommandDir = false;
    private Date startupDate = new Date();
    private boolean executeLocal = true;
    private WorkflowZipper workflowZipper = new WorkflowZipper();
    private FileTools fileTools;

    @Override
    public void addListener(final IListener<IEvent<IWorkflowResult>> l) {
        this.eventSource.addListener(l);
    }

    @Override
    public void append(final IWorkflowResult iwr) {
        if (this.workflowResults == null) {
            this.workflowResults = new ArrayList<IWorkflowResult>();
        }
        if (iwr instanceof IWorkflowProgressResult) {
            final IWorkflowProgressResult iwpr = (IWorkflowProgressResult) iwr;
            log.info("Step {}/{}, Overall progress: {}%", new Object[]{
                        iwpr.getCurrentStep(), iwpr.getNumberOfSteps(),
                        iwpr.getOverallProgress()});
        } else {
            this.workflowResults.add(iwr);
        }
        fireEvent(new AEvent<IWorkflowResult>(iwr, DefaultWorkflow.this.eventSource));
    }

    @Override
    public void appendXML(final Element e) {
        log.debug("Appending xml for DefaultWorkflow " + getName());
        // Element workflow = new Element("workflow");
        // workflow.setAttribute("class", this.getClass().getCanonicalName());
        // workflow.setAttribute("name", name);
        getCommandSequence().appendXML(e);
        for (final IWorkflowResult wr : this.workflowResults) {
            final Element iwr = new Element("workflowElementResult");
            iwr.setAttribute("class", wr.getClass().getCanonicalName());
            iwr.setAttribute("slot", wr.getWorkflowSlot().name());
            iwr.setAttribute("generator", wr.getWorkflowElement().getClass().getCanonicalName());

            final Element resources = new Element("resources");
            if (wr instanceof IWorkflowFileResult) {
                final Element resource = new Element("resource");
                resource.setAttribute("file", ((IWorkflowFileResult) wr).getFile().getAbsolutePath());
                resources.addContent(resource);
            }
            iwr.addContent(resources);
            e.addContent(iwr);
        }
        // e.addContent(workflow);
    }

//    /*
//     * (non-Javadoc)
//     *
//     * @see
//     * cross.IConfigurable#configure(org.apache.commons.configuration.Configuration
//     * )
//     */
//    @Override
//    public void configure(final Configuration cfg) {
//        this.saveHTML = cfg.getBoolean(this.getClass().getName() + ".saveHTML",
//                false);
//        this.saveTEXT = cfg.getBoolean(this.getClass().getName() + ".saveTEXT",
//                false);
//        this.xslPathPrefix = cfg.getString(this.getClass().getName()
//                + ".xslPathPrefix", "");
//        this.fileFilter = cfg.getString(this.getClass().getName()
//                + ".resultFileFilter", DefaultConfigurableFileFilter.class.getName());
//        this.saveInFragmentCommandDir = cfg.getBoolean(this.getClass().getName()
//                + ".saveInFragmentCommandDir", true);
//    }

    @Override
    public void fireEvent(final IEvent<IWorkflowResult> e) {
        this.eventSource.fireEvent(e);
    }

    @Override
    public Iterator<IWorkflowResult> getResults() {
        return this.workflowResults.iterator();
    }

    @Override
    public void readXML(final Element e) throws IOException,
            ClassNotFoundException {
        
        throw new NotImplementedException("Currently not implemented!");
//        final String workflowname = e.getAttributeValue("name");
//        if (workflowname != null) {
//            setName(workflowname);
//        }
//        final List<?> l = e.getChildren("workflowElementResult");
//        for (final Object obj : l) {
//            final Element elem = (Element) obj;
//            final String cls = elem.getAttributeValue("class");
//            final String slot = elem.getAttributeValue("slot");
//            final String generator = elem.getAttributeValue("generator");
//            final String file = elem.getAttributeValue("file");
//            final IWorkflowFileResult iwr = Factory.getInstance().getObjectFactory().instantiate(cls,
//                    IWorkflowFileResult.class);
//            final IWorkflowElement iwe = Factory.getInstance().getObjectFactory().instantiate(generator,
//                    IWorkflowElement.class);
//            iwr.setWorkflowElement(iwe);
//            iwr.setFile(new File(file));
//            iwr.setWorkflowSlot(WorkflowSlot.valueOf(slot));
//            append(iwr);
//        }
    }

    @Override
    public void removeListener(final IListener<IEvent<IWorkflowResult>> l) {
        this.eventSource.removeListener(l);
    }

    @Override
    public void save() {
        try {
            final String wflname = getName();
            log.info("Saving workflow to file {}", wflname);
            final Document doc = new Document();
//			final HashMap<String, String> hm = new HashMap<String, String>();
//			hm.put("type", "text/xsl");
//			hm.put("href", this.xslPathPrefix + "maltcmsResult.xsl");
            final ProcessingInstruction pi = new ProcessingInstruction(
                    "xml-stylesheet",
                    "type=\"text/xsl\" href=\"http://maltcms.sourceforge.net/res/maltcmsHTMLResult.xsl\"");
            doc.addContent(pi);
            doc.addContent(writeXML());
            final XMLOutputter outp = new XMLOutputter(Format.getPrettyFormat());
            try {
                final File f = new File(wflname);
                final File dir = f.getParentFile();
                dir.mkdirs();
                f.createNewFile();
                outp.output(doc, new BufferedOutputStream(new FileOutputStream(
                        f)));
                workflowZipper.setWorkflow(this);
                final File results = new File(dir, "maltcmsResults.zip");
                if (workflowZipper.save(results)) {
                    BinaryFileBase64Wrapper.base64Encode(results, new File(dir,
                            results.getName() + ".b64"));
                } else {
                    log.debug("Did not Base64 encode maltcmsResults.zip");
                }
                if (this.saveHTML) {
                    saveHTML(f);
                }
                if (this.saveTEXT) {
                    saveTEXT(f);
                }
            } catch (final IOException e) {
                log.error(e.getLocalizedMessage());
            }
        } catch (final FileNotFoundException e) {
            log.error(e.getLocalizedMessage());
        } catch (final IOException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    /**
     * Applies a stylesheet transformation to this DefaultWorkflow to create
     * html with clickable links.
     *
     * @param f
     */
    protected void saveHTML(final File f) {

        final TransformerFactory tf = TransformerFactory.newInstance();
        try {
            final InputStream xsstyler = getClass().getClassLoader().getResourceAsStream("res/xslt/maltcmsHTMLResult.xsl");
            final StreamSource sstyles = new StreamSource(xsstyler);
            final FileInputStream xsr = new FileInputStream(f);
            final FileOutputStream xsw = new FileOutputStream(new File(f.getParentFile(), "workflow.html"));
            final StreamSource sts = new StreamSource(xsr);
            final StreamResult str = new StreamResult(xsw);
            final Transformer t = tf.newTransformer(sstyles);
            t.transform(sts, str);
        } catch (final TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Applies a stylesheet transformation to this DefaultWorkflow to create
     * text.
     *
     * @param f
     */
    protected void saveTEXT(final File f) {

        final TransformerFactory tf = TransformerFactory.newInstance();
        try {
            final InputStream xsstyler = getClass().getClassLoader().getResourceAsStream("res/xslt/maltcmsTEXTResult.xsl");
            final StreamSource sstyles = new StreamSource(xsstyler);
            final FileInputStream xsr = new FileInputStream(f);
            final FileOutputStream xsw = new FileOutputStream(new File(f.getParentFile(), "workflow.txt"));
            final StreamSource sts = new StreamSource(xsr);
            final StreamResult str = new StreamResult(xsw);
            final Transformer t = tf.newTransformer(sstyles);
            t.transform(sts, str);
        } catch (final TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public Element writeXML() throws IOException {
        final Element root = new Element("workflow");
        root.setAttribute("class", getClass().getCanonicalName());
        root.setAttribute("name", getName());
        appendXML(root);
        return root;
    }

    /*
     * (non-Javadoc)
     *
     * @seecross.datastructures.workflow.IWorkflow#getOutputDirectory(java.lang.
     * Object)
     */
    @Override
    public File getOutputDirectory(Object iwe) {
        if (iwe instanceof AFragmentCommand) {
            Collection<IFragmentCommand> c = this.commandSequence.getCommands();
            int i = 0;
            int digits = (int) Math.ceil(Math.log10(c.size())) + 1;
            for (IFragmentCommand afc : c) {
                IFragmentCommand iwa = (IFragmentCommand) iwe;
                log.debug("Checking for reference equality!");
                // check for reference equality
                if (iwa == afc) {
                    log.debug("Reference equality holds!");
                    if (activeCommand == null) {
                        activeCommand = iwa;
                    }
                    if (activeCommand != iwa) {
                        activeCommand = iwa;
                    }
                    return fileTools.prependDefaultDirsWithPrefix(String.format("%0" + digits + "d", i)
                            + "_", iwa.getClass(), getStartupDate());
                }
                i++;
            }
            if (activeCommand != null) {
                if (saveInFragmentCommandDir) {
                    return getOutputDirectory(activeCommand);
                } else {
                    File dir = new File(getOutputDirectory(activeCommand), iwe.getClass().getSimpleName());
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    return dir;
                }
            }
        } else if (iwe instanceof IWorkflowElement) {
            if (activeCommand != null) {
                if (saveInFragmentCommandDir) {
                    return getOutputDirectory(activeCommand);
                } else {
                    File dir = new File(getOutputDirectory(activeCommand), iwe.getClass().getSimpleName());
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    return dir;
                }
            }
        }
        if (activeCommand != null) {
            if (saveInFragmentCommandDir) {
                return getOutputDirectory(activeCommand);
            }
        }
        return fileTools.prependDefaultDirsWithPrefix("", iwe.getClass(),
                getStartupDate());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.workflow.IWorkflow#getResultsFor(cross.datastructures
     * .fragments.IFileFragment)
     */
    @Override
    public List<IWorkflowResult> getResultsFor(IFileFragment iff) {
        List<IWorkflowResult> l = new ArrayList<IWorkflowResult>();
        Iterator<IWorkflowResult> iter = getResults();
        while (iter.hasNext()) {
            IWorkflowResult iwr = iter.next();
            if (iwr instanceof IWorkflowFileResult) {
                IWorkflowFileResult iwfr = (IWorkflowFileResult) iwr;
                IFileFragment[] res = iwfr.getResources();
                for (IFileFragment resf : res) {
                    if (StringTools.removeFileExt(resf.getName()).equals(
                            StringTools.removeFileExt(iff.getName()))) {
                        l.add(iwfr);
                    }
                }
            }
        }
        return l;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.workflow.IWorkflow#getResultsFor(cross.commands.
     * fragments.IWorkflowElement)
     */
    @Override
    public List<IWorkflowResult> getResultsFor(IWorkflowElement afc) {
        List<IWorkflowResult> l = new ArrayList<IWorkflowResult>();
        Iterator<IWorkflowResult> iter = getResults();
        while (iter.hasNext()) {
            IWorkflowResult iwr = iter.next();
            IWorkflowElement iwe = iwr.getWorkflowElement();
            if (iwe.getClass().getName().equals(afc.getClass().getName())) {
                l.add(iwr);
            }
        }
        return l;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.workflow.IWorkflow#getResultsFor(cross.commands.
     * fragments.IWorkflowElement, cross.datastructures.fragments.IFileFragment)
     */
    @Override
    public List<IWorkflowResult> getResultsFor(IWorkflowElement afc,
            IFileFragment iff) {
        List<IWorkflowResult> afcl = getResultsFor(afc);
        List<IWorkflowResult> l = new ArrayList<IWorkflowResult>();
        for (IWorkflowResult iwr : afcl) {
            if (iwr instanceof IWorkflowFileResult) {
                IWorkflowFileResult iwfr = (IWorkflowFileResult) iwr;
                IFileFragment[] res = iwfr.getResources();
                for (IFileFragment resf : res) {
                    if (StringTools.removeFileExt(resf.getName()).equals(
                            StringTools.removeFileExt(iff.getName()))) {
                        l.add(iwfr);
                    }
                }
            }
        }
        return l;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.workflow.IWorkflow#getResultsOfType(java.lang.String
     * )
     */
    @Override
    public List<IWorkflowResult> getResultsOfType(String fileExtension) {
        List<IWorkflowResult> l = new ArrayList<IWorkflowResult>();
        Iterator<IWorkflowResult> iter = getResults();
        while (iter.hasNext()) {
            IWorkflowResult iwr = iter.next();
            if (iwr instanceof IWorkflowFileResult) {
                IWorkflowFileResult iwfr = (IWorkflowFileResult) iwr;
                if (iwfr.getFile().getAbsolutePath().endsWith(fileExtension)) {
                    l.add(iwr);
                }
            }
        }
        return l;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.workflow.IWorkflow#getResultsOfType(cross.datastructures
     * .workflow.IWorkflowElement, java.lang.String)
     */
    @Override
    public List<IWorkflowResult> getResultsOfType(IWorkflowElement afc,
            String fileExtension) {
        List<IWorkflowResult> l = new ArrayList<IWorkflowResult>();
        Iterator<IWorkflowResult> iter = getResults();
        while (iter.hasNext()) {
            IWorkflowResult iwr = iter.next();
            if (iwr instanceof IWorkflowFileResult) {
                IWorkflowFileResult iwfr = (IWorkflowFileResult) iwr;
                if (iwfr.getWorkflowElement().getClass().getCanonicalName().equals(afc.getClass().getCanonicalName())) {
                    if (iwfr.getFile().getAbsolutePath().endsWith(fileExtension)) {
                        l.add(iwr);
                    }
                }
            }
        }
        return l;
    }
}