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

import cross.Factory;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import cross.exception.ResourceNotAvailableException;
import cross.io.IDataSource;
import cross.tools.StringTools;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.io.andims.NetcdfDataSource;
import maltcms.io.csv.chromatof.ChromaTOFParser.ColumnName;
import maltcms.io.csv.chromatof.ChromaTOFParser.Mode;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.lang.LocaleUtils;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.nc2.Attribute;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
@Data
@ServiceProvider(service = IDataSource.class)
public class ChromaTOFDataSource implements IDataSource {

    private Locale locale = Locale.US;
    private String[] supportedFormats = {"csv", "txt", "tsv"};
    private NetcdfDataSource ndf = new NetcdfDataSource();

    private File getCdfFileForPeakList(File importDir, String peakListName) {
        File fragmentFile = new File(importDir, StringTools.removeFileExt(
                peakListName) + ".cdf");
        return fragmentFile;
    }

    private synchronized IFileFragment toCdfFile(File peakFile) {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"), "maltcms");
        File baseDir = new File(tmpDir, ChromaTOFDataSource.class.getSimpleName());
        File sourceDirectoryUUID = new File(baseDir, UUID.nameUUIDFromBytes(peakFile.getParentFile().getAbsolutePath().getBytes()).toString());
        sourceDirectoryUUID.mkdirs();
        //TODO digest peakFile, store in sourceDirectoryUUID/.peakFile.getName()
        //only reuse file if digest matches that stored in .peakFile.getName()
        File cdfFile = getCdfFileForPeakList(sourceDirectoryUUID, peakFile.getName());
        if (cdfFile.exists()) {
            log.info("Using existing temporary artificial chromatogram file {}", cdfFile);
            return new FileFragment(cdfFile);
        }
        log.info("Creating new temporary artificial chromatogram file {}", cdfFile);
        try {
            ChromaTOFParser parser = ChromaTOFParser.create(peakFile, true, locale);
            Tuple2D<LinkedHashSet<ChromaTOFParser.TableColumn>, List<TableRow>> report = ChromaTOFParser.parseReport(parser, peakFile, true);
            List<TableRow> peaks = report.getSecond();
            Mode mode = parser.getMode(peaks);
            //this file fragments requires the same file extension as the original peak list, but in the cdfFile location.
            //calling save() below will create the actual cdf file.
            FileFragment f = new FileFragment(cdfFile);
            ChromaTOFImporter importer = new ChromaTOFImporter(locale);
            importer.createArtificialChromatogram(peaks, mode, f).save();
            return new FileFragment(cdfFile);
        } catch (IllegalArgumentException iae) {
            throw new RuntimeException("Could not read file "
                    + cdfFile, iae);
        }
    }

    @Override
    public int canRead(IFileFragment ff) {
        try {
            ChromaTOFParser parser = ChromaTOFParser.create(new File(ff.getUri()), true, locale);
            String fieldSeparator = ChromaTOFParser.FIELD_SEPARATOR_COMMA;
            String quotationCharacter = ChromaTOFParser.QUOTATION_CHARACTER_DOUBLETICK;
            //switch between modes
            if (ff.getName().toLowerCase().endsWith(".csv")) {
                fieldSeparator = ChromaTOFParser.FIELD_SEPARATOR_COMMA;
                quotationCharacter = ChromaTOFParser.QUOTATION_CHARACTER_DOUBLETICK;
            } else if (ff.getName().toLowerCase().endsWith(".txt") || ff.getName().toLowerCase().endsWith(".tsv")) {
                fieldSeparator = ChromaTOFParser.FIELD_SEPARATOR_TAB;
                quotationCharacter = ChromaTOFParser.QUOTATION_CHARACTER_NONE;
            }
            //inspect header
            LinkedHashSet<ChromaTOFParser.TableColumn> header = parser.parseHeader(new File(ff.getUri()), true, fieldSeparator, quotationCharacter);
            ChromaTOFParser.ColumnName[] gcMsColumns = {ColumnName.RETENTION_TIME_SECONDS, ColumnName.SPECTRA};
            //this case also covers fused gcxgc data, where both retention times are stored in the R.T. (s) field
            int isChromaTofGcMsFile = 0;
            for(ChromaTOFParser.TableColumn tc:header) {
                for (ColumnName cn : gcMsColumns) {
                    if (tc.getColumnName()==cn) {
                        isChromaTofGcMsFile++;
                    }
                }
            }
            int isChromaTofGcGcMsFile = 0;
            ChromaTOFParser.ColumnName[] gcgcMsColumns = {ColumnName.FIRST_DIMENSION_TIME_SECONDS, ColumnName.SECOND_DIMENSION_TIME_SECONDS, ColumnName.SPECTRA};
            //this case covers separate gcxgc data
            for(ChromaTOFParser.TableColumn tc:header) {
                for (ColumnName cn : gcgcMsColumns) {
                    if (tc.getColumnName()==cn) {
                        isChromaTofGcGcMsFile++;
                    }
                }
            }
            if (isChromaTofGcMsFile==2 || isChromaTofGcGcMsFile==3) {
                return 1;
            }
        } catch (IllegalArgumentException iae) {
            throw new RuntimeException("Could not read file "
                    + ff, iae);
        }
        return 0;
    }

