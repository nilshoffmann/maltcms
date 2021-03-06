/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package maltcms.db.ui;

import com.db4o.ObjectContainer;
import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import maltcms.datastructures.ms.Metabolite;

/**
 * <p>MetaboliteView class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class MetaboliteView implements TableModelListener, ComponentListener {

    /**
     *
     */
    private static final long serialVersionUID = -1652936063394236524L;
    protected JTable metaboliteView = null;
    protected MetaboliteViewModel mvm = null;
    //protected MetaboliteListDataListener mld = null;
    protected MetaboliteListSelectionListener mlsl = null;
    protected ExecutorService es = Executors.newCachedThreadPool();
    protected JProgressBar jpb = null;
    protected ObjectContainer oc = null;
    protected JPanel jp = null;
    protected double threshold = 0.99d;
    protected JScrollPane jsp = null, jsp2 = null;

    /**
     * <p>Constructor for MetaboliteView.</p>
     *
     * @param mvm a {@link maltcms.db.ui.MetaboliteViewModel} object.
     */
    public MetaboliteView(MetaboliteViewModel mvm) {
        this.mvm = mvm;
        //jpb = new JProgressBar();
        //mld = new MetaboliteListDataListener(this);
        metaboliteView = new JTable();
        //metaboliteView.setCellRenderer(new MetaboliteListCellRenderer());
        //metaboliteView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mlsl = new MetaboliteListSelectionListener(this, this.mvm, metaboliteView);
        //metaboliteView.getSelectionModel().addListSelectionListener(mlsl);
        metaboliteView.addMouseListener(mlsl);
        //mvm.addListDataListener(mld);
        //add(jpb,BorderLayout.SOUTH);
        metaboliteView.setModel(this.mvm);
        metaboliteView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        //metaboliteView.setRowSorter(new TableRowSorter<MetaboliteViewModel>(this.mvm));
        //metaboliteView.setAutoscrolls(true);
        metaboliteView.setFillsViewportHeight(true);
        this.mvm.addTableModelListener(this);
        this.jp = new JPanel();
    }

    /**
     * <p>setEnabled.</p>
     *
     * @param b a boolean.
     */
    public void setEnabled(final boolean b) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                metaboliteView.setEnabled(b);

            }
        };
        SwingUtilities.invokeLater(r);
    }

    /**
     * <p>addComponents.</p>
     *
     * @param jf a {@link javax.swing.JFrame} object.
     */
    public void addComponents(JFrame jf) {
        jf.setLayout(new BorderLayout());
        //jf.add(jp,BorderLayout.CENTER);
        this.jsp = new JScrollPane(metaboliteView);
        //this.jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        //jsp.setPreferredSize(new Dimension(1024,768));
        //jsp.setBorder(BorderFactory.createTitledBorder("Metabolites"));
        //JList predicateList = new JList(getMetaboliteMembers());
        //this.jsp2 = new JScrollPane(predicateList);
        jsp.setBorder(BorderFactory.createTitledBorder("Metabolites"));
        //this.jsp.setAutoscrolls(true);
        //this.jsp2.setAutoscrolls(true);
        jf.add(jsp, BorderLayout.CENTER);
        jf.addComponentListener(this);
        //jp.add(jsp2,BorderLayout.EAST);
        //metaboliteView.setAutoscrolls(true);
        //metaboliteView.setFillsViewportHeight(true);
    }

    /**
     * <p>getMetaboliteMembers.</p>
     *
     * @return a {@link java.util.Vector} object.
     */
    public Vector<String> getMetaboliteMembers() {
        Method[] m = Metabolite.class.getMethods();
        Vector<String> al = new Vector<>();
        for (Method method : m) {
            if (method.getName().startsWith("get") && !method.getName().equals("getClass")) {
                al.add(method.getName().substring(3));
            }
        }
        Collections.sort(al);
        //log.info(al);
        return al;
    }

    /**
     * <p>newRowsAdded.</p>
     *
     * @param arg0 a {@link javax.swing.event.TableModelEvent} object.
     */
    public void newRowsAdded(TableModelEvent arg0) {
        this.metaboliteView.revalidate();
        //this.jsp2.revalidate();
        //this.jsp.revalidate();
        //this.jp.revalidate();
    }

    /** {@inheritDoc} */
    @Override
    public void tableChanged(TableModelEvent arg0) {
        this.metaboliteView.revalidate();
        //this.jsp.revalidate();
        //this.jsp2.revalidate();
        //this.jp.revalidate();
    }

    /** {@inheritDoc} */
    @Override
    public void componentHidden(ComponentEvent e) {
    }

    /** {@inheritDoc} */
    @Override
    public void componentMoved(ComponentEvent e) {
    }

    /** {@inheritDoc} */
    @Override
    public void componentResized(ComponentEvent e) {
        //log.info("Component resized: "+e.toString());
        //this.metaboliteView.setPreferredSize(e.getComponent().getPreferredSize());
        //this.jsp.getViewport().setPreferredSize(e.getComponent().getPreferredSize());
        this.jsp.revalidate();
        //SwingUtilities.updateComponentTreeUI(this.jsp);
        //this.metaboliteView.revalidate();

    }

    /** {@inheritDoc} */
    @Override
    public void componentShown(ComponentEvent e) {
    }
//	public class MetaboliteListDataListener implements ListDataListener{
//
//		protected MetaboliteView mv = null;
//		
//		public MetaboliteListDataListener(MetaboliteView mv) {
//			this.mv = mv;
//		}
//		
//		@Override
//		public void contentsChanged(ListDataEvent e) {
//			//RepaintManager.currentManager(this.mv).markCompletelyDirty(this.mv);
//			//RepaintManager.currentManager(this.mv).addInvalidComponent(this.mv);
//		}
//
//		@Override
//		public void intervalAdded(ListDataEvent e) {
//			//RepaintManager.currentManager(this.mv).markCompletelyDirty(this.mv);
//			//RepaintManager.currentManager(this.mv).addInvalidComponent(this.mv);
//		}
//
//		@Override
//		public void intervalRemoved(ListDataEvent e) {
//			//RepaintManager.currentManager(this.mv).markCompletelyDirty(this.mv);
//			//RepaintManager.currentManager(this.mv).addInvalidComponent(this.mv);
//		}
//		
//	}
}
