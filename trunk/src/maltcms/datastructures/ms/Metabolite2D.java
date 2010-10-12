package maltcms.datastructures.ms;

import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.IndexIterator;

public class Metabolite2D extends Metabolite implements IRetentionInfo2D {

	private double rt2 = 0;
	
	private double ri2 = 0;
	
	private String rt2unit = "sec";
	
	public Metabolite2D(final String name1, final String id1,
	        final String id_type1, final int dbno1, final String comments1,
	        final String formula1, final String date1, final double ri1,
	        final double retentionTime1, final String retentionTimeUnit1,
	        final int mw1, final String sp1, final String shortName,
	        final ArrayDouble.D1 masses1, final ArrayInt.D1 intensities1, final double ri2, final double retentionTime2, final String retentionTimeUnit2) {
		super(name1,id1,id_type1,dbno1,comments1,formula1,date1,ri1,retentionTime1,retentionTimeUnit1,mw1,sp1,shortName,masses1,intensities1);
		this.rt2 = retentionTime2;
		this.rt2unit = retentionTimeUnit2;
		this.ri2 = ri2;
	}
	
	@Override
	public double getRetentionIndex2D() {
		return this.ri2;
	}

	@Override
	public double getRetentionTime2D() {
		return this.rt2;
	}

	@Override
	public String getRetentionTimeUnit2D() {
		return this.rt2unit;
	}

	@Override
	public void setRetentionIndex2D(double d) {
		this.ri2 = d;
	}

	@Override
	public void setRetentionTime2D(double d) {
		this.rt2 = d;
	}

	@Override
	public void setRetentionTimeUnit2D(String s) {
		this.rt2unit = s;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append("Name: " + getName());
		sb.append("\n");
		sb.append("Synon: DATE:" + getDate());
		sb.append("\n");
		sb.append("Synon: NAME:" + getShortName());
		sb.append("\n");
		sb.append("Synon: SP:" + getSP());
		sb.append("\n");
		sb.append("Synon: " + getIDType() + ":" + getID());
		sb.append("\n");
		sb.append("Synon: RI:" + getRetentionIndex());
		sb.append("\n");
		if(getRetentionIndex2D() > 0) {
			sb.append("Synon: RI2:" + getRetentionIndex2D());
			sb.append("\n");
		}
		if (getRetentionTime() > 0) {
			sb.append("Synon: RT:" + getRetentionTimeUnit()
			        + getRetentionTime());
			sb.append("\n");
		}
		if (getRetentionTime2D() > 0) {
			sb.append("Synon: RT2:" + getRetentionTimeUnit2D()
			        + getRetentionTime2D());
			sb.append("\n");
		}
		sb.append("Comments: " + getComments());
		sb.append("\n");
		if (getFormula() != null) {
			sb.append("Formula: " + getFormula());
			sb.append("\n");
		}
		if (getMW() > 0) {
			sb.append("MW: " + getMW());
			sb.append("\n");
		}
		sb.append("DB#: " + getDBNO());
		sb.append("\n");
		sb.append("Num Peaks: " + getMassSpectrum().getFirst().getShape()[0]);
		sb.append("\n");
		final IndexIterator mi = getMassSpectrum().getFirst().getIndexIterator();
		final IndexIterator ii = getMassSpectrum().getSecond().getIndexIterator();
		int linepointcount = 0;
		while (mi.hasNext() && ii.hasNext()) {
			sb.append(mi.getIntNext() + " " + ii.getIntNext() + ";");
			if (linepointcount == 5) {
				sb.append("\n");
				linepointcount = 0;
			} else {
				sb.append(" ");
				linepointcount++;
			}
		}
		sb.append("\n");
		return sb.toString();
	}
	
	public void update(final IMetabolite m) {
		try{
			super.update(m);
			if(m instanceof Metabolite2D) {
				Metabolite2D m2 = (Metabolite2D)m;
				if(m2.getRetentionIndex2D()>0) {
					setRetentionIndex2D(m2.getRetentionIndex2D());
				}
				if(m2.getRetentionTime2D()>0) {
					setRetentionTime2D(m2.getRetentionTime2D());
				}
				if((m2.getRetentionTimeUnit2D()!=null)&&!m2.getRetentionTimeUnit2D().isEmpty()) {
					setRetentionTimeUnit2D(m2.getRetentionTimeUnit2D());
				}
			}
			
		}catch(IllegalArgumentException e) {
				throw e;
		}
	}
	
}
