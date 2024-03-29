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
import javax.swing.Icon;
import javax.swing.JTextField;

import maltcms.datastructures.ms.IMetabolite;
import maltcms.db.predicates.metabolite.IAggregatePredicateFactory;
import maltcms.db.predicates.metabolite.MAggregatePredicate;
import maltcms.db.predicates.metabolite.MAggregatePredicateFactory;
import org.slf4j.LoggerFactory;

/**
 * <p>MetaboliteQueryAction class.</p>
 *
 * @author Nils Hoffmann
 * 
 */

public class MetaboliteQueryAction extends javax.swing.AbstractAction {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(MetaboliteQueryAction.class);

    /**
     *
     */
    private static final long serialVersionUID = 8174435563633316055L;
    private MetaboliteViewModel mvm = null;
    private Predicate<IMetabolite> ap = null;
    private JTextField jtf = null;
    private String filter = null;
    private IAggregatePredicateFactory af = null;

    /**
     * <p>Constructor for MetaboliteQueryAction.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param icon a {@link javax.swing.Icon} object.
     * @param jtf a {@link javax.swing.JTextField} object.
     * @param mvm a {@link maltcms.db.ui.MetaboliteViewModel} object.
     */
    public MetaboliteQueryAction(String name, Icon icon, JTextField jtf, MetaboliteViewModel mvm) {
        super(name, icon);
        this.af = new MAggregatePredicateFactory(new MAggregatePredicate());
        this.jtf = jtf;
        this.mvm = mvm;
    }

    /**
     * <p>Constructor for MetaboliteQueryAction.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param icon a {@link javax.swing.Icon} object.
     * @param filter a {@link java.lang.String} object.
     * @param mvm a {@link maltcms.db.ui.MetaboliteViewModel} object.
     */
    public MetaboliteQueryAction(String name, Icon icon, String filter, MetaboliteViewModel mvm) {
        super(name, icon);
        this.af = new MAggregatePredicateFactory(new MAggregatePredicate());
        this.filter = filter;
        this.mvm = mvm;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
//		SwingWorker<Void,Integer> sw = new SwingWorker<Void,Integer>() {
//		
//			@Override
//			protected Void doInBackground() throws Exception {
        ap = af.digestCommandLine(jtf.getText().split(" "));
        log.info("Parsed command line: " + ap.toString());
        if (mvm != null && ap != null) {
            log.info("Executing query");
            mvm.query(ap);
        }
//				return Void.TYPE.newInstance();
//			}
//		};
//		sw.execute();
    }
}
