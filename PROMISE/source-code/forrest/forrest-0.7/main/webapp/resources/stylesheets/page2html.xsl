<?xml version="1.0"?>
<!--
  Copyright 2002-2005 The Apache Software Foundation or its licensors,
  as applicable.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<!--+
    | Convert samples file to the HTML page. Uses styles/main.css stylesheet.
    +-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink">

  <xsl:param name="contextPath" select="string('/cocoon')"/>

 <xsl:template match="/">
  <html>
   <head>
     <title>Apache Forrest</title>
     <link rel="SHORTCUT ICON" href="favicon.ico"/>
     <xsl:apply-templates select="document/header/style"/>
     <xsl:apply-templates select="document/header/script"/>
   </head>
   <body>
    <table border="0" cellspacing="2" cellpadding="2" align="center" width="100%">
      <tr>
        <td width="50%">
          <h2><xsl:value-of select="document/header/title"/></h2>
        </td>
        <td width="25%">
          <xsl:apply-templates select="document/header/tab"/>
        </td>
      </tr>
    </table>

    <p>
     <xsl:choose>
      <xsl:when test="document/body/row">
       <table width="100%">
        <xsl:apply-templates select="document/body/*"/>
       </table>
      </xsl:when>
      <xsl:otherwise>
       <xsl:apply-templates select="document/body/*"/>
      </xsl:otherwise>
     </xsl:choose>
    </p>

    <p class="copyright">
      Copyright &#169; 1999-2005 <a href="http://www.apache.org/">The Apache Software Foundation</a>.
      All rights reserved.
    </p>
   </body>
  </html>
 </xsl:template>

 <xsl:template match="style">
  <link type="text/css" rel="stylesheet" href="{@href}"/>
 </xsl:template>
 
 <xsl:template match="script">
  <script type="text/javascript" src="{@href}"/>
 </xsl:template>
 
 <xsl:template match="tab">
  <a href="{@href}"><i><xsl:value-of select="@title"/></i></a>&#160;
 </xsl:template>
 
 <xsl:template match="row">
  <tr>
   <xsl:apply-templates select="column"/>
  </tr>
 </xsl:template>
 
 <xsl:template match="column">
  <td valign="top">
   <h4 class="samplesGroup"><xsl:value-of select="@title"/></h4>
   <p class="samplesText"><xsl:apply-templates/></p>
  </td> 
 </xsl:template>

 <xsl:template match="section">
  <xsl:choose> <!-- stupid test for the hierarchy depth -->
   <xsl:when test="../../../section">
    <h5><xsl:value-of select="title"/></h5>
   </xsl:when>
   <xsl:when test="../../section">
    <h4><xsl:value-of select="title"/></h4>
   </xsl:when>
   <xsl:when test="../section">
    <h4 class="samplesGroup"><xsl:value-of select="title"/></h4>
   </xsl:when>
  </xsl:choose>
  <p>
   <xsl:apply-templates select="*[name()!='title']"/>
  </p>
 </xsl:template>

 <xsl:template match="source">
  <div style="background: #b9d3ee; border: thin; border-color: black; border-style: solid; padding-left: 0.8em; 
              padding-right: 0.8em; padding-top: 0px; padding-bottom: 0px; margin: 0.5ex 0px; clear: both;">
  <pre>
   <xsl:value-of select="."/>
  </pre>
  </div>
 </xsl:template>
 
 <xsl:template match="link">
  <xsl:text> </xsl:text>
  <a href="{@href}">
   <xsl:apply-templates/>
  </a>
  <xsl:text> </xsl:text>
 </xsl:template>
 
 <xsl:template match="strong">
  <xsl:text> </xsl:text>
  <b>
   <xsl:apply-templates/>
  </b>
  <xsl:text> </xsl:text>
 </xsl:template>
 
 <xsl:template match="anchor">
  <a name="{@name}">
   <xsl:apply-templates/>
  </a>
 </xsl:template>
 
<!-- <xsl:template match="table">
  <table border="1" cellspacing="3" cellpadding="3">
   <xsl:apply-templates/>
  </table>
 </xsl:template> -->

  <xsl:template match="para">
   <p>
     <xsl:apply-templates/>
   </p>
  </xsl:template>

 <xsl:template match="*|@*|node()|text()" priority="-1">
  <xsl:copy><xsl:apply-templates select="*|@*|node()|text()"/></xsl:copy>
 </xsl:template>

</xsl:stylesheet>
