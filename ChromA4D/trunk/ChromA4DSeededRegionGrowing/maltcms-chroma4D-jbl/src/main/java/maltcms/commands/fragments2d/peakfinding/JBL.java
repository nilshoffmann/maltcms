package maltcms.commands.fragments2d.peakfinding;

import cross.Factory;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import java.awt.Point;
import java.util.logging.Level;
import java.util.logging.Logger;
import maltcms.datastructures.caches.IScanLine;
import maltcms.datastructures.caches.ScanLineCacheFactory;
import maltcms.datastructures.ms.ChromatogramFactory;
import maltcms.datastructures.ms.IChromatogram2D;
import maltcms.datastructures.ms.Scan2D;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;

/**
 *
 * @author nilshoffmann
 * @author jbrezmes
 */
//annotation for variables, which are required for this command
//to be able to run
@RequiresVariables(names = { "var.total_intensity", "var.scan_rate",
		"var.modulation_time", "var.second_column_scan_index",
		"var.scan_acquisition_time_1d","var.peak_index_list"})
//annotation for variable, which are provided/created by this command
//currently left empty
@ProvidesVariables(names = { })
public class JBL extends AFragmentCommand{

    @Override
    public String getDescription() {
        //return a String description of what the command does
        //e.g. return "Calculates significance values for peaks"
        return "No description yet";
    }

    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> in) {
        //initialize working fragments, one for each input file
        TupleND<IFileFragment> out = createWorkFragments(in);
        //loop over each IFileFragment
        for(int i = 0; i<in.size(); i++) {
            //do something
            IFileFragment fin = in.get(i); 
            //retrieve mass spectra
            final IScanLine slc = ScanLineCacheFactory.getDefaultScanLineCache(fin);
            //get the number of mass spectra per modulation (height)
            int spm = slc.getScansPerModulation();
            //get the number of modulations (width)
            int nmod = slc.getScanLineCount();
            //get the number of total (1D) mass spectra
            int nms = fin.getChild("scan_index").getDimensions()[0].getLength();
            //map an arbitrary 1D index within [0,..,nms-1] to the 2D chromatogram
            Point p = slc.mapIndex(300); 
            //retrieve raw mass spectrum at point p
            Tuple2D<Array,Array> rawMs = slc.getSparseMassSpectra(p); 
            LoggerFactory.getLogger(getClass()).info("Retrieved mass spectrum at {}={}",p,rawMs);
            //retrieve masses
            Array masses = rawMs.getFirst();
            Array intensities = rawMs.getSecond();
            //converting Array to e.g. double[]
            double[] mda = (double[])masses.get1DJavaArray(double.class);
            
            //alternatively, you may use Chromatogram2D
            ChromatogramFactory cf = Factory.getInstance().getObjectFactory().instantiate(ChromatogramFactory.class);
            IChromatogram2D ic2d = cf.createChromatogram2D(fin);
            //warning! this reads in ALL mass spectra
            int tic = 0;
            for(Scan2D s2:ic2d) {
                tic+=s2.getTotalIntensity();
            }
            
            //or use raw access
            IVariableFragment ticvar = fin.getChild("total_intensity");
            try {
                ticvar.setRange(new Range[]{new Range(50, 250)});
            } catch (InvalidRangeException ex) {
                LoggerFactory.getLogger(getClass()).error("Invalid range!",ex);
            }
            //if range setting worked, this will return only a slice of 
            //the data. you can safely work on this array, it is a copy 
            //of the data array
            Array tic1D = ticvar.getArray();
            
            Array binnedMsIntensities = slc.getMassSpectra(p); 
            
            
            //create a new IVariableFragment
            IVariableFragment myVar = new VariableFragment(out.get(i), "myVariableName");
            //set array data
            myVar.setArray(tic1D); 
            //Adding e.g. processing results
            out.get(i).addChildren(myVar); 
            
            
            //save data
            out.get(i).save();
        }
        return out;
    }

    @Override
    public WorkflowSlot getWorkflowSlot() {
        //this is just an example and is simply to give users 
        //a category to organnize this command 
        return WorkflowSlot.PEAKFINDING;
    }

}
