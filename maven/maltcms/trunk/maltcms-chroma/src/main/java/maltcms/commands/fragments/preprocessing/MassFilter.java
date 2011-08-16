/**
 * 
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
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import cross.Factory;
import cross.Logging;
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

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
@ProvidesVariables(names = {"var.mass_values", "var.intensity_values",
    "var.total_intensity"})
@RequiresVariables(names = {"var.mass_values", "var.intensity_values",
    "var.scan_index", "var.total_intensity"})
@RequiresOptionalVariables(names = {"var.excluded_masses"})
public class MassFilter extends AFragmentCommand {

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
    private Logger log = Logging.getLogger(this);

    @Override
    public void configure(Configuration cfg) {
        super.configure(cfg);
        this.epsilon = cfg.getDouble(this.getClass().getName()
                + ".mass_epsilon", 0.1d);
        this.excludeMasses = StringTools.toStringList(cfg.getList(this.getClass().
                getName()
                + ".excludeMasses", Collections.emptyList()));
        this.massValuesVar = cfg.getString("var.mass_values", "mass_values");
        this.intensValuesVar = cfg.getString("var.intensity_values",
                "intensity_values");
        this.scanIndexVar = cfg.getString("var.scan_index", "scan_index");
        this.totalIntensVar = cfg.getString("var.total_intensity",
                "total_intensity");
        this.invert = cfg.getBoolean(this.getClass().getName() + ".invert",
                false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.commands.fragments.AFragmentCommand#getDescription()
     */
    @Override
    public String getDescription() {
        return "Removes defined masses and associated intensities from chromatogram.";
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.fragments.AFragmentCommand#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Removes defined masses and associated intensities from a raw chromatogram.";
	}

        // return if we have nothing to do
        // if (exclMass.isEmpty()) {
        // return t;
        // }

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
                this.log.warn(
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
                this.log.info("Removing the following masses: {}", exclMass);
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
				double[] em = (double[]) iff.getChild("excluded_masses")
				        .getArray().get1DJavaArray(double.class);
				for (double d : em) {
					exclMassSet.add(d);
				}
			} catch (ResourceNotAvailableException r) {
				log
				        .warn("Could not load excluded_masses from previous file!");
			}
			// create a new FileFragment to hold processed data
			final IFileFragment retf = Factory.getInstance()
			        .getFileFragmentFactory().create(
			                new File(getWorkflow().getOutputDirectory(this),
			                        iff.getName()), iff);
			if (!exclMassSet.isEmpty()) {
				final List<Double> exclMass = new ArrayList<Double>(exclMassSet);
				// Collections.sort(exclMass);
				log.info("Removing the following masses: {}", exclMass);
				// retrieve original variables
				final IVariableFragment massesV = iff
				        .getChild(this.massValuesVar);
				final IVariableFragment intensV = iff
				        .getChild(this.intensValuesVar);
				final IVariableFragment scanV = iff.getChild(this.scanIndexVar);
				scanV.setRange(null);
				scanV.getArray();
				final IVariableFragment ticV = iff
				        .getChild(this.totalIntensVar);
				// set index
				massesV.setIndex(scanV);
				intensV.setIndex(scanV);
				// get number of scans
				final int scans = MaltcmsTools.getNumberOfScans(iff);

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

    /*
     * (non-Javadoc)
     * 
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.GENERAL_PREPROCESSING;
    }
}
