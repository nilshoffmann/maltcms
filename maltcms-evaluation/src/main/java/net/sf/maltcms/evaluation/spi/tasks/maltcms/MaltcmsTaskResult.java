/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code
 * under. Certain files or entire directories may not be covered by this
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package net.sf.maltcms.evaluation.spi.tasks.maltcms;

import java.io.File;
import java.io.IOException;
import java.net.URI;
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
 * @author Nils Hoffmann
 */
@Data
public class MaltcmsTaskResult extends DefaultTaskResult {

    /**
     *
     */
    private static final long serialVersionUID = -4395347427746465402L;
    private final File workflow;

    public MaltcmsTaskResult(File outputDirectory) {
        super();
        workflow = new File(outputDirectory, "workflow.xml");
        SAXBuilder dbuilder = new SAXBuilder();
        try {
            Document doc = dbuilder.build(workflow);
            List<?> inList = XPath.selectNodes(doc,
                    "//workflowInputs/workflowInput");
            for (Object o : inList) {
                Element e = (Element) o;
                File inputFile = new File(URI.create(e.getAttributeValue("uri")));
                getTaskInputs().add(inputFile);
            }
            List<?> outList = XPath.selectNodes(doc,
                    "//workflowOutputs/workflowOutput");
            for (Object o : outList) {
                Element e = (Element) o;
                File outputFile = new File(
                        URI.create(e.getAttributeValue("uri")));
                getTaskOutputs().add(outputFile);
            }
        } catch (JDOMException | IOException ex) {
            Logger.getLogger(MaltcmsTaskResult.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }
}
