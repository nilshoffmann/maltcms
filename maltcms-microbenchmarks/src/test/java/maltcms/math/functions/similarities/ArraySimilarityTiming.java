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
package maltcms.math.functions.similarities;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.test.IntegrationTest;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments.preprocessing.DenseArrayProducer;
import maltcms.commands.fragments.preprocessing.ScanExtractor;
import maltcms.datastructures.ms.ProfileChromatogram1D;
import maltcms.math.functions.IArraySimilarity;
import maltcms.math.functions.IScalarSimilarity;
import maltcms.math.functions.ProductSimilarity;
import maltcms.test.AFragmentCommandTest;
import maltcms.test.ExtractClassPathFiles;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestRule;
import ucar.ma2.Array;

/**
 * Timing microbenchmarks for different array similarities together with product
 * similarity. The benchmark uses a subset of 501 ms scans from two raw
 * chromatograms with 5 repetitions per instance and 3 warmup rounds before the
 * actual timing is started. It thus runs for quite some time.
 *
 * @author Nils Hoffmann
 */
@BenchmarkMethodChart(filePrefix = "benchmark-lists")
@BenchmarkOptions(callgc = false, benchmarkRounds = 5, warmupRounds = 3)
@Category(IntegrationTest.class)
@Slf4j
public class ArraySimilarityTiming extends AFragmentCommandTest implements IntegrationTest {

    @Rule
    public ExtractClassPathFiles ecpf = new ExtractClassPathFiles(tf,
            "/cdf/1D/glucoseA.cdf.gz", "/cdf/1D/glucoseB.cdf.gz");
    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private List<URI> fileFragments;

    @Before
    public final void setupDenseArrays() throws IOException, Exception {
        sl.setLogLevel("cross", "OFF");
        sl.setLogLevel("maltcms", "OFF");
        sl.setLogLevel("net.sf.maltcms", "OFF");
        if (fileFragments == null) {
            File outputBase = tf.newFolder();
            IFragmentCommand dap = new DenseArrayProducer();
            ScanExtractor se = new ScanExtractor();
            se.setStartScan(1000);
            se.setEndScan(1999);
            List<File> files = ecpf.getFiles();
            IWorkflow w = createWorkflow(outputBase, Arrays.asList(se, dap), files);
            TupleND<IFileFragment> t = w.call();
            fileFragments = new LinkedList<>();
            for (IFileFragment f : t) {
                fileFragments.add(f.getUri());
            }
        }
        sl.setLogLevel("cross", "INFO");
        sl.setLogLevel("maltcms", "INFO");
        sl.setLogLevel("net.sf.maltcms", "INFO");
    }

    @Test
    public void testBhattacharryya() {
        timedTest(new ArrayBhattacharryya());
    }

    @Test
    public void testCorr() {
        timedTest(new ArrayCorr());
    }

    @Test
    public void testCov() {
        timedTest(new ArrayCov());
    }

    @Test
    public void testHamming() {
        timedTest(new ArrayHamming());
    }

    @Test
    public void testLp() {
        timedTest(new ArrayLp());
    }

    @Test
    public void testRankCorr() {
        timedTest(new ArrayRankCorr());
    }

    @Test
    public void testTanimoto() {
        timedTest(new ArrayTanimoto());
    }

    @Test
    public void testDotProduct() {
        timedTest(new ArrayDot());
    }
    @Test
    public void testWeightedCosine() {
        timedTest(new ArrayWeightedCosine());
    }

    @Test
    public void testWeightedCosine2() {
        timedTest(new ArrayWeightedCosine2());
    }

    @Test
    public void testCosine() {
        timedTest(new ArrayCos());
    }
    
    public void timedTest(IArraySimilarity sim) {
        GaussianDifferenceSimilarity gds = new GaussianDifferenceSimilarity();
        gds.setThreshold(0.0);
        gds.setTolerance(0.0);
        ProductSimilarity ps = new ProductSimilarity();
        ps.setScalarSimilarities(new IScalarSimilarity[]{gds});
        ps.setArraySimilarities(new IArraySimilarity[]{sim});
        FileFragment f1 = new FileFragment(fileFragments.get(0));
        FileFragment f2 = new FileFragment(fileFragments.get(1));
        ProfileChromatogram1D pc1 = new ProfileChromatogram1D(f1);
        ProfileChromatogram1D pc2 = new ProfileChromatogram1D(f2);
        List<Array> pc1Intensities = pc1.getBinnedIntensities();
        List<Array> pc2Intensities = pc2.getBinnedIntensities();
        Array rt1 = pc1.getScanAcquisitionTime();
        Array rt2 = pc2.getScanAcquisitionTime();
        DenseDoubleMatrix2D sdm = new DenseDoubleMatrix2D(pc1Intensities.size(), pc2Intensities.size());
        for (int i = 0; i < pc1Intensities.size(); i++) {
            Array iv = pc1Intensities.get(i);
            for (int j = 0; j < pc2Intensities.size(); j++) {
                Array jv = pc2Intensities.get(j);
                sdm.setQuick(i, j, ps.apply(new double[]{rt1.getDouble(i)}, new double[]{rt2.getDouble(j)}, iv, jv));
            }
        }
        log.warn("Similarity stats: " + sim);
    }
}
