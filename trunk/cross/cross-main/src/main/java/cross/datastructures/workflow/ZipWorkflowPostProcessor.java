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

import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowPostProcessor;
import cross.io.misc.BinaryFileBase64Wrapper;
import cross.io.misc.WorkflowZipper;
import java.io.File;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Zips a workflow and creates a base64 encoded version of the zip file, 
 * if configured to do so.
 * 
 * @author Nils Hoffmann
 */
@Slf4j
@Data
public class ZipWorkflowPostProcessor implements IWorkflowPostProcessor {

    private WorkflowZipper workflowZipper = new WorkflowZipper();
    private File outputDirectory = null;
    private boolean useCustomName = false;
    private boolean saveInOutputDirectory = false;
    private String fileName = "maltcmsResults.zip";
    private boolean encodeBase64 = false;
    
    @Override
    public void process(IWorkflow workflow) {
        workflowZipper.setIWorkflow(workflow);
        if(outputDirectory == null) {
            if(saveInOutputDirectory) {
                outputDirectory = workflow.getOutputDirectory();
            }else{
                outputDirectory = workflow.getOutputDirectory().getParentFile();
            }
        }
        String name;
        if(useCustomName) {
            name = fileName;
        }else{
            name = workflow.getOutputDirectory().getName()+".zip";
        }
        final File results = new File(outputDirectory, name);
        
        if (workflowZipper.save(results) && encodeBase64) {
            BinaryFileBase64Wrapper.base64Encode(results, new File(outputDirectory,
                    results.getName() + ".b64"));
        } else {
            log.debug("Did not Base64 encode "+name);
        }
    }
    
}
