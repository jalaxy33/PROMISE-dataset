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

<!--+
    | Add i18n tags so it can be processed.
    +-->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:i18n="http://apache.org/cocoon/i18n/2.1" >

  <xsl:import href="copyover.xsl"/>

  <xsl:template match="@label">
   <xsl:attribute name="i18n:attr">label</xsl:attribute>
   <xsl:attribute name="label">
     <xsl:value-of select="." />
   </xsl:attribute>
     <xsl:apply-templates />
  </xsl:template>
  
  <!-- FIXME: Need support for more than one attribute
  <xsl:template match="@description">
   <xsl:attribute name="i18n:attr">description</xsl:attribute>
   <xsl:attribute name="description">
     <xsl:value-of select="." />
   </xsl:attribute>
     <xsl:apply-templates />
  </xsl:template>
  -->

</xsl:stylesheet>
