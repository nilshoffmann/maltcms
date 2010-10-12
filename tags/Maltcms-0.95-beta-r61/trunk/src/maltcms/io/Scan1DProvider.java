package maltcms.io;

import java.util.WeakHashMap;

import maltcms.datastructures.ms.Scan1D;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import cross.Logging;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.tools.EvalTools;

public class Scan1DProvider implements IScanProvider<Scan1D> {
	
	private Array massesA, intensitiesA, scanIndexA, satA;
	
	private WeakHashMap<Integer,Scan1D> scans = new WeakHashMap<Integer,Scan1D>();
	
	private IFileFragment iff = null;
	
	private String massesVarName = "mass_values", intensitiesVarName = "intensity_values", scanIndexVarName = "scan_index", scanAcquisitionTimeVarName = "scan_acquisition_time";
	
	public Scan1DProvider(IFileFragment iff1) {
		this.iff = iff1;
	}
	
	@Override
	public Scan1D getScan(int i) {
		if(scans.containsKey(Integer.valueOf(i))) {
			return scans.get(Integer.valueOf(i));
		}
		IVariableFragment scanIndex= this.iff.getChild(scanIndexVarName);
		IVariableFragment massVals = this.iff.getChild(massesVarName);
		IVariableFragment intensVals = this.iff.getChild(intensitiesVarName);
		IVariableFragment scanAcquisitionTime = this.iff.getChild(scanAcquisitionTimeVarName);
		massVals.setIndex(scanIndex);
		intensVals.setIndex(scanIndex);
		if(this.scanIndexA == null) {
			this.scanIndexA = scanIndex.getArray();
		}
		EvalTools.inRangeI(0, this.scanIndexA.getShape()[0]-1, i, this);
		if(this.massesA == null) {
			this.massesA = massVals.getArray();
		}
		if(this.intensitiesA == null) {
			this.intensitiesA = intensVals.getArray();
		}
		if(this.satA == null) {
			this.intensitiesA = scanAcquisitionTime.getArray();
		}
		int start = this.scanIndexA.getInt(this.scanIndexA.getIndex().set(i));
		int end = (i<this.scanIndexA.getShape()[0]-1?this.scanIndexA.getInt(this.scanIndexA.getIndex().set(i+1))-1:this.massesA.getShape()[0]-1);
		try {
			Scan1D s = new Scan1D(this.massesA.section(new int[]{start}, new int[]{end-start}),this.intensitiesA.section(new int[]{start}, new int[]{end-start}),i,this.satA.getDouble(this.satA.getIndex().set(i)));
			scans.put(Integer.valueOf(i),s);
			return s;
		} catch (InvalidRangeException e) {
			Logging.getLogger(this).error(e.getLocalizedMessage());
			return new Scan1D();
		}
	}

	@Override
	public void configure(Configuration cfg) {
		// TODO Auto-generated method stub
		
	}

}
