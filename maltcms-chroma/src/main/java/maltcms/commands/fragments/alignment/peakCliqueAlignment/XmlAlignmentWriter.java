/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2013, The authors of Maltcms. All rights reserved.
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
package maltcms.commands.fragments.alignment.peakCliqueAlignment;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.alignment.AlignmentFactory;
import maltcms.datastructures.peak.IPeak;
import maltcms.io.xml.bindings.alignment.Alignment;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class XmlAlignmentWriter {
	public File saveToXMLAlignment(final File out, final TupleND<IFileFragment> tuple,
			final List<List<IBipacePeak>> ll) {
		AlignmentFactory af = new AlignmentFactory();
		Alignment a = af.createNewAlignment(this.getClass().getName(), false);
		HashMap<IFileFragment, List<Integer>> fragmentToScanIndexMap = new HashMap<IFileFragment, List<Integer>>();
		for (final List<IBipacePeak> l : ll) {
			log.debug("Adding {} peaks: {}", l.size(), l);
			HashMap<String, IPeak> fragToPeak = new HashMap<String, IPeak>();
			for (final IPeak p : l) {
				fragToPeak.put(p.getAssociation(), p);
			}
			for (final IFileFragment iff : tuple) {
				int scanIndex = -1;
				if (fragToPeak.containsKey(iff.getName())) {
					IPeak p = fragToPeak.get(iff.getName());
					scanIndex = p.getScanIndex();
				}

				List<Integer> scans = null;
				if (fragmentToScanIndexMap.containsKey(iff)) {
					scans = fragmentToScanIndexMap.get(iff);
				} else {
					scans = new ArrayList<Integer>();
					fragmentToScanIndexMap.put(iff, scans);
				}

				scans.add(scanIndex);
			}
		}

		for (IFileFragment iff : fragmentToScanIndexMap.keySet()) {
			af.addScanIndexMap(a, iff.getUri(),
					fragmentToScanIndexMap.get(iff), false);
		}
		af.save(a, out);
		return out;
	}
}
