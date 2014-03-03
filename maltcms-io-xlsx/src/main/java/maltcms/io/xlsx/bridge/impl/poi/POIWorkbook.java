/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
package maltcms.io.xlsx.bridge.impl.poi;

import java.io.IOException;
import lombok.Data;
import maltcms.io.xlsx.bridge.IInputStreamProvider;
import maltcms.io.xlsx.bridge.ISheet;
import maltcms.io.xlsx.bridge.IWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author Nils Hoffmann
 */
@Data
public class POIWorkbook implements IWorkbook {

    private final Workbook workbook;

    public POIWorkbook(IInputStreamProvider provider) throws IOException, InvalidFormatException {
        try {
            workbook = WorkbookFactory.create(provider.openStream());
        } finally {
            provider.closeStream();
        }
    }

    @Override
    public ISheet getSheet(String name) {
        return new POISheet(workbook.getSheet(name));
    }
}
