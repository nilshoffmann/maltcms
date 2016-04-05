/**
 * *****************************************************************************
 * --------------------------------------------------------------------------- *
 * File: * @(#) MSXMLParser.java * Author: * Ning Zhang
 * nzhang@systemsbiology.org
 * ******************************************************************************
 * * * * This software is provided ``AS IS'' and any express or implied * *
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
 */
package org.systemsbiology.jrap.staxnxt;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import lombok.extern.slf4j.Slf4j;

/**
 * A generic utility class for reading an MSXML file in a random access fashion
 * and utilizing a stored scan index for fast reads.
 *
 * tholzman 200911xx -Inserting changes for compatibility with sequential scan
 * iterators
 *
 * To use this parser with sequential access instead of random access use this
 * constructor:
 *
 * new MSXMLParser(path,true)
 *
 * If you use "false" as the second argument, you will construct a regular jrap
 * parser, just as if you had used the "new MSXMLParser(path)" constructor. The
 * original constructor still works.
 *
 * To get the next scanHeader use
 *
 * ScanHeader scan = parser.nextHeader();
 *
 * Note the absence of the scan index as a parameter. This routine returns the
 * information from the next scan, sequentially in the data file.
 *
 * It would be a bad idea to intermix nextHeader with rapHeader calls. The idea
 * of the sequential parser is to avoid slow random access seeks. In the
 * sequential version the scan index is built up gradually as you "next" through
 * the file. When nextHeader returns null, the parsing is complete, and a call
 * to getOffsets() will return the completed map. Each returned ScanHeader
 * object will contain the correct file offset. The map from getOffsets() will
 * only contain the file offsets up to the current scan.
 *
 * One final note: any XML errors generated by this parser have been recast as
 * IOExceptions. This keeps the parser compatible with previous code.
 *
 */
@Slf4j
public final class MSXMLParser {

    /**
     * The file we are in charge of reading
     */
    protected String fileName = null;

    /**
     * The indexes
     */
    protected Map<Integer, Long> offsets;
    protected int maxScan;
    protected long chrogramIndex;

    protected boolean isXML = false;
    protected boolean isML = false;

    /* TAH Nov 2009 */
    int currentScanIndex;
    EndPatternStringIterator epsi = null;

    /**
     * <p>Setter for the field <code>epsi</code>.</p>
     *
     * @param e a {@link org.systemsbiology.jrap.staxnxt.EndPatternStringIterator} object.
     */
    public void setEpsi(EndPatternStringIterator e) {
        this.epsi = e;
    }

    /**
     * <p>Getter for the field <code>epsi</code>.</p>
     *
     * @return a {@link org.systemsbiology.jrap.staxnxt.EndPatternStringIterator} object.
     */
    public EndPatternStringIterator getEpsi() {
        return epsi;
    }

    /**
     * <p>isMzXML.</p>
     *
     * @param fn a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean isMzXML(String fn) {
        //dhmay 20100223, changing this check to a case-insensitive check
        return fn.toLowerCase().indexOf("mzxml") != -1;
    }

    private void commonInits(String fileName) {
        if (isMzXML(fileName)) {
            isXML = true;
        } else {
            isML = true;
        }
        this.fileName = fileName;
    }

    private void sequentialInits() throws IOException {
        String leftPat = "<msRun", rightPat = ">", attr = "scanCount";
        String nextLeftPat = "<scan", nextRightPat = "</peaks>";
        if (isML) {
            leftPat = "<spectrumList";
            rightPat = ">";
            attr = "count";
            nextLeftPat = "<spectrum";
            nextRightPat = "</spectrum>";
        }
        setEpsi(new EndPatternStringIterator(leftPat, rightPat, fileName));

        XMLStreamReader xmlSR = epsi.xmlsrNext();
        try {
            xmlSR.next();
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            if (xmlSR != null) {
                try {
                    xmlSR.close();
                } catch (XMLStreamException ex) {
                    Logger.getLogger(MSXMLParser.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        maxScan = Integer.parseInt(xmlSR.getAttributeValue(null, attr));
        offsets = new HashMap<>();
        getEpsi().setLeftPatStr(nextLeftPat);
        getEpsi().setRightPatStr(nextRightPat);
        currentScanIndex = 0;
    }

    private void randomInits() {
        //using IndexParser get indexes
        IndexParser indexParser = new IndexParser(fileName);
        indexParser.parseIndexes();
        offsets = indexParser.getOffsetMap();
        maxScan = indexParser.getMaxScan();
        chrogramIndex = indexParser.getChrogramIndex();
    }

    /**
     * <p>Constructor for MSXMLParser.</p>
     *
     * @param fn a {@link java.lang.String} object.
     * @param isSequential a boolean.
     * @throws java.io.IOException if any.
     */
    public MSXMLParser(String fn, boolean isSequential) throws IOException {
        commonInits(fn);
        if (isSequential) {
            sequentialInits();
        } else {
            randomInits();
        }
    }

    /**
     * <p>Constructor for MSXMLParser.</p>
     *
     * @param fileName a {@link java.lang.String} object.
     */
    public MSXMLParser(String fileName) {
        commonInits(fileName);
        randomInits();
    }

    /* end TAH */
    /**
     * this gives back the file header (info before scan)
     *
     * @return the file header info (MZXMLFileInfo)
     */
    public MZXMLFileInfo rapFileHeader() {
        FileHeaderParser fileParser = new FileHeaderParser(fileName);
        fileParser.parseFileHeader();
        return (fileParser.getInfo());
    }

