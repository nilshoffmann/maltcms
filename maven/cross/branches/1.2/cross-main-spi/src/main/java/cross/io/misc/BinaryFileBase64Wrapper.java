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

package cross.io.misc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Provides static utility methods for base64 encoding/decoding, checksum
 * calculation, gzipping and unzipping.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class BinaryFileBase64Wrapper {

	/**
	 * Decode base64 encoded file in to file out.
	 * 
	 * @param in
	 * @param out
	 */
	public static void base64Decode(final File in, final File out) {
		Base64.decodeFileToFile(in.getAbsolutePath(), out.getAbsolutePath());
		// printCompressionRatio(in, out);
	}

	/**
	 * Encode file in to file out using base64 encoding.
	 * 
	 * @param in
	 * @param out
	 */
	public static void base64Encode(final File in, final File out) {
		Base64.encodeFileToFile(in.getAbsolutePath(), out.getAbsolutePath());
		// printCompressionRatio(in, out);
	}

	/**
	 * Calculates a checksum on File.
	 * 
	 * @param a
	 * @param c
	 *            , if null use CRC32 as default checksum algorithm
	 * @return
	 * @throws IOException
	 */
	public static long calcChecksum(final File a, final Checksum c)
	        throws IOException {
		final FileInputStream fis = new FileInputStream(a);
		final CheckedInputStream cis = new CheckedInputStream(fis,
		        (c == null) ? new CRC32() : c);
		final BufferedInputStream bis = new BufferedInputStream(cis);
		while (bis.read() != -1) {
		}
		return cis.getChecksum().getValue();
	}

	// public static void gunzip(File in, File out) throws
	// FileNotFoundException, IOException {
	// GZIPInputStream gis = new GZIPInputStream(new FileInputStream(out));
	// BufferedOutputStream bos = new BufferedOutputStream(new
	// FileOutputStream(in));
	// while(gis.available()!=-1) {
	// bos.write(gis.read());
	// }
	// bos.flush();
	// bos.close();
	// gis.close();
	// printCompressionRatio(in, out);
	// }

	/**
	 * Applies gunzip on in file and writes decompressed result to file out.
	 * 
	 * @param in
	 * @param out
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void gunzip(final File in, final File out)
	        throws FileNotFoundException, IOException {
		System.out.println("Unzipping to " + out);
		final GZIPInputStream gzi = new GZIPInputStream(new FileInputStream(in));
		final BufferedOutputStream bos = new BufferedOutputStream(
		        new FileOutputStream(out));
		int b;
		final byte[] data = new byte[1024];
		while ((b = gzi.read(data, 0, data.length)) != -1) {
			bos.write(data, 0, b);
		}
		bos.flush();
		bos.close();
		gzi.close();
	}

	public static void gzip(final File in, final File out)
	        throws FileNotFoundException, IOException {
		final GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(
		        out));
		final BufferedInputStream bis = new BufferedInputStream(
		        new FileInputStream(in));
		int b;
		final byte[] data = new byte[1024];
		while ((b = bis.read(data, 0, data.length)) != -1) {
			gos.write(data, 0, b);
		}
		bis.close();
		gos.flush();
		gos.close();
		// printCompressionRatio(in, out);
	}

	public static void main(final String[] args) {
		// if (args[0].equals("-d")) {
		// File input = new File(args[1]);
		// String prefix = removeB64FileExtension(input.getName());
		// File output = new File(input.getParentFile(), prefix);
		// base64Decode(input, output);
		// File unzipped = new File(input.getParentFile(),
		// removeGZFileExtension(output.getName()));
		// try {
		// gunzip(output, unzipped);
		// } catch (IOException e) {
		// System.err.println(e.getLocalizedMessage());
		// }
		// } else if (args[0].equals("-e")) {
		// File input = new File(args[1]);
		// try {
		// File gzipf = new File(input.getParentFile(), input.getName()
		// + ".gz");
		// gzip(input,gzipf);
		// File output = new File(gzipf.getParentFile(), gzipf.getName()
		// + ".b64");
		// base64Encode(gzipf, output);
		// } catch (FileNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//	    
		// } else {
		// System.err.println("Unsupported input parameter " + args[0]);
		// }
		for (final String file : args) {
			File b64enc, input;
			try {
				input = new File(file);
				final File tmpdirin = new File("/tmp/in");
				final File tmpdirout = new File("/tmp/out");
				if (!tmpdirin.exists()) {
					tmpdirin.mkdirs();
				}
				if (!tmpdirout.exists()) {
					tmpdirout.mkdirs();
				}
				final File gzipped = new File(tmpdirin, input.getName() + ".gz");
				BinaryFileBase64Wrapper.gzip(input, gzipped);
				b64enc = new File(tmpdirin, gzipped.getName() + ".b64");
				BinaryFileBase64Wrapper.base64Encode(gzipped, b64enc);

				final File b64dec = new File(tmpdirout, input.getName() + ".gz");
				BinaryFileBase64Wrapper.base64Decode(b64enc, b64dec);
				final File gunzipped = new File(tmpdirout, input.getName());
				BinaryFileBase64Wrapper.gunzip(b64dec, gunzipped);
				if (BinaryFileBase64Wrapper.verifyChecksum(input, gunzipped)) {
					System.out
					        .println("Conversion successful for file " + file);
				} else {
					System.out.println("Verification of checksums for " + file
					        + " failed!");
				}
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	/**
	 * Print the compression ration between uncompressed and compressed file.
	 * 
	 * @param unc
	 * @param comp
	 */
	public static void printCompressionRatio(final File unc, final File comp) {
		double unclen = unc.length();
		double complen = comp.length();
		unclen /= (Math.pow(2.0d, 20.0d));
		complen /= (Math.pow(2.0d, 20.0d));
		System.out.printf("%s : %1.2f %s\n", "Size of input file  " + unc,
		        unclen, " MBytes");
		System.out.printf("%s : %1.2f %s\n", "Size of output file  " + comp,
		        complen, " MBytes");
		final float compressionRatio = (float) complen / (float) unclen;
		System.out.printf("%s = %1.2f\n", "Compression ratio (output/input)",
		        compressionRatio);
	}

	/**
	 * Remove file extension ".b64" if existent.
	 * 
	 * @param filename
	 * @return
	 */
	public static String removeB64FileExtension(final String filename) {
		String s = filename;
		if (s.endsWith(".b64")) {
			s = s.replace(".b64", "");
		}
		System.out.println("Without Base64 extension: " + s);
		return s;
	}

	/**
	 * Remove file extension ".gz" if existent.
	 * 
	 * @param filename
	 * @return
	 */
	public static String removeGZFileExtension(final String filename) {
		String s = filename;
		if (s.endsWith(".gz")) {
			s = s.replace(".gz", "");
		}
		System.out.println("Without GZ extension " + s);
		return s;
	}

	/**
	 * Calculates checksums for both files and compares, whether they match.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean verifyChecksum(final File a, final File b) {
		try {
			final long csa = BinaryFileBase64Wrapper.calcChecksum(a, null);
			final long csb = BinaryFileBase64Wrapper.calcChecksum(b, null);
			if (csa == csb) {
				System.out.println("Checksums match!");
				return true;
			}
			System.err.println("Checksum mismatch!");
			return false;
		} catch (final IOException e) {
			return false;
		}

	}

	/**
	 * Write bytes from byte array to file using a buffer.
	 * 
	 * @param bytes
	 * @param out
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void writeBytes(final byte[] bytes, final File out)
	        throws FileNotFoundException, IOException {
		final BufferedOutputStream bos = new BufferedOutputStream(
		        new FileOutputStream(out));
		final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		int b;
		final byte[] data = new byte[1024];
		while ((b = bais.read(data, 0, data.length)) != -1) {
			bos.write(data, 0, b);
		}
		bais.close();
		bos.flush();
		bos.close();
	}

}
