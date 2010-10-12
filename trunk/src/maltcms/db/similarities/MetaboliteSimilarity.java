package maltcms.db.similarities;

import maltcms.commands.distances.ArrayCos;
import maltcms.commands.distances.IArrayDoubleComp;
import maltcms.datastructures.ms.IMetabolite;
import maltcms.tools.ArrayTools;
import maltcms.tools.MaltcmsTools;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.IndexIterator;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import cross.datastructures.tuple.Tuple2D;

public class MetaboliteSimilarity extends Similarity<IMetabolite> {
	
	boolean toggle = true;
	
	private IArrayDoubleComp iadc = new ArrayCos();
	//private HashMap<IMetabolite,ArrayInt.D1> hm = new HashMap<IMetabolite,ArrayInt.D1>();
	private double resolution = 1.0d;
	private int minimumCommonMasses = 0;
	
	private Tuple2D<Array,Array> lastT = null;
	private double lastMin = Double.POSITIVE_INFINITY, lastMax = Double.NEGATIVE_INFINITY;
	private int lastBins = 0;
	private ArrayDouble.D1 lastTMasses = null;
	private ArrayInt.D1 lastTIntens = null;
	
	
	@Override
	public double get(Tuple2D<Array,Array> query, IMetabolite t1) {
		//System.out.print("Similarity to "+t1.getName());
		Tuple2D<Array,Array> tpl1 = new Tuple2D<Array,Array>(t1.getMassSpectrum().getFirst(),t1.getMassSpectrum().getSecond());
		Tuple2D<Array,Array> tpl2 = query;
		return similarity(tpl1, tpl2);
	}
	
