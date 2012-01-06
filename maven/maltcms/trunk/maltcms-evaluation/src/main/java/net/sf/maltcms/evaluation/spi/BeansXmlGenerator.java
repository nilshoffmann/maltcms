/*
 * $license$
 *
 * $Id$
 */
package net.sf.maltcms.evaluation.spi;

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
import net.sf.maltcms.math.Combinatorics;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class BeansXmlGenerator implements Iterable<File>, Iterator<File> {

    private int idx = 0;
    private final List<Object[]> choices;
    private Set<String> keys;
    private final File template;
    private final File outputDirectory;
    private final HashMap<String, String> tokenMap;
    
    private static org.slf4j.Logger log = LoggerFactory.getLogger(BeansXmlGenerator.class);

    public BeansXmlGenerator(LinkedHashMap<String, List<?>> parameterMap,
            File template, File outputDirectory, HashMap<String, String> tokenMap) {
        keys = parameterMap.keySet();
        this.choices = Combinatorics.getKPartiteChoices(Combinatorics.
                toObjectArray(parameterMap));
        this.template = template;
        this.outputDirectory = outputDirectory;
        this.tokenMap = tokenMap;
    }

    public int size() {
        return choices.size();
    }

    @Override
    public boolean hasNext() {
        return (idx < choices.size() - 1);
    }

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
                        outputFile,tokenMap);
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
                    for(String key:tokenMap.keySet()) {
                        if(tmp.contains(key)) {
                            System.out.println("Replacing "+key+" with value "+tokenMap.get(key));
                            tmp = tmp.replaceAll("\\$\\{"+key+"\\}", tokenMap.get(key));
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

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<File> iterator() {
        return this;
    }
}
