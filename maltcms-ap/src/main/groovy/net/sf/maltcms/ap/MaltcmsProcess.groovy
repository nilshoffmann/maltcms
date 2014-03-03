/*
 * Maltcms, modular application toolkit for chromatography mass-spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
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

/**
 *
 * @author Nils Hoffmann
 */
class MaltcmsProcess implements Runnable {

    List<String> commandLine
    OutputAction outputAction
    File workingDirectory = new File(System.getProperty("user.dir"))

    OutputStream out = System.out
    OutputStream err = System.err

    Boolean finished = false

    Process process

    public void run() {
        println "Running maltcms... with commandline ${commandLine}"
        //def processBuilder = new ProcessBuilder(commandLine)
        println "Working directory: ${workingDirectory}"
        //processBuilder.directory(workingDirectory)
        println "Starting process builder"
        process = commandLine.execute(null,workingDirectory)
        //processBuilder.start()
        println "Consuming process output"
        process.consumeProcessOutput(out,err)
        process.waitFor()
        println("Process exited with value ${process.exitValue()}")
        if(process.exitValue()==0) {
            finished = true
            outputAction.run()
        }
        process = null
    }

}

