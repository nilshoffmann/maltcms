/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id$
 */

package maltcms.datastructures.array;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Formatter;
import java.util.concurrent.ArrayBlockingQueue;

import org.slf4j.Logger;

import cross.Logging;
import cross.datastructures.fragments.FileFragment;

/**
 * TODO Implementation due in Maltcms-2.0
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 * @param <T>
 */
public abstract class FileBackedArray<T extends Number> {

	private final int buffer_lines = 100;

	private int chunk_size = 0;

	private int nchunks = 0;

	private final int max_neighbor_chunks = 2;

	private ArrayBlockingQueue<T[]> abq = null;

	private int chunk_size_last = 0;

	private int vwidth, vheight;

	private static int fID = 0;

	public static final String NUMBERFORMAT = "%010d";

	private File[] chunks = null;

	private T[] chunk = null;

	private final int active_chunk = 0;

	private final int requested_chunk = 0;

	private final Logger log = Logging.getLogger(this);

	public FileBackedArray(final int buffer_lines1, final int vwidth1,
	        final int vheight1) {
		this.chunks = createRowSets(buffer_lines1, vwidth1, vheight1);
		this.abq = new ArrayBlockingQueue<T[]>(this.max_neighbor_chunks);
	}

	private T acquire(final int i, final int j) {
		final T[] t = loadChunk(i / this.chunk_size);
		return t[this.vwidth * i + j];
	}

	public abstract T[] createChunk(int height, int width);

	public File[] createRowSets(final int buffer_lines1, final int vwidth1,
	        final int vheight1) {
		this.chunk_size = vheight1 / buffer_lines1;
		this.chunk_size_last = vheight1 % buffer_lines1;
		this.nchunks = (this.chunk_size_last > 0) ? this.chunk_size + 1
		        : this.chunk_size;
		final File[] chunks1 = new File[this.nchunks];
		for (int i = 0; i < this.nchunks; i++) {
			final StringBuilder sb = new StringBuilder();
			final Formatter formatter = new Formatter(sb);
			formatter
			        .format(FileFragment.NUMBERFORMAT, (FileBackedArray.fID++));
			try {
				chunks1[i] = File
				        .createTempFile(sb.toString(), "maltcms_array");
			} catch (final IOException e) {
				this.log.error(e.getLocalizedMessage());
			}
		}
		this.chunk = createChunk(this.chunk_size, vwidth1);
		return chunks1;
	}

	public T get(final int i, final int j) {
		return null;
	}

	private T[] loadChunk(final int index) {
		try {
			final ObjectInputStream ois = new ObjectInputStream(
			        new BufferedInputStream(new FileInputStream(
			                this.chunks[index])));
			final Object o = ois.readObject();
			@SuppressWarnings("unchecked")
			final T[] t = (T[]) o;
			ois.close();
			return t;
		} catch (final FileNotFoundException e) {
			this.log.error(e.getLocalizedMessage());
		} catch (final IOException e) {
			this.log.error(e.getLocalizedMessage());
		} catch (final ClassNotFoundException e) {
			this.log.error(e.getLocalizedMessage());
		}
		return null;
	}

	private boolean saveChunk(final int index, final T[] rowset) {
		final File f = this.chunks[index];
		try {

			final ObjectOutputStream oos = new ObjectOutputStream(
			        new BufferedOutputStream(new FileOutputStream(f)));
			oos.writeObject(rowset);
			oos.flush();
			oos.close();
			return true;
		} catch (final FileNotFoundException e) {
			this.log.error(e.getLocalizedMessage());
		} catch (final IOException e) {
			this.log.error(e.getLocalizedMessage());
		}
		return false;
	}

	public void set(final int i, final int j, final T t) {

	}

}
