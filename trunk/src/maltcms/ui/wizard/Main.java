/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id$
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
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		Main m = new Main();
		Wizard w = WizardPage.createWizard(new Class[] {
		        FileInputOutputPane.class, AnchorDefinitionPane.class,
		        PreprocessingPane.class, AlignmentPane.class,
		        VisualizationPane.class }, m);

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
