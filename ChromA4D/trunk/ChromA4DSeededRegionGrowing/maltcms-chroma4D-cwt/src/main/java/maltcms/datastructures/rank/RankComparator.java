package maltcms.datastructures.rank;

import java.util.Comparator;

import maltcms.datastructures.ridge.Ridge;

public class RankComparator implements Comparator<Rank<Ridge>> {

	private final String feature;
	
	public RankComparator(String feature) {
		this.feature = feature;
	}
	
	@Override
	public int compare(Rank<Ridge> o1, Rank<Ridge> o2) {
		double d1 = o1.getRank(this.feature);
		double d2 = o2.getRank(this.feature);
		return Double.compare(d1, d2);
	}

}
