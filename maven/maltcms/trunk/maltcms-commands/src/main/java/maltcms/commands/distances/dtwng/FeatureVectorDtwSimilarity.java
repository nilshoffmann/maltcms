package maltcms.commands.distances.dtwng;

import lombok.Data;
import maltcms.commands.distances.IDtwScoreFunction;
import maltcms.datastructures.array.IFeatureVector;
import maltcms.math.functions.DtwTimePenalizedPairwiseSimilarity;

@Data
public class FeatureVectorDtwSimilarity extends TwoFeatureVectorOperation {

    private IDtwScoreFunction scoreFunction = new DtwTimePenalizedPairwiseSimilarity();
    private String arrayFeatureName = "intensity_values";
    private String timeFeatureName = "scan_acquisition_time";

    @Override
    public double apply(IFeatureVector f1, IFeatureVector f2) {
        return scoreFunction.apply(0, 0, f1.getFeature(timeFeatureName).
                getDouble(0), f2.getFeature(timeFeatureName).getDouble(0), f1.
                getFeature(arrayFeatureName), f2.getFeature(arrayFeatureName));
    }

    @Override
    public boolean isMinimize() {
        return scoreFunction.minimize();
    }
}
