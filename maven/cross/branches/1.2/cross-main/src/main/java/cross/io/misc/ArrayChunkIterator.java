/*
 * 
 *
 * $Id$
 */
package cross.io.misc;

import java.io.IOException;


import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Dimension;
import cross.Factory;
import cross.datastructures.fragments.IVariableFragment;
import cross.exception.NotImplementedException;
import cross.exception.ResourceNotAvailableException;
import cross.datastructures.tools.EvalTools;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO Due for Maltcms-2.0
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Slf4j
public class ArrayChunkIterator implements IArrayChunkIterator {

    private final IVariableFragment ivf;
    private int chunksize = 1024;
    private int chunk = 0;

    public ArrayChunkIterator(final IVariableFragment ivf1, final int chunksize1) {
        this.chunksize = chunksize1;
        this.ivf = ivf1;
        if (this.ivf.getArray() == null) {
            try {
                Factory.getInstance().getDataSourceFactory().getDataSourceFor(
                        this.ivf.getParent()).readStructure(ivf1);
            } catch (final IOException iex) {
                this.log.warn(iex.getLocalizedMessage());
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
        if ((this.ivf.getDimensions() == null)
                || (this.ivf.getDimensions()[0] == null)) {
            if (this.chunk == 0) {
                return true;
            } else {
                return false;
            }
        }
        final Dimension d = this.ivf.getDimensions()[0];
        if (this.chunk <= (d.getLength() / this.chunksize)) {
            return true;
        } else {
            return false;
        }
    }

    private Array loadChunk(final int i) {
        final Dimension d = this.ivf.getDimensions()[0];
        final Range[] r = this.ivf.getRange();
        Array a = null;
        if (r != null) {
            EvalTools.inRangeI(0, (int) Math.ceil(d.getLength()
                    / this.chunksize), i, this.getClass());
            final int start = d.getLength() / i;
            final int length = Math.min(d.getLength(), (i + 1) * this.chunksize)
                    - (i * this.chunksize);
            try {
                final Range[] tmp = new Range[]{new Range(start, start
                    + length - 1)};
                this.ivf.setRange(tmp);
                a = Factory.getInstance().getDataSourceFactory().
                        getDataSourceFor(this.ivf.getParent()).readSingle(
                        this.ivf);
            } catch (final InvalidRangeException e) {
                this.log.warn(e.getLocalizedMessage());
            } catch (final ResourceNotAvailableException e) {
                this.log.warn(e.getLocalizedMessage());
            } catch (final IOException e) {
                this.log.warn(e.getLocalizedMessage());
            } finally {
                // reset range
                this.ivf.setRange(r);
            }
        } else {// fall back to default behavior, returns complete array
            try {
                a = Factory.getInstance().getDataSourceFactory().
                        getDataSourceFor(this.ivf.getParent()).readSingle(
                        this.ivf);
            } catch (final ResourceNotAvailableException e) {
                this.log.warn(e.getLocalizedMessage());
            } catch (final IOException e) {
                this.log.warn(e.getLocalizedMessage());
            }
        }
        return a;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#next()
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
    @Override
    public void remove() {
        throw new NotImplementedException();
    }
}
