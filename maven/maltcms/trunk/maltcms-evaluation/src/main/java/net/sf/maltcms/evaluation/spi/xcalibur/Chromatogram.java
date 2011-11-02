/**
 * 
 */
package net.sf.maltcms.evaluation.spi.xcalibur;


/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE

 *
 */
public class Chromatogram {

	private final String name;
	
	public Chromatogram(String name) {
		this.name = name;
	}

	public String getName() {
    	return name;
    }
	
	public String toString() {
		return getName();
	}
	
}
