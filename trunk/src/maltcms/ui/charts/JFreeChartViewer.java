/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id$
 */
package maltcms.ui.charts;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.ui.TextAnchor;

import ucar.ma2.Array;
import cross.Factory;
import cross.datastructures.fragments.IFileFragment;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class JFreeChartViewer extends JFrame {

	/**
     * 
     */
	private static final long serialVersionUID = -830245788879399345L;

	private JTabbedPane jtp = null;

	private File lastOpenDir = null;

	public JTabbedPane getJtp() {
		return jtp;
	}

	public void setJtp(JTabbedPane jtp) {
		this.jtp = jtp;
	}

	public JMenuBar getJmb() {
		return jmb;
	}

	public void setJmb(JMenuBar jmb) {
		this.jmb = jmb;
	}

	public JMenu getFileMenu() {
		return fileMenu;
	}

	public void setFileMenu(JMenu fileMenu) {
		this.fileMenu = fileMenu;
	}

	private JMenuBar jmb = null;
	private JMenu fileMenu = null;
	private JLabel status = null;

	public JLabel getStatus() {
		return status;
	}

	public void setStatus(JLabel status1) {
		this.status = status1;
	}

	public JFreeChartViewer() {
		CompositeConfiguration cfg = new CompositeConfiguration();
		cfg.addConfiguration(new SystemConfiguration());
		try {
			PropertiesConfiguration pcfg = new PropertiesConfiguration(
			        JFreeChartViewer.class.getClassLoader().getResource(
			                "cfg/default.properties"));
			cfg.addConfiguration(pcfg);
			Factory.getInstance().configure(cfg);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.jtp = new JTabbedPane();

		final FileOpenAction fopen = new FileOpenAction("Load Chart");
		final ChromatogramOpenAction coa = new ChromatogramOpenAction(
		        "Generate Chart");
		fopen.setParent(this);
		this.jmb = new JMenuBar();
		this.fileMenu = new JMenu("File");
		fileMenu.add(new JMenuItem(fopen));
		fileMenu.add(new JMenuItem(coa));
		this.jmb.add(fileMenu);
		this.status = new JLabel("Status");
		this.add(this.status, BorderLayout.SOUTH);
		setJMenuBar(this.jmb);
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		add(this.jtp);
	}

	public void addChart(JFreeChart c, String name) {
		ChartPanel cp2 = new ChartPanel(c);
		cp2.addChartMouseListener(new ChartPanelMouseListener(cp2));
		CrosshairOverlay cco = new CrosshairOverlay();
		// cp2.addChartMouseListener(cco);
		cp2.addOverlay(cco);
		cp2.setHorizontalAxisTrace(true);
		cp2.setVerticalAxisTrace(true);
		getJtp().addTab(name, cp2);
		ChartSerializeAction csa = new ChartSerializeAction("Serialize Chart");
		csa.setJfc(c);
		cp2.getPopupMenu().add(csa);
	}

	public ChartPanel getActiveChartPanel() {
		Object o = getJtp().getTabComponentAt(getJtp().getSelectedIndex());
		System.out.println("class: " + o.getClass().getName());
		return (ChartPanel) o;
	}

	public static void main(String[] args) {
		final JFrame jf2 = new JFreeChartViewer();
		System.setProperty("log4j.configuration", "cfg/log4j.properties");
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				jf2.setVisible(true);

			}
		});

	}

	public class ChartSerializeAction extends AbstractAction {

		/**
         * 
         */
		public ChartSerializeAction() {
			super();
			// TODO Auto-generated constructor stub
		}

		/**
		 * @param name
		 * @param icon
		 */
		public ChartSerializeAction(String name, Icon icon) {
			super(name, icon);
			// TODO Auto-generated constructor stub
		}

		/**
		 * @param name
		 */
		public ChartSerializeAction(String name) {
			super(name);
			// TODO Auto-generated constructor stub
		}

		/**
         * 
         */
		private static final long serialVersionUID = 425194552644224115L;

		private JFreeChart jfc = null;

		public JFreeChart getJfc() {
			return jfc;
		}

		public void setJfc(JFreeChart jfc) {
			this.jfc = jfc;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			Runnable r = new Runnable() {

				@Override
				public void run() {
					System.out.println("Running chart serialize action!");
					JFileChooser fc = new JFileChooser(lastOpenDir);
					fc.setMultiSelectionEnabled(false);
					int type = fc.showSaveDialog(getParent());
					if (type == JFileChooser.APPROVE_OPTION) {
						final File f = fc.getSelectedFile();
						lastOpenDir = f.getParentFile();
						try {
							ObjectOutputStream oos = new ObjectOutputStream(
							        new BufferedOutputStream(
							                new FileOutputStream(f)));
							oos.writeObject(getJfc());
							oos.flush();
							oos.close();
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						System.out.println("Aborted by user");
						// System.exit(-1);
					}

				}
			};
			SwingUtilities.invokeLater(r);

		}

	}

	public class ChromatogramOpenAction extends AbstractAction {

		/**
         * 
         */
		private static final long serialVersionUID = 5547094237664337509L;

		/**
         * 
         */
		public ChromatogramOpenAction() {
			super();
			// TODO Auto-generated constructor stub
		}

		/**
		 * @param name
		 * @param icon
		 */
		public ChromatogramOpenAction(String name, Icon icon) {
			super(name, icon);
			// TODO Auto-generated constructor stub
		}

		/**
		 * @param name
		 */
		public ChromatogramOpenAction(String name) {
			super(name);
			// TODO Auto-generated constructor stub
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			Runnable r = new Runnable() {

				@Override
				public void run() {
					System.out.println("Running Chromatogram open action!");
					final JFileChooser jfc = new JFileChooser(lastOpenDir);
					jfc.setMultiSelectionEnabled(true);
					jfc.setFileFilter(new FileFilter() {

						@Override
						public String getDescription() {
							List<String> l = Factory.getInstance()
							        .getDataSourceFactory()
							        .getSupportedFormats();
							StringBuilder sb = new StringBuilder();
							for (String s : l) {
								sb.append("." + s + ", ");
							}
							return sb.toString().trim();
						}

						@Override
						public boolean accept(File f) {
							if (f.isDirectory()) {
								return true;
							}
							for (String s : Factory.getInstance()
							        .getDataSourceFactory()
							        .getSupportedFormats()) {
								if (f.getAbsolutePath().toLowerCase().endsWith(
								        s)) {
									return true;
								}
							}
							return false;
						}
					});
					int type = jfc.showOpenDialog(getParent());
					if (type == JFileChooser.APPROVE_OPTION) {
						Runnable r = new Runnable() {
							@Override
							public void run() {
								final File[] f = jfc.getSelectedFiles();
								final String[] labels = new String[f.length];
								final Array[] arrays = new Array[f.length];
								final Array[] domains = new Array[f.length];
								int i = 0;
								VariableSelectionPanel vsp = new VariableSelectionPanel();
								IFileFragment fragment = Factory.getInstance()
								        .getFileFragmentFactory().create(f[0]);
								ArrayList<String> vars = new ArrayList<String>();
								String[] cfgvars = Factory.getInstance()
								        .getConfiguration().getStringArray(
								                "default.vars");
								vars.addAll(Arrays.asList(cfgvars));
								// for(String var:vars) {
								// try{
								// fragment.getChild(var, true);
								// }catch(ResourceNotAvailableException r){
								// vars.remove(var);
								// }
								// }
								vsp.setAvailableVariables(vars
								        .toArray(new String[] {}));
								javax.swing.JOptionPane.showMessageDialog(
								        getParent(), vsp);
								String domainVar = vsp
								        .getSelectedDomainVariable();
								String valueVar = vsp
								        .getSelectedValuesVariable();
								for (File file : f) {
									lastOpenDir = file.getParentFile();
									Factory.getInstance().getConfiguration()
									        .setProperty("output.basedir",
									                file.getParent());
									Factory.getInstance().getConfiguration()
									        .setProperty(
									                "user.name",
									                System.getProperty(
									                        "user.name", ""));
									fragment = Factory.getInstance()
									        .getFileFragmentFactory().create(
									                file);
									labels[i] = fragment.getName();
									arrays[i] = fragment.getChild(valueVar)
									        .getArray();
									// Factory.getInstance().getConfiguration().getString("var.total_intensity","total_intensity")).getArray();
									if (!domainVar.equals("")) {
										domains[i] = fragment.getChild(
										        domainVar).getArray();
									}
									// Factory.getInstance().getConfiguration().getString("var.scan_acquisition_time","scan_acquisition_time")).getArray();
									i++;
								}
								XYChart xyc = null;
								if (!domainVar.equals("")) {
									xyc = new XYChart(
									        f.length > 1 ? lastOpenDir
									                .getName() : f[0].getName(),
									        labels, arrays, domains, "time[s]",
									        "value");
								} else {
									xyc = new XYChart(
									        f.length > 1 ? lastOpenDir
									                .getName() : f[0].getName(),
									        labels, arrays, "time[s]", "value");
								}
								JFreeChart jfc2 = new JFreeChart(xyc.create());
								addChart(jfc2, lastOpenDir.getName());
							}
						};
						SwingUtilities.invokeLater(r);
					} else {
						System.out.println("Aborted by user");
						// System.exit(-1);
					}

				}
			};
			SwingUtilities.invokeLater(r);

		}

	}

	public class FileOpenAction extends AbstractAction {

		/**
         * 
         */
		private static final long serialVersionUID = -8133108907581040380L;
		private Component parent = null;

		public void setParent(Component c) {
			this.parent = c;
		}

		public Component getParent() {
			return this.parent;
		}

		/**
         * 
         */
		public FileOpenAction() {
			super();
			// TODO Auto-generated constructor stub
		}

		/**
		 * @param name
		 * @param icon
		 */
		public FileOpenAction(String name, Icon icon) {
			super(name, icon);
			// TODO Auto-generated constructor stub
		}

		/**
		 * @param name
		 */
		public FileOpenAction(String name) {
			super(name);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			Runnable r = new Runnable() {

				@Override
				public void run() {
					System.out.println("Running File open action!");
					JFileChooser jfc = new JFileChooser(lastOpenDir);
					jfc.setMultiSelectionEnabled(true);
					jfc.setFileFilter(new FileFilter() {

						@Override
						public String getDescription() {
							return "Displays files with .serialized extension";
						}

						@Override
						public boolean accept(File f) {
							if (f.getAbsolutePath().endsWith(".serialized")) {
								return true;
							}
							return false;
						}
					});
					int type = jfc.showOpenDialog(getParent());
					if (type == JFileChooser.APPROVE_OPTION) {
						final File[] f = jfc.getSelectedFiles();
						// Runnable r = new Runnable(){
						//					
						// @Override
						// public void run() {
						for (File file : f) {
							lastOpenDir = file.getParentFile();
							try {
								ObjectInputStream ois = new ObjectInputStream(
								        new BufferedInputStream(
								                new FileInputStream(file)));
								JFreeChart jfc1 = (JFreeChart) ois.readObject();
								ois.close();
								addChart(jfc1, file.getName());
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (ClassNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

						// jf2.pack();
						// }
						// };
						// SwingUtilities.invokeLater(r);

					} else {
						System.out.println("Aborted by user");
						// System.exit(-1);
					}

				}
			};
			SwingUtilities.invokeLater(r);

		}
	}

	public class XYAnnotationAdder {

		public void addAnnotation(final ChartPanel cp, final JFreeChart jfc,
		        final XYItemEntity xyie, final int x, final int y) {
			final double xd = xyie.getDataset().getXValue(
			        xyie.getSeriesIndex(), xyie.getItem());
			final double yd = xyie.getDataset().getYValue(
			        xyie.getSeriesIndex(), xyie.getItem());
			// Runnable r = new Runnable() {

			// @Override
			// public void run() {
			final JPopupMenu jpm = new JPopupMenu("Add annotation");
			final JTextArea jta = new JTextArea("x=" + xd + ", y=" + yd);
			AbstractAction applyAction = new AbstractAction("Apply") {

				/**
                         * 
                         */
				private static final long serialVersionUID = -135368123552109960L;

				@Override
				public void actionPerformed(ActionEvent e) {
					if (!jta.getText().equals("")) {
						final XYPointerAnnotation xya = new XYPointerAnnotation(
						        jta.getText(), xd, yd, -0.6);
						xya.setTipRadius(0);
						xya.setTextAnchor(TextAnchor.BASELINE_LEFT);
						// xya.setBaseRadius(5.0);
						// xya.setArrowLength(10.0);
						Runnable r = new Runnable() {

							@Override
							public void run() {
								jfc.getXYPlot().addAnnotation(xya);

							}
						};
						SwingUtilities.invokeLater(r);
					}
					jpm.setVisible(false);
				}
			};
			jpm.add(jta);
			jpm.add(applyAction);
			jpm.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					jpm.setVisible(false);

				}

				@Override
				public void focusGained(FocusEvent e) {

				}
			});
			jpm.show(cp, x, y);

			// }
			// };
			// SwingUtilities.invokeLater(r);
		}

	}

	public class ChartPanelMouseListener implements ChartMouseListener {

		private ChartPanel cp;

		public ChartPanelMouseListener(ChartPanel cp1) {
			this.cp = cp1;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.jfree.chart.ChartMouseListener#chartMouseClicked(org.jfree.chart
		 * .ChartMouseEvent)
		 */
		@Override
		public void chartMouseClicked(ChartMouseEvent arg0) {
			if (arg0.getEntity() != null
			        && arg0.getEntity() instanceof XYItemEntity) {
				System.out.println(arg0.getEntity().getClass().getName());
				XYAnnotationAdder xya = new XYAnnotationAdder();
				xya.addAnnotation(this.cp, arg0.getChart(), (XYItemEntity) arg0
				        .getEntity(), arg0.getTrigger().getX(), arg0
				        .getTrigger().getY());
			}
			System.out.println(arg0.getSource().getClass().getName());
			System.out.println(arg0.getTrigger().getClass().getName());
			System.out.println(arg0.getTrigger().getX() + " "
			        + arg0.getTrigger().getY());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.jfree.chart.ChartMouseListener#chartMouseMoved(org.jfree.chart
		 * .ChartMouseEvent)
		 */
		@Override
		public void chartMouseMoved(final ChartMouseEvent arg0) {
			// System.out.println("Mouse moved");
			Runnable r = new Runnable() {

				@Override
				public void run() {
					if (arg0.getEntity() != null) {
						System.out.println("Found an entity");
						getStatus().setText(
						        "Status: " + arg0.getEntity().toString());
						// jtt.setLocation(arg0.getTrigger().getX(),
						// arg0.getTrigger().getY());
						// jtt.setVisible(true);
					} else {
						// if(jtt!=null){
						getStatus().setText("Status: ");
						// }
					}

				}
			};
			SwingUtilities.invokeLater(r);

		}

	}

}
