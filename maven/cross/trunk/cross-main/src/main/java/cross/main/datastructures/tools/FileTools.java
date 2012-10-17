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
package cross.main.datastructures.tools;

import cross.main.Factory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.tools.StringTools;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class to ease handling of files and directories.
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
public class FileTools {

    public static final SimpleDateFormat sdf = new SimpleDateFormat(
            "MM-dd-yyyy_HH-mm-ss", Locale.US);

//    public static File inputBasedirectory = new File(".");
//    public static File outputBasedirectory = new File(".");
//    public static boolean omitUserTimePrefix = false;
//    public static boolean overwrite = false;
    public static void deleteDirectory(File directory) {
    }

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

    protected static File createFile(final IFileFragment f) throws IOException {
        File file = null;
        log.debug("File extension: {}", StringTools.getFileExtension(f.getAbsolutePath()));
        IFileFragment ff = f;
        try {
            file = FileTools.findFile(f);
            log.info("File exists, checking, whether we should overwrite or create new file in temporary location!");
            if (Factory.getInstance().getConfiguration().getBoolean(
                    "output.overwrite")) {
                log.info("Option output.overwrite=true in default.properties is set, overwriting existing file!");
                file.delete();
                file = new File(file.getAbsolutePath());
            } else {
                log.info(
                        "File {} already exists, creating file in temporary location!",
                        f.getAbsolutePath());
                final String tmpdir = System.getProperty("java.io.tmpdir");
                final File tmp = new File(tmpdir);
                file = new File(tmp, file.getName());
                log.debug("Setting {} as source file of {}", f.getAbsolutePath(), file.getAbsolutePath());
                ff = Factory.getInstance().getFileFragmentFactory().create(file);
                ff.addSourceFile(f);
            }

            // if(file!=null) {
            // ff.setFile(file.getAbsolutePath());
            // f.setFile(file.getAbsolutePath());
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

    public static File getDefaultDirs(final File baseDirectory, final Date d) {
        File outputBasedir = new File(Factory.getInstance().getConfiguration().getString("output.basedir", ""));
        if (baseDirectory != null) {
            outputBasedir = baseDirectory;
        }
        final boolean omitUserTimePrefix = Factory.getInstance().getConfiguration().getBoolean("omitUserTimePrefix", false);
        if (omitUserTimePrefix) {
            return outputBasedir;
        } else if (d == null) {
            final File usernamebasedir = new File(outputBasedir,
                    Factory.getInstance().getConfiguration().getString(
                    "user.name", "default"));
            return usernamebasedir;
        } else {
            final File basedir = new File(outputBasedir,
                    Factory.getInstance().getConfiguration().getString(
                    "user.name", "default"));
            final File datedir = new File(basedir, FileTools.sdf.format(d));
            return datedir;
        }
    }

    public static File getDefaultDirs(final Date d) {
        return getDefaultDirs(null, d);
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
        // log.debug("Saving file to directory: "
        // + FileTools.getDirname(parent.getAbsolutePath()));
        final File f = FileTools.createFile(parent);
        log.debug("Writing to file " + f.getAbsolutePath() + "\n");
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

    public static File prependDefaultDirsWithPrefix(File baseDir, String prefix, final Class<?> creator, final Date d) {
        return FileTools.appendCreatorNameToBaseDir(
                FileTools.getDefaultDirs(baseDir, d), prefix, creator);
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
     * @throws IOException if an error occurs while resolving the files'
     * canonical names
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
