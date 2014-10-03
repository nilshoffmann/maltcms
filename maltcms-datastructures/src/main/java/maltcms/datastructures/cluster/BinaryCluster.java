/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
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
package maltcms.datastructures.cluster;

import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Concrete implementation of a binary cluster.
 *
 * @author Nils Hoffmann
 * 
 */
public class BinaryCluster extends ACluster {

    private BinaryCluster lchild, rchild;

    /**
     * <p>Constructor for BinaryCluster.</p>
     *
     * @param lchild1 a {@link maltcms.datastructures.cluster.BinaryCluster} object.
     * @param rchild1 a {@link maltcms.datastructures.cluster.BinaryCluster} object.
     * @param distl a double.
     * @param distr a double.
     * @param dist an array of double.
     * @param id a int.
     */
    public BinaryCluster(final BinaryCluster lchild1,
            final BinaryCluster rchild1, final double distl,
            final double distr, final double[] dist, final int id) {

        setLChild(lchild1);
        setRChild(rchild1);
        setSize(lchild1.getSize() + rchild1.getSize());
        getLChild().setDistanceToParent(distl);
        getRChild().setDistanceToParent(distr);
        setDistances(dist);
        setName(lchild1, rchild1);
        // this.name = "("+this.lchild.getName()+" "+this.rchild.getName()+")";
        // if(l!=null && r!=null){
        // this.name = NeighborJoinCluster.getConsensString(lchild)+"_vs_"+
        // }else{
        // }
        // this.level = Math.max(this.lchild.getLevel(),
        // this.rchild.getLevel())+1;
        setID(id);
        // System.out.println("Created new ICluster "+this.name);
    }

    /**
     * <p>Constructor for BinaryCluster.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param id a int.
     * @param dist an array of double.
     */
    public BinaryCluster(final String name, final int id, final double[] dist) {
        setName(name);
        setID(id);
        setDistances(dist);
    }

    /**
     * <p>getLChild.</p>
     *
     * @return a {@link maltcms.datastructures.cluster.ICluster} object.
     */
    public ICluster getLChild() {
        return this.lchild;
    }

    /**
     * <p>getLChildID.</p>
     *
     * @return a int.
     */
    public int getLChildID() {
        if (this.lchild == null) {
            return -1;
        }
        return this.lchild.getID();
    }

    /**
     * <p>getRChild.</p>
     *
     * @return a {@link maltcms.datastructures.cluster.ICluster} object.
     */
    public ICluster getRChild() {
        return this.rchild;
    }

    /**
     * <p>getRChildID.</p>
     *
     * @return a int.
     */
    public int getRChildID() {
        if (this.rchild == null) {
            return -1;
        }
        return this.rchild.getID();
    }

    /**
     * <p>setLChild.</p>
     *
     * @param bc a {@link maltcms.datastructures.cluster.BinaryCluster} object.
     */
    public void setLChild(final BinaryCluster bc) {
        this.lchild = bc;
    }

    /**
     * <p>setRChild.</p>
     *
     * @param bc a {@link maltcms.datastructures.cluster.BinaryCluster} object.
     */
    public void setRChild(final BinaryCluster bc) {
        this.rchild = bc;
    }

    // public int getLevel(){
    // return this.level;
    // }
    /** {@inheritDoc} */
    @Override
    public String toNewick() {
        // (NODE) each Node is encapsulated in parentheses
        String name = getName() == null ? "" : getName();
        name = (this.lchild == null) && (this.rchild == null) ? name : "("
                + this.lchild.toNewick() + "," + this.rchild.toNewick() + ")";

        name = name
                + ((getDistanceToParent() == 0.0d) ? "" : ":"
                + getDistanceToParent());
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        String name = "";
        final String nodeType = "\\Tr";
        // if(this.name!=null){
        // return "\\pstree{\\Tcircle{"+this.name+"}{}";
        // }else if(this.lchild!=null && this.rchild!=null){
        if (this.lchild != null) {
            this.lchild.setLabelString("\\trput");
        }
        if (this.rchild != null) {
            this.rchild.setLabelString("\\tlput");
        }
        if ((this.lchild == null) && (this.rchild == null)) {// leaf
            //
            name = getName() == null ? "" : getName();
            name = nodeType + "{" + name + "}";
        } else {// branch
            name = "\\TC*";
            // name = "";
        }
        // int lskip = this.lchild==null?0:this.level-this.lchild.getLevel();
        // int rskip = this.rchild==null?0:this.level-this.rchild.getLevel();
        // String child1 =
        // this.lchild==null?"":"\\skiplevels{"+lskip+"}"+this.lchild.toString()+
        // "\\endskiplevels";
        // String child2 =
        // this.rchild==null?"":"\\skiplevels{"+rskip+"}"+this.rchild.toString()+
        // "\\endskiplevels";
        final String child1 = this.lchild == null ? "" : this.lchild.toString();
        final String child2 = this.rchild == null ? "" : this.rchild.toString();
        Locale.setDefault(Locale.US);
        // DecimalFormat df = new DecimalFormat("0.###");
        final DecimalFormat df = new DecimalFormat("0.#####");
        final String d = df.format(getDistanceToParent());
        // String d = df.format(this.level);
        // return
        // "\\skiplevels{"+this.level+"}\\pstree{"+name+this.labelstring+"{"+d+
        // "}}{"+child1+child2+"}";

        return "\\pstree{" + name + getLabelString() + "{" + d + "}}{" + child2
                + child1 + "}";
        // }
        // return "\\pstree{}{}";
    }
}
