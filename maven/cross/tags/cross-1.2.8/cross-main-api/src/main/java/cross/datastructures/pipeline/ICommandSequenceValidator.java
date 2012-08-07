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
package cross.datastructures.pipeline;

import cross.exception.ConstraintViolationException;
import java.io.Serializable;

/**
 * Interface definition for command sequence validation.
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public interface ICommandSequenceValidator extends Serializable {
    /**
     * Determines, whether a given commandSequence is valid or not.
     * Returns true for a valid commandSequence and false for an invalid one.
     * May throw a @see ConstraintViolationException if validation fails due to
     * unmet constraints, e.g. for unavailable variables @see cross.annotations.RequiresVariables.
     * @param commandSequence
     * @throws ConstraintViolationException
     * @return 
     */
    boolean isValid(ICommandSequence commandSequence) throws ConstraintViolationException;
}
