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
package maltcms.db.predicates;

import java.lang.reflect.Method;

public class MatchPredicate<T> implements IMatchPredicate<T> {

    protected Method m;
    protected Class<T> c;

    /* (non-Javadoc)
     * @see maltcms.db.predicates.IMatchPredicate#setMethodOnTargetType(java.lang.reflect.Method)
     */
    public void setMethodOnTargetType(Method m) {
        this.m = m;
    }

    /* (non-Javadoc)
     * @see maltcms.db.predicates.IMatchPredicate#setTargetType(java.lang.Class)
     */
    public void setTargetType(Class<T> c) {
        this.c = c;
    }

    @Override
    public Method getMethodOnTargetType() {
        return this.m;
    }
}
