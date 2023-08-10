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

import cross.Factory;
import cross.annotations.AnnotationInspector;
import cross.commands.ICommand;
import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.pipeline.CommandPipeline;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflow;
import cross.datastructures.workflow.IWorkflow;
import cross.exception.ResourceNotAvailableException;
import cross.test.LogMethodName;
import cross.test.SetupLogging;
import cross.vocabulary.CvResolver;
import cross.vocabulary.ICvResolver;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;

/**
 * <p>
 * Abstract AFragmentCommandTest class.</p>
 *
 * @author Nils Hoffmann
 *
 */
public abstract class AFragmentCommandTest {
    
    private static Logger log = LoggerFactory.getLogger(AFragmentCommandTest.class);

    @Rule
    public TemporaryFolder tf = new TemporaryFolder();
    @Rule
    public SetupLogging sl = new SetupLogging();
    @Rule
    public LogMethodName lmn = new LogMethodName();
    @Rule
    public TestName name = new TestName();

    /**
     * <p>
     * setLogLevelFor.</p>
     *
     * @param prefix a {@link java.lang.String} object.
     * @param logLevel a {@link org.apache.log4j.Level} object.
     */
    protected void setLogLevelFor(String prefix, Level logLevel) {
        switch (logLevel.toInt()) {
            case Level.ALL_INT:
                sl.getConfig().put("log4j.category." + prefix, "ALL");
                break;
            case Level.DEBUG_INT:
                sl.getConfig().put("log4j.category." + prefix, "DEBUG");
                break;
            case Level.ERROR_INT:
                sl.getConfig().put("log4j.category." + prefix, "ERROR");
                break;
            case Level.FATAL_INT:
                sl.getConfig().put("log4j.category." + prefix, "FATAL");
                break;
            case Level.INFO_INT:
                sl.getConfig().put("log4j.category." + prefix, "INFO");
                break;
            case Level.OFF_INT:
                sl.getConfig().put("log4j.category." + prefix, "OFF");
                break;
            case Level.WARN_INT:
                sl.getConfig().put("log4j.category." + prefix, "WARN");
                break;
        }
        PropertyConfigurator.configure(sl.getConfig());
    }

    /**
     * <p>
     * setLogLevelFor.</p>
     *
     * @param cls a {@link java.lang.Class} object.
     * @param logLevel a {@link org.apache.log4j.Level} object.
     */
    public void setLogLevelFor(Class<?> cls, Level logLevel) {
        setLogLevelFor(cls.getName(), logLevel);
    }

    /**
     * <p>
     * setLogLevelFor.</p>
     *
     * @param pkg a {@link java.lang.Package} object.
     * @param logLevel a {@link org.apache.log4j.Level} object.
     */
    public void setLogLevelFor(Package pkg, Level logLevel) {
        setLogLevelFor(pkg.getName(), logLevel);
    }

    /**
     * <p>
     * createWorkflow.</p>
     *
     * @param outputDirectory a {@link java.io.File} object.
     * @param commands a {@link java.util.List} object.
     * @param inputFiles a {@link java.util.List} object.
     * @return a {@link cross.datastructures.workflow.IWorkflow} object.
     */
    public IWorkflow createWorkflow(File outputDirectory, List<IFragmentCommand> commands, List<File> inputFiles) {
        CommandPipeline cp = new CommandPipeline();
        List<IFileFragment> fragments = new ArrayList<>();
        for (File f : inputFiles) {
            log.info("Adding input file {}", f);
            fragments.add(new FileFragment(f));
        }
        cp.setCommands(commands);
        cp.setInput(new TupleND<>(fragments));
        setLogLevelFor(AFragmentCommandTest.class, Level.INFO);
        log.info("Workflow using commands {}", commands);
        log.info("Workflow using inputFiles {}", inputFiles);
        DefaultWorkflow dw = new DefaultWorkflow();
        dw.setFactory(Factory.getInstance());
        dw.setStartupDate(new Date());
        dw.setName("testWorkflow");
        dw.setCommandSequence(cp);
        dw.setExecuteLocal(true);
        dw.setOutputDirectory(outputDirectory);
        return dw;
    }

