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
package maltcms.db.predicates.metabolite;

import java.lang.reflect.Method;

import maltcms.datastructures.ms.IMetabolite;
import maltcms.db.predicates.IMatchPredicate;
import maltcms.db.predicates.MatchPredicate;

import com.db4o.query.Predicate;
import cross.datastructures.tuple.Tuple2D;
import java.util.List;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 *
 *
 */
public abstract class MetabolitePredicate extends Predicate<IMetabolite>
        implements IMatchPredicate<IMetabolite> {

    /**
     *
     */
    private static final long serialVersionUID = 4401086253537298137L;

    @Override
    public Method getMethodOnTargetType() {
        return this.im.getMethodOnTargetType();
    }
    protected IMatchPredicate<IMetabolite> im = new MatchPredicate<IMetabolite>();

    @Override
    public void setMethodOnTargetType(Method m) {
        this.im.setMethodOnTargetType(m);
    }

    @Override
    public void setTargetType(Class<IMetabolite> c) {
        this.im.setTargetType(c);
    }
}
