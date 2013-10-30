/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
package maltcms.commands.filters.array;

import cross.annotations.Configurable;
import lombok.Data;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;

/**
 * Multiply a value with all values of an array.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Data
@ServiceProvider(service = AArrayFilter.class)
public class MultiplicationFilter extends AArrayFilter {

	@Configurable
	private double factor = 1.0d;

	public MultiplicationFilter() {
		super();
	}

	public MultiplicationFilter(final double multiplyFactor) {
		this();
		this.factor = multiplyFactor;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see maltcms.ucar.ma2.ArrayFilter#filter(maltcms.ucar.ma2.Array)
	 */
	@Override
	public Array apply(final Array a) {
		final Array arr = super.apply(a);
		final IndexIterator ii = arr.getIndexIteratorFast();
		double next = 0.0d;
		while (ii.hasNext()) {
			next = ii.getDoubleNext();
			final double res = this.factor * next;
			ii.setDoubleCurrent(res);
		}
		return arr;
	}

	@Override
	public void configure(final Configuration cfg) {
		this.factor = cfg.getDouble(this.getClass().getName() + ".factor", 1.0d);
	}

	@Override
	public MultiplicationFilter copy() {
		return new MultiplicationFilter(factor);
	}
}
