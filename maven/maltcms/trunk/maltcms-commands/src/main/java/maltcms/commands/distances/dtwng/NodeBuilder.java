package maltcms.commands.distances.dtwng;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class NodeBuilder {

    private List<double[]> nodes = Collections.emptyList();

    public void setNodes(List<double[]> nodes) {
        this.nodes = nodes;
    }

    public List<double[]> getNodes() {
        return nodes;
    }

    public abstract List<Point> eval(List<double[]> points, int polyOrder);

    public double[] getNodesByDimension(int i, double[] dim) {
        double[] dimt = dim == null ? new double[nodes.size()] : dim;
        for (double[] d : nodes) {
            dimt[i] = d[i];
        }
        return dimt;
    }

    public List<Point> getPointList(List<double[]> points) {
        List<Point> l = new ArrayList<Point>();
        for (double[] d : points) {
            l.add(new Point((int) d[0], (int) d[1]));
        }
        return l;
    }
}
