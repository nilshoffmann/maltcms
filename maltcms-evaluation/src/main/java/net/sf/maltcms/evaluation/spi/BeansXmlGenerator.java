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
package net.sf.maltcms.evaluation.spi;

import cross.datastructures.combinations.ChoiceFactory;
import cross.math.Combinatorics;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.LoggerFactory;

/**
 * <p>BeansXmlGenerator class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class BeansXmlGenerator implements Iterable<File>, Iterator<File> {

    private int idx = 0;
    private final List<Object[]> choices;
    private Set<String> keys;
    private final File template;
    private final File outputDirectory;
    private final HashMap<String, String> tokenMap;
    private static org.slf4j.Logger log = LoggerFactory.getLogger(BeansXmlGenerator.class);

    /**
     * <p>Constructor for BeansXmlGenerator.</p>
     *
     * @param parameterMap a {@link java.util.LinkedHashMap} object.
     * @param template a {@link java.io.File} object.
     * @param outputDirectory a {@link java.io.File} object.
     * @param tokenMap a {@link java.util.HashMap} object.
     */
    public BeansXmlGenerator(LinkedHashMap<String, List<?>> parameterMap,
            File template, File outputDirectory, HashMap<String, String> tokenMap) {
        keys = parameterMap.keySet();
        this.choices = ChoiceFactory.getKPartiteChoices(Combinatorics.
                toObjectArray(parameterMap));
        this.template = template;
        this.outputDirectory = outputDirectory;
        this.tokenMap = tokenMap;
    }

    /**
     * <p>size.</p>
     *
     * @return a int.
     */
    public int size() {
        return choices.size();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasNext() {
        return (idx < choices.size() - 1);
    }

    /** {@inheritDoc} */
    @Override
    public File next() {
        UUID instanceId = UUID.randomUUID();
        File instanceDirectory = new File(this.outputDirectory, instanceId.
                toString());
        instanceDirectory.mkdirs();
        try {
            File pcFile = new File(
                    instanceDirectory, instanceId.toString() + ".parameters");
            PropertiesConfiguration pc = new PropertiesConfiguration(pcFile);
            Object[] choice = choices.get(idx++);
            int i = 0;
            for (String key : keys) {
                pc.setProperty(key, choice[i++].toString());
            }
            pc.save();
            File outputFile = new File(instanceDirectory,
                    instanceId.toString() + ".xml");

            tokenMap.put("paramsLocation", pcFile.getAbsolutePath());
            try {
                writeStreamToFile(template.toURI().toURL().openStream(),
                        outputFile, tokenMap);
                return outputFile;
            } catch (IOException ex) {
                log.error("Caught exception while trying to create " + outputFile.
                        getAbsolutePath(),
                        ex);
            }
        } catch (ConfigurationException ex) {
            Logger.getLogger(BeansXmlGenerator.class.getName()).
                    log(Level.SEVERE, null, ex);
        }

        return null;
    }

    private void writeStreamToFile(InputStream istream, File outputFile,
            HashMap<String, String> tokenMap) {
        BufferedReader bis = new BufferedReader(new InputStreamReader(istream));
        try {
            BufferedWriter bos = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    outputFile)));
            String line = null;
            int state = -1;
            try {
                while ((line = bis.readLine()) != null) {
                    String tmp = line;
                    for (String key : tokenMap.keySet()) {
                        if (tmp.contains(key)) {
                            log.info("Replacing " + key + " with value " + tokenMap.get(key));
                            tmp = tmp.replaceAll("\\$\\{" + key + "\\}", tokenMap.get(key));
                        }
                    }
                    bos.write(tmp);
                    bos.newLine();
                }
                bis.close();
                bos.close();
            } catch (IOException ex) {
                Logger.getLogger(BeansXmlGenerator.class.getName()).
                        log(Level.SEVERE, null, ex);
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException ex) {
                        Logger.getLogger(BeansXmlGenerator.class.getName()).
                                log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BeansXmlGenerator.class.getName()).
                    log(Level.SEVERE, null, ex);
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ex) {
                    Logger.getLogger(BeansXmlGenerator.class.getName()).
                            log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<File> iterator() {
        return this;
    }
}
