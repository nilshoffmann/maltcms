package maltcms.datastructures.ms;

import java.util.Iterator;
import java.util.List;

import maltcms.tools.MaltcmsTools;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.exception.NotImplementedException;

public class ProfileChromatogram1D implements IChromatogram1D {

private IFileFragment parent;
	
	private int nscans = -1;

	public ProfileChromatogram1D(final IFileFragment e) {
		this.parent = e;
		this.nscans = MaltcmsTools.getNumberOfScans(e);
	}

	@Override
	public int getIndexFor(double sat) {
		throw new NotImplementedException();
	}

	@Override
	public Array getScanAcquisitionTime() {
		throw new NotImplementedException();
	}

	@Override
	public String getScanAcquisitionTimeUnit() {
		throw new NotImplementedException();
	}

	@Override
	public void configure(final Configuration cfg) {
	}

	public IFileFragment getFileFragment() {
		return this.parent;
	}

	public double getIntegratedIntensity(final int scan) {
		return MaltcmsTools.getTIC(this.parent, scan);
	}

	public List<Array> getIntensities() {
		return MaltcmsTools.getBinnedMZIs(this.parent).getSecond();
	}

	public List<Array> getMasses() {
		return MaltcmsTools.getBinnedMZIs(this.parent).getFirst();
	}

	@Override
	public Scan1D getScan(final int scan) {
		final Tuple2D<Array, Array> t = MaltcmsTools.getBinnedMS(this.parent, scan);
		final Scan1D s = new Scan1D(t.getFirst(), t.getSecond(), scan,
		        MaltcmsTools.getScanAcquisitionTime(this.parent, scan));
		return s;
	}

	/**
	 * This iterator acts on the underlying collection of scans in
	 * Chromatogram1D, so be careful with concurrent access / modification!
	 */
	@Override
	public Iterator<Scan1D> iterator() {

		final Iterator<Scan1D> iter = new Iterator<Scan1D>() {

			private int currentPos = 0;

			@Override
			public boolean hasNext() {
				if (this.currentPos < getNumberOfScans() - 1) {
					return true;
				}
				return false;
			}

			@Override
			public Scan1D next() {
				return getScan(this.currentPos++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException(
				        "Can not remove scans with iterator!");
			}

		};
		return iter;
	}
	
	@Override
	public int getNumberOfScans() {
		return this.nscans;
	}

	@Override
	public IFileFragment getParent() {
		return this.parent;
	}

}
