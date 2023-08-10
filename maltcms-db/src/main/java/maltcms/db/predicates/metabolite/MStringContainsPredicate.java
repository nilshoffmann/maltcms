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
import org.slf4j.LoggerFactory;

/**
 * <p>MStringContainsPredicate class.</p>
 *
 * @author Nils Hoffmann
 * 
 */

public class MStringContainsPredicate extends MStringMatchPredicate {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(MStringContainsPredicate.class);

    /**
     *
     */
    private static final long serialVersionUID = -8108747283945214056L;

    /**
     * <p>Constructor for MStringContainsPredicate.</p>
     *
     * @param s a {@link java.lang.String} object.
     */
    public MStringContainsPredicate(String s) {
        super(s);
    }

    /** {@inheritDoc} */
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
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
   
                log.warn(e.getLocalizedMessage());
            }
        } else {
            log.warn("Method not initialized!");
        }
        return false;
    }
}