    private IFileFragment getConvertedPeakList(IFileFragment f) {
        IFileFragment convertedPeakList = toCdfFile(new File(f.getUri()));
        return convertedPeakList;
    }

    @Override
    public ArrayList<Array> readAll(IFileFragment f) throws IOException, ResourceNotAvailableException {
        IFileFragment convertedPeakList = getConvertedPeakList(f);
        return ndf.readAll(convertedPeakList);
    }

    @Override
    public ArrayList<Array> readIndexed(IVariableFragment f) throws IOException, ResourceNotAvailableException {
        IFileFragment convertedPeakList = getConvertedPeakList(f.getParent());
        IVariableFragment fc = convertedPeakList.getChild(f.getName());
        IVariableFragment ic = convertedPeakList.getChild(f.getIndex().getName());
        fc.setIndex(ic);
        return ndf.readIndexed(fc);
    }

    @Override
    public Array readSingle(IVariableFragment f) throws IOException, ResourceNotAvailableException {
        IFileFragment convertedPeakList = getConvertedPeakList(f.getParent());
        IVariableFragment toBeRead = VariableFragment.createCompatible(convertedPeakList, f);
        Array array = ndf.readSingle(toBeRead);
        f.setRange(toBeRead.getRange());
        f.setDimensions(toBeRead.getDimensions());
        f.setAttributes(toBeRead.getAttributes().toArray(new Attribute[toBeRead.getAttributes().size()]));
        return array;
    }

    @Override
    public ArrayList<IVariableFragment> readStructure(IFileFragment f) throws IOException {
        IFileFragment convertedPeakList = getConvertedPeakList(f);
        ArrayList<IVariableFragment> al = new ArrayList<>();
        for (IVariableFragment ivf : ndf.readStructure(convertedPeakList)) {
            IVariableFragment toBeCreated = VariableFragment.createCompatible(f, ivf);
            toBeCreated.setDataType(ivf.getDataType());
            toBeCreated.setRange(ivf.getRange());
            toBeCreated.setDimensions(ivf.getDimensions());
            toBeCreated.setAttributes(ivf.getAttributes().toArray(new Attribute[ivf.getAttributes().size()]));
            al.add(toBeCreated);
        }
        return al;
    }

    @Override
    public IVariableFragment readStructure(IVariableFragment f) throws IOException, ResourceNotAvailableException {
        IFileFragment convertedPeakList = getConvertedPeakList(f.getParent());
        IVariableFragment toBeRead = VariableFragment.createCompatible(convertedPeakList, f);
        toBeRead = ndf.readStructure(toBeRead);
        f.setDataType(toBeRead.getDataType());
        f.setRange(toBeRead.getRange());
        f.setDimensions(toBeRead.getDimensions());
        f.setAttributes(toBeRead.getAttributes().toArray(new Attribute[toBeRead.getAttributes().size()]));
        return f;
    }

    @Override
    public List<String> supportedFormats() {
        return Arrays.asList(supportedFormats);
    }

    @Override
    public boolean write(IFileFragment f) {
        EvalTools.notNull(this.ndf, this);
        // TODO Implement real write support
        log.info("Saving {} with ChromaTOFDataSource", f.getUri());
        log.info("Changing output file from: {}", f.toString());
        File file = new File(f.getUri());
        String filename = StringTools.removeFileExt(file.getAbsolutePath());
        filename += ".cdf";
        f.setFile(filename);
        f.addSourceFile(getConvertedPeakList(f));
        log.info("To: {}", filename);
        return Factory.getInstance().getDataSourceFactory().getDataSourceFor(f).write(f);
    }

    @Override
    public void configure(Configuration cfg) {
        locale = LocaleUtils.toLocale(cfg.getString(getClass().getName() + ".locale", "en_US"));
        this.ndf = new NetcdfDataSource();
        this.ndf.configure(cfg);
    }

    @Override
    public void configurationChanged(ConfigurationEvent ce) {
        if (ce.getPropertyName().equals(getClass().getName() + ".locale")) {
            locale = LocaleUtils.toLocale((String) ce.getPropertyValue());
        }
    }

}
