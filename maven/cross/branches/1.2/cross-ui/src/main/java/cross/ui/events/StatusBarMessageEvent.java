/*
 * 
 *
 * $Id$
 */

package cross.ui.events;

import cross.event.AEvent;
import cross.event.IEventSource;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class StatusBarMessageEvent extends AEvent<String> {

	/**
	 * @param v
	 * @param ies
	 */
	public StatusBarMessageEvent(final String v, final IEventSource<String> ies) {
		super(v, ies);
	}

	@Override
	public String getEventName() {
		return "EVENT_MESSAGE_STATUSBAR";
	}

}
