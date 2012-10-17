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
package cross.datastructures;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IFragment;
import java.util.HashMap;
import java.util.Map;

/**
 * StatsMap stores values corresponding to Vars, associated to a given Fragment
 * (not mandatory).
 *
 * @author Nils Hoffmann
 *
 */
public class StatsMap extends HashMap<String, Double> {

    /**
     *
     */
    private static final long serialVersionUID = 2231266409133039440L;
    private IFragment association = null;
    private String label = null;

    /**
     *
     */
    public StatsMap() {
        super();
    }

    public StatsMap(final IFragment association1) {
        this();
        this.association = association1;
    }

    /**
     * @param arg0
     */
    protected StatsMap(final int arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     */
    protected StatsMap(final int arg0, final float arg1) {
        super(arg0, arg1);
    }

    /**
     * @param arg0
     */
    protected StatsMap(final Map<? extends String, ? extends Double> arg0) {
        super(arg0);
    }

    public IFragment getAssociation() {
        return this.association;
    }

    public void setAssociation(IFileFragment f) {
        this.association = f;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(final String label1) {
        this.label = label1;
    }
}
