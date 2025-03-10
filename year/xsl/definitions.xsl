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
                <th>#</th>
                <th>Category</th>
              </tr>
              <xsl:for-each select="column[@tieBreaker]">
                <xsl:sort select="@tieBreaker" />
                <tr>
                  <td style="background-color:white">
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
          <h2 style="display:inline">Columns</h2>
          <br />
          <i>Values for this website are in <b>bold</b></i>
          <xsl:for-each select="column">
            <br />
            <br />
            <xsl:variable name="column" select="@name" />
            <table style="table-layout: fixed; width: 500px">
              <thead>
                <tr style="visibility:collapse">
                  <td style="width:100px" />
                  <td />
                </tr>
                <tr>
                  <th colspan="2" style="font-weight:normal; white-space:normal">
                    <b>
                      <xsl:value-of select="@name" />
                    </b>
                    <hr />
                    <xsl:value-of
                      select="$mappings/column[@name = $column]/@ballot" />
                  </th>
                </tr>
              </thead>
              <tbody>
                <xsl:for-each select="nominee">
                  <tr class="unannounced">
                    <td style="width:100px">
                      <xsl:apply-templates select="." mode="poster">
                        <xsl:with-param name="category" select="$column" />
                      </xsl:apply-templates>
                    </td>
                    <td style="white-space:normal">
                      <center>
                        <b>
                          <xsl:value-of select="@name" />
                        </b>
                      </center>
                      <xsl:variable name="name" select="@name" />
                      <xsl:for-each
                        select="$mappings/column[@name = $column]/nominee[@name = $name]">
                        <hr />
                        <xsl:value-of select="@ballot" />
                      </xsl:for-each>
                    </td>
                  </tr>
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