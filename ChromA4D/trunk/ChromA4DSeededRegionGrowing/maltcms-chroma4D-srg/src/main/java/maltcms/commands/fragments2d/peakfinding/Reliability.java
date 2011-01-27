package maltcms.commands.fragments2d.peakfinding;

public class Reliability {

	double min = 0, max = 0, rel = 0;

	public Reliability(double rel) {
		this.min = rel;
		this.max = rel;
		this.rel = rel;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	public double getReliability() {
		return rel;
	}

	public void addRel(double newRel) {
		if (newRel < this.min) {
			this.min = newRel;
		}
		if (newRel > this.max) {
			this.max = newRel;
		}
		this.rel *= newRel;
	}

}
