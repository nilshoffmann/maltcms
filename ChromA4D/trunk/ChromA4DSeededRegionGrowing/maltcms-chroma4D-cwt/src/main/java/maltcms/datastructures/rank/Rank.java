package maltcms.datastructures.rank;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import maltcms.datastructures.ridge.Ridge;

public class Rank<T extends Ridge> implements Comparable<Rank<? extends T>>{

	private final HashMap<String,Double> featureToRank = new LinkedHashMap<String,Double>();
	
	private final T t;
	
	public Rank(T t) {
		this.t = t;
		//addRank("cwtResponse",t.getRidgePoints().get(0).getSecond());
	}
	
	public T getRidge() {
		return this.t;
	}
	
	public void addRank(String feature, double rank) {
		featureToRank.put(feature, rank);
	}
	
	public Double getRank(String feature) {
		return featureToRank.get(feature);
	}
	
	public Set<String> getFeatures() {
		return featureToRank.keySet();
	}
	
	public Set<String> getCommonFeatures(Rank<? extends T> r) {
		HashSet<String> hs = new HashSet<String>(featureToRank.keySet());
		hs.retainAll(r.getFeatures());
		return hs;
	}
	
	public double[] getFeatureRanks(Rank<? extends T> r) { 
		Set<String> features = getCommonFeatures(r);
		double[] vals = new double[2];
		for(String s:features) {
			vals[0] += getRank(s);
			vals[1] += r.getRank(s);
		}
		return vals;
	}
	
	public int compareRanks(String feature, Rank<? extends T> r) {
		double[] vals = new double[2];
		Set<String> features = getCommonFeatures(r);
		if(features.isEmpty()) {
			return -1;
		}
		vals[0] = getRank(feature);
		vals[1] = r.getRank(feature);
		return Double.compare(vals[0], vals[1]);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(String key:getFeatures()) {
			sb.append("["+key+":"+getRank(key)+"]");
		}
		return sb.toString();
	}

	@Override
	public int compareTo(Rank<? extends T> o) {
		List<String> keys = new LinkedList<String>(featureToRank.keySet());
		Collections.reverse(keys);
		for(String key:keys) {
			int cmp = compareRanks(key,o);
			if(cmp!=0) {
				return cmp;
			}
		}
		return 0;
	}
	
	public static class ResponseComparator<T extends Ridge> implements Comparator<Rank<? extends T>>  {

		@Override
		public int compare(Rank<? extends T> o1, Rank<? extends T> o2) {
			return o1.getRidge().compareTo(o2.getRidge());
		}
		
	}
	
}
