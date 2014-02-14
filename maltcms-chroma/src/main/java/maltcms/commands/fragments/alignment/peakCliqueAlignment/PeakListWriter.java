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

import com.carrotsearch.hppc.DoubleArrayList;
import com.carrotsearch.hppc.IntArrayList;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.workflow.DefaultWorkflowResult;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.ms.IMetabolite;
import maltcms.datastructures.ms.Metabolite;
import maltcms.tools.MaltcmsTools;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class PeakListWriter {
	public File savePeakList(File outputDirectory, Map<String, IFileFragment> nameToFragment, Collection<IBipacePeak> peaks, String filename,String type) {
		File output = new File(outputDirectory, filename);
		output.getParentFile().mkdirs();
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(output));
			int i = 1;
			for (final IBipacePeak p : peaks) {
				IFileFragment fragment = nameToFragment.get(p.getAssociation());
				Tuple2D<Array, Array> t = MaltcmsTools.getMS(fragment, p.getScanIndex());
				int shape = t.getSecond().getShape()[0];
				Array masses = t.getFirst();
				Array intensities = t.getSecond();
				IntArrayList intensList = new IntArrayList();
				DoubleArrayList mzList = new DoubleArrayList();
				for (int j = 0; j < shape; j++) {
					double intensVal = intensities.getDouble(j);
					double massVal = masses.getDouble(j);
					if (intensities.getDouble(j) > 0) {
						intensList.add((int) intensVal);
						mzList.add(massVal);
					}
				}
				ArrayInt.D1 intens = (ArrayInt.D1) Array.factory(intensList.toArray());
				ArrayDouble.D1 massArray = (ArrayDouble.D1) Array.factory(mzList.toArray());
				String name = p.getAssociation() + "-IDX_" + p.getScanIndex() + "-RT_" + p.getScanAcquisitionTime();
				IMetabolite im = new Metabolite(p.getName().isEmpty() ? name : p.getName(), p.getAssociation() + "-IDX_" + p.getScanIndex() + "-RT_" + p.getScanAcquisitionTime(), getClass().getSimpleName() + "-" + type, i++, "", "", "", Double.NaN, p.getScanAcquisitionTime(), "sec", -1, "", p.getName(), massArray, intens);
				bw.write(im.toString());
				bw.newLine();
			}
			bw.close();
		} catch (IOException ex) {
			log.warn("{}", ex);
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException ex1) {
					log.warn("{}", ex1);
				}
			}
		} finally {
			if(bw!=null) {
				try {
					bw.close();
				} catch (IOException ex) {
					log.warn("{}", ex);
				}
			}
		}
		return output;
	}
}