/*
 * $license$
 *
 * $Id$
 */
package net.sf.maltcms.evaluation.spi.tasks.maltcms;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */


@Data
public class WorkflowResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4395347427746465402L;
	
    private final File workflow;
    private final List<File> workflowInputs;
    private final List<File> workflowOutputs;

    public WorkflowResult(File outputDirectory) {
        workflow = new File(outputDirectory, "workflow.xml");
        workflowInputs = new LinkedList<File>();
        workflowOutputs = new LinkedList<File>();
        SAXBuilder dbuilder = new SAXBuilder();
        try {
            Document doc = dbuilder.build(workflow);
            List<?> inList = XPath.selectNodes(doc,
                    "//workflowInputs/workflowInput");
            for (Object o : inList) {
                Element e = (Element) o;
                File inputFile = new File(URI.create(e.getAttributeValue("uri")));
                workflowInputs.add(inputFile);
            }
            List<?> outList = XPath.selectNodes(doc,
                    "//workflowOutputs/workflowOutput");
            for (Object o : outList) {
                Element e = (Element) o;
                File outputFile = new File(
                        URI.create(e.getAttributeValue("uri")));
                workflowOutputs.add(outputFile);
            }
        } catch (JDOMException ex) {
            Logger.getLogger(WorkflowResult.class.getName()).
                    log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WorkflowResult.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }
}
