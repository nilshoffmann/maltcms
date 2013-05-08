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
package maltcms.commands.fragments.io.mzml;

import cross.cache.CacheFactory;
import cross.cache.ICacheDelegate;
import cross.datastructures.collections.CachedLazyList;
import cross.datastructures.collections.IElementProvider;
import java.util.ArrayList;
import java.util.List;
import uk.ac.ebi.jmzml.model.mzml.Spectrum;
import uk.ac.ebi.jmzml.model.mzml.SpectrumList;

/**
 *
 * @author Nils Hoffmann
 */
public class CachedSpectrumList extends SpectrumList {

	private ICacheDelegate<Integer, Spectrum> idxToSpectrum;

	public CachedSpectrumList(String name) {
		idxToSpectrum = CacheFactory.createDefaultCache(name);
	}

	@Override
	public Integer getCount() {
		return idxToSpectrum.keys().size();
	}

	@Override
	public List<Spectrum> getSpectrum() {
		if (this.spectrum == null) {
			this.spectrum = new CachedLazyList<Spectrum>(new IElementProvider<Spectrum>() {
				@Override
				public int size() {
					return idxToSpectrum.keys().size();
				}

				@Override
				public long sizeLong() {
					return size();
				}

				@Override
				public Spectrum get(int i) {
					return idxToSpectrum.get(i);
				}

				@Override
				public List<Spectrum> get(int start, int stop) {
					List<Spectrum> l = new ArrayList<Spectrum>(stop - start + 1);
					for (int i = start; i <= stop; i++) {
						l.set(i, get(i));
					}
					return l;
				}

				@Override
				public void reset() {
				}

				@Override
				public Spectrum get(long l) {
					return get((int) l);
				}

				@Override
				public List<Spectrum> get(long start, long stop) {
					return (get((int) start, (int) stop));
				}
			});
		}
		return this.spectrum;
	}

	public void setSpectrum(List<Spectrum> spectrum) {
		this.spectrum = spectrum;
	}

	public void add(Spectrum spectrum) {
		this.idxToSpectrum.put(spectrum.getIndex(),spectrum);
	}
}
