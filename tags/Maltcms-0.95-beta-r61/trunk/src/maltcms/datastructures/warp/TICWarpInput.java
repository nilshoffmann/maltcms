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

package maltcms.datastructures.warp;

import java.util.List;

import maltcms.tools.MaltcmsTools;
import ucar.ma2.Array;
import cross.datastructures.fragments.FileFragmentFactory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.workflow.IWorkflow;
import cross.tools.FragmentTools;

/**
 * Specialization for TICs.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class TICWarpInput implements IWarpInput {
	private List<Tuple2DI> path = null;

	private IFileFragment targetFile = null;

	private IFileFragment refFile = null;

	private IFileFragment queryFile = null;

	private Tuple2D<List<Array>, List<Array>> tuple = null;

	public TICWarpInput(final IFileFragment ff, final IWorkflow iw) {
		this.path = MaltcmsTools.getWarpPath(ff);
		this.refFile = FragmentTools.getLHSFile(ff);
		this.queryFile = FragmentTools.getRHSFile(ff);
		this.targetFile = FileFragmentFactory.getInstance().createFragment(
		        this.refFile, this.queryFile, this.getClass(),
		        iw.getStartupDate());
		this.tuple = MaltcmsTools
		        .prepareInputArraysTICasList(new Tuple2D<IFileFragment, IFileFragment>(
		                this.refFile, this.queryFile));
	}

	public String getAlgorithm() {
		return "TIC";
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

}
