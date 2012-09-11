/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
