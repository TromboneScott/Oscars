<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" encoding="utf-8" indent="yes" />
  <xsl:include href="header.xsl" />
  <xsl:template match="/categories">
    <xsl:comment>OSCARS website created by Scott McDonald</xsl:comment>
    <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
    <html>
      <xsl:variable name="results" select="document('../results.xml')/results" />
      <xsl:call-template name="init">
        <xsl:with-param name="results" select="$results" />
      </xsl:call-template>
      <body>
        <center>
          <xsl:call-template name="header">
            <xsl:with-param name="results" select="$results" />
          </xsl:call-template>
          <div id="name">
            Category Definitions
          </div>
          <xsl:variable name="mapping" select="document('../categoryMaps.xml')/categories" />
          <xsl:for-each select="category">
            <br />
            <br />
            <hr />
            <br />
            <b><xsl:value-of select="@name" /></b>
            <br />
            <xsl:variable name="name" select="@name" />
            <xsl:value-of select="$mapping/category[@name=$name]/@ballot" />
            <xsl:if test="nominee">
              <table>
                <tbody>
                  <xsl:for-each select="nominee">
                    <tr>
                      <td>
                        <xsl:call-template name="poster">
                          <xsl:with-param name="category" select="$name" />
                          <xsl:with-param name="nominee" select="@name" />
                        </xsl:call-template>
                      </td>
                      <td>
                        <b><xsl:value-of select="@name" /></b>
                        <xsl:variable name="website" select="@name" />
                        <xsl:for-each select="$mapping/category[@name=$name]/map[@website=$website]">
                          <br />
                          <xsl:value-of select="@ballot" />
                        </xsl:for-each>
                      </td>
                    </tr>
                  </xsl:for-each>
                </tbody>
              </table>
            </xsl:if>
          </xsl:for-each>
          <br />
          <br />
          <hr />
        </center>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>