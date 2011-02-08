<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform version="2.0"
	xmlns="http://www.w3.org/1999/xhtml"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="org.apache.xalan.xslt.extensions.Redirect"
	extension-element-prefixes="xalan">
    <xsl:output method="xhtml" encoding="UTF-8"
		doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
		doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
		indent="yes" />


    <xsl:template match="/">
        <xsl:for-each select="//content">
            <xsl:choose>
                <xsl:when test="@publish='yes'">
                    <xsl:variable name="pagename">
						<!-- <xsl:value-of select="@toRoot" /> -->
                        <xsl:value-of select="@ident" />
                    </xsl:variable>
                    <xsl:variable name="activegroup">
                        <xsl:value-of select="@group" />
                    </xsl:variable>
                    <xsl:variable name="filename">
                        <xsl:value-of select="$pagename" />
                        <xsl:text>.html</xsl:text>
                    </xsl:variable>
                    <xsl:variable name="fileout">
                        <xsl:value-of select="@ident" />
                        <xsl:text>.html</xsl:text>
                    </xsl:variable>
                    <trace-write>
						writing out to
                        <xsl:value-of select="$fileout" />
						Variables:
						Group:
                        <xsl:value-of select="$activegroup" />
						Filename:
                        <xsl:value-of select="$filename" />
                    </trace-write>
                    <xalan:write select="$fileout">
                        <xsl:element name="html">
							<!--
								<xsl:attribute
								name="xmlns">http://www.w3.org/1999/xhtml</xsl:attribute>
							-->
							<!--
								OBSOLETE, not conforming to xhtml1.1 <xsl:attribute
								name="lang">en</xsl:attribute>
							-->
                            <xsl:attribute name="xml:lang">en</xsl:attribute>
                            <xsl:element name="head">
                                <xsl:element name="title">
                                    <xsl:value-of select="./@nname" />
                                </xsl:element>
                                <xsl:element name="link">
                                    <xsl:attribute name="type">
                                        <xsl:text>text/css</xsl:text>
                                    </xsl:attribute>
                                    <xsl:attribute name="href">
                                        <xsl:value-of
										select="./@toRoot" />
                                        <xsl:text>main.css</xsl:text>
                                    </xsl:attribute>
                                    <xsl:attribute name="rel">
                                        <xsl:text>stylesheet</xsl:text>
                                    </xsl:attribute>
                                </xsl:element>
                            </xsl:element>
                            <xsl:element name="body">
								<!--
									<xsl:element name="div"> <xsl:attribute
									name="class">all</xsl:attribute>
								-->
                                <xsl:copy-of select="//div[@id='header']" />
								<!--<xsl:apply-templates select="//div[@id='categories']" />-->
								<!-- <div id="page">-->
                                <xsl:element name="div">
                                    <xsl:attribute name="id">nav</xsl:attribute>
                                    <div id="navcontent">
                                        <xsl:element name="div">
                                            <xsl:attribute name="class">nav_stroked</xsl:attribute>
                                            <xsl:for-each select="//group">
                                                <xsl:variable name="currentgroup">
                                                    <xsl:value-of select="@ident" />
                                                </xsl:variable>
                                                <xsl:choose>
                                                    <xsl:when test="$currentgroup=$activegroup">
                                                        <xsl:call-template name="NAVTEMPLATE">
                                                            <xsl:with-param name="PAGENAME" select="$pagename" />
                                                            <xsl:with-param name="GROUPNAME" select="./@nname" />
                                                        </xsl:call-template>
                                                    </xsl:when>
                                                    <!--<xsl:otherwise>green</xsl:otherwise>-->
                                                </xsl:choose>
