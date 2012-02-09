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
package net.sf.maltcms.evaluation.api.tasks;


import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public interface ITask<T extends Serializable> extends Callable<T>, Serializable{
    public List<IPostProcessor> getPostProcessors();
    public HashMap<String,String> getAdditionalEnvironment();
    public List<String> getCommandLine();
    public File getWorkingDirectory();
    public File getOutputDirectory();
    public UUID getTaskId();
}
