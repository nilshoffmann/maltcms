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
package maltcms.commands.fragments.cluster;

import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import java.util.Iterator;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.distances.PairwiseFeatureSequenceSimilarity;
import maltcms.datastructures.cluster.BinaryCluster;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implements UPGMA Algorithm by ? (cite ...) FIXME add citation
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
@ServiceProvider(service = AFragmentCommand.class)
public class UPGMAAlgorithm extends ClusteringAlgorithm {

    public static void main(final String[] args) {
        // Examples from
        // http://evolution.genetics.washington.edu/phylip/doc/neighbor.html
        // double[][] dista = new double[5][5];
        // // Alpha 0.00000 1.00000 2.00000 3.00000 3.00000
        // // Beta 1.00000 0.00000 2.00000 3.00000 3.00000
        // // Gamma 2.00000 2.00000 0.00000 3.00000 3.00000
        // // Delta 3.00000 3.00000 3.00000 0.00000 1.00000
        // // Epsilon 3.00000 3.00000 3.00000 1.00000 0.00000
        // dista[0][0] = 0.0d;
        // dista[0][1] = 1.0d;
        // dista[0][2] = 2.0d;
        // dista[0][3] = 3.0d;
        // dista[0][4] = 3.0d;
        //
        // dista[1][0] = 1.0d;
        // dista[1][1] = 0.0d;
        // dista[1][2] = 2.0d;
        // dista[1][3] = 3.0d;
        // dista[1][4] = 3.0d;
        //
        // dista[2][0] = 2.0d;
        // dista[2][1] = 2.0d;
        // dista[2][2] = 0.0d;
        // dista[2][3] = 3.0d;
        // dista[2][4] = 3.0d;
        //
        // dista[3][0] = 3.0d;
        // dista[3][1] = 3.0d;
        // dista[3][2] = 3.0d;
        // dista[3][3] = 0.0d;
        // dista[3][4] = 1.0d;
        //
        // dista[4][0] = 3.0d;
        // dista[4][1] = 3.0d;
        // dista[4][2] = 3.0d;
        // dista[4][3] = 1.0d;
        // dista[4][4] = 0.0d;
        // double[][] dist = new double[7][7];
        // // Example data from
        // // http://evolution.genetics.washington.edu/phylip/doc/neighbor.html
        // // Bovine 0.00000 1.68660 1.71980 1.66060 1.52430 1.60430 1.59050
        // // Mouse 1.68660 0.00000 1.52320 1.48410 1.44650 1.43890 1.46290
        // // Gibbon 1.71980 1.52320 0.00000 0.71150 0.59580 0.61790 0.55830
        // // Orang 1.66060 1.48410 0.71150 0.00000 0.46310 0.50610 0.47100
        // // Gorilla 1.52430 1.44650 0.59580 0.46310 0.00000 0.34840 0.30830
        // // Chimp 1.60430 1.43890 0.61790 0.50610 0.34840 0.00000 0.26920
        // // Human 1.59050 1.46290 0.55830 0.47100 0.30830 0.26920 0.00000
        // // Bovine 0.0000 1.6866 1.7198 1.6606 1.5243 1.6043 1.5905
        // // Mouse 1.6866 0.0000 1.5232 1.4841 1.4465 1.4389 1.4629
        // // Gibbon 1.7198 1.5232 0.0000 0.7115 0.5958 0.6179 0.5583
        // // Orang 1.6606 1.4841 0.7115 0.0000 0.4631 0.5061 0.4710
        // // Gorilla 1.5243 1.4465 0.5958 0.4631 0.0000 0.3484 0.3083
        // // Chimp 1.6043 1.4389 0.6179 0.5061 0.3484 0.0000 0.2692
        // // Human 1.5905 1.4629 0.5583 0.4710 0.3083 0.2692 0.0000
        // // Bovine
        // dist[0][0] = 0.0d;
        // dist[0][1] = 1.6866d;
        // dist[0][2] = 1.7198d;
        // dist[0][3] = 1.6606d;
        // dist[0][4] = 1.5243d;
        // dist[0][5] = 1.6043d;
        // dist[0][6] = 1.5905d;
        // // Mouse
        // dist[1][0] = 1.6866d;
        // dist[1][1] = 0.0d;
        // dist[1][2] = 1.5232d;
        // dist[1][3] = 1.4841d;
        // dist[1][4] = 1.4465d;
        // dist[1][5] = 1.4389d;
        // dist[1][6] = 1.4629d;
        // // Gibbon
        // dist[2][0] = 1.7198d;
        // dist[2][1] = 1.5232d;
        // dist[2][2] = 0.0d;
        // dist[2][3] = 0.7115d;
        // dist[2][4] = 0.5958d;
        // dist[2][5] = 0.6179d;
        // dist[2][6] = 0.5583d;
        // // Orang
        // dist[3][0] = 1.6606d;
        // dist[3][1] = 1.4841d;
        // dist[3][2] = 0.7115d;
        // dist[3][3] = 0.0d;
        // dist[3][4] = 0.4631d;
        // dist[3][5] = 0.5061d;
        // dist[3][6] = 0.4710d;
        // // Gorilla
        // dist[4][0] = 1.5243d;
        // dist[4][1] = 1.4465d;
        // dist[4][2] = 0.5958d;
        // dist[4][3] = 0.4631d;
        // dist[4][4] = 0.0d;
        // dist[4][5] = 0.3484d;
        // dist[4][6] = 0.3083d;
        // // Chimp
        // dist[5][0] = 1.6043d;
        // dist[5][1] = 1.4389d;
        // dist[5][2] = 0.6179d;
        // dist[5][3] = 0.5061d;
        // dist[5][4] = 0.3484d;
        // dist[5][5] = 0.0d;
        // dist[5][6] = 0.2692d;
        // // Human
        // dist[6][0] = 1.5905d;
        // dist[6][1] = 1.4629d;
        // dist[6][2] = 0.5583d;
        // dist[6][3] = 0.4710d;
        // dist[6][4] = 0.3083d;
        // dist[6][5] = 0.2692d;
        // dist[6][6] = 0.0d;
        //
        // String[] names = new String[] { "1Bovine", "2Mouse", "3Gibbon",
        // "4Orang", "5Gorilla", "6Chimp", "7Human" };
        final double[][] dist = new double[5][5];
        dist[0][0] = 0;
        dist[0][1] = 24;
        dist[0][2] = 24;
        dist[0][3] = 24;
        dist[0][4] = 24;

        dist[1][0] = 24;
        dist[1][1] = 0;
        dist[1][2] = 16;
        dist[1][3] = 8;
        dist[1][4] = 8;

        dist[2][0] = 24;
        dist[2][1] = 16;
        dist[2][2] = 0;
        dist[2][3] = 16;
        dist[2][4] = 16;

        dist[3][0] = 24;
        dist[3][1] = 8;
        dist[3][2] = 16;
        dist[3][3] = 0;
        dist[3][4] = 4;

        dist[4][0] = 24;
        dist[4][1] = 8;
        dist[4][2] = 16;
        dist[4][3] = 4;
        dist[4][4] = 0;

        final String[] names = new String[]{"a", "b", "c", "d", "e"};
        final UPGMAAlgorithm u = new UPGMAAlgorithm(dist, names);
        u.merge();
        u.iterator();

        // nja = new NeighborJoinAlgorithm(dista,namesa);
        // nja.merge();
        // nja.iterator();
    }

