/*
 * Copyright (C) 2009, 2010 Mathias Wilhelm mwilhelm A T
 * TechFak.Uni-Bielefeld.DE
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
 * $Id: Alignment2D.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */
package maltcms.commands.fragments2d.testing;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import maltcms.datastructures.array.IFeatureVector;
import maltcms.datastructures.constraint.ConstraintFactory;
import maltcms.datastructures.feature.FeatureVectorFactory;
import maltcms.experimental.operations.AlignmentFactory;
import maltcms.experimental.operations.Cosine;
import maltcms.experimental.operations.IAlignment;
import maltcms.experimental.operations.IOptimizationFunction;
import maltcms.experimental.operations.ThreePredecessorsOptimization;
import maltcms.experimental.operations.TwoFeatureVectorOperation;
import maltcms.tools.ArrayTools2;
import maltcms.tools.ImageTools;
import ucar.ma2.Array;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;

/**
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
@RequiresVariables(names = { "var.maxms_1d_horizontal",
		"var.maxms_1d_horizontal_index", "var.maxms_1d_vertical",
		"var.maxms_1d_vertical_index", "var.total_intensity",
		"var.second_column_scan_index" })
@RequiresOptionalVariables(names = { "" })
@ProvidesVariables(names = { "var.warp_path_i", "var.warp_path_j" })
public class Alignment2D extends AFragmentCommand {

	@Configurable(name = "var.maxms_1d_horizontal", value = "maxms_1d_horizontal")
	private String horizontalVar = "maxms_1d_horizontal";
	@Configurable(name = "var.maxms_1d_horizontal_index", value = "maxms_1d_horizontal_index")
	private String horizontalIndexVar = "maxms_1d_horizontal_index";
	@Configurable(name = "var.maxms_1d_vertical", value = "maxms_1d_vertical")
	private String verticalVar = "maxms_1d_vertical";
	@Configurable(name = "var.maxms_1d_vertical_index", value = "maxms_1d_vertical_index")
	private String verticalIndexVar = "maxms_1d_vertical_index";
	@Configurable(name = "var.total_intensity", value = "total_intensity")
	private String totalIntensity = "total_intensity";
	@Configurable(name = "var.second_column_scan_index", value = "second_column_scan_index")
	private String secondColumnScanIndexVar = "second_column_scan_index";

	@Configurable(value = "false")
	private boolean filter = false;
	@Configurable(value = "false")
	private boolean scale = false;

	/**
	 * {@inheritDoc}
	 * 
	 * @return
	 */
	@Override
	public String getDescription() {
		return "2D Alignment in spe";
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return
	 */
	@Override
	public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {

		if (t.size() < 2) {
			return t;
		}

		IFileFragment lhsff, rhsff;
		List<IFeatureVector> l1h, l1v, l2h, l2v;
		final AlignmentFactory af = new AlignmentFactory();
		IAlignment ia;
		// prepare recursion
		final IOptimizationFunction iof = new ThreePredecessorsOptimization();
		for (String s : iof.getStates()) {
			if (s.equals("NW")) {
				iof.setWeight(s, 2.25d);
			}
		}
		// prepare pairwise function
		final TwoFeatureVectorOperation tfvo = new Cosine();
		((Cosine) tfvo).setFeatureVarName("FEATURE0");
		double cost;
		List<Array> scanlinesi, scanlinesj;
		for (int i = 0; i < t.size(); i++) {
			lhsff = t.get(i);

			scanlinesi = getScanlineFor(lhsff);

			for (int j = i + 1; j < t.size(); j++) {
				rhsff = t.get(j);

				scanlinesj = getScanlineFor(rhsff);

				// prepare alignment
				ia = af.getDTWInstance();
				// set alignment properties
				ia.setLHSID(lhsff.getName());
				ia.setRHSID(rhsff.getName());
				// set recursion and pairwise function
				ia.setIOptimizationFunction(iof);
				ia.setPairwiseFeatureVectorOperation(tfvo);

				// HORIZONTAL
				// prepare feature vectors
				l1h = getFeatureList(lhsff, this.horizontalVar,
						this.horizontalIndexVar, this.filter, this.scale);
				l2h = getFeatureList(rhsff, this.horizontalVar,
						this.horizontalIndexVar, this.filter, this.scale);
				System.out.println("l1v size: " + l1h.size());

				// set constraints
				ia
						.setConstraints(ConstraintFactory.getInstance()
								.createBandConstraint(0, 0, l1h.size(),
										l2h.size(), 0.5));
				// apply and retrieve score
				cost = ia.apply(l1h, l2h);
				System.out.println("DTW score h: " + cost);
				// retrieve map
				List<Point> h = ia.getMap();

				for (Point p : h) {
					System.out.println(p.x + " - " + p.y);
				}

				// VERTICAL
				// prepare feature vectors
				l1v = getFeatureList(lhsff, this.verticalVar,
						this.verticalIndexVar, this.filter, this.scale);
				l2v = getFeatureList(rhsff, this.verticalVar,
						this.verticalIndexVar, this.filter, this.scale);
				System.out.println("l1v size: " + l1v.size());

				// set constraints
				ia
						.setConstraints(ConstraintFactory.getInstance()
								.createBandConstraint(0, 0, l1v.size(),
										l2v.size(), 0.5));
				// apply and retrieve score
				cost = ia.apply(l1v, l2v);
				System.out.println("DTW score v: " + cost);
				// retrieve map
				List<Point> v = ia.getMap();

				Visualization2D vis = new Visualization2D();

				final BufferedImage image = vis.createImage(scanlinesi,
						scanlinesj, h, v);

				final String baseFilename = StringTools.removeFileExt(lhsff
						.getName())
						+ "_vs_" + StringTools.removeFileExt(rhsff.getName());
				final String filename = baseFilename + "_rgb";
				final File out = ImageTools.saveImage(image, filename, "png",
						getIWorkflow().getOutputDirectory(this), this);

				// for (Point p : v) {
				// System.out.println(p.x + ":" + p.y);
				// }
			}
		}

		return t;
	}

	private List<IFeatureVector> getFeatureList(final IFileFragment ff,
			final String varName, final String indexName, final boolean filter,
			final boolean scale) {
		final FeatureVectorFactory fvf = FeatureVectorFactory.getInstance();
		ff.getChild(varName).setIndex(ff.getChild(indexName));

		List<Array> feature = ff.getChild(varName).getIndexedArray();
		if (filter) {
			final List<Integer> hold = new ArrayList<Integer>();
			feature = ArrayTools2.filterInclude(feature, hold);
		}
		if (scale) {
			feature = ArrayTools2.sqrt(feature);
		}

		final List<IFeatureVector> featureList = fvf
				.createFeatureVectorList(feature);

		return featureList;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return
	 */
	@Override
	public WorkflowSlot getWorkflowSlot() {
		return WorkflowSlot.ALIGNMENT;
	}

	/**
	 * Getter.
	 * 
	 * @param ff
	 *            file fragment
	 * @return scanlines
	 */
	protected List<Array> getScanlineFor(final IFileFragment ff) {
		ff.getChild(this.totalIntensity).setIndex(
				ff.getChild(this.secondColumnScanIndexVar));
		final List<Array> scanlines = ff.getChild(this.totalIntensity)
				.getIndexedArray();
		return scanlines;
	}

}
