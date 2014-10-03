
/**
 * *****************************************************************************
 * --------------------------------------------------------------------------- *
 * File: * @(#) ScanHeaderParser.java * Author: * Ning Zhang
 * nzhang@systemsbiology.org
 * ******************************************************************************
 * This software is provided ``AS IS'' and any express or implied * *
 * warranties, including, but not limited to, the implied warranties of * *
 * merchantability and fitness for a particular purpose, are disclaimed. * * In
 * no event shall the authors or the Institute for Systems Biology * * liable
 * for any direct, indirect, incidental, special, exemplary, or * *
 * consequential damages (including, but not limited to, procurement of * *
 * substitute goods or services; loss of use, data, or profits; or * * business
 * interruption) however caused and on any theory of liability, * * whether in
 * contract, strict liability, or tort (including negligence * * or otherwise)
 * arising in any way out of the use of this software, even * * if advised of
 * the possibility of such damage. * * *
 * *****************************************************************************
 *
 * @author hoffmann
 * 
 * @since 1.3.2
 */
package org.systemsbiology.jrap.staxnxt;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
public class ScanAndHeaderParser {

    public ScanHeader tmpScanHeader;
    public Scan tmpScan;

    FileInputStream fileIN = null;

    boolean isScan = false;

    /**
     * <p>Setter for the field <code>isScan</code>.</p>
     *
     * @param isScan a boolean.
     */
    public void setIsScan(boolean isScan) {
        this.isScan = isScan;
    }

    /**
     * <p>setFileInputStream.</p>
     *
     * @param in a {@link java.io.FileInputStream} object.
     */
    public void setFileInputStream(FileInputStream in) {
        this.fileIN = in;
    }

    /**
     * <p>getHeader.</p>
     *
     * @return a {@link org.systemsbiology.jrap.staxnxt.ScanHeader} object.
     */
    public ScanHeader getHeader() {
        return tmpScanHeader;
    }

    /**
     * <p>getScan.</p>
     *
     * @return a {@link org.systemsbiology.jrap.staxnxt.Scan} object.
     */
    public Scan getScan() {
        return tmpScan;
    }

