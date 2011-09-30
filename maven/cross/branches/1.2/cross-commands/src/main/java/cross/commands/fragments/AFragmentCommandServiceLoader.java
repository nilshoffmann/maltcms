/**
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 *
 * This file is part of Cross/Maltcms.
 *
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * 
 *
 * $Id$
 */
package cross.commands.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Retrieves and loads AFragmentCommand instances from the class path.
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 */
@Slf4j
@ServiceProvider(service = IFragmentCommandServiceLoader.class)
public class AFragmentCommandServiceLoader implements
        IFragmentCommandServiceLoader {

    public static class ClassNameLexicalComparator implements
            Comparator<AFragmentCommand> {

        @Override
        public int compare(AFragmentCommand o1, AFragmentCommand o2) {
            return o1.getClass().getName().compareTo(o2.getClass().getName());
        }
    }

    /**
     * Returns the available implementations of @see{IFragmentCommand}. Elements
     * are sorted according to lexical order on their classnames.
     * 
     * @return
     */
    @Override
    public List<AFragmentCommand> getAvailableCommands() {
        HashSet<AFragmentCommand> s = new HashSet<AFragmentCommand>();
        s.addAll(Lookup.getDefault().lookupAll(AFragmentCommand.class));
        return createSortedListFromSet(s, new ClassNameLexicalComparator());
    }

    public List<AFragmentCommand> createSortedListFromSet(
            Set<AFragmentCommand> s, Comparator<AFragmentCommand> comp) {
        ArrayList<AFragmentCommand> al = new ArrayList<AFragmentCommand>();
        al.addAll(s);
        Collections.sort(al, comp);
        return al;
    }
}