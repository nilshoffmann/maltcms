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
package maltcms.io.csv.gcimage;

import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tools.ArrayTools;
import cross.tools.StringTools;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.ms.IChromatogram;
import maltcms.datastructures.ms.IChromatogram1D;
import maltcms.datastructures.ms.IScan1D;
import maltcms.datastructures.peak.Peak2D;
import maltcms.datastructures.peak.PeakType;
import maltcms.io.csv.ParserUtilities;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.util.Assert;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import ucar.nc2.Dimension;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
@Value
public class GcImageBlobImporter {

    private final String quotationCharacter;
    private final Locale locale;

    public List<Peak2D> importPeaks(File peakReport, IChromatogram chromatogram) {
        CSVParser parser = null;
        try {
            parser = GcImageBlobParser.open(peakReport, quotationCharacter);
            List<CSVRecord> report = parser.getRecords();
            List<Peak2D> peaks = parseTable(report, chromatogram);
            return peaks;
        } catch (IOException ex) {
            log.warn("Caught IOException while reading {}", peakReport, ex);
        } finally {
            if (parser != null) {
                try {
                    parser.close();
                } catch (IOException ex) {
                    log.warn("Caught IOException while closing {}", peakReport, ex);
                }
            }
        }
        return Collections.emptyList();
    }

    public File createArtificialChromatogram(File importDir, IChromatogram1D chromatogram, List<Peak2D> peaks) {
        return createArtificialChromatogram(chromatogram, importDir,
                chromatogram.getParent().getName(),
                peaks);
    }

    public List<Peak2D> parseTable(List<CSVRecord> records,
            IChromatogram chromatogram) {
        List<Peak2D> peaks = new ArrayList<Peak2D>();
        int index = 0;
        for (CSVRecord record : records) {
            Logger.getLogger(GcImageBlobImporter.class.getName()).log(Level.INFO, record.toString());
            double rt1 = ParserUtilities.parseDouble(record.get(GcImageBlobParser.ColumnName.RETENTION_I), locale);
            //convert from minutes to seconds
            rt1 *= 60;
            double rt2 = ParserUtilities.parseDouble(record.get(GcImageBlobParser.ColumnName.RETENTION_II), locale);
            peaks.add(create2DPeak(index, chromatogram, record, rt1, rt2));
            index++;
        }
        return peaks;
    }

    public Peak2D create2DPeak(int index, IChromatogram chromatogram, CSVRecord tr, double rt1, double rt2) {
        log.debug("Adding peak at rt1: " + rt1 + " ; rt2: " + rt2);
        double volume = ParserUtilities.parseDouble(tr.get(GcImageBlobParser.ColumnName.VOLUME), locale);
        double value = ParserUtilities.parseDouble(tr.get(GcImageBlobParser.ColumnName.PEAK_VALUE), locale);
        Peak2D peak2d = Peak2D.builder2D().
            firstRetTime(rt1).
            secondRetTime(rt2).
            apexTime(rt1 + rt2).
            area(volume).
            apexIntensity(value).
            index(index).
            name(tr.get(GcImageBlobParser.ColumnName.COMPOUND_NAME)).
            peakType(PeakType.TIC_FILTERED).
            apexIndex(chromatogram.getIndexFor(rt1 + rt2)).
        build();
        Assert.isTrue(!Double.isNaN(peak2d.getArea()));
        Assert.isTrue(!Double.isNaN(peak2d.getApexIntensity()));
        return peak2d;
    }

