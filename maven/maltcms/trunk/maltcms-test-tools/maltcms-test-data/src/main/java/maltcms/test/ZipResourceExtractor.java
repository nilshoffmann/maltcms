/* 
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package maltcms.test;

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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.Cleanup;

public class ZipResourceExtractor {

    public static File extract(String resourcePath, File destDir) {
        System.out.println("Extracting " + resourcePath + " to directory: " + destDir);
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
                String outname = new File(resourceURL.getPath()).getName();
				outname = outname.replaceAll("%20", " ");
				System.out.println(outname);
				if (resourcePath.endsWith("zip")) {
					outname = outname.substring(0, outname.lastIndexOf(
                            "."));
					return extractZipArchive(resourceInputStream, destDir);
				} else if (resourcePath.endsWith("gz")) {
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
	
	public static File extractZipArchive(InputStream istream, File outputDir) {
		try {
            ZipInputStream zis = new ZipInputStream(
                    new BufferedInputStream(istream));
            ZipEntry entry;
			File outDir = null;
            while ((entry = zis.getNextEntry()) != null) {
                int size;
                byte[] buffer = new byte[2048];
				File outFile = new File(outputDir,entry.getName());
				if(entry.isDirectory()) {
					outFile.mkdirs();
					if(outDir==null) {
						outDir = outFile;
					}
				}else{
					FileOutputStream fos = new FileOutputStream(outFile);
					BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length);
					while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
						bos.write(buffer, 0, size);
					}
					bos.flush();
					bos.close();
					fos.close();
				}
            }
			if(outDir==null) {
				outDir = outputDir;
			}
            zis.close();
            istream.close();
			return outDir;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
}
