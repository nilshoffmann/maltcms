/*
 * $license$
 *
 * $Id$
 */
package maltcms.math.functions;

import ucar.ma2.Array;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public interface IScalarArraySimilarity {

    public double apply(double[] s1, double[] s2, Array a1, Array a2);
    
    public IScalarSimilarity[] getScalarSimilarities();
    
    public void setScalarSimilarities(IScalarSimilarity... scalarSimilarities);
    
    public IArraySimilarity[] getArraySimilarities();
    
    public void setArraySimilarities(IArraySimilarity... arraySimilarities);
    
}