    public File createArtificialChromatogram(IChromatogram1D chrom2d, File importDir,
            String peakListName, List<Peak2D> peaks) {
        File fragment = new File(importDir, StringTools.removeFileExt(
                peakListName) + ".cdf");
        FileFragment f = new FileFragment(fragment);
        Dimension scanNumber = new Dimension("scan_number", peaks.size(), true);
        int points = 0;
//		f.addDimensions(scanNumber);
        List<Array> masses = new ArrayList<>();
        List<Array> intensities = new ArrayList<>();
        Array sat = new ArrayDouble.D1(peaks.size());
        ArrayInt.D1 originalIndex = new ArrayInt.D1(peaks.size());
        ArrayInt.D1 scanIndex = new ArrayInt.D1(peaks.size());
        ArrayInt.D1 tic = new ArrayInt.D1(peaks.size());
        ArrayDouble.D1 massMin = new ArrayDouble.D1(peaks.size());
        ArrayDouble.D1 massMax = new ArrayDouble.D1(peaks.size());
        ArrayDouble.D1 firstColumnElutionTime = new ArrayDouble.D1(peaks.size());
        ArrayDouble.D1 secondColumnElutionTime = new ArrayDouble.D1(peaks.size());
        int i = 0;
        int scanOffset = 0;
        double minMass = Double.POSITIVE_INFINITY;
        double maxMass = Double.NEGATIVE_INFINITY;
        for (Peak2D descr : peaks) {
            IScan1D scan2d = chrom2d.getScan(chrom2d.getIndexFor(descr.getApexTime()));
            MinMax mm = MAMath.getMinMax(scan2d.getMasses());
            minMass = Math.min(minMass, mm.min);
            maxMass = Math.max(maxMass, mm.max);
            massMin.set(i, minMass);
            massMax.set(i, maxMass);
            masses.add(scan2d.getMasses());
            intensities.add(scan2d.getIntensities());
            sat.setDouble(i, descr.getApexTime());
            scanIndex.set(i, scanOffset);
            tic.setDouble(i, MAMath.sumDouble(scan2d.getIntensities()));
            originalIndex.set(i, descr.getIndex());
            firstColumnElutionTime.set(i, descr.getFirstRetTime());
            secondColumnElutionTime.set(i, descr.getSecondRetTime());
            scanOffset += masses.get(i).getShape()[0];
            points += masses.get(i).getShape()[0];
            i++;
        }
        Dimension pointNumber = new Dimension("point_number", points, true);
//		f.addDimensions(scanNumber, pointNumber);
        IVariableFragment scanIndexVar = new VariableFragment(f,
                "scan_index");
        scanIndexVar.setArray(scanIndex);
        scanIndexVar.setDimensions(new Dimension[]{scanNumber});
        IVariableFragment originalIndexVar = new VariableFragment(f,
                "original_index");
        originalIndexVar.setArray(originalIndex);
        originalIndexVar.setDimensions(new Dimension[]{scanNumber});
        IVariableFragment massValuesVar = new VariableFragment(f,
                "mass_values");
        massValuesVar.setArray(ArrayTools.glue(masses));
        massValuesVar.setDimensions(pointNumber);
        IVariableFragment intensityValuesVar = new VariableFragment(f,
                "intensity_values");
        intensityValuesVar.setArray(ArrayTools.glue(intensities));
        intensityValuesVar.setDimensions(pointNumber);
        IVariableFragment satVar = new VariableFragment(f,
                "scan_acquisition_time");
        satVar.setArray(sat);
        satVar.setDimensions(new Dimension[]{scanNumber});
        IVariableFragment ticVar = new VariableFragment(f,
                "total_intensity");
        ticVar.setArray(tic);
        ticVar.setDimensions(new Dimension[]{scanNumber});
        IVariableFragment minMassVar = new VariableFragment(f, "mass_range_min");
        minMassVar.setArray(massMin);
        minMassVar.setDimensions(new Dimension[]{scanNumber});
        IVariableFragment maxMassVar = new VariableFragment(f, "mass_range_max");
        maxMassVar.setArray(massMax);
        maxMassVar.setDimensions(new Dimension[]{scanNumber});
        IVariableFragment firstColumnElutionTimeVar = new VariableFragment(f, "first_column_elution_time");
        firstColumnElutionTimeVar.setArray(firstColumnElutionTime);
        firstColumnElutionTimeVar.setDimensions(new Dimension[]{scanNumber});
        IVariableFragment secondColumnElutionTimeVar = new VariableFragment(f, "second_column_elution_time");
        secondColumnElutionTimeVar.setArray(secondColumnElutionTime);
        secondColumnElutionTimeVar.setDimensions(new Dimension[]{scanNumber});
        f.save();
        return fragment;
    }

}
