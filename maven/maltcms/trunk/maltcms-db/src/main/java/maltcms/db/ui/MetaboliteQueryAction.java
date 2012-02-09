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
