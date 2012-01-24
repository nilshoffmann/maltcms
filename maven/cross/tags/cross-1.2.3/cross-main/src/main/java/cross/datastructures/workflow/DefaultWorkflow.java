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
 * $Id: DefaultWorkflow.java 116 2010-06-17 08:46:30Z nilshoffmann $
 */
package cross.datastructures.workflow;

import cross.Factory;
import cross.annotations.Configurable;
import cross.commands.fragments.AFragmentCommand;
import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.pipeline.ICommandSequence;
import cross.datastructures.tuple.TupleND;
import cross.event.*;
import cross.exception.ConstraintViolationException;
import cross.io.misc.DefaultConfigurableFileFilter;
import cross.io.xml.IXMLSerializable;
import cross.tools.StringTools;
import java.io.*;
import java.util.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * A default implementation of {@link cross.datastructures.workflow.IWorkflow}.
 * Is a source of <code>IEvent<IWorkflowResult></code> events.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Slf4j
public class DefaultWorkflow implements IWorkflow, IXMLSerializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1781229121330043626L;
    private ArrayList<IWorkflowResult> al = new ArrayList<IWorkflowResult>();
    private IEventSource<IWorkflowResult> iwres = new EventSource<IWorkflowResult>();
    private ICommandSequence commandSequence = null;
    private String name = "workflow";
    private IFragmentCommand activeCommand = null;
    @Configurable
    private String xslPathPrefix;
    @Configurable(name = "resultFileFilter")
    private String fileFilter = DefaultConfigurableFileFilter.class.getName();
    @Configurable
    private boolean saveHTML = false;
    @Configurable
    private boolean saveTEXT = false;
