/*
 * Copyright (C) 2009, 2010 Mathias Wilhelm mwilhelm A T
 * TechFak.Uni-Bielefeld.DE
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
 * $Id: Default2DVarLoader.java 135 2010-07-05 08:04:51Z nilshoffmann $
 */
package maltcms.commands.fragments2d.preprocessing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import cross.Factory;
import cross.Logging;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * This command should be the first in all 2d pipelines. It creates different
 * variables like scan_rate, modulation_time etc.
 * 
 * TODO find threshold index and save it?
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
@RequiresVariables(names = {""})
@RequiresOptionalVariables(names = {""})
@ProvidesVariables(names = {"var.total_intensity", "var.modulation_time",
    "var.scan_rate", "var.scan_duration", "var.second_column_time",
    "var.second_column_scan_index", "var.total_intensity_1d",
    "var.scan_acquisition_time", "var.scan_acquisition_time_1d",
    "var.total_intensity_2d"})
@Slf4j
@Data
public class Default2DVarLoader extends AFragmentCommand {

    @Configurable(name = "var.total_intensity", value = "total_intensity")
    private String totalIntensityVar = "total_intensity";
    @Configurable(name = "var.modulation_time", value = "modulation_time")
    private String modulationTimeVar = "modulation_time";
    @Configurable(name = "var.scan_rate", value = "scan_rate")
    private String scanRateVar = "scan_rate";
    @Configurable(name = "var.scan_duration", value = "scan_duration")
    private String scanDurationVar = "scan_duration";
    @Configurable(name = "var.second_column_time", value = "second_column_time")
    private String secondColumnTimeVar = "second_column_time";
    @Configurable(name = "var.second_column_scan_index",
    value = "second_column_scan_index")
    private String secondColumnScanIndexVar = "second_column_scan_index";
    @Configurable(name = "var.total_intensity_1d", value = "total_intensity_1d")
    private String totalIntensity1dVar = "total_intensity_1d";
    @Configurable(name = "var.scan_aquisition", value = "scan_aquisition")
    private String scanAcquisitionTimeVar = "scan_aquisition";
    @Configurable(name = "var.scan_acquisition_1d",
    value = "scan_acquisition_1d")
    private String scanAcquisitionTime1dVar = "scan_acquisition_1d";
    @Configurable(name = "var.total_intensity_2d", value = "total_intensity_2d")
    private String totalIntensity2dVar = "total_intensity_2d";
    @Configurable(name = "var.modulation_time.default", value = "5.0d")
    private double modulationTime = 5.0d;
    private double scanDuration = 0.005d;
    private double scanRate = 200.0d;
    private String modulationIndex0Dimension = "modulation_index_0";
    private String modulationIndex1Dimension = "modulation_index_1";
    private String modulationTimeDimension = "modulation_time";
    private String scanRateDimension = "scan_rate";
    private String scanNumberDimension = "scan_number";

    /**
     * {@inheritDoc}
     */
    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        final ArrayList<IFileFragment> ret = new ArrayList<IFileFragment>();
        for (final IFileFragment ff : t) {
            log.info("Running var loader for {}", ff.getName());
            final IFileFragment fret = Factory.getInstance().
                    getFileFragmentFactory().create(
                    new File(getWorkflow().getOutputDirectory(this),
                    ff.getName()));
            fret.addSourceFile(ff);

            log.info("Creating {}", this.scanRateVar);
            createScanRate(ff, fret);
            log.info("Creating {}", this.modulationTimeVar);
            createModulation(ff, fret);

            log.info("Creating {}", this.secondColumnScanIndexVar);
            createSecondColumnIndex(ff, fret);

            log.info("Creating {}", this.secondColumnTimeVar);
            createSecondColumnTime(ff, fret);
            log.info("Creating {}", this.totalIntensity1dVar);
            create1DTic(ff, fret);
            log.info("Creating {}", this.totalIntensity2dVar);
            createTIC2D(ff, fret);

            final DefaultWorkflowResult dwr = new DefaultWorkflowResult(
                    new File(fret.getAbsolutePath()), this, getWorkflowSlot(),
                    ff);
            getWorkflow().append(dwr);

            fret.save();
            ret.add(fret);
        }

