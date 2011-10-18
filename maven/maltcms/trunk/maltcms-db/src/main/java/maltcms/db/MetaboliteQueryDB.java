package maltcms.db;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import maltcms.datastructures.ms.IMetabolite;
import maltcms.db.predicates.metabolite.MAggregatePredicateFactory;
import maltcms.db.predicates.metabolite.MetabolitePredicate;

import com.db4o.ObjectSet;
import com.db4o.query.Predicate;

public class MetaboliteQueryDB extends QueryDB<IMetabolite> {

    public MetaboliteQueryDB(String dblocation) {
        super(dblocation);
    }

    /**
     * @param dblocation
     * @param p
     */
    public MetaboliteQueryDB(String dblocation, Predicate<IMetabolite> p) {
        super(dblocation, p);
    }

    public MetaboliteQueryDB(String dblocation, String[] args,
            MAggregatePredicateFactory mapf) {
        super(dblocation, args, mapf);
    }

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
            } catch (InterruptedException e) {
                System.err.println(e.getLocalizedMessage());
            } catch (ExecutionException e) {
                System.err.println(e.getLocalizedMessage());
            }
        } else {
            System.out.println("Invalid number of arguments!");
            System.out.println(
                    "Usage: java maltcms.db.MetaboliteQuery /PATH/TO/DB [PREDICATES]");
        }
    }

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
