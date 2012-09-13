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

import java.util.ArrayList;
import java.util.List;

import maltcms.datastructures.array.IFeatureVector;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;

/**
 * FeatureVector implementation, which directly accesses the IFileFragment
 * required at construction time.
 *
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 */
public class MaltcmsFeatureVector implements IFeatureVector {

    /**
     *
     */
    private static final long serialVersionUID = 1423070246312970401L;
    private IFileFragment iff = null;
    private int index = -1;

    public void addFeatures(IFileFragment iff, int i) {
        this.iff = iff;
        this.index = i;
    }

    @Override
    public Array getFeature(String name) {
        if (this.iff.getChild(name).getIndex() == null) {
            Array a = this.iff.getChild(name).getArray();
            try {
                return a.section(new int[]{this.index},
                        new int[]{this.index});
            } catch (InvalidRangeException ex) {
                System.err.println(ex.getLocalizedMessage());
            }
        }
        return this.iff.getChild(name).getIndexedArray().get(this.index);
    }

    @Override
    public List<String> getFeatureNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (IVariableFragment ivf : this.iff) {
            names.add(ivf.getName());
        }
        return names;
    }
}
