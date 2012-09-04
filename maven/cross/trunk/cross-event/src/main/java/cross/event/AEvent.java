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
package cross.event;

/**
 * Abstract implementation of a typed Event.
 *
 * @author Nils Hoffmann
 * @param <V>
 *
 *
 */
public class AEvent<V> implements IEvent<V> {

    private final long ts = System.currentTimeMillis();
    private final V v;
    private final IEventSource<V> ies;
    private String eventName = "EVENT_DEFAULT";

    public AEvent(final V v, final IEventSource<V> ies, final String eventName) {
        this(v, ies);
        this.eventName = eventName;
    }

    public AEvent(final V v, final IEventSource<V> ies) {
        this.v = v;
        this.ies = ies;
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.event.IEvent#get()
     */
    @Override
    public V get() {
        return this.v;
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.event.IEvent#getEventName()
     */
    @Override
    public String getEventName() {
        return this.eventName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.event.IEvent#getSource()
     */
    @Override
    public IEventSource<V> getSource() {
        return this.ies;
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.event.IEvent#getWhen()
     */
    @Override
    public long getWhen() {
        return this.ts;
    }
}
