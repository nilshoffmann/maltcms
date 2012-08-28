package net.sf.maltcms.db.search.spi.similarities;

import maltcms.datastructures.ms.IMetabolite;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import cross.datastructures.tuple.Tuple2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.sf.maltcms.db.search.api.similarities.AMetabolitePredicate;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.ArrayInt;

@ServiceProvider(service = AMetabolitePredicate.class)
public class SteinAndScott extends AMetabolitePredicate {

    boolean toggle = true;
    private AMDISMSSimilarity iadc = new AMDISMSSimilarity();
    private double resolution = 1.0d;
    private double lastMin = Double.POSITIVE_INFINITY,
            lastMax = Double.NEGATIVE_INFINITY;
    private boolean normalize = true;
    private final Comparator<Tuple2D<Double, IMetabolite>> comparator = Collections.reverseOrder(new Comparator<Tuple2D<Double, IMetabolite>>() {
        @Override
        public int compare(Tuple2D<Double, IMetabolite> t,
                Tuple2D<Double, IMetabolite> t1) {
            if (t.getFirst() > t1.getFirst()) {
                return 1;
            } else if (t.getFirst() < t1.getFirst()) {
                return -1;
            }
            return 0;
        }
    });

    public boolean isNormalize() {
        return normalize;
    }

    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }

    public double getResolution() {
        return resolution;
    }

    public void setResolution(double resolution) {
        this.resolution = resolution;
    }

    public SteinAndScott() {
    }

    @Override
    public AMetabolitePredicate copy() {
        SteinAndScott ms = new SteinAndScott();
        ms.setResolution(resolution);
//        ms.setThreshold(threshold);
        ms.setMaxHits(getMaxHits());
        ms.setNormalize(normalize);
        ms.setScoreThreshold(getScoreThreshold());
//        ms.setScan(getScan());
        ms.setMaskedMasses(getMaskedMasses());
        return ms;
    }

    protected double similarity(Array massesRef, Array intensitiesRef,
            Array massesQuery, Array intensitiesQuery, double mw) {
        return iadc.apply(new Tuple2D<Array, Array>(massesRef, intensitiesRef), new Tuple2D<Array, Array>(massesQuery, intensitiesQuery));
    }

    @Override
    public boolean match(IMetabolite et) {
        Tuple2D<ArrayDouble.D1, ArrayInt.D1> etMs = et.getMassSpectrum();
        Array m1 = getScan().getMasses();
        Array s1 = getScan().getIntensities();

        Array m2 = etMs.getFirst();
        Array s2 = etMs.getSecond();
        s1 = filterMaskedMasses(m1, s1);
        s2 = filterMaskedMasses(m2, s2);
        double sim = similarity(m1, s1, m2, s2,
                et.getMW());
        System.out.println("Similarity score: " + sim);
        if (sim >= getScoreThreshold()) {
            Tuple2D<Double, IMetabolite> tple = new Tuple2D<Double, IMetabolite>(
                    sim, et);
            getScoreMap().add(tple);
            return true;
        }
        return false;
    }

    @Override
    public Comparator<Tuple2D<Double, IMetabolite>> getComparator() {
        return this.comparator;
    }
}
