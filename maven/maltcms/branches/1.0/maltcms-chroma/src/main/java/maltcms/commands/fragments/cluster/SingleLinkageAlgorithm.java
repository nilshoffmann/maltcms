/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: SingleLinkageAlgorithm.java 43 2009-10-16 17:22:55Z nilshoffmann $
 */
package maltcms.commands.fragments.cluster;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.cluster.BinaryCluster;

/**
 * Work in progress. Implements SingleLinkage clustering.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Slf4j
@Data
public class SingleLinkageAlgorithm extends ClusteringAlgorithm {

    @Override
    public double[] dmat(final int i, final int j, final int k) {
        final double[] dmat = new double[getNames().length];
        final int ni = getCluster(i).getSize();
        final int nj = getCluster(j).getSize();
        this.log.debug("ICluster " + getCluster(i).getID() + " has " + ni
                + (ni == 1 ? " child" : " children") + ", cluster "
                + getCluster(j).getID() + " has " + nj
                + (nj == 1 ? " child" : " children"));
        for (int m = 0; m < k; m++) {
            if (!getUsedIndices().contains(m) && !getUsedIndices().contains(k)
                    && (m != i) && (m != j)) {
                setd(k, m, isMinimizing() ? Math.min(d(i, m), d(j, m)) : Math.
                        max(d(i, m), d(j, m)));
                dmat[m] = d(Math.max(i, m), Math.min(j, m));
                this.log.info("Distance from cluster k={} to m={} ={}",
                        new Object[]{k, getCluster(m).getName(), dmat[m]});
            }
        }
        return dmat;
    }

    @Override
    public void findBestD(final int numclust) {
        int i = -1;
        int j = -1;
        double mind = isMinimizing() ? Double.POSITIVE_INFINITY
                : Double.NEGATIVE_INFINITY;
        for (int m = 0; m < numclust; m++) {
            for (int n = 0; n < numclust; n++) {
                if (n != m) {
                    if (!getUsedIndices().contains(m)
                            && !getUsedIndices().contains(n)) {
                        final double d = d(m, n);
                        if (isMinimizing()) {
                            if (d < mind) {
                                mind = d;
                                i = m;
                                j = n;
                            }
                        } else {
                            if (d > mind) {
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
                this.log.debug("Found minimum distance " + d(i, j));
            } else {
                this.log.debug("Found maximum similarity " + d(i, j));
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
