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
package maltcms.io.xlsx.bridge;

import java.io.IOException;
import java.net.URI;
import jxl.read.biff.BiffException;
import lombok.extern.slf4j.Slf4j;
import maltcms.io.xlsx.bridge.impl.jexcelapi.JXLWorkbook;
import maltcms.io.xlsx.bridge.impl.poi.POIWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class WorkbookBridge {
	
	public enum IMPL{POI,JEXCELAPI};
	
	/**
	 * May return null if no supporting api could read the corresponding 
	 * spreadsheet or if any other errors occur.
	 * 
	 * @param uri
	 * @param impl
	 * @param iisp
	 * @return 
	 */
	public IWorkbook getWorkbook(URI uri, IMPL impl, IInputStreamProvider iisp) {
		switch(impl) {
			case POI:
				try {
					return new POIWorkbook(iisp);
				} catch (IOException ex) {
					log.warn("IOException: ",ex);
				} catch (InvalidFormatException ex) {
					log.warn("InvalidFormatException: ",ex);
				} catch (java.lang.NoClassDefFoundError ex) {
					log.warn("Could not find implementation for POI: ",ex);
				}
				return null;
			case JEXCELAPI:
				try {
					return new JXLWorkbook(iisp);
				} catch (IOException ex) {
					log.warn("IOException: ",ex);
				} catch (BiffException ex) {
					log.warn("BiffException: ",ex);
				} catch (java.lang.NoClassDefFoundError ex) {
					log.warn("Could not find implementation for JXL: ",ex);
				}
				return null;
			default:
				throw new IllegalArgumentException("Unknown state: "+impl);
		}
	}
	
}
