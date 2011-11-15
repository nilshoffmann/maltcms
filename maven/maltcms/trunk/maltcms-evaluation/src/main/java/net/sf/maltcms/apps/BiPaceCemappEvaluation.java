/*
 * $license$
 *
 * $Id$
 */
package net.sf.maltcms.apps;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.maltcms.evaluation.spi.caap.BeansXmlGenerator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class BiPaceCemappEvaluation extends MaltcmsEvaluation {

    public BiPaceCemappEvaluation(CommandLine commandLine) {
        super(commandLine);
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
        Option help = OptionBuilder.withArgName("HELP").withLongOpt("--help").
                create("h");


        options.addOption(template);
        options.addOption(settings);
        options.addOption(output);
        options.addOption(help);

        GnuParser gp = new GnuParser();
        try {
            CommandLine cl = gp.parse(options, args);
            if (args.length == 0 || cl.hasOption("h")) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp(
                        "java -cp maltcms.jar "
                        + MaltcmsEvaluation.class.getCanonicalName(), options,
                        true);
                System.exit(1);
            }
            BiPaceCemappEvaluation me = new BiPaceCemappEvaluation(cl);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        }

    }

    @Override
    public void run() {
        LinkedHashMap<String, List<?>> templateProperties = new LinkedHashMap<String, List<?>>();
        try {
            //these are the actual parameters that are varied
            PropertiesConfiguration pc = new PropertiesConfiguration(
                    getSettingsFile());
            Iterator keys = pc.getKeys();
            while (keys.hasNext()) {
                String key = keys.next().toString();
                List<?> l = pc.getList(key);
                templateProperties.put(key, l);
            }

            //now create template properties file with unique name
            //create maltcms runtime properties from chromaDefaults.properties
            //setting the pipeline.xml key to the correct location of the 
            //pipeline xml configuration
            //then call maltcms with -Dmaltcms.config.location=path/to/evaluation.properties
            //which contains the parameter settings to be used by XMLApplicationContext
            BeansXmlGenerator bxg = new BeansXmlGenerator(templateProperties,
                    getTemplateFile(), getOutputDirectory());
            while (bxg.hasNext()) {
                //base for 
                File config = bxg.next();
            }
            //preprocessing stage



            //bipace stage

            //bipace + cemapp stage

            //cemapp stage

        } catch (ConfigurationException ex) {
            Logger.getLogger(BiPaceCemappEvaluation.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }
}
