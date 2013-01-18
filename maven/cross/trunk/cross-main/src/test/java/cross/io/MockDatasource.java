/*
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.io;

import cross.cache.ICacheDelegate;
import cross.datastructures.fragments.Fragments;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tools.ArrayTools;
import cross.exception.ResourceNotAvailableException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */
public class MockDatasource implements IDataSource {

    Map<IFileFragment,ICacheDelegate<IVariableFragment,List<Array>>> persistentCache = new HashMap<IFileFragment,ICacheDelegate<IVariableFragment,List<Array>>>();
    
    @Override
    public int canRead(IFileFragment ff) {
        return 1;
    }

    private ICacheDelegate<IVariableFragment,List<Array>> getCache(IFileFragment f) {
        if(persistentCache.containsKey(f)) {
            return persistentCache.get(f);
        }
        ICacheDelegate<IVariableFragment,List<Array>> delegate = Fragments.createFragmentCache(UUID.randomUUID().toString());
        persistentCache.put(f, delegate);
        return delegate;
    }
    
    @Override
    public ArrayList<Array> readAll(IFileFragment f) throws IOException, ResourceNotAvailableException {
        ArrayList<Array> al = new ArrayList<Array>();
        for(IVariableFragment frag:f) {
            al.add(ArrayTools.glue(getCache(f).get(frag)));
        }
        return al;
    }

    @Override
    public ArrayList<Array> readIndexed(IVariableFragment f) throws IOException, ResourceNotAvailableException {
        List<Array> l = getCache(f.getParent()).get(f);
        if(f.getIndex()==null) {
            throw new IllegalStateException("Variable Fragment has no index variable set!");
        }
        if(l== null || l.isEmpty()) {
            throw new ResourceNotAvailableException("Could not read indexed arrays for "+f);
        }
        return new ArrayList<Array>(l);
    }

    @Override
    public Array readSingle(IVariableFragment f) throws IOException, ResourceNotAvailableException {
        List<Array> l = getCache(f.getParent()).get(f);
        if(l== null || l.isEmpty()) {
            throw new ResourceNotAvailableException("Could not read indexed arrays for "+f);
        }
        if(l.size()==1) {
            return l.get(0);
        }else{
            return ArrayTools.glue(l);
        }
    }

    @Override
    public ArrayList<IVariableFragment> readStructure(IFileFragment f) throws IOException {
        return new ArrayList<IVariableFragment>(f.getImmediateChildren());
    }

    @Override
    public IVariableFragment readStructure(IVariableFragment f) throws IOException, ResourceNotAvailableException {
//        if(getCache(f.getParent())!=null) {
//            return f;
//        }
        throw new ResourceNotAvailableException("Variable "+f.getName()+" does not exist on file!");
    }

    @Override
    public List<String> supportedFormats() {
        return Arrays.asList("nc", "nc.gz", "nc.z", "nc.zip", "nc.gzip", "nc.bz2", "cdf", "cdf.gz", "cdf.z", "cdf.zip", "cdf.gzip", "cdf.bz2");
    }

    @Override
    public boolean write(IFileFragment f) {
        for(IVariableFragment v:f.getImmediateChildren()) {
            if(v.getIndex()!=null) {
                getCache(f).put(v, v.getIndexedArray());
            }else{
                getCache(f).put(v, new ArrayList<Array>(Arrays.asList(v.getArray())));
            }
        }
        return true;
    }

    @Override
    public void configure(Configuration cfg) {
        
    }

    @Override
    public void configurationChanged(ConfigurationEvent ce) {
        
    }
    
}
