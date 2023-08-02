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
package maltcms.commands.distances.alignment;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import lombok.extern.slf4j.Slf4j;
import maltcms.io.csv.ColorRampReader;
import maltcms.tools.ImageTools;
import ucar.ma2.ArrayDouble;

/**
 * <p>ImagePanel class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@Slf4j
public class ImagePanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 4300139196647706338L;
    private BufferedImage bi = null;

    /**
     * <p>setData.</p>
     *
     * @param mat an array of double.
     * @param masses1 an array of double.
     * @param masses2 an array of double.
     */
    public void setData(final double[][] mat, final double[] masses1,
            final double[] masses2) {
        BufferedImage bi = new BufferedImage(mat[0].length, mat.length,
                BufferedImage.TYPE_INT_RGB);
        log.info("Creating image: " + mat.length + "x"
                + mat[0].length);
        double max = 0;
        double min = 0;
        ArrayDouble.D2 a = new ArrayDouble.D2(mat.length, mat[0].length);
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[0].length; j++) {
                max = Math.max(max, mat[i][j]);
                min = Math.min(min, mat[i][j]);
                a.set(i, j, mat[i][j]);
            }
        }

        ColorRampReader crr = new ColorRampReader();
        int[][] colorRamp = crr.getDefaultRamp();
        int samples = 1024;
        double[] d = ImageTools.getBreakpoints(a, samples,
                Double.NEGATIVE_INFINITY);
        ImageTools.makeImage2D(bi.getRaster(), a.transpose(1, 0), samples,
                colorRamp, 0.0, d);
        this.bi = bi;
    }

    /** {@inheritDoc} */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.bi != null) {
            // log.info("Painting image");
            setPreferredSize(new Dimension(this.bi.getWidth(), this.bi
                    .getHeight()));
            g.drawImage(this.bi, 0, 0, getWidth(), getHeight(), null);
        }
    }
}
