<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform version="2.0"
               xmlns="http://www.w3.org/1999/xhtml"
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="org.apache.xalan.xslt.extensions.Redirect"
               xmlns:xi="http://www.w3.org/2001/XInclude"
               extension-element-prefixes="xalan" exclude-result-prefixes="xsl xi xalan">
    <!--exclude-result-prefixes="#default xsl xi xalan"> -->
    <xsl:output method="xhtml" encoding="iso-8859-1"
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
                    <xsl:variable name="activesubgroup">
                        <xsl:value-of select="@subgroup" />
                    </xsl:variable>
                    <xsl:variable name="filename">
                        <xsl:value-of select="$pagename" />
                        <xsl:text>.html</xsl:text>
                    </xsl:variable>
                    <xsl:variable name="fileout">
                        <xsl:value-of select="@ident" />
                        <xsl:text>.html</xsl:text>
                    </xsl:variable>
                    <xsl:variable name="pathToRoot">
                        <xsl:value-of select="./@toRoot" />
                    </xsl:variable>
                    <xsl:variable name="headerPathToRoot">
                        <xsl:text>../</xsl:text>
                        <xsl:value-of select="./@pathToRoot" />
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
                                <!--                                <xsl:element name="link">
                                    <xsl:attribute name="type">
                                        <xsl:text>text/css</xsl:text>
                                    </xsl:attribute>
                                    <xsl:attribute name="rel">
                                        <xsl:text>stylesheet</xsl:text>
                                    </xsl:attribute>
                                    <xsl:attribute name="href">
                                        <xsl:value-of
                                            select="$pathToRoot" />
                                        <xsl:text>css/shCore.css</xsl:text>
                                    </xsl:attribute>
                                </xsl:element>
                                <xsl:element name="link">
                                    <xsl:attribute name="type">
                                        <xsl:text>text/css</xsl:text>
                                    </xsl:attribute>
                                    <xsl:attribute name="rel">
                                        <xsl:text>stylesheet</xsl:text>
                                    </xsl:attribute>
                                    <xsl:attribute name="href">
                                        <xsl:value-of
                                            select="$pathToRoot" />
                                        <xsl:text>css/shThemeDefault.css</xsl:text>
                                    </xsl:attribute>
                                </xsl:element>
                                <xsl:element name="script">
                                    <xsl:attribute name="type">
                                        <xsl:text>text/javascript</xsl:text>
                                    </xsl:attribute>
                                    <xsl:attribute name="src">
                                        <xsl:value-of
                                            select="$pathToRoot" />
                                        <xsl:text>scripts/shCore.js</xsl:text>
                                    </xsl:attribute>
                                    <xsl:text>  </xsl:text>
                                </xsl:element>
                                <xsl:element name="script">
                                    <xsl:attribute name="type">
                                        <xsl:text>text/javascript</xsl:text>
                                    </xsl:attribute>
                                    <xsl:attribute name="src">
                                        <xsl:value-of
                                            select="$pathToRoot" />
                                        <xsl:text>scripts/shBrushJava.js</xsl:text>
                                    </xsl:attribute>
                                    <xsl:text>  </xsl:text>
                                </xsl:element>-->
                                <xsl:element name="link">
                                    <!-- Bootstrap -->
                                    <xsl:attribute name="type">
                                        <xsl:text>text/css</xsl:text>
                                    </xsl:attribute>
                                    <xsl:attribute name="rel">
                                        <xsl:text>stylesheet</xsl:text>
                                    </xsl:attribute>
                                    <xsl:attribute name="href">
                                        <xsl:value-of
                                            select="$pathToRoot" />
                                        <xsl:text>css/bootstrap.min.css</xsl:text>
                                    </xsl:attribute>
                                </xsl:element>
                                <xsl:element name="link">
                                    <xsl:attribute name="type">
                                        <xsl:text>text/css</xsl:text>
                                    </xsl:attribute>
                                    <xsl:attribute name="href">
                                        <xsl:value-of
                                            select="$pathToRoot" />
                                        <xsl:text>css/main.css</xsl:text>
                                    </xsl:attribute>
                                    <xsl:attribute name="rel">
                                        <xsl:text>stylesheet</xsl:text>
                                    </xsl:attribute>
                                </xsl:element>
                            </xsl:element>
                            <xsl:element name="body">
                                <xsl:element name="div">
                                    <xsl:attribute name="class">container</xsl:attribute>
                                    <!-- HEADER -->	
                                    <xsl:element name="div">
                                        <xsl:if test="@header='true' or not(@header)">
                                            <xsl:attribute name="id">header</xsl:attribute>
                                    
                                            <xsl:apply-templates select="//header" xsl:exclude-result-prefixes="xsl xi xalan"/>
                                            <xsl:for-each select="//header/download">
                                                <xsl:call-template name="DOWNLOADTEMPLATE">
                                                    <xsl:with-param name="PATHTOROOT" select="$pathToRoot" />
                                                </xsl:call-template>
                                            </xsl:for-each>
                                            <xsl:for-each select="//header/siteimage">
                                                <div id="siteimage" style="float: left clear:none;">
                                                    <xsl:element name="img">
                                                        <xsl:attribute name="src">
                                                            <xsl:value-of select="$pathToRoot"/>
                                                            <xsl:text>img/ChromA4DSurface.png</xsl:text>
                                                        </xsl:attribute>
                                                        <xsl:attribute name="alt">Chroma4D Surface Plot</xsl:attribute>
                                                    </xsl:element>
                                                </div>
                                            </xsl:for-each>
                                        </xsl:if>
                                        <xsl:if test="@categories='true' or not(@categories)">
                                            <xsl:element name="div">
                                                <xsl:attribute name="id">categories</xsl:attribute>
                                                <xsl:element name="ul">
                                                    <xsl:attribute name="class">nav nav-pills</xsl:attribute>
                                                    <xsl:for-each select="//categories">
                                                        <xsl:for-each select="./ref">
                                                            <li>
                                                                <xsl:call-template name="REFTEMPLATE">
                                                                    <xsl:with-param name="PAGENAME" select="$pagename" />
                                                                    <xsl:with-param name="GROUPNAME" select="$activegroup" />
                                                                    <xsl:with-param name="PATHTOROOT" select="$pathToRoot" />
                                                                </xsl:call-template>
                                                            </li>
                                                        </xsl:for-each>
                                                        <xsl:for-each select="./a">
                                                            <li>
                                                                <xsl:call-template name="MATCHXHTML" >
                                                                    <xsl:with-param name="PAGENAME" select="$pagename" />
                                                                    <xsl:with-param name="GROUPNAME" select="$activegroup" />
                                                                    <xsl:with-param name="PATHTOROOT" select="$pathToRoot" />
                                                                </xsl:call-template>
                                                            </li>
                                                        </xsl:for-each>
                                                    </xsl:for-each>
                                                </xsl:element>
                                            </xsl:element>
                                        </xsl:if>
                                    </xsl:element>
                                    <!-- NAVIGATION -->	
                                    <xsl:if test="@navigation='true' or not(@navigation)">
                                        <xsl:for-each select="//nav">
                                            <xsl:element name="div">
                                                <xsl:call-template name="ATTRCLASSTEMPLATE"/>
                                                <!--                                            <xsl:attribute name="id">nav</xsl:attribute>-->
                                                <xsl:for-each select="//group">
                                                    <xsl:variable name="currentgroup">
                                                        <xsl:value-of select="@ident" />
                                                    </xsl:variable>
                                                    <xsl:choose>
                                                        <xsl:when test="$currentgroup=$activegroup">
                                                            <xsl:call-template name="NAVTEMPLATE">
                                                                <xsl:with-param name="PAGENAME" select="$pagename" />
                                                                <xsl:with-param name="GROUPNAME" select="./@nname" />
                                                                <xsl:with-param name="PATHTOROOT" select="$pathToRoot" />
                                                            </xsl:call-template>
                                                        </xsl:when>
                                                    </xsl:choose>
                                                </xsl:for-each>
                                            </xsl:element>
                                        </xsl:for-each>
                                    </xsl:if>
                                    <!-- CONTENT -->
                                    <xsl:apply-templates select="child::node()">
                                        <xsl:with-param name="PAGENAME" select="$pagename" />
                                        <xsl:with-param name="GROUPNAME" select="$activegroup" />
                                        <xsl:with-param name="PATHTOROOT" select="$pathToRoot" />
                                    </xsl:apply-templates>
                                    <xsl:if test="@footer='true' or not(@footer)">
                                        <xsl:copy-of select="//div[@id='footer']" xsl:exclude-result-prefixes="xsl xi xalan"/>
                                    </xsl:if>
                                    <xsl:if test="@sitemap='true' or not(@sitemap)">
                                        <div id="sitemap">
                                            <xsl:for-each select="//group">
                                                <div style="float: left;">
                                                    <span class="sitemapHeading">
                                                        <xsl:value-of select="@nname"/>
                                                    </span>
                                                    <ul class="sitemapList">
                                                        <xsl:for-each select="./ref">
                                                            <li>
                                                                <xsl:call-template name="REFTEMPLATE">
                                                                    <xsl:with-param name="PAGENAME" select="$pagename" />
                                                                    <xsl:with-param name="GROUPNAME" select="$activegroup" />
                                                                    <xsl:with-param name="PATHTOROOT" select="$pathToRoot" />
                                                                </xsl:call-template>
                                                            </li>
                                                        </xsl:for-each>
                                                        <xsl:for-each select="./a">
                                                            <li>
                                                                <xsl:call-template name="MATCHXHTML" >
                                                                    <xsl:with-param name="PAGENAME" select="$pagename" />
                                                                    <xsl:with-param name="GROUPNAME" select="$activegroup" />
                                                                    <xsl:with-param name="PATHTOROOT" select="$pathToRoot" />
                                                                </xsl:call-template>
                                                            </li>
                                                        </xsl:for-each>
                                                    </ul>
                                                </div>
                                            </xsl:for-each>
                                        </div>
                                    </xsl:if>
                                </xsl:element>
                                <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
                                <xsl:element name="script">
                                    <xsl:attribute name="src">
                                        <xsl:text>https://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js</xsl:text>
                                    </xsl:attribute>
                                    <xsl:attribute name="type">
                                        <xsl:text>text/javascript</xsl:text>
                                    </xsl:attribute>
                                    <xsl:text> </xsl:text>
                                </xsl:element>
                                <!-- Include all compiled plugins (below), or include individual files as needed -->
                                <xsl:element name="script">
                                    <xsl:attribute name="src">
                                        <xsl:value-of
                                            select="$pathToRoot" />
                                        <xsl:text>js/bootstrap.min.js</xsl:text>
                                    </xsl:attribute>
                                    <xsl:attribute name="type">
                                        <xsl:text>text/javascript</xsl:text>
                                    </xsl:attribute>
                                    <xsl:text> </xsl:text>
                                </xsl:element>
                                <!-- Include holder.js -->
                                <xsl:element name="script">
                                    <xsl:attribute name="src">
                                        <xsl:value-of
                                            select="$pathToRoot" />
                                        <xsl:text>js/holder.js</xsl:text>
                                    </xsl:attribute>
                                    <xsl:attribute name="type">
                                        <xsl:text>text/javascript</xsl:text>
                                    </xsl:attribute>
                                    <xsl:text> </xsl:text>
                                </xsl:element>
                            </xsl:element>
                        </xsl:element>
                    </xalan:write>
                </xsl:when>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="MESSAGETEMPLATE" match="message">
        <xsl:param name="PAGENAME"/>
        <xsl:param name="GROUPNAME" />
        <xsl:param name="PATHTOROOT" />
        <xsl:element name="div">
            <xsl:attribute name="class">
                <xsl:value-of select="./@class"/>
            </xsl:attribute>
            <h1>
                <xsl:value-of select="./@title"/>
            </h1>
            <p>
                <xsl:apply-templates>
                    <xsl:with-param name="PAGENAME" select="$PAGENAME" />
                    <xsl:with-param name="GROUPNAME" select="$GROUPNAME" />
                    <xsl:with-param name="PATHTOROOT" select="$PATHTOROOT" />
                </xsl:apply-templates>
            </p>
        </xsl:element>
    </xsl:template>
	
    <xsl:template name="DOWNLOADTEMPLATE">
        <xsl:param name="PATHTOROOT"/>
        <div class="download">
            <xsl:element name="a">
                <xsl:attribute name="href">
                    <xsl:value-of select="./@href"/>
                </xsl:attribute>
                <xsl:attribute name="title">
                    <xsl:value-of select="./@title"/>
                </xsl:attribute>
                <span style="font-size:1em; text-align: left; float: left; margin: 0.2em;">
                    <span class="dlTitle">
                        <xsl:value-of select="./@text"/>
                    </span>
                    <span class="dlSubtitle">
                        <xsl:value-of select="./@provider"/>
                    </span>    
                </span>
            </xsl:element>
            <xsl:element name="a">
                <xsl:attribute name="href">
                    <xsl:value-of select="./@href"/>
                </xsl:attribute>
                <xsl:attribute name="title">
                    <xsl:value-of select="./@title"/>
                </xsl:attribute>
                <xsl:element name="img">
                    <xsl:attribute name="src">
                        <xsl:value-of select="$PATHTOROOT"/>
                        <xsl:text>img/tango-go-down-red.png</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="alt">
                        <xsl:text>Download Image</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="style">
                        <xsl:text>float: right;</xsl:text>
                    </xsl:attribute>
                </xsl:element>
            </xsl:element>
        </div>
    </xsl:template>

    <xsl:template name="NAVTEMPLATE">
        <xsl:param name="PAGENAME" />
        <xsl:param name="GROUPNAME" />
        <xsl:param name="PATHTOROOT" />
        <!--<xsl:value-of select="$GROUPNAME" />-->
        <!--        <xsl:element name="div">
        <xsl:attribute name="class">nav_dotted</xsl:attribute>-->
        <xsl:call-template name="ATTRCLASSTEMPLATE" />
        <ul class="unbulletedlist">
            <xsl:for-each select="./ref">
                <xsl:element name="li">
                    <xsl:call-template name="REFTEMPLATE">
                        <xsl:with-param name="PAGENAME" select="$PAGENAME" />
                        <xsl:with-param name="GROUPNAME" select="$GROUPNAME" />
                        <xsl:with-param name="PATHTOROOT" select="$PATHTOROOT" />
                    </xsl:call-template>
                </xsl:element>
            </xsl:for-each>
            <xsl:for-each select="./a">
                <xsl:element name="li">
                    <xsl:call-template name="MATCHXHTML" />
                </xsl:element>
            </xsl:for-each>
        </ul>
        <xsl:for-each select="./group">
            <xsl:call-template name="NAVTEMPLATE">
                <xsl:with-param name="PAGENAME" select="$PAGENAME" />
                <xsl:with-param name="GROUPNAME" select="./@nname" />
                <xsl:with-param name="PATHTOROOT" select="$PATHTOROOT" />
            </xsl:call-template>
        </xsl:for-each>
            
        <!--</xsl:element>-->
    </xsl:template>
    
    <xsl:template name="ATTRCLASSTEMPLATE">
        <!--
            looks, wether the last selected node contains an attribute class and
            sets the value accordingly, need an xsl:element around the call
        -->
        <xsl:if test="./@style!=''">
            <xsl:attribute name="style">
                <xsl:value-of select="./@style" />
            </xsl:attribute>
        </xsl:if>
        <xsl:if test="./@class!=''">
            <xsl:attribute name="class">
                <xsl:value-of select="./@class" />
            </xsl:attribute>
        </xsl:if>
        <xsl:if test="./@id!=''">
            <xsl:attribute name="id">
                <xsl:value-of select="./@id" />
            </xsl:attribute>
        </xsl:if>
    </xsl:template>

    <xsl:template match="ref">
        <xsl:param name="PAGENAME" />
        <xsl:param name="GROUPNAME" />
        <xsl:param name="PATHTOROOT" />
        <xsl:call-template name="REFTEMPLATE">
            <xsl:with-param name="PAGENAME" select="$PAGENAME" />
            <xsl:with-param name="GROUPNAME" select="$GROUPNAME" />
            <xsl:with-param name="PATHTOROOT" select="$PATHTOROOT" />
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="REFTEMPLATE">
        <xsl:param name="PAGENAME" />
        <xsl:param name="GROUPNAME" />
        <xsl:param name="PATHTOROOT" />

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
            <!-- adapt links to current position in page tree -->
            <xsl:choose>
                <xsl:when test="@subgroup">
                    <xsl:if test="./@subgroup=$GROUPNAME">
                        <xsl:text>../</xsl:text>
                    </xsl:if>
                </xsl:when>
                <xsl:when test="@group">
                    <xsl:if test="./@group=$GROUPNAME">
                        <xsl:text>../</xsl:text>
                    </xsl:if>
                </xsl:when>
            </xsl:choose>
            <xsl:value-of select="$PATHTOROOT"/>
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

    <xsl:template name="IMGREFTEMPLATE">
        <xsl:param name="PATHTOROOT" />

        <xsl:element name="img">
            <xsl:attribute name="src">
                <xsl:value-of select="$PATHTOROOT" />
                <xsl:value-of select="./@src" />
            </xsl:attribute>
            <xsl:attribute name="title">
                <xsl:value-of select="./@title" />
            </xsl:attribute>
            <xsl:attribute name="alt">
                <xsl:value-of select="./@alt" />
            </xsl:attribute>
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
        <xsl:param name="PAGENAME" />
        <xsl:param name="GROUPNAME" />
        <xsl:param name="PATHTOROOT" />
        <xsl:element name="div">
            <xsl:call-template name="ATTRCLASSTEMPLATE" />
            <xsl:variable name="LASTCHANGED">
                <xsl:value-of select=".././@lastchanged" />
            </xsl:variable>
            <xsl:element name="div">
                <xsl:attribute name="id">
                    <xsl:text>colcontent</xsl:text>
                </xsl:attribute>
                <xsl:apply-templates>
                    <xsl:with-param name="PAGENAME" select="$PAGENAME" />
                    <xsl:with-param name="GROUPNAME" select="$GROUPNAME" />
                    <xsl:with-param name="PATHTOROOT" select="$PATHTOROOT" />
                </xsl:apply-templates>
            </xsl:element>
            <div id="colfooter">
                Changed on
                <xsl:value-of select="$LASTCHANGED" />
            </div>
        </xsl:element>
    </xsl:template>

    <xsl:template match="info" name="INFOTEMPLATE">
        <xsl:param name="PAGENAME" />
        <xsl:param name="GROUPNAME" />
        <xsl:param name="PATHTOROOT" />
        <xsl:element name="div">
            <xsl:attribute name="class">
                <xsl:text>heading2</xsl:text>
            </xsl:attribute>
            <xsl:apply-templates>
                <xsl:with-param name="PAGENAME" select="$PAGENAME" />
                <xsl:with-param name="GROUPNAME" select="$GROUPNAME" />
                <xsl:with-param name="PATHTOROOT" select="$PATHTOROOT" />
            </xsl:apply-templates>
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
        <xsl:param name="PAGENAME" />
        <xsl:param name="GROUPNAME" />
        <xsl:param name="PATHTOROOT" />
        <xsl:apply-templates>
            <xsl:with-param name="PAGENAME" select="$PAGENAME" />
            <xsl:with-param name="GROUPNAME" select="$GROUPNAME" />
            <xsl:with-param name="PATHTOROOT" select="$PATHTOROOT" />
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="heading" name="HEADINGTEMPLATE">
        <xsl:param name="PAGENAME" />
        <xsl:param name="GROUPNAME" />
        <xsl:param name="PATHTOROOT" />
        <xsl:element name="div">
            <xsl:element name="span">
                <xsl:attribute name="class">
                    <xsl:text>heading</xsl:text>
                    <xsl:value-of select="./@level" />
                </xsl:attribute>
                <!-- sets the content of the column heading -->
                <xsl:apply-templates>
                    <xsl:with-param name="PAGENAME" select="$PAGENAME" />
                    <xsl:with-param name="GROUPNAME" select="$GROUPNAME" />
                    <xsl:with-param name="PATHTOROOT" select="$PATHTOROOT" />
                </xsl:apply-templates>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="para" name="PARATEMPLATE">
        <xsl:param name="PAGENAME" />
        <xsl:param name="GROUPNAME" />
        <xsl:param name="PATHTOROOT" />
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
            <xsl:apply-templates>
                <xsl:with-param name="PAGENAME" select="$PAGENAME" />
                <xsl:with-param name="GROUPNAME" select="$GROUPNAME" />
                <xsl:with-param name="PATHTOROOT" select="$PATHTOROOT" />
            </xsl:apply-templates>
        </xsl:element>
    </xsl:template>

    <xsl:template match="span" name="SPANTEMPLATE">
        <xsl:param name="PAGENAME" />
        <xsl:param name="GROUPNAME" />
        <xsl:param name="PATHTOROOT" />
        <xsl:copy>
            <xsl:call-template name="ATTRCLASSTEMPLATE" />
            <xsl:apply-templates>
                <xsl:with-param name="PAGENAME" select="$PAGENAME" />
                <xsl:with-param name="GROUPNAME" select="$GROUPNAME" />
                <xsl:with-param name="PATHTOROOT" select="$PATHTOROOT" />
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="list" name="LISTTEMPLATE">
        <xsl:param name="PAGENAME" />
        <xsl:param name="GROUPNAME" />
        <xsl:param name="PATHTOROOT" />
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
                <xsl:apply-templates>
                    <xsl:with-param name="PAGENAME" select="$PAGENAME" />
                    <xsl:with-param name="GROUPNAME" select="$GROUPNAME" />
                    <xsl:with-param name="PATHTOROOT" select="$PATHTOROOT" />
                </xsl:apply-templates>
            </xsl:element>
        </div>
    </xsl:template>

    <xsl:template match="item" name="ITEMTEMPLATE">
        <xsl:param name="PAGENAME" />
        <xsl:param name="GROUPNAME" />
        <xsl:param name="PATHTOROOT" />
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
            <xsl:apply-templates>
                <xsl:with-param name="PAGENAME" select="$PAGENAME" />
                <xsl:with-param name="GROUPNAME" select="$GROUPNAME" />
                <xsl:with-param name="PATHTOROOT" select="$PATHTOROOT" />
            </xsl:apply-templates>
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

    <xsl:template match="copy" name="COPYRIGHT">
        <xsl:text>&#169;</xsl:text>
    </xsl:template>

    <xsl:template name="MATCHXHTML"
                  match="a|abbr|acronym|address|b|big|blockquote|br|cite|code|dfn|div|em|h1|h2|h3|h4|h5|h6|hr|i|kbd|p|pre|q|quote|samp|span|small|strong|sub|sup|tt|var|button|fieldset|form|input|label|legend|option|optgroup|select|caption|col|colgroup|table|tbody|td|tfoot|th|thead|tr|dl|dd|dt|ol|ul|li|img|script">

        <xsl:param name="PAGENAME" />
        <xsl:param name="GROUPNAME" />
        <xsl:param name="PATHTOROOT" />

        <xsl:copy xsl:exclude-result-prefixes="xsl xi xalan">
            <xsl:copy-of select="@*" xsl:exclude-result-prefixes="xsl xi xalan"/>
            <xsl:apply-templates>
                <xsl:with-param name="PAGENAME" select="$PAGENAME" />
                <xsl:with-param name="GROUPNAME" select="$GROUPNAME" />
                <xsl:with-param name="PATHTOROOT" select="$PATHTOROOT" />
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>
</xsl:transform>
