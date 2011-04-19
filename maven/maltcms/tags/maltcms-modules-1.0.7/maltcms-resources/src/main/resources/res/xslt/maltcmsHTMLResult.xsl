<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" encoding="UTF-8"/>

  <xsl:template match="/">
    <html>
       	<head><title>Results of Maltcms Run</title></head>
        <body>
        	<table border="1">
        		<tr>
					<td><b>Result File</b></td>
					<td><b>Description</b></td>
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
  		<td><xsl:value-of select="./@slot"/></td>
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
