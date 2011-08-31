package maltcms.experimental.operations;

import java.awt.Point;
import java.util.List;

public class IdentityNodeBuilder extends NodeBuilder {

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.experimental.operations.DTW.NodeBuilder#eval(java.util.List)
     */
    @Override
    public List<Point> eval(List<double[]> points, int polyOrder) {
        setNodes(points);
        return getPointList(getNodes());
    }
}
