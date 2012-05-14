/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
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
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
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
        } catch (JDOMException ex) {
            Logger.getLogger(MaltcmsTaskResult.class.getName()).
                    log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MaltcmsTaskResult.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }

}