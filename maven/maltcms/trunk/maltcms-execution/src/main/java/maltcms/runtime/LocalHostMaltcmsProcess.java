/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package maltcms.runtime;

import cross.Factory;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.pipeline.ICommandSequence;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowResult;
import cross.event.EventSource;
import cross.event.IEvent;
import cross.event.IEventSource;
import cross.event.IListener;
import java.util.Date;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nilshoffmann
 */
@Deprecated
public class LocalHostMaltcmsProcess extends SwingWorker<IWorkflow, IWorkflowResult> implements
        IListener<IEvent<IWorkflowResult>>, IEventSource<IWorkflowResult> {

    private Configuration cfg = null;
    private final EventSource<IWorkflowResult> es = new EventSource<IWorkflowResult>();
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public void addListener(final IListener<IEvent<IWorkflowResult>> l) {
        this.es.addListener(l);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public IWorkflow doInBackground() throws Exception {
        log.info("Starting up Maltcms!");
        log.info("Running Maltcms version {}",
                this.cfg.getString("application.version"));
        log.info("Configuring Factory");

        Factory.getInstance().configure(this.cfg);
        // Set up the command sequence
        log.info("Setting up command sequence");
        final ICommandSequence cs = Factory.getInstance().createCommandSequence();
        Date startup = cs.getWorkflow().getStartupDate();
        final AFragmentCommand[] commands = cs.getCommands().toArray(
                new AFragmentCommand[]{});
        final float nsteps = commands.length;
        cs.getWorkflow().addListener(this);
        EvalTools.notNull(cs, cs);
        float step = 0;
        // Evaluate until empty
        log.info("Executing command sequence");
        int progress = 0;
        while (cs.hasNext()) {
            if (isCancelled()) {
                log.warn("Thread was cancelled, bailing out");
                Factory.getInstance().shutdownNow();
                throw new InterruptedException();
            }
            progress = (int) ((step / nsteps) * 100.0f);
            final int progv = progress;
            log.debug("Progress: {}", progress);
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    setProgress(progv);
                }
            };
            SwingUtilities.invokeLater(r);
            cs.next();
            step++;
        }
        progress = (int) ((step / nsteps) * 100.0f);
        final int progv = progress;
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                setProgress(progv);
            }
        };
        SwingUtilities.invokeLater(r);
        log.info("Progress: {}", progress);
        // Save configuration
        Factory.dumpConfig("runtime.properties",
                startup);
        // Save workflow
        final IWorkflow iw = cs.getWorkflow();
        iw.save();
        return iw;
    }

    public void fireEvent(final IEvent<IWorkflowResult> e) {
        this.es.fireEvent(e);
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.event.IListener#listen(cross.event.IEvent)
     */
    /**
     * Relay method, calling all registered Listeners, if an event is received
     * from a Workflow.
     */
    @Override
    public void listen(final IEvent<IWorkflowResult> v) {
        this.publish(v.get());
        this.es.fireEvent(v);
    }

    public void removeListener(final IListener<IEvent<IWorkflowResult>> l) {
        this.es.removeListener(l);
    }

    public void setConfiguration(final Configuration cfg) {
        this.cfg = cfg;
        log.debug("Using configuration");
        log.debug("{}", ConfigurationUtils.toString(cfg));
    }
}
