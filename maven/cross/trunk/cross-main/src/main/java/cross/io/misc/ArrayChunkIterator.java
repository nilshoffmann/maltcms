/* 
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.io.misc;

import cross.Factory;
import cross.datastructures.fragments.IVariableFragment;
import cross.exception.NotImplementedException;
import cross.exception.ResourceNotAvailableException;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Dimension;

/**
 * Implementation for chunked array iteration of large on-disk arrays.
 * 
 * @author Nils Hoffmann
 */
@Slf4j
public class ArrayChunkIterator implements IArrayChunkIterator {

    private final IVariableFragment ivf;
    private int chunksize = 1024;
    private int chunk = 0;
    private int offset = 0;
    private int nchunks = -1;
    private int length = -1;
    private boolean loadFromFile = false;

    /**
     *
     * @param ivf1
     * @param chunksize1
     */
    public ArrayChunkIterator(final IVariableFragment ivf1, final int chunksize1) {
        this.chunksize = chunksize1;
        this.ivf = ivf1;
        if (!this.ivf.hasArray()) {
            try {
                Factory.getInstance().getDataSourceFactory().getDataSourceFor(
                        this.ivf.getParent()).readStructure(ivf1);
                loadFromFile = true;
            } catch (final IOException iex) {
                log.warn(iex.getLocalizedMessage());
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#hasNext()
     */
    /**
     *
     * @return
     */
    @Override
    public boolean hasNext() {
        if (nchunks < 0) {
            if (loadFromFile) {
                length = ivf.getDimensions()[0].getLength();
            } else {
                length = ivf.getArray().getShape()[0];
            }
            log.info("Length: {}",length);
            int mod = length % chunksize;
            nchunks = (mod==0?0:1) + (length / chunksize);
            log.info("Chunksize: {}",chunksize);
            log.info("#of chunks: {}",nchunks);
        }
        return chunk < nchunks;
    }

    private Array loadChunk(final int i) {
        final Dimension d = this.ivf.getDimensions()[0];
        final Range[] oldRange = this.ivf.getRange();
        Range[] r = null;
        Array a = null;
        int activeChunkSize = Math.min(chunksize, length - offset);
        int lastIndex = Math.max(offset, offset+activeChunkSize-1);
        log.info("Active chunk: {} with size {}",chunk,activeChunkSize);
        try {
            r = new Range[]{new Range(offset,lastIndex)};
        } catch (InvalidRangeException ex) {
            Logger.getLogger(ArrayChunkIterator.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (loadFromFile) {
            ivf.setRange(r);
            try {
                a = Factory.getInstance().getDataSourceFactory()
                        .getDataSourceFor(this.ivf.getParent()).readSingle(
                        this.ivf);
            } catch (IOException ex) {
                Logger.getLogger(ArrayChunkIterator.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ResourceNotAvailableException ex) {
                Logger.getLogger(ArrayChunkIterator.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                a = ivf.getArray().sectionNoReduce(Arrays.asList(r));
            } catch (InvalidRangeException ex) {
                Logger.getLogger(ArrayChunkIterator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.ivf.setRange(oldRange);
        offset+=activeChunkSize;
        return a;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#next()
     */
    /**
     *
     * @return
     */
    @Override
    public Array next() {
        return loadChunk(this.chunk++);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#remove()
     */
    /**
     *
     */
    @Override
    public void remove() {
        throw new NotImplementedException();
    }
}
