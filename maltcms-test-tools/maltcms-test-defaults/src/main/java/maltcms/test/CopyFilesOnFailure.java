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
package maltcms.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Nils Hoffmann
 */

@Slf4j
public class CopyFilesOnFailure {
	
	public static void copyToInspectionDir(File outputDir, Throwable t) {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File outDir = new File(tmpDir, "maltcms-test-failures");
        UUID uid = UUID.randomUUID();
        File instanceDir = new File(outDir, uid.toString());
        instanceDir.mkdirs();
        try {
            File f = new File(instanceDir, "stacktrace.txt");
            PrintWriter bw = null;
            try {
                bw = new PrintWriter(f);
                Throwable cause = t.getCause();
                while(cause!=null) {
                    cause.printStackTrace(bw);
                    cause = cause.getCause();
                }
                bw.flush();
            } catch (IOException ioex) {
                log.error("Received io exception while creating stacktrace file!", ioex);
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }
            System.out.println("Copying output to inspection directory: " + instanceDir.getAbsolutePath());
            FileUtils.copyDirectoryToDirectory(outputDir, instanceDir);
        } catch (IOException ex) {
            log.error("Failed to copy output to inspection directory!", ex);
        }
    }
	
}
