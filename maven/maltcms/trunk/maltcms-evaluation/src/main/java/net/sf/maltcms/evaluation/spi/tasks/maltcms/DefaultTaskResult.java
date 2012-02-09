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
package net.sf.maltcms.evaluation.spi.tasks.maltcms;

import net.sf.maltcms.evaluation.api.tasks.ITaskResult;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nilshoffmann
 * Date: 10.12.11
 * Time: 11:00
 * To change this template use File | Settings | File Templates.
 */
public class DefaultTaskResult implements ITaskResult {
    private final List<File> taskInputs;
    private final List<File> taskOutputs;
    public DefaultTaskResult() {
        taskInputs = new LinkedList<File>();
        taskOutputs = new LinkedList<File>();
    }
    
    public static final ITaskResult EMPTY = new EmptyTaskResult();

    @Override
    public List<File> getTaskInputs() {
        return taskInputs;
    }

    @Override
    public List<File> getTaskOutputs() {
        return taskOutputs;
    }

    private static class EmptyTaskResult implements ITaskResult {

        @Override
        public List<File> getTaskInputs() {
            return Collections.emptyList();
        }

        @Override
        public List<File> getTaskOutputs() {
            return Collections.emptyList();
        }
    }
}
