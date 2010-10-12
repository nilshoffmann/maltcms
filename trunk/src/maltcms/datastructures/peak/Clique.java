/**
 * 
 */
package maltcms.datastructures.peak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import maltcms.tools.ArrayTools;

import org.jfree.data.statistics.BoxAndWhiskerCalculator;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.slf4j.Logger;

import cross.Logging;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class Clique {

	private static long CLIQUEID = -1;

	private long id = -1;

	private Logger log = Logging.getLogger(this);

	private double cliqueMean = 0, cliqueVar = 0;

	private HashSet<Peak> clique = new HashSet<Peak>();

	private Peak centroid = null;

	public Clique() {
		this.id = ++CLIQUEID;
	}

	public long getID() {
		return this.id;
	}

	public boolean addPeak(Peak p) throws IllegalArgumentException {
		if (clique.contains(p)) {
			log.debug("Peak {} already contained in clique!", p);
			return false;
		} else {
			// if (clique.isEmpty()) {

			// check bidi best hit assumption
			// bail out if assumption fails!
			for (Peak q : getPeakList()) {
				if (!p.isBidiBestHitFor(q)) {
					log
					        .debug(
					                "Peak q: {} in clique is not a bidirectional best hit for peak p: {}",
					                q, p);
					return false;
				}
			}
			log.debug("Adding peak {} to clique", p);
			update(p);
			clique.add(p);
			selectCentroid();
			return true;
		}
	}

	public void clear() {
		cliqueMean = 0;
		cliqueVar = 0;
		centroid = null;
		clique.clear();
	}

	public BoxAndWhiskerItem createRTBoxAndWhisker() {
		List<Double> l = new ArrayList<Double>();
		for (Peak p : this.clique) {
			l.add(centroid.getScanAcquisitionTime()
			        - p.getScanAcquisitionTime());
		}
		return BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(l);
	}

	public BoxAndWhiskerItem createApexTicBoxAndWhisker() {
		List<Double> l = new ArrayList<Double>();
		for (Peak p : this.clique) {
			l.add(Math.log(ArrayTools.integrate(centroid.getMSIntensities()))
			        - ArrayTools.integrate(p.getMSIntensities()));
		}
		return BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(l);
	}

	private void selectCentroid() {
		double mindist = Double.POSITIVE_INFINITY;
		double[] dists = new double[clique.size()];
		int i = 0;
		Peak[] peaks = clique.toArray(new Peak[] {});
		for (Peak peak : peaks) {
			for (Peak peak1 : peaks) {
				dists[i] += Math.pow(peak.getScanAcquisitionTime()
				        - peak1.getScanAcquisitionTime(), 2.0d);
			}
			i++;
		}
		int mindistIdx = 0;
		for (int j = 0; j < dists.length; j++) {
			if (dists[j] < mindist) {
				mindist = dists[j];
				mindistIdx = j;
			}
		}
		this.log.debug("Clique centroid is {}", peaks[mindistIdx]);
		this.centroid = peaks[mindistIdx];
	}

	private void update(Peak p) {
		int n = 0;
		double mean = cliqueMean;
		double var = cliqueVar;
		log
		        .debug(
		                "Clique variance before adding peak: {}, clique mean before: {}",
		                var, mean);
		double delta = 0;
		double rt = p.getScanAcquisitionTime();
		n = clique.size() + 1;
		delta = rt - mean;
		if (n > 0) {
			mean = mean + delta / n;
		}
		if (n > 2) {
			var = (var + (delta * (rt - mean))) / ((double) (n - 2));
		}
		cliqueMean = mean;
		cliqueVar = var;
		log
		        .debug(
		                "Clique variance after adding peak: {}, clique mean before: {}",
		                var, mean);
	}

	public double getCliqueRTVariance() {
		return this.cliqueVar;
	}

	public double getCliqueRTMean() {
		return this.cliqueMean;
	}

	public Peak getCliqueCentroid() {
		return this.centroid;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (this.centroid != null) {
			sb.append("Center: " + this.centroid.toString() + "\n");
		} else {
			sb.append("Center: null\n");
		}
		sb.append("\tMean: " + this.cliqueMean + "\n");
		sb.append("\tVariance: " + this.cliqueVar + "\n");
		for (Peak p : this.clique) {
			if (p != null) {
				sb.append(p.toString());
			} else {
				sb.append("null");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public List<Peak> getPeakList() {
		List<Peak> peaks = new ArrayList<Peak>(this.clique);
		Collections.sort(peaks, new Comparator<Peak>() {

			@Override
			public int compare(Peak o1, Peak o2) {
				return o1.getAssociation().getName().compareTo(
				        o2.getAssociation().getName());
			}
		});
		return peaks;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
