/**
 * 
 */
package net.sf.maltcms.evaluation.spi.xcalibur;

import java.util.Arrays;
import java.util.List;

import maltcms.datastructures.array.IFeatureVector;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE

 *
 */
public class Peak implements IFeatureVector {

	private Chromatogram parent;
	private String name = "";
	private String creatorname = "";
	private double rt = Double.NaN;
	private double rtstart = rt;
	private double rtstop = rt;
	private double[] mw = new double[]{};
	private double area = 0;
	private double height = 0;
	
	public static final double AREA_DEFAULT = 0;
	public static final double HEIGHT_DEFAULT = 0;
	public static final double RT_DEFAULT = Double.NaN;
	public static final double RT_START_DEFAULT = Double.NaN;
	public static final double RT_STOP_DEFAULT = Double.NaN;
	
	public Peak(Creator creator, Chromatogram parent, String name, double rt, double rtstart, double rtstop, double[] mw, double area, double height, RTUnit unit) {
		this.parent = parent;
		this.name = name;
		this.creatorname = creator.toString();
		this.mw = mw;
		this.area = area;
		this.height = height; 
		switch(unit) {
			case Hours:
			{
				this.rt = rt*(60.0*60.0);
				this.rtstart = rtstart*(60.0*60.0);
				this.rtstop = rtstop*(60.0*60.0);
				break;
			}
			case Minutes:
			{
				this.rt = rt*(60.0);
				this.rtstart = rtstart*(60.0);
				this.rtstop = rtstop*(60.0);
				break;
				
			}
			case Seconds:
			{
				this.rt = rt;
				this.rtstart = rtstart;
				this.rtstop = rtstop;
				break;
			}	
		
		}
	}
	
	/* (non-Javadoc)
	 * @see maltcms.datastructures.array.IFeatureVector#getFeature(java.lang.String)
	 */
	@Override
	public Array getFeature(String name) {
		if(name.equals("RT")) {
			ArrayDouble.D0 a = new ArrayDouble.D0();
			a.set(this.rt);
			return a;
		}else if(name.equals("RTSTART")) {
			ArrayDouble.D0 a = new ArrayDouble.D0();
			a.set(this.rtstart);
			return a;
		}else if(name.equals("RTSTOP")) {
			ArrayDouble.D0 a = new ArrayDouble.D0();
			a.set(this.rtstop);
			return a;
		}else if(name.equals("AREA")) {
			ArrayDouble.D0 a = new ArrayDouble.D0();
			a.set(this.area);
			return a;
		}else if(name.equals("HEIGHT")) {
			ArrayDouble.D0 a = new ArrayDouble.D0();
			a.set(this.height);
			return a;
		}else if(name.equals("MW")) {
			ArrayDouble.D1 a = new ArrayDouble.D1(this.mw.length);
			for(int i = 0;i<this.mw.length;i++) {
				a.set(i,this.mw[i]);				
			}
			return a;
		}else if(name.equals("FILE")) {
			ArrayChar.D1 a = new ArrayChar.D1(this.parent.getName().length());
			a.setString(this.parent.getName());
			return a;
		}else if(name.equals("NAME")) {
			ArrayChar.D1 a = new ArrayChar.D1(this.name.length());
			a.setString(this.name);
			return a;
		}else if(name.equals("CREATORNAME")) {
			ArrayChar.D1 a = new ArrayChar.D1(this.creatorname.length());
			a.setString(this.creatorname);
			return a;
		}
		
		throw new IllegalArgumentException("Feature with name "+name+" not supported!");
	}

	/* (non-Javadoc)
	 * @see maltcms.datastructures.array.IFeatureVector#getFeatureNames()
	 */
	@Override
	public List<String> getFeatureNames() {
		final String[] names = new String[]{"RT","RTSTART","RTSTOP","AREA","HEIGHT","MW","FILE","NAME","CREATORNAME"};
		return Arrays.asList(names);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(String s:getFeatureNames()) {
			sb.append(s+": "+getFeature(s)+"\t");
		}
		return sb.toString();
	}
	
	public Chromatogram getParent() {
    	return parent;
    }

	public String getName() {
    	return name;
    }

	public String getCreatorname() {
    	return creatorname;
    }

	public double getRt() {
    	return rt;
    }

	public double getRtstart() {
    	return rtstart;
    }

	public double getRtstop() {
    	return rtstop;
    }

	public double[] getMw() {
    	return mw;
    }

	public double getArea() {
    	return area;
    }

	public double getHeight() {
    	return height;
    }

}
