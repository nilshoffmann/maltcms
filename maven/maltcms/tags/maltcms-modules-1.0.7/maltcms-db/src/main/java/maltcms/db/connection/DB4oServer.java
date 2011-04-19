/**
 * 
 */
package maltcms.db.connection;

import com.db4o.Db4o;
import com.db4o.ObjectServer;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE

 *
 */
public class DB4oServer implements Runnable{

	private String address, port;
	
	private boolean shutdown = false;
	
	public static void main(final String[] args) {
		Thread t = new Thread(new DB4oServer(args[0],args[1]));
		t.start();
	}

	public void shutdown() {
		this.shutdown = true;
	}
	
	public DB4oServer(String address, String port) {
		this.address = address;
		this.port = port;
	}
	
	/* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
    	System.out.println("Starting db4o server for "+this.address+" on port "+this.port);
//    	Configuration cfg = Db4o.newConfiguration();
//    	cfg.clientServer().batchMessages(true);
//    	cfg.clientServer().maxBatchQueueSize(10);
//    	cfg.clientServer().prefetchObjectCount(100);
//    	cfg.clientServer().timeoutClientSocket(1000);
//    	cfg.optimizeNativeQueries(true);
		ObjectServer os = Db4o.openServer(this.address, Integer.parseInt(this.port));
		os.grantAccess("default", "default");
		int clients = 0;
		while(!shutdown) {
			int cl = os.ext().clientCount();
			if(cl!=clients) {
				System.out.println("Active clients: "+cl);
				clients = cl;
			}
			try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		}
	    os.close();
    }
	
}
