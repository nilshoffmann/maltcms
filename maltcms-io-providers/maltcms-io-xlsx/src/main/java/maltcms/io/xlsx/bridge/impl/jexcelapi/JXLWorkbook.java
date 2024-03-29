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
package maltcms.io.xlsx.bridge.impl.jexcelapi;

import java.io.IOException;
import java.util.Locale;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import maltcms.io.xlsx.bridge.IInputStreamProvider;
import maltcms.io.xlsx.bridge.ISheet;
import maltcms.io.xlsx.bridge.IWorkbook;

/**
 * <p>JXLWorkbook class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class JXLWorkbook implements IWorkbook {

    private final jxl.Workbook workbook;

    /**
     * <p>Constructor for JXLWorkbook.</p>
     *
     * @param provider a {@link maltcms.io.xlsx.bridge.IInputStreamProvider} object.
     * @throws java.io.IOException if any.
     * @throws jxl.read.biff.BiffException if any.
     */
    public JXLWorkbook(IInputStreamProvider provider) throws IOException, BiffException {
        try {
            WorkbookSettings ws = new WorkbookSettings();
            ws.setLocale(Locale.US);
            workbook = jxl.Workbook.getWorkbook(provider.openStream(), ws);
        } finally {
            provider.closeStream();
        }
    }

    /** {@inheritDoc} */
    @Override
    public ISheet getSheet(String name) {
        Sheet sheet = workbook.getSheet(name);
        return new JXLSheet(sheet);
    }

    public Workbook getWorkbook() {
        return workbook;
    }
}
