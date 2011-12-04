/*
 * $license$
 *
 * $Id$
 */
package net.sf.maltcms.apps;

import java.io.File;
import lombok.Data;
import org.apache.commons.cli.CommandLine;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
public abstract class MaltcmsEvaluation implements Runnable {

    private File outputDirectory;
    private File templateFile;
    private File parametersFile;
    private File configurationFile;
    private final CommandLine commandLine;

    public MaltcmsEvaluation(CommandLine commandLine) {
        this.commandLine = commandLine;
        outputDirectory = new File(System.getProperty("user.dir"));
        if (commandLine.hasOption("o")) {
            outputDirectory = new File(commandLine.getOptionValue("o"));
        }
        templateFile = new File(commandLine.getOptionValue("t"));
        parametersFile = new File(commandLine.getOptionValue("p"));
        configurationFile = new File(commandLine.getOptionValue("c"));

    }
}
