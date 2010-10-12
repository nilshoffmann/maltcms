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

package old;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Group;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import cross.Logging;
import cross.datastructures.fragments.IVariableFragment;

/**
 * Thread based InputStreamProvider for ucar.ma2.Array objects, providing a
 * piped input stream.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * @param <T>
 * 
 */
public class NetcdfArrayPISProvider<T> implements PISProvider<T> {

	static Logger log = Logging.getInstance().logger;

	private int buffer_size = 8192;

	private ExecutorService tp = null;

	public NetcdfArrayPISProvider() {
		this.tp = Executors.newFixedThreadPool(20);
	}

	public NetcdfArrayPISProvider(final int maxsize) {
		this();
		this.buffer_size = maxsize;
	}

	protected int getBufferSize() {
		return this.buffer_size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.system.PISProvider#provide(maltcms.system.PISConsumer)
	 */
	public void provide(final PISConsumer<T> con) {
		final Iterator<IVariableFragment> variableFragment = con.getDataInfo()
		        .iterator();
		while (variableFragment.hasNext()) {
			final IVariableFragment di = variableFragment.next();
			final Runnable r = new Runnable() {

				public void run() {
					final String fname = di.getParent().getName();
					final String vname = di.getVarname();
					final Range[] range = di.getRange();
					if ((range == null) || (fname == null) || (vname == null)) {
						NetcdfArrayPISProvider.log.debug("One Object is null!");
					}
					NetcdfArrayPISProvider.log.debug("Next VariableFragment: "
					        + fname + " " + vname + " "
					        + ((range == null) ? "" : range[0].first()) + ":"
					        + ((range == null) ? "" : range[0].last()) + ":"
					        + ((range == null) ? "" : range[0].stride()));
					try {
						final NetcdfDataset nd = NetcdfDataset.acquireDataset(
						        fname, null);
						final Variable var = nd.findVariable(vname);
						final Group g = var.getParentGroup();
						final VariableDS v = new VariableDS(g, var, true);
						v.setCaching(true);
						final PipedOutputStream pos = new PipedOutputStream();
						Array a;
						try {
							if (range == null) {
								a = v.read();
							} else {
								if (range[0].last() > v.getSize()) {
									a = v.read(new int[] { range[0].first() },
									        null);
								} else {
									a = v.read(new int[] { range[0].first() },
									        new int[] { range[0].last() });
								}
							}
							final IndexIterator it = a.getIndexIterator();
							NetcdfArrayPISProvider.log
							        .debug("Trying to connect pipe!");
							con.consume(di, new PipedInputStream(pos));
							NetcdfArrayPISProvider.log
							        .debug("Pipe connected, writing Object to Stream!");
							final ObjectOutputStream dos = new ObjectOutputStream(
							        pos);
							long cnt = 0;
							while (it.hasNext()) {
								dos.writeObject(it.getDoubleNext());
								if (cnt > getBufferSize()) {
									dos.flush();
									cnt = 0;
								}
							}
							dos.flush();
							dos.close();
						} catch (final InvalidRangeException e) {
							e.printStackTrace();
						} finally {
							nd.close();
						}
					} catch (final IOException e) {
						NetcdfArrayPISProvider.log.error(e.getMessage());
						// throw new IOException("Empty PipedInputStream!");
					}
				}

			};
			this.tp.execute(r);
		}
	}

}
