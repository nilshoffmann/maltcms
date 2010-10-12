package maltcms.io;

import java.util.Iterator;
import java.util.WeakHashMap;

import maltcms.datastructures.ms.Scan1D;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import cross.Logging;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.tools.EvalTools;

/**
 * Use {@link maltcms.datastructures.ms.Chromatogram1D} for the same functionality.
 * @author hoffmann
 *
 */
@Deprecated
public class Scan1DProvider implements IScanProvider<Scan1D> {

	private Array massesA, intensitiesA, scanIndexA, satA;

	private final WeakHashMap<Integer, Scan1D> scans = new WeakHashMap<Integer, Scan1D>();

	private IFileFragment iff = null;

	private final String massesVarName = "mass_values",
	        intensitiesVarName = "intensity_values",
	        scanIndexVarName = "scan_index",
	        scanAcquisitionTimeVarName = "scan_acquisition_time";

	public Scan1DProvider(final IFileFragment iff1) {
		this.iff = iff1;
	}

	@Override
	public void configure(final Configuration cfg) {
		// TODO Auto-generated method stub

	}

	@Override
	public Scan1D getScan(final int i) {
		if (this.scans.containsKey(Integer.valueOf(i))) {
			return this.scans.get(Integer.valueOf(i));
		}
		final IVariableFragment scanIndex = this.iff
		        .getChild(this.scanIndexVarName);
		final IVariableFragment massVals = this.iff
		        .getChild(this.massesVarName);
		final IVariableFragment intensVals = this.iff
		        .getChild(this.intensitiesVarName);
		final IVariableFragment scanAcquisitionTime = this.iff
		        .getChild(this.scanAcquisitionTimeVarName);
		massVals.setIndex(scanIndex);
		intensVals.setIndex(scanIndex);
		if (this.scanIndexA == null) {
			this.scanIndexA = scanIndex.getArray();
		}
		EvalTools.inRangeI(0, this.scanIndexA.getShape()[0] - 1, i, this);
		if (this.massesA == null) {
			this.massesA = massVals.getArray();
		}
		if (this.intensitiesA == null) {
			this.intensitiesA = intensVals.getArray();
		}
		if (this.satA == null) {
			this.intensitiesA = scanAcquisitionTime.getArray();
		}
		final int start = this.scanIndexA.getInt(this.scanIndexA.getIndex()
		        .set(i));
		final int end = (i < this.scanIndexA.getShape()[0] - 1 ? this.scanIndexA
		        .getInt(this.scanIndexA.getIndex().set(i + 1)) - 1
		        : this.massesA.getShape()[0] - 1);
		try {
			final Scan1D s = new Scan1D(this.massesA.section(
			        new int[] { start }, new int[] { end - start }),
			        this.intensitiesA.section(new int[] { start },
			                new int[] { end - start }), i, this.satA
			                .getDouble(this.satA.getIndex().set(i)));
			this.scans.put(Integer.valueOf(i), s);
			return s;
		} catch (final InvalidRangeException e) {
			Logging.getLogger(this).error(e.getLocalizedMessage());
			return null;
		}
	}

	@Override
	public int getNumberOfScans() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Iterator<Scan1D> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

}
