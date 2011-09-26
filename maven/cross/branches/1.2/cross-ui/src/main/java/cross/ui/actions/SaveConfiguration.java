/**
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 *
 * This file is part of Cross/Maltcms.
 *
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * 
 *
 * $Id$
 */

package cross.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.configuration.Configuration;

import cross.event.EventSource;
import cross.event.IEvent;
import cross.event.IEventSource;
import cross.event.IListener;
import cross.ui.SaveConfigurationDialog;
import cross.ui.events.SaveConfigurationEvent;
import cross.ui.filefilters.PropertiesFileFilter;

/**
 * @author hoffmann
 * 
 */
public class SaveConfiguration extends AbstractAction implements
        IEventSource<Configuration> {

	/**
     * 
     */
	private static final long serialVersionUID = -7614438404648702213L;

	private final EventSource<Configuration> es = new EventSource<Configuration>();

	private final FileFilter pf = new PropertiesFileFilter();

	public SaveConfiguration() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SaveConfiguration(final String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public SaveConfiguration(final String name, final Icon icon) {
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
		final Configuration cfg = (e.getSource() instanceof Configuration) ? (Configuration) e
		        .getSource()
		        : null;
		if (cfg != null) {
			SaveConfigurationDialog.getInstance().saveConfiguration(cfg,
			        new PropertiesFileFilter());
			fireEvent(new SaveConfigurationEvent(cfg, this));
		}
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
