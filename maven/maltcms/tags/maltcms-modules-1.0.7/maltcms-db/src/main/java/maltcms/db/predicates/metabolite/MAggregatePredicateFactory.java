package maltcms.db.predicates.metabolite;

import java.lang.reflect.Method;
import java.util.ArrayList;

import maltcms.datastructures.ms.IMetabolite;
import maltcms.tools.PublicMemberGetters;

import com.db4o.query.Predicate;

import cross.datastructures.tools.EvalTools;

public class MAggregatePredicateFactory implements IAggregatePredicateFactory<IMetabolite> {

    private Predicate<IMetabolite> defaultPredicate = null;

    public MAggregatePredicateFactory(Predicate<IMetabolite> p) {
        EvalTools.notNull(p, this);
        this.defaultPredicate = p;
    }

    /* (non-Javadoc)
     * @see maltcms.db.predicates.metabolite.IAggregatePredicateFactory#digestCommandLine(java.lang.String[])
     */
    public Predicate<IMetabolite> digestCommandLine(String[] args) {
        PublicMemberGetters<IMetabolite> pmg = new PublicMemberGetters<IMetabolite>(IMetabolite.class);
        ArrayList<MetabolitePredicate> nl = new ArrayList<MetabolitePredicate>();
        for (String s : args) {
            split(pmg, nl, s);
        }
        if (nl.isEmpty()) {
            System.out.println("Using default match all predicate");
            return this.defaultPredicate;
        } else {
            return new MAggregatePredicate(nl);
        }
    }

    private void split(PublicMemberGetters<IMetabolite> pmg,
            ArrayList<MetabolitePredicate> nl, String s) {
        String[] split = s.split("=");
        if (split.length == 2) {
            String method = split[0];
            Method m = pmg.getMethodForGetterName(method);
            System.out.println(m.getGenericReturnType());
            System.out.println("Found the following method: " + m.getName());
            String arg = split[1];
            if (arg.startsWith("[") && arg.endsWith("]")) {//NumPredicate
                handleNumRange(nl, m, arg);
            } else {//StringPredicate
                handleString(nl, m, arg);
            }
        } else {
            System.err.println("Could not evaluate command-line!");
        }
    }

    private void handleString(ArrayList<MetabolitePredicate> sl, Method m,
            String arg) {
        MStringMatchPredicate msmp = null;
        if (arg.endsWith("<")) {//substring matching
            msmp = new MStringContainsPredicate(arg.substring(0, arg.length() - 1));
            System.out.println("Substring matching on " + arg.substring(0, arg.length() - 1));
        } else {
            msmp = new MStringMatchPredicate(arg);

        }
        if (msmp != null) {
            msmp.setMethodOnTargetType(m);
            sl.add(msmp);
        }
    }

    private void handleNumRange(ArrayList<MetabolitePredicate> nl, Method m,
            String arg) {
        arg = arg.substring(1, arg.length() - 1);
        String[] range = arg.split(",");
        if (range.length > 0 && range.length <= 2) {
            MNumRangePredicate mnrp = null;
            try {
                mnrp = handleIntegral(range, m);
            } catch (NumberFormatException nfe) {
                try {
                    mnrp = handleFP(range, m);
                } catch (NumberFormatException nfe2) {
//					try {
//						Byte b1 = Byte.parseByte(range[0]);
//						Byte b2 = Byte.parseByte(range[1]);
//						mnrp = new NumRangePredicate<T>(b1,b2);
//					} catch (NumberFormatException nfe3) {
                    System.err.println("Could not parse strings as numbers: " + range[0] + " and " + range[1]);
                    //}
                }
            }

            if (mnrp != null) {
                mnrp.setMethodOnTargetType(m);
                nl.add(mnrp);
            }
        } else {
            System.err.println("Could not evaluate numerical arguments!");
        }
    }

    private MNumRangePredicate handleFP(String[] range, Method m) {
        MNumRangePredicate mnrp;
        Double d1 = Double.MIN_VALUE, d2 = Double.MAX_VALUE;
        if (range.length == 2) {
            d1 = Double.parseDouble(range[0]);
            d2 = Double.parseDouble(range[1]);
        } else if (range.length == 1) {
            d1 = Double.parseDouble(range[0]);
            d2 = d1;
        }

        if (d1.floatValue() >= Float.MIN_VALUE && d1.floatValue() <= Float.MAX_VALUE && d2.floatValue() >= Float.MIN_VALUE && d2.floatValue() <= Float.MAX_VALUE) {
            Float f1 = d1.floatValue();
            Float f2 = d2.floatValue();
            mnrp = new MNumRangePredicate(f1, f2, m);
        } else {
            mnrp = new MNumRangePredicate(d1, d2, m);
        }
        //mnrp = new MNumRangePredicate(d1,d2,m);
        return mnrp;
    }

    private MNumRangePredicate handleIntegral(String[] range, Method m) {
        MNumRangePredicate mnrp;
        Long l1 = Long.MIN_VALUE, l2 = Long.MAX_VALUE;
        if (range.length == 2) {
            l1 = Long.parseLong(range[0]);
            l2 = Long.parseLong(range[1]);
        } else if (range.length == 1) {
            l1 = Long.parseLong(range[0]);
            l2 = l1;
        }
        if (l1.intValue() >= Integer.MIN_VALUE && l1.intValue() <= Integer.MAX_VALUE && l2.intValue() >= Integer.MIN_VALUE && l2.intValue() <= Integer.MAX_VALUE) {
            Integer i1 = l1.intValue();
            Integer i2 = l2.intValue();
            mnrp = new MNumRangePredicate(i1, i2, m);
        } else {
            mnrp = new MNumRangePredicate(l1, l2, m);
        }
        return mnrp;
    }
}
