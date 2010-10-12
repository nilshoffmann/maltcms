/**
 * 
 */
package cross.datastructures.threads;

import java.util.concurrent.Callable;

import org.apache.commons.configuration.Configuration;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public abstract class RemoteCallable<T> implements IRemoteCallable<T> {
	private Callable<T> c = null;

	public RemoteCallable(Callable<T> c, Configuration cfg,
	        TupleND<IFileFragment> t) {
		this.c = c;
	}

}
