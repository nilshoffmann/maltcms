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
package cross.io.misc;

import cross.Factory;
import cross.cache.CacheType;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.Fragments;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tools.ArrayTools;
import cross.io.MockDatasource;
import cross.test.LogMethodName;
import cross.test.SetupLogging;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class ArrayChunkIteratorTest {
	@Rule
    public LogMethodName logMethodName = new LogMethodName();
    @Rule
    public SetupLogging logging = new SetupLogging();

    /**
     *
     */
    @Before
    public void setUp() {
        Factory.getInstance().getDataSourceFactory().setDataSources(Arrays.asList(MockDatasource.class.getCanonicalName()));
        Fragments.setDefaultFragmentCacheType(CacheType.NONE);
    }

    /**
     * Test of hasNext method, of class ArrayChunkIterator.
     */
    @Test
    public void testHasNext() {
        int[] lengths = new int[]{41,28,30};
        int[] chunksizes = new int[]{3,5,10,20};
        for (int i = 0; i < lengths.length; i++) {
            for (int j = 0; j < chunksizes.length; j++) {
                testChunkIterator(lengths[i], chunksizes[j]);
            }
        }
    }
    
    private void testChunkIterator(int length, int chunksize) {
        Array ref = ArrayTools.random(new Random(System.nanoTime()), double.class, new int[]{length});
        int activeChunkSize;
        List<Array> refChunks = new ArrayList<Array>();
        int mod = ref.getShape()[0] % chunksize;
        int chunks = (mod==0?0:1)+ (ref.getShape()[0]/chunksize);
        int offset = 0;
        for (int i = 0; i < chunks; i++) {
            activeChunkSize = Math.min(chunksize,length-offset);
            log.info("Creating chunk {} with size {}",i,chunksize);
            int lastIndex = Math.max(offset, offset+activeChunkSize-1);
            try {
                Range r = new Range(offset,lastIndex);
                Array chunkArray = ref.sectionNoReduce(Arrays.asList(r));
                refChunks.add(chunkArray);
            } catch (InvalidRangeException ex) {
                Logger.getLogger(ArrayChunkIteratorTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            offset+=activeChunkSize;
        }
        IFileFragment f = new FileFragment();
        IVariableFragment testVar = f.addChild("testVar");
        testVar.setArray(ref);
        ArrayChunkIterator aci = new ArrayChunkIterator(testVar, chunksize);//21 chunks, 20 of size 10 and one of size 5
        int idx = 0;
        Array reconstructedRef = Array.factory(DataType.getType(ref.getElementType()),ref.getShape());
        List<Array> arrayChunks = new ArrayList<Array>();
        while(aci.hasNext()) {
            Array chunk = aci.next();
            arrayChunks.add(chunk);
            log.info("RefChunks shape: {}, chunk shape: {}",Arrays.toString(refChunks.get(idx).getShape()),Arrays.toString(chunk.getShape()));
            Assert.assertTrue(Arrays.equals(refChunks.get(idx).getShape(), chunk.getShape()));
            Assert.assertTrue(Arrays.equals((double[])refChunks.get(idx).get1DJavaArray(double.class), (double[])chunk.get1DJavaArray(double.class)));
            idx++;
        }
        Assert.assertTrue(Arrays.equals((double[])ref.get1DJavaArray(double.class), (double[])ArrayTools.glue(arrayChunks).get1DJavaArray(double.class)));
    }

}
