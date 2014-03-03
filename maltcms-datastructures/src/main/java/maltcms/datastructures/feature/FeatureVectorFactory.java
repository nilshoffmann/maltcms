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
package maltcms.datastructures.feature;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import java.util.ArrayList;
import java.util.List;
import maltcms.datastructures.array.IFeatureVector;
import maltcms.datastructures.ms.Chromatogram1D;
import maltcms.datastructures.ms.IScan1D;
import maltcms.tools.MaltcmsTools;
import ucar.ma2.Array;

/**
 * Implementation of a Factory for some common FeatureVector types.
 *
 * @author Nils Hoffmann
 */
public class FeatureVectorFactory {

    private final static FeatureVectorFactory fvf = new FeatureVectorFactory();

    private FeatureVectorFactory() {
    }

    public static FeatureVectorFactory getInstance() {
        return FeatureVectorFactory.fvf;
    }

    public List<IFeatureVector> createMSFeatureVectorList(IFileFragment iff) {
        List<IFeatureVector> l = new ArrayList<IFeatureVector>();
        Chromatogram1D c = new Chromatogram1D(iff);
        for (IScan1D s : c) {
            l.add(s);
        }
        return l;
    }

    public List<IFeatureVector> createBinnedMSFeatureVectorList(
        IFileFragment iff, int start, int stop, boolean useFastFeatureVector) {
        List<IFeatureVector> l = new ArrayList<IFeatureVector>();
        int ns = Math.min(stop, MaltcmsTools.getNumberOfBinnedScans(iff));
//		System.out.println("Reading from " + start + " to " + stop);
        if (useFastFeatureVector) {
            for (int i = start; i < ns; i++) {
//				System.out.println("Reading scan " + i);
                IFeatureVector fbmf;
                fbmf = new FastBinnedMSFeatureVector(iff, i);
                l.add(fbmf);
            }
        } else {
            Tuple2D<List<Array>, List<Array>> t = MaltcmsTools
                .getBinnedMZIs(iff);
            for (int i = start; i < ns; i++) {
//				System.out.println("Reading scan " + i);
                IFeatureVector fbmf;
                fbmf = new DefaultBinnedMSFeatureVector(iff, i, t);
                l.add(fbmf);
            }
        }

        return l;
    }

    public List<IFeatureVector> createBinnedMSFeatureVectorList(
        IFileFragment iff, boolean useFastFeatureVector) {
        int ns = MaltcmsTools.getNumberOfBinnedScans(iff);
        return createBinnedMSFeatureVectorList(iff, 0, ns, useFastFeatureVector);
    }

    public List<IFeatureVector> createFeatureVectorList(List<Array> la) {
        List<IFeatureVector> l = new ArrayList<IFeatureVector>();
        int i = 0;
        for (Array a : la) {
            DefaultFeatureVector dfv = new DefaultFeatureVector();
            dfv.addFeature("FEATURE" + i, a);
            l.add(dfv);
        }
        return l;
    }

    public DefaultFeatureVector createFeatureVector(Array a, String featureName) {
        return addFeatureToFeatureVector(null, a, featureName);
    }

    public DefaultFeatureVector addFeatureToFeatureVector(
        DefaultFeatureVector ifv, Array a, String featureName) {
        DefaultFeatureVector dfv = ifv;
        if (ifv == null) {
            dfv = new DefaultFeatureVector();
        }
        dfv.addFeature(featureName, a);
        return dfv;
    }

    public List<DefaultFeatureVector> addFeaturesToFeatureVectorList(
        List<DefaultFeatureVector> l, List<Array> la, String featureName) {
        EvalTools.eqI(l.size(), la.size(), this);
        int i = 0;
        for (Array a : la) {
            DefaultFeatureVector dfv = l.get(i++);
            addFeatureToFeatureVector(dfv, a, featureName);
        }
        return l;
    }

    public List<IFeatureVector> createFeatureVectorList(List<Array> la,
        String featureName) {
        List<IFeatureVector> l = new ArrayList<IFeatureVector>();
        for (Array a : la) {
            DefaultFeatureVector dfv = createFeatureVector(a, featureName);
            l.add(dfv);
        }
        return l;
    }
}
