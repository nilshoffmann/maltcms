/*
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
 * 
 * $Id: FileTools.java 141 2010-07-13 11:32:21Z nilshoffmann $
 */
package cross.datastructures.tools;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;

import cross.Factory;
import cross.Logging;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.tools.StringTools;
import java.util.regex.Pattern;

/**
 * Utility class to ease handling of files and directories.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class FileTools {

    private static Logger log = Logging.getLogger(FileTools.class);
    public static final SimpleDateFormat sdf = new SimpleDateFormat(
            "MM-dd-yyyy_HH-mm-ss", Locale.US);
    
    public static File inputBasedirectory = new File(".");
    public static File outputBasedirectory = new File(".");
    public static boolean omitUserTimePrefix = false;
    public static boolean overwrite = false;

    private static File appendCreatorNameToBaseDir(final File base,
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

    protected static File checkFileReadable(final IFileFragment ff)
            throws IOException {
        FileTools.log.debug("Trying to locate {}", ff.getName());
        final File outF = new File(ff.getAbsolutePath()).getCanonicalFile();
        if (outF.exists()) {
            FileTools.log.debug("Found {} in directory {}", ff.getName(), outF.getParent());
            // knownFiles.put(outF.getAbsolutePath(), outF);
            return outF;
        } else {
            throw new IOException("File does not exist: "
                    + outF.getAbsolutePath());
        }
    }

    protected static File createFile(final IFileFragment f) throws IOException {
        File file = null;
        FileTools.log.debug("File extension: {}", StringTools.getFileExtension(f.getAbsolutePath()));
        IFileFragment ff = f;
        try {
            file = FileTools.findFile(f);
            FileTools.log.info("File exists, checking, whether we should overwrite or create new file in temporary location!");
            if (Factory.getInstance().getConfiguration().getBoolean(
                    "output.overwrite")) {
                FileTools.log.info("Option output.overwrite=true in default.properties is set, overwriting existing file!");
                file.delete();
                file = new File(file.getAbsolutePath());
            } else {
                FileTools.log.info(
                        "File {} already exists, creating file in temporary location!",
                        f.getAbsolutePath());
                final String tmpdir = System.getProperty("java.io.tmpdir");
                final File tmp = new File(tmpdir);
                file = new File(tmp, file.getName());
                FileTools.log.debug("Setting {} as source file of {}", f.getAbsolutePath(), file.getAbsolutePath());
                ff = Factory.getInstance().getFileFragmentFactory().create(file);
                ff.addSourceFile(f);
            }

            // if(file!=null) {
            // ff.setFile(file.getAbsolutePath());
            // f.setFile(file.getAbsolutePath());
        } catch (final IOException ioex) {
            FileTools.log.debug(ioex.getLocalizedMessage());
            // create the file and it's parent directories atomically
            FileTools.log.debug("File does not exist, creating atomically!");
            file = new File(f.getAbsolutePath());
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        // }
        return file;
    }

    protected static File findFile(final IFileFragment f) throws IOException {
        try {
            final File outF = FileTools.checkFileReadable(f);
            return outF;
        } catch (final IOException ioex) {
            throw ioex;
        }
    }

    protected static File findFile(final IVariableFragment vf)
            throws IOException {
        return FileTools.findFile(vf.getParent());
    }

    public static File getDefaultDirs(final Date d) {
        final boolean omitUserTimePrefix = Factory.getInstance().getConfiguration().getBoolean("omitUserTimePrefix", false);
        if (omitUserTimePrefix) {
            final File basedir = new File(Factory.getInstance().getConfiguration().getString("output.basedir"));
            return basedir;
        } else if (d == null) {
            final File usernamebasedir = new File(Factory.getInstance().getConfiguration().getString("output.basedir", ""),
                    Factory.getInstance().getConfiguration().getString(
                    "user.name", "default"));
            return usernamebasedir;
        } else {
            final File basedir = new File(Factory.getInstance().getConfiguration().getString("output.basedir", ""),
                    Factory.getInstance().getConfiguration().getString(
                    "user.name", "default"));
            final File datedir = new File(basedir, FileTools.sdf.format(d));
            return datedir;
        }
    }

    public static String getDirname(final String fullname) {
        final File f = new File(fullname);
        return f.getParent();
    }

    public static File getFile(final IFileFragment ff) throws IOException {
        return FileTools.findFile(ff);
    }

    public static String getFilename(final String fullname) {
        final File f = new File(fullname);
        return f.getName();
    }

    private static File getNextFreeFileName(final File file) {
        File f = file;
        int i = 1;
        if (Factory.getInstance().getConfiguration().getBoolean(
                "output.overwrite", false)) {
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

    private static File getNextFreeFileName(final String filename) {
        return FileTools.getNextFreeFileName(new File(filename));
    }

    public static File prepareOutput(final IFileFragment parent)
            throws IOException {
        // FileTools.log.debug("Saving file to directory: "
        // + FileTools.getDirname(parent.getAbsolutePath()));
        final File f = FileTools.createFile(parent);
        FileTools.log.debug("Writing to file " + f.getAbsolutePath() + "\n");
        return f;
    }

    public static File prepareOutput(final String dir, final String filename) {
        return prepareOutput(dir, filename, "csv");
    }

    public static File prepareOutput(final String dir, final String filename,
            final String filetypeSuffix) {
        final String basedir = ((dir == null) || dir.isEmpty()) ? Factory.getInstance().getConfiguration().getString("output.basedir")
                : dir;
        final File d = new File(basedir);
        if (!d.exists()) {
            d.mkdirs();
        }

        File f = new File(d, filename + "." + filetypeSuffix);
        f = getNextFreeFileName(f);
        return f;
    }

    public static File prepareOutput(final File dir, final String filename) {
        return prepareOutput(dir.getAbsolutePath(), filename);
    }

    public static File prependDefaultDirsWithPrefix(String prefix,
            final Class<?> creator, final Date d) {
        return FileTools.appendCreatorNameToBaseDir(
                FileTools.getDefaultDirs(d), prefix, creator);
    }

    public static String resolveRelativeFile(File base, File relativeFile) throws IOException {
        return new File(base, relativeFile.getPath()).getCanonicalPath();
    }

    public static File getRelativeFile(IFileFragment target, IFileFragment base) throws IOException {
        return getRelativeFile(new File(target.getAbsolutePath()), new File(base.getAbsolutePath()).getParentFile());
    }

    /**
     * Returns the path of one File relative to another.
     *
     * @param target the target directory
     * @param base the base directory
     * @return target's path relative to the base directory
     * @throws IOException if an error occurs while resolving the files' canonical names
     */
    public static File getRelativeFile(File target, File base) throws IOException {
        String[] baseComponents = base.getCanonicalPath().split(Pattern.quote(File.separator));
        String[] targetComponents = target.getCanonicalPath().split(Pattern.quote(File.separator));

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
                result.append(".." + File.separator);
            }
        }
        for (; index < targetComponents.length; ++index) {
            result.append(targetComponents[index] + File.separator);
        }
        if (!target.getPath().endsWith(File.separator) && !target.getPath().endsWith("\\")) {
            // remove final path separator
            result.delete(result.length() - "/".length(), result.length());
        }
        return new File(result.toString());
    }
    // public static File prependDefaultDirs(final Class<?> creator, final Date
    // d) {
    // return FileTools.appendCreatorNameToBaseDir(
    // FileTools.getDefaultDirs(d), "", creator);
    // }
    // public static File prependDefaultDirs(final String filename,
    // final Class<?> creator, final Date d) {
    // return FileTools.getNextFreeFileName(new File(FileTools
    // .prependDefaultDirs(creator, d), filename));
    // }
}
