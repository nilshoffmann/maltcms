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
package maltcms.datastructures.peak.normalization;

import cross.cache.ICacheDelegate;
import cross.cache.ICacheElementProvider;
import cross.cache.softReference.SoftReferenceCacheManager;
import cross.datastructures.fragments.IFileFragment;
import lombok.Data;
import lombok.ToString;
import maltcms.tools.ArrayTools;

/**
 * <p>TicAreaNormalizer class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@Data
@ToString(exclude = "cache")
public class TicAreaNormalizer implements IPeakNormalizer {

    private String ticVariableName = "total_intensity";
    private transient ICacheDelegate<IFileFragment, Double> cache = null;

    private ICacheDelegate<IFileFragment, Double> getCache() {
        if (this.cache == null) {
            this.cache = SoftReferenceCacheManager.getInstance().getAutoRetrievalCache("TicAreaNormalizerCache", new ICacheElementProvider<IFileFragment, Double>() {
                @Override
                public Double provide(IFileFragment key) {
                    return ArrayTools.integrate(key.getChild(ticVariableName).getArray());
                }
            });
        }
        return this.cache;
    }

    /** {@inheritDoc} */
    @Override
    public double getNormalizationFactor(IFileFragment fragment, int peakIndex) {
        return 1.0d / getCache().get(fragment);
    }

    /** {@inheritDoc} */
    @Override
    public String getNormalizationName() {
        return "normalization to sum of " + ticVariableName;
    }

    /** {@inheritDoc} */
    @Override
    public TicAreaNormalizer copy() {
        TicAreaNormalizer tan = new TicAreaNormalizer();
        tan.setTicVariableName(ticVariableName);
        return tan;
    }
}
