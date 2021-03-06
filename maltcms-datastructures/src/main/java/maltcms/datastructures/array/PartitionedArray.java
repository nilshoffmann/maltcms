/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
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
package maltcms.datastructures.array;

import cross.datastructures.tuple.Tuple2D;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.alignment.AnchorPairSet;
import maltcms.datastructures.constraint.ConstraintFactory;
import maltcms.tools.ArrayTools;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayDouble.D2;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayInt.D1;

/**
 * Array, which has been partitioned into rectangular areas, e.g. by anchors.
 * Allows overlap of areas, as well as for neighborhoods around anchors.
 *
 * @author Nils Hoffmann
 * 
 */
@Slf4j
public class PartitionedArray implements IArrayD2Double {

    /**
     * <p>copyLayout.</p>
     *
     * @param pa a {@link maltcms.datastructures.array.PartitionedArray} object.
     * @return a {@link maltcms.datastructures.array.PartitionedArray} object.
     */
    public static PartitionedArray copyLayout(final PartitionedArray pa) {
        final Area a = new Area(pa.getShape());
        return new PartitionedArray(pa.rows(), pa.columns(), (ArrayInt.D1) pa
                .getColStart().copy(), (ArrayInt.D1) pa.getRowLength().copy(),
                (ArrayInt.D1) pa.getRowOffset().copy(), pa.getDataArray(), pa
                .getDefaultValue(), a);
    }

    /**
     * <p>create.</p>
     *
     * @param rows a int.
     * @param cols a int.
     * @param aps a {@link maltcms.datastructures.alignment.AnchorPairSet} object.
     * @param neighborhood1 a int.
     * @param band a double.
     * @param defaultValue a double.
     * @param globalBand a boolean.
     * @return a {@link maltcms.datastructures.array.PartitionedArray} object.
     */
    public static PartitionedArray create(final int rows, final int cols,
            final AnchorPairSet aps, final int neighborhood1,
            final double band, final double defaultValue,
            final boolean globalBand) {
        Area shape = null;
        if (globalBand) {
            shape = ConstraintFactory.getInstance().calculateLayout(rows, cols,
                    neighborhood1, aps, -1, 0, 0);
            if ((band > 0.0) && (band < 1.0)) {
                final Area bandshape = ConstraintFactory.getInstance()
                        .createBandConstraint(0, 0, rows, cols, band);
                // final Area intersection = new Area(bandshape);
                shape.intersect(bandshape);
                // shape = intersection;
            }
        } else {
            shape = ConstraintFactory.getInstance().calculateLayout(rows, cols,
                    neighborhood1, aps, band, 0, 0);
        }

        return PartitionedArray.create(rows, cols, defaultValue, shape);
    }

    /**
     * <p>create.</p>
     *
     * @param rows a int.
     * @param cols a int.
     * @param defaultValue a double.
     * @param shape a {@link java.awt.geom.Area} object.
     * @return a {@link maltcms.datastructures.array.PartitionedArray} object.
     */
    public static PartitionedArray create(final int rows, final int cols,
            final double defaultValue, final Area shape) {
        final ArrayInt.D1 colStart = new ArrayInt.D1(rows);// first valid index
        // of each row
        final ArrayInt.D1 rowLength = new ArrayInt.D1(rows);// last valid index
        // of each row
        final ArrayInt.D1 rowOffset = new ArrayInt.D1(rows);// offsets of row
        // storage in
        final ArrayDouble.D1 dataArray = PartitionedArray.initArrays(rows,
                cols, shape, colStart, rowLength, rowOffset);
        final PartitionedArray pa = new PartitionedArray(rows, cols, colStart,
                rowLength, rowOffset, dataArray, defaultValue, shape);
        log.info(
                "Number of elements to calculate: "
                + pa.getElementsToCalculate());
        log.info(
                "Number of virtual elements to calculate: "
                + pa.getTotalNumElements());
        log.debug(
                "Columns: " + pa.columns() + "\tRows: " + pa.rows());
        return pa;
    }

