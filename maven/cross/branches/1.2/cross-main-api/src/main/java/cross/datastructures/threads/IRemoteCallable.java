/*
 * $license$
 *
 * $Id$
 */

package cross.datastructures.threads;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public interface IRemoteCallable extends Remote{
    <T> T invoke(Callable<T> c) throws RemoteException;
}
