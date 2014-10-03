/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2013, The authors of Maltcms. All rights reserved.
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
package maltcms.datastructures.quadTree;

import cross.datastructures.tuple.Tuple2D;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.LinkedList;
import java.util.Queue;

/**
 * <p>QuadTreeVisualizer class.</p>
 *
 * @author Nils Hoffmann
 * 
 * @since 1.3.2
 */
public class QuadTreeVisualizer {

    /**
     * <p>createImage.</p>
     *
     * @param qt a {@link maltcms.datastructures.quadTree.QuadTree} object.
     * @param <T> a T object.
     * @return a {@link java.awt.image.RenderedImage} object.
     */
    public <T> RenderedImage createImage(QuadTree<T> qt) {
        return createImage(qt, 1.0d, 1.0d);
    }

    /**
     * <p>createImage.</p>
     *
     * @param qt a {@link maltcms.datastructures.quadTree.QuadTree} object.
     * @param pointSizeX a double.
     * @param pointSizeY a double.
     * @param <T> a T object.
     * @return a {@link java.awt.image.RenderedImage} object.
     */
    public <T> RenderedImage createImage(QuadTree<T> qt, double pointSizeX, double pointSizeY) {
        BufferedImage bi = new BufferedImage((int) Math.ceil(qt.getDataBounds().getWidth()) + 1, (int) Math.ceil(qt.getDataBounds().getHeight()) + 1, BufferedImage.TYPE_INT_ARGB);
        QuadTreeNode<T> root = qt.getRoot();
        Graphics2D g2 = bi.createGraphics();
        g2.setTransform(AffineTransform.getTranslateInstance(-qt.getDataBounds().getMinX(), -qt.getDataBounds().getMinY()));
        Queue<QuadTreeNode<T>> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            QuadTreeNode<T> qtn = queue.poll();
            if (qtn != null) {
                if (qtn.getChildren() != null && qtn.getChildren().isEmpty()) {
                    g2.setColor(Color.BLACK);
                    g2.draw(qtn.getArea());
                    if (qtn.getImmediateChildren() != null) {
                        g2.setColor(new Color(0, 0, 255, 128));
                        for (Tuple2D<Point2D, T> t : qtn.getImmediateChildren()) {
                            g2.draw(new Rectangle2D.Double(t.getFirst().getX() - (pointSizeX / 2.0d), t.getFirst().getY() - (pointSizeY / 2.0d), pointSizeX, pointSizeY));
                        }
                    }
                } else if (qtn.getChildren() != null) {
                    g2.setColor(Color.BLACK);
                    g2.draw(qtn.getArea());
                    queue.addAll(qtn.getChildren());
                } else {
                    if (qtn.getImmediateChildren() != null) {
                        g2.setColor(Color.BLACK);
                        g2.draw(qtn.getArea());
                        g2.setColor(new Color(0, 0, 255, 128));
                        for (Tuple2D<Point2D, T> t : qtn.getImmediateChildren()) {
                            g2.draw(new Rectangle2D.Double(t.getFirst().getX() - (pointSizeX / 2.0d), t.getFirst().getY() - (pointSizeY / 2.0d), pointSizeX, pointSizeY));
                        }
                    }
                }
            }
        }
        g2.dispose();
        return bi;
    }

}
