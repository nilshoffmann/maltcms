/*
 * $license$
 *
 * $Id$
 */

package cross.ui.events;

import org.apache.commons.configuration.Configuration;

import cross.event.IEventSource;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class SaveConfigurationEvent extends ConfigurationEvent {

	/**
	 * @param v
	 * @param ies
	 */
	public SaveConfigurationEvent(final Configuration v,
	        final IEventSource<Configuration> ies) {
		super(v, ies);
	}

	@Override
	public String getEventName() {
		return "EVENT_CONFIGURATION_SAVE";
	}

}
