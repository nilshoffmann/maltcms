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
package maltcms.commands.fragments.preprocessing.denseArrayProducer;

import cross.Factory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.ImmutableFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.exception.ResourceNotAvailableException;
import java.io.Serializable;
import java.net.URI;
import java.util.concurrent.Callable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ucar.ma2.Array;
import ucar.ma2.MAMath;

/**
 * <p>MinMaxMassFinderWorker class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@Slf4j
@Data
public class MinMaxMassFinderWorker implements Callable<double[]>, Serializable {

    private URI fileToLoad;
    private boolean ignoreMinMaxMassArrays = false;
    private String minMassVariableName = "mass_range_min";
    private String maxMassVariableName = "mass_range_max";
    private String fallbackVariableName = "mass_values";

    /** {@inheritDoc} */
    @Override
    public double[] call() throws Exception {
        Double min = Double.MAX_VALUE;
        Double max = Double.MIN_VALUE;
        boolean useFallback = true;
        final IFileFragment f = new ImmutableFileFragment(fileToLoad);
        if (!ignoreMinMaxMassArrays) {
            try {
                log.debug(
                        "Trying to load children from file {}", f);
                final IVariableFragment vmin = f.getChild(minMassVariableName);
                final IVariableFragment vmax = f.getChild(maxMassVariableName);
                min = Math.min(MAMath.getMinimum(vmin.getArray()), min);
                max = Math.max(MAMath.getMaximum(vmax.getArray()), max);
                log.debug("Min={},Max={}", min, max);
                useFallback = false;
            } catch (final ResourceNotAvailableException e) {
                log.debug(
                        "Trying to load children from file {} failed", f);
                log.warn(e.getLocalizedMessage());
            }
        }
        if (useFallback) {
            // There are some vendor formats of netcdf, where values in min
            // mass
            // value
            // array are 0, which is not the minimum of measured masses, so
            // check
            // values unless we were successful above
            log.debug("Trying to load fallback {} from {}",
                    fallbackVariableName, f);
            final IVariableFragment mass_vals = f.getChild(fallbackVariableName);
            final Array a = mass_vals.getArray();
            final MAMath.MinMax mm = MAMath.getMinMax(a);
            min = Math.min(min, mm.min);
            max = Math.max(max, mm.max);
            log.debug(" From fallback: Min={},Max={}", min, max);
        }
        EvalTools.neqD(min, Double.MAX_VALUE, MinMaxMassFinderWorker.class);
        EvalTools.neqD(max, Double.MIN_VALUE, MinMaxMassFinderWorker.class);
        log.info("Found minimum mass: {} and maximum mass {} for file {}",
                new Object[]{min, max, f.getName()});
        Factory.getInstance().getConfiguration().setProperty(
                "maltcms.commands.filters.DenseArrayProducer.min_mass", min);
        Factory.getInstance().getConfiguration().setProperty(
                "maltcms.commands.filters.DenseArrayProducer.max_mass", max);
        return new double[]{min, max};
    }
}
