/**
 * 
 */
package maltcms.db.ui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import maltcms.commands.filters.array.MultiplicationFilter;
import maltcms.datastructures.ms.IMetabolite;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.MAMath;
import cross.datastructures.tuple.Tuple2D;

public class MetaboliteListSelectionListener implements ListSelectionListener,
        WindowListener {

	/**
		 * 
		 */
	private final MetaboliteView metaboliteView;

	protected Hashtable<String, JFrame> hm = new Hashtable<String, JFrame>();

	protected Hashtable<String, XYSeries> series = new Hashtable<String, XYSeries>();

	protected XYSeriesCollection xysc = null;

	protected XYPlot p = null;

	protected JFreeChart jfc = null;

	protected ChartPanel cp = null;

	protected JFrame msframe = null;

	private int frameIndex = 0;

	protected MetaboliteViewModel mvm2 = null;

	private ExecutorService es = Executors.newFixedThreadPool(5);

	public MetaboliteListSelectionListener(MetaboliteView metaboliteView,
	        MetaboliteViewModel mvm) {
		this.metaboliteView = metaboliteView;
		this.mvm2 = mvm;
	}

	public void valueChanged(ListSelectionEvent e) {
		// final int first_index = e.getFirstIndex();

		final int last_index = e.getLastIndex();
		System.out.println("ListSelection Event at " + last_index
		        + " originated from " + e.getSource().getClass());
		// if(first_index==last_index) {
		Object o = e.getSource();
		System.out.println(o.getClass());
		if (o instanceof JList) {
			System.out.println("Event from JList");
			JList jl = (JList) o;
			Object o2 = jl.getSelectedValue();
			final int index = jl.getSelectedIndex();
			if (o2 instanceof Tuple2D<?, ?>) {
				Double d = -1.0d;
				IMetabolite m = null;
				if (((Tuple2D<?, ?>) o2).getFirst() instanceof Double) {
					d = (Double) ((Tuple2D<?, ?>) o2).getFirst();
				}
				if (((Tuple2D<?, ?>) o2).getSecond() instanceof IMetabolite) {
					m = (IMetabolite) ((Tuple2D<?, ?>) o2).getSecond();
				}
				if (m != null && !hm.containsKey(m.getID())) {
					final IMetabolite met = m;
					Runnable fr = new Runnable() {

						public void run() {
							createFrame(met);
						}

					};
					SwingUtilities.invokeLater(fr);
					// JFrame jf = new JFrame(m.getName());
					// jf.setName(""+index);
					// jf.add(new JTextArea(m.toString()));
					// jf.setVisible(true);
					// jf.pack();
					// jf.addWindowListener(this);
					// jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					// hm.put(m.getID(),jf);
				}
			}
			if (o2 instanceof IMetabolite) {
				final IMetabolite m = (IMetabolite) o2;
				System.out.println(m.getMassSpectrum().getSecond());
				Runnable fr = new Runnable() {

					public void run() {
						createFrame(m);
					}

				};
				SwingUtilities.invokeLater(fr);
			}
		}
		if (o instanceof ListSelectionModel) {
			System.out.println("Event from ListSelectionModel");
			ListSelectionModel ls = (ListSelectionModel) o;
			final int lsi = ls.getLeadSelectionIndex();
			int collsi = ls.getLeadSelectionIndex();

			System.out.println("LeadSelectionIndex: " + lsi);
			System.out.println("Column LeadSelectionIndex: " + collsi);
			final IMetabolite m = (IMetabolite) mvm2.getMetaboliteAtRow(lsi);
			if (m != null) {
				System.out.println(m.getMassSpectrum().getSecond());
				Runnable fr = new Runnable() {

					public void run() {
						createFrame(m);
					}

				};
				SwingUtilities.invokeLater(fr);
			}
		}
	}

	private void createFrame(IMetabolite m) {
		//if (!hm.containsKey(m.getID())) {
//			final JFrame jf = new JFrame(m.getName());
//			hm.put(m.getID(), jf);
//			jf.setName(m.getID());
//			JTextArea jta = new JTextArea(m.toString());
//			jta.setBorder(BorderFactory
//			        .createTitledBorder("Metabolite Details"));
//			jf.add(jta, BorderLayout.CENTER);
			Tuple2D<ArrayDouble.D1, ArrayInt.D1> t = m.getMassSpectrum();
			// System.out.println(t.getFirst());
			// System.out.println(t.getSecond());
			if (t != null) {
				if (msframe == null) {
					System.out.println("Creating new Frame");
					msframe = new JFrame("Mass Spectra");
					msframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				}
				addMSChart(msframe, m, m.getName());
			}else{
				System.err.println("Mass spectrum is null!");
			}
//			jf.addWindowListener(this);
//			jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			
			// Runnable r = new Runnable() {
			//				
			// @Override
			// public void run() {
//			jf.setVisible(true);
//			jf.pack();

			// }
			// };
			// SwingUtilities.invokeLater(r);
		//}
	}

	private void addMSChart(final JFrame jf, final IMetabolite m,
	        final String name) {
		System.out.println("Adding ms chart!");
		if (xysc == null) {
			xysc = new XYSeriesCollection();
		}
		
		try{
			xysc.getSeries(name);
			System.out.println("Series for " + name + " already created!");
		}catch(UnknownKeyException uke) {
			Runnable r = new Runnable() {

				public void run() {
					Tuple2D<ArrayDouble.D1, ArrayInt.D1> t = m
					        .getMassSpectrum();
					XYSeries xys = new XYSeries(name);
					
					ArrayDouble.D1 masses = t.getFirst();
					ArrayInt.D1 intens = t.getSecond();
					ArrayDouble.D1 intensd = new ArrayDouble.D1(intens.getShape()[0]);
					MAMath.copyDouble(intensd, intens);
					MAMath.MinMax mm = MAMath.getMinMax(intens);
					MultiplicationFilter mf = new MultiplicationFilter(
					        1.0 / mm.max);
					intensd = (ArrayDouble.D1)mf.apply(intensd);
					for (int i = 0; i < masses.getShape()[0]; i++) {
						xys.add(masses.get(i), (double) intensd.get(i));
					}
					XYBarRenderer dir = new XYBarRenderer();
					dir.setDrawBarOutline(true);
					dir.setShadowVisible(false);
					xysc.addSeries(xys);
					if (p == null) {
						p = new XYPlot(xysc, new NumberAxis("m/z"),
						        new NumberAxis("rel. intensity"), dir);
						p.setForegroundAlpha(0.5f);
					}
					if (jfc == null) {
						jfc = new JFreeChart(p);
					}
					if (cp == null) {
						cp = new ChartPanel(jfc);
						cp.setBorder(BorderFactory
						        .createTitledBorder("Mass spectra"));
						jf.add(cp);
					}
					// Runnable tr = new Runnable() {
					//						
					// @Override
					// public void run() {
					jf.setVisible(true);
					jf.pack();
					// }
					// };
					// SwingUtilities.invokeLater(tr);
				}
			};
			SwingUtilities.invokeLater(r);
		}	
	}

	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	public void windowClosed(WindowEvent e) {
		if (e.getWindow() instanceof JFrame) {
			// String s = ((JFrame)e.getWindow()).getTitle();
			// int index = Integer.parseInt(((JFrame)e.getWindow()).getName());
			System.out.println("Removing JFrame "
			        + ((JFrame) e.getWindow()).getName()
			        + " from map of active clients!");
			hm.remove(((JFrame) e.getWindow()).getName());
		}

	}

	public void windowClosing(WindowEvent e) {
		// if(e.getWindow() instanceof JFrame) {
		// String s = ((JFrame)e.getWindow()).getTitle();
		// System.out.println("Removing JFrame "+s+" from map of active clients!");
		// hm.remove(s);
		// }
	}

	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}

}
