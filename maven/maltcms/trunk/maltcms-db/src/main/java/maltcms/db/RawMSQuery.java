/**
 * 
 */
package maltcms.db;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

import maltcms.datastructures.ms.IMetabolite;
import maltcms.db.predicates.metabolite.MetabolitePredicate;
import maltcms.tools.ArrayTools;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;

import com.db4o.ObjectSet;

import cross.datastructures.tuple.Tuple2D;
import maltcms.db.predicates.metabolite.MSimilarityPredicate;
import maltcms.db.predicates.metabolite.MetaboliteSimilarity;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE

 *
 */
public class RawMSQuery implements IDBQuery<MSimilarityPredicate, IMetabolite> {

    private QueryDB<IMetabolite> mqdb;
    private String dbloc;
    private double threshold = 0;
    private Tuple2D<Array, Array> query;

    /**
     * @param args
     */
    public static void main(String[] args) {
        //opening dialog
        String dblocation = null;
        if (args.length == 0) {
            JFileChooser jfc = new JFileChooser();
            int option = jfc.showOpenDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
                dblocation = jfc.getSelectedFile().getAbsolutePath();
            }
        } else {
            dblocation = args[0];
        }

        if (dblocation == null) {
            System.err.println("No db selected, exiting!");
            System.exit(1);
        }
        //dblocation = "http://default@127.0.0.1:8888";
        String serverfile = "/Users/nilshoffmann/Documents/workspace/MetaboliteDBRolf/res/db4o/db_new.db4o";
        Array masses = ArrayTools.indexArray(701, 0);
        Array intensities = new ArrayInt.D1(701);
        Index intIdx = intensities.getIndex();
        intensities.setInt(intIdx.set(72), 2000230);
        intensities.setInt(intIdx.set(73), 1213231);
        intensities.setInt(intIdx.set(251), 100000);
        //System.out.println(masses);
        //System.out.println(intensities);
        RawMSQuery rmq = new RawMSQuery(new Tuple2D<Array, Array>(masses,
                intensities));
        rmq.setDB(serverfile);//dblocation);
        URL url;
//		ObjectServer os = null;
//		try {
//			url = new URL(dblocation);
//			int portnumber = url.getPort();
//			os = Db4o.openServer(serverfile, 8888);
//			os.grantAccess("default", "default");
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		}
        //rmq.setQuery(masses, intensities);
        MetaboliteSimilarity ms = new MetaboliteSimilarity(masses, intensities,
                0.6,
                10, true);
        MSimilarityPredicate msp = new MSimilarityPredicate(ms);
        //synchronized(os) {
        for (Tuple2D<Double, IMetabolite> im : rmq.getBestHits(10, msp)) {
            System.out.println(im.getFirst() + ": " + im.getSecond().getName());

        }
        //}
        //os.close();
        System.exit(0);
    }

    public RawMSQuery(Tuple2D<Array, Array> t) {
        this.query = t;
    }


    /* (non-Javadoc)
     * @see maltcms.db.IDBQuery#setDB(java.lang.String)
     */
    @Override
    public void setDB(final String dbLocation) {
        this.dbloc = dbLocation;
    }

    /* (non-Javadoc)
     * @see maltcms.db.IDBQuery#getBestHits(int)
     */
    @Override
    public Collection<Tuple2D<Double, IMetabolite>> getBestHits(final int k,
            final MSimilarityPredicate ssp) {
        if (this.mqdb == null) {
            this.mqdb = new MetaboliteQueryDB(dbloc, ssp);
        } else {
            this.mqdb.setPredicate(ssp);
        }
        Future<ObjectSet<IMetabolite>> c = mqdb.invoke(mqdb.getCallable());
        try {
            ObjectSet<IMetabolite> os = c.get();
            return ssp.getSimilaritiesAboveThreshold().subList(0, k-1);
        } catch (InterruptedException ex) {
            Logger.getLogger(RawMSQuery.class.getName()).log(Level.SEVERE, null,
                    ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(RawMSQuery.class.getName()).log(Level.SEVERE, null,
                    ex);
        }
        return Collections.emptyList();
    }
}
