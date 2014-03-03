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
package smueller.tools;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;

// �berf�hrt die Daten, abh�ngig von der Alphabetgr��e in eine Symbol.
// Repr�sentation
/**
 * @author Soeren Mueller, smueller@cebitec.uni-bielefeld.de
 */
public class SymbolConvert {

    private static String alphabet;

    /**
     *
     * @return
     */
    public static String getAlphabet() {
        return SymbolConvert.alphabet;
    }

    /**
     *
     * @param reduced1
     * @param bp
     * @return
     */
    public static Array symbolic(final Array reduced1, final double[] bp) {
        final Array reduced = reduced1.copy();
        // erzeuge neues Array f�r Daten in Stringrep.
        final Array stringrep = Array
            .factory(DataType.CHAR, reduced.getShape());
        // Alphabetgr��e ermitteln
        final int buchstanz = bp.length - 1;
        double save = 0;
        // Buchstabenvorat
        // int[] charintervals = {65,90,97,122};
        final char[] buchst = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
            'k'};
        // int [] characters = new int[bp.length];
        // for(int i=0,offset=charintervals[0];i<characters.length;i++) {
        // if(charintervals[0]+i > charintervals[1]) {
        // offset = charintervals[2];
        // }
        // if(charintervals[2]+i > charintervals[3]) {
        // System.err.println("Symbols will not be visible!");
        // }
        // characters[i] = offset+i;
        // }
        //
        final IndexIterator ii4 = reduced.getIndexIterator();
        final IndexIterator stre = stringrep.getIndexIterator();
        // int bpcnt = 0;
        // //System.out.println("Size of breakpoint array: "+bp.length);
        // while(ii4.hasNext()) {
        // save = ii4.getDoubleNext();
        // int insert = Arrays.binarySearch(bp, save);
        // if(insert>=0) { // Found in Breakpoint
        // stre.setCharNext((char)characters[insert]);
        // }else{ // No direct hit
        // //-(insertionpoint)-1 is returned
        // //System.out.println(insert);
        // if(-insert-1==bp.length) {
        // stre.setCharNext((char)characters[bp.length-1]);
        // }else{
        // stre.setCharNext((char)characters[(-(insert)-1)]);
        // }
        // }
        // }
        // StringBuffer alphabetbuf = new StringBuffer(characters.length);
        // alphabetbuf.append('-');
        // for(int i=1;i<characters.length;i++){
        // alphabetbuf.append((char)characters[i-1]);
        // }
        // alphabet = alphabetbuf.toString();
        // System.out.println("Alphabet: "+alphabet);

