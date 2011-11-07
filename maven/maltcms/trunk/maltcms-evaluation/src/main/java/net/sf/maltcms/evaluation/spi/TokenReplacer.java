/*
 * $license$
 *
 * $Id$
 */
package net.sf.maltcms.evaluation.spi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import lombok.Cleanup;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Slf4j
@Data
public class TokenReplacer {

    private HashMap<String, String> tokenToValueMap = new HashMap<String, String>();
    
    public void replace(File inFile, File outFile) {
        try {
            @Cleanup
            FileInputStream fis = new FileInputStream(inFile);
            @Cleanup
            FileOutputStream fos = new FileOutputStream(outFile);
            replace(fis,fos,Charset.forName("UTF-8"));
        } catch (IOException e) {
            log.error("Caught exception while opening files for replace: ", e);
        }
    }

    public void replace(InputStream inputStream, OutputStream outputStream,
            Charset cs) {
        try {
            @Cleanup
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    inputStream,
                    cs));
            @Cleanup
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    outputStream, cs));
            String s = null;

            while ((s = br.readLine()) != null) {
                for (String key : tokenToValueMap.keySet()) {
                    String skey = "${"+key+"}";
                    if (s.contains(skey)) {
                        log.info("Replacing key {} in {} with {}", new Object[]{
                                    key, s, tokenToValueMap.get(key)});
                        s = s.replaceAll(skey, tokenToValueMap.get(key));
                    }
                    bw.write(s);
                    bw.newLine();
                }
            }
            bw.close();
            br.close();
        } catch (IOException ex) {
            log.error("Caught exception while replacing: ", ex);
        }
    }
}
