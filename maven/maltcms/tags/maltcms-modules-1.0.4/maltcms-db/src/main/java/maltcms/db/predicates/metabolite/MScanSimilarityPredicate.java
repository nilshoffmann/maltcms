package maltcms.db.predicates.metabolite;

import maltcms.datastructures.ms.IMetabolite;
import maltcms.db.similarities.MetaboliteSimilarity;
import maltcms.db.similarities.Similarity;
import cross.datastructures.tuple.Tuple2D;

public class MScanSimilarityPredicate extends MetabolitePredicate{

	/**
     * 
     */
    private static final long serialVersionUID = -3687114440251319097L;

	private Tuple2D<ucar.ma2.Array, ucar.ma2.Array> scan;
	
	private Similarity<IMetabolite> sim = new MetaboliteSimilarity();
	
//	public ConcurrentHashMap<IMetabolite,Double> res = new ConcurrentHashMap<IMetabolite,Double>();
	
	public double matchThreshold = 0.9d;
	
	public MScanSimilarityPredicate(Tuple2D<ucar.ma2.Array, ucar.ma2.Array> arg0) {
		this.scan = arg0;
	}
	
//	public void resetResultList() {
//		this.res.clear();
//	}
	
	@Override
	public boolean match(
			IMetabolite im) {
		final double sim = this.sim.get(this.scan, im);
		//System.out.println("Sim: "+sim+" threshold: "+this.matchThreshold);
		
		if(sim>=this.matchThreshold) {
			//System.out.println("Sim: "+sim+" >= threshold: "+this.matchThreshold);
			//this.res.put(im,sim);
			return true;
		}
		return false;
	}
	
	//@Override
	//public boolean appliesTo(IMetabolite im) {
	//	return match(im);
	//}
	
	public void setScan(Tuple2D<ucar.ma2.Array, ucar.ma2.Array> arg0) {
//		this.res.clear();
		this.scan = arg0;
	}

//	public Double getSimilarityTo(IMetabolite im) {
//		if(this.res.containsKey(im)) {
//			return this.res.get(im);
//		}
//		return Double.NEGATIVE_INFINITY;
//	}
	
//    public List<Tuple2D<Double, IMetabolite>> getSimilaritiesAboveThreshold() {
//	    List<Tuple2D<Double, IMetabolite>> sims = new ArrayList<Tuple2D<Double,IMetabolite>>(this.res.size());
//	    for(IMetabolite im:this.res.keySet()) {
//	    	Double d = this.res.get(im);
//	    	sims.add(new Tuple2D<Double,IMetabolite>(d,im));
//	    }
//	    Collections.sort(sims, new Comparator<Tuple2D<Double, IMetabolite>>() {
//			
//			@Override
//			public int compare(Tuple2D<Double, IMetabolite> o1,
//			        Tuple2D<Double, IMetabolite> o2) {
//				return o1.getFirst().compareTo(o2.getFirst());
//			}
//		});
//	    return sims;
//    }


	public void setThreshold(double d) {
    	if(d>1) {
    		this.matchThreshold=1;
    	}else if(d<0) {
    		this.matchThreshold=0;
    	}else{
    		this.matchThreshold=d;
    	}
    	System.out.println("Match threshold set to "+this.matchThreshold);
    }

}
