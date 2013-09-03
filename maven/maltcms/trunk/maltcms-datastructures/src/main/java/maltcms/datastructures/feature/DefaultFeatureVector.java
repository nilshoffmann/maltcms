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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import maltcms.datastructures.array.IFeatureVector;
import ucar.ma2.Array;

/**
 * @author Nils Hoffmann
 *
 */
public class DefaultFeatureVector implements IFeatureVector {

    /**
     *
     */
    private static final long serialVersionUID = -2245293151270164895L;
    private Map<String, Integer> featureToIndex = null;
    private List<Array> datalist = null;
    private final UUID uniqueId;
	
	public DefaultFeatureVector() {
		this(UUID.randomUUID());
	}
	
	public DefaultFeatureVector(UUID uniqueId) {
		if(uniqueId == null) {
			throw new NullPointerException();
		}
		this.uniqueId = uniqueId;
	}
    
    private List<Array> getDataList() {
        if(this.datalist == null) {
            this.datalist = new ArrayList<Array>(1);
        }
        return this.datalist;
    }
    
    private Map<String,Integer> getFeatureToIndex() {
        if(this.featureToIndex==null) {
            this.featureToIndex = new HashMap<String, Integer>();
        }
        return this.featureToIndex;
    }
    
    @Override
    public Array getFeature(String name) {
        final int idx = getFeatureIndex(name);
        if (idx >= 0) {
            return getDataList().get(idx);
        }
        return null;
    }

    private int getFeatureIndex(String name) {
        if (featureToIndex.containsKey(name)) {
            return getFeatureToIndex().get(name).intValue();
        }
        return -1;
    }

    public void addFeature(String name, Array a) {
        final int idx = getFeatureIndex(name);
        if (idx >= 0) {
            getDataList().set(idx, a);
        } else {
            getFeatureToIndex().put(name, this.datalist.size());
            getDataList().add(a);
        }
    }

    @Override
    public List<String> getFeatureNames() {
        final List<String> l = new ArrayList<String>();
        l.addAll(getFeatureToIndex().keySet());
        Collections.sort(l);
        return l;
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }
    
}
