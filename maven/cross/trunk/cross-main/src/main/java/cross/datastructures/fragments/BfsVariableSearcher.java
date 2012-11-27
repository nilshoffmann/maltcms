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
package cross.datastructures.fragments;

import cross.Factory;
import cross.datastructures.tools.FragmentTools;
import cross.exception.ResourceNotAvailableException;
import cross.math.SetOperations;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Performs a breadth-first search for a given variable, using a
 * {@link IFileFragment} as root and exploring its source files.
 *
 * @author Nils Hoffmann
 */
@Slf4j
@Data
public class BfsVariableSearcher {

    /**
     * Returns a collection of {@link IFileFragment} instances that have the
     * given variableName as their child. <p> If no variable with that name is
     * found, an empty collection is returned. The method begins exploration by
     * checking the root for the given variableName child variable. On failure
     * to find it, the method descends into the source files of root and
     * continues to search for variableName in a breadth-first manner. If
     * multiple files contain variableName as their child, all of them are
     * returned. The method terminates either when the first match is found, or
     * when all ancestors of root have been explored.
     *
     * @param root the root from where to start searching for variableName
     * @param variableName the name of the variable to find
     * @return matches containing varName
     */
    public final Collection<IFileFragment> getClosestParent(IFileFragment root, String variableName) {
        return getClosestParent(root, variableName, Double.POSITIVE_INFINITY);
    }

    /**
     * Returns a collection of {@link IFileFragment} instances that have the
     * given variableName as their child. <p> If no variable with that name is
     * found, an empty collection is returned. The method begins exploration by
     * checking the root for the given variableName child variable. On failure
     * to find it, the method descends into the source files of root and
     * continues to search for variableName in a breadth-first manner. If
     * multiple files contain variableName as their child, all of them are
     * returned. The method terminates either when the first match is found, or
     * when all ancestors of root have been explored that are at most maxLevel
     * steps away from root, whichever happens first. To fully explore the
     * ancestors of root, use
     * <code>maxLevel=Double.POSITIVE_INFINITY</code>.
     *
     * @param root the root from where to start searching for variableName
     * @param variableName the name of the variable to find
     * @param maxLevel maximum search depth
     * @return matches containing varName
     */
    public final Collection<IFileFragment> getClosestParent(IFileFragment root, String variableName, double maxLevel) {
        //initial search level has distance 0 from root
        Level initial = new Level(0, Arrays.asList(root));
        //breadth first queue of levels, each levels contains file fragments which 
        //have the same distance to root
        Queue<Level> toExplore = new LinkedList<Level>(Arrays.asList(initial));
        //explore until empty
        while (!toExplore.isEmpty()) {
            //retrieve the oldest element in the queue
            Level l = toExplore.poll();
            if (l.getLevel() <= maxLevel) {
                Collection<IFileFragment> matches = explore(toExplore, l, variableName);
                if (!matches.isEmpty()) {
                    return matches;
                }
            } else {
                throw new ResourceNotAvailableException("Could not find variable " + variableName + " in any ancestor of " + root.getUri() + " at maximum level " + maxLevel);
            }
        }

        return Collections.emptyList();
    }

    private Collection<IFileFragment> explore(Queue<Level> toExplore, Level l, String varname) {
        Level next = new Level(l.getLevel() + 1, new LinkedList<IFileFragment>());
        log.info("Level {}", l.getLevel());
        for (IFileFragment f : l.getMembers()) {
            log.info("Checking file {} for variable {}", f, varname);
            //if getChild succeeds, the variables was on disk, otherwise ResourceNotAvailableException
            //will terminate the call
            if (f.hasChild(varname)) {
                log.info("Found as direct child!");
                l.getMatches().add(f);
            } else {
                IVariableFragment ivf = null;
                try {
                    ivf = new ImmutableVariableFragment2(f, varname);
                    Factory.getInstance().getDataSourceFactory().getDataSourceFor(f).readStructure(ivf);
                    //the variable was on disk
                    log.info("Found as persistent child!");
                    ((ImmutableVariableFragment2) ivf).setUseCachedList(Factory.getInstance().
                            getConfiguration().getBoolean(FileFragment.class.getName() + ".useCachedList",
                            false));
                    l.getMatches().add(f);
                    //keep child
                } catch (FileNotFoundException fnfe) {
                    //this usually means, that f has not been saved yet, so ignore
                    log.debug("File location {} does not exist!",f.getUri());
                    //try to explore f's source files
                    next.members.addAll(f.getSourceFiles());
                    f.removeChild(ivf);
                } catch (IOException ioex) {
                    //this is serious so pass it on
                    throw new RuntimeException(ioex);
                } catch (ResourceNotAvailableException rnae) {
                    //since we did not find the file immediately in f, try to explore f's source files
                    Set<IFileFragment> sourceFiles = SetOperations.union(
                            SetOperations.newSet(f.getSourceFiles()),
                            SetOperations.newSet(FragmentTools.getSourceFiles(f).values()));
                    next.members.addAll(sourceFiles);
                    f.removeChild(ivf);
                }
            }
        }
        //no matches found but further source files?
        if (l.matches.isEmpty() && !next.getMembers().isEmpty()) {
            toExplore.add(next);
        }
        return l.matches;
    }

    @Data
    public final class Level {

        private final int level;
        private final Collection<IFileFragment> members;
        private final Collection<IFileFragment> matches = new LinkedList<IFileFragment>();
    }
}
