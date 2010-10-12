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

/*
 * Logging.java
 * 
 * Created on 22. Januar 2007, 10:51
 * 
 * To change this template, choose Tools | Template Manager and open the
 * template in the editor.
 */

package cross;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides logging via Simple Logging Facade for Java (slf4j).
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class Logging {

	protected static Logger getClassLogger(final Class<?> c) {
		return LoggerFactory.getLogger(c.getName());
	}

	public static Logger getLogger(final Class<?> c) {
		Logging.getRootLogger().debug("Adding logger for {}", c.getName());
		if (Logging.logRegistry.containsKey(c.getName())) {
			return Logging.logRegistry.get(c.getName());
		}
		final Logger l = Logging.getClassLogger(c);
		Logging.logRegistry.put(c.getName(), l);
		return l;
	}

	public static Logger getLogger(final Object o1) {
		return Logging.getLogger(o1.getClass());
	}

	protected static Logger getPackageLogger(final Class<?> c) {
		return LoggerFactory.getLogger(c.getPackage().getName());
	}

	public static Logger getRootLogger() {
		return Logging.getInstance().logger;
	}

	public Logger logger = null;

	private static Logging o;

	private static HashMap<String, Logger> logRegistry = new HashMap<String, Logger>();

	public static Logging getInstance() {
		if (Logging.o == null) {
			Logging.o = new Logging();
		}
		return Logging.o;
	}

	private Logging() {
		this.logger = LoggerFactory.getLogger("Root");
	}

}
