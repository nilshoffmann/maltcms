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
 * $Id: ChromatogramWarp2.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */
package maltcms.commands.fragments.warp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import maltcms.datastructures.ms.IAnchor;
import maltcms.io.csv.CSVReader;
import maltcms.tools.ArrayTools;
import maltcms.tools.MaltcmsTools;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.MAMath;
import cross.Logging;
import cross.annotations.Configurable;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import cross.datastructures.tools.EvalTools;
import cross.tools.StringTools;
import java.util.Arrays;

/**
 * Use Objects of this class to apply an alignment, warping a source
 * chromatogram to the time/scans of reference chromatogram.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@RequiresVariables(names = {"var.multiple_alignment",
    "var.multiple_alignment_names", "var.multiple_alignment_type",
    "var.multiple_alignment_creator"})
public class ChromatogramWarp2 extends AFragmentCommand {

    @Configurable
    private List<String> indexedVars = Arrays.asList("mass_values",
            "intensity_values");
    @Configurable
    private List<String> plainVars = Arrays.asList("total_intensity",
            "scan_acquisition_time");
    @Configurable
    private String indexVar = "scan_index";
    private final Logger log = Logging.getLogger(this);
    @Configurable(name = "var.anchors.retention_scans", type = String.class)
    private String anchorScanIndexVariableName = "retention_scans";
    @Configurable(name = "var.anchors.retention_index_names",
    type = String.class)
    private String anchorNameVariableName = "retention_index_names";
    @Configurable(name = "var.multiple_alignment")
    private String multipleAlignmentVariableName = "multiple_alignment";
    @Configurable(name = "var.multiple_alignment_names")
    private String multipleAlignmentNamesVariableName = "multiple_alignment_names";
    @Configurable(name = "var.multiple_alignment_type")
    private String multipleAlignmentTypeVariableName = "multiple_alignment_type";
    @Configurable(name = "var.multiple_alignment_creator")
    private String multipleAlignmentCreatorVariableName = "multiple_alignment_creator";
    // @Configurable
    // private String alignmentReference = "";
    //
    // @Configurable
    // private boolean warpToFirst = true;
    @Configurable
    private boolean averageCompressions = false;
    @Configurable
    private String alignmentLocation = "";
    @Configurable(name = "var.scan_acquisition_time")
    private String satvar = "scan_acquisition_time";

    private AlignmentTable buildTableFromFragment(IFileFragment f) {
        try {
            IVariableFragment ma = f.getChild(this.multipleAlignmentVariableName);
            ArrayChar.D3 maa = (ArrayChar.D3) ma.getArray();
            IVariableFragment maNames = f.getChild(
                    this.multipleAlignmentNamesVariableName);
            ArrayChar.D2 mana = (ArrayChar.D2) maNames.getArray();
            AlignmentTable at = new AlignmentTable(maa, mana);

            return at;
        } catch (ResourceNotAvailableException rnae) {
            this.log.warn("Could not retrieve alignment from FileFragment!");

        }
        return null;
    }

    public IFileFragment copyReference(final IFileFragment ref,
            final IFileFragment copy, final IWorkflow iw) {
        IVariableFragment ivf = null;
        for (final String s : this.indexedVars) {
            if (ref.hasChild(s)) {
                ivf = ref.getChild(s);
            } else {
                ivf = new VariableFragment(ref, s);
            }
            final IVariableFragment ivfnew = copy.hasChild(s) ? copy.getChild(s) : new VariableFragment(
                    copy, s);
            ivfnew.setArray(ivf.getArray());
        }
        for (final String s : this.plainVars) {
            if (ref.hasChild(s)) {
                ivf = ref.getChild(s);
            } else {
                ivf = new VariableFragment(ref, s);
            }
            final IVariableFragment ivfnew = copy.hasChild(s) ? copy.getChild(s) : new VariableFragment(
                    copy, s);
            ivfnew.setArray(ivf.getArray());
        }
        copy.addSourceFile(ref);
        return copy;
    }

			return at;
		} catch (ResourceNotAvailableException rnae) {
			log.warn("Could not retrieve alignment from FileFragment!");

        EvalTools.notNull(at, this);
        String refname = at.getRefName();

        IFileFragment refOrig = null;
        for (IFileFragment f : t) {
            if (StringTools.removeFileExt(f.getName()).equals(refname)) {
                refOrig = f;
            }
        }
        EvalTools.notNull(refOrig, this);
        // if (warpToFirst) {
        // ref = wt.get(0);
        // System.out.println("Warping to " + ref.getName());
        // }
        // if (alignmentReference != null && !alignmentReference.isEmpty()) {
        // for (IFileFragment f : wt) {
        // if (f.getName().contains(this.alignmentReference)
        // || f.getAbsolutePath()
        // .contains(this.alignmentReference)) {
        // ref = f;
        // ref.save();
        // }
        // }
        // }

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.ICommand#apply(java.lang.Object)
	 */
	@Override
	public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
		TupleND<IFileFragment> wt = createWorkFragments(t);
		log.info("{}", t);
		log.info("{}", wt);
		IFileFragment ref = null;
		// TODO enable arbitrary reference selection
		AlignmentTable at = null;
		// alignment location overrides, so try first
		if (this.alignmentLocation == null || this.alignmentLocation.isEmpty()) {
			log.info("Using alignment from fragment");
			at = buildTableFromFragment(t.get(0));
		} else {
			log.info("Using alignmentLocation");
			at = new AlignmentTable(new File(this.alignmentLocation));
		}

    class AlignmentTable {

        HashMap<String, List<int[]>> columnMap = new HashMap<String, List<int[]>>();
        String refName = "";

		log.info("Reference is {}", refOrig);
		for (int i = 0; i < wt.size(); i++) {
			IFileFragment queryTarget = wt.get(i);
			IFileFragment querySource = t.get(i);
			log.info("Processing {}/{}", (i + 1), wt.size());
			if (!StringTools.removeFileExt(queryTarget.getName()).equals(
			        refOrig.getName())) {
				log.info("Warping {} to {}", querySource.getName(),
				        refOrig.getName());
				IFileFragment res = warp(refOrig, querySource, queryTarget,
				        at.getPathFor(refOrig, queryTarget), true,
				        getWorkflow());
				res.save();
				DefaultWorkflowResult dwr = new DefaultWorkflowResult(new File(
				        res.getAbsolutePath()), this, WorkflowSlot.WARPING, res);
				getWorkflow().append(dwr);
			} else {
				IFileFragment res = copyReference(querySource, queryTarget,
				        getWorkflow());
				res.save();
				DefaultWorkflowResult dwr = new DefaultWorkflowResult(new File(
				        res.getAbsolutePath()), this, WorkflowSlot.WARPING, res);
				getWorkflow().append(dwr);
			}
		}
		return wt;
	}

        AlignmentTable(File f) {
            CSVReader csvr = new CSVReader();
            try {
                Tuple2D<Vector<Vector<String>>, Vector<String>> table = csvr.
                        read(new FileInputStream(f));
                refName = table.getSecond().get(0);
                for (Vector<String> line : table.getFirst()) {
                    int col = 0;
                    for (String s : line) {
                        String colName = table.getSecond().get(col);
                        String[] entry = s.split(",");
                        int[] vals = new int[entry.length];
                        int i = 0;
                        for (String es : entry) {
                            vals[i++] = Integer.parseInt(es);
                        }
                        List<int[]> l = null;
                        if (columnMap.containsKey(colName)) {
                            l = columnMap.get(colName);
                            l.add(vals);
                        } else {
                            l = new ArrayList<int[]>();
                            l.add(vals);
                            this.columnMap.put(colName, l);
                        }
                        col++;
                    }
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        String getRefName() {
            return refName;
        }

        List<Tuple2DI> getPathFor(IFileFragment ref, IFileFragment query) {
            List<int[]> refIdx = columnMap.get(StringTools.removeFileExt(ref.
                    getName()));
            List<int[]> queryIdx = columnMap.get(StringTools.removeFileExt(query.
                    getName()));
            EvalTools.eqI(refIdx.size(), queryIdx.size(), this);
            List<Tuple2DI> path = new ArrayList<Tuple2DI>();
            for (int i = 0; i < refIdx.size(); i++) {
                int[] ridx = refIdx.get(i);
                int[] qidx = queryIdx.get(i);
                for (int j = 0; j < qidx.length; j++) {
                    path.add(new Tuple2DI(ridx[0], qidx[j]));
                }
            }
            return path;
        }
    }

    @Override
    public void configure(final Configuration cfg) {
        this.indexedVars = StringTools.toStringList(cfg.getList(this.getClass().
                getName() + ".indexedVars"));
        this.indexVar = cfg.getString(this.getClass().getName() + ".indexVar",
                "scan_index");
        this.plainVars = StringTools.toStringList(cfg.getList(this.getClass().
                getName() + ".plainVars"));
        this.log.info("{}", this.plainVars);
        this.anchorScanIndexVariableName = cfg.getString(
                "var.anchors.retention_scans", "retention_scans");
        this.anchorNameVariableName = cfg.getString(
                "var.anchors.retention_names", "retention_names");
        this.averageCompressions = cfg.getBoolean(this.getClass().getName()
                + ".averageCompressions", false);
        this.alignmentLocation = cfg.getString(this.getClass().getName()
                + ".alignmentLocation");
        // this.warpToFirst = cfg.getBoolean(this.getClass().getName()
        // + ".warpToFirst", true);
        this.satvar = cfg.getString("var.scan_acquisition_time",
                "scan_acquisition_time");
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

	@Override
	public void configure(final Configuration cfg) {
		this.indexedVars = StringTools.toStringList(cfg.getList(this.getClass()
		        .getName() + ".indexedVars"));
		this.indexVar = cfg.getString(this.getClass().getName() + ".indexVar",
		        "scan_index");
		this.plainVars = StringTools.toStringList(cfg.getList(this.getClass()
		        .getName() + ".plainVars"));
		log.info("{}", this.plainVars);
		this.anchorScanIndexVariableName = cfg.getString(
		        "var.anchors.retention_scans", "retention_scans");
		this.anchorNameVariableName = cfg.getString(
		        "var.anchors.retention_names", "retention_names");
		this.averageCompressions = cfg.getBoolean(this.getClass().getName()
		        + ".averageCompressions", false);
		this.alignmentLocation = cfg.getString(this.getClass().getName()
		        + ".alignmentLocation");
		// this.warpToFirst = cfg.getBoolean(this.getClass().getName()
		// + ".warpToFirst", true);
		this.satvar = cfg.getString("var.scan_acquisition_time",
		        "scan_acquisition_time");
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
     * @param query
     * @param path
     * @param toLHS
     * @return
     */
    public IFileFragment warp(final IFileFragment ref,
            final IFileFragment querySource, final IFileFragment queryTarget,
            final List<Tuple2DI> path, final boolean toLHS, final IWorkflow iw) {
        this.log.info("Warping {}, saving in {}",
                querySource.getAbsolutePath(), queryTarget.getAbsolutePath());
        warp2D(queryTarget, ref, querySource, path, this.indexedVars, toLHS);
        warp1D(queryTarget, ref, querySource, path, this.plainVars, toLHS);
        warpAnchors(queryTarget, querySource, path, toLHS);
        queryTarget.addSourceFile(querySource);
        return queryTarget;
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
            if (s.equals(this.satvar)) {
                warpSAT(warpedB, ref, toBeWarped, path, plainVars, toLHS);
                continue;
            }
            this.log.info("Warping variable {}", s);
            IVariableFragment var = null;
            Array warpedA = null;
            if (warpedB.hasChild(s)) {
                var = warpedB.getChild(s);
            } else {
                var = new VariableFragment(warpedB, s);
            }
            if (toLHS) {// a is on lhs of path
                this.log.debug("Warping to lhs {}, {} from file {}",
                        new Object[]{ref.getName(), s,
                            toBeWarped.getName()});
                final Array refA = ref.getChild(s).getArray();
                warpedA = toBeWarped.getChild(s).getArray();
                // there exists a subtle bug, if variables in original file has
                // an empty array
                // then this array might be null
                if (warpedA != null) {
                    // refA is only needed for correct shape and data type
                    warpedA = ArrayTools.projectToLHS(refA, path, warpedA, true);
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
	 * @param query
	 * @param path
	 * @param toLHS
	 * @return
	 */
	public IFileFragment warp(final IFileFragment ref,
	        final IFileFragment querySource, final IFileFragment queryTarget,
	        final List<Tuple2DI> path, final boolean toLHS, final IWorkflow iw) {
		log.info("Warping {}, saving in {}",
		        querySource.getAbsolutePath(), queryTarget.getAbsolutePath());
		warp2D(queryTarget, ref, querySource, path, this.indexedVars, toLHS);
		warp1D(queryTarget, ref, querySource, path, this.plainVars, toLHS);
		warpAnchors(queryTarget, querySource, path, toLHS);
		queryTarget.addSourceFile(querySource);
		return queryTarget;
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
			if (s.equals(this.satvar)) {
				warpSAT(warpedB, ref, toBeWarped, path, plainVars, toLHS);
				continue;
			}
			log.info("Warping variable {}", s);
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
            this.log.warn("Could not warp scans: {}", rnae);
        }
        return warpedB;
    }

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
			        this.averageCompressions);
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

	private IFileFragment warpSAT(final IFileFragment warpedB,
	        final IFileFragment ref, final IFileFragment toBeWarped,
	        final List<Tuple2DI> path, final List<String> plainVars,
	        final boolean toLHS) {
		log.info("Warping scan acquisition time");
		IVariableFragment var = null;
		Array warpedA = null;
		final String s = this.satvar;
		if (warpedB.hasChild(s)) {
			var = warpedB.getChild(s);
		} else {
			var = new VariableFragment(warpedB, s);
		}
		if (toLHS) {// a is on lhs of path
			log.debug("Warping to lhs {}, {} from file {}", new Object[] {
			        ref.getName(), s, toBeWarped.getName() });
			final Array refA = ref.getChild(s).getArray();
			warpedA = toBeWarped.getChild(s).getArray();
			// there exists a subtle bug, if variables in original file has
			// an empty array
			// then this array might be null
			if (warpedA != null) {
				// refA is only needed for correct shape and data type
				warpedA = projectToLHS(refA, path, warpedA, true);
			}

		} else { // whether b is on lhs of path
			log.debug("Warping to lhs {}, {} from file {}", new Object[] {
			        ref.getName(), s, toBeWarped.getName() });
			final Array refA = ref.getChild(s).getArray();
			warpedA = toBeWarped.getChild(s).getArray();
			// there exists a subtle bug, if variables in original file has
			// an empty array
			// then this array might be null
			if (warpedA != null) {
				// refA is only needed for correct shape and data type
				warpedA = projectToRHS(refA, path, warpedA, true);
			}
		}
		var.setArray(warpedA);
		return warpedB;
	}

    /**
     * Warp anchors according to pairwise scan mapping in path, reads anchors
     * from processedFile and stores them in warped file. If toLHS is true,
     * assumes target scale of warp on left side of path, processedFile should
     * contain the data to the right side of the path.
     * 
     * @param queryTarget
     * @param processedFile
     * @param path
     * @param toLHS
     */
    public void warpAnchors(final IFileFragment queryTarget,
            final IFileFragment querySource, final List<Tuple2DI> path,
            final boolean toLHS) {
        final List<IAnchor> l = MaltcmsTools.prepareAnchors(querySource);
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
        IVariableFragment anchorScanIndex = null;
        try {
            anchorScanIndex = queryTarget.getChild(
                    this.anchorScanIndexVariableName);
            queryTarget.removeChild(anchorScanIndex);
        } catch (ResourceNotAvailableException rnae) {
        }
        anchorScanIndex = new VariableFragment(queryTarget,
                this.anchorScanIndexVariableName);

        anchorScanIndex.setArray(anchPos);

        IVariableFragment anchorName = null;
        try {
            anchorName = queryTarget.getChild(this.anchorNameVariableName);
            queryTarget.removeChild(anchorName);
        } catch (ResourceNotAvailableException rnae) {
        }
        anchorName = new VariableFragment(queryTarget,
                this.anchorNameVariableName);

        anchorName.setArray(anchNames);
    }
}
