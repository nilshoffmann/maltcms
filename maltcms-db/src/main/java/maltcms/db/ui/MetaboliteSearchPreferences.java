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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

/**
 * <p>MetaboliteSearchPreferences class.</p>
 *
 * @author Rolf Hilker
 *
 *
 *
 * Opens a JFrame, which allows to specify the search preferences for a certain
 *
 * metabolite
 * 
 */
public class MetaboliteSearchPreferences extends JFrame {

    /**
     *
     *
     *
     */
    private static final long serialVersionUID = 1L;

    private static final Font BORDER_FONT = new Font("Dialog", Font.BOLD, 12);

    private static final Dimension DIMENSION = new Dimension(160, 25);

    private MetaboliteViewModel mvm = null;

    private HashMap<Integer, JTextField> textFields = new HashMap<>();

    private HashMap<Integer, JPanel> panels = new HashMap<>();

    private JButton submitFilterButton = null;

    private JPanel buttonPanel = null;

    private boolean error = false;

    private static MetaboliteSearchPreferences msp = null;

    /**
     *
     * Returns an instance of this class, if there is not an instance yet
     *
     * @param mvm a {@link maltcms.db.ui.MetaboliteViewModel} object.
     * @return msp
     */
    public static MetaboliteSearchPreferences getInstance(
            MetaboliteViewModel mvm) {

        if (msp == null) {

            msp = new MetaboliteSearchPreferences(mvm);

        }

        return msp;

    }

    /**
     *
     * Constructor, configures all standard window parameters
     *
     *
     *
     * @param mvm
     *
     */
    private MetaboliteSearchPreferences(MetaboliteViewModel mvm) {

        this.mvm = mvm;

        this.setTitle("Metabolite Search Preferences");

        this.setSize(800, 600);

        Box outerOuterBox = new Box(BoxLayout.Y_AXIS);

        Box outerBox = new Box(BoxLayout.X_AXIS);

        this.textFields.put(0, createTextField(mvm.COMMENTS));

        this.textFields.put(1, createTextField(mvm.DATE));

        this.textFields.put(2, createTextField(mvm.FORMULA));

        this.textFields.put(3, createTextField(mvm.ID));

        this.textFields.put(4, createTextField(mvm.MASSWEIGHT));

        this.textFields.put(5, createTextField(mvm.MASSSPECTRUM));

        this.textFields.put(6, createTextField(mvm.MAXINTENSITY));

        this.textFields.put(7, createTextField(mvm.MAXINTNORM));

        this.textFields.put(8, createTextField(mvm.MAXMASS));

        this.textFields.put(9, createTextField(mvm.MININTENSITY));

        this.textFields.put(10, createTextField(mvm.MININTNORM));

        this.textFields.put(11, createTextField(mvm.MINMASS));

        this.textFields.put(12, createTextField(mvm.NAME));

        this.textFields.put(13, createTextField(mvm.RETINDEX));

        this.textFields.put(14, createTextField(mvm.RETTIME));

        this.textFields.put(15, createTextField(mvm.RETTIMEUNIT));

        this.textFields.put(16, createTextField(mvm.SP));

        this.textFields.put(17, createTextField(mvm.SCANINDEX));

        this.textFields.put(18, createTextField(mvm.SHORTNAME));

        // Put all textfields into their panels
        final int tfHashSize = textFields.size();

        for (int i = 0; i < tfHashSize; i++) {

            JTextField tf = this.textFields.get(i);

            String name = tf.getName();

            this.panels.put(i, getAPanel(name, tf));

        }

        this.textFields = addTooltipsToTextFields(this.textFields, tfHashSize);

        // add all components in boxes:
        Box innerBox1 = new Box(BoxLayout.Y_AXIS);

        innerBox1.add(this.panels.get(0));

        innerBox1.add(this.panels.get(1));

        innerBox1.add(this.panels.get(2));

        innerBox1.add(this.panels.get(3));

        innerBox1.add(this.panels.get(4));

        Box innerBox2 = new Box(BoxLayout.Y_AXIS);

        innerBox2.add(this.panels.get(5));

        innerBox2.add(this.panels.get(6));

        innerBox2.add(this.panels.get(7));

        innerBox2.add(this.panels.get(8));

        innerBox2.add(this.panels.get(9));

        Box innerBox3 = new Box(BoxLayout.Y_AXIS);

        innerBox3.add(this.panels.get(10));

        innerBox3.add(this.panels.get(11));

        innerBox3.add(this.panels.get(12));

        innerBox3.add(this.panels.get(13));

        innerBox3.add(this.panels.get(14));

        Box innerBox4 = new Box(BoxLayout.Y_AXIS);

        innerBox4.add(this.panels.get(15));

        innerBox4.add(this.panels.get(16));

        innerBox4.add(this.panels.get(17));

        innerBox4.add(this.panels.get(18));

        outerBox.add(innerBox1);

        outerBox.add(innerBox2);

        outerBox.add(innerBox3);

        outerBox.add(innerBox4);

        this.buttonPanel = new JPanel(new BorderLayout());

        this.buttonPanel.add(getSubmitFilterButton(), BorderLayout.CENTER);

        outerOuterBox.add(outerBox);

        outerOuterBox.add(this.buttonPanel);

        this.add(outerOuterBox);

        this.setVisible(true);

        this.pack();

    }

