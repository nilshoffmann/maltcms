/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package maltcms.mpaxs;

import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowResult;
import cross.event.EventSource;
import cross.event.IEvent;
import cross.event.IEventSource;
import cross.event.IListener;
import javax.swing.SwingWorker;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author nilshoffmann
 */
public class MaltcmsProcess extends SwingWorker<IWorkflow, IWorkflowResult> implements
            IListener<IEvent<IWorkflowResult>>, IEventSource<IWorkflowResult> {

        private Configuration cfg = null;
        private final EventSource<IWorkflowResult> es = new EventSource<IWorkflowResult>();

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
//            LocalHostLauncher.this.log.info("Starting up Maltcms!");
//            LocalHostLauncher.this.log.info("Running Maltcms version {}",
//                    this.cfg.getString("application.version"));
//            LocalHostLauncher.this.log.info("Configuring Factory");
//
//            Factory.getInstance().configure(this.cfg);
//            // Set up the command sequence
//            LocalHostLauncher.this.log.info("Setting up command sequence");
//            final ICommandSequence cs = Factory.getInstance().createCommandSequence();
//            LocalHostLauncher.this.startup = cs.getWorkflow().getStartupDate();
//            final AFragmentCommand[] commands = cs.getCommands().toArray(
//                    new AFragmentCommand[]{});
//            final float nsteps = commands.length;
//            cs.getWorkflow().addListener(this);
//            EvalTools.notNull(cs, cs);
//            float step = 0;
//            // Evaluate until empty
//            LocalHostLauncher.this.log.info("Executing command sequence");
//            int progress = 0;
//            while (cs.hasNext()) {
//                if (isCancelled()) {
//                    LocalHostLauncher.this.log.warn("Thread was cancelled, bailing out");
//                    Factory.getInstance().shutdownNow();
//                    throw new InterruptedException();
//                }
//                progress = (int) ((step / nsteps) * 100.0f);
//                final int progv = progress;
//                LocalHostLauncher.this.log.debug("Progress: {}", progress);
//                final Runnable r = new Runnable() {
//
//                    @Override
//                    public void run() {
//                        setProgress(progv);
//                    }
//                };
//                SwingUtilities.invokeLater(r);
//                cs.next();
//                step++;
//            }
//            progress = (int) ((step / nsteps) * 100.0f);
//            final int progv = progress;
//            final Runnable r = new Runnable() {
//
//                @Override
//                public void run() {
//                    setProgress(progv);
//                }
//            };
//            SwingUtilities.invokeLater(r);
//            LocalHostLauncher.this.log.info("Progress: {}", progress);
//            LocalHostLauncher.shutdown(30, LocalHostLauncher.this.log);
//            // Save configuration
//            Factory.dumpConfig("runtime.properties",
//                    LocalHostLauncher.this.startup);
//            // Save workflow
//            final IWorkflow iw = cs.getWorkflow();
//            iw.save();
//            return iw;
            return null;
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
         * Relay method, calling all registered Listeners, if an event is
         * received from a Workflow.
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
//            LocalHostLauncher.this.log.debug("Using configuration");
//            LocalHostLauncher.this.log.debug("{}", ConfigurationUtils.toString(cfg));
        }
    }
