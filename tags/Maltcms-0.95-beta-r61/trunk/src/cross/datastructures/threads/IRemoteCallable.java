/**
 * 
 */
package cross.datastructures.threads;

import java.rmi.Remote;
import java.util.concurrent.Callable;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public interface IRemoteCallable<T> extends Remote, Callable<T> {

}
