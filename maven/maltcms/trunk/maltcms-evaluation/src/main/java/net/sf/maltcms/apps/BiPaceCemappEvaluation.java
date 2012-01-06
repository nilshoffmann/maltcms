/*
 * $license$
 *
 * $Id$
 */
package net.sf.maltcms.apps;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.maltcms.evaluation.api.tasks.IPostProcessor;
import net.sf.maltcms.evaluation.api.tasks.ITask;
import net.sf.maltcms.evaluation.api.tasks.ITaskResult;
import net.sf.maltcms.evaluation.spi.BeansXmlGenerator;
import net.sf.maltcms.evaluation.spi.tasks.Task;
import net.sf.maltcms.evaluation.spi.tasks.maltcms.MaltcmsTaskResult;
import net.sf.maltcms.execution.api.ICompletionService;
import net.sf.maltcms.execution.spi.CompletionServiceFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;

import cross.tools.StringTools;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class BiPaceCemappEvaluation extends MaltcmsEvaluation {

    public BiPaceCemappEvaluation(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        BiPaceCemappEvaluation bpce = new BiPaceCemappEvaluation(args);
        bpce.run();
    }
    
    @Override
    public void handleOptions(CommandLine cmdLine) {
        super.handleOptions(cmdLine);
    }
    
    @Override
    public Options createOptions() {
        return super.createOptions();
    }

    @Override
    public void run() {
        
        Map<String,String> taskArguments;
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
            CompletionServiceFactory<ITaskResult> csf = new CompletionServiceFactory<ITaskResult>();
            int nchoices = bxg.size();
            int failed = 0;
            int submitted = 0;
            int batch = 0;
            System.out.println(
                    "Using " + nchoices + " different configurations!");

            while (submitted + failed < nchoices) {
                ICompletionService<ITaskResult> ics = csf.
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
                        File odir = new File(outputDirectory.getAbsolutePath(),"workflow");
                        //"-DparamsLocation="+parametersFile.getAbsolutePath(),
                        ITask t = new Task(Arrays.asList("java","-jar",
                                "maltcms.jar", "-i", "data/", "-f", "*.cdf",
                                "-o", odir.getAbsolutePath(), "-c",
                                propertiesFile.getAbsolutePath()), new File("."),
                                new LinkedList<IPostProcessor>(),odir);
                        ics.submit(t);
                        submitted++;
                    } else {
                        break;
                    }
                }
                try {
                    List<ITaskResult> results = ics.call();
                    failed+=ics.getFailedTasks().size();
                } catch (Exception ex) {
                    Logger.getLogger(BiPaceCemappEvaluation.class.getName()).
                            log(Level.SEVERE, null, ex);
                }
                batch++;
            }

            //bipace stage

            //-Dmaltcms.config.location=/location/to/my.

            //bipace + cemapp stage

            //cemapp stage

        } catch (ConfigurationException ex) {
            Logger.getLogger(BiPaceCemappEvaluation.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }
}
