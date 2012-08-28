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
import java.lang.reflect.Method;

import maltcms.datastructures.ms.IMetabolite;

public class MNumRangePredicate extends MetabolitePredicate {

    /**
     *
     */
    private static final long serialVersionUID = 4384503672151011994L;
    private Number lB, uB;

    public MNumRangePredicate(Number lowerBound, Number upperBound, Method value) {
        this.lB = lowerBound;
        this.uB = upperBound;
        setMethodOnTargetType(value);
    }

    @Override
    public boolean match(IMetabolite met) {
        try {
            Object val = getMethodOnTargetType().invoke(met, (Object[]) null);
            if (val instanceof Number) {
                Number n = (Number) val;
                if (val instanceof Float) {
                    return ((lB.floatValue() <= n.floatValue()) && (n.floatValue() <= uB.
                            floatValue()));
                }
                if (val instanceof Double) {
                    return ((lB.doubleValue() <= n.doubleValue()) && (n.
                            doubleValue() <= uB.doubleValue()));
                }
                if (val instanceof Integer) {
                    return ((lB.intValue() <= n.intValue()) && (n.intValue() <= uB.
                            intValue()));
                }
                if (val instanceof Long) {
                    return ((lB.longValue() <= n.longValue()) && (n.longValue() <= uB.
                            longValue()));
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
        return false;
    }
}
