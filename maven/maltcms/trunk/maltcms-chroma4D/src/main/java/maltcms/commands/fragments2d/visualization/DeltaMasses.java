/*
 * Copyright (C) 2009, 2010 Mathias Wilhelm mwilhelm A T
 * TechFak.Uni-Bielefeld.DE
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
 * $Id: DeltaMasses.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */
package maltcms.commands.fragments2d.visualization;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import maltcms.io.csv.ColorRampReader;
import maltcms.tools.ArrayTools2;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.IndexIterator;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.lookup.ServiceProvider;

/**
 * Computes relative deltamasses.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
@Slf4j
@Data
@RequiresVariables(names = {""})
@RequiresOptionalVariables(names = {""})
@ProvidesVariables(names = {""})
@ServiceProvider(service=AFragmentCommand.class)
public class DeltaMasses extends AFragmentCommand {

    private final String colorrampLocation = "res/colorRamps/bcgyr.csv";
    private final double lowThreshold = 0.0d;

    @Override
    public String toString() {
        return getClass().getName();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        for (final IFileFragment ff : t) {
            final List<Integer> sdd = new ArrayList<Integer>();
            final Array sda = ff.getChild("v_mass_values").getArray();
            final IndexIterator sdaiter = sda.getIndexIterator();
            while (sdaiter.hasNext()) {
                sdd.add(sdaiter.getIntNext());
            }

            final IVariableFragment meanH = ff.getChild("meanms_1d_horizontal");
            meanH.setIndex(ff.getChild("meanms_1d_horizontal_index"));
            final List<Array> aaH = meanH.getIndexedArray();
            aaH.remove(aaH.size() - 1);

            int dm, dmM;
            final List<Array> dmList = new ArrayList<Array>();
            for (final Array ms : aaH) {
                final Map<Integer, Integer> dmm = new HashMap<Integer, Integer>();
                final ArrayDouble.D1 msD = (ArrayDouble.D1) ms;
                for (final Integer ma1 : sdd) {
                    for (final Integer ma2 : sdd) {
                        dm = (int) Math.abs(msD.get(ma1) - msD.get(ma2));
                        dmM = Math.abs(ma1 - ma2);
                        if (dmm.containsKey(dmM)) {
                            dmm.put(dmM, dmm.get(dmM) + dm);
                        } else {
                            dmm.put(dmM, dm);
                        }
                    }
                }
                final Array a = getArray(dmm, aaH.get(0).getShape()[0]);
                dmList.add(a);
            }
            // final Array meanDM = ArrayTools.mean(dmList);
            final List<Array> picList = new ArrayList<Array>();
            for (final Array a : dmList) {
                // final Array ab = ArrayTools.sum(a, meanDM);
                // final IndexIterator iter = ab.getIndexIterator();
                // while (iter.hasNext()) {
                // iter.getDoubleNext();
                // if (iter.getDoubleCurrent() < 0.0d) {
                // iter.setDoubleCurrent(0);
                // }
                // }
                for (int i = 0; i < 5; i++) {
                    picList.add(a);
                }
            }

            final String file = StringTools.removeFileExt(ff.getName());
            final ColorRampReader crr = new ColorRampReader();
            final int[][] colorRamp = crr.readColorRamp(this.colorrampLocation);
            createImage(file + "_dm", "delta masses", picList, colorRamp,
                    this.lowThreshold, this.getClass(), this);
            createImage(file + "_dm_sqr", "delta masses", ArrayTools2.sqrt(
                    picList), colorRamp, this.lowThreshold, this.getClass(),
                    this);

        }
        return t;
    }

    /**
     * Creates an image of a list of arrays.
     * 
     * @param filename
     *            filename
     * @param title
     *            title
     * @param aa
     *            list of arrays
     * @param colorRamp
     *            color ramp
     * @param lowThreshold
     *            threshold
     * @param creator
     *            creator class
     * @param elem
     *            workflow element
     */
    private void createImage(final String filename, final String title,
            final List<Array> aa, final int[][] colorRamp,
            final double lowThreshold, final Class<?> creator,
            final IWorkflowElement elem) {
        final BufferedImage bi2 = maltcms.tools.ImageTools.fullSpectrum(title,
                aa, aa.get(0).getShape()[0], colorRamp, 1024, true,
                lowThreshold);
        maltcms.tools.ImageTools.saveImage(bi2, filename, "png", getWorkflow().
                getOutputDirectory(this), elem);
    }

    /**
     * Creates a dense array.
     * 
     * @param dma
     *            delta masses
     * @param maxSize
     *            max size
     * @return new array
     */
    private Array getArray(final Map<Integer, Integer> dma, final int maxSize) {
        final ArrayDouble.D1 a = new ArrayDouble.D1(maxSize);
        for (final Integer i : dma.keySet()) {
            a.set(i, dma.get(i));
        }
        return a;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Visualization of delta masses";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.STATISTICS;
    }
}
