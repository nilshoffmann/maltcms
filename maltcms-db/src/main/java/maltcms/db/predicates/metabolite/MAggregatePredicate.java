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

import java.util.ArrayList;
import java.util.Collection;

import maltcms.datastructures.ms.IMetabolite;

/**
 * Implementation of a combined Predicate for IMetabolite instances. An
 * arbitrary number of Numerical or String Predicates can be used in order to
 * work as a Predicate when querying a db container for contained Metabolites.
 * Metabolites will only be returned, if all MetabolitePredicates apply to this
 * Metabolite (return true for evalOn).
 *
 * @author Nils Hoffmann
 *
 */
public class MAggregatePredicate extends MetabolitePredicate {

    /**
     *
     */
    private static final long serialVersionUID = -2688853916226964219L;
    private Collection<MetabolitePredicate> mpl;

    /**
     * @param mpl
     */
    public MAggregatePredicate(Collection<MetabolitePredicate> mpl) {
        this.mpl = mpl;
    }

    public MAggregatePredicate() {
        this.mpl = new ArrayList<MetabolitePredicate>();
        this.mpl.add(new MetabolitePredicate() {
            /**
             *
             */
            private static final long serialVersionUID = 2855317514847457027L;

            @Override
            public boolean match(IMetabolite arg0) {
                return true;
            }
        });
    }

    /* (non-Javadoc)
     * @see com.db4o.query.Predicate#match(java.lang.Object)
     */
    @Override
    public boolean match(IMetabolite arg0) {
        //System.out.println("Match called!");
        if (this.mpl == null) {
            System.out.println("No Predicates defined!");
            return false;
        }
        for (MetabolitePredicate mp : mpl) {
            //System.out.println("Processing Predicate "+mp.getClass().getName());
            if (!mp.match(arg0)) {
                return false;
            }
        }
        return true;
    }
}
