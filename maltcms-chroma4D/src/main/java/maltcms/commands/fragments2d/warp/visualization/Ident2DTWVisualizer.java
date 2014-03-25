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
package maltcms.commands.fragments2d.warp.visualization;

import cross.datastructures.tuple.Tuple2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;

/**
 * Basically the same as {@link Default2DTWVisualizer} but dont uses the warp
 * path.
 *
 * @author Mathias Wilhelm
 */
@Slf4j
@Data
public class Ident2DTWVisualizer extends Default2DTWVisualizer {

    private int currentrasterline = -1;

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedImage createImage(final List<Array> scanlinesi,
            final List<Array> scanlinesj, final Array warpPathi,
            final Array warpPathj) {

        final int imageheight = (int) (scanlinesi.get(0).getSize());

        final List<Tuple2D<Array, Array>> outputintensities = new ArrayList<>();
        final List<Tuple2D<Integer, Integer>> outputintensitiescounter = new ArrayList<>();

        final Tuple2D<double[], Tuple2D<double[], double[]>> sb = getSampleAndBreakpointTable(
                scanlinesi, scanlinesj);

        final ArrayDouble.D1 emptyScanline = new ArrayDouble.D1(imageheight);
        maltcms.tools.ArrayTools.fill(emptyScanline, 0);
        for (int i = 0; i < Math.max(scanlinesi.size(), scanlinesj.size()); i++) {
            final Array scanlinei;
            final Array scanlinej;
            if (i < scanlinesi.size()) {
                scanlinei = scanlinesi.get(i);
            } else {
                scanlinei = emptyScanline;
            }
            if (i < scanlinesj.size()) {
                scanlinej = scanlinesj.get(i);
            } else {
                scanlinej = emptyScanline;
            }
            outputintensities.add(new Tuple2D<>(scanlinei,
                    scanlinej));
            outputintensitiescounter.add(new Tuple2D<>(1, 1));
            this.currentrasterline++;
        }

        return ci(outputintensities, outputintensitiescounter, imageheight, sb.
                getFirst(), sb.getSecond().getFirst(),
                sb.getSecond().getSecond());
    }
}
