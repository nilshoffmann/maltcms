/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package maltcms.db.ui;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
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
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.MAMath;
import cross.datastructures.tuple.Tuple2D;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.LinkedHashMap;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JToolBar;

public class MetaboliteListSelectionListener implements ListSelectionListener,
        WindowListener, MouseListener {

    /**
     *
     */
    private final MetaboliteView metaboliteView;
    private JTable table;
    protected HashMap<String, JFrame> hm = new LinkedHashMap<String, JFrame>();
    protected HashMap<String, XYSeries> series = new LinkedHashMap<String, XYSeries>();
    protected XYSeriesCollection xysc = null;
    protected XYPlot p = null;
    protected JFreeChart jfc = null;
    protected ChartPanel cp = null;
    protected JFrame msframe = null;
    protected JToolBar toolbar = null;
    private int frameIndex = 0;
    protected MetaboliteViewModel mvm2 = null;
    private ExecutorService es = Executors.newFixedThreadPool(5);

    public MetaboliteListSelectionListener(MetaboliteView metaboliteView,
            MetaboliteViewModel mvm, JTable table) {
        this.metaboliteView = metaboliteView;
        this.mvm2 = mvm;
        this.table = table;
    }

    public void valueChanged(ListSelectionEvent e) {
        // final int first_index = e.getFirstIndex();
        final int last_index = e.getLastIndex();
        System.out.println("ListSelection Event at " + last_index
                + " originated from " + e.getSource().getClass());
        // if(first_index==last_index) {
//        if (o instanceof ListSelectionModel) {

//        }
    }

    private JFrame getMSFrame() {
        if (msframe == null) {
            System.out.println("Creating new Frame");
            msframe = new JFrame("Mass Spectra");
            msframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            msframe.add(getJToolBar(),BorderLayout.PAGE_START);

        }
        return msframe;
    }

    private JToolBar getJToolBar() {
        if(toolbar == null) {
            toolbar = new JToolBar();
            toolbar.add(new AbstractAction("Clear"){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    series.clear();
                    updatePlot();
                }
            });
        }
        return toolbar;
    }

    private void addMetaboliteToChart(final IMetabolite m) {
        System.out.println("Adding ms chart!");
        if (series.containsKey(m.getName())) {
            System.out.println("Series for " + m.getName() + " already created!");
        } else {
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    series.put(m.getName(), createXYSeries(m));
                    updatePlot();
                }
            };
            SwingUtilities.invokeLater(r);
        }
    }

    private XYSeries createXYSeries(final IMetabolite m) throws IllegalArgumentException {
        Tuple2D<ArrayDouble.D1, ArrayInt.D1> t = m.getMassSpectrum();
        XYSeries xys = new XYSeries(m.getName());
        ArrayDouble.D1 masses = t.getFirst();
        ArrayInt.D1 intens = t.getSecond();
        ArrayDouble.D1 intensd = new ArrayDouble.D1(intens.getShape()[0]);
        MAMath.copyDouble(intensd, intens);
        MAMath.MinMax mm = MAMath.getMinMax(intens);
        MultiplicationFilter mf = new MultiplicationFilter(1.0 / mm.max);
        intensd = (ArrayDouble.D1) mf.apply(intensd);
        for (int i = 0; i < masses.getShape()[0]; i++) {
            xys.add(masses.get(i), (double) intensd.get(i));
        }
        return xys;
    }

    private ChartPanel getChartPanel() {
        if (cp == null) {
            cp = new ChartPanel(getChart());
            cp.setBorder(BorderFactory.createTitledBorder("Mass spectra"));
        }
        return cp;
    }

    private JFreeChart getChart() {
        if (jfc == null) {
            jfc = new JFreeChart(getPlot());
        }
        return jfc;
    }

    private XYPlot getPlot() {
        if (p == null) {
            XYBarRenderer dir = new XYBarRenderer();
            dir.setDrawBarOutline(true);
            dir.setShadowVisible(false);
            p = new XYPlot(getSeries(), new NumberAxis("m/z"), new NumberAxis("rel. intensity"), dir);
            p.setForegroundAlpha(0.5f);
        }
        return p;
    }

    private XYSeriesCollection getSeries() {
        if (xysc == null) {
            xysc = new XYSeriesCollection();
        }
        return xysc;
    }

    private void updatePlot() {
        getSeries().removeAllSeries();
        for (String s : series.keySet()) {
            getSeries().addSeries(series.get(s));
        }
        getMSFrame().add(getChartPanel());
        getMSFrame().setVisible(true);
        getMSFrame().pack();
    }

    private void removeMetaboliteFromChart(final IMetabolite m) {
        this.series.remove(m.getName());
        updatePlot();
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

    @Override
    public void mouseClicked(MouseEvent me) {
        if (me.getButton() == MouseEvent.BUTTON3) {
            int row = table.getSelectedRow();
            final IMetabolite m = (IMetabolite) mvm2.getMetaboliteAtRow(row);
            if (m != null) {
                System.out.println(m.getMassSpectrum().getSecond());
                Runnable fr = new Runnable() {

                    @Override
                    public void run() {
                        JPopupMenu jpm = new JPopupMenu();
                        boolean contained = series.containsKey(m.getName());
                        if (!contained) {
                            jpm.add(new AbstractAction("Add to chart") {

                                @Override
                                public void actionPerformed(ActionEvent ae) {
                                    addMetaboliteToChart(m);
                                }
                            });
                        } else {
                            jpm.add(new AbstractAction("Remove from chart") {

                                @Override
                                public void actionPerformed(ActionEvent ae) {
                                    removeMetaboliteFromChart(m);
                                }
                            });
                        }
                        jpm.show(table, table.getMousePosition().x, table.getMousePosition().y);
                    }
                };
                SwingUtilities.invokeLater(fr);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent me) {
    }

    @Override
    public void mouseReleased(MouseEvent me) {
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }
}
