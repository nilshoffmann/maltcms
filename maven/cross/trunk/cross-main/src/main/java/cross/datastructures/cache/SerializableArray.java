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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import ucar.ma2.Array;
import ucar.ma2.DataType;

/**
 *
 * Serialization proxy for {@link ucar.ma2.Array} instances.
 * 
 * @author Nils Hoffmann
 */
@Data
public class SerializableArray implements Externalizable {
    
    @Setter(AccessLevel.NONE)
    private Array array;

    public SerializableArray() {
    }
    
    public SerializableArray(Array array) {
        this.array = array;
    }

    @Override
    public void writeExternal(ObjectOutput oo) throws IOException {
        if(array!=null) {
            oo.writeObject(DataType.getType(array.getElementType()));
            oo.writeObject(array.getShape());
            oo.writeObject(array.getStorage());
        }
    }

    @Override
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
        this.array = Array.factory((DataType)oi.readObject(), (int[])oi.readObject(), oi.readObject());
    }

}
