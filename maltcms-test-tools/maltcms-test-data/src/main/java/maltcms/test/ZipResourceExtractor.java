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
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * ZipResourceExtractor class.</p>
 *
 * @author Nils Hoffmann
 *
 */
public class ZipResourceExtractor {

    private static final Logger log = LoggerFactory.getLogger(ZipResourceExtractor.class);

    /**
     * <p>
     * extract.</p>
     *
     * @param resourcePath a {@link java.lang.String} object.
     * @param destDir a {@link java.io.File} object.
     * @return a {@link java.io.File} object.
     */
    public static File extract(String resourcePath, File destDir) {
        log.info("Extracting " + resourcePath + " to directory: " + destDir);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        URL resourceURL = ZipResourceExtractor.class.getResource(resourcePath);
        if (resourceURL == null) {
            throw new NullPointerException(
                    "Could not retrieve resource for path: " + resourcePath);
        }
        String outname = new File(resourceURL.getPath()).getName();
        outname = outname.replaceAll("%20", " ");
        log.info(outname);
        try (InputStream resourceInputStream = resourceURL.openStream()) {
            if (resourcePath.endsWith("zip")) {
                outname = outname.substring(0, outname.lastIndexOf(
                        "."));
                return extractZipArchive(resourceInputStream, destDir);
            } else {
                if (resourcePath.endsWith("gz")) {
                    try (GZIPInputStream in = new GZIPInputStream(new BufferedInputStream(
                            resourceInputStream))) {
                        outname = outname.substring(0, outname.lastIndexOf(
                                "."));
                        File outputFile = new File(destDir, outname);
                        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                            copyInToOut(in, bos);
                            return outputFile;
                        } catch (FileNotFoundException ex) {
                            log.error(ex.getLocalizedMessage(), ex);
                        } catch (IOException ex) {
                            log.error(ex.getLocalizedMessage(), ex);
                        }

                    } catch (IOException ex) {
                        log.error(ex.getLocalizedMessage(), ex);
                    }
                } else {
                    try (BufferedInputStream bin = new BufferedInputStream(resourceInputStream)) {
                        File outputFile = new File(destDir, outname);
                        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                            copyInToOut(bin, bos);
                            return outputFile;
                        } catch (FileNotFoundException ex) {
                            log.error(ex.getLocalizedMessage(), ex);
                        } catch (IOException ex) {
                            log.error(ex.getLocalizedMessage(), ex);
                        }
                    } catch (IOException ex) {
                        log.error(ex.getLocalizedMessage(), ex);
                    }

                }
            }
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage(), ex);
        }
        throw new NullPointerException(
                "Could not copy resource for path: " + resourcePath + " to output: " + outname);
    }

    private static void copyInToOut(InputStream in, OutputStream out) throws IOException {
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
    }

    /**
     * <p>
     * extractZipArchive.</p>
     *
     * @param istream a {@link java.io.InputStream} object.
     * @param outputDir a {@link java.io.File} object.
     * @return a {@link java.io.File} object.
     */
    public static File extractZipArchive(InputStream istream, File outputDir) {
        try {
            File outDir;
            try (ZipInputStream zis = new ZipInputStream(
                    new BufferedInputStream(istream))) {
                ZipEntry entry;
                outDir = null;
                while ((entry = zis.getNextEntry()) != null) {
                    int size;
                    byte[] buffer = new byte[2048];
                    File outFile = new File(outputDir, entry.getName());
                    if (entry.isDirectory()) {
                        outFile.mkdirs();
                        if (outDir == null) {
                            outDir = outFile;
                        }
                    } else {
                        try (FileOutputStream fos = new FileOutputStream(outFile); BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length)) {
                            while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
                                bos.write(buffer, 0, size);
                            }
                            bos.flush();
                        }
                    }
                }
                if (outDir == null) {
                    outDir = outputDir;
                }
            }
            return outDir;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
