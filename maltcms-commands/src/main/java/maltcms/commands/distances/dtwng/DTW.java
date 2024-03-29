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
package maltcms.commands.distances.dtwng;

import cross.Factory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tools.FragmentTools;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import maltcms.datastructures.array.ArrayFactory;
import maltcms.datastructures.array.IArrayD2Double;
import maltcms.datastructures.array.IFeatureVector;
import maltcms.tools.ArrayTools;
import org.apache.commons.configuration.Configuration;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayDouble;

/**
 * <p>DTW class.</p>
 *
 * @author Nils Hoffmann
 * 
 */

public class DTW implements IAlignment, Serializable {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DTW.class);

    /**
     *
     */
    private static final long serialVersionUID = -4958589994859792653L;
    private String leftHandSideId, rightHandSideId;
    private TwoFeatureVectorOperation similarity = new FeatureVectorDtwSimilarity();
    private List<Point> alignmentMap = Collections.emptyList();
    private Area area = null;
    private double defaultValue = Double.NEGATIVE_INFINITY;
    private IOptimizationFunction optimizationFunction = new ThreePredecessorsOptimization();

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.experimental.operation.PairwiseFeatureVectorSequenceOperation
     * #apply(java.util.List, java.util.List)
     */
    /** {@inheritDoc} */
    @Override
    public Double apply(List<IFeatureVector> l1, List<IFeatureVector> l2) {
        final ArrayFactory f = Factory.getInstance().getObjectFactory()
                .instantiate(ArrayFactory.class);
        final IArrayD2Double alignment = f.create(l1.size(), l2.size(),
                this.defaultValue, this.area);
        final IArrayD2Double pwvalues = f.createSharedLayout(alignment);
        // saveImage(this.lhsID + "-" + this.rhsID + "_layout", alignment,
        // new ArrayList<Point>());
        optimizationFunction.init(l1, l2, alignment, pwvalues, this.similarity);
        double percentDone = 0;
        long elemCnt = 0;
        long elements = alignment.getNumberOfStoredElements();
        long partCnt = 0;
        int[] point = new int[]{0, 0};
        for (int i = 0; i < alignment.rows(); i++) {
            final int[] bounds = alignment.getColumnBounds(i);
            for (int j = bounds[0]; j < bounds[0] + bounds[1]; j++) {
                percentDone = ArrayTools.calcPercentDone(elements, elemCnt);
                partCnt = ArrayTools.printPercentDone(percentDone, 10, partCnt,
                        System.out);
                elemCnt++;
                point[0] = i;
                point[1] = j;
                optimizationFunction.apply(point);
            }
        }
        // ((ThreePredecessorsOptimization) iof).showCumScoreMatrix();
        // ((ThreePredecessorsOptimization) iof).showPwScoreMatrix();
        percentDone = ArrayTools.calcPercentDone(elements, elemCnt);
        ArrayTools.printPercentDone(percentDone, 10, partCnt, System.out);
        this.alignmentMap = optimizationFunction.getTrace();
        log.info("Number of Points in trace: "
                + this.alignmentMap.size());

        // minimum of four points for each polynomial
        // UnivariateRealInterpolator interpolator = new SplineInterpolator();
        // try {
        //
        List<double[]> l = new ArrayList<>();
        // create list of all aligned points
        for (int i = 0; i < this.alignmentMap.size() - 1; i++) {
            Point p = this.alignmentMap.get(i);
            l.add(new double[]{p.x, p.y, pwvalues.get(p.x, p.y)});
        }

        ContinuityArgmaxNodeBuilder canb10 = new ContinuityArgmaxNodeBuilder(10);
        List<Point> interp10 = canb10.eval(l, 3);

        ContinuityArgmaxNodeBuilder canb20 = new ContinuityArgmaxNodeBuilder(20);
        List<Point> interp20 = canb20.eval(l, 3);

        ContinuityArgmaxNodeBuilder canb100 = new ContinuityArgmaxNodeBuilder(
                100);
        List<Point> interp100 = canb100.eval(l, 3);
        // remove start and end point -> fixed anchors
        // double[] start = l.remove(0);
        // double[] end = l.remove(l.size() - 1);
        // List<double[]> anchors = new ArrayList<double[]>();
        //
        // Point q = this.alignmentMap.get(this.alignmentMap.size() - 1);
        // if (q.getX() == p.getX() || q.getY() == p.getY()) {
        // l.remove(l.size() - 1);
        // }
        // l.add(q);
        // log.info("Number of Surviving Points: " + l.size());
        // double[] x = new double[l.size()];
        // double[] y = new double[l.size()];
        // for (int i = 0; i < l.size(); i++) {
        // x[i] = l.get(i).getX();
        // y[i] = l.get(i).getY();
        // }
        // UnivariateRealFunction function = interpolator.interpolate(x, y);
        // List<Point> interp = new ArrayList<Point>();
        // for (int i = 0; i < alignment.rows(); i += 10) {
        // Point ip = new Point(i, (int) (Math.round(function
        // .value((double) i))));
        // interp.add(ip);
        // }
        // if (interp.get(interp.size() - 1).x != alignment.columns() - 1) {
        // Point ip = new Point(alignment.columns() - 1,
        // (int) (Math.round(function.value((double) alignment
        // .columns() - 1))));
        // interp.add(ip);
        // }
        BufferedImage bi = createImage(alignment);
        addAlignmentMap(bi, this.alignmentMap, Color.WHITE);
        // addAlignmentMap(bi, l, Color.LIGHT_GRAY);
        addAlignmentMap(bi, interp10, Color.RED);
        addAlignmentMap(bi, interp20, Color.BLUE);
        addAlignmentMap(bi, interp100, Color.GREEN);
        saveImage(this.leftHandSideId + "-" + this.rightHandSideId
                + "_interpolatedLayoutWithTrace", bi);
        // } catch (MathException e) {
        //
        // log.warn(e.getLocalizedMessage());
        // }
        BufferedImage rp = createRecurrencePlot(pwvalues, 0.99);
        saveImage("recurrencePlot-" + this.leftHandSideId + "-"
                + this.rightHandSideId, rp);
        saveImage(this.leftHandSideId + "-" + this.rightHandSideId
                + "_layoutWithTrace", alignment, this.alignmentMap);
        return optimizationFunction.getOptimalValue();
    }

    private void saveImage(String name, IArrayD2Double alignment,
            List<Point> l, Color mapColor) {
        BufferedImage bi = createImage(alignment);
        addAlignmentMap(bi, l, mapColor);
        saveImage(name, bi);
    }

    private void saveImage(String name, BufferedImage bi) {
        try {
            ImageIO.write(bi, "PNG", new File(Factory.getInstance()
                    .getConfiguration().getString("output.basedir"), name
                    + ".png"));
        } catch (IOException ex) {
            Logger.getLogger(DTW.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private BufferedImage createImage(IArrayD2Double alignment) {
        final ArrayFactory f = Factory.getInstance().getObjectFactory()
                .instantiate(ArrayFactory.class);
        BufferedImage bi = f.createLayoutImage(alignment);
        return bi;
    }

    private BufferedImage createRecurrencePlot(IArrayD2Double pwd,
            double threshold) {
        BufferedImage bi = createImage(pwd);
        Graphics2D g2 = bi.createGraphics();
        Color hit = Color.BLACK;
        Color miss = Color.WHITE;
        for (int x = 0; x < bi.getWidth(); x++) {
            for (int y = 0; y < bi.getHeight(); y++) {
                if (pwd.get(y, x) > threshold) {
                    g2.setColor(hit);
                    g2.fillRect(x, y, 1, 1);
                } else {
                    g2.setColor(miss);
                    g2.fillRect(x, y, 1, 1);
                }
            }
        }
        return bi;
    }

    private void addAlignmentMap(BufferedImage bi, List<Point> l, Color mapColor) {
        Color c = mapColor;
        Graphics2D g2 = (Graphics2D) bi.getGraphics();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                0.3f));
        g2.setColor(c);
        Point last = null;
        for (Point p : l) {
            if (last == null) {
                last = p;
            } else {
                g2.fillRect(last.y, last.x, p.y - last.y, p.x - last.x);
                last = p;
            }
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                0.8f));
        GeneralPath gp = new GeneralPath();
        gp.moveTo(0, 0);
        for (Point p : l) {
            gp.lineTo(p.getY(), p.getX());
        }
        g2.setColor(c);
        g2.draw(gp);
    }

    private void saveImage(String name, IArrayD2Double alignment, List<Point> l) {
        saveImage(name, alignment, l, Color.WHITE);
    }

    /** {@inheritDoc} */
    @Override
    public List<Point> getMap() {
        return this.alignmentMap;
    }

    /** {@inheritDoc} */
    @Override
    public String getLeftHandSideId() {
        return this.leftHandSideId;
    }

    /** {@inheritDoc} */
    @Override
    public String getRightHandSideId() {
        return this.rightHandSideId;
    }

    /** {@inheritDoc} */
    @Override
    public void setLeftHandSideId(String lhsid) {
        this.leftHandSideId = lhsid;
    }

    /** {@inheritDoc} */
    @Override
    public void setRightHandSideId(String rhsid) {
        this.rightHandSideId = rhsid;
    }

    /** {@inheritDoc} */
    @Override
    public void setPairwiseFeatureVectorOperation(TwoFeatureVectorOperation tfvo) {
        this.similarity = tfvo;
    }

    /** {@inheritDoc} */
    @Override
    public void setConstraints(Area a) {
        this.area = a;
    }

    /** {@inheritDoc} */
    @Override
    public void setDefaultValue(double d) {
        this.defaultValue = d;
    }

    /** {@inheritDoc} */
    @Override
    public void setOptimizationFunction(IOptimizationFunction iof) {
        this.optimizationFunction = iof;
    }

    /** {@inheritDoc} */
    @Override
    public IOptimizationFunction getOptimizationFunction() {
        return this.optimizationFunction;
    }

    /** {@inheritDoc} */
    @Override
    public TwoFeatureVectorOperation getPairwiseFeatureVectorOperation() {
        return this.similarity;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.experimental.datastructures.IFileFragmentModifier#decorate(cross
     * .datastructures.fragments.IFileFragment)
     */
    /** {@inheritDoc} */
    @Override
    public void modify(IFileFragment iff) {
        String arrayComparatorVariableName = Factory
                .getInstance()
                .getConfiguration()
                .getString("var.alignment.pairwise_distance.class",
                        "pairwise_distance_class");
        String arrayDistanceClassName = Factory
                .getInstance()
                .getConfiguration()
                .getString("var.alignment.cumulative_distance.class",
                        "cumulative_distance_class");
        String alignmentClassVariableName = Factory.getInstance()
                .getConfiguration()
                .getString("var.alignment.class", "alignment_class");
        FragmentTools.createString(iff, arrayComparatorVariableName,
                this.similarity.getClass().getName());
        FragmentTools.createString(iff, arrayDistanceClassName,
                this.optimizationFunction.getClass().getName());
        FragmentTools.createString(iff, alignmentClassVariableName, this
                .getClass().getName());
        ArrayDouble.D0 result = new ArrayDouble.D0();
        result.set(getOptimizationFunction().getOptimalValue());
        final String distvar = Factory.getInstance().getConfiguration()
                .getString("var.alignment.distance", "distance");
        final IVariableFragment dvar = new VariableFragment(iff, distvar);
        dvar.setArray(result);
        this.optimizationFunction.modify(iff);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.IConfigurable#configure(org.apache.commons.configuration.Configuration
     * )
     */
    /** {@inheritDoc} */
    @Override
    public void configure(Configuration cfg) {
    }

    /** {@inheritDoc} */
    @Override
    public Area getConstraints() {
        return area;
    }

    /** {@inheritDoc} */
    @Override
    public double getDefaultValue() {
        return defaultValue;
    }
}
