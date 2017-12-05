/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2013, The authors of Maltcms. All rights reserved.
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
package maltcms.io.xml.mzML;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import lombok.Data;
import org.xml.sax.SAXException;

/**
 * Validates a given mzML file against the indexed or plain mzML 1.1.0 schema.
 *
 * @author Nils Hoffmann
 * 
 * @since 1.3.2
 */
public class MZMLValidator {

    /**
     * Validate the given file against the indexed mzML schema.
     *
     * @param mzML the file to validate
     * @return the validation result
     */
    public ValidationResult validateIndexedMzML(File mzML) {
        return validateMzML(mzML, this.getClass().getClassLoader().getResource("mzML1.1.1-idx.xsd"));
    }

    /**
     * Validate the given file against the plain mzML schema.
     *
     * @param mzML the file to validate
     * @return the validation result
     */
    public ValidationResult validateMzML(File mzML) {
        return validateMzML(mzML, this.getClass().getClassLoader().getResource("mzML1.1.0.xsd"));
    }

    /**
     * Validate the given file against the provided mzML schema location.
     *
     * @param mzML the file to validate
     * @param schemaLocation the schema location
     * @return the validation result
     */
    public ValidationResult validateMzML(File mzML, URL schemaLocation) {
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        Schema schema;
        try {
            schema = factory.newSchema(schemaLocation);
        } catch (SAXException e) {
            return new ValidationResult(e, mzML, schemaLocation);
        }
        Validator validator = schema.newValidator();
        Source source = new StreamSource(mzML);
        try {
            validator.validate(source);
        } catch (SAXException | IOException ex) {
            return new ValidationResult(ex, mzML, schemaLocation);
        }
        return new ValidationResult(null, mzML, schemaLocation);
    }

    /**
     * A Validation result.
     */
    @Data
    public class ValidationResult {

        private final Throwable exception;
        private final File sourceFile;
        private final URL schemaLocation;

        /**
         * Returns true if the <code>sourceFile</code> passed validation against
         * the schema at <code>schemaLocation</code>.
         *
         * @return true if the sourceFile is valid, false otherwise
         */
        public boolean isValid() {
            return exception == null;
        }
    }

}
