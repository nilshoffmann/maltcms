/* 
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.io;

import cross.IConfigurable;
import cross.ObjectFactory;
import cross.annotations.Configurable;
import cross.commands.fragments.AFragmentCommand;
import cross.tools.StringTools;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;

/**
 * Loads available implementations of {@link cross.commands.fragments.AFragmentCommand} 
 * from classpath resources using the {@link java.util.ServiceLoader}.
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class AFragmentCommandServiceLoader implements IConfigurable {

    @Configurable
    private List<String> fragmentCommands = Collections.emptyList();

    public static class ClassNameLexicalComparator implements
            Comparator<AFragmentCommand> {

        @Override
        public int compare(AFragmentCommand o1, AFragmentCommand o2) {
            return o1.getClass().getName().compareTo(o2.getClass().getName());
        }
    }

    /**
     * Returns the available implementations of {@link IFragmentCommand}. 
     * 
     * Elements are sorted according to lexical order on their classnames.
     *
     * @return
     */
    public List<AFragmentCommand> getAvailableCommands() {
        ServiceLoader<AFragmentCommand> sl = ServiceLoader
                .load(AFragmentCommand.class);
        HashSet<AFragmentCommand> s = new HashSet<AFragmentCommand>();
        for (AFragmentCommand ifc : sl) {
            s.add(ifc);
        }
        return createSortedListFromSet(s, new ClassNameLexicalComparator());
    }

    public List<AFragmentCommand> getAvailableUserCommands(ObjectFactory of) {
        HashSet<AFragmentCommand> s = new HashSet<AFragmentCommand>();
        for (String uc : fragmentCommands) {
            try {
                AFragmentCommand af = of
                        .instantiate(uc, AFragmentCommand.class);
                s.add(af);
            } catch (IllegalArgumentException iae) {
                log.warn(iae.getLocalizedMessage());
            }
        }
        return createSortedListFromSet(s, new ClassNameLexicalComparator());
    }

    public List<AFragmentCommand> createSortedListFromSet(
            Set<AFragmentCommand> s, Comparator<AFragmentCommand> comp) {
        ArrayList<AFragmentCommand> al = new ArrayList<AFragmentCommand>();
        al.addAll(s);
        Collections.sort(al, comp);
        return al;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * cross.IConfigurable#configure(org.apache.commons.configuration.Configuration
     * )
     */
    @Override
    public void configure(Configuration cfg) {
        fragmentCommands = StringTools.toStringList(cfg.getList(getClass()
                .getName()
                + ".fragmentCommands"));
    }
}
