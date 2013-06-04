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
package cross.annotations;

import java.util.Arrays;
import java.util.List;
import lombok.Data;

/**
 *
 * @author Nils Hoffmann
 */
@RequiresVariables(names = {"a", "b", "c"})
@ProvidesVariables(names = {"b", "d"})
@RequiresOptionalVariables(names = {"e"})
@Data
public class AnnotatedObject {

    @Configurable(type = int.class, description = "field1 for testing", name = "myField1", value = "10")
    private int field1 = 10;
    @Configurable(description = "field2 for testing")
    private int field2 = 12;
    @Configurable(value = "field3value")
    private String field3;
    private Long field4 = 1l;

    public static final List<String> getRequiredVariables() {
        return Arrays.asList("a", "b", "c");
    }

    public static final List<String> getOptionalRequiredVariables() {
        return Arrays.asList("e");
    }

    public static final List<String> getProvidedVariables() {
        return Arrays.asList("b", "d");
    }

    public static final List<String> getRequiredConfigKeys() {
        return Arrays.asList("cross.annotations.AnnotatedObject.myField1", "cross.annotations.AnnotatedObject.field2", "cross.annotations.AnnotatedObject.field3");
    }
}
