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
import cross.tools.StringTools;
import cross.vocabulary.IControlledVocabularyProvider;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
    private Set<String> deprecatedVariables;

    public MaltcmsCvProvider() {
        String homeDir = System.getProperty("maltcms.home");
        URL file = null;
        if(homeDir!=null && !homeDir.isEmpty()) {
            try {
                file = new File(homeDir, "cfg/cv/cv_combined.properties").toURI().toURL();
                log.info("Using cv from file system at {}",file);
            } catch (MalformedURLException ex) {
               log.warn("Could not retrieve cv file from filesystem at "+file,ex);
            }   
        }
        if(file==null) {
            file = MaltcmsCvProvider.class.getResource("/cfg/cv/cv_combined.properties");
            log.info("Using cv from classpath at {}",file);
        }
        try {
            pc = new PropertiesConfiguration(file);
            log.info("Using cv.version={}",pc.getString("cv.version"));
            deprecatedVariables = new LinkedHashSet<String>();
            StringBuilder message = new StringBuilder();
            List<String> l = StringTools.toStringList(pc.getList("deprecated.variables", Collections.emptyList()));
            for (String s : l) {
                String key = checkDeprecation(s, pc, message);
                deprecatedVariables.add(key);
            }
            log.warn("{}",message);
        } catch (ConfigurationException ex) {
            log.error("Exception while configuring CvProvider:", ex);
        }
    }

    protected final String checkDeprecation(String s, PropertiesConfiguration pc, StringBuilder message) {
        String key = "";
        if (s.contains(":")) {
            String[] split = s.split(":");
            key = split[0];
            message.append("Variable name ").
                    append(pc.getString(key)).
                    append(" is deprecated. Please replace with ").
                    append(pc.getString(split[1])).
                    append("!");
        } else {
            key = s;
            message.append("Variable name ").append(pc.getString(key)).append(" is deprecated!");
        }
        if (pc.containsKey("deprecated." + key)) {
            message.append(" Reason: ").append(pc.getString("deprecated." + key));
        }
        message.append("\n");
        return key;
    }

    @Override
    public String translate(String variable) throws MappingNotAvailableException {
        String resolvedVariableName = variable;
        if(deprecatedVariables.contains(variable)) {
            StringBuilder message = new StringBuilder();
            checkDeprecation(variable, pc, message);
            log.warn("{}",message);
        }
        if (pc.containsKey(resolvedVariableName)) {
            return pc.getString(resolvedVariableName);
        }
        throw new MappingNotAvailableException("No mapping for " + variable);
    }

    @Override
    public String getName() {
        return "maltcms";
    }

    @Override
    public String getNamespace() {
        return "var";
    }

    @Override
    public String getVersion() {
        if(pc==null) {
            return "NA";
        }else{
            return pc.getString("cv.version");
        }
    }
}
