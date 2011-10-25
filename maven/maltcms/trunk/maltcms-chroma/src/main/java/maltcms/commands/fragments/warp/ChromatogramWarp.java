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
 * $Id: ChromatogramWarp.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */
package maltcms.commands.fragments.warp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import maltcms.datastructures.ms.IAnchor;
import maltcms.tools.ArrayTools;
import maltcms.tools.MaltcmsTools;

import org.apache.commons.configuration.Configuration;
import java.util.Arrays;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.MAMath;
import ucar.nc2.Attribute;
import cross.Factory;
import cross.Logging;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.NotImplementedException;
import cross.exception.ResourceNotAvailableException;
import cross.tools.StringTools;

/**
 * Use Objects of this class to apply an alignment, warping a source
 * chromatogram to the time/scans of reference chromatogram. Use {@code
 * ChromatogramWarp2} instead!
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Deprecated
public class ChromatogramWarp extends AFragmentCommand {

    private List<String> indexedVars = Collections.emptyList();
    private List<String> plainVars = Collections.emptyList();
    private String indexVar = "scan_index";
    private final Logger log = Logging.getLogger(this);
    private String anchorScanIndexVariableName = "retention_scans";
    private String anchorNameVariableName = "retention_names";
    private boolean average = false;

    @Override
    public String toString() {
        return getClass().getName();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see cross.commands.ICommand#apply(java.lang.Object)
     */
    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        throw new NotImplementedException();
    }

	@Override
	public void configure(final Configuration cfg) {
		this.indexedVars = StringTools.toStringList(cfg.getList(this.getClass()
		        .getName()
		        + ".indexedVars"));
		this.indexVar = cfg.getString(this.getClass().getName() + ".indexVar",
		        "scan_index");
		this.plainVars = StringTools.toStringList(cfg.getList(this.getClass()
		        .getName()
		        + ".plainVars"));
		log.info("{}", this.plainVars);
		this.anchorScanIndexVariableName = cfg.getString(
		        "var.anchors.retention_scans", "retention_scans");
		this.anchorNameVariableName = cfg.getString(
		        "var.anchors.retention_scans", "retention_scans");
		this.average = cfg.getBoolean(this.getClass().getName()
		        + ".averageCompressions", false);
	}

	public IFileFragment copyReference(final IFileFragment ref,
	        final IWorkflow iw) {
		try {
			Factory.getInstance().getDataSourceFactory().getDataSourceFor(ref)
			        .readStructure(ref);
		} catch (final IOException e) {
			throw new RuntimeException(e.fillInStackTrace());
		}
		final IFileFragment copy = Factory.getInstance()
		        .getFileFragmentFactory().create(
		                new File(getWorkflow().getOutputDirectory(this),
		                        StringTools.removeFileExt(ref.getName())
		                                + ".cdf"));
		log.info("Ref: {}, copy: {}", ref.getAbsolutePath(), copy
		        .getAbsolutePath());
		copy.setAttributes(ref.getAttributes().toArray(new Attribute[] {}));
		IVariableFragment ivf = null;
		for (final String s : this.indexedVars) {
			if (ref.hasChild(s)) {
				ivf = ref.getChild(s);
			} else {
				ivf = new VariableFragment(ref, s);
			}
			final IVariableFragment ivfnew = copy.hasChild(s) ? copy
			        .getChild(s) : new VariableFragment(copy, s);
			ivfnew.setArray(ivf.getArray());
		}
		for (final String s : this.plainVars) {
			if (ref.hasChild(s)) {
				ivf = ref.getChild(s);
			} else {
				ivf = new VariableFragment(ref, s);
			}
			final IVariableFragment ivfnew = copy.hasChild(s) ? copy
			        .getChild(s) : new VariableFragment(copy, s);
			ivfnew.setArray(ivf.getArray());
		}
		copy.addSourceFile(ref);
		copy.save();
		return copy;
	}

    /*
     * (non-Javadoc)
     * 
     * @see cross.commands.fragments.AFragmentCommand#getDescription()
     */
    @Override
    public String getDescription() {
        return "Warps Chromatograms to a given reference, according to alignment paths.";
    }

    /**
     * @return the indexedVars
     */
    public List<String> getIndexedVars() {
        return this.indexedVars;
    }

    /**
     * @return the indexVar
     */
    public String getIndexVar() {
        return this.indexVar;
    }

    /**
     * @return the plainVars
     */
    public List<String> getPlainVars() {
        return this.plainVars;
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.WARPING;
    }

    /**
     * @param indexedVars
     *            the indexedVars to set
     */
    public void setIndexedVars(final List<String> indexedVars) {
        this.indexedVars = indexedVars;
    }

    /**
     * @param indexVar
     *            the indexVar to set
     */
    public void setIndexVar(final String indexVar) {
        this.indexVar = indexVar;
    }

    /**
     * @param plainVars
     *            the plainVars to set
     */
    public void setPlainVars(final List<String> plainVars) {
        this.plainVars = plainVars;
    }

	/**
	 * Warps by projecting data from originalFile to ref, given the alignment
	 * path. toLHS determines, whether the alignment reference ref is on the
	 * left hand side of the alignment path, or on the right hand side.
	 * processedFile is used to keep a backpointer to processing, like anchor
	 * finding. The returned FileFragment will have processedFile as its source
	 * file, not originalFile, since we want to keep additional information,
	 * which we already found out.
	 * 
	 * @param ref
	 * @param originalFile
	 * @param processedFile
	 * @param path
	 * @param toLHS
	 * @return
	 */
	public IFileFragment warp(final IFileFragment ref,
	        final IFileFragment originalFile,
	        final IFileFragment processedFile, final List<Tuple2DI> path,
	        final boolean toLHS, final IWorkflow iw) {
		log.info("Warping {}, saving in {}", originalFile
		        .getAbsolutePath(), processedFile.getAbsolutePath());
		final IFileFragment warped = Factory.getInstance()
		        .getFileFragmentFactory().create(
		                new File(getWorkflow().getOutputDirectory(this),
		                        StringTools.removeFileExt(originalFile
		                                .getName())
		                                + ".cdf"));

		try {
			Factory.getInstance().getDataSourceFactory().getDataSourceFor(
			        originalFile).readStructure(originalFile);
		} catch (final IOException e) {
			throw new RuntimeException(e.fillInStackTrace());
		}
		warped.setAttributes(originalFile.getAttributes().toArray(
		        new Attribute[] {}));
		log.debug("Currently set attributes on file {}:{}", originalFile
		        .getAbsolutePath(), Arrays.deepToString(originalFile
		        .getAttributes().toArray()));
		log.debug("Currently set attributes on file {}:{}", warped
		        .getAbsolutePath(), Arrays.deepToString(warped.getAttributes()
		        .toArray()));
		log.debug("Warping {} to {}, saving in {}", new Object[] {
		        originalFile.getAbsolutePath(), ref.getAbsolutePath(),
		        warped.getAbsolutePath() });
		warp2D(warped, ref, originalFile, path, this.indexedVars, toLHS);
		warp1D(warped, ref, originalFile, path, this.plainVars, toLHS);
		warpAnchors(warped, processedFile, path, toLHS);
		warped.addSourceFile(processedFile);
		warped.save();
		return warped;
	}

	/**
	 * Warp non-indexed arrays, which are for example scan_acquisisition_time,
	 * tic etc.
	 * 
	 * @param warpedB
	 *            target IFileFragment to store warped arrays
	 * @param ref
	 *            reference IFileFragment
	 * @param toBeWarped
	 *            the to-be-warped IFileFragment
	 * @param path
	 *            alignment path of alignment between a and b
	 * @param plainVars
	 *            list of variable names, which should be warped
	 * @param toLHS
	 *            warp to left hand side
	 * @return FileFragment containing warped data
	 */
	public IFileFragment warp1D(final IFileFragment warpedB,
	        final IFileFragment ref, final IFileFragment toBeWarped,
	        final List<Tuple2DI> path, final List<String> plainVars,
	        final boolean toLHS) {
		for (final String s : plainVars) {
			if (s.equals(this.indexVar)) {
				continue;
			}
			log.debug("Warping variable {}", s);
			IVariableFragment var = null;
			Array warpedA = null;
			if (warpedB.hasChild(s)) {
				var = warpedB.getChild(s);
			} else {
				var = new VariableFragment(warpedB, s);
			}
			if (toLHS) {// a is on lhs of path
				log
				        .debug("Warping to lhs {}, {} from file {}",
				                new Object[] { ref.getName(), s,
				                        toBeWarped.getName() });
				final Array refA = ref.getChild(s).getArray();
				warpedA = toBeWarped.getChild(s).getArray();
				// there exists a subtle bug, if variables in original file has
				// an empty array
				// then this array might be null
				if (warpedA != null) {
					// refA is only needed for correct shape and data type
					warpedA = ArrayTools
					        .projectToLHS(refA, path, warpedA, true);
				}

			} else { // whether b is on lhs of path
				log
				        .debug("Warping to lhs {}, {} from file {}",
				                new Object[] { ref.getName(), s,
				                        toBeWarped.getName() });
				final Array refA = ref.getChild(s).getArray();
				warpedA = toBeWarped.getChild(s).getArray();
				// there exists a subtle bug, if variables in original file has
				// an empty array
				// then this array might be null
				if (warpedA != null) {
					// refA is only needed for correct shape and data type
					warpedA = ArrayTools
					        .projectToRHS(refA, path, warpedA, true);
				}
			}
			var.setArray(warpedA);
		}
		return warpedB;
	}

    /**
     * Warp indexed arrays, which are lists of 1D arrays, so pseudo 2D. In this
     * case limited to mass_values and intensity_values.
     * 
     * @param warpedB
     *            target IFileFragment to store warped arrays
     * @param ref
     *            reference IFileFragment
     * @param toBeWarped
     *            the to-be-warped IFileFragment
     * @param path
     *            alignment path of alignment between a and b
     * @param plainVars
     *            list of variable names, which should be warped
     * @param toLHS
     *            whether warping should be done from right to left (true) or
     *            vice versa (false)
     * @return FileFragment containing warped data
     */
    public IFileFragment warp2D(final IFileFragment warpedB,
            final IFileFragment ref, final IFileFragment toBeWarped,
            final List<Tuple2DI> path, final List<String> indexedVars,
            final boolean toLHS) {
        // indexVar.setArray(b.getChild(this.indexVar).getArray());
        // log.info("Index Variable {}",indexVar.getArray());
        // for(String s:indexedVars) {
        final String s1 = "mass_values";
        final String s2 = "intensity_values";

		IVariableFragment ivf1 = null, ivf2 = null;
		List<Array> tbwa1 = null, tbwa2 = null;
		Tuple2D<List<Array>, List<Array>> t = null;
		final ArrayList<Tuple2DI> al = new ArrayList<Tuple2DI>(path.size());
		al.addAll(path);
		// if(sourceOnLHS){ //whether b is on lhs of path
		// log.info("Processing {} indexed by {} from file {}",new
		// Object[]{s1,this.indexVar,b.getName()});
		// log.info("Processing {} indexed by {} from file {}",new
		// Object[]{s2,this.indexVar,b.getName()});
		// a.getChild(s1).setIndex(a.getChild(this.indexVar));
		// a.getChild(s2).setIndex(a.getChild(this.indexVar));
		// List<Array> aA1 = a.getChild(s1).getIndexedArray();
		// List<Array> aA2 = a.getChild(s2).getIndexedArray();
		// b.getChild(s1).setIndex(b.getChild(this.indexVar));
		// b.getChild(s2).setIndex(b.getChild(this.indexVar));
		// bA1 = b.getChild(s1).getIndexedArray();
		// bA2 = b.getChild(s2).getIndexedArray();
		// //aA is only needed for correct shape and data type
		// t = ArrayTools.project2(false,aA1, aA2, al, bA1, bA2);
		// bA1 = t.getFirst();
		// bA2 = t.getSecond();
		// }else{//a is on lhs of path
		// log.info("Processing {} indexed by {} from file {}",new
		// Object[]{s1,this.indexVar,b.getName()});
		// log.info("Processing {} indexed by {} from file {}",new
		// Object[]{s2,this.indexVar,b.getName()});
		// a.getChild(s1).setIndex(a.getChild(this.indexVar));
		// a.getChild(s2).setIndex(a.getChild(this.indexVar));
		// List<Array> aA1 = a.getChild(s1).getIndexedArray();
		// List<Array> aA2 = a.getChild(s2).getIndexedArray();
		// b.getChild(s1).setIndex(b.getChild(this.indexVar));
		// b.getChild(s2).setIndex(b.getChild(this.indexVar));
		// bA1 = b.getChild(s1).getIndexedArray();
		// bA2 = b.getChild(s2).getIndexedArray();
		// //aA is only needed for correct shape and data type
		// t = ArrayTools.project2(true,aA1, aA2, al, bA1, bA2);
		// bA1 = t.getFirst();
		// bA2 = t.getSecond();
		// }
		try {
			log.debug("Processing {} indexed by {} from file {}",
			        new Object[] { s1, this.indexVar, toBeWarped.getName() });
			log.debug("Processing {} indexed by {} from file {}",
			        new Object[] { s2, this.indexVar, toBeWarped.getName() });
			ref.getChild(s1).setIndex(ref.getChild(this.indexVar));
			ref.getChild(s2).setIndex(ref.getChild(this.indexVar));
			final List<Array> aA1 = ref.getChild(s1).getIndexedArray();
			final List<Array> aA2 = ref.getChild(s2).getIndexedArray();
			toBeWarped.getChild(s1)
			        .setIndex(toBeWarped.getChild(this.indexVar));
			toBeWarped.getChild(s2)
			        .setIndex(toBeWarped.getChild(this.indexVar));
			tbwa1 = toBeWarped.getChild(s1).getIndexedArray();
			tbwa2 = toBeWarped.getChild(s2).getIndexedArray();
			// aA is only needed for correct shape and data type
			// if(toLHS) {
			// t = ArrayTools.project2(toLHS,bA1, bA2, al, aA1, aA2);
			// }else{
			// t = ArrayTools.project2(toLHS,aA1, aA2, al, bA1, bA2);
			// }
			t = ArrayTools.project2(toLHS, aA1, aA2, al, tbwa1, tbwa2,
			        this.average);
			tbwa1 = t.getFirst();
			tbwa2 = t.getSecond();
			// Update index variable
			IVariableFragment indexVar = null;
			if (warpedB.hasChild(this.indexVar)) {
				indexVar = warpedB.getChild(this.indexVar);
			} else {
				indexVar = new VariableFragment(warpedB, this.indexVar);
			}
			final ArrayInt.D1 index = new ArrayInt.D1(tbwa1.size());
			int offset = 0;
			for (int i = 0; i < tbwa1.size(); i++) {
				index.set(i, offset);
				offset += tbwa1.get(i).getShape()[0];
			}
			indexVar.setArray(index);
			// Set all arrays
			if (warpedB.hasChild(s1)) {
				ivf1 = warpedB.getChild(s1);
			} else {
				ivf1 = new VariableFragment(warpedB, s1);
			}
			if (warpedB.hasChild(s2)) {
				ivf2 = warpedB.getChild(s2);
			} else {
				ivf2 = new VariableFragment(warpedB, s2);
			}
			ivf1.setIndex(indexVar);
			ivf2.setIndex(indexVar);

			final List<Array> warpedMasses = new ArrayList<Array>();
			for (final Array a : tbwa1) {
				final ArrayDouble.D1 ad = new ArrayDouble.D1(a.getShape()[0]);
				MAMath.copyDouble(ad, a);
				warpedMasses.add(ad);
			}
			ivf1.setIndexedArray(warpedMasses);
			final List<Array> warpedIntensities = new ArrayList<Array>();
			for (final Array a : tbwa2) {
				final ArrayDouble.D1 ad = new ArrayDouble.D1(a.getShape()[0]);
				MAMath.copyDouble(ad, a);
				warpedIntensities.add(ad);
			}
			ivf2.setIndexedArray(warpedIntensities);
			// }
		} catch (final ResourceNotAvailableException rnae) {
			log.warn("Could not warp scans: {}", rnae);
		}
		return warpedB;
	}

    /**
     * Warp anchors according to pairwise scan mapping in path, reads anchors
     * from processedFile and stores them in warped file. If toLHS is true,
     * assumes target scale of warp on left side of path, processedFile should
     * contain the data to the right side of the path.
     * 
     * @param warped
     * @param processedFile
     * @param path
     * @param toLHS
     */
    public void warpAnchors(final IFileFragment warped,
            final IFileFragment toBeWarped, final List<Tuple2DI> path,
            final boolean toLHS) {
        final List<IAnchor> l = MaltcmsTools.prepareAnchors(toBeWarped);
        if (l.isEmpty()) {
            return;
        }
        final ArrayInt.D1 anchPos = new ArrayInt.D1(l.size());
        final ArrayChar.D2 anchNames = cross.datastructures.tools.ArrayTools.
                createStringArray(l.size(), 1024);
        for (int j = 0; j < l.size(); j++) {
            if (toLHS) {
                final int warpedAnchor = ArrayTools.getNewIndexOnLHS(l.get(j).
                        getScanIndex(), path);
                anchPos.set(j, warpedAnchor);
                anchNames.setString(j, l.get(j).getName());
            } else {
                final int warpedAnchor = ArrayTools.getNewIndexOnRHS(l.get(j).
                        getScanIndex(), path);
                anchPos.set(j, warpedAnchor);
                anchNames.setString(j, l.get(j).getName());
            }
        }
        IVariableFragment anchorScanIndex = new VariableFragment(warped,
                this.anchorScanIndexVariableName);
        anchorScanIndex.setArray(anchPos);
        IVariableFragment anchorName = new VariableFragment(warped,
                this.anchorNameVariableName);

        anchorName.setArray(anchNames);
    }
}
