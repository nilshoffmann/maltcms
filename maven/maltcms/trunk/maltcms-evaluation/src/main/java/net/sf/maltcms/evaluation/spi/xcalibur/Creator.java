/**
 * 
 */
package net.sf.maltcms.evaluation.spi.xcalibur;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE

 *
 */
public class Creator {

	private String name;
	
	private String creatorVersion;
	
	public Creator(String name, String creatorVersion) {
		this.name = name;
		this.creatorVersion = creatorVersion;
	}

	public String getName() {
    	return name;
    }

	public void setName(String name) {
    	this.name = name;
    }

	public String getCreatorVersion() {
    	return creatorVersion;
    }

	public void setCreatorVersion(String creatorVersion) {
    	this.creatorVersion = creatorVersion;
    }
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName()+" "+getCreatorVersion());
		return sb.toString();
	}
	
}
