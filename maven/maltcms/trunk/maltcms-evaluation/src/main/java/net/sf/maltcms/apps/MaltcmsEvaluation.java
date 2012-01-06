/*
 * $license$
 *
 * $Id$
 */
package net.sf.maltcms.apps;

import java.io.File;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.io.PagingMemoryStorage;
import com.db4o.ta.DeactivatingRollbackStrategy;
import com.db4o.ta.TransparentActivationSupport;
import com.db4o.ta.TransparentPersistenceSupport;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.apache.commons.cli.*;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public abstract class MaltcmsEvaluation implements Runnable {
    
    private File outputDirectory;
    private File templateFile;
    
    private File parametersFile;
    private File configurationFile;
    private CommandLine commandLine = null;
    @Setter(AccessLevel.NONE)
    private ObjectContainer database = null;

    public MaltcmsEvaluation(String[] args) {
        Options options = createOptions();
        GnuParser gp = new GnuParser();
        try {
            commandLine = gp.parse(options, args);
            if (args.length == 0 || commandLine.hasOption("h")) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp(
                        "java -cp maltcms.jar "
                                + MaltcmsEvaluation.class.getCanonicalName(), options,
                        true);
                System.exit(1);
            }
            handleOptions(commandLine);
        } catch (ParseException e) {
            e.printStackTrace();
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp(
                    "java -cp maltcms.jar "
                            + MaltcmsEvaluation.class.getCanonicalName(), options,
                    true);
            System.exit(1);
        }
    }

    public void handleOptions(CommandLine cmdLine) {
        outputDirectory = new File(System.getProperty("user.dir"));
        if (commandLine.hasOption("o")) {
            outputDirectory = new File(commandLine.getOptionValue("o"));
        }
        templateFile = new File(commandLine.getOptionValue("t"));
        parametersFile = new File(commandLine.getOptionValue("p"));
        configurationFile = new File(commandLine.getOptionValue("c"));
    }
    
    public Options createOptions() {
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
        return options;
    }
    
    public ObjectContainer getDatabase() {
        if(this.database == null) {
            EmbeddedConfiguration configuration = Db4oEmbedded.newConfiguration();
            configuration.common().add(new TransparentActivationSupport());
            configuration.common().add(new TransparentPersistenceSupport(new DeactivatingRollbackStrategy()));
            PagingMemoryStorage memory = new PagingMemoryStorage();
            configuration.file().storage(memory);
            this.database = Db4oEmbedded.openFile(configuration,new File(getOutputDirectory(), "evaluation.db").getAbsolutePath());
        }
        return this.database.ext().openSession();
    }
    
    public File getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public File getTemplateFile() {
		return templateFile;
	}

	public void setTemplateFile(File templateFile) {
		this.templateFile = templateFile;
	}

	public File getParametersFile() {
		return parametersFile;
	}

	public void setParametersFile(File parametersFile) {
		this.parametersFile = parametersFile;
	}

	public File getConfigurationFile() {
		return configurationFile;
	}

	public void setConfigurationFile(File configurationFile) {
		this.configurationFile = configurationFile;
	}
}
