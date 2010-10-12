<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text" encoding="UTF-8"/>
<xsl:template match="/workflow">
#Results of Maltcms Run
<xsl:apply-templates/>
</xsl:template>
<xsl:template match="/workflow/workflowElementResult">
file<xsl:number/>=<xsl:value-of select="./@file"/>
</xsl:template>
</xsl:stylesheet>
