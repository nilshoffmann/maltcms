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
package cross.io.misc;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;

/**
 * This example was written by Kai Runte and uses the Apache Commons Codec
 * Project: http://jakarta.apache.org/commons/codec/ to implement the base64
 * encoding.
 *
 * @author Kai Runte
 *
 * This file is used by the mzData io provider in Maltcms.
 */
public class Base64Util {

    public static List<Float> base64StringToFloatList(
            final String base64String, final boolean bigEndian)
            throws DecoderException {
        final Base64 base64 = new Base64();
        final byte[] encoded = base64String.getBytes();
        final byte[] raw = base64.decode(encoded);
        final List<Float> floatList = Base64Util.byteArrayToFloatList(raw,
                bigEndian);
        return floatList;
    }

    public static double[] byteArrayToDoubleArray(final byte[] raw,
            final boolean bigEndian, final int length) {
        final double[] d = new double[length];
        int i = 0;
        if (bigEndian) {
            for (int iii = 0; iii < raw.length; iii += 8) {
                long ieee754 = 0;
                ieee754 |= ((raw[iii]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii + 1]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii + 2]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii + 3]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii + 4]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii + 5]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii + 6]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii + 7]) & 0xff);
                final double aDouble = Double.longBitsToDouble(ieee754);
                d[i++] = aDouble;
            }
        } else {
            for (int iii = 0; iii < raw.length; iii += 8) {
                long ieee754 = 0;
                ieee754 |= ((raw[iii + 7]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii + 6]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii + 5]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii + 4]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii + 3]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii + 2]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii + 1]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii]) & 0xff);
                final double aDouble = Double.longBitsToDouble(ieee754);
                d[i++] = aDouble;
            }
        }
        return d;
    }

    public static float[] byteArrayToFloatArray(final byte[] raw,
            final boolean bigEndian, final int length) {
        final float[] f = new float[length];
        int i = 0;
        if (bigEndian) {
            for (int iii = 0; iii < raw.length; iii += 4) {
                int ieee754 = 0;
                ieee754 |= ((raw[iii]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii + 1]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii + 2]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii + 3]) & 0xff);
                final float aFloat = Float.intBitsToFloat(ieee754);
                f[i++] = aFloat;
            }
        } else {
            for (int iii = 0; iii < raw.length; iii += 4) {
                int ieee754 = 0;
                ieee754 |= ((raw[iii + 3]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii + 2]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii + 1]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii]) & 0xff);
                final float aFloat = Float.intBitsToFloat(ieee754);
                f[i++] = aFloat;
            }
        }
        return f;
    }

    public static List<Float> byteArrayToFloatList(final byte[] raw,
            final boolean bigEndian) {
        final List<Float> f = new ArrayList<Float>();
        if (bigEndian) {
            for (int iii = 0; iii < raw.length; iii += 4) {
                int ieee754 = 0;
                ieee754 |= ((raw[iii]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii + 1]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii + 2]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii + 3]) & 0xff);
                final float aFloat = Float.intBitsToFloat(ieee754);
                f.add(Float.valueOf(aFloat));
            }
        } else {
            for (int iii = 0; iii < raw.length; iii += 4) {
                int ieee754 = 0;
                ieee754 |= ((raw[iii + 3]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii + 2]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii + 1]) & 0xff);
                ieee754 <<= 8;
                ieee754 |= ((raw[iii]) & 0xff);
                final float aFloat = Float.intBitsToFloat(ieee754);
                f.add(Float.valueOf(aFloat));
            }
        }
        return f;
    }

    public static String floatListToBase64String(final List<Float> floatList,
            final boolean bigEndian) throws EncoderException {
        final byte[] raw = Base64Util
                .floatListToByteArray(floatList, bigEndian);
        final Base64 base64 = new Base64();
        final byte[] encoded = base64.encode(raw);
        return new String(encoded);
    }

    /**
     * Returns a byte array representing the float values in the IEEE 754
     * floating-point "single format" bit layout.
     *
     * @param floatList a list of float values
     * @param bigEndian
     * @return a byte array representing the float values in the IEEE 754
     * floating-point "single format" bit layout.
     */
    public static byte[] floatListToByteArray(final List<Float> floatList,
            final boolean bigEndian) {
        final int floatListSize = floatList.size();
        final byte[] raw = new byte[floatListSize * 4];
        int jjj = 0;
        if (bigEndian) {
            for (int iii = 0; iii < floatListSize; iii++) {
                final Float aFloat = floatList.get(iii);
                final int ieee754 = Float.floatToIntBits(aFloat.floatValue());
                raw[jjj] = (byte) ((ieee754 >> 24) & 0xff);
                raw[jjj + 1] = (byte) ((ieee754 >> 16) & 0xff);
                raw[jjj + 2] = (byte) ((ieee754 >> 8) & 0xff);
                raw[jjj + 3] = (byte) ((ieee754) & 0xff);
                jjj += 4;
            }
        } else {
            for (int iii = 0; iii < floatListSize; iii++) {
                final Float aFloat = floatList.get(iii);
                final int ieee754 = Float.floatToIntBits(aFloat.floatValue());
                raw[jjj] = (byte) ((ieee754) & 0xff);
                raw[jjj + 1] = (byte) ((ieee754 >> 8) & 0xff);
                raw[jjj + 2] = (byte) ((ieee754 >> 16) & 0xff);
                raw[jjj + 3] = (byte) ((ieee754 >> 24) & 0xff);
                jjj += 4;
            }
        }
        return raw;
    }
}
