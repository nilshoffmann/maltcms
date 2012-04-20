/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package cross;

import java.io.Serializable;
import org.apache.commons.configuration.Configuration;

/**
 * Interface for objects which are configurable.
 * Should be replaced in favor of Beans compatible objects with getter and setter
 * methods. Configuration will then be performed by a Spring ApplicationContext 
 * instance.
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Deprecated
public interface IConfigurable extends Serializable {

    public void configure(Configuration cfg);
}
