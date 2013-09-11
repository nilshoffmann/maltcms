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

import static org.junit.Assert.*;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.MAMath;

/**
 *
 * @author Nils Hoffmann
 */
public class BatchFilterTest {

	@Test
	public void testNormalization() {
		Array intensities = Array.factory(new float[]{0, 200, 3023, 214, 97324, 977213, 23, 325});
		MAMath.MinMax mmi = MAMath.getMinMax(intensities);
		MultiplicationFilter mf1 = new MultiplicationFilter(999.0/(mmi.max-mmi.min));
		Array normalizedIntensities = mf1.apply(intensities);
		MAMath.MinMax mm = MAMath.getMinMax(normalizedIntensities);
		assertEquals(0.0d,mm.min,10e-6);
		assertEquals(999.0d,mm.max,10e-6);
	}
}