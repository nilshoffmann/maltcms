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
package net.sf.maltcms.evaluation.api.classification;

/**
 * @author Nils Hoffmann
 *
 *
 */
public class PeakNameFeatureVectorComparator implements IFeatureVectorComparator<INamedPeakFeatureVector> {

	/* (non-Javadoc)
	 * @see maltcms.experimental.eval.IFeatureVectorComparator#isFN(maltcms.datastructures.array.IFeatureVector, maltcms.datastructures.array.IFeatureVector)
	 */
	@Override
	public boolean isFN(INamedPeakFeatureVector gt, INamedPeakFeatureVector test) {
		final String gtName = gt.getName();
		final String testName = test.getName();
		if (gtName != null && testName == null) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see maltcms.experimental.eval.IFeatureVectorComparator#isFP(maltcms.datastructures.array.IFeatureVector, maltcms.datastructures.array.IFeatureVector)
	 */
	@Override
	public boolean isFP(INamedPeakFeatureVector gt, INamedPeakFeatureVector test) {
		final String gtName = gt.getName();
		final String testName = test.getName();
		//if gt is NaN, but test has a value, if FP=true
		if (gtName == null && testName != null) {
			return true;
		}
		if (gtName != null && testName!=null && !gtName.equals(testName)) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see maltcms.experimental.eval.IFeatureVectorComparator#isTN(maltcms.datastructures.array.IFeatureVector, maltcms.datastructures.array.IFeatureVector)
	 */
	@Override
	public boolean isTN(INamedPeakFeatureVector gt, INamedPeakFeatureVector test) {
		final String gtName = gt.getName();
		final String testName = test.getName();
		//both are NaNs
		if (gtName == null && testName == null) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see maltcms.experimental.eval.IFeatureVectorComparator#isTP(maltcms.datastructures.array.IFeatureVector, maltcms.datastructures.array.IFeatureVector)
	 */
	@Override
	public boolean isTP(INamedPeakFeatureVector gt, INamedPeakFeatureVector test) {
		final String gtName = gt.getName();
		final String testName = test.getName();
		//if either is a NaN or both, TP=false
		if(gtName==null || testName==null) {
			return false;
		}
		if (gtName.equals(testName)) {
			return true;
		}
		return false;
	}

	@Override
	public double getSquaredDiff(INamedPeakFeatureVector gt, INamedPeakFeatureVector test) {
		return Double.NaN;
	}
}