    /**
     *
     * Adds Tooltips to a HashMap of JTextFields
     *
     *
     *
     * @param textFieldsHash
     *
     * @param tfHashSize
     *
     */
    private HashMap<Integer, JTextField> addTooltipsToTextFields(
            HashMap<Integer, JTextField> textFieldsHash, int tfHashSize) {

        String stringHelp = "<html>This field takes all variants of strings to filter"
                + "<ul><li>Enable substring filtering by adding a '&lt;' like: fie&lt;<li></html>";

        String intHelp = "<html>This field takes two variants:"
                + "<ul><li>A single whole number (Integer) like: 1<li>"
                + "A range of integers like: 10,20</ul></html>";

        String doubleHelp = "<html>This field takes two variants:"
                + "<ul><li>A single floating-point number (Double) like: 1.54<li>"
                + "A range of floating-point numers like: 10.54,20.45</ul></html>";

        for (int i = 0; i < tfHashSize; ++i) {

            if (i < 4 || i == 12 || i == 15 || i == 16 || i == 18) {

                textFieldsHash.get(i).setToolTipText(stringHelp);

            }

            if (i == 4 || i == 17) {

                textFieldsHash.get(i).setToolTipText(intHelp);

            }

            if ((i > 5 && i < 12) || i == 13 || i == 14) {

                textFieldsHash.get(i).setToolTipText(doubleHelp);

            }

        }

        return textFieldsHash;

    }

    /**
     *
     * creates a panel with a titled border & a textfield
     *
     *
     *
     * @return panel
     *
     */
    private JPanel getAPanel(String title, JTextField jtf) {

        JPanel panel = new JPanel();

        TitledBorder tb = setBorder(title);

        panel.setBorder(tb);

        panel.add(jtf);

        return panel;

    }

    /**
     *
     * Creates titled border for a panel
     *
     *
     *
     * @param title
     *
     * @return tb
     *
     */
    private TitledBorder setBorder(String title) {

        TitledBorder tb = new TitledBorder(title + " Filter");

        tb.setTitleFont(BORDER_FONT);

        return tb;

    }

    /**
     *
     * Creates textfield for filtering
     *
     *
     *
     * @return jtf
     *
     */
    private JTextField createTextField(String name) {

        JTextField jtf = new JTextField();// begrenze ("",20)

        jtf.setName(name);

        jtf.setPreferredSize(DIMENSION);

        return jtf;

    }

    /**
     *
     * submits filter
     *
     *
     *
     * @return
     *
     */
    private JButton getSubmitFilterButton() {

        if (this.submitFilterButton == null) {

            ActionListener al = new FilterButtonActionListener(mvm, error,
                    textFields, this);

            this.submitFilterButton = new JButton("Submit Filter");

            this.submitFilterButton.addActionListener(al);

        }

        return this.submitFilterButton;

    }

    /*

     * comments //string dateTF); //string formulaTF); //string idTF); //string

     * massWeightTF); //int --

     * massSpectrumTF);//cross.datastructures.tuple.Tuple2D

     * <ucar.ma2.ArrayDouble$D1, ucar.ma2.ArrayInt$D1> maxIntensityTF); //double

     * -- maxIntNormalizedTF);//double -- maxMassTF); //double --

     * minIntensityTF); //double -- minIntNormalizedTF);//double -- minMassTF);

     * //double -- nameTF); //string retentionIndexTF); //double --

     * retentionTimeTF); //double -- retentionTimeUnitTF);//string spTF);

     * //string scanIndexTF); //int -- shortNameTF); //string

     */
}
