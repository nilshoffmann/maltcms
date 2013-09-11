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
package maltcms.commands.fragments.cluster;

import cross.commands.fragments.AFragmentCommand;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.cluster.BinaryCluster;
import org.openide.util.lookup.ServiceProvider;

/**
 * Work in progress. Implements CompleteLinkage clustering.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Slf4j
@ServiceProvider(service = AFragmentCommand.class)
public class CompleteLinkageAlgorithm extends ClusteringAlgorithm {

    @Override
    public double[] dmat(final int i, final int j, final int k) {
        final double[] dmat = new double[getNames().length];
        final int ni = getCluster(i).getSize();
        final int nj = getCluster(j).getSize();
        log.debug("ICluster " + getCluster(i).getID() + " has " + ni
                + (ni == 1 ? " child" : " children") + ", cluster "
                + getCluster(j).getID() + " has " + nj
                + (nj == 1 ? " child" : " children"));
        for (int m = 0; m < k; m++) {
            if (!getUsedIndices().contains(m) && !getUsedIndices().contains(k)
                    && (m != i) && (m != j)) {
                setd(k, m, isMinimizing() ? Math.max(d(i, m), d(j, m)) : Math
                        .min(d(i, m), d(j, m)));
                dmat[m] = d(Math.max(i, m), Math.min(j, m));
                log.info("Distance from cluster k={} to m={} ={}",
                        new Object[]{k, getCluster(m).getName(), dmat[m]});
            }
        }
        return dmat;
    }

    @Override
    public void findBestD(final int numclust) {
        int i = -1;
        int j = -1;
        double mind = isMinimizing() ? Double.NEGATIVE_INFINITY
                : Double.POSITIVE_INFINITY;
        for (int m = 0; m < numclust; m++) {
            for (int n = 0; n < numclust; n++) {
                if (n != m) {
                    if (!getUsedIndices().contains(m)
                            && !getUsedIndices().contains(n)) {
                        final double d = d(m, n);
                        if (isMinimizing()) {
                            if (d > mind) {
                                mind = d;
                                i = m;
                                j = n;
                            }
                        } else {
                            if (d < mind) {
                                mind = d;
                                i = m;
                                j = n;
                            }
                        }
                    }
                }
            }
        }
        if ((i >= 0) && (j >= 0)) {
            if (isMinimizing()) {
                log.debug("Found minimum distance " + d(i, j));
            } else {
                log.debug("Found maximum similarity " + d(i, j));
            }
            // between "+this.names[i]+" and "+this.names[j]);
            addNodeK(i, j, numclust);
        } else {
            throw new IllegalArgumentException("Could not find minimum!");
        }

    }

    @Override
    public void joinIJtoK(final int i, final int j, final int k,
            final double[] dist) {
        final double dij = d(i, j);
        final double dik = dij;
        final double djk = dik;
        final BinaryCluster njc = new BinaryCluster(getCluster(i),
                getCluster(j), dik, djk, dist, k);
        putCluster(k, njc);
        printDistanceToNewCluster(i, j, k);
        getUsedIndices().add(i);
        getUsedIndices().add(j);
        if (getFragments() != null) {
            handleFileFragments(i, j, k);
        }

    }
}
