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

package maltcms.datastructures.ms;

import java.util.Map;
import java.util.TreeMap;

import cross.Factory;
import cross.datastructures.fragments.IFileFragment;
import cross.tools.EvalTools;

/**
 * ArrayFactory to create Experiments.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class ExperimentFactory {

	private static Map<IFileFragment, IExperiment1D> exp1DCache = new TreeMap<IFileFragment, IExperiment1D>();

	private static Map<IFileFragment, IExperiment2D> exp2DCache = new TreeMap<IFileFragment, IExperiment2D>();

	private static boolean checkIn1DCache(final IFileFragment ff) {
		if (ExperimentFactory.exp1DCache.containsKey(ff)) {
			return true;
		}
		return false;
	}

	private static boolean checkIn2DCache(final IFileFragment ff) {
		if (ExperimentFactory.exp2DCache.containsKey(ff)) {
			return true;
		}
		return false;
	}

	/**
	 * Create an instance of Experiment1D, unless FileFragment is already
	 * registered, then return instance of either IExperiment1D or
	 * IExperiment2D.
	 * 
	 * @param ff
	 * @return
	 */
	public static IExperiment createExperiment(final IFileFragment ff) {
		if (ExperimentFactory.checkIn1DCache(ff)) {
			return ExperimentFactory.exp1DCache.get(ff);
		}
		if (ExperimentFactory.checkIn2DCache(ff)) {
			return ExperimentFactory.exp2DCache.get(ff);
		}
		return ExperimentFactory.createExperiment1D(ff);
	}

	/**
	 * Create an instance of IExperiment1D and initialize with IFileFragment.
	 * 
	 * @param ff
	 * @param c
	 * @return null if an Exception was caught, else new instance of IExperiment
	 */
	public static IExperiment1D createExperiment1D(final IFileFragment ff) {
		if (ExperimentFactory.checkIn1DCache(ff)) {
			return ExperimentFactory.exp1DCache.get(ff);
		}
		final Class<Experiment1D> c = Experiment1D.class;
		final IExperiment1D ie = Factory.getInstance().instantiate(c);
		EvalTools.notNull(ie, ExperimentFactory.class);
		ie.setFileFragment(ff);
		ExperimentFactory.exp1DCache.put(ff, ie);
		return ie;
	}

	/**
	 * Create an instance of IExperiment2D and initialize with IFileFragment.
	 * 
	 * @param ff
	 * @param c
	 * @return null if an Exception was caught, else new instance of IExperiment
	 */
	public static IExperiment2D createExperiment2D(final IFileFragment ff) {
		if (ExperimentFactory.checkIn2DCache(ff)) {
			return ExperimentFactory.exp2DCache.get(ff);
		}
		final Class<Experiment2D> c = Experiment2D.class;
		final IExperiment2D ie = Factory.getInstance().instantiate(c);
		EvalTools.notNull(ie, ie);
		ie.setFileFragment(ff);
		ExperimentFactory.exp2DCache.put(ff, ie);
		return ie;
	}

}
