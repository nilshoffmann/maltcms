/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package maltcms.datastructures.tree;

import java.util.LinkedList;

/**
 *
 * @author nilshoffmann
 */
public interface QuadTreeNodeVisitor<T> {

    public LinkedList<T> visit(LinkedList<T> l);

}
