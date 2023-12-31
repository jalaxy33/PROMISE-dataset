<xsl:stylesheet version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:recutil="org.apache.poi.generator.RecordUtil"
   xmlns:field="org.apache.poi.generator.FieldIterator"
   xmlns:java="java" >

<xsl:template match="record">
<document>
    <header>
        <title><xsl:value-of select="@name"/> Record Documentation</title>
    </header>

    <body>
        <s1 title="Record Description">
            <p><xsl:value-of select="/record/description"/>
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
          $Revision: 1.1 $ $Date: 2002/02/10 04:32:07 $
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