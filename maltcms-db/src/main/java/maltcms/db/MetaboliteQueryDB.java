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
package maltcms.db;

import com.db4o.ObjectSet;
import com.db4o.query.Predicate;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import maltcms.datastructures.ms.IMetabolite;
import maltcms.db.predicates.metabolite.MAggregatePredicateFactory;
import maltcms.db.predicates.metabolite.MetabolitePredicate;

/**
 * <p>MetaboliteQueryDB class.</p>
 *
 * @author hoffmann
 * 
 */
public class MetaboliteQueryDB extends QueryDB<IMetabolite> {

    /**
     * <p>Constructor for MetaboliteQueryDB.</p>
     *
     * @param dblocation a {@link java.lang.String} object.
     */
    public MetaboliteQueryDB(String dblocation) {
        super(dblocation);
    }

    /**
     * <p>Constructor for MetaboliteQueryDB.</p>
     *
     * @param dblocation a {@link java.lang.String} object.
     * @param p a {@link com.db4o.query.Predicate} object.
     */
    public MetaboliteQueryDB(String dblocation, Predicate<IMetabolite> p) {
        super(dblocation, p);
    }

    /**
     * <p>Constructor for MetaboliteQueryDB.</p>
     *
     * @param dblocation a {@link java.lang.String} object.
     * @param args an array of {@link java.lang.String} objects.
     * @param mapf a {@link maltcms.db.predicates.metabolite.MAggregatePredicateFactory} object.
     */
    public MetaboliteQueryDB(String dblocation, String[] args,
            MAggregatePredicateFactory mapf) {
        super(dblocation, args, mapf);
    }

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {

        if (args.length >= 1) {//at least the db has to be given
            MetaboliteQueryDB mqdb = null;
            if (args.length > 1) {
                String[] predicates = new String[args.length - 1];
                System.arraycopy(args, 1, predicates, 0, predicates.length);
                MAggregatePredicateFactory mapf = new MAggregatePredicateFactory(new MetabolitePredicate() {
                    /**
                     *
                     */
                    private static final long serialVersionUID = -3219329449275655325L;

                    @Override
                    public boolean match(IMetabolite arg0) {
                        return true;
                    }
                });
                mqdb = new MetaboliteQueryDB(args[0], predicates, mapf);
            } else {
                mqdb = new MetaboliteQueryDB(args[0]);
            }
            Future<ObjectSet<IMetabolite>> c = mqdb.invoke(mqdb.getCallable());
            try {
                ObjectSet<IMetabolite> os = c.get();
                //System.out.println("Found the following metabolites:");
                StringBuffer sb = new StringBuffer();
                int i = 0;
                for (IMetabolite m : os) {
                    if (i % 49 == 0) {
                        //System.out.println(sb);
                        sb = new StringBuffer();
                        i = 0;
                    }
                    sb.append(m.toString());
                    i++;
                }
            } catch (InterruptedException | ExecutionException e) {
                System.err.println(e.getLocalizedMessage());
            }
        } else {
            System.out.println("Invalid number of arguments!");
            System.out.println(
                    "Usage: java maltcms.db.MetaboliteQuery /PATH/TO/DB [PREDICATES]");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Predicate<IMetabolite> getDefaultPredicate() {
        return new Predicate<IMetabolite>() {
            /**
             *
             */
            private static final long serialVersionUID = 7856118698961191698L;

            @Override
            public boolean match(IMetabolite arg0) {
                return true;
            }
        };
    }
}
