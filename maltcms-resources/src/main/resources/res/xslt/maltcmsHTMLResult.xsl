<?xml version="1.0" encoding="UTF-8"?>
<!--
Maltcms, modular application toolkit for chromatography-mass spectrometry. 
Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.

Project website: http://maltcms.sf.net

Maltcms may be used under the terms of either the

GNU Lesser General Public License (LGPL)
http://www.gnu.org/licenses/lgpl.html

or the

Eclipse Public License (EPL)
http://www.eclipse.org/org/documents/epl-v10.php

As a user/recipient of Maltcms, you may choose which license to receive the code 
under. Certain files or entire directories may not be covered by this 
dual license, but are subject to licenses compatible to both LGPL and EPL.
License exceptions are explicitly declared in all relevant files or in a 
LICENSE file in the relevant directories.

Maltcms is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
for details.
-->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" encoding="UTF-8"/>

    <xsl:template match="/">
        <html>
            <head>
                <title>Results of Maltcms Run</title>
            </head>
            <body>
                <table border="1">
                    <tr>
                        <td>
                            <b>Result File</b>
                        </td>
                        <td>
                            <b>Description</b>
                        </td>
                    </tr>
                    <xsl:apply-templates/>
                </table>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="/workflow/workflowElementResult">
        <tr>
            <td>
                <xsl:apply-templates/>
            </td>
            <td>
                <xsl:value-of select="./@slot"/>
            </td>
        </tr>
    </xsl:template>
  
    <xsl:template match="/workflow/workflowElementResult/resources/resource">
        <xsl:element name="a">
            <xsl:attribute name="href">
                <xsl:value-of select="./@file"/>
            </xsl:attribute>
            <xsl:value-of select="./@file"/>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
