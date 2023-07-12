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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.QuoteMode;

/**
 *
 * @author Nils Hoffmann
 */
public class GcImageBlobParser {

    public enum ColumnName {

        /**
         * The blob id. Original column name: "BlobID"
         */
        BLOBID("BlobID"),
        /**
         * The putative compound name. Original column name: "Compound Name"
         */
        COMPOUND_NAME("Compound Name"),
        /**
         * The assigned group name. Original column name: "Group Name"
         */
        GROUP_NAME("Group Name"),
        /**
         * The inclusion flag. Original column name: "Inclusion"
         */
        INCLUSION("Inclusion"),
        /**
         * The name of the internal standard. Original column name: "Internal Standard"
         */
        INTERNAL_STANDARD("Internal Standard"),
        /**
         * The first column retention time of a GCxGC peak. Original column name: "Retention I (min)"
         */
        RETENTION_I("Retention I (min)"),
        /**
         * The second column retention time of a GCxGC peak. Original column name: "Retention II (sec)"
         */
        RETENTION_II("Retention II (sec)"),
        /**
         * The peak value. Original column name: "Peak Value"
         */
        PEAK_VALUE("Peak Value"),
        /**
         * The integrated volume. Original column name: "Volume"
         */
        VOLUME("Volume");

        private final String originalName;

        private ColumnName(String originalName) {
            this.originalName = originalName;
        }

        @Override
        public String toString() {
            return originalName;
        }
        
        public String originalName() {
            return originalName;
        }

        public static ColumnName fromString(String name) {
            switch (name) {
                case "BlobID":
                    return BLOBID;
                case "Compound Name":
                    return COMPOUND_NAME;
                case "Group Name":
                    return GROUP_NAME;
                case "Inclusion":
                    return INCLUSION;
                case "Internal Standard":
                    return INTERNAL_STANDARD;
                case "Retention I (min)":
                    return RETENTION_I;
                case "Retention II (sec)":
                    return RETENTION_II;
                case "Peak Value":
                    return PEAK_VALUE;
                case "Volume":
                    return VOLUME;
                default:
                    throw new IllegalArgumentException("Unsupported column name '" + name + "'");
            }
        }
    };

    public static CSVParser open(File file, String quotationCharacter) throws IOException {
        CSVFormat format = null;
        if (file.getName().endsWith(".txt") || file.getName().endsWith(".tsv")) {
            format = CSVFormat.TDF.withHeader();
        } else {
            format = CSVFormat.RFC4180.withHeader().withQuote(quotationCharacter.charAt(0)).withQuoteMode(QuoteMode.MINIMAL);
        }
        return CSVParser.parse(file, Charset.forName("UTF-8"), format);
    }

}
