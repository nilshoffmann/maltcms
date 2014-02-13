/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.pipeline.CommandPipeline;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflow;
import cross.datastructures.workflow.IWorkflow;
import cross.test.LogMethodName;
import cross.test.SetupLogging;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Slf4j
public abstract class AFragmentCommandTest {

    @Rule
    public TemporaryFolder tf = new TemporaryFolder();
    @Rule
    public SetupLogging sl = new SetupLogging();
    @Rule
    public LogMethodName lmn = new LogMethodName();

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

    public void setLogLevelFor(Class<?> cls, Level logLevel) {
        setLogLevelFor(cls.getName(), logLevel);
    }

    public void setLogLevelFor(Package pkg, Level logLevel) {
        setLogLevelFor(pkg.getName(), logLevel);
    }

    public IWorkflow createWorkflow(File outputDirectory, List<IFragmentCommand> commands, List<File> inputFiles) {
        CommandPipeline cp = new CommandPipeline();
        List<IFileFragment> fragments = new ArrayList<IFileFragment>();
        for (File f : inputFiles) {
            log.info("Adding input file {}", f);
            fragments.add(new FileFragment(f));
        }
        cp.setCommands(commands);
        cp.setInput(new TupleND<IFileFragment>(fragments));
        setLogLevelFor(AFragmentCommandTest.class, Level.INFO);
        log.info("Workflow using commands {}", commands);
        log.info("Workflow using inputFiles {}", inputFiles);
        DefaultWorkflow dw = new DefaultWorkflow();
        dw.setStartupDate(new Date());
        dw.setName("testWorkflow");
        dw.setCommandSequence(cp);
        dw.setExecuteLocal(true);
        dw.setOutputDirectory(outputDirectory);
        return dw;
    }

    public TupleND<IFileFragment> testWorkflow(IWorkflow w) {
        try {
            w.getConfiguration().setProperty("output.overwrite", true);
            TupleND<IFileFragment> t = w.call();
            w.save();
            return t;
        } catch (Exception e) {
            copyToInspectionDir(w, e);
            log.error("Caught exception while running workflow:", e);
            throw new RuntimeException(e);
        }
    }

    public void copyToInspectionDir(IWorkflow w, Throwable t) {
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
                while (cause != null) {
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
            System.out.println("Copying workflow output to inspection directory: " + instanceDir.getAbsolutePath());
            FileUtils.copyDirectoryToDirectory(w.getOutputDirectory(), instanceDir);
        } catch (IOException ex) {
            log.error("Failed to copy workflow output to inspection directory!", ex);
        }
    }
}
