package maltcms.db.similarities;

import maltcms.commands.distances.ArrayCos;
import maltcms.commands.distances.IArrayDoubleComp;
import maltcms.datastructures.ms.IMetabolite;
import maltcms.datastructures.feature.DefaultFeatureVector;
import maltcms.experimental.operations.WeightedCosine;
import maltcms.tools.ArrayTools;
import maltcms.tools.MaltcmsTools;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.IndexIterator;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import cross.datastructures.tuple.Tuple2D;

/**
 * Class is deprecated use @see maltcms.db.predicates.metabolite.MetaboliteSimilarity
 * for a better replacement.
 * @author nilshoffmann
 * @deprecated
 */
@Deprecated
public class MetaboliteSimilarity extends Similarity<IMetabolite> {

    boolean toggle = true;
    private IArrayDoubleComp iadc = new ArrayCos();
    // private HashMap<IMetabolite,ArrayInt.D1> hm = new
    // HashMap<IMetabolite,ArrayInt.D1>();
    private double resolution = 1.0d;
    private int minimumCommonMasses = 0;
    private Tuple2D<Array, Array> lastT = null;
    private double lastMin = Double.POSITIVE_INFINITY,
            lastMax = Double.NEGATIVE_INFINITY;
    private int lastBins = 0;
    private ArrayDouble.D1 lastTMasses = null;
    private ArrayDouble.D1 lastTIntens = null;
    private int mw = -1;

    @Override
    public double get(Tuple2D<Array, Array> query, IMetabolite t1) {
        // System.out.print("Similarity to "+t1.getName());
        Tuple2D<Array, Array> tpl1 = new Tuple2D<Array, Array>(t1.getMassSpectrum().getFirst(), t1.getMassSpectrum().getSecond());
        Tuple2D<Array, Array> tpl2 = query;
        mw = t1.getMW();
        return similarity(tpl1, tpl2);
    }

    protected double similarity(Tuple2D<Array, Array> t1,
            Tuple2D<Array, Array> t2) {
        MinMax mm1 = MAMath.getMinMax(t1.getFirst());
        MinMax mm2 = MAMath.getMinMax(t2.getFirst());
        // Union, greatest possible interval
        double max = Math.max(mm1.max, mm2.max);
        double min = Math.min(mm1.min, mm2.min);
        int bins = MaltcmsTools.getNumberOfIntegerMassBins(min, max, resolution);

        ArrayDouble.D1 s1 = null, s2 = null;
        ArrayDouble.D1 dmasses1 = new ArrayDouble.D1(bins);
        s1 = new ArrayDouble.D1(bins);
        ArrayTools.createDenseArray(t1.getFirst(), t1.getSecond(),
                new Tuple2D<Array, Array>(dmasses1, s1), ((int) Math.floor(min)), ((int) Math.ceil(max)), bins,
                resolution, 0.0d);
//		}
        //normalization to 0..1
        double maxS1 = MAMath.getMaximum(s1);
        s1 = (ArrayDouble.D1) ArrayTools.mult(s1, 1.0d / maxS1);

        ArrayDouble.D1 dmasses2 = new ArrayDouble.D1(bins);
        s2 = new ArrayDouble.D1(bins);
        ArrayTools.createDenseArray(t2.getFirst(), t2.getSecond(),
                new Tuple2D<Array, Array>(dmasses2, s2),
                ((int) Math.floor(min)), ((int) Math.ceil(max)), bins,
                resolution, 0.0d);
        double maxS2 = MAMath.getMaximum(s2);
        //normalization
        s2 = (ArrayDouble.D1) ArrayTools.mult(s2, 1.0d / maxS2);

        double d = this.iadc.apply(-1, -1, 0.0d, 0.0d, s1, s2);
        return d;
    }

    @Override
    public double get(IMetabolite t1, IMetabolite t2) {
        return get(new Tuple2D<Array, Array>(t2.getMassSpectrum().getFirst(),
                t2.getMassSpectrum().getSecond()), t1);
    }
}
