/**
 * *****************************************************************************
 * --------------------------------------------------------------------------- *
 * File: * @(#) ScanHeader.java * Author: * Mathijs Vogelzang m_v@dds.nl
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
 * ******************************************************************************
 *
 * ChangeLog
 *
 * 10-05-2004 Added this header
 *
 * Created on Jan 12, 2004
 *
 * add support for mzXML_schema_3.0 and S(M)RM by Ning Zhang
 *
 *****************************************************************************
 */
package org.systemsbiology.jrap.staxnxt;

import java.io.Serializable;

/**
 * ScanHeader is a class that contains all information associated with a Scan,
 * except for the actual peakList. The separation between the peaklist and the
 * other information was made because parsing the peaklist costs a lot of time,
 * and in this way, programs can parse headers separately, and not parse the
 * peaklist when it's not needed.
 *
 * dhmay: rt and retentionTime are completely separate fields, which is horribly
 * confusing. Probably getRetentionTime() should form a String around rt, and
 * getDoubleRetentionTime() should just be a cover for getRT(), if both need to
 * exist. Noting this on 2009/03/10 but not touching it, in case there are
 * unknown dependencies on this separation.
 *
 * @author M. Vogelzang
 * 
 */
public class ScanHeader implements Serializable {
//    class Members and Defaults
    //

    /**
     * Scan Number
     */
    protected int num = -1;

    /**
     * MS Scan Level
     */
    protected int msLevel = -1;

    /**
     * Number of peaks in scan
     */
    protected int peaksCount = -1;

    /**
     * TODO: Describe
     */
    protected String polarity = null;

    /**
     * TODO: Describe
     */
    protected String scanType = null;

    /**
     * TODO: Describe
     */
    protected int centroided = -1;

    /**
     * TODO: Describe
     */
    protected int deisotoped = -1;

    /**
     * TODO: Describe
     */
    protected int chargeDeconvoluted = -1;

    /**
     * TODO: Describe
     */
    protected String retentionTime = null;

    /**
     * for mzML
     */
    protected double rt = -1;

    /**
     * TODO: Describe
     */
    protected float startMz = -1;

    /**
     * TODO: Describe
     */
    protected float endMz = -1;

    /**
     * TODO: Describe
     */
    protected float lowMz = -1;

    /**
     * TODO: Describe
     */
    protected float highMz = -1;

    /**
     * TODO: Describe
     */
    protected float basePeakMz = -1;

    /**
     * TODO: Describe
     */
    protected float basePeakIntensity = -1;

    /**
     * TODO: Describe
     */
    protected float totIonCurrent = -1;

    /**
     * TODO: Describe
     */
    protected float precursorMz = -1;

    /**
     * TODO: Describe
     */
    protected int precursorScanNum = -1;

    /**
     * TODO: Describe
     */
    protected int precursorCharge = -1;

    protected float precursorIntensity = -1f;

    /**
     * TODO: Describe
     */
    protected float collisionEnergy = -1;

    /**
     * TODO: Describe
     */
    protected float ionisationEnergy = -1;

    /**
     * TODO: Describe
     */
    protected int precision = -1;

    /**
     * for S(M)RM
     */
    protected String filterLine = null;

    /**
     * Peaks attribute for mzXML_3.0
     */
    protected String byteOrder = null;

    protected String contentType = null;

    protected String compressionType = null;

    protected int compressedLen = -1;

    /**
     * for mzML
     */
    protected int massPrecision = -1;
    protected String massCompressionType = null;
    protected int massCompressedLen = -1;

    protected int intenPrecision = -1;
    protected String intenCompressionType = null;
    protected int intenCompressedLen = -1;

    /**
     * Store the byte offset, within the mz(X)ML file, at which the binary data
     * for this scan are found. dhmay re-adding 20091021. This was removed by in
     * mid-2008, super important. Note: this must be set explicitly by calling
     * code -- the offset won't be found in the scan XML itself
     */
    protected long scanOffset = -1;

    /**
     * <p>Getter for the field <code>basePeakIntensity</code>.</p>
     *
     * @return Returns the basePeakIntensity.
     */
    public float getBasePeakIntensity() {
        return basePeakIntensity;
    }

    /**
     * <p>Setter for the field <code>basePeakIntensity</code>.</p>
     *
     * @param basePeakIntensity The basePeakIntensity to set.
     */
    public void setBasePeakIntensity(float basePeakIntensity) {
        this.basePeakIntensity = basePeakIntensity;
    }

    /**
     * <p>Getter for the field <code>basePeakMz</code>.</p>
     *
     * @return Returns the basePeakMz.
     */
    public float getBasePeakMz() {
        return basePeakMz;
    }

