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
package maltcms.commands.fragments.cluster.pairwiseDistanceCalculator;

import lombok.Data;

import org.openide.util.lookup.ServiceProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 *
 * Use this if you are instantiating a workflow containing
 * PairwiseDistanceCalculator using any of the spring xml bean configuration
 * files.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
@ServiceProvider(service = AWorkerFactory.class)
public class ApplicationContextWorkerFactory extends AWorkerFactory implements ApplicationContextAware {

    /**
     *
     */
    private static final long serialVersionUID = 8253151151321756579L;
    private ApplicationContext context;
    private String beanId;

    @Override
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
