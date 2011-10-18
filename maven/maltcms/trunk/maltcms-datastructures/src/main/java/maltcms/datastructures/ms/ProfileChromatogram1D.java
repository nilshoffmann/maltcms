package maltcms.datastructures.ms;

import java.util.List;

import maltcms.tools.MaltcmsTools;
import ucar.ma2.Array;
import cross.datastructures.fragments.IFileFragment;

public class ProfileChromatogram1D extends Chromatogram1D {

    /**
     * @param e
     */
    public ProfileChromatogram1D(IFileFragment e) {
        super(e);
    }

    public List<Array> getBinnedIntensities() {
        return MaltcmsTools.getBinnedMZIs(getParent()).getSecond();
    }

    public List<Array> getBinnedMasses() {
        return MaltcmsTools.getBinnedMZIs(getParent()).getFirst();
    }
}
