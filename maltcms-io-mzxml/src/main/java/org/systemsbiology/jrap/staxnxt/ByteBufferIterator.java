package org.systemsbiology.jrap.staxnxt;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>ByteBufferIterator class.</p>
 *
 */
@Slf4j
public class ByteBufferIterator implements Iterator {

    private int INITIAL_BUFFERSIZE = 10000;
    private int bufferSize = INITIAL_BUFFERSIZE;
    private FileInputStream fis = null;
    private FileChannel fc = null;
    private String fPath;
    private long fSize;
    private ByteBuffer bb = null;
    private long totBytesRead = 0;

    /**
     * <p>Setter for the field <code>bufferSize</code>.</p>
     *
     * @param b a int.
     */
    public void setBufferSize(int b) {
        this.bufferSize = b;
        //bb = ByteBuffer.allocate(bufferSize);
    }

    /**
     * <p>Getter for the field <code>bufferSize</code>.</p>
     *
     * @return a int.
     */
    public int getBufferSize() {
        return this.bufferSize;
    }

    /**
     * <p>getFileSize.</p>
     *
     * @return a long.
     */
    public long getFileSize() {
        return this.fSize;
    }

    /**
     * <p>getPath.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPath() {
        return this.fPath;
    }

    /**
     * <p>getFilePos.</p>
     *
     * @return a long.
     */
    public long getFilePos() {
        return totBytesRead;
    }

    /**
     * <p>Constructor for ByteBufferIterator.</p>
     *
     * @param fN a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    public ByteBufferIterator(String fN) throws IOException {
        fPath = fN;
        log.info("Processing file " + fN);
        log.info("Opening file input stream!");
        fis = new FileInputStream(fN);
        log.info("Opening file channel!");
        fc = fis.getChannel();
        fSize = fc.size();
    }

    /**
     * <p>Constructor for ByteBufferIterator.</p>
     *
     * @param fN a {@link java.lang.String} object.
     * @param buflen a int.
     * @throws java.io.IOException if any.
     */
    public ByteBufferIterator(String fN, int buflen) throws IOException {
        this(fN);
        bufferSize = buflen;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasNext() {
        boolean hasNext = totBytesRead < fSize;
        if (!hasNext) {
            if (fc != null) {
                try {
                    log.info("Closing file channel!");
                    fc.close();
                } catch (IOException ex) {
                    Logger.getLogger(ByteBufferIterator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (fis != null) {
                try {
                    log.info("Closing file input stream!");
                    fis.close();
                } catch (IOException ex) {
                    Logger.getLogger(ByteBufferIterator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return hasNext;
    }
    /*
     public ByteBuffer next() {
     try {
     if(bb == null) bb = ByteBuffer.allocate(bufferSize);
     bb.rewind();
     int bytesRead = fc.read(bb);
     if(bytesRead > 0){
     totBytesRead += bytesRead;
     bb.limit(bytesRead);
     } else {
     fis.close();
     }
     bb.rewind();
     //log.info("read "+bytesRead+" bytes, current total is "+totBytesRead+"; and filesize is "+fSize);
     } catch (Exception e) {
     log.warn("Problem in ByteBufferIterator.next(): "+e);
     log.warn(e.getLocalizedMessage());
     return null;
     }
     return bb;
     }
     */

    /** {@inheritDoc} */
    @Override
    public ByteBuffer next() {
        try {
            //dhmay 20100223, fixing issue with small files in which you can't try to read the full buffer size
            //on the last scan
            long numBytesToRead = Math.min(bufferSize, fSize - totBytesRead);
            bb = fc.map(FileChannel.MapMode.READ_ONLY, totBytesRead, numBytesToRead);
            int bytesRead = bb.capacity();
            if (bytesRead > 0) {
                totBytesRead += bytesRead;
            } else {
                fc.close();
                fis.close();
            }
            bb.rewind();
            //log.info("read "+bytesRead+" bytes, current total is "+totBytesRead+"; and filesize is "+fSize);
        } catch (Exception e) {
            log.warn("Problem in ByteBufferIterator.next(): " + e);
            log.warn(e.getLocalizedMessage());
            return null;
        } finally {
            if (fc != null) {
                try {
                    fc.close();
                } catch (IOException ex) {
                    Logger.getLogger(ByteBufferIterator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                    Logger.getLogger(ByteBufferIterator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return bb;
    }

    /** {@inheritDoc} */
    @Override
    public void remove() {
    }

//    protected void finalize() throws Throwable {
//        try {
//            if (fis != null) {
//                fis.close();
//            }
//        } catch (Throwable t) {
//        };
//        super.finalize();
//    }
}
