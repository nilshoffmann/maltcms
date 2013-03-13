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
package net.sf.maltcms.apps;

/**
 * Moves workflow output directory (--from) to another directory (--to), rewriting 
 * absolute URIs of workflow results. Input URIs are untouched unless input 
 * location is specified with --in.
 * @author Nils Hoffmann
 */
public class MoveOutput {
	
	public static void main(String[] args) {
		//TODO define cli parameters
		//-check for given workflow.xml file (--from)
		// resolve against user.dir
		//-check output directory, create if non-existant (--to)
		//copy directory containing workflow recursively to "--to" location
		//read workflow.xml in "--to" location and substitute absolute path with target path
		//if "--in" is given, substitute input locations with paths given by in (one common path 
		//prefix for all file fragments or n paths for n fragments)
		//save 
	}
	
}
