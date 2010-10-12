/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
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
 * $Id$
 */

package cross.datastructures.workflow;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.configuration.Configuration;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;

import annotations.Configurable;
import cross.Factory;
import cross.Logging;
import cross.datastructures.pipeline.ICommandSequence;
import cross.datastructures.tuple.Tuple2D;
import cross.event.AEvent;
import cross.event.EventSource;
import cross.event.IEvent;
import cross.event.IEventSource;
import cross.event.IListener;
import cross.io.misc.BinaryFileBase64Wrapper;
import cross.io.misc.DefaultConfigurableFileFilter;
import cross.io.misc.WorkflowZipper;
import cross.io.xml.IXMLSerializable;

/**
 * A default implementation of {@link cross.datastructures.workflow.IWorkflow}.
 * Is a source of <code>IEvent<IWorkflowResult></code> events.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class DefaultWorkflow implements IWorkflow, IXMLSerializable {

	private final Logger log = Logging.getLogger(this);

	private ArrayList<IWorkflowResult> al = new ArrayList<IWorkflowResult>();

	private IEventSource<IWorkflowResult> iwres = new EventSource<IWorkflowResult>();

	private ICommandSequence ics = null;

	private String name = null;

	@Configurable(name = "xslPathPrefix")
	private String xslPathPrefix;

	private String fileFilter = DefaultConfigurableFileFilter.class.getName();

	private boolean saveHTML = false;

	private boolean saveTEXT = false;

	private Date date = new Date();

	private Configuration cfg;

	public void addListener(final IListener<IEvent<IWorkflowResult>> l) {
		this.iwres.addListener(l);
	}

	@Override
	public void append(final IWorkflowResult iwr) {
		if (this.al == null) {
			this.al = new ArrayList<IWorkflowResult>();
		}
		if (iwr instanceof IWorkflowProgressResult) {
			IWorkflowProgressResult iwpr = (IWorkflowProgressResult) iwr;
			log.info("Step {}/{}, Overall progress: {}%", new Object[] {
			        iwpr.getCurrentStep(), iwpr.getNumberOfSteps(),
			        iwpr.getOverallProgress() });
		} else {
			this.al.add(iwr);
		}
		fireEvent(new AEvent<IWorkflowResult>(iwr, DefaultWorkflow.this.iwres));
	}

	@Override
	public void appendXML(final Element e) {
		this.log.debug("Appending xml for DefaultWorkflow " + getName());
		// Element workflow = new Element("workflow");
		// workflow.setAttribute("class", this.getClass().getCanonicalName());
		// workflow.setAttribute("name", name);
		for (final IWorkflowResult wr : this.al) {
			final Element iwr = new Element("workflowElementResult");
			iwr.setAttribute("class", wr.getClass().getCanonicalName());
			iwr.setAttribute("slot", wr.getWorkflowSlot().name());
			iwr.setAttribute("generator", wr.getIWorkflowElement().getClass()
			        .getCanonicalName());
			if (wr instanceof IWorkflowFileResult) {
				iwr.setAttribute("file", ((IWorkflowFileResult) wr).getFile()
				        .getAbsolutePath());
				final Collection<Tuple2D<String, String>> iRes = ((IWorkflowFileResult) wr)
				        .getImmediateResults();
				final Element ir = new Element("immediateResults");
				for (final Tuple2D<String, String> tpl : iRes) {
					final Element el = new Element("result");
					el.setAttribute("name", tpl.getFirst());
					el.setAttribute("value", tpl.getSecond());
					ir.addContent(el);
				}
				iwr.addContent(ir);
				e.addContent(iwr);
			}

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
		        + ".resultFileFilter", DefaultConfigurableFileFilter.class
		        .getName());
	}

	public void fireEvent(final IEvent<IWorkflowResult> e) {
		this.iwres.fireEvent(e);
	}

	/**
	 * @return the ICommandSequence
	 */
	public ICommandSequence getCommandSequence() {
		return this.ics;
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
			final Element immediateResults = elem.getChild("immediateResults");
			final Collection<Tuple2D<String, String>> iRes = new ArrayList<Tuple2D<String, String>>();
			if (immediateResults != null) {
				for (final Object child : immediateResults
				        .getChildren("result")) {
					final Element res = (Element) child;
					final String rname = res.getAttributeValue("name");
					final String rvalue = res.getAttributeValue("value");
					iRes.add(new Tuple2D<String, String>(rname, rvalue));
				}
			}
			final IWorkflowFileResult iwr = Factory.getInstance().instantiate(
			        cls, IWorkflowFileResult.class);
			final IWorkflowElement iwe = Factory.getInstance().instantiate(
			        generator, IWorkflowElement.class);
			iwr.setIWorkflowElement(iwe);
			iwr.setFile(new File(file));
			iwr.setWorkflowSlot(WorkflowSlot.valueOf(slot));
			iwr.setImmediateResult(iRes);
			append(iwr);
		}
	}

	public void removeListener(final IListener<IEvent<IWorkflowResult>> l) {
		this.iwres.removeListener(l);
	}

	public void save() {
		try {
			final String wflname = getName();
			this.log.info("Saving workflow to file {}", wflname);
			final Document doc = new Document();
			final HashMap<String, String> hm = new HashMap<String, String>();
			hm.put("type", "text/xsl");
			hm.put("href", this.xslPathPrefix + "maltcmsResult.xsl");
			final ProcessingInstruction pi = new ProcessingInstruction(
			        "xml-stylesheet",
			        "type=\"text/xsl\" href=\"http://bibiserv.techfak.uni-bielefeld.de/chroma/maltcmsResult.xsl\"");
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
				final WorkflowZipper wz = Factory.getInstance().instantiate(
				        WorkflowZipper.class);
				wz.setFileFilter(Factory.getInstance().instantiate(
				        this.fileFilter, java.io.FileFilter.class));
				wz.setIWorkflow(this);
				final File results = new File(dir, "maltcmsResults.zip");
				if (wz.save(results)) {
					BinaryFileBase64Wrapper.base64Encode(results, new File(dir,
					        results.getName() + ".b64"));
				} else {
					this.log.debug("Did not Base64 encode maltcmsResults.zip");
				}
				if (this.saveHTML) {
					saveHTML(f);
				}
				if (this.saveTEXT) {
					saveTEXT(f);
				}
			} catch (final IOException e) {
				this.log.error(e.getLocalizedMessage());
			}
		} catch (final FileNotFoundException e) {
			this.log.error(e.getLocalizedMessage());
		} catch (final IOException e) {
			this.log.error(e.getLocalizedMessage());
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
			final InputStream xsstyler = getClass().getClassLoader()
			        .getResourceAsStream("res/xslt/maltcmsHTMLResult.xsl");
			final StreamSource sstyles = new StreamSource(xsstyler);
			final FileInputStream xsr = new FileInputStream(f);
			final FileOutputStream xsw = new FileOutputStream(new File(f
			        .getParentFile(), "workflow.html"));
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
			final InputStream xsstyler = getClass().getClassLoader()
			        .getResourceAsStream("res/xslt/maltcmsTEXTResult.xsl");
			final StreamSource sstyles = new StreamSource(xsstyler);
			final FileInputStream xsr = new FileInputStream(f);
			final FileOutputStream xsw = new FileOutputStream(new File(f
			        .getParentFile(), "workflow.txt"));
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
	 * @param ics
	 *            the ICommandSequence to set
	 */
	public void setCommandSequence(final ICommandSequence ics) {
		this.ics = ics;
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

	/**
	 * @param al
	 *            the IWorkflowResult List to set
	 */
	public void setWorkflowResult(final ArrayList<IWorkflowResult> al) {
		this.al = al;
	}

	/**
	 * @param xslPathPrefix
	 *            the xslPathPrefix to set
	 */
	public void setXslPathPrefix(final String xslPathPrefix) {
		this.xslPathPrefix = xslPathPrefix;
	}

	public Element writeXML() throws IOException {
		final Element root = new Element("workflow");
		root.setAttribute("class", getClass().getCanonicalName());
		root.setAttribute("name", getName());
		appendXML(root);
		return root;
	}

}
