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

import java.awt.event.ActionEvent;

import javax.swing.Icon;
import javax.swing.JTextField;

import maltcms.datastructures.ms.IMetabolite;
import maltcms.db.predicates.metabolite.IAggregatePredicateFactory;
import maltcms.db.predicates.metabolite.MAggregatePredicate;
import maltcms.db.predicates.metabolite.MAggregatePredicateFactory;

import com.db4o.query.Predicate;

public class MetaboliteQueryAction extends javax.swing.AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 8174435563633316055L;
    private MetaboliteViewModel mvm = null;
    private Predicate<IMetabolite> ap = null;
    private JTextField jtf = null;
    private String filter = null;
    private IAggregatePredicateFactory af = null;

    public MetaboliteQueryAction(String name, Icon icon, JTextField jtf, MetaboliteViewModel mvm) {
        super(name, icon);
        this.af = new MAggregatePredicateFactory(new MAggregatePredicate());
        this.jtf = jtf;
        this.mvm = mvm;
    }

    public MetaboliteQueryAction(String name, Icon icon, String filter, MetaboliteViewModel mvm) {
        super(name, icon);
        this.af = new MAggregatePredicateFactory(new MAggregatePredicate());
        this.filter = filter;
        this.mvm = mvm;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
//		SwingWorker<Void,Integer> sw = new SwingWorker<Void,Integer>() {
//		
//			@Override
//			protected Void doInBackground() throws Exception {
        ap = af.digestCommandLine(jtf.getText().split(" "));
        System.out.println("Parsed command line: " + ap.toString());
        if (mvm != null && ap != null) {
            System.out.println("Executing query");
            mvm.query(ap);
        }
//				return Void.TYPE.newInstance();
//			}
//		};
//		sw.execute();
    }
}
