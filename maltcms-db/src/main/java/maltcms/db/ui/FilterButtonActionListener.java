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

import com.db4o.query.Predicate;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.JTextField;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.ms.IMetabolite;
import maltcms.db.predicates.metabolite.IAggregatePredicateFactory;
import maltcms.db.predicates.metabolite.MAggregatePredicate;
import maltcms.db.predicates.metabolite.MAggregatePredicateFactory;

/**
 * <p>FilterButtonActionListener class.</p>
 *
 * @author Rolf Hilker
 * 
 */
@Slf4j
public class FilterButtonActionListener implements ActionListener {

    private IAggregatePredicateFactory af = null;

    private Predicate<IMetabolite> ap = null;

    private MetaboliteViewModel mvm = null;

    private boolean error = false;

    private HashMap<Integer, JTextField> textFields = null;

    private MetaboliteSearchPreferences msp = null;

    /**
     * <p>Constructor for FilterButtonActionListener.</p>
     *
     * @param mvm a {@link maltcms.db.ui.MetaboliteViewModel} object.
     * @param error a boolean.
     * @param textFields a {@link java.util.HashMap} object.
     * @param msp a {@link maltcms.db.ui.MetaboliteSearchPreferences} object.
     */
    public FilterButtonActionListener(MetaboliteViewModel mvm, boolean error, HashMap<Integer, JTextField> textFields, MetaboliteSearchPreferences msp) {

        this.mvm = mvm;

        this.error = error;

        this.textFields = textFields;

        this.msp = msp;

        this.af = new MAggregatePredicateFactory(new MAggregatePredicate());

    }

    /** {@inheritDoc} */
    @Override

    public void actionPerformed(ActionEvent e) {

        int length = textFields.size();

        String[] filterA = new String[length];

        for (int i = 0; i < length; i++) {

            filterA[i] = getFilter(textFields.get(i));

        }

        //check if there is a wrong input to a filter textfield
        if (error == false) {

            String filter = "";

            for (int i = 0; i < length; i++) {

                filter = filter + filterA[i];

            }

            log.info(filter.toString());

            submitFilter(filter);

            closeWindow();

        } else {

            error = false;

        }

    }

    /**
     *
     * reads out a textfield & checks the obtained values
     *
     *
     *
     * @param jtf
     *
     * @return filter
     *
     */
    private String getFilter(JTextField jtf) {

        int intValue = 0;

        double doubleValue = 0.0;

        String input = jtf.getText();

        String methodName = jtf.getName();

        log.info("NAMEEE: " + methodName); //Mit MassWeight stimmt noch was net, teste alle Namen!

        if (input.length() != 0) {

            String methodReturnType = mvm.getMethodForGetterName(methodName).getGenericReturnType().toString();

            //strings don't need to be checked!
            //check for correct integer input
            if (methodReturnType.contains("integer") == true) {

                try {

                    intValue = Integer.parseInt(input);

                    input = methodName + "=" + input + " ";

                } catch (NumberFormatException e) {

                    jtf.setText("Enter number like: 3");

                    error = true;

                }

            } else //check for correct double range input
            if (methodReturnType.contains("double") == true) {

                if (input.contains(",")) {

                    String[] check = input.split(",");

                    if (check.length == 2) {

                        try {

                            doubleValue = Double.parseDouble(check[0]);

                            doubleValue = Double.parseDouble(check[1]);

                            input = methodName + "=[" + input + "] ";

                        } catch (NumberFormatException e) {

                            jtf.setText("Enter range like: 5,6");

                            error = true;

                        }

                    }

                } else {

                    try {

                        doubleValue = Double.parseDouble(input);

                        input = methodName + "=" + input + " ";

                    } catch (NumberFormatException e) {

                        jtf.setText("Enter number like: 4.0");

                        error = true;

                    }

                }

            } else {

                input = methodName + "=" + input + " ";

            }

        }

        return input;

    }

    /**
     *
     * Submit filter method for the extra Search Preferences Frame
     *
     * @param filter a {@link java.lang.String} object.
     */
    public void submitFilter(String filter) {

        // SwingWorker<Void,Integer> sw = new SwingWorker<Void,Integer>() {
        // @Override
        // protected Void doInBackground() throws Exception {
        ap = af.digestCommandLine(filter.split(" "));

        log.info("Parsed command line: " + filter);

        if (mvm != null && ap != null) {

            log.info("Executing query");

            mvm.query(ap);

        }

    }

    /**
     *
     * Closes the preferences screen after submitting a correct filter
     *
     */
    private void closeWindow() {

        this.msp.setVisible(false);

        //this.msp = null;
    }

}