        return new TupleND<IFileFragment>(ret);
    }

    /**
     * Build a list containing scansPerModulation elements.
     *
     * @param scansPerModulation
     *            scans per modulation
     * @param array
     *            array
     * @return list of array
     */
    private ArrayList<Array> buildIndexedArray(final int scansPerModulation,
            final Array array) {
        final int size = array.getShape()[0] / scansPerModulation;
        final int modulus = array.getShape()[0] % scansPerModulation;
        if (modulus != 0) {
            log.info(
                    "Warning: found {} dangling scans at end of chromatogram, will truncate!",
                    modulus);
        }
        log.info("Size of array: {}, reading {} scans!",
                array.getShape()[0], size);
        final ArrayList<Array> al = new ArrayList<Array>(size);
        int offset = 0;
        final int len = scansPerModulation;
        for (int i = 0; i < size; i++) {
            try {
                log.debug("offset: {}", offset);
                log.debug("reading until {}", offset + len - 1);
                if ((offset + len) <= array.getShape()[0]) {
                    final Array a = array.section(new int[]{offset},
                            new int[]{len});
                    al.add(a);
                }
            } catch (final InvalidRangeException e) {
                log.warn("Invalid range while reading scan {}/{}", i, size);
                e.printStackTrace();
            }
            offset += len;
        }
        log.info("Number of scans in list: {}", al.size());
        return al;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final Configuration cfg) {
        this.totalIntensityVar = cfg.getString("var.total_intensity",
                "total_intensity");
        this.modulationTimeVar = cfg.getString("var.modulation_time",
                "modulation_time");
        this.scanRateVar = cfg.getString("var.scan_rate", "scan_rate");
        this.scanDurationVar = cfg.getString("var.scan_duration",
                "scan_duration");
        this.secondColumnTimeVar = cfg.getString("var.second_column_time",
                "second_column_time");
        this.secondColumnScanIndexVar = cfg.getString(
                "var.second_column_scan_index", "second_column_scan_index");
        this.totalIntensity1dVar = cfg.getString("var.total_intensity_1d",
                "total_intensity_1d");
        this.scanAcquisitionTimeVar = cfg.getString(
                "var.scan_acquisition_time", "scan_acquisition_time");
        this.scanAcquisitionTime1dVar = cfg.getString(
                "var.scan_acquisition_time_1d", "scan_acquisition_time_1d");
        this.totalIntensity2dVar = cfg.getString("var.total_intensity_2d",
                "total_intensity_2d");

        this.scanRate = cfg.getDouble("var.scan_rate.default", 200.0d);
        this.modulationTime = cfg.getDouble("var.modulation_time.default", 5.0d);
    }

    /**
     * Creates the 1d tic array and saves it to the parent file.
     *
     * @param source
     *            source file fragment
     * @param parent
     *            parent file fragment
     * @return {@link IVariableFragment} for total_intensity_1d
     */
    private IVariableFragment create1DTic(final IFileFragment source,
            final IFileFragment parent) {
        final int scanspermodulation = (int) (this.scanRate * this.modulationTime);
        // FIXME sollte eigentlich schon mit indexed array laufen. leider nur
        // fehler
        // Could not find physical file for
        // /homes/mwilhelm/maltcms/Default2DVarLoader/090303-Gc4H210mg_1.cdf
        Array totalIntensityArray = source.getChild(this.totalIntensityVar).
                getArray();
        final List<Array> tic = buildIndexedArray(scanspermodulation,
                totalIntensityArray);
        final List<Array> sat = buildIndexedArray(scanspermodulation, source.
                getChild(this.scanAcquisitionTimeVar).getArray());
        log.info("Number of scans in list: {}", tic.size());
        final Array tic1d = Array.factory(totalIntensityArray.getElementType(), new int[]{tic.
                size()});
        Index tic1dIndex = tic1d.getIndex();
        final ArrayDouble.D1 sat1d = new ArrayDouble.D1(tic.size());
        int count = 0;
        for (final Array ticArray : tic) {
            final IndexIterator iter = ticArray.getIndexIterator();
            double sum = 0;
            while (iter.hasNext()) {
                sum += iter.getDoubleNext();
            }
            log.debug("Processing modulation {}",count);
            tic1dIndex.set(count);
            tic1d.setDouble(tic1dIndex, sum);
            sat1d.set(count, ((ArrayDouble.D1) sat.get(count)).get(0));
            count++;
        }

        final IVariableFragment tic1dvar = new VariableFragment(parent,
                this.totalIntensity1dVar);
        tic1dvar.setArray(tic1d);
        tic1dvar.setDimensions(new Dimension[]{new Dimension(
                    modulationIndex0Dimension,
                    tic.size(), true)});
        final IVariableFragment sat1dvar = new VariableFragment(parent,
                this.scanAcquisitionTime1dVar);
        sat1dvar.setArray(sat1d);
        sat1dvar.setDimensions(new Dimension[]{new Dimension(
                    modulationIndex0Dimension,
                    tic.size(), true)});
        return tic1dvar;
    }

    /**
     * Creates the modulation_time.
     *
     * @param source
     *            source file fragment
     * @param parent
     *            parent file fragment
     * @return {@link IVariableFragment} for modulation_time
     */
    private IVariableFragment createModulation(final IFileFragment source,
            final IFileFragment parent) {
        final Index idx = Index.scalarIndexImmutable;
        final IVariableFragment modulationvar = new VariableFragment(parent,
                this.modulationTimeVar);
        final Array modulationArray = Array.factory(Double.class,
                new int[]{1});
        if (Math.rint(this.modulationTime) != this.modulationTime) {
            throw new IllegalArgumentException(
                    "Modulation time must be an integer, was: "
                    + this.modulationTime);
        }
        log.info("Setting modulation time to {}", this.modulationTime);
        modulationArray.setDouble(idx, this.modulationTime);
        modulationvar.setArray(modulationArray);
        modulationvar.setDimensions(new Dimension[]{new Dimension(modulationTimeDimension,1,true)});
        return modulationvar;
    }

    /**
     * Estimates the scan_rate from the scan_duration and saves it to the parent
     * {@link IFileFragment}.
     *
     * @param source
     *            source file fragment
     * @param parent
     *            parent file fragment
     * @return {@link IVariableFragment} for scan_rate
     */
    private IVariableFragment createScanRate(final IFileFragment source,
            final IFileFragment parent) {

        try {
            final Array durationarray = source.getChild(this.scanDurationVar).
                    getArray();
            final IndexIterator iter = durationarray.getIndexIterator();
            this.scanDuration = iter.getDoubleNext();
            this.scanRate = 1 / this.scanDuration;
            log.info("Found " + this.scanDurationVar + "({}) and "
                    + this.scanRateVar + "({})", this.scanDuration,
                    this.scanRate);
        } catch (final ResourceNotAvailableException e) {
            log.error("Couldnt find {} using default {}",
                    this.scanDurationVar, this.scanDuration);
        }

        final Index idx = Index.scalarIndexImmutable;
        final IVariableFragment scanratevar = new VariableFragment(parent,
                this.scanRateVar);
        final Array scanRateArray = Array.factory(Double.class, new int[]{1});
        scanRateArray.setDouble(idx, this.scanRate);
        scanratevar.setArray(scanRateArray);
        scanratevar.setDimensions(new Dimension[]{new Dimension(scanRateDimension,1,true)});
        return scanratevar;
    }

    /**
     * Creates the {@link IVariableFragment} for second_column_scan_index.
     *
     * @param source
     *            source file fragment
     * @param parent
     *            parent file fragment
     * @return {@link IVariableFragment} for second_column_scan_index
     */
    private IVariableFragment createSecondColumnIndex(
            final IFileFragment source, final IFileFragment parent) {
        final Integer ticcount = (int) (source.getChild(this.totalIntensityVar).
                getArray().getShape()[0]);
        final Integer scanspermodulation = (int) (this.scanRate * this.modulationTime);
        Integer modulationCnt;
        // if (ticcount % scanspermodulation == 0) {
        modulationCnt = (ticcount / scanspermodulation);
        log.info("ticcount: {}, scanspermodulation: {}, scancount: {}",
                new Object[]{ticcount, scanspermodulation, modulationCnt});
        // } else {
        // scancount = (ticcount / scanspermodulation) + 1;
        // }
        final ArrayInt.D1 secondColumnIndex = new ArrayInt.D1(modulationCnt);
        for (int i = 0; i < modulationCnt; i++) {
            secondColumnIndex.set(i, scanspermodulation * i);
        }
        final IVariableFragment index2dvar = new VariableFragment(parent,
                this.secondColumnScanIndexVar);
        index2dvar.setArray(secondColumnIndex);
        index2dvar.setDimensions(new Dimension[]{new Dimension(
                    modulationIndex0Dimension, modulationCnt, true)});
        return index2dvar;
    }

    /**
     * Creates an {@link Array} with the same shape like the total_intensity and
     * saves the second_column_time.
     *
     * @param source
     *            source file fragment
     * @param parent
     *            parent file fragment
     * @return {@link IVariableFragment} for second_column_time
     */
    private IVariableFragment createSecondColumnTime(
            final IFileFragment source, final IFileFragment parent) {

        final IVariableFragment originalTICVar = source.getChild(
                this.totalIntensityVar);
        final Array secondRetTimeArray = Array.factory(Double.class, originalTICVar.
                getArray().getShape());

//		final IndexIterator ticiter = source.getChild(this.totalIntensityVar)
//		        .getArray().getIndexIterator();
        final IndexIterator timeiter = secondRetTimeArray.getIndexIterator();

        double c = 0;
        final Integer scanspermodulation = (int) (this.scanRate * this.modulationTime);
        while (timeiter.hasNext()) {
            timeiter.setDoubleNext(
                    ((c % scanspermodulation) * this.scanDuration));
//			                + this.scanDuration);
//			ticiter.next();
            c++;
        }

        final IVariableFragment secondcolumnvar = new VariableFragment(parent,
                this.secondColumnTimeVar);
        secondcolumnvar.setArray(secondRetTimeArray);
        secondcolumnvar.setDimensions(new Dimension[]{new Dimension(scanNumberDimension,
                    secondRetTimeArray.getShape()[0], true)});
        return secondcolumnvar;
    }

    private IVariableFragment createTIC2D(final IFileFragment source,
            final IFileFragment parent) {
        final int scanspermodulation = (int) (this.scanRate * this.modulationTime);
        final List<Array> tic = buildIndexedArray(scanspermodulation, source.
                getChild(this.totalIntensityVar).getArray());
        log.info("Number of scans in list: {}", tic.size());
        final Array tic2d = Array.factory(tic.get(0).getElementType(), new int[]{tic.
                    size(), scanspermodulation});
        final Index idx2d = tic2d.getIndex();
        int slcount = 0;
        for (final Array ticArray : tic) {
            for (int i = 0; i < scanspermodulation; i++) {
                idx2d.set(slcount, i);
                tic2d.setDouble(idx2d, ticArray.getDouble(i));
            }
            slcount++;
        }

        final IVariableFragment tic2dvar = new VariableFragment(parent,
                this.totalIntensity2dVar);
        tic2dvar.setArray(tic2d);
        tic2dvar.setDimensions(new Dimension[]{new Dimension(
                    modulationIndex0Dimension,
                    tic.size(), true), new Dimension(modulationIndex1Dimension,
                    scanspermodulation, true)});
        return tic2dvar;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Default var loader for 2d data. Will create different variables.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.GENERAL_PREPROCESSING;
    }
}
