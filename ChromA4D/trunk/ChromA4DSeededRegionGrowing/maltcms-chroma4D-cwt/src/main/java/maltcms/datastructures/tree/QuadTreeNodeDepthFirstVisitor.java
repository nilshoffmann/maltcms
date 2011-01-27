/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package maltcms.datastructures.tree;

import cross.datastructures.tuple.Tuple2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.Stack;

/**
 *
 * @author nilshoffmann
 */
public class QuadTreeNodeDepthFirstVisitor<T> implements QuadTreeNodeVisitor<T>{

    private final QuadTreeNode<T> root;

    private final Stack<QuadTreeNode<T>> stack;

    public QuadTreeNodeDepthFirstVisitor(QuadTreeNode<T> root) {
        this.root = root;
        this.stack = new Stack<QuadTreeNode<T>>();
    }

    public LinkedList<T> visit(LinkedList<T> l) {
        //leaf case, simply append all elements
        if(this.root.getImmediateChildren() != null) {
            for(Tuple2D<Point2D,T> tple:this.root.getImmediateChildren()) {
                l.add(tple.getSecond());
            }
            return l;
        }
        //recursion case, proceed through children depth first
        if(this.root.getChildren()!=null) {
            this.stack.addAll(this.root.getChildren());
            while(!this.stack.isEmpty()) {
                QuadTreeNode<T> qtn = this.stack.pop();
                QuadTreeNodeDepthFirstVisitor qtnv = new QuadTreeNodeDepthFirstVisitor(qtn);
                qtnv.visit(l);
            }
        }
        return l;
    }

}