    /**
     * <p>Setter for the field <code>basePeakMz</code>.</p>
     *
     * @param basePeakMz The basePeakMz to set.
     */
    public void setBasePeakMz(float basePeakMz) {
        this.basePeakMz = basePeakMz;
    }

    /**
     * <p>Getter for the field <code>byteOrder</code>.</p>
     *
     * @return returns the byteOrder
     */
    public String getByteOrder() {
        return byteOrder;
    }

    /**
     * <p>Setter for the field <code>byteOrder</code>.</p>
     *
     * @param byteOrder set the byteOrder
     */
    public void setByteOrder(String byteOrder) {
        this.byteOrder = byteOrder;
    }

    /**
     * <p>Getter for the field <code>centroided</code>.</p>
     *
     * @return Returns the centroided.
     */
    public int getCentroided() {
        return centroided;
    }

    /**
     * <p>Setter for the field <code>centroided</code>.</p>
     *
     * @param centroided The centroided to set.
     */
    public void setCentroided(int centroided) {
        this.centroided = centroided;
    }

    /**
     * <p>Getter for the field <code>chargeDeconvoluted</code>.</p>
     *
     * @return Returns the chargeDeconvoluted.
     */
    public int getChargeDeconvoluted() {
        return chargeDeconvoluted;
    }

    /**
     * <p>Setter for the field <code>chargeDeconvoluted</code>.</p>
     *
     * @param chargeDeconvoluted The chargeDeconvoluted to set.
     */
    public void setChargeDeconvoluted(int chargeDeconvoluted) {
        this.chargeDeconvoluted = chargeDeconvoluted;
    }

    /**
     * <p>Getter for the field <code>collisionEnergy</code>.</p>
     *
     * @return Returns the collisionEnergy.
     */
    public float getCollisionEnergy() {
        return collisionEnergy;
    }

    /**
     * <p>Setter for the field <code>collisionEnergy</code>.</p>
     *
     * @param collisionEnergy The collisionEnergy to set.
     */
    public void setCollisionEnergy(float collisionEnergy) {
        this.collisionEnergy = collisionEnergy;
    }

    /**
     * <p>Getter for the field <code>compressionType</code>.</p>
     *
     * @return returns compressionType
     */
    public String getCompressionType() {
        return compressionType;
    }

    /**
     * <p>Setter for the field <code>compressionType</code>.</p>
     *
     * @param compressionType set compressionType
     */
    public void setCompressionType(String compressionType) {
        this.compressionType = compressionType;
    }

    /**
     * <p>Getter for the field <code>compressedLen</code>.</p>
     *
     * @return returns compressedLen
     */
    public int getCompressedLen() {
        return compressedLen;
    }

    /**
     * <p>Setter for the field <code>compressedLen</code>.</p>
     *
     * @param compressedLen set compressedLen
     */
    public void setCompressedLen(int compressedLen) {
        this.compressedLen = compressedLen;
    }

    /**
     * <p>Getter for the field <code>contentType</code>.</p>
     *
     * @return returns the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * <p>Setter for the field <code>contentType</code>.</p>
     *
     * @param contentType set the contentType
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * <p>Getter for the field <code>deisotoped</code>.</p>
     *
     * @return Returns the deisotoped.
     */
    public int getDeisotoped() {
        return deisotoped;
    }

    /**
     * <p>Setter for the field <code>deisotoped</code>.</p>
     *
     * @param deisotoped The deisotoped to set.
     */
    public void setDeisotoped(int deisotoped) {
        this.deisotoped = deisotoped;
    }

    /**
     * <p>Getter for the field <code>endMz</code>.</p>
     *
     * @return Returns the endMz.
     */
    public float getEndMz() {
        return endMz;
    }

    /**
     * <p>Setter for the field <code>endMz</code>.</p>
     *
     * @param endMz The endMz to set.
     */
    public void setEndMz(float endMz) {
        this.endMz = endMz;
    }

    /**
     * <p>Getter for the field <code>filterLine</code>.</p>
     *
     * @return Returns the filterLine.
     */
    public String getFilterLine() {
        return filterLine;
    }

    /**
     * <p>Setter for the field <code>filterLine</code>.</p>
     *
     * @param filterLine a {@link java.lang.String} object.
     */
    public void setFilterLine(String filterLine) {
        this.filterLine = filterLine;
    }

    /**
     * <p>Getter for the field <code>highMz</code>.</p>
     *
     * @return Returns the highMz.
     */
    public float getHighMz() {
        return highMz;
    }

