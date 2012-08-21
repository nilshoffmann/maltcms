/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
    
    public static enum FType{CDF_1D,CDF_2D,MZML,MZDATA,MZXML};
    
    public static File[] extractForType(File tf, FType t, String...paths) {
        return extractFilesToDir(tf,paths);
    }
    
    public static File[] extractFilesToDir(File outputFolder, String[] paths) {
        List<File> files = new LinkedList<File>();
        for(String path:paths) {
            File outputFile = ZipResourceExtractor.extract(path, outputFolder);
            files.add(outputFile);
        }
        return files.toArray(new File[files.size()]);
    }
    
    public static File[] extractAllForType(File tf, FType t) {
        String[] paths = null;
        switch(t) {
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
                paths= new String[]{
                    "/cdf/2D/090306_37_FAME_Standard_1.cdf.gz"
                };
                break;
            case MZML:
                paths= new String[]{
                    "/mzML/MzMLFile_PDA.mzML.xml.gz",
                    "/mzML/small.pwiz.1.1.mzML.gz",
                    "/mzML/tiny.pwiz.1.1.mzML.gz",
                };
                break;
            case MZDATA:
                paths= new String[]{
                    "/mzData/tiny1.mzData1.05.mzData.xml.gz"
                };
                break;
            case MZXML:
                paths= new String[]{
                    "/mzXML/tiny1.mzXML2.0.mzXML.gz",
                    "/mzXML/tiny1.mzXML3.0.mzXML.gz",
                };
                break;
            default:
                throw new IllegalArgumentException("Unknown file type "+t);
        }
        return extractForType(tf, t, paths);
    }
    
}
