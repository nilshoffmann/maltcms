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
package maltcms.commands.fragments.alignment.peakCliqueAlignment.peakFactory;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import java.io.Serializable;

/**
 * <p>IPeakFactory interface.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public interface IPeakFactory extends Serializable {

    /**
     * <p>createInstance.</p>
     *
     * @param sourceFile a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param minMaxMassRange a {@link cross.datastructures.tuple.Tuple2D} object.
     * @param size a int.
     * @param massBinResolution a double.
     * @param useSparseArrays a boolean.
     * @param associationId a int.
     * @return a {@link maltcms.commands.fragments.alignment.peakCliqueAlignment.peakFactory.IPeakFactoryImpl} object.
     */
    IPeakFactoryImpl createInstance(IFileFragment sourceFile, Tuple2D<Double, Double> minMaxMassRange, int size, double massBinResolution, boolean useSparseArrays, int associationId);
}
