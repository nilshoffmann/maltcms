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
package maltcms.test;

import java.io.File;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>ExtractHelper class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class ExtractHelper {

    /** Constant <code>typeToPaths</code> */
    public static EnumMap<FType, String[]> typeToPaths = new EnumMap<>(FType.class);

    public static enum FType {

        CDF_1D, CDF_2D, MZML, MZDATA, MZXML, MZ5
    };

    /**
     * <p>setDefaults.</p>
     */
    public static void setDefaults() {
        typeToPaths.clear();
        typeToPaths.put(FType.CDF_1D, new String[]{
            "/cdf/1D/glucoseA.cdf.gz",
            "/cdf/1D/glucoseB.cdf.gz",
            "/cdf/1D/mannitolA.cdf.gz",
            "/cdf/1D/mannitolB.cdf.gz",
            "/cdf/1D/succinatA.cdf.gz",
            "/cdf/1D/succinatB.cdf.gz"
        });
        typeToPaths.put(FType.CDF_2D, new String[]{
            "/cdf/2D/090306_37_FAME_Standard_1.cdf.gz"
        });
        typeToPaths.put(FType.MZML, new String[]{
            "/mzML/MzMLFile_PDA.mzML.xml.gz",
            "/mzML/small.pwiz.1.1.mzML.gz",
            "/mzML/tiny.pwiz.1.1.mzML.gz"});
        typeToPaths.put(FType.MZDATA, new String[]{
            "/mzData/tiny1.mzData1.05.mzData.xml.gz"
        });
        typeToPaths.put(FType.MZXML, new String[]{
            "/mzXML/tiny1.mzXML2.0.mzXML.gz",
            "/mzXML/tiny1.mzXML3.0.mzXML.gz",});
        typeToPaths.put(FType.MZ5, new String[]{
            "/mz5/small_raw.mz5.gz"});
    }

    /**
     * <p>extractForType.</p>
     *
     * @param tf a {@link java.io.File} object.
     * @param t a {@link maltcms.test.ExtractHelper.FType} object.
     * @param paths a {@link java.lang.String} object.
     * @return an array of {@link java.io.File} objects.
     */
    public static File[] extractForType(File tf, FType t, String... paths) {
        return extractFilesToDir(tf, paths);
    }

    /**
     * <p>extractFilesToDir.</p>
     *
     * @param outputFolder a {@link java.io.File} object.
     * @param paths a {@link java.lang.String} object.
     * @return an array of {@link java.io.File} objects.
     */
    public static File[] extractFilesToDir(File outputFolder, String... paths) {
        List<File> files = new LinkedList<>();
        for (String path : paths) {
            File outputFile = ZipResourceExtractor.extract(path, outputFolder);
            files.add(outputFile);
        }
        return files.toArray(new File[files.size()]);
    }

    /**
     * <p>getPathsForType.</p>
     *
     * @param t a {@link maltcms.test.ExtractHelper.FType} object.
     * @return an array of {@link java.lang.String} objects.
     */
    public static String[] getPathsForType(FType t) {
        if (typeToPaths.isEmpty()) {
            setDefaults();
        }
        return typeToPaths.get(t);
    }

    /**
     * <p>extractAllForType.</p>
     *
     * @param tf a {@link java.io.File} object.
     * @param t a {@link maltcms.test.ExtractHelper.FType} object.
     * @return an array of {@link java.io.File} objects.
     */
    public static File[] extractAllForType(File tf, FType t) {
        return extractForType(tf, t, getPathsForType(t));
    }
}
