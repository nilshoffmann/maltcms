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
package maltcms.datastructures.warp;

import java.util.List;

import maltcms.tools.MaltcmsTools;

import org.slf4j.Logger;

import ucar.ma2.Array;
import cross.Logging;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FragmentTools;

/**
 * Specialization for MZIWarp, using mass spectra.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class MZIWarpInput implements IWarpInput {

	private final Logger log = Logging.getLogger(this.getClass());

	private List<Tuple2DI> path = null;

	private IFileFragment targetFile = null;

	private IFileFragment refFile = null;

	private IFileFragment queryFile = null;

	private Tuple2D<List<Array>, List<Array>> tuple = null;

	public MZIWarpInput(final IFileFragment ff, final IWorkflow iw) {
		this.log
		        .info("#############################################################################");
		final String s = this.getClass().getName();
		this.log.info("# {} running", s);
		this.log
		        .info("#############################################################################");
		this.log.info("Preparing input for {} with sources {}", new Object[] {
		        ff.getAbsolutePath(), ff.getSourceFiles() });
		final IFileFragment queryFile1 = FragmentTools.getRHSFile(ff);
		final IFileFragment referenceFile = FragmentTools.getLHSFile(ff);

		final List<Array> i1 = MaltcmsTools.getBinnedMZIs(referenceFile)
		        .getSecond();
		final List<Array> i2 = MaltcmsTools.getBinnedMZIs(queryFile1)
		        .getSecond();
		final Tuple2D<List<Array>, List<Array>> t = new Tuple2D<List<Array>, List<Array>>(
		        i1, i2);
		final IFileFragment target = FragmentTools.createFragment(
		        referenceFile, queryFile1, iw.getOutputDirectory(this));
		init(MaltcmsTools.getWarpPath(ff), t, referenceFile, queryFile1, target);
	}

	public MZIWarpInput(final List<Tuple2DI> path1,
	        final Tuple2D<List<Array>, List<Array>> tuple1,
	        final IFileFragment referenceFile, final IFileFragment queryFile1,
	        final IFileFragment targetFile1) {
		init(path1, tuple1, referenceFile, queryFile1, targetFile1);
	}

	public String getAlgorithm() {
		return "MZI";
	}

	public Tuple2D<List<Array>, List<Array>> getArrays() {
		return this.tuple;
	}

	public IFileFragment getFileFragment() {
		return this.targetFile;
	}

	public List<Tuple2DI> getPath() {
		return this.path;
	}

	public IFileFragment getQueryFileFragment() {
		return this.queryFile;
	}

	public IFileFragment getReferenceFileFragment() {
		return this.refFile;
	}

	protected void init(final List<Tuple2DI> path1,
	        final Tuple2D<List<Array>, List<Array>> tuple1,
	        final IFileFragment referenceFile, final IFileFragment queryFile1,
	        final IFileFragment targetFile1) {
		EvalTools.notNull(new Object[] { path1, targetFile1, referenceFile,
		        queryFile1, tuple1 }, this);
		this.path = path1;
		this.targetFile = targetFile1;
		this.refFile = referenceFile;
		this.queryFile = queryFile1;
		this.tuple = tuple1;
	}

}
