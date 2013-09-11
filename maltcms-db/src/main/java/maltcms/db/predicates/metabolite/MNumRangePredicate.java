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
