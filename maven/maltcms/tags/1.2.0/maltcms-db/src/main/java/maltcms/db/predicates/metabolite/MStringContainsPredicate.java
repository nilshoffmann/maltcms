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

import java.lang.reflect.InvocationTargetException;

import maltcms.datastructures.ms.IMetabolite;

public class MStringContainsPredicate extends MStringMatchPredicate {

    /**
     * 
     */
    private static final long serialVersionUID = -8108747283945214056L;

    public MStringContainsPredicate(String s) {
        super(s);
    }

    @Override
    public boolean match(IMetabolite q) {
        if (getMethodOnTargetType() != null) {
            try {
                Object o = getMethodOnTargetType().invoke(q, (Object[]) null);
                if (o instanceof String) {
                    if (isCaseInsensitiveMatching()) {
                        return ((String) o).toLowerCase().contains(this.match.
                                toLowerCase());
                    } else {
                        return ((String) o).contains(this.match);
                    }
                }
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            System.err.println("Method not initialized!");
        }
        return false;
    }
}
