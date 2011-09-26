/*
 * $license$
 *
 * $Id$
 */

package cross.datastructures;

import java.util.HashMap;
import java.util.Map;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IFragment;

/**
 * StatsMap stores values corresponding to Vars, associated to a given Fragment
 * (not mandatory).
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
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
     * @param label
     *            the label to set
     */
    public void setLabel(final String label1) {
        this.label = label1;
    }
}
