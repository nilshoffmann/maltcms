/**
 * 
 */
package net.sf.maltcms.evaluation.api.classification;

/**
 * A class which allows the definition of a category for an entity.
 * This could be for example the name of the file, for which an entity
 * was generated (e.g. a feature, such as a peak).
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 *
 */
public class Category implements Comparable<Category> {

    private final String name;

    public Category(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return getName();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Category o) {
        return toString().compareTo(o.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Category)) {
            return super.equals(obj);
        }
        Category other = (Category) obj;
        return getName().equals(other.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
