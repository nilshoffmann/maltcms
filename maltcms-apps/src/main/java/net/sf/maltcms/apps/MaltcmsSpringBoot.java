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
package net.sf.maltcms.apps;

import cross.Factory;
import cross.IFactory;
import cross.datastructures.pipeline.ICommandSequence;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.workflow.IWorkflow;
import cross.exception.ConstraintViolationException;
import cross.exception.ExitVmException;
import static net.sf.maltcms.apps.Maltcms.addVmStats;
import net.sf.maltcms.apps.util.ThreadTimer;
import org.apache.commons.configuration.CompositeConfiguration;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 *
 * @author nilshoffmann
 */
@SpringBootApplication
public class MaltcmsSpringBoot implements CommandLineRunner {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(MaltcmsSpringBoot.class);
    
    public static void main(String... args) {
        SpringApplication.run(MaltcmsSpringBoot.class, args);
    }
    
//    @Bean
//    IFactory factory(CompositeConfiguration cfg) {
//        Factory.getInstance().setConfiguration(cfg);
//        return Factory.getInstance();
//    }
    
    @Override
    public void run(String... args) throws Exception {
        Maltcms m = Maltcms.getInstance();
        ThreadTimer tt = new ThreadTimer(5000);
        tt.start();

        //final Logger log = cross.Logging.getLogger(Maltcms.class);
        final int ecode = 0;
        ICommandSequence cs = null;
        try {
            final CompositeConfiguration cfg = m.parseCommandLine(args);
            log.info("Running Maltcms version {}",
                    cfg.getString("application.version"));
            EvalTools.notNull(cfg, cfg);
            log.info("Configuring Factory");
            log.info("Using pipeline definition at {}", cfg.getString("pipeline.xml"));
            Factory.getInstance().configure(cfg);
            // Set up the command sequence
            cs = Factory.getInstance().createCommandSequence();
            final IWorkflow iw = cs.getWorkflow();
            if (cs.validate()) {
                // Evaluate until empty
                try {
                    iw.call();
                    Maltcms.shutdown(30, log);
                    // Save workflow
                    iw.save();
                    addVmStats(tt, iw.getOutputDirectory());
                    System.exit(ecode);
                } catch (final ExitVmException e) {
                    Maltcms.handleExitVmException(log, e);
                } catch (final IllegalArgumentException iae) {
                    Maltcms.handleExitVmException(log, new ExitVmException(iae));
                } catch (final Throwable t) {
                    Maltcms.handleRuntimeException(log, t, cs);
                }
            } else {
                throw new ConstraintViolationException(
                        "Pipeline is invalid, but strict checking was requested!");
            }
        } catch (final ExitVmException e) {
            Maltcms.handleExitVmException(log, e);
        } catch (final IllegalArgumentException iae) {
            Maltcms.handleExitVmException(log, new ExitVmException(iae));
        } catch (final Throwable t) {
            Maltcms.handleRuntimeException(log, t, cs);
        }
    }
    
}
