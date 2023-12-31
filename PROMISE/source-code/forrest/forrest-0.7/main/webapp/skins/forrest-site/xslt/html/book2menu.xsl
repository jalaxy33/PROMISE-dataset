<?xml version="1.0"?>
<!--
  Copyright 2002-2004 The Apache Software Foundation or its licensors,
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
<!--
book2menu.xsl generates the HTML menu.  See the imported book2menu.xsl for
details.
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:import href="../../../common/xslt/html/book2menu.xsl"/>

  <xsl:template match="book">
    <div class="menu">
      <ul>
        <xsl:apply-templates select="menu"/>
      </ul>
    </div>
  </xsl:template>

  <xsl:template match="menu">
    <li>
      <font color="#CFDCED"><xsl:value-of select="@label"/></font>
      <ul>
        <xsl:apply-templates/>
      </ul>
    </li>
  </xsl:template>

  <xsl:template match="menu-item[@type='hidden']"/>

  <xsl:template match="menu-item">
    <li>
      <xsl:apply-imports/>
    </li>
  </xsl:template>

  <xsl:template name="selected">
    <span class="sel">
      <font color="#ffcc00">
        <xsl:value-of select="@label"/>
      </font>
    </span>
  </xsl:template>

  <xsl:template name="print-external">
    <font color="#ffcc00">
      <xsl:apply-imports/>
    </font>
  </xsl:template>

</xsl:stylesheet>
