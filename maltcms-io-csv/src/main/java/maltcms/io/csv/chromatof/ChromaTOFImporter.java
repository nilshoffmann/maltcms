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
package maltcms.io.csv.chromatof;

import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tools.ArrayTools;
import cross.datastructures.tuple.Tuple2D;
import cross.tools.MathTools;
import cross.tools.StringTools;
import java.io.File;
import static java.lang.Double.parseDouble;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.ms.IChromatogram;
import maltcms.datastructures.peak.Peak1D;
import maltcms.datastructures.peak.Peak2D;
import maltcms.datastructures.peak.PeakType;
import maltcms.io.csv.ParserUtilities;
import maltcms.io.csv.chromatof.ChromaTOFParser.Mode;
import org.springframework.util.Assert;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.MAMath;
import ucar.nc2.Dimension;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
@Value
public class ChromaTOFImporter {

    private final String fieldSeparator;
    private final String quotationCharacter;
    private final Locale locale;

    /**
     * Import a list of 1D peaks from the given peak report and chromatogram.
     * These peaks 
     * @param peakReport the peak report to use for the import.
     * @param chromatogram the chromatogram related to this peak report.
     * @return  a list of {@link Peak1D} objects.
     */
    public List<Peak1D> importPeaks(File peakReport, IChromatogram chromatogram) {
        ChromaTOFParser parser = ChromaTOFParser.create(peakReport, true, locale);
        LinkedHashSet<ChromaTOFParser.TableColumn> columnNames = parser.parseHeader(peakReport, true, ChromaTOFParser.FIELD_SEPARATOR_COMMA, ChromaTOFParser.QUOTATION_CHARACTER_DOUBLETICK);
        List<TableRow> records = parser.parseBody(columnNames, peakReport, true, ChromaTOFParser.FIELD_SEPARATOR_COMMA, ChromaTOFParser.QUOTATION_CHARACTER_DOUBLETICK);
        List<Peak1D> peaks = parseTable(peakReport, records, chromatogram, parser.getMode(records));
        return peaks;
    }

    /**
     * Creates an artificial chromatogram of mass spectra from the peaks contained 
     * in the given peakReport file. The newly created chromatogram will be saved 
     * in extended cdf format, which may contain first and second column retention 
     * times, if they were present in the original peak file. Other peak data is not 
     * available from the cdf file.
     * @param parser the parser instance to use.
     * @param importDir the directory in which the new artificial chromatogram should be created.
     * @param peakReport the peak report file.
     * @param peaks the list of table rows of the parser.
     * @return the created artifical chromatogram in cdf format.
     */
    public File createArtificialChromatogram(ChromaTOFParser parser, File importDir, File peakReport, List<TableRow> peaks) {
        ChromaTOFParser.Mode mode = parser.getMode(peaks);
        return createArtificialChromatogram(parser, importDir, peakReport.getName(), peaks, mode);
    }

    protected List<Peak1D> parseTable(File peakReport, List<TableRow> records,
            IChromatogram chromatogram, Mode mode) {
        List<Peak1D> peaks = new ArrayList<>();
        int index = 0;
        for (TableRow record : records) {
            Logger.getLogger(ChromaTOFImporter.class.getName()).log(Level.INFO, record.toString());
            if(mode==Mode.RT_2D_FUSED || mode==Mode.RT_2D_SEPARATE) {
                double rt1 = ParserUtilities.parseDouble(record.getValueForName(ChromaTOFParser.ColumnName.FIRST_DIMENSION_TIME_SECONDS), locale);
                double rt2 = ParserUtilities.parseDouble(record.getValueForName(ChromaTOFParser.ColumnName.SECOND_DIMENSION_TIME_SECONDS), locale);
                peaks.add(create2DPeak(index, peakReport, record, rt1, rt2));
            }else{
                peaks.add(create1DPeak(index, peakReport, record));
            }
            index++;
        }
        return peaks;
    }

    protected Peak1D create1DPeak(int index, File peakReport, TableRow tr) {
        //System.out.println("1D chromatogram peak data detected");
        double area = ParserUtilities.parseDouble(tr.getValueForName(ChromaTOFParser.ColumnName.AREA), locale);
        Peak1D p1 = Peak1D.builder1D().
            name(tr.getValueForName(ChromaTOFParser.ColumnName.NAME)).
            file(peakReport.getAbsolutePath()).
            apexTime(parseDouble((tr.getValueForName(ChromaTOFParser.ColumnName.RETENTION_TIME_SECONDS)))).
            area(area).
            apexIntensity(area).
            name(tr.getValueForName(ChromaTOFParser.ColumnName.NAME)).
            peakType(PeakType.EIC_FILTERED).
        build();
        Assert.isTrue(!Double.isNaN(p1.getArea()));
        Assert.isTrue(!Double.isNaN(p1.getApexIntensity()));
        return p1;
    }

