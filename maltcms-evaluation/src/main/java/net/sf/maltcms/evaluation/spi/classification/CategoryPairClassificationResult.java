/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
package net.sf.maltcms.evaluation.spi.classification;

import net.sf.maltcms.evaluation.api.classification.Category;

/**
 *
 * Holds classification results for pairs of aligned features from two
 * categories.
 * 
 * @author Nils Hoffmann
 */
public class CategoryPairClassificationResult implements Comparable<CategoryPairClassificationResult> {
	private final Category lhs;
	private final Category rhs;
	private final int tp, tn, fp, fn;
    private final double dist;
	
	public CategoryPairClassificationResult(Category lhs, Category rhs, int tp, int tn, int fp, int fn, double dist) {
        this.lhs = lhs;
        this.rhs = rhs;
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

    public Category getRightHandSideCategory() {
        return rhs;
    }

    public Category getLeftHandSideCategory() {
        return lhs;
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
        return "CategoryPairClassificationResult{\n" + "lhs=\n" + lhs + ",\n rhs=\n" + rhs + ",\n tp=" + tp + ", tn=" + tn + ", fp=" + fp + ", fn=" + fn + ", dist=" + dist + '}';
    }

    /**
     *
     * @param o
     * @throws IllegalArgumentException
     * @return
     */
    @Override
    public int compareTo(CategoryPairClassificationResult o) throws IllegalArgumentException {
        final int WORSE = -1;
        final int BETTER = 1;
        final int EQUAL = 0;
        if (o.getLeftHandSideCategory().equals(this.lhs)) {
            if (tp > o.getTp()) {
                return BETTER;
            } else if (tp == o.getTp()) {
                if (tn > o.getTn()) {
                    return BETTER;
                } else if (tn == o.getTn()) {
                    if (fp < o.getFp()) {
                        return BETTER;
                    } else if (fp == o.getFp()) {
                        if (fn < o.getFn()) {
                            return BETTER;
                        } else if (fn == o.getFn()) {
                            if (dist < o.getDist()) {
                                return BETTER;
                            } else if (dist > o.getDist()) {
                                return WORSE;
                            } else {
                                return EQUAL;
                            }
                        } else {
                            return WORSE;
                        }
                    }
                }
            }
            return WORSE;
        } else {
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
        final CategoryPairClassificationResult other = (CategoryPairClassificationResult) obj;
        if (this.lhs != other.lhs && (this.lhs == null || !this.lhs.equals(other.lhs))) {
            return false;
        }
        if (this.rhs != other.rhs && (this.rhs == null || !this.rhs.equals(other.rhs))) {
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
        hash = 11 * hash + (this.lhs != null ? this.lhs.hashCode() : 0);
        hash = 11 * hash + (this.rhs != null ? this.rhs.hashCode() : 0);
        hash = 11 * hash + this.tp;
        hash = 11 * hash + this.tn;
        hash = 11 * hash + this.fp;
        hash = 11 * hash + this.fn;
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.dist) ^ (Double.doubleToLongBits(this.dist) >>> 32));
        return hash;
    }
}
