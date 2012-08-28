<?xml version="1.0" encoding="UTF-8"?>
<!--
 Copyright (C) 2008-2012 Nils Hoffmann
 Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE

 This file is part of Cross/Maltcms.

 Cross/Maltcms is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Cross/Maltcms is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with Cross/Maltcms.  If not, see &lt;http://www.gnu.org/licenses/>.

 $Id$

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
