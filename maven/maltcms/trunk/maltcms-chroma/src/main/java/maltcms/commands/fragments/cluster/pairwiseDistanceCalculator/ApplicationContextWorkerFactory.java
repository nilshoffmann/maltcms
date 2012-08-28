/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
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
