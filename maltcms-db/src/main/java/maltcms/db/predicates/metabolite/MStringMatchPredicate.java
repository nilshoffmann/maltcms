/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
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
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            System.err.println("Method not initialized!");
        }
        return false;
    }
}
