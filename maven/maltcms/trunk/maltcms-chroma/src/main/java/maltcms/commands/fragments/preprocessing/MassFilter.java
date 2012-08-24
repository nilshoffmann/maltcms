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
package maltcms.commands.fragments.preprocessing;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import maltcms.tools.ArrayTools;
import maltcms.tools.MaltcmsTools;

import org.apache.commons.configuration.Configuration;
import java.util.Collections;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import cross.Factory;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowProgressResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import cross.datastructures.tools.EvalTools;
import cross.tools.StringTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
@Slf4j
@Data
@ProvidesVariables(names = {"var.mass_values", "var.intensity_values",
    "var.total_intensity"})
@RequiresVariables(names = {"var.mass_values", "var.intensity_values",
    "var.scan_index", "var.total_intensity"})
@RequiresOptionalVariables(names = {"var.excluded_masses"})
@ServiceProvider(service=AFragmentCommand.class)
public class MassFilter extends AFragmentCommand {

    private final String description = "Removes defined masses and associated intensities from chromatogram.";
    private final WorkflowSlot workflowSlot = WorkflowSlot.GENERAL_PREPROCESSING;
    
    @Configurable
    private List<String> excludeMasses = new LinkedList<String>();
    @Configurable(name = "mass_epsilon")
    private double epsilon = 0.1;
    @Configurable(name = "var.mass_values")
    private String massValuesVar = "mass_values";
    @Configurable(name = "var.intensity_values")
    private String intensValuesVar = "intensity_values";
    @Configurable(name = "var.scan_index")
    private String scanIndexVar = "scan_index";
    @Configurable(name = "var.total_intensity")
    private String totalIntensVar = "total_intensity";
    @Configurable
    private boolean invert = false;
    
    @Override
    public void configure(Configuration cfg) {
        super.configure(cfg);
        this.massValuesVar = cfg.getString("var.mass_values", "mass_values");
        this.intensValuesVar = cfg.getString("var.intensity_values",
                "intensity_values");
        this.scanIndexVar = cfg.getString("var.scan_index", "scan_index");
        this.totalIntensVar = cfg.getString("var.total_intensity",
                "total_intensity");
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.commands.ICommand#apply(java.lang.Object)
     */
    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {

        // create new ProgressResult
        DefaultWorkflowProgressResult dwpr = new DefaultWorkflowProgressResult(
                t.getSize(), this, getWorkflowSlot());
        TupleND<IFileFragment> rett = new TupleND<IFileFragment>();
        for (IFileFragment iff : t) {
            final SortedSet<Double> exclMassSet = new TreeSet<Double>();
            for (String s : this.excludeMasses) {
                if (!s.isEmpty()) {
                    exclMassSet.add(Double.parseDouble(s));
                }
            }
            try {
                double[] em = (double[]) iff.getChild("excluded_masses").
                        getArray().get1DJavaArray(double.class);
                for (double d : em) {
                    exclMassSet.add(d);
                }
            } catch (ResourceNotAvailableException r) {
                log.warn(
                        "Could not load excluded_masses from previous file!");
            }
            // create a new FileFragment to hold processed data
            final IFileFragment retf = Factory.getInstance().
                    getFileFragmentFactory().create(
                    new File(getWorkflow().getOutputDirectory(this),
                    iff.getName()), iff);
            if (!exclMassSet.isEmpty()) {
                final List<Double> exclMass = new ArrayList<Double>(exclMassSet);
                // Collections.sort(exclMass);
                log.info("Removing the following masses: {}", exclMass);
                // retrieve original variables
                final IVariableFragment massesV = iff.getChild(
                        this.massValuesVar);
                final IVariableFragment intensV = iff.getChild(
                        this.intensValuesVar);
                final IVariableFragment scanV = iff.getChild(this.scanIndexVar);
                scanV.setRange(null);
                scanV.getArray();
                final IVariableFragment ticV = iff.getChild(this.totalIntensVar);
                // set index
                massesV.setIndex(scanV);
                intensV.setIndex(scanV);
                // get number of scans
                final int scans = MaltcmsTools.getNumberOfScans(iff);

                // create a new array for the tic
                final ArrayDouble.D1 newTic = new ArrayDouble.D1(scans);
                final ArrayInt.D1 newSidx = new ArrayInt.D1(scans);
                // create lists for mass and intensity values
                final ArrayList<Array> newMassesList = new ArrayList<Array>(
                        scans);
                final ArrayList<Array> newIntensList = new ArrayList<Array>(
                        scans);
                // loop over scans
                int elems = 0;
                for (int i = 0; i < scans; i++) {
                    newSidx.set(i, elems);
                    Array intens = intensV.getIndexedArray().get(i);
                    Array masses = massesV.getIndexedArray().get(i);
                    EvalTools.eqI(intens.getShape()[0], masses.getShape()[0],
                            this);
                    // find masked masses
                    List<Integer> maskedIndices = MaltcmsTools.findMaskedMasses(
                            masses, exclMass, this.epsilon);
                    // filter intensities
                    Array newIntens = null;

                    newIntens = ArrayTools.filterIndices(intens, maskedIndices,
                            this.invert, 0.0d);

                    newIntensList.add(newIntens);
                    // set new tic values
                    newTic.set(i, ArrayTools.integrate(newIntens));

                    newMassesList.add(masses);
                    EvalTools.eqI(newIntens.getShape()[0],
                            masses.getShape()[0], this);
                    elems += newIntens.getShape()[0];
                }
                // create new variables and set arrays/lists of arrays (indexed)
                final IVariableFragment newMassesV = VariableFragment.
                        createCompatible(retf, massesV);
                newMassesV.setIndexedArray(newMassesList);
                final IVariableFragment newIntensV = VariableFragment.
                        createCompatible(retf, intensV);
                newIntensV.setIndexedArray(newIntensList);
                final IVariableFragment newTicV = VariableFragment.
                        createCompatible(retf, ticV);
                newTicV.setArray(newTic);
                final IVariableFragment newSidxV = VariableFragment.
                        createCompatible(retf, scanV);
                newSidxV.setArray(newSidx);
            }
            // save fragment
            retf.save();
            rett.add(retf);
            // notify workflow
            getWorkflow().append(dwpr.nextStep());
        }
        return rett;
    }

}
