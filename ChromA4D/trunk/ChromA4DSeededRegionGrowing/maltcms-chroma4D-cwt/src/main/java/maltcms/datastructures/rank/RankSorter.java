package maltcms.datastructures.rank;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import maltcms.datastructures.ridge.Ridge;


public class RankSorter {

	private LinkedHashSet<String> features = new LinkedHashSet<String>();
	
	public RankSorter(List<Rank<Ridge>> l) {
		LinkedHashSet<String> union = new LinkedHashSet<String>();
		for(Rank<Ridge> r:l) {
			union.addAll(r.getFeatures());
		}
		LinkedHashSet<String> intersection = new LinkedHashSet<String>();
		for(Rank<Ridge> r:l) {	
			intersection.retainAll(r.getFeatures());
		}
		this.features = intersection;
	}
	
	public void sort(List<Rank<Ridge>> l) {
		List<String> ll = new LinkedList<String>(this.features);
		//Collections.reverse(ll);
		System.out.println("Sorting by: ");
		for(String str:ll) {
			System.out.print(str+" ");
			Collections.sort(l, new RankComparator(str));
		}
		System.out.println();
	}
	
	public void sortToOrder(List<String> features, List<Rank<Ridge>> l) {
		List<String> ll = new LinkedList<String>(features);
		Collections.reverse(ll);
		System.out.println("Sorting by: ");
		for(String str:ll) {
			System.out.print(str+" ");
			Collections.sort(l, new RankComparator(str));
		}
		System.out.println();
	}
	
}
