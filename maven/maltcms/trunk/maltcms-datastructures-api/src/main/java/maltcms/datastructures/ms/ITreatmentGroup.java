package maltcms.datastructures.ms;

import java.util.List;

public interface ITreatmentGroup<T extends IChromatogram> {

    public List<T> getChromatograms();

    public void addChromatogram(T t);

    public void setName(String name);

    public String getName();
}
