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
package maltcms.math.functions;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 *
 * @author nilshoffmann
 */
public class ProductPairwiseFunctionTest extends TestCase {

    public ProductPairwiseFunctionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}

    public void testApply() {
//    	String[] resourceNames = new String[]{"glucoseA","mannitolA"};
//    	List<File> files = new ArrayList<File>();
//    	File f = new File("test");
//    	f.mkdirs();
//    	for(String name:resourceNames) {
//    		File file = new File(f,name+".cdf");
//    		ZipResourceExtractor.extract("/cdf/1D/"+name+".cdf", file);
//    	}
//        List<Tuple2D<double[], Array>> i1 = new ArrayList<Tuple2D<double[], Array>>();
//        int nfeatures = 200;
//        int nvectors = 50;
//        double rtStart = 10.0d;
//        double rtIncr = 0.33d;
//        for (int i = 0; i < nvectors; i++) {
//            double[] rt = new double[]{rtStart += (rtIncr)};
//            i1.add(new Tuple2D<double[], Array>(rt, ArrayTools.randomUniform(
//                    nfeatures, 0.0, 10.0)));
//        }
//
//        List<Tuple2D<double[], Array>> i2 = new ArrayList<Tuple2D<double[], Array>>();
//        for (int i = 0; i < nvectors; i++) {
//            double[] rt = i1.get(i).getFirst();
//            for (int j = 0; j < rt.length; j++) {
//                rt[j] = rt[j] + (((Math.random() * rtIncr) - 0.02) / 10.0d);
//            }
//            Array a = i1.get(i).getSecond();
//            Array b = ArrayTools.sum(a, ArrayTools.randomGaussian(nfeatures, 0.0,
//                    1.0));
//            double minb = MAMath.getMinimum(b);
//            AdditionFilter af = new AdditionFilter(minb);
//            b = af.apply(b);
//            i2.add(new Tuple2D<double[], Array>(rt, b));
//        }
//
//        System.out.println(i1);
//        System.out.println(i2);
//
//        ProductSimilarity ppf = new ProductSimilarity();
//        for (IScalarSimilarity ipsf : Lookup.getDefault().lookupAll(
//                IScalarSimilarity.class)) {
//            for (IArraySimilarity ipwf : Lookup.getDefault().lookupAll(
//                    IArraySimilarity.class)) {
//                ppf.setScalarSimilarities(new IScalarSimilarity[]{ipsf});
//                ppf.setArraySimilarities(new IArraySimilarity[]{ipwf});
//                System.out.println("Using pairwise scalar function " + ipsf.
//                        getClass().getName());
//                System.out.println("Using pairwise array function " + ipwf.
//                        getClass().getName());
//                ArrayDouble.D2 arr = new ArrayDouble.D2(i1.size(), i2.size());
//                int i = 0;
//                for (Tuple2D<double[], Array> t1 : i1) {
//                    int j = 0;
//                    for (Tuple2D<double[], Array> t2 : i2) {
//                        arr.set(i, j++, ppf.apply(t1.getFirst(), t2.getFirst(),
//                                t1.getSecond(), t2.getSecond()));
//                    }
//                    i++;
//                }
//                System.out.println(arr);
//                BufferedImage bi = ImageTools.makeImage2D(arr, 1024);
//                new File("output/").mkdirs();
//                try {
//                    ImageIO.write(bi, "png",
//                            new File("output/" + ipsf.getClass().getSimpleName() + "_" + ipwf.
//                            getClass().getSimpleName() + ".png"));
//                } catch (IOException ex) {
//                    Logger.getLogger(ProductPairwiseFunctionTest.class.getName()).
//                            log(Level.SEVERE, null, ex);
//                }
//            }
//        }
//
//
//        GaussianDifferenceSimilarity ipsf = new GaussianDifferenceSimilarity();
//        for (int k = 0; k < 10; k++) {
//            ipsf.setTolerance(1.0f + (float) k);
//            ArrayDouble.D2 arr = new ArrayDouble.D2(i1.size(), i2.size());
//            int i = 0;
//            for (Tuple2D<double[], Array> t1 : i1) {
//                int j = 0;
//                for (Tuple2D<double[], Array> t2 : i2) {
//                    arr.set(i, j++, ipsf.apply(t1.getFirst()[0],
//                            t2.getFirst()[0]));
//                }
//                i++;
//            }
//            System.out.println(arr);
//            BufferedImage bi = ImageTools.makeImage2D(arr, 1024);
//            new File("output/").mkdirs();
//            try {
//                ImageIO.write(bi, "png",
//                        new File(
//                        "output/" + ipsf.getClass().getSimpleName() + "_D" + (float) k + ".png"));
//            } catch (IOException ex) {
//                Logger.getLogger(ProductPairwiseFunctionTest.class.getName()).
//                        log(Level.SEVERE, null, ex);
//            }
//        }
//
//        for (IScalarSimilarity sim : Lookup.getDefault().lookupAll(
//                IScalarSimilarity.class)) {
//            for (IArraySimilarity ipwf : Lookup.getDefault().lookupAll(
//                    IArraySimilarity.class)) {
//                ppf.setScalarSimilarities(new IScalarSimilarity[]{sim});
//                ppf.setArraySimilarities(new IArraySimilarity[]{ipwf});
//                System.out.println("Using pairwise scalar function " + sim.
//                        getClass().getName());
//                System.out.println("Using pairwise array function " + ipwf.
//                        getClass().getName());
//
//                PairwiseDistance pd = new PairwiseDistance();
//                DtwTimePenalizedPairwiseSimilarity dtwSim = new DtwTimePenalizedPairwiseSimilarity();
//                dtwSim.setRetentionTimeScore(ipsf);
//                dtwSim.setDenseMassSpectraScore(ipwf);
//                pd.setCostFunction(dtwSim);
//                DefaultWorkflow dw = new DefaultWorkflow();
//                
//                MZIDynamicTimeWarp mdtw = new MZIDynamicTimeWarp();
//                CumulativeDistance cd = new CumulativeDistance();
//                
//                mdtw.setWorkflow(dw);
//                mdtw.setPairwiseScanDistance(pd);
//                mdtw.setCumulativeDistance(cd);
//
//                mdtw.
//                ArrayDouble.D2 arr = new ArrayDouble.D2(i1.size(), i2.size());
//                int i = 0;
//                for (Tuple2D<double[], Array> t1 : i1) {
//                    int j = 0;
//                    for (Tuple2D<double[], Array> t2 : i2) {
//                        arr.set(i, j++, ppf.apply(t1.getFirst(), t2.getFirst(),
//                                t1.getSecond(), t2.getSecond()));
//                    }
//                    i++;
//                }
//                System.out.println(arr);
//                BufferedImage bi = ImageTools.makeImage2D(arr, 1024);
//                new File("output/").mkdirs();
//                try {
//                    ImageIO.write(bi, "png",
//                            new File("output/" + ipsf.getClass().getSimpleName() + "_" + ipwf.
//                            getClass().getSimpleName() + ".png"));
//                } catch (IOException ex) {
//                    Logger.getLogger(ProductPairwiseFunctionTest.class.getName()).
//                            log(Level.SEVERE, null, ex);
//                }
//            }
//        }
    }
}
