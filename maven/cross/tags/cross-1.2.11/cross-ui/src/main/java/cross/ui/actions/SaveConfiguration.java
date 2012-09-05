/* 
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.ui.actions;

import cross.event.EventSource;
import cross.event.IEvent;
import cross.event.IEventSource;
import cross.event.IListener;
import cross.ui.SaveConfigurationDialog;
import cross.ui.events.SaveConfigurationEvent;
import cross.ui.filefilters.PropertiesFileFilter;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.configuration.Configuration;

/**
 * @author Nils Hoffmann
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
