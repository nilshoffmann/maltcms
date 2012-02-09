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

public class MStringMatchPredicate extends MetabolitePredicate {

    /**
     * 
     */
    private static final long serialVersionUID = 7126522681487993030L;
    protected String match;
    private boolean matchCaseInsensitive = true;

    public MStringMatchPredicate(String s) {
        this.match = s;
    }

    public void setCaseInsensitiveMatching(boolean b) {
        this.matchCaseInsensitive = b;
    }

    public boolean isCaseInsensitiveMatching() {
        return this.matchCaseInsensitive;
    }

    @Override
    public boolean match(IMetabolite m) {
        if (getMethodOnTargetType() != null) {
            try {
                Object o = getMethodOnTargetType().invoke(m, (Object[]) null);
                if (o instanceof String) {
                    if (matchCaseInsensitive) {
                        return this.match.equalsIgnoreCase((String) o);
                    }
                    return this.match.equals((String) o);
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
