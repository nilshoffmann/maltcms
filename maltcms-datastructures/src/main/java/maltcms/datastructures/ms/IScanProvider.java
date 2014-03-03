/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
package maltcms.datastructures.ms;

import cross.IConfigurable;
import cross.exception.ResourceNotAvailableException;
import java.util.Collection;
import java.util.List;

/**
 * Interface giving access to specific scans within an experiment.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
public interface IScanProvider<T extends IScan> extends IConfigurable, Iterable<T> {

    /**
     * Retrieve the Scan at the specified index.
     *
     * @param i the scan index to retrieve
     * @return the IScan
     */
    public T getScan(int i);

    /**
     * Returns the total number of ms scans available in this chromatogram.
     * May contain scans of all fragmentation levels.
     *
     * @return the total number of scans
     */
    public int getNumberOfScans();

    /**
     * Returns the available ms fragmentation levels in ascending order.
     *
     * @return the available ms fragmentation levels
     */
    public Collection<Short> getMsLevels();

    /**
     * Returns the number of scans available for the supplied level.
     *
     * @param level the ms fragmentation level (1,2,...,n)
     * @return the number of scans for the specified level
     */
    public int getNumberOfScansForMsLevel(short level);

    /**
     * Return a scan for the specific index, bound by the number of available
     * scans for the given ms fragmentation level.
     *
     * @param i     the requested scan index for the given level
     * @param level the ms fragmentation level (1,2,...,n)
     * @return the corresponding scan
     * @throws ResourceNotAvailableException if a scan at that index or level is not available
     */
    public T getScanForMsLevel(int i, short level);

    /**
     * Returns a list of integer indices based on the scan_index variable into the raw
     * data arrays. This allows for a much lighter memory footprint manual access
     * to the underlying data on selected variables.
     *
     * @param level the ms fragmentation level (1,2,...,n)
     * @return the list of indices of scans into the raw data variables based on scan_index
     */
    public List<Integer> getIndicesOfScansForMsLevel(short level);
}
