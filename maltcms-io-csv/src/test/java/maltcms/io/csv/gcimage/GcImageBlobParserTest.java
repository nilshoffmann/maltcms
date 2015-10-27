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
import cross.test.LogMethodName;
import cross.test.SetupLogging;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.ms.Chromatogram2D;
import maltcms.datastructures.peak.Peak2D;
import maltcms.io.csv.ParserUtilities;
import maltcms.io.csv.gcimage.GcImageBlobParser.ColumnName;
import maltcms.test.ZipResourceExtractor;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.LocaleUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class GcImageBlobParserTest {

    @Rule
    public TemporaryFolder tf = new TemporaryFolder();
    @Rule
    public SetupLogging sl = new SetupLogging();
    @Rule
    public LogMethodName lmn = new LogMethodName();

    private static final double[] secondColumnRts = {
        2.23,
        1.99,
        2.72,
        1.92,
        2,
        2.3,
        2.79,
        1.89,
        1.95,
        1.97
    };

    @Test
    public void testCommonsCsvParsing() throws IOException {
        File dataFolder = tf.newFolder("gcImageTestFolder");
        File file = ZipResourceExtractor.extract(
                "/csv/gcimage/reduced/mut_t1_a.csv.gz", dataFolder);
        Locale locale = Locale.US;
        CSVParser parser = null;
        try {
            parser = GcImageBlobParser.open(file, "\"");
            List<CSVRecord> records = parser.getRecords();
            Assert.assertEquals(9, parser.getHeaderMap().size());
            Assert.assertEquals(375, records.size());
            long counter = 1;
            for (CSVRecord r : records) {
                log.info("Record " + r);
                String blobId = r.get(ColumnName.BLOBID);
                Assert.assertEquals(counter + "", blobId);
                Assert.assertEquals("Group " + counter, r.get(ColumnName.GROUP_NAME));
                Assert.assertEquals("", r.get(ColumnName.INCLUSION));
                if (counter <= 10) {
                    Assert.assertEquals(
                            secondColumnRts[((int) counter) - 1],
                            ParserUtilities.parseDouble(r.get(ColumnName.RETENTION_II), locale),
                            0.0d
                    );
                }
                counter++;
            }
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }

    @Test
    public void testCommonsCsvBlobImporter() throws IOException {
        File dataFolder = tf.newFolder("gcImageTestFolder");
        File sourceFile = ZipResourceExtractor.extract(
                "/cdf/2D/mut_t1_a.cdf.gz", dataFolder);
        File file = ZipResourceExtractor.extract(
                "/csv/gcimage/reduced/mut_t1_a.csv.gz", dataFolder);
        File subFolder = tf.newFolder("gcImageRefTestFolder");
        FileFragment tmpFrag = new FileFragment(subFolder, sourceFile.getName());
        tmpFrag.addSourceFile(new FileFragment(sourceFile));
        tmpFrag.addChild("modulation_time").setArray(Array.factory(new double[]{5}));
        tmpFrag.addChild("scan_rate").setArray(Array.factory(new double[]{100}));
        Chromatogram2D chrom = new Chromatogram2D(tmpFrag);
        Assert.assertEquals(375, chrom.getNumberOfScans());
        Assert.assertEquals(500, chrom.getNumberOfScansPerModulation());
        Locale locale = Locale.US;
        GcImageBlobImporter importer = new GcImageBlobImporter("\"", locale);
        List<Peak2D> peaks = importer.importPeaks(file, chrom);
        Assert.assertEquals(375, peaks.size());
    }

    @Test
    public void testLocaleParsing() {
        Locale localeEnUs = LocaleUtils.toLocale("en_US");
        Assert.assertEquals(Locale.US, localeEnUs);
        Locale localeDeDe = LocaleUtils.toLocale("de_DE");
        Assert.assertEquals(Locale.GERMANY, localeDeDe);
    }
}
