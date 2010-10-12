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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * Implementation of a XMLReader for Stax based XML parsing. TODO Due in
 * Maltcms-2.0
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class StaxXMLReader implements Configurable {

	public static void main(final String[] args) {
		final StaxXMLReader sr = new StaxXMLReader();
		for (final String s : args) {
			sr.parseFile(new File(s));
		}
	}

	private final TreeMap<Integer, List<StaxXMLEventHandler>> handlers = new TreeMap<Integer, List<StaxXMLEventHandler>>();

	public void addHandler(final int event, final StaxXMLEventHandler eh) {
		if (this.handlers.containsKey(event)) {
			final List<StaxXMLEventHandler> l = this.handlers.get(event);
			l.add(eh);
		} else {
			final List<StaxXMLEventHandler> l = new ArrayList<StaxXMLEventHandler>();
			l.add(eh);
			this.handlers.put(event, l);
		}
	}

	public void callHandler(final int event, final XMLStreamReader xlr) {
		callHandlers(this.handlers.get(event), xlr);
		switch (event) {

			case XMLStreamConstants.START_ELEMENT: {
				System.out.println("START_ELEMENT= " + xlr.getLocalName());
				break;
			}
			case XMLStreamConstants.END_ELEMENT: {
				System.out.println("END_ELEMENT= " + xlr.getLocalName());
				break;
			}
			case XMLStreamConstants.CHARACTERS: {
				System.out.println("CHARACTERS= " + xlr.getText());
				break;
			}
		}

	}

	public void callHandlers(final List<StaxXMLEventHandler> l,
	        final XMLStreamReader xmlsr) {
		for (final StaxXMLEventHandler se : l) {
			se.handle(xmlsr);
		}
	}

	@Override
	public void configure(final Configuration arg0)
	        throws ConfigurationException {
		// TODO Auto-generated method stub

	}

	public void parseFile(final File f) {
		FileInputStream fileInputStream;
		try {
			fileInputStream = new FileInputStream(f);
			final XMLStreamReader xmlStreamReader = XMLInputFactory
			        .newInstance().createXMLStreamReader(fileInputStream);
			while (xmlStreamReader.hasNext()) {
				final int event = xmlStreamReader.next();
				callHandler(event, xmlStreamReader);
			}
			xmlStreamReader.close();
		} catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
