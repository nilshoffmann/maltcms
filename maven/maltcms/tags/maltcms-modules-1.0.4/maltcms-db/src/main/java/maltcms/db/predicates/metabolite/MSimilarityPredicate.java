package maltcms.db.predicates.metabolite;

import java.util.ArrayList;
import java.util.List;

import maltcms.datastructures.ms.IMetabolite;
import maltcms.db.similarities.MetaboliteSimilarity;
import maltcms.db.similarities.Similarity;
import cross.datastructures.tuple.Tuple2D;

public class MSimilarityPredicate extends MetabolitePredicate {

	/**
     * 
     */
    private static final long serialVersionUID = -3684834963267981958L;

	private double matchThreshold = 0.99;
	
	private Similarity<IMetabolite> s = new MetaboliteSimilarity();
	
	private List<Tuple2D<Double,IMetabolite>> res = new ArrayList<Tuple2D<Double,IMetabolite>>();
	
	private IMetabolite compareTo = null;
	
	public MSimilarityPredicate(IMetabolite compareTo, double threshold) {
		this.compareTo = compareTo;
		setThreshold(threshold);
	}

	public List<Tuple2D<Double,IMetabolite>> getSimilaritiesAboveThreshold(){
		return this.res;
	}
	
	public void resetResultList() {
		this.res.clear();
	}
	
	public void setThreshold(double d) {
		if(d>1) {
			this.matchThreshold=1;
		}else if(d<0) {
			this.matchThreshold=0;
		}else{
			this.matchThreshold=d;
		}
	}
	
    @Override
	public boolean match(IMetabolite arg0) {
		double sim = s.get(this.compareTo,arg0);
		//System.out.print(this.compareTo.getID()+" and "+arg0.getID()+" similarity = "+sim+" ");
		if(sim>=matchThreshold) {
			res.add(new Tuple2D<Double,IMetabolite>(sim,arg0));
			//System.out.print(" >= "+threshold+"\n");
			return true;
		}
		//System.out.print(" < "+threshold+"\n");
		return false;
	}

}
