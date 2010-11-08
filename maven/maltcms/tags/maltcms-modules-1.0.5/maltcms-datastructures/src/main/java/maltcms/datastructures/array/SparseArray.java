/**
 * 
 */
package maltcms.datastructures.array;

import java.awt.Rectangle;
import java.awt.geom.Area;

import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayDouble.D2;
import ucar.ma2.ArrayInt.D1;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cross.datastructures.tuple.Tuple2D;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public final class SparseArray implements IArrayD2Double {
	private final SparseDoubleMatrix2D sdm;

	public SparseArray(int rows, int columns) {
		sdm = new SparseDoubleMatrix2D(rows, columns);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.array.IArrayD2Double#columns()
	 */
	@Override
	public int columns() {
		return this.sdm.columns();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.array.IArrayD2Double#flatten()
	 */
	@Override
	public Tuple2D<D1, ucar.ma2.ArrayDouble.D1> flatten() {
		ArrayInt.D1 nelems = new ArrayInt.D1(rows());
		ArrayDouble.D1 data = new ArrayDouble.D1(getNumberOfStoredElements());
		int dtidx = 0;
		for (int i = 0; i < rows(); i++) {
			nelems.set(i, dtidx);
			for (int j = 0; j < columns(); j++) {
				double d = this.sdm.get(i, j);
				if (d != 0) {
					data.set(dtidx++, d);
				}
			}
		}
		return new Tuple2D<D1, ucar.ma2.ArrayDouble.D1>(nelems, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.array.IArrayD2Double#get(int, int)
	 */
	@Override
	public double get(int row, int col) {
		return this.sdm.get(row, col);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.array.IArrayD2Double#getArray()
	 */
	@Override
	public D2 getArray() {
		ArrayDouble.D2 arr = new ArrayDouble.D2(rows(), columns());
		for (int i = 0; i < rows(); i++) {
			for (int j = 0; j < columns(); j++) {
				arr.set(i, j, this.sdm.get(i, j));
			}
		}
		return arr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.array.IArrayD2Double#getColumnBounds(int)
	 */
	@Override
	public int[] getColumnBounds(final int row) {
		return new int[] { 0, columns() };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.alignment.IArrayD2Double#getDefaultValue()
	 */
	@Override
	public double getDefaultValue() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * maltcms.datastructures.alignment.IArrayD2Double#getNumberOfStoredElements
	 * ()
	 */
	@Override
	public int getNumberOfStoredElements() {
		return sdm.cardinality();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.array.IArrayD2Double#getShape()
	 */
	@Override
	public Area getShape() {
		return new Area(new Rectangle(0, 0, columns(), rows()));
	}

	@Override
	public boolean inRange(final int i, final int j) {
		if ((i > 0) && (j > 0) && (i < rows()) && (j < columns())) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.array.IArrayD2Double#rows()
	 */
	@Override
	public int rows() {
		return this.sdm.rows();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.array.IArrayD2Double#set(int, int, double)
	 */
	@Override
	public void set(int row, int col, double d)
	        throws ArrayIndexOutOfBoundsException {
		this.sdm.set(row, col, d);
	}

}
