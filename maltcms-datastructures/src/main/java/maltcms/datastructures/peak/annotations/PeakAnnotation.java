/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
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
package maltcms.datastructures.peak.annotations;

import java.io.Serializable;
import lombok.Data;
import lombok.Builder;
import maltcms.datastructures.ms.IMetabolite;

/**
 * <p>
 * PeakAnnotation class.</p>
 *
 * @author Nils Hoffmann
 *
 */
public class PeakAnnotation implements Serializable, Comparable {

    private final double score;
    private final String database;
    private final String similarityFunction;
    private final IMetabolite metabolite;

    public PeakAnnotation(double score, String database, String similarityFunction, IMetabolite metabolite) {
        this.score = score;
        this.database = database;
        this.similarityFunction = similarityFunction;
        this.metabolite = metabolite;
    }

    public double getScore() {
        return score;
    }

    public String getDatabase() {
        return database;
    }

    public String getSimilarityFunction() {
        return similarityFunction;
    }

    public IMetabolite getMetabolite() {
        return metabolite;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PeakAnnotation{");
        sb.append("score=").append(score);
        sb.append(", database=").append(database);
        sb.append(", similarityFunction=").append(similarityFunction);
        sb.append(", metabolite=").append(metabolite);
        sb.append('}');
        return sb.toString();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Object t) {
        if (t instanceof PeakAnnotation) {
            return Double.compare(score, ((PeakAnnotation) t).getScore());
        }
        return 0;
    }
}
