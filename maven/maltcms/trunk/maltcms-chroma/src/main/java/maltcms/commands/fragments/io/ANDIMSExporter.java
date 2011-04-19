/**
 * 
 */
package maltcms.commands.fragments.io;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.LoggerFactory;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.MAMath;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import cross.Factory;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ConstraintViolationException;
import cross.exception.ResourceNotAvailableException;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class ANDIMSExporter extends AFragmentCommand {

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
			try {
				Factory.getInstance().getDataSourceFactory()
				        .getDataSourceFor(f).readStructure(f);
			} catch (final IOException e) {
				throw new RuntimeException(e.fillInStackTrace());
			}
			IFileFragment outf = Factory
			        .getInstance()
			        .getFileFragmentFactory()
			        .create(new File(getIWorkflow().getOutputDirectory(this), f
			                .getName()));
			List<IFileFragment> deepestAncestors = cross.datastructures.tools.FragmentTools
			        .getDeepestAncestor(f);
			if (deepestAncestors.size() > 1) {
				throw new ConstraintViolationException("Found "
				        + deepestAncestors.size() + " possible roots for "
				        + f.getAbsolutePath() + ". Maximum is 1! Roots are: "
				        + deepestAncestors);
			} else if (deepestAncestors.isEmpty()) {
				deepestAncestors.add(f);
			}
			try {
				Factory.getInstance().getDataSourceFactory()
				        .getDataSourceFor(deepestAncestors.get(0))
				        .readStructure(deepestAncestors.get(0));
			} catch (IOException e) {
				throw new RuntimeException(e.fillInStackTrace());
			}
			List<Attribute> lattributes = deepestAncestors.get(0)
			        .getAttributes();
			int scans = f.getChild("scan_index").getArray().getShape()[0];
			int points = f.getChild("mass_values", true).getArray().getShape()[0];

			Dimension scan_number = new Dimension("scan_number", scans, true);

			Dimension point_number = new Dimension("point_number", points, true);
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

			copyData(f, outf, "mass_values", double.class, point_number);
			copyData(f, outf, "intensity_values", double.class, point_number);
			copyData(f, outf, "scan_index", int.class, scan_number);
			copyData(f, outf, "scan_acquisition_time", double.class,
			        scan_number);
			copyData(f, outf, "total_intensity", int.class, scan_number);
			copyData(f, outf, "mass_range_min", double.class, scan_number);
			copyData(f, outf, "mass_range_max", double.class, scan_number);
			copyData(f, outf, "point_count", int.class, scan_number);
			copyData(f, outf, "flag_count", int.class, scan_number);
			copyData(f, outf, "error_log", char.class, error_number,
			        _64_byte_string);
			copyData(f, outf, "a_d_sampling_rate", double.class, scan_number);
			copyData(f, outf, "scan_duration", double.class, scan_number);
			copyData(f, outf, "resolution", double.class, scan_number);

			addGlobalAttributes(outf, lattributes);

			outf.save();
			DefaultWorkflowResult dwr = new DefaultWorkflowResult(new File(
			        outf.getAbsolutePath()), this, getWorkflowSlot(), outf);
			getIWorkflow().append(dwr);
		}
		return t;
	}

	/**
	 * @param outf
	 */
	private void addGlobalAttributes(IFileFragment outf,
	        List<Attribute> attributes) {
		outf.setAttributes(attributes.toArray(new Attribute[attributes.size()]));
//		int numberOfScans = outf.getChild("total_intensity").getArray()
//		        .getShape()[0];
//		String massFormat = outf.getChild("mass_values").getDataType()
//		        .toString();
//		String intensFormat = outf.getChild("intensity_values").getDataType()
//		        .toString();
//		Array sat = outf.getChild("scan_acquisition_time").getArray();
//		Index sati = sat.getIndex();
//		double timeOfFirstScan = sat.getDouble(sati.set(0));
//		double timeOfLastScan = sat.getDouble(sati.set(numberOfScans - 1));
//		double minMass = MAMath.getMinimum(outf.getChild("mass_range_min")
//		        .getArray());
//		double maxMass = MAMath.getMaximum(outf.getChild("mass_range_max")
//		        .getArray());
//
//		// :dataset_completeness = "C1+C2" ;
//		Attribute mtr = new Attribute("ms_template_revision", "1.0.1");
//		// :administrative_comments = "" ;
//		// :dataset_owner = "" ;
//		// :experiment_title = "" ;
//		// :experiment_date_time_stamp = "20070313053747+0100" ;
//		// :netcdf_file_date_time_stamp = "20070305161532+0000" ;
//		// :experiment_type = "Centroided Mass Spectrum" ;
//		Attribute netcdfRevision = new Attribute("netcdf_revision", "2.3.2");
//		// :netcdf_revision = "2.3.2" ;
//		// :operator_name = "operator" ;
//		// :source_file_reference = "C:\\Xcalibur\\Data\\$$$tempsource.raw" ;
//		// :source_file_date_time_stamp = "20070313053747+0100" ;
//		// :source_file_format = "Finnigan" ;
//		// :languages = "English" ;
//		// :external_file_ref_0 = "" ;
//		// :instrument_number = 1 ;
//		// :sample_prep_comments = "" ;
//		// :sample_comments = "" ;
//		// :test_separation_type = "" ;
//		// :test_ms_inlet = "" ;
//		// :test_ionization_mode = " " ;
//		// :test_ionization_polarity = "Positive Polarity" ;
//		// :test_detector_type = "Conversion Dynode Electron Multiplier" ;
//		// :test_scan_function = "Mass Scan" ;
//		// :test_scan_direction = "" ;
//		// :test_scan_law = "Linear" ;
//		Attribute nos = new Attribute("number_of_scans", numberOfScans);
//		Attribute rdmf = new Attribute("raw_data_mass_format", massFormat);
//		Attribute rdif = new Attribute("raw_data_intensity_format",
//		        intensFormat);
//		Attribute art = new Attribute("actual_run_time", timeOfLastScan
//		        - timeOfFirstScan);
//		Attribute adt = new Attribute("actual_delay_time", timeOfFirstScan);
//		Attribute gmmin = new Attribute("global_mass_min", minMass);
//		Attribute gmmax = new Attribute("global_mass_max", maxMass);
//		// :calibrated_mass_min = 0. ;
//		// :calibrated_mass_max = 0. ;
//		Attribute mal = new Attribute("mass_axis_label", "M/Z");
//		Attribute ial = new Attribute("intensity_axis_label", "Abundance");
//		outf.setAttributes(netcdfRevision, mtr, nos, rdmf, rdif, art, adt,
//		        gmmin, gmmax, mal, ial);

	}

	private void copyData(IFileFragment source, IFileFragment target,
	        String varname, Class<?> elementType, Dimension... d) {
		IVariableFragment targetV = new VariableFragment(target, varname);
		int[] shape = new int[d.length];
		int i = 0;
		for (Dimension dim : d) {
			System.out.println("Checking dimension: " + dim);
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
			        new Attribute[] {}));
			targetV.setDimensions(d);
		} catch (ResourceNotAvailableException r) {
			Array a = Array.factory(elementType, shape);
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
	@Override
	public WorkflowSlot getWorkflowSlot() {
		return WorkflowSlot.FILECONVERSION;
	}

}
