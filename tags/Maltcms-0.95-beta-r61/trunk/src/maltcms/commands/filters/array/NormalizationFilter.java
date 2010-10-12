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

package maltcms.commands.filters.array;

import maltcms.commands.scanners.ArrayStatsScanner;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import cross.Factory;
import cross.datastructures.StatsMap;
import cross.tools.ArrayTools;
import cross.tools.EvalTools;

/**
 * Normalize all values of an array given a normalization string.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class NormalizationFilter extends AArrayFilter {

	private String normalization = "Max-Min";

	private boolean normalizeGlobal = false;

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
		final ArrayStatsScanner ass = Factory.getInstance().instantiate(
		        ArrayStatsScanner.class);
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
			ArrayTools.normalize(arr, sm, this.normalization, this.log);
		}
	}

	protected void applyNormalization(final Array arr,
	        final ArrayStatsScanner ass, final boolean log1) {
		final StatsMap sm = ass.apply(new Array[] { arr })[0];
		ArrayTools.normalize(arr, sm, this.normalization, this.log);
	}

	@Override
	public void configure(final Configuration cfg) {
		super.configure(cfg);
		this.normalization = cfg.getString(this.getClass().getName()
		        + ".normalization_command", "Max-Min");
		this.normalizeGlobal = cfg.getBoolean(this.getClass().getName()
		        + ".normalize_global", false);
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

}
