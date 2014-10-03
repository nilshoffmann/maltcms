package org.systemsbiology.jrap.staxnxt;

import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA. User: tholzman Date: Nov 16, 2009 Time: 2:34:17 PM
 * To change this template use File | Settings | File Templates.
 *
 * @author hoffmann
 * 
 * @since 1.3.2
 */
public class LineIterator implements Iterator {

    private ByteBufferIterator bbi = null;
    private ByteBuffer bb = null;

    /**
     * <p>Getter for the field <code>bbi</code>.</p>
     *
     * @return a {@link org.systemsbiology.jrap.staxnxt.ByteBufferIterator} object.
     */
    public ByteBufferIterator getBbi() {
        return bbi;
    }

    /**
     * <p>Setter for the field <code>bbi</code>.</p>
     *
     * @param bbi a {@link org.systemsbiology.jrap.staxnxt.ByteBufferIterator} object.
     */
    public void setBbi(ByteBufferIterator bbi) {
        this.bbi = bbi;
    }

    /**
     * <p>Getter for the field <code>bb</code>.</p>
     *
     * @return a {@link java.nio.ByteBuffer} object.
     */
    public ByteBuffer getBb() {
        return bb;
    }

    /**
     * <p>Getter for the field <code>filePos</code>.</p>
     *
     * @return a long.
     */
    public long getFilePos() {
        return filePos;
    }

    /**
     * <p>Getter for the field <code>curLine</code>.</p>
     *
     * @return a {@link java.lang.StringBuilder} object.
     */
    public StringBuilder getCurLine() {
        return curLine;
    }

    /**
     * <p>Getter for the field <code>lineNum</code>.</p>
     *
     * @return a int.
     */
    public int getLineNum() {
        return lineNum;
    }

    private long filePos = 0;
    private StringBuilder curLine = new StringBuilder();
    private int lineNum = 0;

    /**
     * <p>Constructor for LineIterator.</p>
     *
     * @param bbit a {@link org.systemsbiology.jrap.staxnxt.ByteBufferIterator} object.
     */
    public LineIterator(ByteBufferIterator bbit) {
        bbi = bbit;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasNext() {
        return bbi.hasNext() || (bb != null && bb.hasRemaining());
    }

    //This code is a little iffy.  If a \r\n pair (or \n\n etc) straddle a ByteBuffer boundary
    // it will not work.  Also, line numbers will be wrong for \n\n.
    /** {@inheritDoc} */
    @Override
    public StringBuilder next() {
        curLine.setLength(0);
        if (bb == null && bbi.hasNext()) {
            bb = bbi.next();
        }
        lineNum++;
        filePos = bbi.getFilePos() - bb.remaining();
        for (;;) {
            if (!bb.hasRemaining()) {
                if (!bbi.hasNext()) {
                    break;
                }
                bb = bbi.next();
            }
            byte curByte = bb.get();
            if (curByte != '\n' && curByte != '\r') {
                curLine.append((char) curByte);
            } else {
                if (bb.hasRemaining()) {
                    byte nextByte = bb.get();
                    if (nextByte != '\n' && nextByte != '\r') {
                        bb.position(bb.position() - 1);
                    }
                }
                break;
            }
        }
        return curLine;
    }

    /** {@inheritDoc} */
    @Override
    public void remove() {
    }

}
