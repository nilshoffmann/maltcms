/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
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
 * 
 * $Id$
 */

package cross.io.xml;

import javax.xml.stream.XMLStreamReader;

import org.apache.avalon.framework.configuration.Configurable;

/**
 * Reading of XML documents using the Streaming API for XML. TODO Due in
 * Maltcms-2.0
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public abstract class StaxXMLEventHandler implements Configurable {

	private int event = -1;

	public StaxXMLEventHandler(final int event1) {
		this.event = event1;
	}

	public boolean checkType(final XMLStreamReader xmlsr) {
		if (xmlsr.getEventType() == this.event) {
			return true;
		}
		return false;
	}

	public abstract void handle(XMLStreamReader xmlsr);

	public void handleEvent(final XMLStreamReader xmlsr) {
		if (checkType(xmlsr)) {
			handle(xmlsr);
		}
	}

}