    /**
     * <p>Setter for the field <code>highMz</code>.</p>
     *
     * @param highMz The highMz to set.
     */
    public void setHighMz(float highMz) {
        this.highMz = highMz;
    }

    /**
     * <p>Getter for the field <code>ionisationEnergy</code>.</p>
     *
     * @return Returns the ionisationEnergy.
     */
    public float getIonisationEnergy() {
        return ionisationEnergy;
    }

    /**
     * <p>Setter for the field <code>ionisationEnergy</code>.</p>
     *
     * @param ionisationEnergy The ionisationEnergy to set.
     */
    public void setIonisationEnergy(float ionisationEnergy) {
        this.ionisationEnergy = ionisationEnergy;
    }

    /**
     * <p>Getter for the field <code>lowMz</code>.</p>
     *
     * @return Returns the lowMz.
     */
    public float getLowMz() {
        return lowMz;
    }

    /**
     * <p>Setter for the field <code>lowMz</code>.</p>
     *
     * @param lowMz The lowMz to set.
     */
    public void setLowMz(float lowMz) {
        this.lowMz = lowMz;
    }

    /**
     * <p>Getter for the field <code>msLevel</code>.</p>
     *
     * @return Returns the msLevel.
     */
    public int getMsLevel() {
        return msLevel;
    }

    /**
     * <p>Setter for the field <code>msLevel</code>.</p>
     *
     * @param msLevel The msLevel to set.
     */
    public void setMsLevel(int msLevel) {
        this.msLevel = msLevel;
    }

    /**
     * <p>Getter for the field <code>num</code>.</p>
     *
     * @return Returns the num.
     */
    public int getNum() {
        return num;
    }

    /**
     * <p>Setter for the field <code>num</code>.</p>
     *
     * @param num The num to set.
     */
    public void setNum(int num) {
        this.num = num;
    }

    /**
     * <p>Getter for the field <code>peaksCount</code>.</p>
     *
     * @return Returns the peaksCount.
     */
    public int getPeaksCount() {
        return peaksCount;
    }

    /**
     * <p>Setter for the field <code>peaksCount</code>.</p>
     *
     * @param peaksCount The peaksCount to set.
     */
    public void setPeaksCount(int peaksCount) {
        this.peaksCount = peaksCount;
    }

    /**
     * <p>Getter for the field <code>polarity</code>.</p>
     *
     * @return Returns the polarity.
     */
    public String getPolarity() {
        return polarity;
    }

    /**
     * <p>Setter for the field <code>polarity</code>.</p>
     *
     * @param polarity The polarity to set.
     */
    public void setPolarity(String polarity) {
        this.polarity = polarity;
    }

    /**
     * <p>Getter for the field <code>precision</code>.</p>
     *
     * @return Returns the precision.
     */
    public int getPrecision() {
        return precision;
    }

    /**
     * <p>Setter for the field <code>precision</code>.</p>
     *
     * @param precision The precision to set.
     */
    public void setPrecision(int precision) {
        this.precision = precision;
    }

    /**
     * <p>Getter for the field <code>precursorCharge</code>.</p>
     *
     * @return Returns the precursorCharge.
     */
    public int getPrecursorCharge() {
        return precursorCharge;
    }

    /**
     * <p>Setter for the field <code>precursorCharge</code>.</p>
     *
     * @param precursorCharge The precursorCharge to set.
     */
    public void setPrecursorCharge(int precursorCharge) {
        this.precursorCharge = precursorCharge;
    }

    /**
     * <p>Getter for the field <code>precursorMz</code>.</p>
     *
     * @return Returns the precursorMz.
     */
    public float getPrecursorMz() {
        return precursorMz;
    }

    /**
     * <p>Setter for the field <code>precursorMz</code>.</p>
     *
     * @param precursorMz The precursorMz to set.
     */
    public void setPrecursorMz(float precursorMz) {
        this.precursorMz = precursorMz;
    }

    /**
     * <p>Getter for the field <code>precursorScanNum</code>.</p>
     *
     * @return Returns the precursorScanNum.
     */
    public int getPrecursorScanNum() {
        return precursorScanNum;
    }

    /**
     * <p>Setter for the field <code>precursorScanNum</code>.</p>
     *
     * @param precursorScanNum The precursorScanNum to set.
     */
    public void setPrecursorScanNum(int precursorScanNum) {
        this.precursorScanNum = precursorScanNum;
    }

    /**
     * <p>Getter for the field <code>precursorIntensity</code>.</p>
     *
     * @return Returns the precursorIntensity
     */
    public float getPrecursorIntensity() {
        return precursorIntensity;
    }

