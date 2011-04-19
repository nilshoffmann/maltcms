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
 * $Id: NormalizationFilter.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */

package maltcms.commands.filters.array;

import maltcms.commands.scanners.ArrayStatsScanner;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import cross.Factory;
import cross.annotations.Configurable;
import cross.datastructures.StatsMap;
import cross.datastructures.tools.ArrayTools;
import cross.datastructures.tools.EvalTools;
import java.util.Arrays;
import java.util.List;
import org.slf4j.LoggerFactory;
import ucar.ma2.MAVector;

/**
 * Normalize all values of given arrays to the interval of 0..1, given min and
 * max values.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class NormalizationFilter extends AArrayFilter {

	@Configurable
	private String normalization = "Max-Min";

	@Configurable
	private boolean normalizeGlobal = false;

	@Configurable
	private boolean log = false;

	public NormalizationFilter() {
		super();
	}

	public NormalizationFilter(final String normalization1, final boolean log1,
	        final boolean global) {
		this();
		this.normalization = normalization1;
		this.log = log1;
		this.normalizeGlobal = global;
	}

	@Override
	public Array[] apply(final Array[] a) {
		final Array[] b = super.apply(a);
		final ArrayStatsScanner ass = Factory.getInstance().getObjectFactory()
		        .instantiate(ArrayStatsScanner.class);
		if (this.normalizeGlobal) {
			applyGlobalNormalization(b, ass, this.log);
		} else {
			for (final Array arr : b) {
				applyNormalization(arr, ass, this.log);
			}
		}
		return b;
	}

	protected void applyGlobalNormalization(final Array[] a,
	        final ArrayStatsScanner ass, final boolean log1) {
		ass.apply(a);
		final StatsMap sm = ass.getGlobalStatsMap();
		EvalTools.notNull(sm, "Global StatsMap is null", this);
		for (final Array arr : a) {
			normalize(arr, sm, this.normalization, this.log);
		}
	}

	protected void applyNormalization(final Array arr,
	        final ArrayStatsScanner ass, final boolean log1) {
		final StatsMap sm = ass.apply(new Array[] { arr })[0];
		normalize(arr, sm, this.normalization, this.log);
	}

	@Override
	public void configure(final Configuration cfg) {
		super.configure(cfg);
		this.normalization = cfg.getString(this.getClass().getName()
		        + ".normalization_command", "Max-Min");
		this.normalizeGlobal = cfg.getBoolean(this.getClass().getName()
		        + ".normalize_global", false);
		this.log = cfg.getBoolean(this.getClass().getName() + ".log", false);
	}

	public String getNormalization() {
		return this.normalization;
	}

	public boolean isLog() {
		return this.log;
	}

	public boolean isNormalizeGlobal() {
		return this.normalizeGlobal;
	}

	public void setLog(final boolean log1) {
		this.log = log1;
	}

	public void setNormalization(final String normalization1) {
		this.normalization = normalization1;
	}

	public void setNormalizeGlobal(final boolean normalizeGlobal1) {
		this.normalizeGlobal = normalizeGlobal1;
	}

        public static Array normalize(final Array a, final StatsMap sm,
	        final String normalization, final boolean log1) {
		final MultiplicationFilter mf = new MultiplicationFilter(
		        1.0d / (log1 ? (Math.log(1.0d + EvalTools.eval(normalization,
		                sm))) : (EvalTools.eval(normalization, sm))));
		return mf.apply(new Array[] { a })[0];
	}

        public static List<Array> normalize(final List<Array> al,
	        final String normalization) {
		final NormalizationFilter nf = Factory.getInstance().getObjectFactory()
		        .instantiate(NormalizationFilter.class);
		nf.setNormalization(normalization);
		nf.setNormalizeGlobal(false);
		nf.setLog(false);
		return Arrays.asList(nf.apply(al.toArray(new Array[] {})));
	}

	public static List<Array> normalizeGlobal(final List<Array> al) {
		final NormalizationFilter nf = Factory.getInstance().getObjectFactory()
		        .instantiate(NormalizationFilter.class);
		nf.setNormalization("Max-Min");
		nf.setNormalizeGlobal(true);
		nf.setLog(false);
		return Arrays.asList(nf.apply(al.toArray(new Array[] {})));
	}

	public static List<Array> normalizeLocalToUnitLength(final List<Array> al) {
		for (final Array a : al) {
			if (a.getRank() == 1) {
				final MAVector mav = new MAVector(a);
				mav.normalize();
			} else {
				LoggerFactory.getLogger(NormalizationFilter.class)
				        .error(
				                "Can not normalize a matrix to unit length, array has rank {}",
				                a.getRank());
			}
		}
		return al;
	}
}
