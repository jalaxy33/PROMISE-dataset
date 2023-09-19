<?xml version="1.0" encoding="UTF-8"?>
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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:preserve-space elements="*" />
  <!-- faq-v20x.dtd to faq-v12.dtd transformation -->

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="faqs">
    <faqs>
      <xsl:attribute name="title">
        <xsl:value-of select="title"/>
      </xsl:attribute>
      <xsl:apply-templates/>
    </faqs>
  </xsl:template>

  <xsl:template match="faqs/title"/>

  <xsl:template match="faqsection">
    <part>
      <xsl:apply-templates select="node()|@*"/>
    </part>
  </xsl:template>

  <xsl:template match="a">
    <link>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="node()"/>
    </link>
  </xsl:template>


  <!-- the obligatory copy-everything -->
  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