    /**
     * <p>Setter for the field <code>precursorIntensity</code>.</p>
     *
     * @param precursorIntensity The precursorIntensity to set
     */
    public void setPrecursorIntensity(float precursorIntensity) {
        this.precursorIntensity = precursorIntensity;
    }

    /**
     * <p>Getter for the field <code>retentionTime</code>.</p>
     *
     * @return Returns the retentionTime.
     */
    public String getRetentionTime() {
        return retentionTime;
    }

    /**
     * <p>Setter for the field <code>retentionTime</code>.</p>
     *
     * @param retentionTime The retentionTime to set.
     */
    public void setRetentionTime(String retentionTime) {
        this.retentionTime = retentionTime;
    }

    /**
     *
     * The retentionTime for mzML.
     *
     * @return a double.
     */
    public double getRT() {
        return rt;
    }

    /**
     * <p>setRT.</p>
     *
     * @param rt a double.
     */
    public void setRT(double rt) {
        this.rt = rt;
    }

    /**
     * <p>Getter for the field <code>scanType</code>.</p>
     *
     * @return Returns the scanType.
     */
    public String getScanType() {
        return scanType;
    }

    /**
     * <p>Setter for the field <code>scanType</code>.</p>
     *
     * @param scanType The scanType to set.
     */
    public void setScanType(String scanType) {
        this.scanType = scanType;
    }

    /**
     * <p>Getter for the field <code>startMz</code>.</p>
     *
     * @return Returns the startMz.
     */
    public float getStartMz() {
        return startMz;
    }

    /**
     * <p>Setter for the field <code>startMz</code>.</p>
     *
     * @param startMz The startMz to set.
     */
    public void setStartMz(float startMz) {
        this.startMz = startMz;
    }

    /**
     * <p>Getter for the field <code>totIonCurrent</code>.</p>
     *
     * @return Returns the totIonCurrent.
     */
    public float getTotIonCurrent() {
        return totIonCurrent;
    }

    /**
     * <p>Setter for the field <code>totIonCurrent</code>.</p>
     *
     * @param totIonCurrent The totIonCurrent to set.
     */
    public void setTotIonCurrent(float totIonCurrent) {
        this.totIonCurrent = totIonCurrent;
    }

    /**
     * <p>getDoubleRetentionTime.</p>
     *
     * @return a double.
     */
    public double getDoubleRetentionTime() {
        // TODO: more robust ISO time conversion?
        if (retentionTime.charAt(0) != 'P'
                || retentionTime.charAt(1) != 'T'
                || retentionTime.charAt(retentionTime.length() - 1) != 'S') {
            throw new IllegalArgumentException(
                    "Format of retentiontime is not PTxxxxS, don't know how to parse "
                    + retentionTime);
        }

        return Double.parseDouble(
                retentionTime.substring(2, retentionTime.length() - 1));
    }

    /**
     * <p>Getter for the field <code>massPrecision</code>.</p>
     *
     * @return massPrecision
     */
    public int getMassPrecision() {
        return massPrecision;
    }

    /**
     * <p>Setter for the field <code>massPrecision</code>.</p>
     *
     * @param massPrecision to set
     */
    public void setMassPrecision(int massPrecision) {
        this.massPrecision = massPrecision;
    }

    /**
     * <p>Getter for the field <code>massCompressionType</code>.</p>
     *
     * @return massCompressionType
     */
    public String getMassCompressionType() {
        return massCompressionType;
    }

    /**
     * <p>Setter for the field <code>massCompressionType</code>.</p>
     *
     * @param massCompressionType a {@link java.lang.String} object.
     */
    public void setMassCompressionType(String massCompressionType) {
        this.massCompressionType = massCompressionType;
    }

    /**
     * <p>Getter for the field <code>massCompressedLen</code>.</p>
     *
     * @return massCompressionLen
     */
    public int getMassCompressedLen() {
        return massCompressedLen;
    }

    /**
     * <p>Setter for the field <code>massCompressedLen</code>.</p>
     *
     * @param massCompressedLen a int.
     */
    public void setMassCompressedLen(int massCompressedLen) {
        this.massCompressedLen = massCompressedLen;
    }

    /**
     * <p>Getter for the field <code>intenPrecision</code>.</p>
     *
     * @return intenPrecision
     */
    public int getIntenPrecision() {
        return intenPrecision;
    }

    /**
     * <p>Setter for the field <code>intenPrecision</code>.</p>
     *
     * @param intenPrecision a int.
     */
    public void setIntenPrecision(int intenPrecision) {
        this.intenPrecision = intenPrecision;
    }

    /**
     * <p>Getter for the field <code>intenCompressionType</code>.</p>
     *
     * @return intenCompressionType
     */
    public String getIntenCompressionType() {
        return intenCompressionType;
    }

