/*
 * Maltcms, modular application toolkit for chromatography mass-spectrometry.
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
package net.sf.maltcms.ap

import javax.swing.JTextArea
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

/**
 *
 * @author Nils Hoffmann
 */
class MaltcmsExecution {

    JTextArea textArea
    MaltcmsProcess activeProcess
    String inputFiles
    File outputBaseDir = new File(System.getProperty("user.dir"),"ap-output")
    File workingDirectory = new File(System.getProperty("user.dir"))
    File apProperties = null
    ExecutorService es = Executors.newSingleThreadExecutor()
    String arguments = "-Xmx1G"
    Integer parallelThreads = 1

    public void start() {
        if(activeProcess==null) {
            println "Process started"
            es = Executors.newSingleThreadExecutor()
            textArea.setText("")
            File outputDir = getOutputDir(outputBaseDir)
            File apDir = new File(System.getProperty("ap.home"))
			if(apProperties==null) {
				apProperties = new File(apDir,"cfg/pipelines/ap.mpl")
			}
			println "Using pipeline ${apProperties}"
            File apParameters = new File(System.getProperty("user.dir"),"ap-parameters.properties")
            File maltcmsDir = new File(System.getProperty("maltcms.home"))
            def commandLine = ["java"]
            def argsList = arguments.split(" ")
            argsList.each{ arg ->
                commandLine << arg
            }
            commandLine << "-Dmaltcms.parallelThreads=${parallelThreads}"
            commandLine << "-Dmaltcms.home=${maltcmsDir.absolutePath}"
            commandLine << "-Djava.util.logging.config.file=${maltcmsDir.absolutePath}/cfg/logging.properties"
            commandLine << "-Dlog4j.configuration=file:${maltcmsDir.absolutePath.replaceAll(" ","%20")}/cfg/log4j.properties"
            commandLine << "-DomitUserTimePrefix=true"
            commandLine << "-DconfigLocation=file:${apParameters.absolutePath.replaceAll(" ","%20")}"
            commandLine << "-jar"
            commandLine << "${maltcmsDir.absolutePath}/maltcms.jar"
            commandLine << "-r"
            commandLine << "-c"
            commandLine << "\"${apProperties.absolutePath}\""
            commandLine << "-o"
            commandLine << "\"${outputDir.absolutePath}\""
            commandLine << "-f"
            commandLine << "\"${inputFiles}\""
            println "Running  ${commandLine.join(" ")}\n"
            textArea.append("Running ${commandLine.join(" ")}\n")
            activeProcess = new MaltcmsProcess(commandLine: commandLine,
                out: new AppendingOutputStream(textArea:textArea),
                err: new AppendingOutputStream(textArea:textArea),
                outputAction: new OutputAction(outputDir: outputDir),
                workingDirectory: workingDirectory)
            es.submit(activeProcess)
        }
    }

    File getOutputDir(File baseDir) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "MM-dd-yyyy_HH-mm-ss", Locale.US);
        String userName = System.getProperty("user.name", "default");
        File outputDir = new File(baseDir, userName);
        outputDir = new File(outputDir, dateFormat.format(
                new Date()));
        return outputDir
    }

    public void cancel() {
        if(activeProcess!=null && activeProcess.process!=null) {
            activeProcess.process.destroy()
            activeProcess.process.waitFor()
            println "Process killed"
            es.shutdownNow()
            es.awaitTermination(10,TimeUnit.SECONDS)
            activeProcess = null
            textArea.append("\nCanceled process!\n")
        }
    }
}