<!--                                                <xsl:when test="./@ident = '$activegroup'">
                                                    <xsl:call-template name="NAVTEMPLATE">
                                                        <xsl:with-param name="PAGENAME" select="$pagename" />
                                                        <xsl:with-param name="GROUPNAME" select="./@nname" />
                                                    </xsl:call-template>
                                                </xsl:when>-->
                                            </xsl:for-each>
                                        </xsl:element>
                                    </div>
                                </xsl:element>
                                <xsl:apply-templates select="child::node()">
                                    <xsl:with-param name="PAGENAME" select="$pagename" />
                                    <xsl:with-param name="GROUPNAME" select="$activegroup" />
                                </xsl:apply-templates>
								<!-- </div> -->
                                <div id="footer">
                                    <xsl:call-template name="COPYRIGHT" />
									2008-2011,
                                    <a title="Contact" href="../info/contact.html">Nils Hoffmann</a> | Maltcms is hosted by
                                    <a title="Hosting by sourceforge.net" href="http://sourceforge.net/">sourceforge.net</a> |
                                    <a href="http://validator.w3.org/check?uri=http%3A%2F%2Fmaltcms.sourceforge.net%2Fhome%2Findex.html">Valid XHTML 1.0 Transitional</a>
                                    <div id="sitemap">
                                            <xsl:for-each select="//group">
                                                <div style="float: left;">
                                                    <h3>
                                                        <xsl:value-of select="@ident"/>
                                                    </h3>
                                                    <ul style="list-style:none;">
                                                        <xsl:for-each select="./ref">
                                                            <li>
                                                                <xsl:call-template name="REFTEMPLATE">
                                                                    <xsl:with-param name="PAGENAME" select="$pagename" />
                                                                    <xsl:with-param name="GROUPNAME" select="$activegroup" />
                                                                </xsl:call-template>
                                                            </li>
                                                        </xsl:for-each>
                                                        <xsl:for-each select="./a">
                                                            <li>
                                                                <xsl:call-template name="MATCHXHTML" />
                                                            </li>
                                                        </xsl:for-each>
                                                    </ul>
                                                </div>
                                            </xsl:for-each>
                                    </div>
                                </div>
                            </xsl:element>
                        </xsl:element>
                    </xalan:write>
                </xsl:when>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="NAVTEMPLATE">
        <xsl:param name="PAGENAME" />
        <xsl:param name="GROUPNAME" />
        <xsl:value-of select="$GROUPNAME" />
        <xsl:element name="div">
            <xsl:call-template name="ATTRCLASSTEMPLATE" />
            <ul class="unbulletedlist">
                <xsl:for-each select="./ref">
                    <xsl:element name="li">
                        <xsl:call-template name="REFTEMPLATE">
                            <xsl:with-param name="PAGENAME" select="$PAGENAME" />
                            <xsl:with-param name="GROUPNAME" select="$GROUPNAME" />
                        </xsl:call-template>
                    </xsl:element>
                </xsl:for-each>
                <xsl:for-each select="./a">
                    <xsl:element name="li">
                        <xsl:call-template name="MATCHXHTML" />
                    </xsl:element>
                </xsl:for-each>
            </ul>
        </xsl:element>
    </xsl:template>

    <xsl:template name="ATTRCLASSTEMPLATE">
		<!--
			looks, wether the last selected node contains an attribute class and
			sets the value accordingly, need an xsl:element around the call
		-->
        <xsl:choose>
            <xsl:when test="./@class!=''">
                <xsl:attribute name="class">
                    <xsl:value-of select="./@class" />
                </xsl:attribute>
            </xsl:when>
            <xsl:when test="./@id!=''">
                <xsl:attribute name="id">
                    <xsl:value-of select="./@id" />
                </xsl:attribute>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="ref" name="REFTEMPLATE">
        <xsl:param name="PAGENAME" />
        <xsl:param name="GROUPNAME" />

        <xsl:variable name="posi">
            <xsl:value-of select="./@target" />
        </xsl:variable>
        <xsl:variable name="NNAME">
            <xsl:for-each select="//content">
                <xsl:choose>
                    <xsl:when test="./@ident=$posi">
                        <xsl:value-of select="./@nname" />
                    </xsl:when>
                </xsl:choose>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="PATHCORRECTION">
            <xsl:choose>
                <xsl:when test="not($NNAME=$GROUPNAME) and not($GROUPNAME='')">
                    <xsl:text>../</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>../</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="./@target=$PAGENAME">
                <xsl:attribute name="class">active</xsl:attribute>
            </xsl:when>
        </xsl:choose>

        <xsl:element name="a">
            <xsl:attribute name="href">
                <xsl:value-of select="$PATHCORRECTION" />
                <xsl:value-of select="./@target" />
                <xsl:text>.html</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="title">
                <xsl:value-of select="$NNAME" />
            </xsl:attribute>
			<!--
				<xsl:choose> <xsl:when test="./@target=$PAGENAME"> <xsl:attribute
				name="class">active</xsl:attribute> </xsl:when> </xsl:choose>
			-->
            <xsl:choose>
                <xsl:when test="./@alt!=''">
                    <xsl:value-of select="./@alt" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$NNAME" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>


    </xsl:template>

    <xsl:template match="new" name="NEWTEMPLATE">
        <xsl:choose>
            <xsl:when test="./@new!=''">
                <xsl:element name="span">
                    <xsl:attribute name="class">
                        <xsl:text>new</xsl:text>
                    </xsl:attribute>
                    <xsl:text>new</xsl:text>
                </xsl:element>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="column" name="COLUMNTEMPLATE">
        <xsl:element name="div">
            <xsl:call-template name="ATTRCLASSTEMPLATE" />
            <!--<xsl:element name="div">
                <xsl:attribute name="id">
                    <xsl:text>pagename</xsl:text>
                </xsl:attribute>
                <xsl:value-of select=".././@nname" />
            </xsl:element>-->
            <xsl:variable name="LASTCHANGED">
                <xsl:value-of select=".././@lastchanged" />
            </xsl:variable>
            <div id="colfooter">
				Changed on
                <xsl:value-of select="$LASTCHANGED" />
            </div>
            <xsl:element name="div">
                <xsl:attribute name="id">
                    <xsl:text>colcontent</xsl:text>
                </xsl:attribute>
                <xsl:apply-templates />
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="info" name="INFOTEMPLATE">
        <xsl:element name="div">
            <xsl:attribute name="class">
                <xsl:text>heading2</xsl:text>
            </xsl:attribute>
            <xsl:apply-templates />
        </xsl:element>

    </xsl:template>

    <xsl:template match="date" name="DATETEMPLATE">
        <xsl:value-of select="." />
        <xsl:text>, </xsl:text>
    </xsl:template>

    <xsl:template match="author" name="AUTHORTEMPLATE">
        <xsl:value-of select="." />
    </xsl:template>

    <xsl:template match="include" name="INCLUDETEMPLATE">
        <xsl:apply-templates />
    </xsl:template>

    <xsl:template match="heading" name="HEADINGTEMPLATE">
        <xsl:element name="div">
            <xsl:element name="span">
                <xsl:attribute name="class">
                    <xsl:text>heading</xsl:text>
                    <xsl:value-of select="./@level" />
                </xsl:attribute>
				<!-- sets the content of the column heading -->
                <xsl:apply-templates />
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="para" name="PARATEMPLATE">
        <xsl:variable name="paratitle">
            <xsl:value-of select="./@title" />
        </xsl:variable>
        <xsl:element name="div">
            <xsl:call-template name="ATTRCLASSTEMPLATE" />
            <xsl:if test="$paratitle!=''">
                <xsl:element name="div">
                    <span class="heading1">
                        <xsl:value-of select="$paratitle" />
                    </span>
                </xsl:element>
            </xsl:if>
            <xsl:apply-templates />
        </xsl:element>
    </xsl:template>

    <xsl:template match="span" name="SPANTEMPLATE">
        <xsl:copy>
            <xsl:call-template name="ATTRCLASSTEMPLATE" />
            <xsl:apply-templates />
        </xsl:copy>
    </xsl:template>

    <xsl:template match="list" name="LISTTEMPLATE">
        <div>
            <xsl:variable name="listclass">
                <xsl:value-of select="./@class" />
            </xsl:variable>
            <xsl:element name="ul">
                <xsl:if test="$listclass!=''">
                    <xsl:attribute name="class">
                        <xsl:value-of
						select="$listclass" />
                    </xsl:attribute>
                </xsl:if>
                <xsl:apply-templates />
            </xsl:element>
        </div>
    </xsl:template>

    <xsl:template match="item" name="ITEMTEMPLATE">
        <xsl:variable name="tindent">
            <xsl:value-of select="../@indent-items" />
        </xsl:variable>
        <xsl:element name="li">
            <xsl:attribute name="class">item</xsl:attribute>
            <xsl:if test="$tindent!=''">
                <xsl:attribute name="style">text-indent:
                    <xsl:value-of
					select="$tindent" />
                </xsl:attribute>
            </xsl:if>
            <xsl:apply-templates />
        </xsl:element>


    </xsl:template>

    <xsl:template match="text" name="TEXTTEMPLATE">
        <xsl:value-of select="." />
    </xsl:template>

    <xsl:template name="XHTMLVALIDTEMPLATE">
        <div class="bottom">
            <a href="http://validator.w3.org/check?uri=referer">
                <img src="img/valid_xhtml1_0.png" alt="Valid XHTML 1.0!" height="25"
					width="88" />
            </a>
        </div>
    </xsl:template>

	<!--
		<xsl:template match="new" name="NEWTEMPLATE"> <span
		class="high">new</span> </xsl:template>
	-->

    <xsl:template match="copy" name="COPYRIGHT">
        <xsl:text>&#169;</xsl:text>
    </xsl:template>

    <xsl:template name="MATCHXHTML"
		match="a|abbr|acronym|address|b|big|blockquote|br|cite|dfn|div|em|h1|h2|h3|h4|h5|h6|hr|i|kbd|p|pre|q|quote|samp|span|small|strong|sub|sup|tt|var|button|fieldset|form|input|label|legend|option|optgroup|select|caption|col|colgroup|table|tbody|td|tfoot|th|thead|tr|dl|dd|dt|ol|ul|li|img">
		<!-- alle XHTML 1.1 Tags werden kopiert -->

        <xsl:copy>
            <xsl:copy-of select="@*" />
            <xsl:apply-templates />
        </xsl:copy>
    </xsl:template>
</xsl:transform>
