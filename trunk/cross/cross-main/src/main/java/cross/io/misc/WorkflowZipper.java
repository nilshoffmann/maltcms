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
package cross.io.misc;

import cross.IConfigurable;
import cross.datastructures.tools.FileTools;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowFileResult;
import cross.datastructures.workflow.IWorkflowResult;
import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;

/**
 * If configured to do so, zips all elements of a given
 * <code>IWorkflow</code> matching the given
 * <code>FileFilter</code>. Marks directories and files which are unmatched for
 * deletion on exit of the virtual machine if configured.
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
public class WorkflowZipper implements IConfigurable {

    private IWorkflow iw = null;
    private FileFilter ff = new DefaultMatchAllFileFilter();
    private boolean zipWorkflow = true;
    private boolean deleteOnExit = false;
    private boolean flatten = false;

    private void addZipEntry(final int bufsize, final ZipOutputStream zos,
            final byte[] input_buffer, final File file, final HashSet<String> zipEntries) throws IOException {
        log.debug("Adding zip entry for file {}", file);
        if (file.exists() && file.isFile()) {
            // Use the file name for the ZipEntry name.
            final ZipEntry zip_entry = new ZipEntry(file.getName());
            if (zipEntries.contains(file.getName())) {
                log.info("Skipping duplicate zip entry {}", file.getName());
                return;
            } else {
                zipEntries.add(file.getName());
            }
            zos.putNextEntry(zip_entry);

            // Create a buffered input stream from the file stream.
            final FileInputStream in = new FileInputStream(file);
            final BufferedInputStream source = new BufferedInputStream(in,
                    bufsize);

            // Read from source into buffer and write, thereby compressing
            // on the fly
            int len = 0;
            while ((len = source.read(input_buffer, 0, bufsize)) != -1) {
                zos.write(input_buffer, 0, len);
            }
            zos.flush();
            source.close();
            zos.closeEntry();
        } else {
            log.warn("Skipping nonexistant file or directory {}", file);
        }
    }

    private void addRelativeZipEntry(final int bufsize, final ZipOutputStream zos,
            final byte[] input_buffer, final String relativePath, final File file, final HashSet<String> zipEntries) throws IOException {
        log.debug("Adding zip entry for file {}", file);
        if (file.exists() && file.isFile()) {
            // Use the file name for the ZipEntry name.
            final ZipEntry zip_entry = new ZipEntry(relativePath);
            if (zipEntries.contains(relativePath)) {
                log.info("Skipping duplicate zip entry {}", relativePath + "/" + file.getName());
                return;
            } else {
                zipEntries.add(relativePath);
            }
            zos.putNextEntry(zip_entry);

            // Create a buffered input stream from the file stream.
            final FileInputStream in = new FileInputStream(file);
            final BufferedInputStream source = new BufferedInputStream(in,
                    bufsize);

            // Read from source into buffer and write, thereby compressing
            // on the fly
            int len = 0;
            while ((len = source.read(input_buffer, 0, bufsize)) != -1) {
                zos.write(input_buffer, 0, len);
            }
            zos.flush();
            source.close();
            zos.closeEntry();
        } else {
            log.warn("Skipping nonexistant file or directory {}", file);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.IConfigurable#configure(org.apache.commons.configuration.Configuration
     * )
     */
    @Override
    public void configure(final Configuration cfg) {
    }

    /**
     * @return the deleteOnExit
     */
    public boolean isDeleteOnExit() {
        return this.deleteOnExit;
    }

    /**
     * @return the zipWorkflow
     */
    public boolean isZipWorkflow() {
        return this.zipWorkflow;
    }

    /**
     * Saves the currently assigned workflow elements, matching currently
     * assigned FileFilter to File. Marks all files for deletion on exit.
     *
     * @param f
     * @return
     */
    public boolean save(final File f) {
        if (this.zipWorkflow) {
            HashSet<String> zipEntries = new HashSet<String>();
            final int bufsize = 1024;
            final File zipFile = f;
            ZipOutputStream zos;
            try {
                final FileOutputStream fos = new FileOutputStream(zipFile);
                zos = new ZipOutputStream(new BufferedOutputStream(fos));
                log.info("Created zip output stream");
                final byte[] input_buffer = new byte[bufsize];
                File basedir = FileTools.prependDefaultDirsWithPrefix("", null,
                        this.iw.getStartupDate());
                if (this.deleteOnExit) {
                    log.info("marked basedir for deletion on exit: {}",
                            basedir);
                    basedir.deleteOnExit();
                }
                if (flatten) {
                    log.info("setting basedir to parent file: {}", basedir.getParentFile());
                    basedir = basedir.getParentFile();
                    final Iterator<IWorkflowResult> iter = this.iw.getResults();
                    while (iter.hasNext()) {
                        final IWorkflowResult iwr = iter.next();
                        if (iwr instanceof IWorkflowFileResult) {
                            final IWorkflowFileResult iwfr = (IWorkflowFileResult) iwr;
                            final File file = iwfr.getFile();
                            log.info("Retrieving file result {}", file);
                            // mark file for deletion
                            final File parent = file.getParentFile();
                            log.info("Retrieving parent of file result {}",
                                    parent);
                            // Also delete the parent directory in which file was
                            // contained,
                            // unless it is the base directory + possibly additional
                            // defaultDirs
                            if (parent.getAbsolutePath().startsWith(
                                    basedir.getAbsolutePath())
                                    && !parent.getAbsolutePath().equals(
                                    basedir.getAbsolutePath())) {
                                log.info("Marking file and parent for deletion");
                                if (this.deleteOnExit) {
                                    parent.deleteOnExit();
                                    file.deleteOnExit();
                                }
                            }
                            if (file.getAbsolutePath().startsWith(
                                    basedir.getAbsolutePath())) {
                                log.info("Marking file for deletion");
                                if (this.deleteOnExit) {
                                    file.deleteOnExit();
                                }
                            }
                            if ((this.ff != null) && !this.ff.accept(file)) {
                                // Skip file if file filter does not accept it
                                continue;
                            } else {
                                log.info("Adding zip entry!");
                                addZipEntry(bufsize, zos, input_buffer, file, zipEntries);
                            }
                        }

                    }
                } else {
                    LinkedList<File> files = new LinkedList<File>(Arrays.asList(basedir.listFiles(ff)));
                    File archiveBase = basedir.getParentFile();
                    while (!files.isEmpty()) {
                        File currentFile = files.removeFirst();
                        if (currentFile.isDirectory()) {
                            files.addAll(Arrays.asList(currentFile.listFiles(ff)));
                        } else {
                            try {
                                String relativePath = FileTools.getRelativeFile(archiveBase, currentFile).getPath().replaceAll("\\\\", "/");
                                log.info("Adding zip entry for {} below {}", relativePath, archiveBase);
                                addRelativeZipEntry(bufsize, zos, input_buffer, relativePath, currentFile, zipEntries);
                            } catch (Exception ex) {
                                log.warn("Caught exception while retrieving relative path:", ex);
                            }
                        }
                        if (this.deleteOnExit) {
                            log.info("Marking file for deletion");
                            currentFile.deleteOnExit();
                        }
                    }
                }

                try {
                    zos.flush();
                    zos.close();
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        } else {
            log.debug("Configured to not zip Workflow results!");
            return false;
        }
    }

    public boolean save(final File parentDir, final String filename) {
        return save(new File(parentDir, filename));
    }

    /**
     * @param deleteOnExit the deleteOnExit to set
     */
    public void setDeleteOnExit(final boolean deleteOnExit) {
        this.deleteOnExit = deleteOnExit;
    }

    /**
     * @param fileFilter
     */
    public void setFileFilter(final FileFilter fileFilter) {
        this.ff = fileFilter;

    }

    /**
     * @param workflow
     */
    public void setIWorkflow(final IWorkflow workflow) {
        this.iw = workflow;
    }

    /**
     * @param zipWorkflow the zipWorkflow to set
     */
    public void setZipWorkflow(final boolean zipWorkflow) {
        this.zipWorkflow = zipWorkflow;
    }

    /**
     *
     * @param flatten the flatten to set
     */
    public void setFlatten(boolean flatten) {
        this.flatten = flatten;
    }
}
