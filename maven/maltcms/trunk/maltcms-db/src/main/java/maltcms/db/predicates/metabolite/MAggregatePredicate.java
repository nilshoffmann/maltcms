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

import java.util.ArrayList;
import java.util.Collection;

import maltcms.datastructures.ms.IMetabolite;

/**
 * Implementation of a combined Predicate for IMetabolite instances.
 * An arbitrary number of Numerical or String Predicates can be used 
 * in order to work as a Predicate when querying a db container 
 * for contained Metabolites. Metabolites will only be returned,
 * if all MetabolitePredicates apply to this Metabolite (return true for evalOn).
 * @author hoffmann
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
