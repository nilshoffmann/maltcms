package maltcms.io.mztab;

import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.FragmentTools;
import cross.exception.ConstraintViolationException;
import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.jmztab.model.MZTabDescription;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.jmztab.model.Metadata;
import uk.ac.ebi.pride.jmztab.model.MsRun;
import uk.ac.ebi.pride.jmztab.model.Software;

/**
 * <p>MzTabExporterWorker class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public record MzTabExporterWorker(List<URI> inputFileFragments, URI outputFileFragment, String mzTabId, List<String> softwareSettings) implements Callable<URI>, Serializable {
            
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(MzTabExporterWorker.class);

    /** {@inheritDoc} */
    @Override
    public URI call() throws Exception {
        MZTabDescription descr = new MZTabDescription("1.0", MZTabDescription.Mode.Complete, MZTabDescription.Type.Quantification);
        descr.setId(mzTabId);
        Metadata m = new Metadata(descr);
        Software s = new Software(m.getSoftwareMap().size());
        for (String setting : softwareSettings) {
            s.addSetting(setting);
        }
        m.addSoftware(s);
        int i = 0;
        for (URI uri : inputFileFragments) {
            FileFragment f = new FileFragment(uri);
            MsRun run = new MsRun(i);
            List<IFileFragment> ancestors = FragmentTools.getDeepestAncestor(f);
            if (ancestors.isEmpty()) {
                throw new ConstraintViolationException("Ancestors must not be empty!");
            }
            run.setLocation(ancestors.get(0).getUri().toURL());
            m.addAssayMsRun(i, run);

            i++;
        }
        MZTabFile mtf = new MZTabFile(m);

        return null;
    }

}
