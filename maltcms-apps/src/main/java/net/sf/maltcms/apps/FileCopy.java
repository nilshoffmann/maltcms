/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
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
package net.sf.maltcms.apps;

import cross.Factory;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.FileFragmentFactory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FileTools;
import cross.datastructures.tuple.TupleND;
import java.io.File;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;

/**
 * Copy the named Variables given on the command line from a source file to an
 * output file in a different directory.
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
public class FileCopy {

    public static void main(final String[] args) {
        final Maltcms m = Maltcms.getInstance();
        log.info("Starting Maltcms");
        Factory.getInstance().configure(m.parseCommandLine(args));
        log.info("Configured ArrayFactory");
        final TupleND<IFileFragment> t = Factory.getInstance().
                getInputDataFactory().prepareInputData(Factory.getInstance().
                        getConfiguration().getStringArray("input.dataInfo"));
        final Date d = new Date();
        for (final IFileFragment f : t) {
            log.info("Reading defined Variables: {}", f.getUri());
            final IFileFragment al = f;
            // File target = new File(ArrayFactory.getConfiguration().getString(
            // "output.basedir"), al.getName());
            final IFileFragment fcopy = new FileFragmentFactory().create(
                    new File(FileTools.prependDefaultDirsWithPrefix("",
                                    FileCopy.class, d), al.getName()));
            fcopy.addSourceFile(al);
            for (final IVariableFragment vf : al) {
                log.info("Retrieving Variable {}", vf);
                final IVariableFragment toplevel = fcopy.getChild(
                        vf.getName());
                if (toplevel.getIndex() != null) {
                    final List<Array> arraysI = toplevel.getIndexedArray();
                    EvalTools.notNull(arraysI, arraysI);
                } else {
                    final Array a = toplevel.getArray();
                    EvalTools.notNull(a, a);
                }
            }
            final IVariableFragment index_fragment = fcopy.getChild(Factory.
                    getInstance().getConfiguration().getString(
                            "var.scan_index", "scan_index"));
            ArrayInt.D1 index_array;
            final Range[] index_range = index_fragment.getRange();
            try {
                index_array = (ArrayInt.D1) index_fragment.getArray().section(
                        Range.toList(index_range));
                if ((index_range != null) && (index_range[0] != null)) {
                    final ArrayInt.D1 new_index = new ArrayInt.D1(index_array.
                            getShape()[0]);
                    for (int i = 0; i < new_index.getShape()[0]; i++) {
                        // log.info("i: {}, index_start: {}, index_end: {}",new
                        // Object[]{i,index_start,index_end});
                        new_index.set(i, index_array.get(i)
                                - index_array.get(0));
                    }
                    index_fragment.setArray(new_index);

                }
            } catch (final InvalidRangeException e) {
                log.error(e.getLocalizedMessage());
            }

            log.info("{}", fcopy.toString());
            log.info("{}", FileFragment.printFragment(fcopy));
            fcopy.removeSourceFiles();
            fcopy.save();
        }
        System.exit(0);
    }
}
