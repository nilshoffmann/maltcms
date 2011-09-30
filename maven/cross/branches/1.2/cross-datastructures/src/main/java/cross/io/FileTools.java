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
package cross.io;

import cross.datastructures.fragments.FileFragment;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.tools.StringTools;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class to ease handling of files and directories.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Data
@Slf4j
public class FileTools {

    private SimpleDateFormat dateFormat = new SimpleDateFormat(
            "MM-dd-yyyy_HH-mm-ss", Locale.US);
    private boolean overwriteOutput = false;
    private boolean omitUserTimePrefix = false;
    private String outputBasedir = ".";

    private File appendCreatorNameToBaseDir(final File base,
            String prefix, final Class<?> creator) {
        File creatordir = base;
        if (creator != null) {
            final String creatorName = creator.getSimpleName();
            if (!creatorName.equals("")) {
                creatordir = new File(base, prefix + creatorName);
            }
        }
        if (!creatordir.exists()) {
            creatordir.mkdirs();
        }
        return creatordir;
    }

    protected File checkFileReadable(final IFileFragment ff)
            throws IOException {
        log.debug("Trying to locate {}", ff.getName());
        final File outF = new File(ff.getAbsolutePath()).getCanonicalFile();
        if (outF.exists()) {
            log.debug("Found {} in directory {}", ff.getName(), outF.getParent());
            // knownFiles.put(outF.getAbsolutePath(), outF);
            return outF;
        } else {
            throw new IOException("File does not exist: "
                    + outF.getAbsolutePath());
        }
    }

    protected File createFile(final IFileFragment f) throws IOException {
        File file = null;
        log.debug("File extension: {}", StringTools.getFileExtension(f.
                getAbsolutePath()));
        IFileFragment ff = f;
        try {
            file = findFile(f);
            log.info(
                    "File exists, checking, whether we should overwrite or create new file in temporary location!");
            if (overwriteOutput) {
                log.info(
                        "Option overwriteOutput=true, overwriting existing file!");
                file.delete();
                file = new File(file.getAbsolutePath());
            } else {
                log.info(
                        "File {} already exists, creating file in temporary location!",
                        f.getAbsolutePath());
                final String tmpdir = System.getProperty("java.io.tmpdir");
                final File tmp = new File(tmpdir);
                file = new File(tmp, file.getName());
                log.debug("Setting {} as source file of {}", f.getAbsolutePath(),
                        file.getAbsolutePath());
                ff = new FileFragment(file);
                ff.addSourceFile(f);
            }
        } catch (final IOException ioex) {
            log.debug(ioex.getLocalizedMessage());
            // create the file and it's parent directories atomically
            log.debug("File does not exist, creating atomically!");
            file = new File(f.getAbsolutePath());
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        // }
        return file;
    }

    protected File findFile(final IFileFragment f) throws IOException {
        try {
            final File outF = checkFileReadable(f);
            return outF;
        } catch (final IOException ioex) {
            throw ioex;
        }
    }

    protected File findFile(final IVariableFragment vf)
            throws IOException {
        return findFile(vf.getParent());
    }

    public File getDefaultDirs(final Date d) {
        if (omitUserTimePrefix) {
            final File basedir = new File(outputBasedir);
            return basedir;
        } else if (d == null) {
            final File usernamebasedir = new File(outputBasedir,
                    System.getProperty("user.name"));
            return usernamebasedir;
        } else {
            final File basedir = new File(outputBasedir,
                    System.getProperty("user.name"));
            final File datedir = new File(basedir, dateFormat.format(d));
            return datedir;
        }
    }

    public String getDirname(final String fullname) {
        final File f = new File(fullname);
        return f.getParent();
    }

    public File getFile(final IFileFragment ff) throws IOException {
        return findFile(ff);
    }

    public String getFilename(final String fullname) {
        final File f = new File(fullname);
        return f.getName();
    }

    private File getNextFreeFileName(final File file) {
        File f = file;
        int i = 1;
        if (overwriteOutput) {
            f.delete();
        }
        if (!f.exists()) {
            return f;
        }
        while (f.exists()) {
            final String ext = StringTools.getFileExtension(f.getAbsolutePath());
            final String base = StringTools.removeFileExt(f.getAbsolutePath());
            f = new File(base + "" + i + "." + ext);
            i++;
        }
        return f;
    }

    private File getNextFreeFileName(final String filename) {
        return getNextFreeFileName(new File(filename));
    }

    public File prepareOutput(final IFileFragment parent)
            throws IOException {
        final File f = createFile(parent);
        log.debug("Writing to file " + f.getAbsolutePath() + "\n");
        return f;
    }

    public File prepareOutput(final String dir, final String filename) {
        return prepareOutput(dir, filename, "csv");
    }

    public File prepareOutput(final String dir, final String filename,
            final String filetypeSuffix) {
        final String basedir = ((dir == null) || dir.isEmpty()) ? outputBasedir
                : dir;
        final File d = new File(basedir);
        if (!d.exists()) {
            d.mkdirs();
        }

        File f = new File(d, filename + "." + filetypeSuffix);
        f = getNextFreeFileName(f);
        return f;
    }

    public File prepareOutput(final File dir, final String filename) {
        return prepareOutput(dir.getAbsolutePath(), filename);
    }

    public File prependDefaultDirsWithPrefix(String prefix,
            final Class<?> creator, final Date d) {
        return appendCreatorNameToBaseDir(
                getDefaultDirs(d), prefix, creator);
    }

    public String resolveRelativeFile(File base, File relativeFile) throws IOException {
        return new File(base, relativeFile.getPath()).getCanonicalPath();
    }

    public File getRelativeFile(IFileFragment target, IFileFragment base) throws IOException {
        return getRelativeFile(new File(target.getAbsolutePath()),
                new File(base.getAbsolutePath()).getParentFile());
    }

    /**
     * Returns the path of one File relative to another.
     *
     * @param target the target directory
     * @param base the base directory
     * @return target's path relative to the base directory
     * @throws IOException if an error occurs while resolving the files' canonical names
     */
    public File getRelativeFile(File target, File base) throws IOException {
        String[] baseComponents = base.getCanonicalPath().split(Pattern.quote(
                File.separator));
        String[] targetComponents = target.getCanonicalPath().split(Pattern.
                quote(File.separator));

        // skip common components
        int index = 0;
        for (; index < targetComponents.length && index < baseComponents.length; ++index) {
            if (!targetComponents[index].equals(baseComponents[index])) {
                break;
            }
        }

        StringBuilder result = new StringBuilder();
        if (index != baseComponents.length) {
            // backtrack to base directory
            for (int i = index; i < baseComponents.length; ++i) {
                result.append("..");
                result.append(File.separator);
            }
        }
        for (; index < targetComponents.length; ++index) {
            result.append(targetComponents[index]);
            result.append(File.separator);
        }
        if (!target.getPath().endsWith(File.separator) && !target.getPath().
                endsWith("\\")) {
            // remove final path separator
            result.delete(result.length() - "/".length(), result.length());
        }
        return new File(result.toString());
    }
}
