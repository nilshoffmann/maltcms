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
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 *
 *
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
     * Returns the available implementations of
     *
     * @see{IFragmentCommand}. Elements are sorted according to lexical order on
     * their classnames.
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
