/**
 * 
 */
package maltcms.commands.fragments2d.peakfinding;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import maltcms.datastructures.ridge.IRidgeCost;
import maltcms.datastructures.ridge.Ridge;
import maltcms.datastructures.quadTree.QuadTree;
import maltcms.tools.ImageTools;
import maltcms.ui.charts.GradientPaintScale;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.DefaultXYZDataset;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class CwtChartFactory {

	public static List<JFreeChart> createCostHistogramDS(int nbins, Ridge... r) {
		List<IRidgeCost> l = Ridge.getAvailableRidgeCostClasses();
		List<JFreeChart> dsl = new ArrayList<JFreeChart>(l.size());
		int k = 0;
		for (IRidgeCost irc : l) {
			HistogramDataset dxyzd = new HistogramDataset();
			double[] vals = new double[r.length];
			int v = 0;
			for (Ridge ridge : r) {
				double penalty = ridge.getRidgeCosts().get(k).getSecond();
				vals[v++] = penalty;
			}
			dxyzd.addSeries(irc.getClass().getName(), vals, nbins);
			JFreeChart jfc = ChartFactory.createHistogram("Cost histogram for "
			        + irc.getClass().getName(), "cost", "count", dxyzd,
			        PlotOrientation.VERTICAL, true, true, true);
			XYPlot xyp = jfc.getXYPlot();
			XYBarRenderer xybr = (XYBarRenderer)xyp.getRenderer();
			xybr.setShadowVisible(false);
			xybr.setBarAlignmentFactor(0);
			dsl.add(jfc);
			k++;
		}
		return dsl;
	}

	public static List<JFreeChart> createNeighborhoodHistogramDS(int spm, int nbins,
	        QuadTree<Ridge> qt, int radius, Ridge... r) {
		List<IRidgeCost> l = Ridge.getAvailableRidgeCostClasses();
		List<JFreeChart> dsl = new ArrayList<JFreeChart>(l.size());
		for (int k = 1; k <= radius; k++) {
			HistogramDataset dxyzd = new HistogramDataset();
			List<Double> vals = new ArrayList<Double>();
			int v = 0;
			for (Ridge ridge : r) {
				Point2D root = ridge.getRidgePoints().get(0).getFirst();
				double x = root.getX() / spm;
				double y = root.getX() % spm;
				double n = qt.getNeighborsInRadius(new Point2D.Double(x,y), k).size();
				if (n > 0) {
					vals.add(n);
				}
				// vals[v++] = n;
			}
			double[] vs = new double[vals.size()];
			for (int i = 0; i < vals.size(); i++) {
				vs[i] = vals.get(i).doubleValue();
			}
			if (vs != null && vs.length > 0) {
				dxyzd.addSeries("Neighborhood histogram for r=" + k, vs, nbins);
				JFreeChart jfc = ChartFactory.createHistogram(
				        "Neighborhood histogram for radius " + k,
				        "neighborhood size", "count", dxyzd,
				        PlotOrientation.VERTICAL, true, true, true);
				XYPlot xyp = jfc.getXYPlot();
				XYBarRenderer xybr = (XYBarRenderer)xyp.getRenderer();
				xybr.setShadowVisible(false);
				xybr.setBarAlignmentFactor(0);
				dsl.add(jfc);
			}
		}
		return dsl;
	}

	public static List<JFreeChart> create2DRidgeCostDS(int spm, Ridge... r) {
		List<IRidgeCost> l = Ridge.getAvailableRidgeCostClasses();
		List<JFreeChart> dsl = new ArrayList<JFreeChart>(l.size());
		int k = 0;
		for (IRidgeCost irc : l) {
			DefaultXYZDataset dxyzd = new DefaultXYZDataset();
			double[][] tic2ddata = new double[3][r.length];
			int cnt = 0;
			double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
			for (Ridge ridge : r) {
				Point2D root = ridge.getRidgePoints().get(0).getFirst();
				tic2ddata[0][cnt] = root.getX() / spm;
				tic2ddata[1][cnt] = root.getX() % spm;
				double v = ridge.getRidgeCosts().get(k).getSecond();
				min = Math.min(min, v);
				max = Math.max(max, v);
				tic2ddata[2][cnt] = v;
				cnt++;
			}
			dxyzd.addSeries(irc.getClass().getName(), tic2ddata);
			XYBlockRenderer xybr = new XYBlockRenderer();
			xybr.setBlockHeight(1.0d);
			xybr.setBlockWidth(1.0d);
			if (min < max) {
				xybr.setPaintScale(new GradientPaintScale(ImageTools.createSampleTable(256),min, max, new Color[]{Color.BLUE, Color.GREEN, Color.YELLOW, Color.orange,Color.red,Color.MAGENTA}));
			} else {
				xybr.setPaintScale(new GradientPaintScale(ImageTools.createSampleTable(256),min, max, new Color[]{Color.BLUE, Color.GREEN, Color.YELLOW, Color.orange,Color.red,Color.MAGENTA}));
			}
			XYPlot xyp = new XYPlot(dxyzd, new NumberAxis("x"), new NumberAxis(
			        "y"), xybr);
			JFreeChart jfc = new JFreeChart(xyp);
			// jfc.addSubtitle(new PaintScaleLegend(xybr.getPaintScale(),
			// new NumberAxis("ridge cost of " + irc.getClass().getName())));
			dsl.add(jfc);
			k++;
		}
		return dsl;
	}

	public static List<JFreeChart> create2DRidgeNeighborhoodDS(int spm,
	        QuadTree<Ridge> qt, int radius, Ridge... r) {
		List<JFreeChart> l = new ArrayList<JFreeChart>();
//		HashMap<Ridge, Integer> ridgeToPreviousNeighbors = new HashMap<Ridge, Integer>();
		for (int i = 1; i <= radius; i++) {
			DefaultXYZDataset dxyzd = new DefaultXYZDataset();
			double[][] tic2ddata = new double[3][r.length];
			int cnt = 0;
			double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
			for (Ridge ridge : r) {
				Point2D root = ridge.getRidgePoints().get(0).getFirst();
				tic2ddata[0][cnt] = root.getX() / spm;
				tic2ddata[1][cnt] = root.getX() % spm;
				double v = qt.getVerticalNeighborsInRadius(new Point2D.Double(tic2ddata[0][cnt],tic2ddata[1][cnt]), i).size();
//				if (ridgeToPreviousNeighbors.containsKey(ridge)) {
//					v = v - ridgeToPreviousNeighbors.get(ridge);
//				}
//				ridgeToPreviousNeighbors.put(ridge, (int) v);
				min = Math.min(min, v);
				max = Math.max(max, v);
				tic2ddata[2][cnt] = v;
				cnt++;
			}
			dxyzd.addSeries("vertical ridge neighborhood, radius=" + i, tic2ddata);
			XYBlockRenderer xybr = new XYBlockRenderer();
			xybr.setBlockHeight(1.0d);
			xybr.setBlockWidth(1.0d);
			if (min < max) {
				xybr.setPaintScale(new GradientPaintScale(ImageTools.createSampleTable(256),min, max, new Color[]{Color.WHITE,Color.BLUE, Color.GREEN, Color.YELLOW, Color.orange,Color.red,Color.MAGENTA}));
			} else {
				xybr.setPaintScale(new GradientPaintScale(ImageTools.createSampleTable(256),min, max, new Color[]{Color.WHITE,Color.BLUE, Color.GREEN, Color.YELLOW, Color.orange,Color.red,Color.MAGENTA}));
			}
			XYPlot xyp = new XYPlot(dxyzd, new NumberAxis("x"), new NumberAxis(
			        "y"), xybr);
			JFreeChart jfc = new JFreeChart(
			        "2D Vertical Ridge Neighborhood difference for radius: " + i, xyp);
			// jfc.addSubtitle(new PaintScaleLegend(xybr.getPaintScale(),
			// new NumberAxis("neighbors in radius " + i)));
			l.add(jfc);
		}
		for (int i = 1; i <= radius; i++) {
			DefaultXYZDataset dxyzd = new DefaultXYZDataset();
			double[][] tic2ddata = new double[3][r.length];
			int cnt = 0;
			double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
			for (Ridge ridge : r) {
				Point2D root = ridge.getRidgePoints().get(0).getFirst();
				tic2ddata[0][cnt] = root.getX() / spm;
				tic2ddata[1][cnt] = root.getX() % spm;
				double v = qt.getHorizontalNeighborsInRadius(new Point2D.Double(tic2ddata[0][cnt],tic2ddata[1][cnt]), i).size();
//				if (ridgeToPreviousNeighbors.containsKey(ridge)) {
//					v = v - ridgeToPreviousNeighbors.get(ridge);
//				}
//				ridgeToPreviousNeighbors.put(ridge, (int) v);
				min = Math.min(min, v);
				max = Math.max(max, v);
				tic2ddata[2][cnt] = v;
				cnt++;
			}
			dxyzd.addSeries("horizontal ridge neighborhood, radius=" + i, tic2ddata);
			XYBlockRenderer xybr = new XYBlockRenderer();
			xybr.setBlockHeight(1.0d);
			xybr.setBlockWidth(1.0d);
			if (min < max) {
				xybr.setPaintScale(new GradientPaintScale(ImageTools.createSampleTable(256),min, max, new Color[]{Color.WHITE,Color.BLUE, Color.GREEN, Color.YELLOW, Color.orange,Color.red,Color.MAGENTA}));
			} else {
				xybr.setPaintScale(new GradientPaintScale(ImageTools.createSampleTable(256),min, max, new Color[]{Color.WHITE,Color.BLUE, Color.GREEN, Color.YELLOW, Color.orange,Color.red,Color.MAGENTA}));
			}
			XYPlot xyp = new XYPlot(dxyzd, new NumberAxis("x"), new NumberAxis(
			        "y"), xybr);
			JFreeChart jfc = new JFreeChart(
			        "2D Horizontal Ridge Neighborhood difference for radius: " + i, xyp);
			// jfc.addSubtitle(new PaintScaleLegend(xybr.getPaintScale(),
			// new NumberAxis("neighbors in radius " + i)));
			l.add(jfc);
		}
		for (int i = 1; i <= radius; i++) {
			DefaultXYZDataset dxyzd = new DefaultXYZDataset();
			double[][] tic2ddata = new double[3][r.length];
			int cnt = 0;
			double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
			for (Ridge ridge : r) {
				Point2D root = ridge.getRidgePoints().get(0).getFirst();
				tic2ddata[0][cnt] = root.getX() / spm;
				tic2ddata[1][cnt] = root.getX() % spm;
				double v = qt.getNeighborsInRadius(new Point2D.Double(tic2ddata[0][cnt],tic2ddata[1][cnt]), i).size();
//				if (ridgeToPreviousNeighbors.containsKey(ridge)) {
//					v = v - ridgeToPreviousNeighbors.get(ridge);
//				}
//				ridgeToPreviousNeighbors.put(ridge, (int) v);
				min = Math.min(min, v);
				max = Math.max(max, v);
				tic2ddata[2][cnt] = v;
				cnt++;
			}
			dxyzd.addSeries("ridge neighborhood, radius=" + i, tic2ddata);
			XYBlockRenderer xybr = new XYBlockRenderer();
			xybr.setBlockHeight(1.0d);
			xybr.setBlockWidth(1.0d);
			if (min < max) {
				xybr.setPaintScale(new GradientPaintScale(ImageTools.createSampleTable(256),min, max, new Color[]{Color.WHITE,Color.BLUE, Color.GREEN, Color.YELLOW, Color.orange,Color.red,Color.MAGENTA}));
			} else {
				xybr.setPaintScale(new GradientPaintScale(ImageTools.createSampleTable(256),min, max, new Color[]{Color.WHITE,Color.BLUE, Color.GREEN, Color.YELLOW, Color.orange,Color.red,Color.MAGENTA}));
			}
			XYPlot xyp = new XYPlot(dxyzd, new NumberAxis("x"), new NumberAxis(
			        "y"), xybr);
			JFreeChart jfc = new JFreeChart(
			        "2D Ridge Neighborhood difference for radius: " + i, xyp);
			// jfc.addSubtitle(new PaintScaleLegend(xybr.getPaintScale(),
			// new NumberAxis("neighbors in radius " + i)));
			l.add(jfc);
		}
		return l;
	}

	public static void saveImages(File outputDirectory, String prefix,
	        List<JFreeChart> l) {
		int i = 0;
		for (JFreeChart jfc : l) {
			File image = new File(outputDirectory, prefix + "_" + i + ".png");
			try {
				ChartUtilities.saveChartAsPNG(image, jfc, 1024, 768, null,
				        true, 0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			i++;
		}
	}

	public static void saveImages(String outputDirectory, String prefix,
	        List<JFreeChart> l) {
		saveImages(new File(outputDirectory), prefix, l);
	}

}
