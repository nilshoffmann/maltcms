/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package maltcms.tools;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import cross.annotations.NoFeature;

public class PublicMemberGetters<T> {

    protected HashMap<String, Method> hm;

    public PublicMemberGetters(Class<?> c) {
        this(c, new String[]{});
    }

    public PublicMemberGetters(Class<?> c, String... suffixesToExclude) {

        hm = new HashMap<String, Method>();
        Method[] m = c.getMethods();
        for (Method method : m) {
            if (!method.isAnnotationPresent(NoFeature.class)) {
                if (method.getName().startsWith("get")
                        && !method.getName().equals("getClass")) {
                    // look for method name (after get) in suffixes to exclude
                    int idx = Arrays.binarySearch(suffixesToExclude, method.
                            getName().substring(3));
                    // if idx < 0, suffix is not contained, so add method to
                    // pool
                    if (idx < 0) {
                        hm.put(method.getName().substring(3), method);
                    }
                }
            }
        }
    }

    public PublicMemberGetters(T t) {
        this(t.getClass());
    }

    public Method getMethodForGetterName(String s) {
        String name = s;
        if (name.startsWith("get")) {
            name = name.substring(3);
        }
        if (hm.containsKey(name)) {
            return hm.get(name);
        } else {
            return null;
        }
    }

    public String[] getGetterNames(String[] s) {
        ArrayList<String> al = new ArrayList<String>(s.length);
        for (String method : s) {
            if (getMethodForGetterName(method) != null) {
                al.add(method);
            }
        }
        return al.toArray(new String[al.size()]);
    }

    public String[] getGetterNames() {
        String[] names = new String[hm.size()];
        int i = 0;
        for (String s : hm.keySet()) {
            names[i] = s;
            i++;
        }
        Arrays.sort(names);
        return names;
    }
}
