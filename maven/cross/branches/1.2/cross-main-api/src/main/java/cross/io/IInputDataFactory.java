/*
 * 
 *
 * $Id$
 */

package cross.io;

import cross.IConfigurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import java.util.List;

/**
 *
 * @author nilshoffmann
 */
public interface IInputDataFactory extends IConfigurable {

    List<IFileFragment> getInitialFiles();

    /**
     * Preprocess input data (files and variables).
     *
     * @return
     */
    TupleND<IFileFragment> prepareInputData();

}
