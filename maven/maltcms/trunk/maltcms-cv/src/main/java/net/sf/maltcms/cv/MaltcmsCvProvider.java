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
package net.sf.maltcms.cv;

import cross.exception.MappingNotAvailableException;
import cross.vocabulary.IControlledVocabularyProvider;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
@ServiceProvider(service = IControlledVocabularyProvider.class)
public class MaltcmsCvProvider implements IControlledVocabularyProvider {

    private PropertiesConfiguration pc;

    public MaltcmsCvProvider() {
        String homeDir = System.getProperty("maltcms.home");
        File cfg = new File(homeDir, "cfg/cv/cv_maltcms.properties");
        try {
            PropertiesConfiguration pc = new PropertiesConfiguration(cfg);
        } catch (ConfigurationException ex) {
            Logger.getLogger(MaltcmsCvProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String translate(String variable) throws MappingNotAvailableException {
        String resolvedVariableName = variable;
        if (!variable.startsWith(getNamespace() + ".")) {
            if (pc.containsKey(resolvedVariableName)) {
                log.warn("Returning plain mapping!");
                return pc.getString(resolvedVariableName);
            }
        }
        resolvedVariableName = getNamespace()+"."+resolvedVariableName;
        if(pc.containsKey(resolvedVariableName)) {
            log.info("Using fully qualified mapping!");
            return pc.getString(resolvedVariableName);
        }
        throw new MappingNotAvailableException("No mapping for " + variable);
    }

    @Override
    public String getName() {
        return pc.getFileName();
    }

    @Override
    public String getNamespace() {
        return "maltcms";
    }

    @Override
    public String getVersion() {
        return "1.2";
    }
}
