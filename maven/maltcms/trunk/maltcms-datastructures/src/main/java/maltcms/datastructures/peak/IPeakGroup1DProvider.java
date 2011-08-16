/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package maltcms.datastructures.peak;

/**
 *
 * @author nils
 */
public interface IPeakGroup1DProvider<T extends Peak1D> extends Iterable<T> {

	/**
	 * 
	 * @param i the scan index to retrieve
	 * @return the Peak1D
	 */
	public T getPeak(int i);
	
	/**
	 * 
	 * @return the number of peaks
	 */
	public int getNumberOfPeaks();
}
