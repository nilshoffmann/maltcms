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
package maltcms.experimental.operations;

/**
 * Implementation of the Edit-Distance on Strings.
 *
 * @author Nils Hoffmann
 */
public class EditDistance {

    /**
     * Calculates the Edit-Distance on Strings a and b.
     *
     * @param a
     * @param b
     * @return
     */
    public double getDistance(String a, String b) {
        //check, if a or b are empty
        if (a.isEmpty() && !b.isEmpty()) {
            return b.length();
        } else if (!a.isEmpty() && b.isEmpty()) {
            return a.length();
        }
        //calculate edit distance matrix
        int[][] editDistanceMatrix = calculateEditDistanceMatrix(a, b);
        //print the matrix
        printMatrix(editDistanceMatrix, a, b);
        //recover trace (only one)
        String editSequence = traceback(editDistanceMatrix);
        //print the alignment
        printAlignment(a, b, editSequence);
        return editDistanceMatrix[editDistanceMatrix.length - 1][editDistanceMatrix[0].length - 1];
    }

    /**
     * Calculates the edit distance matrix of strings a and b.
     *
     * @param a
     * @param b
     * @return
     */
    public int[][] calculateEditDistanceMatrix(String a, String b) {
        //define shape of array
        int rows = a.length() + 1;
        int cols = b.length() + 1;
        //initialize array
        int[][] d = new int[rows][cols];
        int diag, ins, del;
        //d[0][0] = 0 => arrays are initialized with 0 in java
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (i > 0 && j > 0) {//most common case
                    diag = d[i - 1][j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1);
                    del = d[i - 1][j] + 1;//deletion
                    ins = d[i][j - 1] + 1;//insertion
                    d[i][j] = Math.min(diag, Math.min(ins, del));
                } else if (i == 0 && j > 0) {//column initialization
                    d[i][j] = j;//gap in column, insertion
                } else if (j == 0 && i > 0) {//row initialization
                    d[i][j] = i;//gap in row, deletion
                } else {//only once
                    d[i][j] = 0;
                }
            }
        }
        return d;
    }

    /**
     * Prints the alignment of Strings a and b, given an edit sequence.
     *
     * @param a
     * @param b
     * @param editSequence
     * @return
     */
    public String printAlignment(String a, String b, String editSequence) {
        StringBuilder as = new StringBuilder();
        StringBuilder bs = new StringBuilder();
        EditOperation editOp = EditOperation.C;
        int ai = 0;
        int bj = 0;
        for (int i = 0; i < editSequence.length(); i++) {
            editOp = EditOperation.valueOf(editSequence.substring(i, i + 1));

            switch (editOp) {
                case C:
                    System.out.println("EditOp:" + editOp.toString() + " i: " + a.substring(ai, ai + 1) + " j: " + b.substring(bj, bj + 1));
                    as.append(a.substring(ai, ai + 1));
                    bs.append(b.substring(bj, bj + 1));
                    ai++;
                    bj++;
                    break;
                case S:
                    System.out.println("EditOp:" + editOp.toString() + " i: " + a.substring(ai, ai + 1) + " j: " + b.substring(bj, bj + 1));
                    as.append(a.substring(ai, ai + 1));
                    bs.append(b.substring(bj, bj + 1));
                    ai++;
                    bj++;
                    break;
                case D:
                    System.out.println("EditOp:" + editOp.toString() + " i: " + a.substring(ai, ai + 1) + " j: -");
                    as.append(a.substring(ai, ai + 1));
                    bs.append("-");
                    ai++;
                    break;
                case I:
                    System.out.println("EditOp:" + editOp.toString() + " i: -" + " j: " + b.substring(bj, bj + 1));
                    bs.append(b.substring(bj, bj + 1));
                    as.append("-");
                    bj++;
                    break;
            }
        }
        StringBuilder alignment = new StringBuilder();
        alignment.append(as.toString() + "\n");
        alignment.append(bs.toString());
        String al = alignment.toString();
        System.out.println(al);
        return al;
    }

    /**
     * Returns a String encoding an optimal edit sequence, recovered from the
     * edit matrix d.
     *
     * @param d
     * @return
     */
    public String traceback(int[][] d) {
        int j = d[0].length - 1;
        int i = d.length - 1;
        double diag, ins, del;
        double min;
        StringBuilder sb = new StringBuilder(i + j + 1);
        double subst = 0;
        while (i != 0 || j != 0) {//repeat until we run out of left or right bound or both
            if (i > 0 && j == 0) {//first column deletions
                sb.append(EditOperation.D.name());
                i--;
            } else if (i == 0 && j > 0) {//first row insertions
                sb.append(EditOperation.I.name());
                j--;
            } else if (i > 0 && j > 0) {//all other cases
                diag = d[i - 1][j - 1];//diagonal
                subst = d[i][j] - d[i - 1][j - 1];
                del = d[i - 1][j];//deletion
                ins = d[i][j - 1];//insertion
                min = Math.min(Math.min(diag, ins), del);//min of all predecessors
                if (min == diag) {//min is diagonal
                    sb.append(subst == 1 ? EditOperation.S.name() : EditOperation.C.name());
                    i--;
                    j--;
                } else if (min == ins) {//
                    sb.append(EditOperation.I.name());
                    j--;
                } else if (min == del) {
                    sb.append(EditOperation.D.name());
                    i--;
                }
            }
        }
        String esequence = sb.reverse().toString();
        System.out.println("EditSequence: " + esequence);
        return esequence;
    }

    enum EditOperation {

        C, S, D, I;
    }

    /**
     * Pretty-print an alignment matrix with gaps.
     *
     * @param d
     * @param a
     * @param b
     */
    private void printMatrix(int[][] d, String a, String b) {
        StringBuilder sb = new StringBuilder();
        sb.append("\t-\t");
        for (int j = 0; j < b.length(); j++) {
            sb.append(b.charAt(j) + ((j == b.length() - 1) ? "" : "\t"));
        }
        sb.append("\n");
        for (int i = 0; i < a.length() + 1; i++) {
            sb.append((i == 0 ? "-\t" : a.charAt(i - 1) + "\t"));
            for (int j = 0; j < b.length() + 1; j++) {
                sb.append(((int) d[i][j]) + (j == b.length() ? "" : "\t"));
            }
            sb.append("\n");
        }
        System.out.println(sb.toString());
    }
}
