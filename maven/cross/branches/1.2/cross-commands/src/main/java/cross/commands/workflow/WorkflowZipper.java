/**
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 *
 * This file is part of Cross/Maltcms.
 *
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * 
 *
 * $Id$
 */
package cross.commands.workflow;

import cross.io.FileTools;
import cross.io.misc.DefaultConfigurableFileFilter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * If configured to do so, zips all elements of a given <code>IWorkflow</code>
 * matching the given <code>FileFilter</code>. Marks directories and files which
 * are unmatched for deletion on exit of the virtual machine if configured.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Data
@Slf4j
public class WorkflowZipper {

    private IWorkflow workflow = null;
    private FileFilter fileFilter = new DefaultConfigurableFileFilter();
    private boolean zipWorkflow = true;
    private boolean deleteOnExit = false;
    private HashSet<String> zipEntries = new HashSet<String>();
    private FileTools fileTools;

    private void addZipEntry(final int bufsize, final ZipOutputStream zos,
            final byte[] input_buffer, final File file) throws IOException {
        log.debug("Adding zip entry for file {}", file);
        if (file.exists() && file.isFile()) {
            // Use the file name for the ZipEntry name.
            final ZipEntry zip_entry = new ZipEntry(file.getName());
            if (this.zipEntries.contains(file.getName())) {
                log.info("Skipping duplicate zip entry {}", file.getName());
                return;
            } else {
                this.zipEntries.add(file.getName());
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

    /**
     * Saves the currently assigned workflow elements, matching currently
     * assigned FileFilter to File. Marks all files for deletion on exit.
     * 
     * @param f
     * @return
     */
    public boolean save(final File f) {
        if (this.zipWorkflow) {
            this.zipEntries.clear();
            final int bufsize = 1024;
            final File zipFile = f;
            ZipOutputStream zos;
            try {
                final FileOutputStream fos = new FileOutputStream(zipFile);
                zos = new ZipOutputStream(new BufferedOutputStream(fos));
                log.info("Created zip output stream");
                final byte[] input_buffer = new byte[bufsize];
                final Iterator<IWorkflowResult> iter = this.workflow.getResults();
                File basedir = fileTools.prependDefaultDirsWithPrefix("", null,
                        this.workflow.getStartupDate());
                log.info("marked basedir for deletion on exit: {}",
                        basedir);
                if (this.deleteOnExit) {
                    basedir.deleteOnExit();
                }
                log.info("setting basedir to parent file: {}", basedir.
                        getParentFile());
                basedir = basedir.getParentFile();
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
                        if ((this.fileFilter != null) && !this.fileFilter.accept(file)) {
                            // Skip file if file filter does not accept it
                            continue;
                        } else {
                            log.info("Adding zip entry!");
                            addZipEntry(bufsize, zos, input_buffer, file);
                        }
                    }

                }
//                throw new NotImplementedException();
//                final File runtimeProps = fileTools.prependDefaultDirsWithPrefix(
//                        "", null, this.workflow.getStartupDate());
//                log.info("Saving config to {}", runtimeProps);
//                Factory.saveConfiguration(
//                        Factory.getInstance().getConfiguration(), runtimeProps);
//                if (runtimeProps.exists() && runtimeProps.canRead()) {
//                    log.info("Marking file for deletion");
//                    if (this.deleteOnExit) {
//                        runtimeProps.deleteOnExit();
//                    }
//                    addZipEntry(bufsize, zos, input_buffer, runtimeProps);
//                }

                try {
                    zos.flush();
                    zos.close();
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                    // log.error(e.getLocalizedMessage());
                    // return false;
                }
            } catch (final IOException e) {
                throw new RuntimeException(e);
                // log.error(e.getLocalizedMessage());
                // return false;
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
}