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
package maltcms.ui.wizard;

import java.util.Map;

import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPage;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class Main implements WizardPage.WizardResultProducer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main m = new Main();
        Wizard w = WizardPage.createWizard(new Class[]{
                    FileInputOutputPane.class, AnchorDefinitionPane.class,
                    PreprocessingPane.class, AlignmentPane.class,
                    VisualizationPane.class}, m);

        Map gatheredSettings = (Map) WizardDisplayer.showWizard(w);

    }

    public Object finish(Map arg0) throws WizardException {
        for (Object key : arg0.keySet()) {
            System.out.println(key + " = " + arg0.get(key));
        }
        return arg0;
    }

    public boolean cancel(Map arg0) {
        return true;
    }
}
