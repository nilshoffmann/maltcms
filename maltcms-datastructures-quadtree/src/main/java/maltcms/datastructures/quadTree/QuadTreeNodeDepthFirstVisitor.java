/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package maltcms.datastructures.quadTree;

import cross.datastructures.tuple.Tuple2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.Stack;

/**
 *
 * @author nilshoffmann
 */
public class QuadTreeNodeDepthFirstVisitor<T> implements QuadTreeNodeVisitor<T> {

    private final QuadTreeNode<T> root;
    private final Stack<QuadTreeNode<T>> stack;

    public QuadTreeNodeDepthFirstVisitor(QuadTreeNode<T> root) {
        this.root = root;
        this.stack = new Stack<QuadTreeNode<T>>();
    }

    @Override
    public LinkedList<Tuple2D<Point2D, T>> visit(LinkedList<Tuple2D<Point2D, T>> l) {
        if (this.root == null) {
            return l;
        }
        //leaf case, simply append all elements
        if (this.root.getImmediateChildren() != null) {
            for (Tuple2D<Point2D, T> tple : this.root.getImmediateChildren()) {
                l.add(tple);
            }
            return l;
        }
        //recursion case, proceed through children depth first
        if (this.root.getChildren() != null) {
            this.stack.addAll(this.root.getChildren());
            while (!this.stack.isEmpty()) {
                QuadTreeNode<T> qtn = this.stack.pop();
                QuadTreeNodeDepthFirstVisitor qtnv = new QuadTreeNodeDepthFirstVisitor(qtn);
                qtnv.visit(l);
            }
        }
        return l;
    }
}
