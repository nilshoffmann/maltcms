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

package maltcms.commands.fragments.io;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.io.csv.CSVWriter;
import cross.tools.FileTools;
import cross.tools.StringTools;

/**
 * Writes 1D-variables to csv files, one entry per row.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class VariableDataExporter extends AFragmentCommand {

	private ArrayList<String> varNames = new ArrayList<String>(0);

	@Override
	public String getDescription() {
		return "Exports one-dimensional variables to csv format ";
	}

	@Override
	public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
		CSVWriter csvw = new CSVWriter();
		csvw.setIWorkflow(getIWorkflow());
		for(IFileFragment iff:t) {
			File path = FileTools.prependDefaultDirs(this.getClass(), getIWorkflow().getStartupDate());
			for(String s:this.varNames) {
				Array a = iff.getChild(s).getArray();
				csvw.writeArray(path.getAbsolutePath(), StringTools.removeFileExt(iff.getName())+"_"+s, a);
			}
		}
		return t;
	}

	/* (non-Javadoc)
	 * @see cross.commands.fragments.AFragmentCommand#configure(org.apache.commons.configuration.Configuration)
	 */
	@Override
	public void configure(Configuration cfg) {
		super.configure(cfg);
		this.varNames  = StringTools.toStringList(cfg.getList(this.getClass().getName()+".variables"));
	}

	@Override
	public WorkflowSlot getWorkflowSlot() {
		return WorkflowSlot.FILEIO;
	}

}
