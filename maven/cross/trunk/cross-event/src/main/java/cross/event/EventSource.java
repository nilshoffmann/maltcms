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

import java.util.LinkedHashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * EventSource of Type V, where V can be any Object produced by the EventSource.
 *
 * @author Nils Hoffmann
 *
 * @param <V>
 */
public class EventSource<V> implements IEventSource<V> {

    private final LinkedHashSet<IListener<IEvent<V>>> listenerMap;
    private ExecutorService es = Executors.newCachedThreadPool();

    public EventSource(int nThreads) {
        this();
        this.es = Executors.newFixedThreadPool(nThreads);
    }

    public EventSource() {
        this.listenerMap = new LinkedHashSet<IListener<IEvent<V>>>();
    }

    @Override
    public void addListener(final IListener<IEvent<V>> l) {
        if (this.listenerMap.contains(l)) {
            // System.out.println("IListener already known, ignoring!");
        } else {
            // System.out.println("Adding listener!");
            this.listenerMap.add(l);
        }
    }

    @Override
    public void fireEvent(final IEvent<V> e) {
        for (final IListener<IEvent<V>> lst : this.listenerMap) {
            es.submit(new Runnable() {
                @Override
                public void run() {
                    lst.listen(e);
                }
            });
        }
    }

    @Override
    public void removeListener(final IListener<IEvent<V>> l) {
        if (this.listenerMap.contains(l)) {
            this.listenerMap.remove(l);
        }
    }
}
