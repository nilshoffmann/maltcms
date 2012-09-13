/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
package maltcms.ui.wizard;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.event.ListDataEvent;
import javax.swing.filechooser.FileFilter;

import org.netbeans.spi.wizard.WizardPage;

import cross.tools.StringTools;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class FileInputOutputPane extends WizardPage {

    /**
     *
     */
    private static final long serialVersionUID = 8468582599008051433L;

    /**
     * Creates new form FileInputOutputPane
     */
    public FileInputOutputPane() {
        initComponents();
    }

    @Override
    protected Object valueFrom(Component c) {
        if (c instanceof JList) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < this.dlm.getSize(); i++) {
                sb.append(this.dlm.get(i)
                        + ((i < this.dlm.getSize() - 1) ? (",") : ("")));
            }
            return sb.toString();
        } else {
            return super.valueFrom(c);
        }
    }

    @Override
    protected void valueTo(Map m, Component c) {
        if (c instanceof JList) {
            String s = (String) m.get(getMapKeyFor(c));
            String[] parts = s.split(",");
            for (String str : parts) {
                this.dlm.addElement(str);
            }
        } else {
            super.valueTo(m, c);
        }
    }

    @Override
    protected String validateContents(Component component, Object event) {
        // System.out.println("Validate component called");
        // if(component!=null) {
        // System.out.println(" on component: "+component.getName());
        ArrayList<String> lastMessage = new ArrayList<String>();
        // System.out.println("Input file list: " + valueFrom(this.jList1));
        if (this.dlm.getSize() < 2) {
            lastMessage.add("Please select at least two input files!");
        } else {
        }

        // System.out.println("Output dir list");
        if (jTextField6.getText().trim().isEmpty()) {
            lastMessage.add("Please select an output directory!");
        }
        if (lastMessage.size() > 0) {
            return lastMessage.get(0);
        }
        // System.out
        // .println("Output of FIOP: " + getWizardData("input.dataInfo"));
        return null;
        // }else{
        // return "Not all components ready yet!";
        // }

    }

    public static String getDescription() {
        return "File input and output";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		jPanel2 = new javax.swing.JPanel();
		jLabel6 = new javax.swing.JLabel();
		jTextField5 = new javax.swing.JTextField();
		jScrollPane1 = new javax.swing.JScrollPane();
		jList1 = new javax.swing.JList();
		jButton1 = new javax.swing.JButton();
		jButton3 = new javax.swing.JButton();
		jLabel1 = new javax.swing.JLabel();
		jPanel3 = new javax.swing.JPanel();
		jLabel7 = new javax.swing.JLabel();
		jTextField6 = new javax.swing.JTextField();
		jButton4 = new javax.swing.JButton();
		jCheckBox1 = new javax.swing.JCheckBox();

		setPreferredSize(new java.awt.Dimension(500, 250));

		jPanel2
		        .setBorder(javax.swing.BorderFactory
		                .createTitledBorder("Input"));

		jLabel6.setText("Input File");

		jTextField5
		        .setToolTipText("Enter path to an input file or choose Select");

		jList1.setModel(this.dlm);
		jList1.setMaximumSize(new java.awt.Dimension(200, 50));
		jList1.setMinimumSize(new java.awt.Dimension(200, 50));
		jList1.setName("input.dataInfo"); // NOI18N
		jList1.setPrototypeCellValue("abcdefghijklmnopqrstuvwxyzABCDEFGHI");
		jScrollPane1.setViewportView(jList1);

		jButton1.setText("Select");
		jButton1.setToolTipText("Click to select files for input");
		jButton1.setActionCommand("addInputFile");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addInputFileHandler(evt);
			}
		});

		jButton3.setText("Remove");
		jButton3.setToolTipText("Click to remove selected files");
		jButton3.setActionCommand("removeInputFile");
		jButton3.setMaximumSize(new java.awt.Dimension(81, 29));
		jButton3.setMinimumSize(new java.awt.Dimension(81, 29));
		jButton3.setPreferredSize(new java.awt.Dimension(81, 29));
		jButton3.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				removeInputFile(evt);
			}
		});

		jLabel1.setText("Selected");

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(
		        jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout
		        .setHorizontalGroup(jPanel2Layout
		                .createParallelGroup(
		                        javax.swing.GroupLayout.Alignment.LEADING)
		                .addGroup(
		                        jPanel2Layout
		                                .createSequentialGroup()
		                                .addContainerGap()
		                                .addGroup(
		                                        jPanel2Layout
		                                                .createParallelGroup(
		                                                        javax.swing.GroupLayout.Alignment.LEADING)
		                                                .addComponent(jLabel6)
		                                                .addComponent(jLabel1))
		                                .addPreferredGap(
		                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		                                .addGroup(
		                                        jPanel2Layout
		                                                .createParallelGroup(
		                                                        javax.swing.GroupLayout.Alignment.LEADING,
		                                                        false)
		                                                .addComponent(
		                                                        jScrollPane1)
		                                                .addComponent(
		                                                        jTextField5,
		                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
		                                                        273,
		                                                        Short.MAX_VALUE))
		                                .addPreferredGap(
		                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		                                .addGroup(
		                                        jPanel2Layout
		                                                .createParallelGroup(
		                                                        javax.swing.GroupLayout.Alignment.LEADING)
		                                                .addComponent(
		                                                        jButton1,
		                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
		                                                        104,
		                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
		                                                .addComponent(
		                                                        jButton3,
		                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
		                                                        104,
		                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
		                                .addContainerGap()));
		jPanel2Layout
		        .setVerticalGroup(jPanel2Layout
		                .createParallelGroup(
		                        javax.swing.GroupLayout.Alignment.LEADING)
		                .addGroup(
		                        jPanel2Layout
		                                .createSequentialGroup()
		                                .addGroup(
		                                        jPanel2Layout
		                                                .createParallelGroup(
		                                                        javax.swing.GroupLayout.Alignment.BASELINE)
		                                                .addComponent(jLabel6)
		                                                .addComponent(
		                                                        jTextField5,
		                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
		                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
		                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
		                                                .addComponent(jButton1))
		                                .addGroup(
		                                        jPanel2Layout
		                                                .createParallelGroup(
		                                                        javax.swing.GroupLayout.Alignment.LEADING)
		                                                .addGroup(
		                                                        jPanel2Layout
		                                                                .createSequentialGroup()
		                                                                .addGap(
		                                                                        8,
		                                                                        8,
		                                                                        8)
		                                                                .addGroup(
		                                                                        jPanel2Layout
		                                                                                .createParallelGroup(
		                                                                                        javax.swing.GroupLayout.Alignment.LEADING)
		                                                                                .addComponent(
		                                                                                        jScrollPane1,
		                                                                                        0,
		                                                                                        58,
		                                                                                        Short.MAX_VALUE)
		                                                                                .addComponent(
		                                                                                        jLabel1)))
		                                                .addGroup(
		                                                        jPanel2Layout
		                                                                .createSequentialGroup()
		                                                                .addPreferredGap(
		                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		                                                                .addComponent(
		                                                                        jButton3,
		                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
		                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
		                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
		                                .addContainerGap()));

		jPanel3.setBorder(javax.swing.BorderFactory
		        .createTitledBorder("Output"));

		jLabel7.setText("Output Directory");

		jTextField6
		        .setToolTipText("Enter path to directory for output or click Select");
		jTextField6.setName("output.basedir"); // NOI18N

		jButton4.setToolTipText("Click to choose an output directory");
		jButton4.setActionCommand("selectOutputDirectory");
		jButton4.setLabel("Select");
		jButton4.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				selectOutputDirectory(evt);
			}
		});

		jCheckBox1.setSelected(false);
		jCheckBox1.setText("Store directly in output directory");
		jCheckBox1
		        .setToolTipText("Check to save output directly in output directory instead of automatically appending user name and startup time");
		jCheckBox1.setName("omitUserTimePrefix"); // NOI18N

		javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(
		        jPanel3);
		jPanel3.setLayout(jPanel3Layout);
		jPanel3Layout
		        .setHorizontalGroup(jPanel3Layout
		                .createParallelGroup(
		                        javax.swing.GroupLayout.Alignment.LEADING)
		                .addGroup(
		                        jPanel3Layout
		                                .createSequentialGroup()
		                                .addContainerGap()
		                                .addComponent(jLabel7)
		                                .addPreferredGap(
		                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		                                .addGroup(
		                                        jPanel3Layout
		                                                .createParallelGroup(
		                                                        javax.swing.GroupLayout.Alignment.LEADING)
		                                                .addComponent(
		                                                        jCheckBox1)
		                                                .addGroup(
		                                                        jPanel3Layout
		                                                                .createSequentialGroup()
		                                                                .addComponent(
		                                                                        jTextField6,
		                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
		                                                                        218,
		                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
		                                                                .addGap(
		                                                                        18,
		                                                                        18,
		                                                                        18)
		                                                                .addComponent(
		                                                                        jButton4,
		                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
		                                                                        103,
		                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
		                                .addContainerGap()));
		jPanel3Layout
		        .setVerticalGroup(jPanel3Layout
		                .createParallelGroup(
		                        javax.swing.GroupLayout.Alignment.LEADING)
		                .addGroup(
		                        jPanel3Layout
		                                .createSequentialGroup()
		                                .addGroup(
		                                        jPanel3Layout
		                                                .createParallelGroup(
		                                                        javax.swing.GroupLayout.Alignment.BASELINE)
		                                                .addComponent(jLabel7)
		                                                .addComponent(jButton4)
		                                                .addComponent(
		                                                        jTextField6,
		                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
		                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
		                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
		                                .addPreferredGap(
		                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
		                                .addComponent(jCheckBox1)
		                                .addContainerGap(
		                                        javax.swing.GroupLayout.DEFAULT_SIZE,
		                                        Short.MAX_VALUE)));

		jLabel7.getAccessibleContext().setAccessibleName("OutputDirectory");

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(
		        javax.swing.GroupLayout.Alignment.LEADING).addComponent(
		        jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE,
		        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE,
		                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		layout
		        .setVerticalGroup(layout
		                .createParallelGroup(
		                        javax.swing.GroupLayout.Alignment.LEADING)
		                .addGroup(
		                        layout
		                                .createSequentialGroup()
		                                .addComponent(
		                                        jPanel2,
		                                        javax.swing.GroupLayout.PREFERRED_SIZE,
		                                        javax.swing.GroupLayout.DEFAULT_SIZE,
		                                        javax.swing.GroupLayout.PREFERRED_SIZE)
		                                .addPreferredGap(
		                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		                                .addComponent(
		                                        jPanel3,
		                                        javax.swing.GroupLayout.DEFAULT_SIZE,
		                                        javax.swing.GroupLayout.DEFAULT_SIZE,
		                                        Short.MAX_VALUE)));

		jPanel2.getAccessibleContext().setAccessibleName("InputFile");
	}// </editor-fold>//GEN-END:initComponents

    private void addInputFileHandler(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_addInputFileHandler
        if (evt.getActionCommand().equals("addInputFile")) {
            if (this.jTextField5 != null) {
                String s = this.jTextField5.getText();

                JFileChooser jfc = new JFileChooser(cwd);
                jfc.setFileFilter(new FileFilter() {
                    @Override
                    public String getDescription() {
                        return "mzxml,cdf,nc,mzdata";
                    }

                    @Override
                    public boolean accept(File f) {
                        if (f.isDirectory()) {
                            return true;
                        }
                        String extension = StringTools.getFileExtension(f
                                .getAbsolutePath());
                        // System.out.println("Checking extension: " +
                        // extension);
                        // List<String> formats =
                        // DataSourceFactory.getInstance()
                        // .getSupportedFormats();
                        String[] formats = new String[]{"mzxml", "cdf", "nc",
                            "mzdata"};
                        for (String s : formats) {
                            // System.out.println("Format: " + s);
                            if (extension.toLowerCase().endsWith(
                                    s.toLowerCase())) {
                                return true;
                            }
                        }
                        return false;
                    }
                });
                if (!s.equals("") && new File(s).isFile()) {
                    jfc.setSelectedFile(new File(s));
                }
                jfc.setMultiSelectionEnabled(true);
                int state = jfc.showOpenDialog(getTopLevelAncestor());
                if (state == JFileChooser.APPROVE_OPTION) {
                    File[] f = jfc.getSelectedFiles();
                    for (File file : f) {
                        if (file.isFile()) {
                            if (!this.dlm.contains(file.getAbsolutePath())) {
                                this.dlm.addElement(file.getAbsolutePath());
                            }
                        } else {
                            System.err.println("File " + file.getAbsolutePath()
                                    + " is not a regular file!");
                        }
                    }
                    cwd = f[0].getParentFile();
                    this.jTextField5.setText("");
                    userInputReceived(this.jList1, new ListDataEvent(
                            this.jList1, ListDataEvent.CONTENTS_CHANGED,
                            this.dlm.getSize() - f.length, f.length));
                }
            }
        }
    }// GEN-LAST:event_addInputFileHandler

    private void removeInputFile(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_removeInputFile
        if (evt.getActionCommand().equals("removeInputFile")) {
            if (this.jList1 != null) {
                Object[] indices = this.jList1.getSelectedValues();
                for (Object obj : indices) {
                    this.dlm.removeElement(obj);
                }
                validateContents(this.jList1, null);
            }
        }
    }// GEN-LAST:event_removeInputFile

    private void selectOutputDirectory(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_selectOutputDirectory
        if (evt.getActionCommand().equals("selectOutputDirectory")) {
            if (this.jTextField6 != null) {
                JFileChooser jfc = new JFileChooser(cod);
                jfc.setMultiSelectionEnabled(false);
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int state = jfc.showOpenDialog(getTopLevelAncestor());
                if (state == JFileChooser.APPROVE_OPTION) {
                    File f = jfc.getSelectedFile();
                    if (f.isDirectory()) {
                        this.jTextField6.setText(f.getAbsolutePath());
                    }
                    cod = f;

                }
            }
        }
    }// GEN-LAST:event_selectOutputDirectory

    public String[] getInputFiles() {
        String[] s = new String[this.dlm.size()];
        int i = 0;
        for (Object o : this.dlm.toArray()) {
            s[i] = (String) o;
        }
        return s;
    }
    private File cwd = null, cod = null;
    private DefaultListModel dlm = new DefaultListModel();
	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton3;
	private javax.swing.JButton jButton4;
	private javax.swing.JCheckBox jCheckBox1;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JLabel jLabel7;
	private javax.swing.JList jList1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JTextField jTextField5;
	private javax.swing.JTextField jTextField6;
	// End of variables declaration//GEN-END:variables
}
