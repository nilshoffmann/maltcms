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
package maltcms.commands.filters.array;

import cross.ICopyable;
import cross.annotations.Configurable;
import cross.commands.ICommand;
import lombok.Data;
import maltcms.commands.filters.AElementFilter;
import org.apache.commons.configuration.Configuration;
import ucar.ma2.Array;

/**
 * AArrayFilter applicable to Array objects, returning Array objects.
 *
 * @author Nils Hoffmann
 * 
 */
@Data
public abstract class AArrayFilter implements ICommand<Array, Array>, ICopyable<AArrayFilter> {

    protected transient AElementFilter ef = null;
    @Configurable
    private boolean copyArray = true;

    /*
     * (non-Javadoc)
     *
     * @see maltcms.ucar.ma2.Filter#filter(java.lang.Object)
     */
    /** {@inheritDoc} */
    @Override
    public Array apply(final Array a) {
        if (this.copyArray) {
            return a.copy();
        }
        return a;
    }

    /**
     * <p>apply.</p>
     *
     * @param a an array of {@link ucar.ma2.Array} objects.
     * @return an array of {@link ucar.ma2.Array} objects.
     */
    public Array[] apply(final Array[] a) {
        Array[] ret = new Array[a.length];
        for (int i = 0; i < a.length; i++) {
            ret[i] = apply(a[i]);
        }
        return ret;
    }

    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration cfg) {
        this.copyArray = cfg.getBoolean(this.getClass().getName()
                + ".copyArray", true);
    }

}
