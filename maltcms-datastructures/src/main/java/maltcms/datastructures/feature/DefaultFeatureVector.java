/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
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

import cross.datastructures.cache.SerializableArray;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import maltcms.datastructures.array.IFeatureVector;
import ucar.ma2.Array;


/**
 * <p>DefaultFeatureVector class.</p>
 *
 * @author hoffmann
 * 
 */
public class DefaultFeatureVector implements IFeatureVector {

    /**
     *
     */
    private static final long serialVersionUID = -2245293151270164895L;
    private Map<String, Integer> featureToIndex = null;
    private transient List<Array> datalist = null;
    private UUID uniqueId;

    /**
     * <p>Constructor for DefaultFeatureVector.</p>
     */
    public DefaultFeatureVector() {
        this(UUID.randomUUID());
    }

    /**
     * <p>Constructor for DefaultFeatureVector.</p>
     *
     * @param uniqueId a {@link java.util.UUID} object.
     */
    public DefaultFeatureVector(UUID uniqueId) {
        if (uniqueId == null) {
            throw new NullPointerException();
        }
        this.uniqueId = uniqueId;
    }

    private List<Array> getDataList() {
        if (this.datalist == null) {
            this.datalist = new ArrayList<>(1);
        }
        return this.datalist;
    }

    private Map<String, Integer> getFeatureToIndex() {
        if (this.featureToIndex == null) {
            this.featureToIndex = new HashMap<>();
        }
        return this.featureToIndex;
    }

    /** {@inheritDoc} */
    @Override
    public Array getFeature(String name) {
        final int idx = getFeatureIndex(name);
        if (idx >= 0) {
            return getDataList().get(idx);
        }
        return null;
    }

    private int getFeatureIndex(String name) {
        if (getFeatureToIndex().containsKey(name)) {
            return getFeatureToIndex().get(name);
        }
        return -1;
    }

    /**
     * <p>addFeature.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param a a {@link ucar.ma2.Array} object.
     */
    public void addFeature(String name, Array a) {
        final int idx = getFeatureIndex(name);
        if (idx >= 0) {
            getDataList().set(idx, a);
        } else {
            getFeatureToIndex().put(name, getDataList().size());
            getDataList().add(a);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getFeatureNames() {
        final List<String> l = new ArrayList<>();
        l.addAll(getFeatureToIndex().keySet());
        Collections.sort(l);
        return l;
    }

    /** {@inheritDoc} */
    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        getDataList();
        getFeatureToIndex();
        out.writeObject(uniqueId);
        out.writeInt(featureToIndex.keySet().size());
        String[] keys = featureToIndex.keySet().toArray(new String[featureToIndex.keySet().size()]);
        Arrays.sort(keys);
        for(String key:keys) {
            out.writeUTF(key);
            out.writeObject(new SerializableArray(datalist.get(featureToIndex.get(key))));
        }
    }

     private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.uniqueId = (UUID)in.readObject();
        int records = in.readInt();
        this.featureToIndex = new HashMap<>();
        this.datalist = new ArrayList<>(records); 
        for (int i = 0; i< records; i++) {
            String key = in.readUTF();
            SerializableArray sa = (SerializableArray)in.readObject();
            addFeature(key, sa.getArray());
        }
     }
}
