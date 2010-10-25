/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: BinaryCluster.java 73 2009-12-16 08:45:14Z nilshoffmann $
 */

package maltcms.datastructures.cluster;

import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Concrete implementation of a binary cluster.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class BinaryCluster extends ACluster {

	private BinaryCluster lchild, rchild;

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

	public BinaryCluster(final String name, final int id, final double[] dist) {
		setName(name);
		setID(id);
		setDistances(dist);
	}

	public ICluster getLChild() {
		return this.lchild;
	}

	public int getLChildID() {
		if (this.lchild == null) {
			return -1;
		}
		return this.lchild.getID();
	}

	public ICluster getRChild() {
		return this.rchild;
	}

	public int getRChildID() {
		if (this.rchild == null) {
			return -1;
		}
		return this.rchild.getID();
	}

	public void setLChild(final BinaryCluster bc) {
		this.lchild = bc;
	}

	public void setRChild(final BinaryCluster bc) {
		this.rchild = bc;
	}

	// public int getLevel(){
	// return this.level;
	// }

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
