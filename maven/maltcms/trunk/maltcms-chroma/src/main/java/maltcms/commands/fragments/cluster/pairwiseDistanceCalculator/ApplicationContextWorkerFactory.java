/*
 * $license$
 *
 * $Id$
 */
package maltcms.commands.fragments.cluster.pairwiseDistanceCalculator;

import lombok.Data;

import org.openide.util.lookup.ServiceProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 *
 * Use this if you are instantiating a workflow containing PairwiseDistanceCalculator
 * using any of the spring xml bean configuration files.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
@ServiceProvider(service=AWorkerFactory.class)
public class ApplicationContextWorkerFactory extends AWorkerFactory implements ApplicationContextAware {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8253151151321756579L;
	
    private ApplicationContext context;
    private String beanId;

    public PairwiseDistanceWorker create() {
        if (context != null) {
            return context.getBean(beanId, PairwiseDistanceWorker.class);
        }
        throw new NullPointerException("ApplicationContext is null!");
    }

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.context = ac;
    }
}
