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
package maltcms.datastructures.fragments;

import cross.IConfigurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FileTools;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.NotImplementedException;
import maltcms.datastructures.IFileFragmentModifier;
import org.apache.commons.configuration.Configuration;
import org.jdom2.Element;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayDouble.D2;
import ucar.ma2.ArrayInt;

/**
 * <p>PairwiseDistances class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class PairwiseDistances implements IFileFragmentModifier, IConfigurable,
        IWorkflowElement {

    /**
     * Static factory method which reconstructs a PairwiseDistances object from
     * the given FileFragment.
     *
     * @param pwdFrag a {@link cross.datastructures.fragments.IFileFragment} object.
     * @return a {@link maltcms.datastructures.fragments.PairwiseDistances} object.
     */
    public static PairwiseDistances fromFileFragment(final IFileFragment pwdFrag) {
        final PairwiseDistances pwd = new PairwiseDistances();
        pwd.pwDistFileFragment = pwdFrag;
        return pwd;
    }
    private IFileFragment pwDistFileFragment;
    private ArrayDouble.D2 pairwiseDistances;
    private String pwDistMatrixVariableName = "pairwise_distance_matrix";
    private String pwDistVariableName = "pairwise_distance_names";
    private String pwDistAlignmentsVarName = "pairwise_distance_alignment_names";
    private String name = "pairwise_distances.cdf";
    private boolean minimize;
    private String minArrayComp = "minimizing_array_comp";
    private ucar.ma2.ArrayChar.D2 names;
    private TupleND<IFileFragment> alignments;
    private IWorkflow iw;

    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration cfg) {
        this.pwDistMatrixVariableName = cfg.getString(
                "var.pairwise_distance_matrix", "pairwise_distance_matrix");
        this.pwDistVariableName = cfg.getString("var.pairwise_distance_names",
                "pairwise_distance_names");
        this.pwDistAlignmentsVarName = cfg.getString(
                "var.pairwise_distance_alignment_names",
                "pairwise_distance_alignment_names");
        this.name = cfg.getString("pairwise_distances_file_name",
                "pairwise_distances.cdf");

    }

    /**
     * <p>Getter for the field <code>alignments</code>.</p>
     *
     * @return a {@link cross.datastructures.tuple.TupleND} object.
     */
    public TupleND<IFileFragment> getAlignments() {
        return this.alignments;
    }

    /** {@inheritDoc} */
    @Override
    public IWorkflow getWorkflow() {
        return this.iw;
    }

    /**
     * <p>Getter for the field <code>minArrayComp</code>.</p>
     *
     * @return the minArrayComp
     */
    public String getMinArrayComp() {
        return this.minArrayComp;
    }

    /**
     * <p>Getter for the field <code>pairwiseDistances</code>.</p>
     *
     * @return the pairwiseDistances
     */
    public ArrayDouble.D2 getPairwiseDistances() {
        return this.pairwiseDistances;
    }

    /**
     * <p>Getter for the field <code>pwDistAlignmentsVarName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPwDistAlignmentsVarName() {
        return this.pwDistAlignmentsVarName;
    }

    /**
     * <p>Getter for the field <code>pwDistMatrixVariableName</code>.</p>
     *
     * @return the pwDistMatrixVariableName
     */
    public String getPwDistMatrixVariableName() {
        return this.pwDistMatrixVariableName;
    }

    /**
     * <p>Getter for the field <code>pwDistVariableName</code>.</p>
     *
     * @return the pwDistVariableName
     */
    public String getPwDistVariableName() {
        return this.pwDistVariableName;
    }

    /**
     * <p>isMinimize.</p>
     *
     * @return the minimize
     */
    public boolean isMinimize() {
        return this.minimize;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.experimental.datastructures.IFileFragmentModifier#decorate(cross
     * .datastructures.fragments.IFileFragment)
     */
    /** {@inheritDoc} */
    @Override
    public void modify(IFileFragment f) {
        final IVariableFragment pwd = new VariableFragment(f,
                this.pwDistMatrixVariableName);
        pwd.setArray(this.pairwiseDistances);
        final IVariableFragment na = new VariableFragment(f,
                this.pwDistVariableName);
        na.setArray(this.names);
        final IVariableFragment minimizing = new VariableFragment(f,
                this.minArrayComp);
        final ArrayInt.D0 ab = new ArrayInt.D0();
        ab.set(this.minimize ? 1 : 0);
        minimizing.setArray(ab);
        final IVariableFragment alignments = new VariableFragment(f,
                this.pwDistAlignmentsVarName);
        int maxlength = 128;
        for (final IFileFragment iff : this.alignments) {
            if (iff.getUri().toString().length() > maxlength) {
                maxlength = iff.getUri().toString().length();
            }
        }
        final ArrayChar.D2 anames = cross.datastructures.tools.ArrayTools.createStringArray(
                this.alignments.getSize(), maxlength);
        int i = 0;
        for (final IFileFragment iff : this.alignments) {
            anames.setString(i++, FileTools.getRelativeUri(f.getUri(), iff.getUri()).toString());
        }
        alignments.setArray(anames);
    }

    /**
     * <p>Setter for the field <code>alignments</code>.</p>
     *
     * @param t a {@link cross.datastructures.tuple.TupleND} object.
     */
    public void setAlignments(final TupleND<IFileFragment> t) {
        this.alignments = t;
    }

    /** {@inheritDoc} */
    @Override
    public void setWorkflow(final IWorkflow iw1) {
        this.iw = iw1;
    }

    /**
     * <p>Setter for the field <code>minArrayComp</code>.</p>
     *
     * @param minArrayComp the minArrayComp to set
     */
    public void setMinArrayComp(final String minArrayComp) {
        this.minArrayComp = minArrayComp;
    }

    /**
     * <p>Setter for the field <code>minimize</code>.</p>
     *
     * @param minimize the minimize to set
     */
    public void setMinimize(final boolean minimize) {
        this.minimize = minimize;
    }

    /**
     * <p>Setter for the field <code>name</code>.</p>
     *
     * @param name1 a {@link java.lang.String} object.
     */
    public void setName(final String name1) {
        this.name = name1;
    }

    /**
     * <p>Setter for the field <code>names</code>.</p>
     *
     * @param names1 a {@link ucar.ma2.ArrayChar.D2} object.
     */
    public void setNames(final ucar.ma2.ArrayChar.D2 names1) {
        EvalTools.notNull(names1, this);
        this.names = names1;
    }

    /**
     * <p>Setter for the field <code>pairwiseDistances</code>.</p>
     *
     * @param pairwiseDistances1 a {@link ucar.ma2.ArrayDouble.D2} object.
     */
    public void setPairwiseDistances(final D2 pairwiseDistances1) {
        EvalTools.notNull(pairwiseDistances1, this);
        this.pairwiseDistances = pairwiseDistances1;
    }

    /**
     * <p>Setter for the field <code>pwDistAlignmentsVarName</code>.</p>
     *
     * @param pwDistAlignmentsVarName a {@link java.lang.String} object.
     */
    public void setPwDistAlignmentsVarName(final String pwDistAlignmentsVarName) {
        this.pwDistAlignmentsVarName = pwDistAlignmentsVarName;
    }

    /**
     * <p>Setter for the field <code>pwDistMatrixVariableName</code>.</p>
     *
     * @param pwDistMatrixVariableName the pwDistMatrixVariableName to set
     */
    public void setPwDistMatrixVariableName(
            final String pwDistMatrixVariableName) {
        this.pwDistMatrixVariableName = pwDistMatrixVariableName;
    }

    /**
     * <p>Setter for the field <code>pwDistVariableName</code>.</p>
     *
     * @param pwDistVariableName the pwDistVariableName to set
     */
    public void setPwDistVariableName(final String pwDistVariableName) {
        this.pwDistVariableName = pwDistVariableName;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    /** {@inheritDoc} */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.CLUSTERING;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.io.xml.IXMLSerializable#appendXML(org.jdom.Element)
     */
    /** {@inheritDoc} */
    @Override
    public void appendXML(Element e) {
        throw new NotImplementedException();
    }
}