    /**
     * <p>createLayoutImage.</p>
     *
     * @param pa a {@link maltcms.datastructures.array.PartitionedArray} object.
     * @param bg a {@link java.awt.Color} object.
     * @param fg a {@link java.awt.Color} object.
     * @return a {@link java.awt.image.BufferedImage} object.
     */
    public static BufferedImage createLayoutImage(final PartitionedArray pa,
            final Color bg, final Color fg) {
        final BufferedImage bi = new BufferedImage(pa.columns(), pa.rows(),
                BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D g = (Graphics2D) bi.getGraphics();
        final Area a = pa.getShape();
        final Color b = bg;
        g.setColor(b);
        g.fill(new Rectangle(0, 0, pa.columns(), pa.rows()));
        final Color c = fg;
        g.setColor(c);
        g.fill(a);
        return bi;
    }

    /**
     * <p>initArrays.</p>
     *
     * @param rows a int.
     * @param cols a int.
     * @param bounds a {@link java.awt.geom.Area} object.
     * @param colStart a {@link ucar.ma2.ArrayInt.D1} object.
     * @param rowLength a {@link ucar.ma2.ArrayInt.D1} object.
     * @param rowOffset a {@link ucar.ma2.ArrayInt.D1} object.
     * @return a {@link ucar.ma2.ArrayDouble.D1} object.
     */
    public static ArrayDouble.D1 initArrays(final int rows, final int cols,
            final Area bounds, final ArrayInt.D1 colStart,
            final ArrayInt.D1 rowLength, final ArrayInt.D1 rowOffset) {
        final long start = System.currentTimeMillis();
        int offset = 0;
        Area a = null;// new Area(r);
        for (int i = 0; i < rows; i++) {
            int startCol = -1;
            int len = 0;

            // log.info("Checking bounds of row "+i);
            a = new Area(new Rectangle(0, i, cols, 1));
            a.intersect(bounds);
            // log.info(
            // "Row {}, start column={} end={}",new
            // Object[]{i,a.getBounds().x,a.
            // getBounds().x+a.getBounds().width-1});
            // log.info("Row "+i+" start column="+a.getBounds().x+
            // " ncols="+(a.getBounds().width-1));
            // for(int j = 0;j<cols;j++) {
            // if(bounds.contains(j, i)){
            // // log.info(""+i+","+j+" contained in bounds");
            // if(startCol==-1) {
            // startCol = j;
            // //log.info("start column = "+startCol);
            // }else{
            // len++;
            // //log.info("length of column = "+len);
            // //neighborhoods.add(new Rectangle(startCol,i,j-startCol,1));
            // }
            // //partitions.add(new Rectangle(j,i,1,1));
            // }
            // }
            // log.info("length of column = "+len);
            // EvalTools.eqI(len, a.getBounds().width-1,
            // PartitionedArray.class);
            startCol = a.getBounds().x;
            len = a.getBounds().width - 1;
            // r.setBounds(0, r.getBounds().y+1, cols, 1);
            // log.info("Offset before adding cols: "+offset);
            offset = PartitionedArray.prepareRow(offset, i, startCol, len + 1,
                    rowOffset, colStart, rowLength);
            // log.info("Offset after adding cols: "+offset);
        }
        final long end = System.currentTimeMillis() - start;
        log.debug(
                "Time to calculate row layout: " + end);
        return new ArrayDouble.D1(offset);
    }

//	public static void main(final String[] args) {
//		final int xs = 250;
//		final int ys = 200;
//		final int neighborhood = 1;
//		final JFrame jf = new JFrame();
//		final JTabbedPane jtp = new JTabbedPane();
//		jf.add(jtp);
//
//		TabbedPanel jp = null;
//		final ArrayDouble.D2 a = ArrayTools.matrix(xs, ys);
//		final IndexIterator ii = a.getIndexIterator();
//		while (ii.hasNext()) {
//			ii.setDoubleNext(ArrayTools.nextUniform());
//		}
//
//		final IRetentionInfo a1 = new RetentionInfo();
//		a1.setName("RI1");
//		a1.setRetentionIndex(1200.0d);
//		a1.setScanIndex(5);
//		final IRetentionInfo a2 = new RetentionInfo();
//		a2.setName("RI2");
//		a2.setRetentionIndex(1600.0d);
//		a2.setScanIndex(10);
//
//		final IRetentionInfo b1 = new RetentionInfo();
//		b1.setName("RI1");
//		b1.setRetentionIndex(1200.0d);
//		b1.setScanIndex(7);
//		final IRetentionInfo b2 = new RetentionInfo();
//		b2.setName("RI2");
//		b2.setRetentionIndex(1400.0d);
//		b2.setScanIndex(11);
//		final IRetentionInfo b6 = new RetentionInfo();
//		b6.setName("RI3");
//		b6.setRetentionIndex(1600.0d);
//		b6.setScanIndex(13);
//
//		final ArrayList<IAnchor> al1 = new ArrayList<IAnchor>();
//		final ArrayList<IAnchor> al2 = new ArrayList<IAnchor>();
//
//		al1.add(a1);
//		al1.add(a2);
//
//		al2.add(b1);
//		al2.add(b2);
//		al2.add(b6);
//
//		final AnchorPairSet aps = new AnchorPairSet(al1, al2, 15, 16, 5);
//		final PartitionedArray pa = PartitionedArray.create(15, 16, aps,
//		        neighborhood, 0.0f, Double.POSITIVE_INFINITY, true);
//		pa.getEnclosingRectangle();
//
//		for (int i = 0; i < 15; i++) {
//			log.info("Row " + i + " offset: "
//			        + pa.getRowOffset().get(i) + " colStart: "
//			        + pa.getColStart().get(i) + " colLen: "
//			        + pa.getRowLength().get(i));
//		}
//
//		int cnt = 0;
//		for (int i = 0; i < pa.rows(); i++) {
//			for (int j = 0; j < pa.columns(); j++) {
//				// EvalTools.eqI(cnt, pa.getAddressInDataArray(i, j),
//				// PartitionedArray.class);
//				// double d = Math.random() * 1024;
//				// if(i==0 && j==0) {
//				// log.info("Getting element "+i+" "+j+"="+pa.get(i,
//				// j));
//				// log.info("Setting element "+i+" "+j+"="+cnt);
//				// pa.set(i,j,cnt);
//				// log.info("Getting element "+i+" "+j+"="+pa.get(i,
//				// j));
//				// log.info("Address in array: "+pa.
//				// getAddressInDataArray(0, 0));
//				// }else{
//
//				// }
//
//				if (pa.inRange(i, j)) {
//
//					EvalTools.eqI(cnt, pa.getAddressInDataArray(i, j),
//					        PartitionedArray.class);
//					pa.set(i, j, cnt);
//					// log.info("Getting element "+i+" "+j+"="+cnt);
//					// log.info("Retrieving element "+i+" "+j);
//					EvalTools.eqD(cnt, pa.get(i, j), pa);
//					log.info("Array index: row=" + i + " col=" + j
//					        + " value= " + pa.get(i, j));
//					cnt++;
//				} else {// should return default value
//					// log.info("Retrieving element "+i+" "+j);
//					EvalTools.eqD(pa.getDefaultValue(), pa.get(i, j), pa);
//				}
//
//			}
//		}
//		log.info("Last Array index: row=" + (pa.rows() - 1) + " col="
//		        + (pa.columns() - 1) + " value= "
//		        + pa.get(pa.rows() - 1, pa.columns() - 1));
//
//		log.info("Size of data array = "
//		        + pa.getDataArray().getShape()[0]);
//
//		jp = new TabbedPanel(pa, aps);
//		jtp.add("Partitioned array", new JScrollPane(jp));
//
//		jf.setSize(400, 300);
//		jf.setVisible(true);
//		jf.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//	}
    /**
     * <p>prepareRow.</p>
     *
     * @param offset a int.
     * @param row a int.
     * @param startcol a int.
     * @param len a int.
     * @param rowOffset a {@link ucar.ma2.ArrayInt.D1} object.
     * @param colStart a {@link ucar.ma2.ArrayInt.D1} object.
     * @param rowLength a {@link ucar.ma2.ArrayInt.D1} object.
     * @return a int.
     */
    public static int prepareRow(int offset, final int row, final int startcol,
            final int len, final ArrayInt.D1 rowOffset,
            final ArrayInt.D1 colStart, final ArrayInt.D1 rowLength) {
        // + " size of offset array: " + rowOffset.getShape()[0]);

        rowOffset.set(row, offset);
        // log.info("Processing row " + k + " with " + n
        // + " elements");
        // log.info("Startindex " + scol);
        // log.info("ncolumns " + (len));
        // log.info("Offset " + offset);
        // log.info("Setting colstart of row " + row + " to " +
        // startcol);
        colStart.set(row, startcol);
        // log.info("Setting rowlength of row " + row + " to " + len);
        rowLength.set(row, len);
        offset += len;
        // log.info("Setting Row "+row+" offset: "+rowOffset.get(row)+
        // " colStart: "+colStart.get(row)+" colLen: "+rowLength.get(row));
        return offset;
    }

    /**
     * <p>shareLayout.</p>
     *
     * @param pa a {@link maltcms.datastructures.array.PartitionedArray} object.
     * @return a {@link maltcms.datastructures.array.PartitionedArray} object.
     */
    public static PartitionedArray shareLayout(final PartitionedArray pa) {
        return new PartitionedArray(pa.rows(), pa.columns(), pa.getColStart(),
                pa.getRowLength(), pa.getRowOffset(), pa.getDataArray(), pa
                .getDefaultValue(), pa.getShape());
    }
    private Area shape = null;
    private ArrayInt.D1 colStart;
    private final ArrayDouble.D1 dataArray;
    private double defaultValue = Double.POSITIVE_INFINITY;
    private final float elemsToCalculate;
    private List<Rectangle> neighborhoods;
    private int nPartitions;
    private List<Rectangle> partitions;
    private int row;
    private ArrayInt.D1 rowLength;
    private ArrayInt.D1 rowOffset;
    private final float totalNumElements;
    private int virtualCols = 0;
    private int virtualRows = 0;

    /**
     * <p>Constructor for PartitionedArray.</p>
     *
     * @param rows a int.
     * @param cols a int.
     * @param colStart1 a {@link ucar.ma2.ArrayInt.D1} object.
     * @param rowLength1 a {@link ucar.ma2.ArrayInt.D1} object.
     * @param rowOffset1 a {@link ucar.ma2.ArrayInt.D1} object.
     * @param dataArray1 a {@link ucar.ma2.ArrayDouble.D1} object.
     * @param defaultValue1 a double.
     * @param shape1 a {@link java.awt.geom.Area} object.
     */
    public PartitionedArray(final int rows, final int cols,
            final ArrayInt.D1 colStart1, final ArrayInt.D1 rowLength1,
            final ArrayInt.D1 rowOffset1, final ArrayDouble.D1 dataArray1,
            final double defaultValue1, final Area shape1) {
        this.virtualRows = rows;
        this.virtualCols = cols;
        this.colStart = colStart1;
        this.rowLength = rowLength1;
        this.rowOffset = rowOffset1;
        this.dataArray = dataArray1;
        this.defaultValue = defaultValue1;
        this.elemsToCalculate = this.dataArray.getShape()[0];
        this.totalNumElements = this.virtualCols * this.virtualRows;
        this.shape = shape1;
        ArrayTools.fill(this.dataArray, this.defaultValue);
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.alignment.IArrayD2Double#columns()
     */
    /** {@inheritDoc} */
    @Override
    public int columns() {
        return this.virtualCols;
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IArrayD2Double#flatten()
     */
    /** {@inheritDoc} */
    @Override
    public Tuple2D<D1, ucar.ma2.ArrayDouble.D1> flatten() {
        final ArrayDouble.D1 arr = new ArrayDouble.D1(
                getNumberOfStoredElements());
        final ArrayInt.D1 si = new ArrayInt.D1(rows());
        int offset = 0;
        for (int i = 0; i < rows(); i++) {
            si.set(i, 0 + offset);
            final int[] bds = getColumnBounds(i);
            int ecnt = 0;
            for (int j = bds[0]; j < bds[0] + bds[1]; j++) {
                arr.set(offset + ecnt, get(i, j));
                ecnt++;
            }
            offset += bds[1];
        }
        return new Tuple2D<>(si, arr);
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.alignment.IArrayD2Double#get(int, int)
     */
    /** {@inheritDoc} */
    @Override
    public double get(final int row, final int col) {
        final int index = getAddressInDataArray(row, col);
        if (index == -1) {
            return this.defaultValue;
        } else {
            return this.dataArray.get(index);
        }
    }

    /**
     * <p>getAddressInDataArray.</p>
     *
     * @param row a int.
     * @param col a int.
     * @return a int.
     */
    protected int getAddressInDataArray(final int row, final int col) {
        if (inRange(row, col)) {
            final int rowOffsetPos = this.rowOffset.get(row);
            final int colStart = this.colStart.get(row);
            // int len = this.rowLength.get(row);
            // log.info("Adress in array: "+(rowOffsetPos+col));
            return rowOffsetPos + col - colStart;
        }
        return -1;
    }

    /**
     * <p>getAddressInDataArrayFast.</p>
     *
     * @param row a int.
     * @param col a int.
     * @return a int.
     * @throws java.lang.ArrayIndexOutOfBoundsException if any.
     */
    protected int getAddressInDataArrayFast(final int row, final int col)
            throws ArrayIndexOutOfBoundsException {
        final int rowOffsetPos = this.rowOffset.get(row);
        final int colStart = this.colStart.get(row);
        // int len = this.rowLength.get(row);
        return rowOffsetPos + col - colStart;
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.alignment.IArrayD2Double#getArray()
     */
    /** {@inheritDoc} */
    @Override
    public D2 getArray() {
        final ArrayDouble.D2 arr = new ArrayDouble.D2(rows(), columns());
        // fill array with default value
        ArrayTools.fill(arr, this.defaultValue);
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < columns(); j++) {
                arr.set(i, j, get(i, j));
            }
        }
        return arr;
    }

    /**
     * <p>Getter for the field <code>colStart</code>.</p>
     *
     * @return the colStart
     */
    public ArrayInt.D1 getColStart() {
        return this.colStart;
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IArrayD2Double#getColumnBounds(int)
     */
    /** {@inheritDoc} */
    @Override
    public int[] getColumnBounds(final int row) {
        final int colStartPos = this.colStart.get(row);
        final int len = this.rowLength.get(row);
        return new int[]{colStartPos, len};
    }

    // public int getNumPartitions() {
    // return this.partitions.size();
    // }
    /**
     * <p>Getter for the field <code>dataArray</code>.</p>
     *
     * @return the dataArray
     */
    public ArrayDouble.D1 getDataArray() {
        return (ArrayDouble.D1) this.dataArray.copy();
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.alignment.IArrayD2Double#getDefaultValue()
     */
    /** {@inheritDoc} */
    @Override
    public double getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * <p>getElementsToCalculate.</p>
     *
     * @return a float.
     */
    public float getElementsToCalculate() {
        return this.elemsToCalculate;
    }

    /**
     * <p>getEnclosingRectangle.</p>
     *
     * @return a {@link java.awt.Rectangle} object.
     */
    public Rectangle getEnclosingRectangle() {
        return new Rectangle(0, 0, this.virtualCols, this.virtualRows);
    }

    /**
     * <p>Getter for the field <code>neighborhoods</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Rectangle> getNeighborhoods() {
        return this.neighborhoods;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.datastructures.alignment.IArrayD2Double#getNumberOfStoredElements
     * ()
     */
    /** {@inheritDoc} */
    @Override
    public int getNumberOfStoredElements() {
        return this.dataArray.getShape()[0];
    }

    /**
     * <p>getNumPartitions.</p>
     *
     * @return a int.
     */
    public int getNumPartitions() {
        return this.nPartitions;
    }

    /**
     * <p>getPartitionIterator.</p>
     *
     * @return a {@link java.util.Iterator} object.
     */
    public Iterator<Rectangle> getPartitionIterator() {
        return iterator();
    }

    /**
     * <p>Getter for the field <code>rowLength</code>.</p>
     *
     * @return the rowLength
     */
    public ArrayInt.D1 getRowLength() {
        return this.rowLength;
    }

    /**
     * <p>Getter for the field <code>rowOffset</code>.</p>
     *
     * @return the rowOffset
     */
    public ArrayInt.D1 getRowOffset() {
        return this.rowOffset;
    }

    /** {@inheritDoc} */
    @Override
    public Area getShape() {
        return this.shape;
    }

    /**
     * <p>Getter for the field <code>totalNumElements</code>.</p>
     *
     * @return a float.
     */
    public float getTotalNumElements() {
        return this.totalNumElements;
    }

    /** {@inheritDoc} */
    @Override
    public boolean inRange(final int row, final int col) {
        if ((row < 0) || (row >= rows()) || (col < 0) || (col >= columns())) {
            return false;
        }
        // log.info("Retrieving address for row "+row+" col "+col);
        final int[] colBounds = getColumnBounds(row);
        // log.info("["+row+","+col+"] =>" +
        // " column start= "+colStartPos+" len: "+len);
        if ((col < colBounds[0]) || (col >= (colBounds[0] + colBounds[1]))) {
            return false;
        }
        return true;
    }

    /**
     * <p>iterator.</p>
     *
     * @return a {@link java.util.Iterator} object.
     */
    public Iterator<Rectangle> iterator() {
        return this.partitions.iterator();
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.alignment.IArrayD2Double#rows()
     */
    /** {@inheritDoc} */
    @Override
    public int rows() {
        return this.virtualRows;
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.alignment.IArrayD2Double#set(int, int,
     * double)
     */
    /** {@inheritDoc} */
    @Override
    public void set(final int row, final int col, final double d)
            throws ArrayIndexOutOfBoundsException {
        final int index = getAddressInDataArray(row, col);
        // log.info("Row "+row+" Column "+col+
        // " Index in dataArray array: "+
        // index);
        if (index == -1) {
            // log.info(index+" for "+row+" "+col+
            // " : Index out of bounds!");
        } else {
            this.dataArray.set(index, d);
        }
    }

    /**
     * <p>Setter for the field <code>colStart</code>.</p>
     *
     * @param colStart the colStart to set
     */
    public void setColStart(final ArrayInt.D1 colStart) {
        this.colStart = colStart;
    }

    /**
     * <p>setNumPartitions.</p>
     *
     * @param i a int.
     */
    public void setNumPartitions(final int i) {
        this.nPartitions = i;
    }

    /**
     * <p>Setter for the field <code>rowLength</code>.</p>
     *
     * @param rowLength the rowLength to set
     */
    public void setRowLength(final ArrayInt.D1 rowLength) {
        this.rowLength = rowLength;
    }

    /**
     * <p>Setter for the field <code>rowOffset</code>.</p>
     *
     * @param rowOffset the rowOffset to set
     */
    public void setRowOffset(final ArrayInt.D1 rowOffset) {
        this.rowOffset = rowOffset;
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.alignment.IArrayD2Double#toString()
     */
    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < columns(); j++) {
                sb.append(get(i, j));
                sb.append(", ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
