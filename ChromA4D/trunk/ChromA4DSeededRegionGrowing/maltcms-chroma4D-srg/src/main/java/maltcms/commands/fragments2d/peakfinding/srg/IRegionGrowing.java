package maltcms.commands.fragments2d.peakfinding.srg;

import java.awt.Point;
import java.util.List;

import ucar.ma2.ArrayDouble;

import maltcms.datastructures.caches.IScanLine;
import maltcms.datastructures.peak.PeakArea2D;
import cross.IConfigurable;
import cross.datastructures.fragments.IFileFragment;

public interface IRegionGrowing extends IConfigurable {

	List<PeakArea2D> getAreasFor(List<Point> seeds, IFileFragment ff, IScanLine slc);

	double getMinDist();
	
	List<ArrayDouble.D1> getIntensities();
	
}
