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
package maltcms.commands.fragments.io;

import java.io.File;
import java.io.IOException;
import java.util.List;


import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import cross.Factory;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tools.FragmentTools;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ConstraintViolationException;
import cross.exception.ResourceNotAvailableException;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 *
 *
 */
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class ANDIMSExporter extends AFragmentCommand {

    private boolean skipAggregatedVariables = true;

    @Override
    public String toString() {
        return getClass().getName();
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.commands.fragments.AFragmentCommand#getDescription()
     */
    @Override
    public String getDescription() {
        return "Creates ANDIMS compliant output.";
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.commands.ICommand#apply(java.lang.Object)
     */
    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
        for (IFileFragment f : t) {
//            try {
//                Factory.getInstance().getDataSourceFactory().getDataSourceFor(f).
//                        readStructure(f);
//            } catch (final IOException e) {
//                throw new RuntimeException(e.fillInStackTrace());
//            }
            log.info("Processing {}", f.getName());
            IFileFragment outf = Factory.getInstance().getFileFragmentFactory().
                    create(new File(getWorkflow().getOutputDirectory(this), f.
                    getName()));
            List<IFileFragment> deepestAncestors = cross.datastructures.tools.FragmentTools.
                    getDeepestAncestor(f);
            if (deepestAncestors.size() > 1) {
                throw new ConstraintViolationException("Found "
                        + deepestAncestors.size() + " possible roots for "
                        + f.getAbsolutePath() + ". Maximum is 1! Roots are: "
                        + deepestAncestors);
            } else if (deepestAncestors.isEmpty()) {
                deepestAncestors.add(f);
            }
            try {
                Factory.getInstance().getDataSourceFactory().getDataSourceFor(deepestAncestors.
                        get(0)).readStructure(deepestAncestors.get(0));
            } catch (IOException e) {
                throw new RuntimeException(e.fillInStackTrace());
            }
            //FIXME amalgamate attributes over all ancestors???
            List<Attribute> lattributes = deepestAncestors.get(0).getAttributes();
            int scans = f.getChild("scan_index").getArray().getShape()[0];
            int points = f.getChild("mass_values", true).getArray().getShape()[0];

            Dimension scan_number = new Dimension("scan_number", scans, true);

            Dimension point_number = new Dimension("point_number", points, true,
                    true, false);
            Dimension error_number = new Dimension("error_number", 1, true);
            Dimension instrument_number = new Dimension("instrument_number", 1,
                    true);
            Dimension _2_byte_string = new Dimension("_2_byte_string", 2, true);
            Dimension _4_byte_string = new Dimension("_4_byte_string", 4, true);
            Dimension _8_byte_string = new Dimension("_8_byte_string", 8, true);
            Dimension _16_byte_string = new Dimension("_16_byte_string", 16,
                    true);
            Dimension _32_byte_string = new Dimension("_32_byte_string", 32,
                    true);
            Dimension _64_byte_string = new Dimension("_64_byte_string", 64,
                    true);
            Dimension _80_byte_string = new Dimension("_80_byte_string", 80,
                    true);
            Dimension _128_byte_string = new Dimension("_128_byte_string", 128,
                    true);
            Dimension _255_byte_string = new Dimension("_255_byte_string", 255,
                    true);

            outf.addDimensions(scan_number, point_number, error_number,
                    instrument_number, _2_byte_string, _4_byte_string,
                    _8_byte_string, _16_byte_string, _32_byte_string,
                    _64_byte_string, _80_byte_string, _128_byte_string,
                    _255_byte_string);

            Set<VariableInfo> defaultVariables = new LinkedHashSet<VariableInfo>();
            defaultVariables.add(new VariableInfo("mass_values", double.class,
                    point_number));
            defaultVariables.add(new VariableInfo("intensity_values",
                    double.class, point_number));
            defaultVariables.add(new VariableInfo("scan_index", int.class,
                    scan_number));
            defaultVariables.add(new VariableInfo("scan_acquisition_time",
                    double.class,
                    scan_number));
            defaultVariables.add(new VariableInfo("total_intensity", int.class,
                    scan_number));
            defaultVariables.add(new VariableInfo("mass_range_min", double.class,
                    scan_number));
            defaultVariables.add(new VariableInfo("mass_range_max", double.class,
                    scan_number));
            defaultVariables.add(new VariableInfo("point_count", int.class,
                    scan_number));
            defaultVariables.add(new VariableInfo("flag_count", int.class,
                    scan_number));
            defaultVariables.add(new VariableInfo("error_log", char.class,
                    error_number,
                    _64_byte_string));
            defaultVariables.add(new VariableInfo("a_d_sampling_rate",
                    double.class, scan_number));
            defaultVariables.add(new VariableInfo("scan_duration", double.class,
                    scan_number));
            defaultVariables.add(new VariableInfo("resolution", double.class,
                    scan_number));

            for (VariableInfo vi : defaultVariables) {
                copyData(f, outf, vi.name, vi.elementType, vi.dimensions);
            }

            if (!skipAggregatedVariables) {
                for (IVariableFragment ivf : FragmentTools.getAggregatedVariables(f)) {
                    try {
                        copyData(f, outf, ivf.getName(), ivf.getDataType().
                                getClassType(), ivf.getDimensions());
                    } catch (IllegalArgumentException iae) {
                        log.warn("Exception while trying to add Variable " + ivf.
                                getName() + " to FileFragment " + f.getName(),
                                iae);
                    }
                }
            }

            addGlobalAttributes(outf, lattributes);

            outf.save();
            DefaultWorkflowResult dwr = new DefaultWorkflowResult(new File(
                    outf.getAbsolutePath()), this, getWorkflowSlot(), outf);
            getWorkflow().append(dwr);
        }
        return t;
    }

    class VariableInfo {

        String name;
        Class<?> elementType;
        Dimension[] dimensions;

        VariableInfo(String name, Class<?> elementType, Dimension... dimensions) {
            this.name = name;
            this.elementType = elementType;
            this.dimensions = dimensions;
        }
    }

    /**
     * @param outf
     */
    private void addGlobalAttributes(IFileFragment outf,
            List<Attribute> attributes) {
        outf.setAttributes(attributes.toArray(new Attribute[attributes.size()]));
    }

    private void copyData(IFileFragment source, IFileFragment target,
            String varname, Class<?> elementType, Dimension... d) {
        if (d.length == 0) {
            log.warn("No dimensions given, skipping variable {}", varname);
        }
        IVariableFragment targetV = null;
        if (target.hasChildren(varname)) {
            targetV = target.getChild(varname);
        } else {
            targetV = new VariableFragment(target, varname);
        }
        int[] shape = new int[d.length];
        int i = 0;
        for (Dimension dim : d) {
            log.debug("Checking dimension: {}", dim);
            shape[i] = dim.getLength();
        }
        try {
            IVariableFragment sourceV = source.getChild(varname);
            Array a = sourceV.getArray();
            int[] shape2 = a.getShape();
//			EvalTools.eqI(shape2.length, d.length, this);
//			for (int j = 0; j < shape2.length; j++) {
//				EvalTools.eqI(shape2[j], d[j].getLength(), this);
//			}
            targetV.setArray(a);
            targetV.setAttributes(sourceV.getAttributes().toArray(
                    new Attribute[]{}));
            targetV.setDimensions(d);
        } catch (ResourceNotAvailableException r) {
            Array a;
            if (char.class.equals(elementType)) {
                a = new ArrayChar(shape);
            } else {
                a = Array.factory(elementType, shape);
            }
            targetV.setArray(a);
            targetV.setDimensions(d);
        }
        targetV.setRange(null);
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    /**
     *
     * @return
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.FILECONVERSION;
    }
}
