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
 * $Id: NeighborJoinAlgorithm.java 73 2009-12-16 08:45:14Z nilshoffmann $
 */
package maltcms.commands.fragments.cluster;

import java.util.Iterator;

import maltcms.commands.distances.ListDistanceFunction;
import maltcms.datastructures.cluster.BinaryCluster;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of NeighborJoining by Saitou and Nei (cite...) FIXME add
 * citation
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Slf4j
public class NeighborJoinAlgorithm extends ClusteringAlgorithm {

    public static void main(final String[] args) {
        // Examples from
        // http://evolution.genetics.washington.edu/phylip/doc/neighbor.html
        // double[][] dista = new double[5][5];
        // Alpha 0.00000 1.00000 2.00000 3.00000 3.00000
        // Beta 1.00000 0.00000 2.00000 3.00000 3.00000
        // Gamma 2.00000 2.00000 0.00000 3.00000 3.00000
        // Delta 3.00000 3.00000 3.00000 0.00000 1.00000
        // Epsilon 3.00000 3.00000 3.00000 1.00000 0.00000
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
        final NeighborJoinAlgorithm nja = new NeighborJoinAlgorithm(dist, names);
        nja.merge();
        nja.iterator();

    }
    private double[] R = null;

    public NeighborJoinAlgorithm() {
        super();
    }

    public NeighborJoinAlgorithm(final double[][] distances,
            final String[] names) {
        init(distances, names, null);
    }

    public NeighborJoinAlgorithm(final double[][] distances,
            final TupleND<IFileFragment> fragments,
            final ListDistanceFunction ld) {
        final String[] names = new String[fragments.getSize()];
        final Iterator<IFileFragment> iter = fragments.getIterator();
        int i = 0;
        while (iter.hasNext()) {
            names[i++] = iter.next().getName();
        }
        init(distances, names, fragments);
        setLDF(ld);
    }

    public void calcR(final int numclust) {
        for (int i = 0; i < numclust; i++) {
            double sum = 0.0d;
            for (int j = 0; j < numclust; j++) {
                if (!getUsedIndices().contains(j)
                        && !getUsedIndices().contains(i)) {
                    sum += d(i, j);
                }
            }
            if (!getUsedIndices().contains(i)) {
                final double div = ((getL()) - 2.0d);
                this.R[i] = div == 0.0d ? 0.0d : sum / div;
            }
        }
    }

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
                dmat[m] = (d(i, m) + d(j, m) - d(i, j)) / 2.0d;
                this.log.info("Distance from cluster k={} to m={} ={}",
                        new Object[]{k, getCluster(m).getName(), dmat[m]});
            }
        }
        return dmat;
    }

    @Override
    public void findBestD(final int numclust) {
        calcR(numclust);
        int i = -1;
        int j = -1;
        double mind = Double.POSITIVE_INFINITY;
        for (int m = 0; m < numclust; m++) {
            for (int n = 0; n < numclust; n++) {
                if (n != m) {
                    if (!getUsedIndices().contains(m)
                            && !getUsedIndices().contains(n)) {
                        final double d = d(m, n) - (this.R[m] + this.R[n]);
                        if (d < mind) {
                            mind = d;
                            i = m;
                            j = n;
                        }
                    }
                }
            }
        }
        if ((i >= 0) && (j >= 0)) {
            if (isMinimizing()) {
                log.debug("Found minimum distance " + d(i, j));
            } else {
                // log.debug("Found maximum similarity "+d(i,j));
            }
            // System.out.println("Found minimum distance "+this.dist[i][j]+"
            // between "+this.names[i]+" and "+this.names[j]);
            addNodeK(i, j, numclust);
        } else {
            throw new IllegalArgumentException("Could not find minimum!");
        }
    }

    @Override
    public void init(final double[][] distances, final String[] names,
            final TupleND<IFileFragment> fragments) {
        super.init(distances, names, fragments);
        this.R = new double[(distances.length) * 2 - 1];
        if (!isMinimizing()) {
            log.warn("{} should not be used on similarities!", this.getClass().
                    getName());
        }
    }

    @Override
    public void joinIJtoK(final int i, final int j, final int k,
            final double[] dist) {
        final double dij = d(i, j);
        final double dik = (dij + this.R[i] - this.R[j]) / 2.0d;
        final double djk = dij - dik;
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

    public void printRMatrix() {
        log.info("Printing relative Distance Vector:");
        log.info(java.util.Arrays.toString(this.R));
    }
    // /* (non-Javadoc)
    // * @see
    // maltcms.commands.fragments.multiplealignment.ClusteringAlgorithm#iterator(
    // )
    // */
    // public Iterator<Tuple2D<String, String>> iterator() {
    // synchronized (this) {// lock the object during construction of the
    // // iterator
    // Iterator<Integer> iter = this.nameToNameLookup.keySet().iterator();
    // ArrayList<Tuple2D<String, String>> list = new ArrayList<Tuple2D<String,
    // String>>();
    // while (iter.hasNext()) {
    // Integer i = iter.next();
    // System.out.println(this.cnames[i] + " "
    // + this.nameToNameLookup.get(i).getFirst() + " "
    // + this.nameToNameLookup.get(i).getSecond());
    // list.add(this.nameToNameLookup.get(i));
    // // NeighborJoinCluster njc = this.cluster.get(i);
    // //
    // System.out.println(NeighborJoinCluster.getConsensString(this.cluster.get(
    // njc.getLChild()),this.cluster.get(njc.getRChild())));
    // }
    //
    // return list.iterator();
    // }
    // }
}
