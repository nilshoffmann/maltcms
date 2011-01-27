package maltcms.commands.fragments2d.peakfinding.srg;

import java.awt.Point;
import java.util.List;

import maltcms.datastructures.caches.IScanLine;
import maltcms.datastructures.peak.PeakArea2D;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.ArrayDouble.D1;

import cross.datastructures.fragments.IFileFragment;

public class AllAtOnceRegionGrowing implements IRegionGrowing {

	@Override
	public List<PeakArea2D> getAreasFor(List<Point> seeds, IFileFragment ff, IScanLine slc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void configure(Configuration cfg)  {
		// TODO Auto-generated method stub

	}

	@Override
	public double getMinDist() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<D1> getIntensities() {
		// TODO Auto-generated method stub
		return null;
	}

}
