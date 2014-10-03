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
import cross.datastructures.tools.FragmentTools;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.workflow.IWorkflow;
import java.util.List;
import maltcms.tools.MaltcmsTools;
import ucar.ma2.Array;

/**
 * Specialization for TICs.
 *
 * @author Nils Hoffmann
 * 
 */
public class TICWarpInput implements IWarpInput {

    private List<Tuple2DI> path = null;
    private IFileFragment targetFile = null;
    private IFileFragment refFile = null;
    private IFileFragment queryFile = null;
    private Tuple2D<List<Array>, List<Array>> tuple = null;

    /**
     * <p>Constructor for TICWarpInput.</p>
     *
     * @param ff a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param iw a {@link cross.datastructures.workflow.IWorkflow} object.
     */
    public TICWarpInput(final IFileFragment ff, final IWorkflow iw) {
        this.path = MaltcmsTools.getWarpPath(ff);
        this.refFile = FragmentTools.getLHSFile(ff);
        this.queryFile = FragmentTools.getRHSFile(ff);
        this.targetFile = FragmentTools.createFragment(this.refFile,
                this.queryFile, iw.getOutputDirectory(this));
        this.tuple = MaltcmsTools
                .prepareInputArraysTICasList(new Tuple2D<>(
                                this.refFile, this.queryFile));
    }

    /** {@inheritDoc} */
    @Override
    public String getAlgorithm() {
        return "TIC";
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
}
