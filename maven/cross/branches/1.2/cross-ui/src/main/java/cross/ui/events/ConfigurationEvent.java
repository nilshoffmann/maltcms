/*
 * $license$
 *
 * $Id$
 */

package cross.ui.events;

import org.apache.commons.configuration.Configuration;

import cross.event.AEvent;
import cross.event.IEventSource;

public class ConfigurationEvent extends AEvent<Configuration> {

	/**
	 * @param v
	 * @param ies
	 */
	public ConfigurationEvent(final Configuration v,
	        final IEventSource<Configuration> ies) {
		super(v, ies);
	}

	@Override
	public String getEventName() {
		return "EVENT_CONFIGURATION";
	}

}
