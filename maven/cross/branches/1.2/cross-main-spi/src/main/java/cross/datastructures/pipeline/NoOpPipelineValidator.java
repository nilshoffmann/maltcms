/**
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
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
 */
/*
 * 
 *
 * $Id$
 */

package cross.datastructures.pipeline;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.exception.ConstraintViolationException;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author nilshoffmann
 */
@Data
@Slf4j
public class NoOpPipelineValidator implements IPipelineValidator {

    private List<IFragmentCommand> fragmentCommands;
    private TupleND<IFileFragment> inputFragments;
    
    @Override
    public void validate() throws ConstraintViolationException {
        
    }
    
}
