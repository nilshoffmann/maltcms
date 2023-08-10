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
package maltcms.datastructures.warp;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FragmentTools;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.workflow.IWorkflow;
import java.util.List;

import maltcms.tools.MaltcmsTools;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;

/**
 * Specialization for MZIWarp, using mass spectra.
 *
 * @author Nils Hoffmann
 * 
 */

public class MZIWarpInput implements IWarpInput {
        
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(MZIWarpInput.class);

    private List<Tuple2DI> path = null;
    private IFileFragment targetFile = null;
    private IFileFragment refFile = null;
    private IFileFragment queryFile = null;
    private Tuple2D<List<Array>, List<Array>> tuple = null;

    /**
     * <p>Constructor for MZIWarpInput.</p>
     *
     * @param ff a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param iw a {@link cross.datastructures.workflow.IWorkflow} object.
     */
    public MZIWarpInput(final IFileFragment ff, final IWorkflow iw) {
        log
                .info("#############################################################################");
        final String s = this.getClass().getName();
        log.info("# {} running", s);
        log
                .info("#############################################################################");
        log.info("Preparing input for {} with sources {}", new Object[]{
            ff.getUri(), ff.getSourceFiles()});
        final IFileFragment queryFile1 = FragmentTools.getRHSFile(ff);
        final IFileFragment referenceFile = FragmentTools.getLHSFile(ff);

        final List<Array> i1 = MaltcmsTools.getBinnedMZIs(referenceFile)
                .getSecond();
        final List<Array> i2 = MaltcmsTools.getBinnedMZIs(queryFile1)
                .getSecond();
        final Tuple2D<List<Array>, List<Array>> t = new Tuple2D<>(
                i1, i2);
        final IFileFragment target = FragmentTools.createFragment(
                referenceFile, queryFile1, iw.getOutputDirectory(this));
        init(MaltcmsTools.getWarpPath(ff), t, referenceFile, queryFile1, target);
    }

    /**
     * <p>Constructor for MZIWarpInput.</p>
     *
     * @param path1 a {@link java.util.List} object.
     * @param tuple1 a {@link cross.datastructures.tuple.Tuple2D} object.
     * @param referenceFile a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param queryFile1 a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param targetFile1 a {@link cross.datastructures.fragments.IFileFragment} object.
     */
    public MZIWarpInput(final List<Tuple2DI> path1,
            final Tuple2D<List<Array>, List<Array>> tuple1,
            final IFileFragment referenceFile, final IFileFragment queryFile1,
            final IFileFragment targetFile1) {
        init(path1, tuple1, referenceFile, queryFile1, targetFile1);
    }

    /** {@inheritDoc} */
    @Override
    public String getAlgorithm() {
        return "MZI";
    }

    /** {@inheritDoc} */
    @Override
    public Tuple2D<List<Array>, List<Array>> getArrays() {
        return this.tuple;
    }

    /** {@inheritDoc} */
    @Override
    public IFileFragment getFileFragment() {
        return this.targetFile;
    }

    /** {@inheritDoc} */
    @Override
    public List<Tuple2DI> getPath() {
        return this.path;
    }

    /** {@inheritDoc} */
    @Override
    public IFileFragment getQueryFileFragment() {
        return this.queryFile;
    }

    /** {@inheritDoc} */
    @Override
    public IFileFragment getReferenceFileFragment() {
        return this.refFile;
    }

    /**
     * <p>init.</p>
     *
     * @param path1 a {@link java.util.List} object.
     * @param tuple1 a {@link cross.datastructures.tuple.Tuple2D} object.
     * @param referenceFile a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param queryFile1 a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param targetFile1 a {@link cross.datastructures.fragments.IFileFragment} object.
     */
    protected void init(final List<Tuple2DI> path1,
            final Tuple2D<List<Array>, List<Array>> tuple1,
            final IFileFragment referenceFile, final IFileFragment queryFile1,
            final IFileFragment targetFile1) {
        EvalTools.notNull(new Object[]{path1, targetFile1, referenceFile,
            queryFile1, tuple1}, this);
        this.path = path1;
        this.targetFile = targetFile1;
        this.refFile = referenceFile;
        this.queryFile = queryFile1;
        this.tuple = tuple1;
    }
}
