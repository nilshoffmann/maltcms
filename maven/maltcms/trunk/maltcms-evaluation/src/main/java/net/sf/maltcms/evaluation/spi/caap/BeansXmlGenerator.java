/*
 * $license$
 *
 * $Id$
 */
package net.sf.maltcms.evaluation.spi.caap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import net.sf.maltcms.math.Combinatorics;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Slf4j
public class BeansXmlGenerator implements Iterable<File>, Iterator<File> {

    private int idx = 0;
    private final List<Object[]> choices;
    private Set<String> keys;
    private final File template;
    private final File outputDirectory;

    public BeansXmlGenerator(LinkedHashMap<String, List<?>> parameterMap,
            File template, File outputDirectory) {
        keys = parameterMap.keySet();
        this.choices = Combinatorics.getKPartiteChoices(Combinatorics.
                toObjectArray(parameterMap));
        this.template = template;
        this.outputDirectory = outputDirectory;
    }

    @Override
    public boolean hasNext() {
        if (idx < choices.size() - 1) {
            return true;
        }
        return false;
    }

    @Override
    public File next() {
        UUID instanceId = UUID.randomUUID();
        File instanceDirectory = new File(this.outputDirectory, instanceId.
                toString());
        instanceDirectory.mkdirs();
        try {
            PropertiesConfiguration pc = new PropertiesConfiguration(new File(
                    instanceDirectory, "parameters.properties"));
            Object[] choice = choices.get(idx++);
            int i = 0;
            for (String key : keys) {
                pc.setProperty(key, choice[i++].toString());
            }
            pc.save();
            File outputFile = new File(instanceDirectory,
                    UUID.randomUUID() + ".xml");
            try {
                FileUtils.copyInputStreamToFile(template.toURI().toURL().openStream(), outputFile);
                return outputFile;
            } catch (IOException ex) {
                log.error("Caught exception while trying to create "+outputFile.getAbsolutePath(),
                        ex);
            }
        } catch (ConfigurationException ex) {
            Logger.getLogger(BeansXmlGenerator.class.getName()).
                    log(Level.SEVERE, null, ex);
        }

        return null;
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