    protected Peak2D create2DPeak(int index, File peakReport, TableRow tr, double rt1, double rt2) {
        double area = ParserUtilities.parseDouble(tr.getValueForName(ChromaTOFParser.ColumnName.AREA), locale);
        Peak2D p2 = Peak2D.builder2D().
            name(tr.getValueForName(ChromaTOFParser.ColumnName.NAME)).
            file(peakReport.getAbsolutePath()).
            firstRetTime(rt1).
            secondRetTime(rt2).
            apexTime(rt1 + rt2).
            area(area).
            apexIntensity(area).
            name(tr.getValueForName(ChromaTOFParser.ColumnName.NAME)).
            peakType(PeakType.EIC_FILTERED).
        build();
        Assert.isTrue(!Double.isNaN(p2.getArea()));
        Assert.isTrue(!Double.isNaN(p2.getApexIntensity()));
        return p2;
    }

    protected File createArtificialChromatogram(ChromaTOFParser parser, File importDir,
            String peakListName, List<TableRow> peaks, Mode mode) {
        File fragment = new File(importDir, StringTools.removeFileExt(
                peakListName) + ".cdf");
        FileFragment f = new FileFragment(fragment);
        Dimension scanNumber = new Dimension("scan_number", peaks.size(), true);
        int points = 0;
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
        for (TableRow tr : peaks) {
            Tuple2D<double[], int[]> ms = ParserUtilities.convertMassSpectrum(tr.getValueForName(ChromaTOFParser.ColumnName.SPECTRA));
            minMass = Math.min(minMass, MathTools.min(ms.getFirst()));
            maxMass = Math.max(maxMass, MathTools.max(ms.getFirst()));
            massMin.set(i, minMass);
            massMax.set(i, maxMass);
            masses.add(Array.factory(ms.getFirst()));
            Array intensA = Array.factory(ms.getSecond());
            intensities.add(intensA);
            scanIndex.set(i, scanOffset);
            tic.setDouble(i, MAMath.sumDouble(intensA));
            originalIndex.set(i, i);
            double satValue = Double.NaN;
            if (mode == ChromaTOFParser.Mode.RT_2D_FUSED) {
                String[] rts = tr.getValueForName(ChromaTOFParser.ColumnName.RETENTION_TIME_SECONDS).split(",");
                firstColumnElutionTime.set(i, parser.parseDouble(rts[0]));
                secondColumnElutionTime.set(i, parser.parseDouble(rts[1]));
                satValue = firstColumnElutionTime.get(i) + secondColumnElutionTime.get(i);
            } else if (mode == ChromaTOFParser.Mode.RT_2D_SEPARATE) {
                firstColumnElutionTime.set(i, parser.parseDouble(tr.getValueForName(ChromaTOFParser.ColumnName.FIRST_DIMENSION_TIME_SECONDS)));
                secondColumnElutionTime.set(i, parser.parseDouble(tr.getValueForName(ChromaTOFParser.ColumnName.SECOND_DIMENSION_TIME_SECONDS)));
                satValue = firstColumnElutionTime.get(i) + secondColumnElutionTime.get(i);
            } else {
                satValue = parser.parseDouble(tr.getValueForName(ChromaTOFParser.ColumnName.RETENTION_TIME_SECONDS));
            }
            sat.setDouble(i, satValue);
            scanOffset += ms.getFirst().length;
            points += ms.getFirst().length;
            i++;
        }
        Dimension pointNumber = new Dimension("point_number", points, true);
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
        if (mode == ChromaTOFParser.Mode.RT_2D_FUSED || mode == ChromaTOFParser.Mode.RT_2D_SEPARATE) {
            IVariableFragment firstColumnElutionTimeVar = new VariableFragment(f, "first_column_elution_time");
            firstColumnElutionTimeVar.setArray(firstColumnElutionTime);
            firstColumnElutionTimeVar.setDimensions(new Dimension[]{scanNumber});
            IVariableFragment secondColumnElutionTimeVar = new VariableFragment(f, "second_column_elution_time");
            secondColumnElutionTimeVar.setArray(secondColumnElutionTime);
            secondColumnElutionTimeVar.setDimensions(new Dimension[]{scanNumber});
        }
        f.save();
        return fragment;
    }

}