    /**
     * <p>Setter for the field <code>intenCompressionType</code>.</p>
     *
     * @param intenCompressionType a {@link java.lang.String} object.
     */
    public void setIntenCompressionType(String intenCompressionType) {
        this.intenCompressionType = intenCompressionType;
    }

    /**
     * <p>Getter for the field <code>intenCompressedLen</code>.</p>
     *
     * @return intenCompressedLen
     */
    public int getIntenCompressedLen() {
        return intenCompressedLen;
    }

    /**
     * <p>Setter for the field <code>intenCompressedLen</code>.</p>
     *
     * @param intenCompressedLen a int.
     */
    public void setIntenCompressedLen(int intenCompressedLen) {
        this.intenCompressedLen = intenCompressedLen;
    }

    /**
     * {@inheritDoc}
     *
     * String respresentation of a ScanHeader object.
     *
     * Note: This is most likely not an optimal way to build the string.
     * Hopefully this method will only be used for testing.
     */
    @Override
    public String toString() {
        StringBuffer tmpStrBuffer = new StringBuffer(1000);
        tmpStrBuffer.append("SCANHEADER\n");
        tmpStrBuffer.append("==========\n");
        tmpStrBuffer.append("num = " + num + "\n");
        tmpStrBuffer.append("msLevel = " + msLevel + "\n");
        tmpStrBuffer.append("peaksCount = " + peaksCount + "\n");
        tmpStrBuffer.append("polarity = " + polarity + "\n");
        tmpStrBuffer.append("scanType = " + scanType + "\n");
        tmpStrBuffer.append("centroided = " + centroided + "\n");
        tmpStrBuffer.append("deisotoped = " + deisotoped + "\n");
        tmpStrBuffer.append(
                "chargeDeconvoluted = " + chargeDeconvoluted + "\n");
        tmpStrBuffer.append("retentionTime = " + retentionTime + "\n");
        tmpStrBuffer.append("startMz = " + startMz + "\n");
        tmpStrBuffer.append("endMz = " + endMz + "\n");
        tmpStrBuffer.append("lowMz = " + lowMz + "\n");
        tmpStrBuffer.append("highMz = " + highMz + "\n");
        tmpStrBuffer.append("basePeakMz = " + basePeakMz + "\n");
        tmpStrBuffer.append("basePeakIntensity = " + basePeakIntensity + "\n");
        tmpStrBuffer.append("totIonCurrent = " + totIonCurrent + "\n");
        tmpStrBuffer.append("precursorMz = " + precursorMz + "\n");
        tmpStrBuffer.append("precursorScanNum = " + precursorScanNum + "\n");
        tmpStrBuffer.append("precursorCharge = " + precursorCharge + "\n");
        tmpStrBuffer.append("precursorIntensity = " + precursorIntensity + "\n");
        tmpStrBuffer.append("collisionEnergy = " + collisionEnergy + "\n");
        tmpStrBuffer.append("ionisationEnergy = " + ionisationEnergy + "\n");
        tmpStrBuffer.append("precision = " + precision + "\n");
        //add for mzXML_3.0
        tmpStrBuffer.append("byteOrder = " + byteOrder + "\n");
        tmpStrBuffer.append("contentType = " + contentType + "\n");
        tmpStrBuffer.append("compressionType = " + compressionType + "\n");
        tmpStrBuffer.append("compressedLen = " + compressedLen + "\n");

        //for mzML
        tmpStrBuffer.append("rt " + rt + "\n");
        tmpStrBuffer.append("massPrecision " + massPrecision + "\n");
        tmpStrBuffer.append("massCompressionType " + massCompressionType + "\n");
        tmpStrBuffer.append("massCompressedLen " + massCompressedLen + "\n");
        tmpStrBuffer.append("intenPrecision " + intenPrecision + "\n");
        tmpStrBuffer.append("intenCompressionType " + intenCompressionType + "\n");
        tmpStrBuffer.append("intenCompressedLen " + intenCompressedLen + "\n");

        //add for support S(M)RM
        tmpStrBuffer.append("filterLine = " + filterLine + "\n");

        return (tmpStrBuffer.toString());
    }

    /**
     * <p>Getter for the field <code>scanOffset</code>.</p>
     *
     * @return a long.
     */
    public long getScanOffset() {
        return scanOffset;
    }

    /**
     * <p>Setter for the field <code>scanOffset</code>.</p>
     *
     * @param scanOffset a long.
     */
    public void setScanOffset(long scanOffset) {
        this.scanOffset = scanOffset;
    }
}
