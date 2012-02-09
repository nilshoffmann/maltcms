/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package maltcms.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import lombok.Cleanup;

public class ZipResourceExtractor {

    public static File extract(String resourcePath, File destDir) {
        System.out.println("Extracting "+resourcePath+" to directory: "+destDir);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        URL resourceURL = ZipResourceExtractor.class.getResource(resourcePath);
        if (resourceURL == null) {
            throw new NullPointerException(
                    "Could not retrieve resource for path: " + resourcePath);
        }
        File outputFile = null;
        try {
            @Cleanup
            InputStream resourceInputStream = resourceURL.openStream();
            @Cleanup
            InputStream in = null;
            @Cleanup
            OutputStream out = null;
            try {
                String outname = new File(resourceURL.getPath()).getName();;
                if (resourcePath.endsWith("gz")) {
                    in = new GZIPInputStream(new BufferedInputStream(
                            resourceInputStream));

                    outname = outname.substring(0, outname.lastIndexOf(
                            "."));
                } else {
                    in = new BufferedInputStream(resourceInputStream);
                }
                outputFile = new File(destDir, outname);
                out = new BufferedOutputStream(new FileOutputStream(outputFile));
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (IOException ex) {
            Logger.getLogger(ZipResourceExtractor.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
        
        return outputFile;
    }
}
