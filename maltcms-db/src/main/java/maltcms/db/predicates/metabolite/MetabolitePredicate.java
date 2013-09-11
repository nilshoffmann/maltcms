/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
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
 * @author Nils Hoffmann
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