    /**
     * <p>
     * createWorkflow.</p>
     *
     * @param commands a {@link java.util.List} object.
     * @param inputFiles a {@link java.util.List} object.
     * @return a {@link cross.datastructures.workflow.IWorkflow} object.
     * @throws IOException if creation of temporary folder fails.
     */
    public IWorkflow createWorkflow(List<IFragmentCommand> commands, List<File> inputFiles) throws IOException {
        return createWorkflow(createOutputDir(), commands, inputFiles);
    }

    /**
     * <p>
     * testWorkflow.</p>
     *
     * @param w a {@link cross.datastructures.workflow.IWorkflow} object.
     * @return a {@link cross.datastructures.tuple.TupleND} object.
     */
    public TupleND<IFileFragment> testWorkflow(IWorkflow w) {
        try {
            w.getConfiguration().setProperty("output.overwrite", true);
            TupleND<IFileFragment> t = w.call();
            w.save();
            setLogLevelFor(ICvResolver.class, Level.ALL);
            CvResolver cvResolver = new CvResolver();
            for (IFileFragment ff : t) {
                for (ICommand command : w.getCommandSequence().getCommands()) {
                    IFileFragment testFrag = new FileFragment();
                    testFrag.addSourceFile(ff);
                    for (String variable : AnnotationInspector.getProvidedVariables(command)) {
                        log.info("Evaluating results for variable {}, resolved: {}", variable, cvResolver.translate(variable));
                        try {
                            IVariableFragment var = testFrag.getChild(cvResolver.translate(variable));
                            Array a = var.getArray();
                            Assert.assertNotNull(a);
                        } catch (ResourceNotAvailableException rnae) {
                            log.warn("Could not find variable {}, annotated as provided by fragment command {}. Please fix!", cvResolver.translate(variable), command.getClass().getName());
//                            Assert.fail(rnae.getLocalizedMessage());
                        }
                    }
                }
            }

            return t;
        } catch (Exception e) {
            copyToInspectionDir(w, e);
            log.error("Caught exception while running workflow:", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>
     * copyToInspectionDir.</p>
     *
     * @param w a {@link cross.datastructures.workflow.IWorkflow} object.
     * @param t a {@link java.lang.Throwable} object.
     */
    public void copyToInspectionDir(IWorkflow w, Throwable t) {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File outDir = new File(tmpDir, "maltcms-test-failures");
        String inspectionDirName = getClass().getSimpleName() + "." + name.getMethodName();
        File instanceDir = new File(outDir, inspectionDirName);
        instanceDir.mkdirs();
        try {
            File f = new File(instanceDir, "stacktrace.txt");
            try (PrintWriter bw = new PrintWriter(f)) {
                Throwable cause = t.getCause();
                while (cause != null) {
                    cause.printStackTrace(bw);
                    cause = cause.getCause();
                }
                bw.flush();
            } catch (IOException ioex) {
                log.error("Received io exception while creating stacktrace file!", ioex);
            }
            log.info("Copying workflow output to inspection directory: " + instanceDir.getAbsolutePath());
            FileUtils.copyDirectoryToDirectory(w.getOutputDirectory(), instanceDir);
        } catch (IOException ex) {
            log.error("Failed to copy workflow output to inspection directory!", ex);
        }
    }

    /**
     * Creates a new output directory that will be removed, once the test
     * completes.
     *
     * @return the newly created output directory.
     * @throws IOException if any file- or directory-related error occurs.
     */
    public File createOutputDir() throws IOException {
        return tf.newFolder(getOutputDirName());
    }

    /**
     * Returns the unique output directory name for the extended
     * AFragmentCommandTest class and current test method name.
     *
     * @return the unique output directory name.
     */
    public String getOutputDirName() {
        return getClass().getSimpleName() + "_" + name.getMethodName() + "_" + UUID.randomUUID();
    }
}
