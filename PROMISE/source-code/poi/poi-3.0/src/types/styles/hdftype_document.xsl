<?xml version="1.0"?>
<!--
   ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   ====================================================================
-->
<xsl:stylesheet version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:recutil="org.apache.poi.generator.RecordUtil"
   xmlns:field="org.apache.poi.generator.FieldIterator"
   xmlns:java="java" >

<xsl:template match="type">
<document>
    <header>
        <title><xsl:value-of select="@name"/> Documentation For HDF Type</title>
    </header>

    <body>
        <s1 title="Type Description">
            <p><xsl:value-of select="/type/description"/>
            </p>
        </s1>
        <s1 title="Fields">
            <table>
                <tr>
                    <th colspan="1" rowspan="1">Name</th>
                    <th colspan="1" rowspan="1">Size</th>
                    <th colspan="1" rowspan="1">Offset</th>
                    <th colspan="1" rowspan="1">Description</th>
                    <th colspan="1" rowspan="1">Default Value</th>
                </tr>
                <xsl:apply-templates select="//field"/>
            </table>
        </s1>
    </body>
    <footer>
        <legal>
          Copyright (c) @year@ The Poi Project All rights reserved.
          $Revision: 496533 $ $Date: 2007-01-15 23:04:42 +0000 (Mon, 15 Jan 2007) $
        </legal>
    </footer>

</document>
</xsl:template>

<xsl:template match="field">
    <tr>
        <td><xsl:value-of select="@name"/></td>
        <td><xsl:value-of select="@size"/></td>
        <td> </td>
        <td><xsl:value-of select="@description"/></td>
        <td><xsl:value-of select="@default"/></td>
    </tr>
</xsl:template>

</xsl:stylesheet>
