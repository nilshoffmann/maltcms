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
 * $Id$
 */

package maltcms.datastructures.fragments;

import java.util.List;

import maltcms.commands.distances.CumulativeDistance;
import maltcms.commands.distances.IRecurrence;
import maltcms.commands.distances.PairwiseDistance;
import maltcms.datastructures.array.IArrayD2Double;
import maltcms.tools.PathTools;

import org.apache.commons.configuration.Configuration;
import org.jdom.Element;
import org.slf4j.Logger;

import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.ArrayDouble.D0;
import ucar.ma2.ArrayDouble.D1;
import ucar.nc2.Dimension;
import annotations.Configurable;
import annotations.ProvidesVariables;
import cross.Factory;
import cross.IConfigurable;
import cross.Logging;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.workflow.DefaultWorkflow;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import cross.io.IFileFragmentProvider;
import cross.tools.EvalTools;
import cross.tools.FragmentTools;

/**
 * Implementation of IFileFragmentProvider for PairwiseAlignment.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@ProvidesVariables(names = { "var.minimizing_array_comp" })
public class PairwiseAlignment implements IFileFragmentProvider, IConfigurable,
        IWorkflowElement {

	private final Logger log = Logging.getLogger(this.getClass());

	private IFileFragment ff;

	@Configurable(name = "alignment.save.pairwise.distance.matrix")
	private boolean savePWDM;
	@Configurable(name = "alignment.save.cumulative.distance.matrix")
	private boolean saveCDM;

	private IArrayD2Double alignment;

	private IArrayD2Double distance;

	private boolean isMinimize;

	private int refsize, querysize;

	private IRecurrence cd;

	private D0 result;

	private D1 resultVector;

	private PairwiseDistance pwd;

	private IFileFragment ref;

	private IFileFragment target;

	private List<Tuple2DI> path;

	private Class<?> creator;

	private List<Tuple2DI> localPathOptima;

	@Configurable(name = "var.alignment.cumulative_distance")
	private String cumulativeDistanceVariableName = "cumulative_distance";

	@Configurable(name = "var.alignment.pairwise_distance")
	private String pairwiseDistanceVariableName = "pairwise_distance";

	@Configurable(name = "var.minimizing_array_comp")
	private String arrayComparatorVariableName = "array_comp";

	@Configurable(name = "alignment.algorithm.distance")
	private String arrayDistanceClassName = "maltcms.commands.distances.ArrayLp";

	private IWorkflow iw;

	private int[][] predecessors;

	@Configurable(name = "normalizeAlignmentValueByMapWeights")
	private boolean normalizeAlignmentValueByMapWeights;

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.io.misc.IXMLSerializable#appendXML(org.jdom.Element)
	 */
	@Override
	public void appendXML(final Element e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void configure(final Configuration cfg) {
		this.saveCDM = cfg.getBoolean(
		        "alignment.save.cumulative.distance.matrix", false);
		this.savePWDM = cfg.getBoolean(
		        "alignment.save.pairwise.distance.matrix", false);
		this.cumulativeDistanceVariableName = cfg.getString(
		        "var.alignment.cumulative_distance", "cumulative_distance");
		this.pairwiseDistanceVariableName = cfg.getString(
		        "var.alignment.pairwise_distance", "pairwise_distance");
		this.arrayComparatorVariableName = cfg.getString(
		        "var.minimizing_array_comp", "array_comp");
		this.arrayDistanceClassName = cfg.getString(
		        "alignment.algorithm.distance",
		        "maltcms.commands.distances.ArrayLp");
		this.normalizeAlignmentValueByMapWeights = cfg.getBoolean(this
		        .getClass().getName()
		        + ".normalizeAlignmentValueByMapWeights", false);
	}

	/**
	 * @return the alignment
	 */
	public IArrayD2Double getAlignment() {
		return this.alignment;
	}

	/**
	 * @return the arrayDistanceClassName
	 */
	public String getArrayDistanceClassName() {
		return this.arrayDistanceClassName;
	}

	/**
	 * @return the cd
	 */
	public IRecurrence getCd() {
		return this.cd;
	}

	/**
	 * @return the creator
	 */
	public Class<?> getCreator() {
		return this.creator;
	}

	/**
	 * @return the distance
	 */
	public IArrayD2Double getDistance() {
		return this.distance;
	}

	/**
	 * @return the ff
	 */
	public IFileFragment getFf() {
		return this.ff;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.datastructures.workflow.IWorkflowElement#getIWorkflow()
	 */
	@Override
	public IWorkflow getIWorkflow() {
		return this.iw;
	}

	/**
	 * @return the path
	 */
	public List<Tuple2DI> getPath() {
		return this.path;
	}

	/**
	 * @return the predecessors
	 */
	public int[][] getPredecessors() {
		return this.predecessors;
	}

	/**
	 * @return the pwd
	 */
	public PairwiseDistance getPwd() {
		return this.pwd;
	}

	/**
	 * @return the querysize
	 */
	public int getQuerysize() {
		return this.querysize;
	}

	/**
	 * @return the ref
	 */
	public IFileFragment getRef() {
		return this.ref;
	}

	/**
	 * @return the refsize
	 */
	public int getRefsize() {
		return this.refsize;
	}

	/**
	 * @return the result
	 */
	public D0 getResult() {
		return this.result;
	}

	/**
	 * @return the resultVector
	 */
	public D1 getResultVector() {
		return this.resultVector;
	}

	/**
	 * @return the target
	 */
	public IFileFragment getTarget() {
		return this.target;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
	 */
	@Override
	public WorkflowSlot getWorkflowSlot() {
		return WorkflowSlot.ALIGNMENT;
	}

	/**
	 * @return the isMinimize
	 */
	public boolean isMinimize() {
		return this.isMinimize;
	}

	/**
	 * @return the normalizeAlignmentValueByMapWeights
	 */
	public boolean isNormalizeByMapWeights() {
		return this.normalizeAlignmentValueByMapWeights;
	}

	/**
	 * @return the saveCDM
	 */
	public boolean isSaveCDM() {
		return this.saveCDM;
	}

	/**
	 * @return the savePWDM
	 */
	public boolean isSavePWDM() {
		return this.savePWDM;
	}

	@Override
	public IFileFragment provideFileFragment() {
		if (this.ff == null) {
			long start = System.currentTimeMillis();
			if (this.iw == null) {
				this.iw = new DefaultWorkflow();
			}
			this.ff = FragmentTools.createFragment(this.ref, this.target,
			        this.creator, getIWorkflow().getStartupDate());
			final PathTools pt = Factory.getInstance().instantiate(
			        PathTools.class);
			long time = start;
			if (this.path == null) {
				EvalTools.notNull(new Object[] { this.ff, this.alignment,
				        this.distance, this.isMinimize, this.cd }, this);
				// path = pt.makeMap(ff, this.alignment, this.distance,
				// this.isMinimize, this.cd);
				this.path = pt.traceback(this.predecessors);
				time = System.currentTimeMillis() - start;
				this.log.info("Calculated traceback in {} milliseconds", time);
				start = System.currentTimeMillis();
				pt.savePathCSV(this.ff, this.alignment, this.distance,
				        this.path, getIWorkflow(), isMinimize());
				pt.decorate(this.ff, this.distance);
			} else {
				PathTools.getFragments(this.ff, this.path, this.distance);
			}
			final double expw = pt.getNexp()
			        * this.pwd.getDistance().getExpansionWeight();
			final double compw = pt.getNcomp()
			        * this.pwd.getDistance().getCompressionWeight();
			final double diagw = pt.getNdiag()
			        * this.pwd.getDistance().getDiagonalWeight();
			// this.log.info("Alignment arrayDistanceClassName: {}",
			// this.alignment.get(this.alignment
			// .rows() - 1, this.alignment.columns() - 1)
			// / path.size());
			final int maplength = this.path.size();

			if (this.saveCDM) {
				final IVariableFragment vf = new VariableFragment(this.ff,
				        this.cumulativeDistanceVariableName, null);
				vf.setDimensions(new Dimension[] {
				        new Dimension("reference_scan", this.refsize, true,
				                false, false),
				        new Dimension("query_scan", this.querysize, true,
				                false, false) });
				vf.setDataType(DataType.DOUBLE);
				vf.setArray(this.alignment.getArray());
				// CSVWriter csvw = new CSVWriter();
				// csvw.write(new File(target.getAbsolutePath()).getParent(),
				// target.getName()+"_cdist.csv", this.alignment);
			}
			if (this.savePWDM) {
				final IVariableFragment vf = new VariableFragment(this.ff,
				        this.pairwiseDistanceVariableName, null);
				vf.setDimensions(new Dimension[] {
				        new Dimension("reference_scan", this.refsize, true,
				                false, false),
				        new Dimension("query_scan", this.querysize, true,
				                false, false) });
				vf.setDataType(DataType.DOUBLE);
				vf.setArray(this.distance.getArray());
				// CSVWriter csvw = new CSVWriter();
				// csvw.write(new File(target.getAbsolutePath()).getParent(),
				// target.getName()+"_pwdist.csv", this.distance);
			}

			if (this.arrayComparatorVariableName != null) {
				FragmentTools.createString(this.ff,
				        this.arrayComparatorVariableName,
				        this.arrayDistanceClassName);
			}
			if (this.pwd != null) {
				FragmentTools.createString(this.ff, "pairwise_distance_class",
				        this.pwd.getClass().getName());
			}
			if (this.result == null) {
				this.result = new ArrayDouble.D0();
				double distance1 = this.alignment
				        .get(this.alignment.rows() - 1, this.alignment
				                .columns() - 1);
				if (this.normalizeAlignmentValueByMapWeights) {
					distance1 = distance1 / (expw + compw + diagw);
					this.log.info(
					        "Alignment value normalized by path weights: {}",
					        distance1);
				}
				this.result.set(distance1);
			}
			final String distvar = Factory.getInstance().getConfiguration()
			        .getString("var.alignment.distance", "distance");
			final IVariableFragment dvar = new VariableFragment(this.ff,
			        distvar);
			dvar.setArray(this.result);

			this.resultVector = new ArrayDouble.D1(1);
			this.resultVector.set(0, this.result.get());
			time = System.currentTimeMillis() - start;
			this.log.debug("Set Variables on {} in {} milliseconds", this.ff
			        .getName(), time);
		}
		return this.ff;
	}

	public void setAlignment(final IArrayD2Double al) {
		// ArrayDouble.D2 d2 = new ArrayDouble.D2(al.columns(),al.rows());
		// for(int i=0;i<al.rows();i++) {
		// for(int j=0;j<al.columns();j++) {
		// d2.set(j, i, al.get(i, j));
		// }
		// }
		this.alignment = al;
	}

	/**
	 * @param arrayDistanceClassName
	 *            the arrayDistanceClassName to set
	 */
	public void setArrayDistanceClassName(final String arrayDistanceClassName) {
		this.arrayDistanceClassName = arrayDistanceClassName;
	}

	/**
	 * @param cd
	 *            the cd to set
	 */
	public void setCd(final IRecurrence cd) {
		this.cd = cd;
	}

	/**
	 * @param creator
	 *            the creator to set
	 */
	public void setCreator(final Class<?> creator) {
		this.creator = creator;
	}

	public void setCumulativeDistance(final CumulativeDistance cd1) {
		this.cd = cd1;
	}

	/**
	 * @param distance
	 *            the distance to set
	 */
	public void setDistance(final IArrayD2Double distance) {
		this.distance = distance;
	}

	/**
	 * @param ff
	 *            the ff to set
	 */
	public void setFf(final IFileFragment ff) {
		this.ff = ff;
	}

	public void setFileFragments(final IFileFragment ref1,
	        final IFileFragment target1, final Class<?> creator1) {
		this.ref = ref1;
		this.target = target1;
		this.creator = creator1;
	}

	public void setIsMinimizing(final boolean b) {
		this.isMinimize = b;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecross.datastructures.workflow.IWorkflowElement#setIWorkflow(cross.
	 * datastructures.workflow.IWorkflow)
	 */
	@Override
	public void setIWorkflow(final IWorkflow iw1) {
		this.iw = iw1;

	}

	/**
	 * @param isMinimize
	 *            the isMinimize to set
	 */
	public void setMinimize(final boolean isMinimize) {
		this.isMinimize = isMinimize;
	}

	/**
	 * @param normalizeAlignmentValueByMapWeights
	 *            the normalizeAlignmentValueByMapWeights to set
	 */
	public void setNormalizeByMapLength(final boolean normalizeByMapWeights) {
		this.normalizeAlignmentValueByMapWeights = normalizeByMapWeights;
	}

	public void setNumberOfScansQuery(final int n) {
		this.querysize = n;
	}

	public void setNumberOfScansReference(final int n) {
		this.refsize = n;
	}

	public void setPairwiseDistance(final PairwiseDistance pwd1) {
		this.pwd = pwd1;
	}

	public void setPairwiseDistances(final IArrayD2Double pwd1) {
		// ArrayDouble.D2 d2 = new ArrayDouble.D2(pwd1.columns(),pwd1.rows());
		// for(int i=0;i<pwd1.rows();i++) {
		// for(int j=0;j<pwd1.columns();j++) {
		// d2.set(j, i, pwd1.get(i, j));
		// }
		// }
		this.distance = pwd1;
	}

	public void setPath(final List<Tuple2DI> path1) {
		this.path = path1;
	}

	/**
	 * @param predecessors
	 *            the predecessors to set
	 */
	public void setPredecessors(final int[][] predecessors) {
		this.predecessors = predecessors;
	}

	/**
	 * @param pwd
	 *            the pwd to set
	 */
	public void setPwd(final PairwiseDistance pwd) {
		this.pwd = pwd;
	}

	/**
	 * @param querysize
	 *            the querysize to set
	 */
	public void setQuerysize(final int querysize) {
		this.querysize = querysize;
	}

	/**
	 * @param ref
	 *            the ref to set
	 */
	public void setRef(final IFileFragment ref) {
		this.ref = ref;
	}

	/**
	 * @param refsize
	 *            the refsize to set
	 */
	public void setRefsize(final int refsize) {
		this.refsize = refsize;
	}

	public void setResult(final double d) {
		this.result = new ArrayDouble.D0();
		this.result.set(d);
	}

	/**
	 * @param saveCDM
	 *            the saveCDM to set
	 */
	public void setSaveCDM(final boolean saveCDM) {
		this.saveCDM = saveCDM;
	}

	/**
	 * @param savePWDM
	 *            the savePWDM to set
	 */
	public void setSavePWDM(final boolean savePWDM) {
		this.savePWDM = savePWDM;
	}

	/**
	 * @param target
	 *            the target to set
	 */
	public void setTarget(final IFileFragment target) {
		this.target = target;
	}

	public void setTraceMatrix(final int[][] predecessors1) {
		this.predecessors = predecessors1;
	}

}
