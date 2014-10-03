/**
 * *****************************************************************************
 * --------------------------------------------------------------------------- *
 * File: * @(#) SoftwareInfo.java * Author: * Mathijs Vogelzang m_v@dds.nl
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
 * Created on May 21, 2004
 *
 *****************************************************************************
 */
package org.systemsbiology.jrap.staxnxt;

/**
 * SoftwareInfo represents data that is available on software that has processed
 * a mzXML file.
 *
 * @author Mathijs Vogelzang
 * 
 * @since 1.3.2
 */
public class SoftwareInfo {

    public String type, name, version;

    /**
     * <p>Constructor for SoftwareInfo.</p>
     *
     * @param type a {@link java.lang.String} object.
     * @param name a {@link java.lang.String} object.
     * @param version a {@link java.lang.String} object.
     */
    public SoftwareInfo(String type, String name, String version) {
        this.type = type;
        this.name = name;
        this.version = version;
    }

    /**
     * <p>Setter for the field <code>type</code>.</p>
     *
     * @param type a {@link java.lang.String} object.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * <p>Setter for the field <code>name</code>.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * <p>Setter for the field <code>version</code>.</p>
     *
     * @param version a {@link java.lang.String} object.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return ("type " + type + " name " + name + " version " + version);
    }
}
