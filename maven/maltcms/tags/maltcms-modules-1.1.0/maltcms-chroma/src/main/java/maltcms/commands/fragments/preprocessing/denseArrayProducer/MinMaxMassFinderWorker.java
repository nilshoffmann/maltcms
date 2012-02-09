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
package maltcms.commands.fragments.preprocessing.denseArrayProducer;

import cross.Factory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.ImmutableFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import cross.exception.ResourceNotAvailableException;
import java.io.File;
import java.io.Serializable;
import java.util.concurrent.Callable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.tools.MaltcmsTools;
import ucar.ma2.Array;
import ucar.ma2.MAMath;

/**
 *
 * @author nils
 */
@Slf4j
@Data
public class MinMaxMassFinderWorker implements Callable<double[]>, Serializable {

    private File fileToLoad;
    private boolean ignoreMinMaxMassArrays = false;
    private String minMassVariableName = "mass_range_min";
    private String maxMassVariableName = "mass_range_max";
    private String fallbackVariableName = "mass_values";

    @Override
    public double[] call() throws Exception {
        Double min = Double.MAX_VALUE;
        Double max = Double.MIN_VALUE;
//        final boolean ignoreMinMaxMassArrays = Factory.getInstance().getConfiguration().getBoolean(
//                "maltcms.tools.MaltcmsTools.ignoreMinMaxMassArrays",
//                true);
        boolean useFallback = true;
        final IFileFragment f = new ImmutableFileFragment(fileToLoad);
        if (!ignoreMinMaxMassArrays) {
            try {
                log.info(
                        "Trying to load children from file {}", f);
                final IVariableFragment vmin = f.getChild(minMassVariableName);
                final IVariableFragment vmax = f.getChild(maxMassVariableName);
                min = Math.min(MAMath.getMinimum(vmin.getArray()), min);
                max = Math.max(MAMath.getMaximum(vmax.getArray()), max);
                log.info("Min={},Max={}", min, max);
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
            log.info(" From fallback: Min={},Max={}", min, max);
        }
        EvalTools.neqD(min, Double.MAX_VALUE, MinMaxMassFinderWorker.class);
        EvalTools.neqD(max, Double.MIN_VALUE, MinMaxMassFinderWorker.class);
        MaltcmsTools.log.info("Found minimum mass: {} and maximum mass {}",
                min, max);
        Factory.getInstance().getConfiguration().setProperty(
                "maltcms.commands.filters.DenseArrayProducer.min_mass", min);
        Factory.getInstance().getConfiguration().setProperty(
                "maltcms.commands.filters.DenseArrayProducer.max_mass", max);
        return new double[]{min, max};
    }
}