//    @Configurable
//    private boolean saveInFragmentCommandDir = false;
    private Date date = new Date();
    private Configuration cfg = new PropertiesConfiguration();
    private boolean executeLocal = true;
    private File outputDirectory = new File(System.getProperty("user.dir"));

    @Override
    public void addListener(final IListener<IEvent<IWorkflowResult>> l) {
        this.iwres.addListener(l);
    }

    @Override
    public void append(final IWorkflowResult iwr) {
        if (this.al == null) {
            this.al = new ArrayList<IWorkflowResult>();
        }
        if (iwr instanceof IWorkflowProgressResult) {
            final IWorkflowProgressResult iwpr = (IWorkflowProgressResult) iwr;
            log.info("Step {}/{}, Overall progress: {}%", new Object[]{
                        iwpr.getCurrentStep(), iwpr.getNumberOfSteps(),
                        iwpr.getOverallProgress()});
        } else {
            this.al.add(iwr);
        }
        fireEvent(new AEvent<IWorkflowResult>(iwr, DefaultWorkflow.this.iwres));
    }

    @Override
    public void appendXML(final Element e) {
        log.debug("Appending xml for DefaultWorkflow " + getName());
        // Element workflow = new Element("workflow");
        // workflow.setAttribute("class", this.getClass().getCanonicalName());
        // workflow.setAttribute("name", name);
        getCommandSequence().appendXML(e);
        for (final IWorkflowResult wr : this.al) {
            wr.appendXML(e);
//            final Element iwr = new Element("workflowElementResult");
//            iwr.setAttribute("class", wr.getClass().getCanonicalName());
//            iwr.setAttribute("slot", wr.getWorkflowSlot().name());
//            iwr.setAttribute("generator", wr.getWorkflowElement().getClass().
//                    getCanonicalName());
//
//            if (wr instanceof IWorkflowFileResult) {
//                final Element resources = new Element("resources");
//                final Element resource = new Element("resource");
//                resource.setAttribute("file",
//                        ((IWorkflowFileResult) wr).getFile().getAbsolutePath());
//                resources.addContent(resource);
//                iwr.addContent(resources);
//            }
//
//            e.addContent(iwr);
        }
        // e.addContent(workflow);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.IConfigurable#configure(org.apache.commons.configuration.Configuration
     * )
     */
    @Override
    public void configure(final Configuration cfg) {
        this.saveHTML = cfg.getBoolean(this.getClass().getName() + ".saveHTML",
                false);
        this.saveTEXT = cfg.getBoolean(this.getClass().getName() + ".saveTEXT",
                false);
        this.xslPathPrefix = cfg.getString(this.getClass().getName()
                + ".xslPathPrefix", "");
        this.fileFilter = cfg.getString(this.getClass().getName()
                + ".resultFileFilter", DefaultConfigurableFileFilter.class.
                getName());
//        this.saveInFragmentCommandDir = cfg.getBoolean(this.getClass().getName()
//                + ".saveInFragmentCommandDir", true);
    }

    @Override
    public void fireEvent(final IEvent<IWorkflowResult> e) {
        this.iwres.fireEvent(e);
    }

    /**
     * @return the ICommandSequence
     */
    @Override
    public ICommandSequence getCommandSequence() {
        return this.commandSequence;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflow#getConfiguration()
     */
    @Override
    public Configuration getConfiguration() {
        return this.cfg;
    }

    /**
     * @return the fileFilter
     */
    public String getFileFilter() {
        return this.fileFilter;
    }

    /**
     * @return the iwres
     */
    public IEventSource<IWorkflowResult> getIwres() {
        return this.iwres;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Iterator<IWorkflowResult> getResults() {
        return this.al.iterator();
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflow#getStartupDate()
     */
    @Override
    public Date getStartupDate() {
        return this.date;
    }

    /**
     * @return the IWorkflowResults as List
     */
    public ArrayList<IWorkflowResult> getWorkflowResults() {
        return this.al;
    }

    /**
     * @return the xslPathPrefix
     */
    public String getXslPathPrefix() {
        return this.xslPathPrefix;
    }

    @Override
    public void readXML(final Element e) throws IOException,
            ClassNotFoundException {
        final String workflowname = e.getAttributeValue("name");
        if (workflowname != null) {
            setName(workflowname);
        }
        final List<?> l = e.getChildren("workflowElementResult");
        for (final Object obj : l) {
            final Element elem = (Element) obj;
            final String cls = elem.getAttributeValue("class");
            final String slot = elem.getAttributeValue("slot");
            final String generator = elem.getAttributeValue("generator");
            final String file = elem.getAttributeValue("file");
            final IWorkflowFileResult iwr = Factory.getInstance().
                    getObjectFactory().instantiate(cls,
                    IWorkflowFileResult.class);
            final IWorkflowElement iwe = Factory.getInstance().getObjectFactory().
                    instantiate(generator,
                    IWorkflowElement.class);
            iwr.setWorkflowElement(iwe);
            iwr.setFile(new File(file));
            iwr.setWorkflowSlot(WorkflowSlot.valueOf(slot));
            append(iwr);
        }
    }

    @Override
    public void removeListener(final IListener<IEvent<IWorkflowResult>> l) {
        this.iwres.removeListener(l);
    }

    @Override
    public void save() {
        //TODO extract as IWorkflowPostProcessor
        try {
            final String wflname = getName();
            log.info("Saving workflow {}", wflname);
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
                final File f = new File(getOutputDirectory(), getName()+".xml");//new File(wflname);
                final File dir = f.getParentFile();
                dir.mkdirs();
                f.createNewFile();
                outp.output(doc, new BufferedOutputStream(new FileOutputStream(
                        f)));
//                final WorkflowZipper wz = Factory.getInstance().getObjectFactory().
//                        instantiate(WorkflowZipper.class);
//                wz.setFileFilter(Factory.getInstance().getObjectFactory().
//                        instantiate(this.fileFilter,
//                        java.io.FileFilter.class));
//                wz.setIWorkflow(this);
//                final File results = new File(dir, "maltcmsResults.zip");
//                if (wz.save(results)) {
//                    BinaryFileBase64Wrapper.base64Encode(results, new File(dir,
//                            results.getName() + ".b64"));
//                } else {
//                    log.debug("Did not Base64 encode maltcmsResults.zip");
//                }
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
            final InputStream xsstyler = getClass().getClassLoader().
                    getResourceAsStream("res/xslt/maltcmsHTMLResult.xsl");
            final StreamSource sstyles = new StreamSource(xsstyler);
            final FileInputStream xsr = new FileInputStream(f);
            final FileOutputStream xsw = new FileOutputStream(new File(f.
                    getParentFile(), "workflow.html"));
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
            final InputStream xsstyler = getClass().getClassLoader().
                    getResourceAsStream("res/xslt/maltcmsTEXTResult.xsl");
            final StreamSource sstyles = new StreamSource(xsstyler);
            final FileInputStream xsr = new FileInputStream(f);
            final FileOutputStream xsw = new FileOutputStream(new File(f.
                    getParentFile(), "workflow.txt"));
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
     * @param commandSequence
     *            the ICommandSequence to set
     */
    @Override
    public void setCommandSequence(final ICommandSequence ics) {
        this.commandSequence = ics;
        this.commandSequence.setWorkflow(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.workflow.IWorkflow#setConfiguration(org.apache.commons
     * .configuration.Configuration)
     */
    @Override
    public void setConfiguration(final Configuration configuration) {
        this.cfg = configuration;

    }

    /**
     * @param iwres
     *            the EventSource<IWorkflowResult> to set
     */
    public void setEventSource(final IEventSource<IWorkflowResult> iwres) {
        this.iwres = iwres;
    }

    /**
     * @param fileFilter
     *            the fileFilter to set
     */
    public void setFileFilter(final String fileFilter) {
        this.fileFilter = fileFilter;
    }

    @Override
    public void setName(final String name1) {
        this.name = name1;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.workflow.IWorkflow#setStartupDate(java.util.Date)
     */
    @Override
    public void setStartupDate(final Date date1) {
        this.date = date1;
    }

//    /**
//     * @param al
//     *            the IWorkflowResult List to set
//     */
//    public void setWorkflowResult(final ArrayList<IWorkflowResult> al) {
//        this.al = al;
//    }
    /**
     * @param xslPathPrefix
     *            the xslPathPrefix to set
     */
    public void setXslPathPrefix(final String xslPathPrefix) {
        this.xslPathPrefix = xslPathPrefix;
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
                    File outputFile = new File(
                            outputDirectory, String.format(
                            "%0" + digits + "d", i)
                            + "_" + iwa.getClass().getSimpleName());
                    outputFile.mkdirs();
                    log.info("Output dir for object of type {}: {}",iwe.getClass().getSimpleName(),this.outputDirectory);
                    return outputFile;
                }
                i++;
            }
            if (activeCommand != null) {
//                if (saveInFragmentCommandDir) {
//                    return getOutputDirectory(activeCommand);
//                } else {
                File dir = new File(getOutputDirectory(activeCommand), iwe.
                        getClass().getSimpleName());
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                log.info("Output dir for object of type {}: {}",iwe.getClass().getSimpleName(),this.outputDirectory);
                return dir;
//                }
            }
        } else if (iwe instanceof IWorkflowElement) {
            if (activeCommand != null) {
//                if (saveInFragmentCommandDir) {
//                    return getOutputDirectory(activeCommand);
//                } else {
                File dir = new File(getOutputDirectory(activeCommand), iwe.
                        getClass().getSimpleName());
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                log.info("Output dir for object of type {}: {}",iwe.getClass().getSimpleName(),this.outputDirectory);
                return dir;
//                }
            }
        }

        File outputFile = new File(outputDirectory,
                iwe.getClass().getSimpleName());
        outputFile.mkdirs();
        log.info("Output dir for object of type {}: {}",iwe.getClass().getSimpleName(),this.outputDirectory);
        return outputFile;
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
                if (iwfr.getWorkflowElement().getClass().getCanonicalName().
                        equals(afc.getClass().getCanonicalName())) {
                    if (iwfr.getFile().getAbsolutePath().endsWith(fileExtension)) {
                        l.add(iwr);
                    }
                }
            }
        }
        return l;
    }
    
    @Override
    public <T> List<IWorkflowObjectResult> getResultsOfType(IWorkflowElement afc,
            Class<? extends T> c) {
        List<IWorkflowObjectResult> l = new ArrayList<IWorkflowObjectResult>();
        Iterator<IWorkflowResult> iter = getResults();
        while (iter.hasNext()) {
            IWorkflowResult iwr = iter.next();
            if (iwr instanceof IWorkflowObjectResult) {
                IWorkflowObjectResult iwfr = (IWorkflowObjectResult) iwr;
                if (iwfr.getWorkflowElement().getClass().getCanonicalName().
                        equals(afc.getClass().getCanonicalName())) {
                    if (c.isAssignableFrom(iwfr.getObject().getClass())) {
                        l.add((IWorkflowObjectResult)iwr);
                    }
                }
            }
        }
        return l;
    }

    @Override
    public boolean isExecuteLocal() {
        return executeLocal;
    }

    @Override
    public void setExecuteLocal(boolean executeLocal) {
        this.executeLocal = executeLocal;
    }

    @Override
    public TupleND<IFileFragment> call() throws Exception {
        TupleND<IFileFragment> results = null;
        if(commandSequence.isCheckCommandDependencies() && !commandSequence.validate()){
            throw new ConstraintViolationException("Pipeline validation failed! Check output for details!");
        }
        while (commandSequence.hasNext()) {
            results = commandSequence.next();
        }
        return results;
    }

    @Override
    public File getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    public void setOutputDirectory(File f) {
        this.outputDirectory = f;
        log.info("Workflow output base directory: "+this.outputDirectory);
    }

}