    /**
     * <p>parseScanAndHeader.</p>
     */
    public void parseScanAndHeader() {
        XMLStreamReader xmlSR = null;
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            xmlSR = inputFactory.createXMLStreamReader(fileIN, "ISO-8859-1");

            parseScanAndHeader(xmlSR);

        } catch (XMLStreamException e) {
            String exception1 = e.getMessage();
            if (!exception1.equals("ScanHeaderEndFoundException")) {
                if (!exception1.equals("ScanEndFoundException")) {
                    Logger.getLogger(ScanAndHeaderParser.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        } finally {
            if (xmlSR != null) {
                try {
                    xmlSR.close();
                } catch (XMLStreamException e) {
                    Logger.getLogger(ScanAndHeaderParser.class.getName()).log(Level.SEVERE, null, e);
                }
            }
            if (fileIN != null) {
                try {
                    fileIN.close();
                } catch (IOException ex) {
                    Logger.getLogger(ScanAndHeaderParser.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * <p>parseScanAndHeader.</p>
     *
     * @param xmlSR a {@link javax.xml.stream.XMLStreamReader} object.
     * @throws javax.xml.stream.XMLStreamException if any.
     */
    public void parseScanAndHeader(XMLStreamReader xmlSR)
            throws XMLStreamException {
        boolean inPrecursorMZ = false;
        boolean inPeaks = false;
        String elementName = null;
        String attriName = null;
        String attriValue = null;

        StringBuffer precursorBuffer = null;
        StringBuffer peaksBuffer = null;

        while (xmlSR.hasNext()) {
            int event = xmlSR.next();
            if (event == xmlSR.START_ELEMENT) {
                elementName = xmlSR.getLocalName();
                if (elementName.equals("scan")) {
                    tmpScanHeader = new ScanHeader();
                    tmpScanHeader.setNum(getIntValue(xmlSR, "num"));
                    tmpScanHeader.setMsLevel(getIntValue(xmlSR, "msLevel"));
                    tmpScanHeader.setPeaksCount(getIntValue(xmlSR, "peaksCount"));
                    tmpScanHeader.setPolarity(getStringValue(xmlSR, "polarity"));
                    tmpScanHeader.setScanType(getStringValue(xmlSR, "scanType"));
                    tmpScanHeader.setCentroided(getIntValue(xmlSR, "centroided"));
                    tmpScanHeader.setDeisotoped(getIntValue(xmlSR, "deisotoped"));
                    tmpScanHeader.setChargeDeconvoluted(getIntValue(xmlSR, "chargeDeconvoluted"));
                    tmpScanHeader.setRetentionTime(getStringValue(xmlSR, "retentionTime"));
                    tmpScanHeader.setStartMz(getFloatValue(xmlSR, "startMz"));
                    tmpScanHeader.setEndMz(getFloatValue(xmlSR, "endMz"));
                    tmpScanHeader.setLowMz(getFloatValue(xmlSR, "lowMz"));
                    tmpScanHeader.setHighMz(getFloatValue(xmlSR, "highMz"));
                    tmpScanHeader.setBasePeakMz(getFloatValue(xmlSR, "basePeakMz"));
                    tmpScanHeader.setBasePeakIntensity(getFloatValue(xmlSR, "basePeakIntensity"));
                    tmpScanHeader.setTotIonCurrent(getFloatValue(xmlSR, "totIonCurrent"));
                    //for S(M)RM
                    tmpScanHeader.setFilterLine(getStringValue(xmlSR, "filterLine"));
                }
                if (elementName.equals("peaks")) {
                    tmpScanHeader.setPrecision(getIntValue(xmlSR, "precision"));
                    tmpScanHeader.setByteOrder(getStringValue(xmlSR, "byteOrder"));
                    tmpScanHeader.setContentType(getStringValue(xmlSR, "contentType"));
                    tmpScanHeader.setCompressionType(getStringValue(xmlSR, "compressionType"));
                    tmpScanHeader.setCompressedLen(getIntValue(xmlSR, "compressedLen"));

                    if (isScan) {
                        inPeaks = true;
                        peaksBuffer = new StringBuffer();
                        tmpScan = new Scan();
                        tmpScan.setHeader(tmpScanHeader);
                    } else {
                        throw new XMLStreamException("ScanHeaderEndFoundException");
                    }
                }

                if (elementName.equals("precursorMz")) {
                    tmpScanHeader.setPrecursorScanNum(getIntValue(xmlSR, "precursorScanNum"));
                    tmpScanHeader.setPrecursorCharge(getIntValue(xmlSR, "precursorCharge"));
                    tmpScanHeader.setCollisionEnergy(getFloatValue(xmlSR, "collisionEnergy"));
                    tmpScanHeader.setIonisationEnergy(getFloatValue(xmlSR, "ionisationEnergy"));
                    tmpScanHeader.setPrecursorIntensity(getFloatValue(xmlSR, "precursorIntensity"));

                    precursorBuffer = new StringBuffer();
                    inPrecursorMZ = true;
                }
            }
            if (event == xmlSR.CHARACTERS) {
                if (inPrecursorMZ) {
                    precursorBuffer.append(xmlSR.getText());
                }
                if (inPeaks) {
                    peaksBuffer.append(xmlSR.getText());
                }
            }
            if (event == xmlSR.END_ELEMENT) {
                elementName = xmlSR.getLocalName();
                if (elementName.equals("precursorMz")) {
                    tmpScanHeader.setPrecursorMz(Float.parseFloat(precursorBuffer.toString()));

                    precursorBuffer = null; // make available for garbage collection

                    inPrecursorMZ = false;
                }
                if (elementName.equals("peaks")) {

                    //get peaks, this time use ByteBuffer
                    getPeaks(peaksBuffer.toString());
                    inPeaks = false;
                    peaksBuffer = null;
                    throw new XMLStreamException("ScanEndFoundException");

                }
            }
        }
    }

    /**
     * <p>getStringValue.</p>
     *
     * @param xmlSR a {@link javax.xml.stream.XMLStreamReader} object.
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getStringValue(XMLStreamReader xmlSR, String name) {
        String value = "";
        try {
            if (xmlSR.getAttributeValue(null, name) == null) {
                value = "";
            } else {
                value = xmlSR.getAttributeValue(null, name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * <p>getIntValue.</p>
     *
     * @param xmlSR a {@link javax.xml.stream.XMLStreamReader} object.
     * @param name a {@link java.lang.String} object.
     * @return a int.
     */
    public int getIntValue(XMLStreamReader xmlSR, String name) {
        int value = -1;
        try {
            if (xmlSR.getAttributeValue(null, name) == null) {
                value = -1;
            } else {
                value = Integer.parseInt(xmlSR.getAttributeValue(null, name));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * <p>getFloatValue.</p>
     *
     * @param xmlSR a {@link javax.xml.stream.XMLStreamReader} object.
     * @param name a {@link java.lang.String} object.
     * @return a float.
     */
    public float getFloatValue(XMLStreamReader xmlSR, String name) {
        float value = -1f;
        try {
            if (xmlSR.getAttributeValue(null, name) == null) {
                value = -1f;
            } else {
                value = Float.parseFloat(xmlSR.getAttributeValue(null, name));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * <p>getPeaks.</p>
     *
     * @param peakData a {@link java.lang.String} object.
     */
    public void getPeaks(String peakData) {
        //support non-zlib
        byte[] peakArray = peakData.getBytes();
        byte[] outPeakArray = peakArray;
        int outpos = Base64.decode(peakArray, 0, peakArray.length, outPeakArray);

        double[][] massIntenList = null;
        int arrayLen = -1;
        ByteBuffer peakBuffer = null;
        //check if it's compressed
        byte[] result = null;
        if ((tmpScanHeader.getCompressionType()).equals("zlib")) {
            try {
                Inflater decompresser = new Inflater();
                decompresser.setInput(outPeakArray, 0, outpos);
                int unCompLen = (tmpScanHeader.getPeaksCount()) * (tmpScanHeader.getPrecision() / 4);
                result = new byte[unCompLen];
                decompresser.inflate(result);
                decompresser.end();

            } catch (DataFormatException e) {
                e.printStackTrace();
            }

            arrayLen = result.length / (tmpScanHeader.getPrecision() / 8) / 2;
            massIntenList = new double[2][arrayLen];
            peakBuffer = ByteBuffer.wrap(result);

        } else {
            arrayLen = outpos / (tmpScanHeader.getPrecision() / 8) / 2;
            massIntenList = new double[2][arrayLen];
            peakBuffer = ByteBuffer.wrap(outPeakArray, 0, outpos);
        }

        int i = 0;
        while (peakBuffer.hasRemaining()) {
            if (tmpScanHeader.getPrecision() == 32) {
                massIntenList[0][i] = (double) peakBuffer.getFloat();
                massIntenList[1][i] = (double) peakBuffer.getFloat();
            } else {
                massIntenList[0][i] = peakBuffer.getDouble();
                massIntenList[1][i] = peakBuffer.getDouble();
            }
            i++;
        }

        tmpScan.setMassIntensityList(massIntenList);
    }
}
