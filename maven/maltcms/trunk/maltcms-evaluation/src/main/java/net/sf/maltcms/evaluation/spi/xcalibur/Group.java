/**
 * 
 */
package net.sf.maltcms.evaluation.spi.xcalibur;

import java.util.List;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE

 *
 */
public class Group {

	private final String name;
	
	private List<Chromatogram> chromatograms;
	
	public Group(String name, List<Chromatogram> chromatograms) {
		this.name = name;
		this.chromatograms = chromatograms;
	}

	public String getName() {
    	return name;
    }

	public List<Chromatogram> getChromatograms() {
    	return chromatograms;
    }

	public void setChromatograms(List<Chromatogram> chromatograms) {
    	this.chromatograms = chromatograms;
    }
	
}
