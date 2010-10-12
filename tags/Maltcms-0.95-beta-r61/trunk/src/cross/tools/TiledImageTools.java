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
 * $Id$
 */

package cross.tools;

import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.media.jai.TiledImage;

import cross.Logging;

/**
 * Utility Class providing access to reading and writing of TiledImages (java
 * advanced imaging jai).
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class TiledImageTools {

	public static TiledImage initializeImage(final TiledImage ti,
	        final String format) {
		final String prefix = String.valueOf(ti.hashCode());
		final String suffix = "png";
		File f = null;
		try {
			f = File.createTempFile(prefix, suffix);
			Logging.getLogger(TiledImageTools.class).info(
			        "Writing TiledImage to file: " + f.getCanonicalPath());
			TiledImageTools.saveImage(ti, f, format);
			TiledImageTools.updateImage(ti, f);
			return ti;
		} catch (final IOException ioex) {
			ioex.printStackTrace();
		}
		return ti;
	}

	public static TiledImage prepareImage(final int width, final int height,
	        final int tilesx, final int tilesy, final ColorModel cm) {
		final TiledImage ri = new TiledImage(0, 0, width, height, 0, 0, cm
		        .createCompatibleSampleModel(width, height), cm);
		return ri;
	}

	public static TiledImage prepareImage(final RenderedImage ri,
	        final int tilesx, final int tilesy) {
		return new TiledImage(ri, tilesx, tilesy);
	}

	public static void saveImage(final RenderedImage ti, final File f,
	        final String format) {
		if (!f.exists()) {
			f.getParentFile().mkdirs();
		}
		try {
			ImageIO.write(ti, format, f);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void saveImage(final RenderedImage ti, final String filename,
	        final String format) {
		final File f = new File(filename + "." + format);
		TiledImageTools.saveImage(ti, f, format);
	}

	public static void updateImage(final TiledImage ti, final File f) {
		try {
			TiledImageTools.saveImage(ti, f, "png");
			ti.set(ImageIO.read(f));
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void updateImage(final TiledImage ti, final String filename) {
		TiledImageTools.updateImage(ti, new File(filename));
	}

	public static TiledImage writeToTiledImage(final TiledImage target,
	        final TiledImage bi, final int tilesUntilFlush,
	        final String filename, final String format) {

		final TiledImage ti = target;
		if (target == null) {
			TiledImageTools.initializeImage(TiledImageTools
			        .prepareImage(bi.getWidth(), bi.getHeight(), 1000, 1000, bi
			                .getColorModel()), format);
		}

		int tilecounter = 0;
		for (int i = 0; i < ti.getMaxTileY(); i++) {
			for (int j = 0; j < ti.getMaxTileX(); j++) {
				final WritableRaster wr = ti.getWritableTile(i, j);
				final Raster r = bi.getTile(i, j);
				wr.setRect(r);
				if (tilecounter < tilesUntilFlush) {
					TiledImageTools.saveImage(ti, filename, format);
					TiledImageTools.updateImage(ti, filename);
				}
				tilecounter++;
			}
		}

		return ti;
	}

}
