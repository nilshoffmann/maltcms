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
package net.sf.maltcms.cv;

import cross.test.SetupLogging;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author Nils Hoffmann
 */
public class MaltcmsCvProviderTest {

    @Rule
    public SetupLogging sl = new SetupLogging();

    /**
     * Test of translate method, of class MaltcmsCvProvider.
     */
    @Test
    public void testTranslate() {
        MaltcmsCvProvider mcp = new MaltcmsCvProvider();
        Assert.assertEquals("modulation_time", mcp.translate("var.modulation_time"));
        Assert.assertEquals("scan_acquisition_time", mcp.translate("var.scan_acquisition_time"));
        Assert.assertEquals("first_column_time", mcp.translate("var.first_column_time"));
    }

    /**
     * Test of getName method, of class MaltcmsCvProvider.
     */
    @Test
    public void testGetName() {
        MaltcmsCvProvider mcp = new MaltcmsCvProvider();
        Assert.assertEquals("maltcms", mcp.getName());
    }

    /**
     * Test of getNamespace method, of class MaltcmsCvProvider.
     */
    @Test
    public void testGetNamespace() {
        MaltcmsCvProvider mcp = new MaltcmsCvProvider();
        Assert.assertEquals("var", mcp.getNamespace());
    }
}
