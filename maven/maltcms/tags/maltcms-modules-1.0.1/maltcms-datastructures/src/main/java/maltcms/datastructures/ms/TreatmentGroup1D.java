/**
 * 
 */
package maltcms.datastructures.ms;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class TreatmentGroup1D implements ITreatmentGroup<IChromatogram1D> {

	List<IChromatogram1D> l = new ArrayList<IChromatogram1D>();

	private String name = "";

	/*
	 * (non-Javadoc)
	 * 
	 * @seemaltcms.datastructures.ms.ITreatmentGroup#addChromatogram(maltcms.
	 * datastructures.ms.IChromatogram)
	 */
	@Override
	public void addChromatogram(IChromatogram1D t) {
		this.l.add(t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.ms.ITreatmentGroup#getChromatograms()
	 */
	@Override
	public List<IChromatogram1D> getChromatograms() {
		return this.l;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.ms.ITreatmentGroup#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.ms.ITreatmentGroup#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

}