    public UPGMAAlgorithm() {
        super();
    }

    public UPGMAAlgorithm(final double[][] distances, final String[] names) {
        init(distances, names, null);
    }

    public UPGMAAlgorithm(final double[][] distances,
        final TupleND<IFileFragment> fragments,
        final PairwiseFeatureSequenceSimilarity ld) {
        final String[] names = new String[fragments.getSize()];
        final Iterator<IFileFragment> iter = fragments.getIterator();
        int i = 0;
        while (iter.hasNext()) {
            names[i++] = iter.next().getName();
        }
        init(distances, names, fragments);

        setLDF(ld);
    }

    @Override
    public double[] dmat(final int i, final int j, final int k) {
        final double[] dmat = new double[getNames().length];
        final int ni = getCluster(i).getSize();
        final int nj = getCluster(j).getSize();
        log.debug("ICluster " + getCluster(i).getID() + " has " + ni
            + (ni == 1 ? " child" : " children") + ", cluster "
            + getCluster(j).getID() + " has " + nj
            + (nj == 1 ? " child" : " children"));
        final int sij = ni + nj;
        for (int m = 0; m < k; m++) {
            if (!getUsedIndices().contains(m) && !getUsedIndices().contains(k)
                && (m != i) && (m != j)) {
                setd(
                    k,
                    m,
                    (((double) ni / (double) sij) * d(i, m) + ((double) nj / (double) sij)
                    * d(j, m)));
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
        final double dik = dij / 2.0d;
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
