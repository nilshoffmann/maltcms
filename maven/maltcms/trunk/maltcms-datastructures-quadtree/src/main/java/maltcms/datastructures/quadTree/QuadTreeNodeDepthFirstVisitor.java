/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
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
