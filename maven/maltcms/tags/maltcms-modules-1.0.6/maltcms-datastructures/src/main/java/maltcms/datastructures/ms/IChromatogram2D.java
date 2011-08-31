/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
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
 * 
 * $Id: IChromatogram2D.java 115 2010-04-23 15:42:15Z nilshoffmann $
 */

package maltcms.datastructures.ms;

import java.awt.Point;

/**
 * Interface representing a 2-dimensional chromatogram.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public interface IChromatogram2D extends IChromatogram, IScan2DProvider {

	public Scan2D getScan2D(int globalScan, int localScan);

	public int getNumberOfModulations();

	public int getNumberOfScansPerModulation();

	public int getNumberOf2DScans();

	public double getModulationDuration();

	public String getSecondColumnScanAcquisitionTimeUnit();

	public Point getPointFor(int scan);

	public Point getPointFor(double scan_acquisition_time);

}