package net.sf.maltcms.db.search.spi.similarities;

import java.util.Comparator;
import java.util.TreeSet;
import maltcms.datastructures.ms.IMetabolite;
import cross.datastructures.tuple.Tuple2D;
import java.util.LinkedList;
import java.util.List;
import net.sf.maltcms.db.search.api.similarities.AMetabolitePredicate;

//@ServiceProvider(service = AMetabolitePredicate.class)
public class RetentionIndexMatcher extends AMetabolitePredicate {

    private AMetabolitePredicate delegate = new Cosine();
    private double retentionIndex = Double.NaN;
    private List<Tuple2D<Double, IMetabolite>> metabolites = new LinkedList<Tuple2D<Double, IMetabolite>>();

    public AMetabolitePredicate getDelegate() {
        return delegate;
    }

    public void setDelegate(AMetabolitePredicate delegate) {
        this.delegate = delegate;
        this.delegate.setScoreThreshold(0.0);
    }

    public double getRetentionIndex() {
        return retentionIndex;
    }

    public void setRetentionIndex(double retentionIndex) {
        this.retentionIndex = retentionIndex;
    }

    @Override
    public double getScoreThreshold() {
        return delegate.getScoreThreshold();
    }

    @Override
    public void setScoreThreshold(double scoreThreshold) {
        delegate.setScoreThreshold(scoreThreshold);
    }

    @Override
    public TreeSet<Tuple2D<Double, IMetabolite>> getScoreMap() {
        return delegate.getScoreMap();
    }
//    private List<Tuple2D<Double, IMetabolite>> metToScore = new ArrayList<Tuple2D<Double, IMetabolite>>();
//    private final Comparator<Tuple2D<Double, IMetabolite>> comparator = Collections.
//            reverseOrder(new Comparator<Tuple2D<Double, IMetabolite>>() {
//
//        @Override
//        public int compare(Tuple2D<Double, IMetabolite> t,
//                Tuple2D<Double, IMetabolite> t1) {
//            if (t.getFirst() > t1.getFirst()) {
//                return 1;
//            } else if (t.getFirst() < t1.getFirst()) {
//                return -1;
//            }
//            return 0;
//        }
//    });
    private double window = 10.0;

    public double getWindow() {
        return window;
    }

    public void setWindow(double threshold) {
        this.window = threshold;
    }

    public RetentionIndexMatcher() {
    }

    @Override
    public AMetabolitePredicate copy() {
        RetentionIndexMatcher ms = new RetentionIndexMatcher();
        ms.setWindow(getWindow());
        ms.setMaxHits(getMaxHits());
        ms.setScoreThreshold(getScoreThreshold());
//        ms.setScan(getScan());
        ms.setDelegate(getDelegate());
        ms.setRetentionIndex(getRetentionIndex());
        ms.setMaskedMasses(getMaskedMasses());
        return ms;
    }

    @Override
    public boolean match(IMetabolite et) {

        if (Double.isNaN(retentionIndex) || et.getRetentionIndex() == 0.0) {
//            System.out.println("RI is not a number! Using fallback similarity");
            //fallback
//            return delegate.match(et);
            return false;
        } else {
            //filter
            double delta = Math.abs(retentionIndex - et.getRetentionIndex());
            if (delta <= window) {

//                metabolites.add(new Tuple2D<Double, IMetabolite>(delta, et));
                //FIXME this is a quickfix
                if (delegate.match(et)) {
                    System.out.println("Candidate above threshold and within window: " + delta);
                    return true;
                }
//                //System.out.println("Delegate score "+delegate.getClass().getName()+" match: "+match);
//                return true;
//                return true;
            }
            return false;
        }
    }

    @Override
    public Comparator<Tuple2D<Double, IMetabolite>> getComparator() {
        return delegate.getComparator();
    }
}
