package maltcms.db.ui;

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

import com.db4o.ObjectContainer;

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

    public MetaboliteView(MetaboliteViewModel mvm) {
        this.mvm = mvm;
        //jpb = new JProgressBar();
        //mld = new MetaboliteListDataListener(this);
        metaboliteView = new JTable();
        //metaboliteView.setCellRenderer(new MetaboliteListCellRenderer());
        //metaboliteView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mlsl = new MetaboliteListSelectionListener(this, this.mvm,metaboliteView);
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

    public void setEnabled(final boolean b) {
        Runnable r = new Runnable() {

            public void run() {
                metaboliteView.setEnabled(b);

            }
        };
        SwingUtilities.invokeLater(r);
    }

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

    public Vector<String> getMetaboliteMembers() {
        Method[] m = Metabolite.class.getMethods();
        Vector<String> al = new Vector<String>();
        for (Method method : m) {
            if (method.getName().startsWith("get") && !method.getName().equals("getClass")) {
                al.add(method.getName().substring(3));
            }
        }
        Collections.sort(al);
        //System.out.println(al);
        return al;
    }

    public void newRowsAdded(TableModelEvent arg0) {
        this.metaboliteView.revalidate();
        //this.jsp2.revalidate();
        //this.jsp.revalidate();
        //this.jp.revalidate();
    }

    public void tableChanged(TableModelEvent arg0) {
        this.metaboliteView.revalidate();
        //this.jsp.revalidate();
        //this.jsp2.revalidate();
        //this.jp.revalidate();
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
        //System.out.println("Component resized: "+e.toString());
        //this.metaboliteView.setPreferredSize(e.getComponent().getPreferredSize());
        //this.jsp.getViewport().setPreferredSize(e.getComponent().getPreferredSize());
        this.jsp.revalidate();
        //SwingUtilities.updateComponentTreeUI(this.jsp);
        //this.metaboliteView.revalidate();

    }

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