        // Vergleiche, abh�ngig von Alphabetgr.
        switch (buchstanz) {
            case 3:

                while (ii4.hasNext()) {
                    save = ii4.getDoubleNext();
                    if (save < bp[0]) {
                        stre.setCharNext(buchst[0]);
                    } else if (save < bp[1]) {
                        stre.setCharNext(buchst[1]);
                    } else {
                        stre.setCharNext(buchst[2]);
                    }

                }

                SymbolConvert.alphabet = "-abc";
                break;
            case 4:
                while (ii4.hasNext()) {
                    save = ii4.getDoubleNext();
                    if (save < bp[1]) {
                        if (save < bp[0]) {
                            stre.setCharNext(buchst[0]);
                        } else {
                            stre.setCharNext(buchst[1]);
                        }
                    } else if (save < bp[2]) {
                        stre.setCharNext(buchst[2]);
                    } else {
                        stre.setCharNext(buchst[3]);
                    }
                }
                SymbolConvert.alphabet = "-abcd";

                break;
            case 5:
                while (ii4.hasNext()) {
                    save = ii4.getDoubleNext();
                    if (save < bp[1]) {
                        if (save < bp[0]) {
                            stre.setCharNext(buchst[0]);
                        } else {
                            stre.setCharNext(buchst[1]);
                        }
                    } else if (save < bp[2]) {
                        stre.setCharNext(buchst[2]);
                    } else if (save < bp[3]) {
                        stre.setCharNext(buchst[3]);
                    } else {
                        stre.setCharNext(buchst[4]);
                    }
                }
                SymbolConvert.alphabet = "-abcde";

                break;
            case 6:

                while (ii4.hasNext()) {
                    save = ii4.getDoubleNext();
                    if (save < bp[2]) {
                        if (save < bp[1]) {
                            if (save < bp[0]) {
                                stre.setCharNext(buchst[0]);
                            } else {
                                stre.setCharNext(buchst[1]);
                            }
                        } else {
                            stre.setCharNext(buchst[2]);
                        }
                    } else if (save < bp[3]) {
                        stre.setCharNext(buchst[3]);
                    } else if (save < bp[4]) {
                        stre.setCharNext(buchst[4]);
                    } else {
                        stre.setCharNext(buchst[5]);
                    }

                }
                SymbolConvert.alphabet = "-abcdef";

                break;
            case 7:

                while (ii4.hasNext()) {
                    save = ii4.getDoubleNext();
                    if (save < bp[2]) {
                        if (save < bp[0]) {
                            stre.setCharNext(buchst[0]);
                        } else if (save < bp[1]) {
                            stre.setCharNext(buchst[1]);
                        } else {
                            stre.setCharNext(buchst[2]);
                        }
                    } else {
                        if (save < bp[4]) {
                            if (save < bp[3]) {
                                stre.setCharNext(buchst[3]);
                            } else {
                                stre.setCharNext(buchst[4]);
                            }
                        } else if (save < bp[5]) {
                            stre.setCharNext(buchst[5]);
                        } else {
                            stre.setCharNext(buchst[6]);
                        }
                    }
                    SymbolConvert.alphabet = "-abcdefg";

                }
                break;
            case 8:
                while (ii4.hasNext()) {
                    save = ii4.getDoubleNext();
                    if (save < bp[3]) {
                        if (save < bp[1]) {
                            if (save < bp[0]) {
                                stre.setCharNext(buchst[0]);
                            } else {
                                stre.setCharNext(buchst[1]);
                            }
                        } else if (save < bp[2]) {
                            stre.setCharNext(buchst[2]);
                        } else {
                            stre.setCharNext(buchst[3]);
                        }
                    } else {
                        if (save < bp[5]) {
                            if (save < bp[4]) {
                                stre.setCharNext(buchst[4]);
                            } else {
                                stre.setCharNext(buchst[5]);
                            }
                        } else if (save < bp[6]) {
                            stre.setCharNext(buchst[6]);
                        } else {
                            stre.setCharNext(buchst[7]);
                        }
                    }

                }
                SymbolConvert.alphabet = "-abcdefgh";
                break;
            case 9:
                while (ii4.hasNext()) {
                    save = ii4.getDoubleNext();

                    if (save < bp[3]) {
                        if (save < bp[1]) {
                            if (save < bp[0]) {
                                stre.setCharNext(buchst[0]);
                            } else {
                                stre.setCharNext(buchst[1]);
                            }
                        } else if (save < bp[2]) {
                            stre.setCharNext(buchst[2]);
                        } else {
                            stre.setCharNext(buchst[3]);
                        }
                    } else {
                        if (save < bp[5]) {
                            if (save < bp[4]) {
                                stre.setCharNext(buchst[4]);
                            } else {
                                stre.setCharNext(buchst[5]);
                            }
                        } else if (save < bp[6]) {
                            stre.setCharNext(buchst[6]);
                        } else if (save < bp[7]) {
                            stre.setCharNext(buchst[7]);
                        } else {
                            stre.setCharNext(buchst[8]);
                        }

                    }
                }
                SymbolConvert.alphabet = "-abcdefghi";

                break;
            case 10:
                while (ii4.hasNext()) {
                    save = ii4.getDoubleNext();

                    if (save < bp[4]) {
                        if (save < bp[2]) {
                            if (save < bp[0]) {
                                stre.setCharNext(buchst[0]);
                            } else if (save < bp[1]) {
                                stre.setCharNext(buchst[1]);
                            } else {
                                stre.setCharNext(buchst[2]);
                            }
                        } else if (save < bp[3]) {
                            stre.setCharNext(buchst[3]);
                        } else {
                            stre.setCharNext(buchst[4]);
                        }
                    } else {
                        if (save < bp[7]) {
                            if (save < bp[5]) {
                                stre.setCharNext(buchst[5]);
                            } else if (save < bp[6]) {
                                stre.setCharNext(buchst[6]);
                            } else {
                                stre.setCharNext(buchst[7]);
                            }
                        } else if (save < bp[8]) {
                            stre.setCharNext(buchst[8]);
                        } else {
                            stre.setCharNext(buchst[9]);
                        }
                    }
                }
                SymbolConvert.alphabet = "-abcdefghik";

                break;
        }

        return stringrep;
    }
}