	protected double similarity(Tuple2D<Array,Array> t1, Tuple2D<Array,Array> t2) {
		MinMax mm1 = MAMath.getMinMax(t1.getFirst());
		MinMax mm2 = MAMath.getMinMax(t2.getFirst());
		//Union, greatest possible interval
		double max = Math.max(mm1.max,mm2.max);
		double min = Math.min(mm1.min,mm2.min);
		int bins = MaltcmsTools.getNumberOfIntegerMassBins(min, max, resolution);
		//BitSet bs1 = new BitSet(bins);
		//BitSet bs2 = new BitSet(bins);
//		double maxdelta = 0.01;
//		
//		Array masses1 = t1.getFirst();
////		Array int1 = t1.getSecond();
////		Index int1i = int1.getIndex();
//		IndexIterator ii1= masses1.getIndexIterator();
//		while(ii1.hasNext()) {
//			//bs1.set(MaltcmsTools.binMZ(ii1.getDoubleNext(),min,max,resolution));
//		}
//		Array masses2 = t2.getFirst();
////		Array int2 = t2.getSecond();
////		Index int2i = int2.getIndex();
//		IndexIterator ii2 = masses2.getIndexIterator();
//		while(ii2.hasNext()) {
//			//bs2.set(MaltcmsTools.binMZ(ii2.getDoubleNext(),min,max,resolution));
//		}
		//bs1.and(bs2);
		//int commonMasses = bs1.cardinality();
		//System.out.println("Found "+commonMasses);
		//if(commonMasses<minimumCommonMasses) {
		//	return Double.NEGATIVE_INFINITY;
		//}
//		int ii1cnt = 0;
//		int ii2cnt = 0;
//		double score = 0;
//		double scorel = 0;
//		double scorer = 0;
//		double lhs = 0;
//		double rhs = 0;
//		double delta = 0;
//		int advanceWhich = -1; //-1 none, 0 both, 1 lhs, 2 rhs
//		while(ii1.hasNext() && ii2.hasNext()) {
//			if(ii1cnt == 0  && ii2cnt == 0) {
//				lhs = ii1.getDoubleNext();
//				rhs = ii2.getDoubleNext();
//				ii1cnt++;
//				ii2cnt++;
//			}else{
//				if(advanceWhich>=0){
//					switch(advanceWhich) {
//						case 0:
//							lhs = ii1.getDoubleCurrent();
//							rhs = ii2.getDoubleCurrent();
//							ii1cnt++;
//							ii2cnt++;
//							break;
//						case 1:
//							lhs = ii1.getDoubleCurrent();
//							ii1cnt++;
//							break;
//						case 2:
//							rhs = ii2.getDoubleCurrent();
//							ii2cnt++;
//							break;
//					}
//				}
//				//first check if lhs and rhs are within bounds of maxdelta
//				//maybe abs could be removed
//				delta = Math.abs(lhs-rhs);
//				if(delta>maxdelta) {//check, which one is larger and advance smaller one
//					score+=(scorel*scorer);
//					scorel = 0;
//					scorer = 0;
//					if(lhs>rhs) {
//						advanceWhich = 2;
//					}else if(lhs<rhs){
//						advanceWhich = 1;
//					}else{
//						throw new IllegalStateException("Cannot advance both sides if delta is too large!");
//						//advanceWhich = 0;
//					}
//				}else{//delta<=maxdelta
//					scorel+=(int1.getDouble(int1i.set(ii1cnt)));
//					scorer+=(int2.getDouble(int2i.set(ii2cnt)));
//					if(lhs>rhs) {
//						advanceWhich = 2;
//					}else if(lhs<rhs){
//						advanceWhich = 1;
//					}else{
//						advanceWhich = 0;
//					}
//				}
//			}
//		}
		
		ArrayInt.D1 s1 = null, s2 = null;
////		if(hm.containsKey(t1)) {
////			s1 = hm.get(t1);
////		}else{
		ArrayDouble.D1 dmasses1 = null;
		if(this.lastT!=null) {
			if(lastBins == bins && lastMin == min  && lastMax == max) {
				dmasses1 = this.lastTMasses;
				s1 = this.lastTIntens;
			}
		}else{
			dmasses1 = new ArrayDouble.D1(bins);
				s1 = new ArrayInt.D1(bins);
				ArrayTools.createDenseArray(t1.getFirst(), t1.getSecond(),
					    new Tuple2D<Array, Array>(dmasses1, s1), ((int) Math
						    .floor(min)), ((int) Math.ceil(max)),
					    bins, resolution, 0.0d);
			this.lastTMasses = dmasses1;
			this.lastTIntens = s1;
			this.lastMax = max;
			this.lastMin = min;
			this.lastBins = bins;
		}
			
//			//s1 = new ArrayInt.D1(tpl1.getFirst(),tpl1.getSecond(),(int)min,(int)max);
////			hm.put(t1,s1);
////		}
////		if(hm.containsKey(t2)) {
////			s2 = hm.get(t2);
////		}else{
			ArrayDouble.D1 dmasses2 = new ArrayDouble.D1(bins);
			s2 = new ArrayInt.D1(bins);
			ArrayTools.createDenseArray(t2.getFirst(), t2.getSecond(),
				    new Tuple2D<Array, Array>(dmasses2, s2), ((int) Math
					    .floor(min)), ((int) Math.ceil(max)),
				    bins, resolution, 0.0d);
////			hm.put(t2, s2);
////		}
			Array a = ArrayTools.sum(s1, s2);
			IndexIterator ii = a.getIndexIterator();
			int matches = 0;
			while(ii.hasNext()) {
				if(ii.getDoubleNext()>0) {
					matches++;
				}
			}
			if(matches<minimumCommonMasses) {
				//System.out.println(": -INF");
				return Double.NEGATIVE_INFINITY;
			}
			//System.out.print("Number of shared peaks: "+matches);
//		//if(toggle){
//		//	System.out.println(tpl1.getSecond());
//		//	System.out.println(tpl2.getSecond());
//		//}
//		
//		
////		Array indx1 = Array.factory(tpl1.getFirst().getElementType(), new int[] { bins });
////		Array vals1 = Array.factory(tpl1.getSecond().getElementType(), new int[] { bins });
////
////		ArrayTools.createDenseArray((Array)tpl1.getFirst(), (Array)tpl1.getSecond(), new Tuple2D<Array,Array>(indx1, vals1),
////				((int) Math.rint(min)), ((int) Math.rint(max)),bins,0.0d);
////		
////		Array indx2 = Array.factory(tpl2.getFirst().getElementType(), new int[] { bins });
////		Array vals2 = Array.factory(tpl2.getSecond().getElementType(), new int[] { bins });
////		ArrayTools.createDenseArray((Array)tpl2.getFirst(), (Array)tpl2.getSecond(), new Tuple2D<Array,Array>(indx2, vals2),
////				((int) Math.rint(min)), ((int) Math.rint(max)),bins,
////				0.0d);
////		EvalTools.notNull(vals1,vals2);
////		//if(toggle){
////		//	System.out.println(vals1);
////		//	System.out.println(vals2);
////		//}
//		//ArrayCos ac = new ArrayCos();
			double d = this.iadc.apply(-1,-1,0.0d,0.0d,s1,s2);
//		//toggle = false;
			//System.out.println(": "+d+" with "+matches+" shared peaks");
			return d;
		//return score;
	}
	
	@Override
	public double get(IMetabolite t1, IMetabolite t2) {
		return get(new Tuple2D<Array,Array>(t2.getMassSpectrum().getFirst(),t2.getMassSpectrum().getSecond()), t1);
	}

}
