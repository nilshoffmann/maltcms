/*
 * $license$
 *
 * $Id$
 */
package net.sf.maltcms.evaluation.spi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import net.sf.maltcms.math.Combinatorics;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Slf4j
public class BeansXmlGenerator implements Iterable<File>, Iterator<File> {
    
    private int idx = 0;
    private final List<Object[]> choices;
    private Set<String> keys;
    private final URL template; 
    private final File outputDirectory;
   
    public BeansXmlGenerator(LinkedHashMap<String, List<?>> parameterMap, URL template, File outputDirectory) {
        keys = parameterMap.keySet();
        this.choices = Combinatorics.getKPartiteChoices(Combinatorics.toObjectArray(parameterMap));
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
        LinkedHashMap<String, String> bpparams = new LinkedHashMap<String, String>();
        Object[] choice = choices.get(idx++);
        int i = 0;
        for (String key : keys) {
            bpparams.put(key, choice[i++].toString());
        }
        TokenReplacer tr = new TokenReplacer();
        tr.setTokenToValueMap(bpparams);
        File outputFile = new File(outputDirectory,UUID.randomUUID()+".xml");
        try {
            FileOutputStream fos = new FileOutputStream(outputFile);
            tr.replace(template.openStream(), fos, Charset.forName("UTF-8"));
            return outputFile;
        } catch (IOException ex) {
            log.error("Caught exception while trying to create beans.xml:",ex);
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
