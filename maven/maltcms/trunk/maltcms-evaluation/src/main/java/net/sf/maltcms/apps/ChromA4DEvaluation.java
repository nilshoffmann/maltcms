/*
 * $license$
 *
 * $Id$
 */
package net.sf.maltcms.apps;

import cross.tools.StringTools;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.maltcms.evaluation.api.IPostProcessor;
import net.sf.maltcms.evaluation.spi.BeansXmlGenerator;
import net.sf.maltcms.evaluation.spi.tasks.Task;
import net.sf.maltcms.execution.api.ICompletionService;
import net.sf.maltcms.execution.spi.CompletionServiceFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class ChromA4DEvaluation extends MaltcmsEvaluation {

    public ChromA4DEvaluation(CommandLine commandLine) {
        super(commandLine);
    }

    public static void main(String[] args) {
        Options options = new Options();
        Option template = OptionBuilder.withArgName("TEMPLATE").hasArg().
                isRequired().
                withDescription("Template beans.xml file.").create("t");
        Option settings = OptionBuilder.withArgName("PARAMETERS").hasArg().
                isRequired().withDescription(
                "Property file containing key,value mappings for template parameters.").
                create("p");
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
        options.addOption(config);
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
            ChromA4DEvaluation me = new ChromA4DEvaluation(cl);
            me.run();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp(
                    "java -cp maltcms.jar "
                    + MaltcmsEvaluation.class.getCanonicalName(), options,
                    true);
            System.exit(1);
        }

    }

    @Override
    public void run() {
        //preprocessing stage
        /**
         * templateFile for creating a maltcms pipeline
         * maltcmsDefaultProperties
         * 
         * 
         * 
         */
        LinkedHashMap<String, List<?>> templateProperties = new LinkedHashMap<String, List<?>>();
        try {
            //these are the actual parameters that are varied
            PropertiesConfiguration pc = new PropertiesConfiguration(
                    getParametersFile());
            Iterator keys = pc.getKeys();
            while (keys.hasNext()) {
                String key = keys.next().toString();
                List<?> l = pc.getList(key);
                templateProperties.put(key, l);
            }
            
            PropertiesConfiguration defaultProps = new PropertiesConfiguration(
                    getConfigurationFile());
            String[] peakFilesArray = defaultProps.getStringArray("peakFiles");
            StringBuilder sb = new StringBuilder();
            for(String str:peakFilesArray) {
                sb.append("<value>");
                sb.append(str);
                sb.append("</value>");
            }
            
            HashMap<String,String> tokenMap = new HashMap<String,String>();

            tokenMap.put("peakFiles",sb.toString());
            //now create template properties file with unique name
            //create maltcms runtime properties from chromaDefaults.properties
            //setting the pipeline.xml key to the correct location of the 
            //pipeline xml configuration
            //then call maltcms with -Dmaltcms.config.location=path/to/evaluation.properties
            //which contains the parameter settings to be used by XMLApplicationContext
            BeansXmlGenerator bxg = new BeansXmlGenerator(templateProperties,
                    getTemplateFile(), getOutputDirectory(),tokenMap);
            
            int batchSize = 100;
            CompletionServiceFactory<Boolean> csf = new CompletionServiceFactory<Boolean>();
            int nchoices = bxg.size();
            int failed = 0;
            int submitted = 0;
            int batch = 0;
            System.out.println(
                    "Using " + nchoices + " different configurations!");

            while (submitted + failed < nchoices) {
                ICompletionService<Boolean> ics = csf.
                        createVMLocalCompletionService();
                for (int i = 0; i < batchSize; i++) {
                    if (bxg.hasNext()) {
                        //base for 
                        File config = bxg.next();
                        System.out.println("Handling configuration " + config+" as element "+(i+1)+"/"+batchSize+" in batch "+(batch+1));
                        File outputDirectory = config.getParentFile();
                        File propertiesFile = new File(outputDirectory,
                                StringTools.removeFileExt(config.getName()) + ".properties");
//                        File parametersFile = new File(outputDirectory,StringTools.removeFileExt(config.getName())+".parameters");
                        PropertiesConfiguration properties = new PropertiesConfiguration(
                                propertiesFile);
                        ConfigurationUtils.copy(defaultProps, properties);
                        properties.setProperty("pipeline.xml", "file:"+config.getAbsolutePath());
                        properties.save();
                        //"-DparamsLocation="+parametersFile.getAbsolutePath(),
//                        Task t = new Task(Arrays.asList("java","-jar",
//                                "maltcms.jar", "-i", "data/", "-f", "*.cdf",
//                                "-o", new File(outputDirectory.getAbsolutePath(),"workflow").getAbsolutePath(), "-c",
//                                propertiesFile.getAbsolutePath()), new File("."),
//                                new LinkedList<IPostProcessor>());
//                        ics.submit(t);
                        submitted++;
                    } else {
                        break;
                    }
                }
                try {
                    List<Boolean> results = ics.call();
                    failed+=ics.getFailedTasks().size();
                } catch (Exception ex) {
                    Logger.getLogger(ChromA4DEvaluation.class.getName()).
                            log(Level.SEVERE, null, ex);
                }
                batch++;
            }

            //bipace stage

            //-Dmaltcms.config.location=/location/to/my.

            //bipace + cemapp stage

            //cemapp stage

        } catch (ConfigurationException ex) {
            Logger.getLogger(ChromA4DEvaluation.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }
}
