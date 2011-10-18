/*
 * $license$
 *
 * $Id$
 */
package maltcms.math.functions;

/**
 * The implemented similarity function should have the following properties:
 * The maximal function value must be greater than the minimal 
 * function value, assuming that a similarity between
 * two scalars is maximal iff both entities are identical/equal
 * and minimal iff they are completely unrelated and that increasing similarity
 * is reflected by a greater value of the function.
 * Note that -Inf is reserved for special cases, where the 
 * similarity is not determinable or was not calculated due to an unmet threshold
 * criterion.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public interface IScalarSimilarity {

    public double apply(double a, double b);
    
}
