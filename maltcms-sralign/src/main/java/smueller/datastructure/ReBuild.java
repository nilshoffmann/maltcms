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
package smueller.datastructure;

import smueller.SymbolicRepresentationAlignment;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;

/**
 * <p>ReBuild class.</p>
 *
 * @author Soeren Mueller, smueller@cebitec.uni-bielefeld.de
 * @version $Id: $Id
 */
public class ReBuild {

    /**
     * <p>addbaseclean.</p>
     *
     * @param a1 a {@link ucar.ma2.Array} object.
     * @param m a double.
     * @return a {@link ucar.ma2.Array} object.
     */
    public static Array addbaseclean(final Array a1, final double m) {

        final Array a = a1.copy();

        final IndexIterator ii = a.getIndexIterator();

        while (ii.hasNext()) {

            final double save = ((ii.getDoubleNext() + m));

            ii.setDoubleCurrent(save);

            // System.out.println(save);
        }

        return a;

    }

    /**
     * <p>strToDoubArray.</p>
     *
     * @param str a {@link java.lang.String} object.
     * @return a {@link ucar.ma2.Array} object.
     */
    public static Array strToDoubArray(final String str) {

        final int[] dimension = {((str.length() - 1) * (SymbolicRepresentationAlignment
            .getFenstergr()))};

        final Array rebuild = Array.factory(DataType.DOUBLE, dimension);

        final IndexIterator reb = rebuild.getIndexIterator();

        for (int i = 1; i < str.length(); i++) {

            if (str.charAt(i) == 'a') {

                for (int j = 0; j < SymbolicRepresentationAlignment
                        .getFenstergr(); j++) {

                    reb.setDoubleNext(SymbolicRepresentationAlignment
                            .getBpois().getCommon()[0] / 2);

                }

            } else if (str.charAt(i) == 'b') {

                for (int j = 0; j < SymbolicRepresentationAlignment
                        .getFenstergr(); j++) {

                    reb
                            .setDoubleNext((SymbolicRepresentationAlignment
                                    .getBpois().getCommon()[0] + SymbolicRepresentationAlignment
                                    .getBpois().getCommon()[1]) / 2);

                }

            } else if (str.charAt(i) == 'c') {

                for (int j = 0; j < SymbolicRepresentationAlignment
                        .getFenstergr(); j++) {

                    reb
                            .setDoubleNext((SymbolicRepresentationAlignment
                                    .getBpois().getCommon()[1] + SymbolicRepresentationAlignment
                                    .getBpois().getCommon()[2]) / 2);

                }

            } else if (str.charAt(i) == 'd') {

                for (int j = 0; j < SymbolicRepresentationAlignment
                        .getFenstergr(); j++) {

                    reb
                            .setDoubleNext((SymbolicRepresentationAlignment
                                    .getBpois().getCommon()[2] + SymbolicRepresentationAlignment
                                    .getBpois().getCommon()[3]) / 2);

                }

            } else if (str.charAt(i) == 'e') {

                for (int j = 0; j < SymbolicRepresentationAlignment
                        .getFenstergr(); j++) {

                    reb
                            .setDoubleNext((SymbolicRepresentationAlignment
                                    .getBpois().getCommon()[3] + SymbolicRepresentationAlignment
                                    .getBpois().getCommon()[4]) / 2);

                }

            } else if (str.charAt(i) == 'f') {

                for (int j = 0; j < SymbolicRepresentationAlignment
                        .getFenstergr(); j++) {

                    reb
                            .setDoubleNext((SymbolicRepresentationAlignment
                                    .getBpois().getCommon()[4] + SymbolicRepresentationAlignment
                                    .getBpois().getCommon()[5]) / 2);

                }

            } else if (str.charAt(i) == 'g') {

                for (int j = 0; j < SymbolicRepresentationAlignment
                        .getFenstergr(); j++) {

                    reb
                            .setDoubleNext((SymbolicRepresentationAlignment
                                    .getBpois().getCommon()[5] + SymbolicRepresentationAlignment
                                    .getBpois().getCommon()[6]) / 2);

                }

            } else if (str.charAt(i) == 'h') {

                for (int j = 0; j < SymbolicRepresentationAlignment
                        .getFenstergr(); j++) {

                    reb
                            .setDoubleNext((SymbolicRepresentationAlignment
                                    .getBpois().getCommon()[6] + SymbolicRepresentationAlignment
                                    .getBpois().getCommon()[7]) / 2);

                }

            } else if (str.charAt(i) == 'i') {

                for (int j = 0; j < SymbolicRepresentationAlignment
                        .getFenstergr(); j++) {

                    reb
                            .setDoubleNext((SymbolicRepresentationAlignment
                                    .getBpois().getCommon()[7] + SymbolicRepresentationAlignment
                                    .getBpois().getCommon()[8]) / 2);

                }

            } else if (str.charAt(i) == 'k') {

                for (int j = 0; j < SymbolicRepresentationAlignment
                        .getFenstergr(); j++) {

                    reb
                            .setDoubleNext((SymbolicRepresentationAlignment
                                    .getBpois().getCommon()[8] + SymbolicRepresentationAlignment
                                    .getBpois().getCommon()[9]) / 2);

                }

            }

        }

        return rebuild;

    }

    /**
     * <p>unlog.</p>
     *
     * @param a1 a {@link ucar.ma2.Array} object.
     * @return a {@link ucar.ma2.Array} object.
     */
    public static Array unlog(final Array a1) {

        final Array a = a1.copy();

        final IndexIterator ii = a.getIndexIterator();

        while (ii.hasNext()) {

            final double save = Math.exp(ii.getDoubleNext());

            ii.setDoubleCurrent(save);

        }

        return a;

    }

    /**
     * <p>unscale.</p>
     *
     * @param a1 a {@link ucar.ma2.Array} object.
     * @param min a double.
     * @param max a double.
     * @return a {@link ucar.ma2.Array} object.
     */
    public static Array unscale(final Array a1, final double min,
            final double max) {

        final Array a = a1.copy();

        final IndexIterator ii4 = a.getIndexIterator();

        final double minmax = 1 / (max - min);

        while (ii4.hasNext()) {

            final double save = (ii4.getDoubleNext() / minmax + min);

            ii4.setDoubleCurrent(save);

            // System.out.println(save);
            System.out.println("minmax" + Math.round(minmax) + "Min" + min
                    + "max" + max);

        }

        return a;

    }

}
