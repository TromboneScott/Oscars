<?xml version="1.0" encoding="utf-8"?>
<!-- OSCARS website created by Scott McDonald -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:include href="include.xsl" />
  <xsl:template match="/definitions">
    <html>
      <xsl:call-template name="header" />
      <body>
        <center>
          <div id="name">Definitions</div>
          <br />
          <h2>Tie Breakers</h2>
          <table>
            <tbody>
              <tr>
                <th class="header">#</th>
                <th>Category</th>
              </tr>
              <xsl:for-each select="column[@tieBreaker]">
                <xsl:sort select="@tieBreaker" />
                <tr>
                  <td class="header">
                    <xsl:value-of select="@tieBreaker" />
                  </td>
                  <td class="unannounced">
                    <xsl:value-of select="@name" />
                  </td>
                </tr>
              </xsl:for-each>
            </tbody>
          </table>
          <br />
          <br />
          <br />
          <h2 style="display: inline">Columns</h2>
          <br />
          <i>Values for this website are in <b>bold</b></i>
          <xsl:for-each select="column">
            <br />
            <br />
            <xsl:variable name="column" select="@name" />
            <table style="table-layout: fixed; width: 500px">
              <thead>
                <tr style="visibility: collapse">
                  <td style="width: 100px" />
                  <td />
                </tr>
                <tr>
                  <td colspan="2"
                    style="background-color: white; text-align: center">
                    <b>
                      <xsl:value-of select="@name" />
                    </b>
                  </td>
                </tr>
                <xsl:if test="$mappings/column[@name = $column]">
                  <tr>
                    <td colspan="2"
                      style="background-color: white; text-align: center; font-weight: normal; white-space: normal">
                      <xsl:value-of
                        select="$mappings/column[@name = $column]/@ballot" />
                    </td>
                  </tr>
                </xsl:if>
              </thead>
              <tbody>
                <xsl:for-each select="nominee">
                  <xsl:variable name="name" select="@name" />
                  <tr class="unannounced" style="border-top: 2pt solid black">
                    <td class="header" style="width: 100px">
                      <xsl:attribute name="rowspan">
                        <xsl:value-of
                          select="count($mappings/column[@name = $column]/nominee[@name = $name]) + 1" />
                      </xsl:attribute>
                      <xsl:apply-templates select="." mode="poster">
                        <xsl:with-param name="category" select="$column" />
                      </xsl:apply-templates>
                    </td>
                    <td>
                      <center>
                        <b>
                          <xsl:value-of select="@name" />
                        </b>
                      </center>
                    </td>
                  </tr>
                  <xsl:for-each
                    select="$mappings/column[@name = $column]/nominee[@name = $name]">
                    <tr class="unannounced">
                      <td style="white-space: normal">
                        <xsl:value-of select="@ballot" />
                      </td>
                    </tr>
                  </xsl:for-each>
                </xsl:for-each>
              </tbody>
            </table>
          </xsl:for-each>
        </center>
      </body>
      <xsl:call-template name="footer" />
    </html>
  </xsl:template>
</xsl:stylesheet>