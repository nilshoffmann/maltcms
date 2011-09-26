/*
 * $license$
 *
 * $Id$
 */

package cross.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import cross.event.EventSource;
import cross.event.IEvent;
import cross.event.IEventSource;
import cross.event.IListener;
import cross.ui.LoadConfigurationDialog;
import cross.ui.events.LoadConfigurationEvent;
import cross.ui.filefilters.PropertiesFileFilter;
import org.slf4j.LoggerFactory;

public class LoadConfiguration extends AbstractAction implements
        IEventSource<Configuration> {

	private final EventSource<Configuration> es = new EventSource<Configuration>();
	/**
	 * 
	 */
	private static final long serialVersionUID = 5395067677304337055L;

	private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

	private final File cwd = null;

	private final FileFilter pf = new PropertiesFileFilter();

	public LoadConfiguration() {
		super();
		// TODO Auto-generated constructor stub
	}

	public LoadConfiguration(final String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public LoadConfiguration(final String name, final Icon icon) {
		super(name, icon);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		final Configuration cfg = LoadConfigurationDialog.getInstance()
		        .loadConfiguration(new PropertiesFileFilter());
		fireEvent(new LoadConfigurationEvent(cfg, this));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.event.IEventSource#addListener(cross.event.IListener<cross.event
	 * .IEvent<V>>[])
	 */
	@Override
	public void addListener(final IListener<IEvent<Configuration>> l) {
		this.es.addListener(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.event.IEventSource#fireEvent(cross.event.IEvent)
	 */
	@Override
	public void fireEvent(final IEvent<Configuration> e) {
		this.es.fireEvent(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.event.IEventSource#removeListener(cross.event.IListener<cross.event
	 * .IEvent<V>>[])
	 */
	@Override
	public void removeListener(final IListener<IEvent<Configuration>> l) {
		this.es.removeListener(l);
	}

}
