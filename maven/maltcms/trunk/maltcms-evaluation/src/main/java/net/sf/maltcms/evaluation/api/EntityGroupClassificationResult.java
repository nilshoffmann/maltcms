/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.maltcms.evaluation.api;

/**
 *
 * @author nilshoffmann
 */
public class EntityGroupClassificationResult implements Comparable<EntityGroupClassificationResult> {
    
    private final EntityGroup toolEntityGroup;
    private final EntityGroup groundTruthEntityGroup;
    
    private final int tp, tn, fp, fn;
    private final double dist;
    
    public EntityGroupClassificationResult(EntityGroup toolEntityGroup, EntityGroup groundTruthEntityGroup, int tp, int tn, int fp, int fn, double dist) {
        this.toolEntityGroup = toolEntityGroup;
        this.groundTruthEntityGroup = groundTruthEntityGroup;
        this.tp = tp;
        this.tn = tn;
        this.fp = fp;
        this.fn = fn;
        this.dist = dist;
    }

    public double getDist() {
        return dist;
    }

    public int getFn() {
        return fn;
    }

    public EntityGroup getGroundTruthEntityGroup() {
        return groundTruthEntityGroup;
    }

    public EntityGroup getToolEntityGroup() {
        return toolEntityGroup;
    }

    public int getFp() {
        return fp;
    }

    public int getTn() {
        return tn;
    }

    public int getTp() {
        return tp;
    }

    @Override
    public String toString() {
        return "EntityGroupClassificationResult{\n" + "toolEntityGroup=\n" + toolEntityGroup + ",\n groundTruthEntityGroup=\n" + groundTruthEntityGroup + ",\n tp=" + tp + ", tn=" + tn + ", fp=" + fp + ", fn=" + fn + ", dist=" + dist + '}';
    }

    /**
     * 
     * @param o
     * @throws IllegalArgumentException
     * @return 
     */
    @Override
    public int compareTo(EntityGroupClassificationResult o) throws IllegalArgumentException{
        final int WORSE = -1;
        final int BETTER = 1;
        final int EQUAL = 0;
        if(o.getGroundTruthEntityGroup().equals(this.groundTruthEntityGroup)) {
            if (tp > o.getTp()) {
                return BETTER;
            }else if (tp == o.getTp()) {
                if (tn > o.getTn()) {
                    return BETTER;
                } else if (tn == o.getTn()) {
                    if (fp < o.getFp()) {
                        return BETTER;
                    } else if (fp == o.getFp()) {
                        if (fn < o.getFn()) {
                            return BETTER;
                        } else if(fn == o.getFn()){
                            if (dist < o.getDist()) {
                                return BETTER;
                            }else if(dist>o.getDist()){
                                return WORSE;
                            }else{
                                return EQUAL;
                            }
                        } else {
                            return WORSE;
                        }
                    }
                }
            }
            return WORSE;
        }else{
            throw new IllegalArgumentException("Can not compare EntityGroupClassificationResults with different ground truth assignments!");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EntityGroupClassificationResult other = (EntityGroupClassificationResult) obj;
        if (this.toolEntityGroup != other.toolEntityGroup && (this.toolEntityGroup == null || !this.toolEntityGroup.equals(other.toolEntityGroup))) {
            return false;
        }
        if (this.groundTruthEntityGroup != other.groundTruthEntityGroup && (this.groundTruthEntityGroup == null || !this.groundTruthEntityGroup.equals(other.groundTruthEntityGroup))) {
            return false;
        }
        if (this.tp != other.tp) {
            return false;
        }
        if (this.tn != other.tn) {
            return false;
        }
        if (this.fp != other.fp) {
            return false;
        }
        if (this.fn != other.fn) {
            return false;
        }
        if (Double.doubleToLongBits(this.dist) != Double.doubleToLongBits(other.dist)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + (this.toolEntityGroup != null ? this.toolEntityGroup.hashCode() : 0);
        hash = 11 * hash + (this.groundTruthEntityGroup != null ? this.groundTruthEntityGroup.hashCode() : 0);
        hash = 11 * hash + this.tp;
        hash = 11 * hash + this.tn;
        hash = 11 * hash + this.fp;
        hash = 11 * hash + this.fn;
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.dist) ^ (Double.doubleToLongBits(this.dist) >>> 32));
        return hash;
    }

}
