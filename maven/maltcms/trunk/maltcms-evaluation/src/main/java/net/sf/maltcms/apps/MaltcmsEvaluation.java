/*
 * $license$
 *
 * $Id$
 */
package net.sf.maltcms.apps;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import net.sf.maltcms.evaluation.spi.Evaluation;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
public class MaltcmsEvaluation implements Runnable {

    private final File outputDirectory;
    private final File templateFile;
    private final File settingsFile;
    private final File configurationFile;
    
    

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static void main(String[] args) {
        Options options = new Options();
        Option template = OptionBuilder.withArgName("TEMPLATE").hasOptionalArg().
                withDescription("Template beans.xml file.").create("t");
        Option settings = OptionBuilder.withArgName("SETTINGS").hasArg().
                isRequired().withDescription(
                "Settings property file containing key,value mappings for template.").
                create("s");
        Option output = OptionBuilder.withArgName("OUTPUT").hasArg().isRequired().
                withDescription("Base directory for storage of output.").create(
                "o");
        Option config = OptionBuilder.withArgName("CONFIGURATION").hasArg().
                isRequired().withDescription(
                "Configuration property file for maltcms.").
                create("c");


        options.addOption(template);
        options.addOption(settings);
        options.addOption(output);

        if (args.length == 0) {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp(
                    "java -cp maltcms.jar "
                    + Evaluation.class.getCanonicalName(), options,
                    true);
            System.exit(1);
        }
        GnuParser gp = new GnuParser();
        try {
            CommandLine cl = gp.parse(options, args);
            File outputDir = new File(System.getProperty("user.dir"));
            if (cl.hasOption("o")) {
                outputDir = new File(cl.getOptionValue("o"));
            }
            File templateFile = new File(cl.getOptionValue("t"));
            File settingsFile = new File(cl.getOptionValue("s"));
            File configFile = new File(cl.getOptionValue("c"));
            MaltcmsEvaluation meval = new MaltcmsEvaluation(outputDir,
                    templateFile, settingsFile, configFile);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        }
    }
}
