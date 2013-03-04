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
package cross.datastructures.cache;

import cross.datastructures.cache.SerializableArray;
import cross.test.SetupLogging;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;
import org.junit.Rule;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class SerializableArrayTest {
    
    @Rule
    public SetupLogging logging = new SetupLogging();

    /**
     * Test of readWriteExternal method, of class SerializableArray.
     */
    @Test
    public void testReadWriteExternal() throws Exception {
        DataType[] types = new DataType[]{DataType.BOOLEAN, DataType.BYTE, 
            DataType.CHAR, DataType.DOUBLE, DataType.FLOAT, DataType.INT,
            DataType.LONG, DataType.SHORT, DataType.STRING};
        
        List<int[]> shapes = Arrays.asList(new int[]{10},new int[]{5,39},new int[]{8,21,4},new int[]{87,221,3,23});
        for(DataType dt:types) {
            log.info("Checking arrays of type {}",dt);
            for(int[] shape:shapes) {
                Array a = createArray(dt,shape);
                SerializableArray sa = writeAndRestore(new SerializableArray(a));
                checkArraysEqual(a, sa.getArray());
            }
        }
    }
    
    public SerializableArray writeAndRestore(SerializableArray sa) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        sa.writeExternal(oos);
        oos.close();
        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        sa.readExternal(ois);
        return sa;
    }

    public Array createArray(DataType dt, int[] shape) {
        Array a = Array.factory(dt, shape);
        IndexIterator ii = a.getIndexIterator();
        while (ii.hasNext()) {
            Object obj = null;
            switch (dt) {
                case BOOLEAN:
                    obj = ((Math.random() - 0.5) > 0 ? true : false);
                    break;
                case BYTE:
                    obj = ((int) (Math.random() * 255));
                    break;
                case CHAR:
                    obj = ((char) (Math.random() * 255));
                    break;
                case DOUBLE:
                    obj = ((double) (Math.random() * Double.MAX_VALUE));
                    break;
                case FLOAT:
                    obj = ((float) (Math.random() * Float.MAX_VALUE));
                    break;
                case INT:
                    obj = ((int) (Math.random() * Integer.MAX_VALUE));
                    break;
                case LONG:
                    obj = ((long) (Math.random() * Long.MAX_VALUE));
                    break;
                case SHORT:
                    obj = ((short) (Math.random() * Short.MAX_VALUE));
                    break;
                case STRING:
                    obj = "a"+((char) (Math.random() * 255));
                    break;
            }
            ii.setObjectNext(obj);
        }
        return a;
    }

    public void checkArraysEqual(Array a, Array b) {
        Assert.assertEquals(a.getShape()[0], b.getShape()[0]);
        Assert.assertEquals(a.getElementType(), b.getElementType());
        IndexIterator itera = a.getIndexIterator();
        IndexIterator iterb = b.getIndexIterator();
        while (itera.hasNext() && iterb.hasNext()) {
            Assert.assertEquals(itera.getObjectNext(), iterb.getObjectNext());
        }
    }
}
