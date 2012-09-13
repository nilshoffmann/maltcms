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
package maltcms.test;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import cross.io.misc.ZipResourceExtractor;

/**
 *
 * @author hoffmann
 */
public class ExtractHelper {

    public static enum FType {

        CDF_1D, CDF_2D, MZML, MZDATA, MZXML
    };

    public static File[] extractForType(File tf, FType t, String... paths) {
        return extractFilesToDir(tf, paths);
    }

    public static File[] extractFilesToDir(File outputFolder, String[] paths) {
        List<File> files = new LinkedList<File>();
        for (String path : paths) {
            File outputFile = ZipResourceExtractor.extract(path, outputFolder);
            files.add(outputFile);
        }
        return files.toArray(new File[files.size()]);
    }

    public static File[] extractAllForType(File tf, FType t) {
        String[] paths = null;
        switch (t) {
            case CDF_1D:
                paths = new String[]{
                    "/cdf/1D/glucoseA.cdf.gz",
                    "/cdf/1D/glucoseB.cdf.gz",
                    "/cdf/1D/mannitolA.cdf.gz",
                    "/cdf/1D/mannitolB.cdf.gz",
                    "/cdf/1D/succinatA.cdf.gz",
                    "/cdf/1D/succinatB.cdf.gz"
                };
                break;
            case CDF_2D:
                paths = new String[]{
                    "/cdf/2D/090306_37_FAME_Standard_1.cdf.gz"
                };
                break;
            case MZML:
                paths = new String[]{
                    "/mzML/MzMLFile_PDA.mzML.xml.gz",
                    "/mzML/small.pwiz.1.1.mzML.gz",
                    "/mzML/tiny.pwiz.1.1.mzML.gz",};
                break;
            case MZDATA:
                paths = new String[]{
                    "/mzData/tiny1.mzData1.05.mzData.xml.gz"
                };
                break;
            case MZXML:
                paths = new String[]{
                    "/mzXML/tiny1.mzXML2.0.mzXML.gz",
                    "/mzXML/tiny1.mzXML3.0.mzXML.gz",};
                break;
            default:
                throw new IllegalArgumentException("Unknown file type " + t);
        }
        return extractForType(tf, t, paths);
    }
}