    /**
     * <p>rapHeader.</p>
     *
     * @return a scan header object without peaks information. dhmay changing
     * 20091021 to set the scanOffset on the returned scanHeader. This was
     * earlier behavior that was removed by Ning. Replacing, because it
     * increases efficiency quite a bit for calling code.
     * @param scanNumber a int.
     */
    public ScanHeader rapHeader(int scanNumber) {
        FileInputStream fileIN = null;
        long scanOffset = -1;
        try {
            fileIN = new FileInputStream(fileName);
            scanOffset = getScanOffset(scanNumber);
            if (scanOffset == -1) {
                closeFile(fileIN);
                return null;
            }

            fileIN.skip(scanOffset);
            ScanHeader scanHeader = null;
            if (isXML) {
                ScanAndHeaderParser headerParser = new ScanAndHeaderParser();
                headerParser.setIsScan(false);
                headerParser.setFileInputStream(fileIN);
                headerParser.parseScanAndHeader();

                closeFile(fileIN);
                scanHeader = headerParser.getHeader();
            } else {
                MLScanAndHeaderParser headerParser = new MLScanAndHeaderParser();
                headerParser.setIsScan(false);
                headerParser.setFileInputStream(fileIN);
                headerParser.parseMLScanAndHeader();
                closeFile(fileIN);
                scanHeader = headerParser.getHeader();
            }
            scanHeader.setScanOffset(scanOffset);
            return scanHeader;
        } catch (Exception e) {
            log.info("File exception:" + e);
            log.warn(e.getLocalizedMessage());
        } finally {
            closeFile(fileIN);
        }
        return null;
    }

    /* TAH Nov 2009 */
    /**
     * <p>nextHeader.</p>
     *
     * @return a {@link org.systemsbiology.jrap.staxnxt.ScanHeader} object.
     */
    public ScanHeader nextHeader() {
        ScanHeader scanHeader = null;
        StringBuilder curScanInfo = epsi.next();
        if (curScanInfo == null || curScanInfo.length() == 0 || curScanInfo.charAt(0) != '<') {
            return null;
        }
        currentScanIndex++;
        if (isXML) {
            ScanAndHeaderParser headerParser = new ScanAndHeaderParser();
            headerParser.setIsScan(false);
            try {
                headerParser.parseScanAndHeader(epsi.xmlsrCur());
            } catch (Exception e) {
            };
            scanHeader = headerParser.getHeader();
        } else {
            MLScanAndHeaderParser headerParser = new MLScanAndHeaderParser();
            headerParser.setIsScan(false);
            try {
                headerParser.parseMLScanAndHeader(epsi.xmlsrCur());
            } catch (Exception e) {
            };
            scanHeader = headerParser.getHeader();
        }
        offsets.put(scanHeader.getNum(), epsi.getFilePos());
        scanHeader.setScanOffset(epsi.getFilePos());
        return scanHeader;
    }

    /**
     * <p>Getter for the field <code>offsets</code>.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<Integer, Long> getOffsets() {
        return offsets;
    }


    /* end TAH      */
    private void closeFile(FileInputStream fileIN) {
        if (fileIN != null) {
            try {
                fileIN.close();
            } catch (IOException e) {
                log.warn(e.getLocalizedMessage());
            }
        }
    }

    /**
     * Read a particular scan from a MSXML file and return a generic Scan object
     * with it's data. Note: scanNumbers are 1-based, so scanNumber must be at
     * least 1 and be not greater than getScanCount() + 1
     *
     * @return a scan object. It has all the infomation in a scanheader object
     * and also peaks information that doesn't included in scanHeader object.
     * @param scanNumber a int.
     */
    public Scan rap(int scanNumber) {
        FileInputStream fileIN = null;
        try {
            fileIN = new FileInputStream(fileName);
            long scanOffset = getScanOffset(scanNumber);
            if (scanOffset == -1) {
                closeFile(fileIN);
                return null;
            }

            fileIN.skip(scanOffset);

            if (isXML) {
                ScanAndHeaderParser scanParser = new ScanAndHeaderParser();
                scanParser.setIsScan(true);
                scanParser.setFileInputStream(fileIN);
                scanParser.parseScanAndHeader();
                return (scanParser.getScan());
            } else {
                MLScanAndHeaderParser scanParser = new MLScanAndHeaderParser();
                scanParser.setIsScan(true);
                scanParser.setFileInputStream(fileIN);
                scanParser.parseMLScanAndHeader();
                return (scanParser.getScan());
            }
        } catch (Exception e) {
            log.info("File exception:" + e);
            log.warn(e.getLocalizedMessage());
        } finally {
            closeFile(fileIN);
        }
        return null;
    }

    /**
     * Get the total number of scans in the mzXMLfile handled by this parser.
     *
     * @return The number of scans.
     */
    public int getScanCount() /* TAH Nov 2009 */ {
        if (epsi != null) { //sequential scan, index scan hasn't been run
            return maxScan;
        } else {
            return offsets.size();
        }
    }         /* end TAH */


    /**
     * <p>getMaxScanNumber.</p>
     *
     * @return a int.
     */
    public int getMaxScanNumber() {
        return maxScan;
    }

    /**
     * get scan offset, scan number is 1 based.
     *
     * @param scanNumber a int.
     * @return a long.
     */
    public long getScanOffset(int scanNumber) {
        if (scanNumber > 0 && offsets.containsKey(scanNumber)) {
            return ((offsets.get(scanNumber)));
        } else {
            return (-1);
        }
    }
}