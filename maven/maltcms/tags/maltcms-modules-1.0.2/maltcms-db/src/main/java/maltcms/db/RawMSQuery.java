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

import javax.swing.JFileChooser;

import maltcms.datastructures.ms.IMetabolite;
import maltcms.db.predicates.metabolite.MScanSimilarityPredicate;
import maltcms.db.predicates.metabolite.MetabolitePredicate;
import maltcms.db.similarities.MetaboliteSimilarity;
import maltcms.tools.ArrayTools;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;

import com.db4o.ObjectSet;

import cross.datastructures.tuple.Tuple2D;



/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE

 *
 */
public class RawMSQuery implements IDBQuery<MetabolitePredicate,IMetabolite>{

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
		RawMSQuery rmq = new RawMSQuery(new Tuple2D<Array,Array>(masses,intensities));
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
		MScanSimilarityPredicate ssp = new MScanSimilarityPredicate(new Tuple2D<Array,Array>(masses,intensities));
		ssp.setThreshold(0.8);
		
		MetabolitePredicate mapID = new MetabolitePredicate() {
		
			/**
             * 
             */
            private static final long serialVersionUID = 1L;

			@Override
			public boolean match(IMetabolite arg0) {
				return true;
			}
		};
		//synchronized(os) {
			for(Tuple2D<Double,IMetabolite> im:rmq.getBestHits(10, ssp)){
				System.out.println(im.getFirst()+": "+im.getSecond().getName());
			
			}
		//}
		//os.close();
		System.exit(0);
	}
	
	public RawMSQuery(Tuple2D<Array,Array> t) {
		this.query = t;
	}
	/* (non-Javadoc)
     * @see maltcms.db.IDBQuery#setThreshold(double)
     */
	public void setThreshold(final double d) {
		this.threshold = d;
	}
	
	/* (non-Javadoc)
     * @see maltcms.db.IDBQuery#setDB(java.lang.String)
     */
	public void setDB(final String dbLocation) {
		this.dbloc = dbLocation;
	}
	
	/* (non-Javadoc)
     * @see maltcms.db.IDBQuery#getBestHits(int)
     */
	public Collection<Tuple2D<Double,IMetabolite>> getBestHits(final int k,final MetabolitePredicate ssp) {
		if(this.mqdb == null) {
			this.mqdb = new MetaboliteQueryDB(dbloc,ssp);
		}else{
			this.mqdb.setPredicate(ssp);
		}
		Future<ObjectSet<IMetabolite>> c = mqdb.invoke(mqdb.getCallable());
		try {
			ObjectSet<IMetabolite> os = c.get();
			System.out.println("Query returned "+os.size()+" Metabolites!");
			StringBuffer sb = new StringBuffer();
			ArrayList<IMetabolite> res = new ArrayList<IMetabolite>(os.size());
			int i = 0;
			//synchronized(os) {
				for(IMetabolite m:os) {
					res.add(m);
					if(i%49==0) {
						//System.out.println(sb);
						sb = new StringBuffer();
						i=0;
					}
					sb.append(m.toString());
					i++;
				}
			//}
			//System.out.println(sb.toString());
//			if(ssp instanceof MScanSimilarityPredicate) {
//				List<Tuple2D<Double,IMetabolite>> l = ((MScanSimilarityPredicate)ssp).getSimilaritiesAboveThreshold();
//				Comparator<Tuple2D<Double,IMetabolite>> comp = new Comparator<Tuple2D<Double,IMetabolite>>() {
//					
//					@Override
//	                public int compare(Tuple2D<Double, IMetabolite> o1,
//	                        Tuple2D<Double, IMetabolite> o2) {
//		                return o1.getFirst().compareTo(o2.getFirst());
//	                }
//				};
//				Collections.sort(l,Collections.reverseOrder(comp));
//				return l.subList(0, Math.min(l.size(), k-1));
//			}
				List<Tuple2D<Double,IMetabolite>> l = new ArrayList<Tuple2D<Double,IMetabolite>>();//((MScanSimilarityPredicate)ssp).getSimilaritiesAboveThreshold();
				MetaboliteSimilarity ms = new MetaboliteSimilarity();
				for(IMetabolite im:res) {
					l.add(new Tuple2D<Double,IMetabolite>(ms.get(this.query, im),im));
				}
				Comparator<Tuple2D<Double,IMetabolite>> comp = new Comparator<Tuple2D<Double,IMetabolite>>() {
	
					@Override
	                public int compare(Tuple2D<Double, IMetabolite> o1,
	                        Tuple2D<Double, IMetabolite> o2) {
		                return o1.getFirst().compareTo(o2.getFirst());
	                }
				};
				Collections.sort(l,Collections.reverseOrder(comp));
				return l.subList(0, Math.min(l.size(), k));
//			}else{
//				List<Tuple2D<Double,IMetabolite>> l = new ArrayList<Tuple2D<Double,IMetabolite>>();
//				for(IMetabolite im:os) {
//					l.add(new Tuple2D<Double,IMetabolite>(Double.NaN,im));
//				}
//				return l;
			//}
		} catch (InterruptedException e) {
			System.err.println(e.getLocalizedMessage());
		} catch (ExecutionException e) {
			System.err.println(e.getLocalizedMessage());
		}
		return new ArrayList<Tuple2D<Double,IMetabolite>>(0);
	}

}
