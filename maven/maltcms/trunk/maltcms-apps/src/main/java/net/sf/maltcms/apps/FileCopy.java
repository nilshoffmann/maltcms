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
package net.sf.maltcms.apps;

import net.sf.maltcms.apps.Maltcms;
import java.io.File;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import cross.Factory;
import cross.Logging;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FileTools;

/**
 * Copy the named Variables given on the command line from a source file to an
 * output file in a different directory.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class FileCopy {

    public static void main(final String[] args) {
        final Logger log = Logging.getLogger(FileCopy.class);
        final Maltcms m = Maltcms.getInstance();
        log.info("Starting Maltcms");
        Factory.getInstance().configure(m.parseCommandLine(args));
        log.info("Configured ArrayFactory");
        final TupleND<IFileFragment> t = Factory.getInstance().
                getInputDataFactory().prepareInputData(Factory.getInstance().
                getConfiguration().getStringArray("input.dataInfo"));
        final Date d = new Date();
        for (final IFileFragment f : t) {
            log.info("Reading defined Variables: {}", f.getAbsolutePath());
            final IFileFragment al = f;
            // File target = new File(ArrayFactory.getConfiguration().getString(
            // "output.basedir"), al.getName());
            final IFileFragment fcopy = Factory.getInstance().
                    getFileFragmentFactory().create(
                    new File(FileTools.prependDefaultDirsWithPrefix("",
                    FileCopy.class, d), al.getName()));
            fcopy.addSourceFile(al);
            for (final IVariableFragment vf : al) {
                log.info("Retrieving Variable {}", vf);
                final IVariableFragment toplevel = fcopy.getChild(
                        vf.getVarname());
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
