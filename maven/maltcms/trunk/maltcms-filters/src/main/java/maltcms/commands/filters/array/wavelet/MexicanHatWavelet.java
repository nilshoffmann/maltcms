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
package maltcms.commands.filters.array.wavelet;

public final class MexicanHatWavelet implements IWavelet {

    /**
     * First param in params is expected to be the variance sigma. For
     * performance reasons, no null checking is performed, so please ensure to
     * supply at least one value in params.
     */
    @Override
    public final double applyMotherWavelet(final double t,
            final double... params) {
        final double tsq = Math.pow(t, 2.0d);
        final double val = (1.0d - (tsq / params[0]));
        final double expf = Math.exp((-tsq) / 2.0d * params[0]);
        final double norm = 2.0d / (Math.sqrt(3.0 * Math.sqrt(params[0])) * Math
                .pow(Math.PI, 0.25d));
        return norm * val * expf;
    }
    private final double admissConst = 4.0 * Math.sqrt(Math.PI) / 3.0;

    @Override
    public final double getAdmissabilityConstant() {
        return admissConst;
    }
}
