<?xml version="1.0" encoding="utf-8"?>
<!-- OSCARS website created by Scott McDonald -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/winners">
    <html>
      <xsl:comment>OSCARS website created by Scott McDonald</xsl:comment>
      <head>
        <link rel="stylesheet" type="text/css" href="/oscars.css" />
        <title>Oscars History</title>
        <style>body {background-color: PaleGoldenrod}</style>
      </head>
      <body>
        <center>
          <a href="/" style="display: table; text-decoration: none; color: inherit">
            <table id="header"
              style="border: 2px solid black; color: PaleGoldenrod; background-image: url('http://oscars.site44.com/RedCurtain.jpg'); background-repeat: no-repeat; background-size: 100%">
              <tr>
                <td rowspan="3" />
                <td rowspan="3">
                  <img src="http://oscars.site44.com/trophy.png" id="trophy" />
                </td>
                <td>
                  <br />
                </td>
                <td rowspan="3">
                  <img src="http://oscars.site44.com/trophy.png" id="trophy" />
                </td>
                <td rowspan="3" />
              </tr>
              <tr>
                <th style="background-color: transparent">
                  Oscars History
                </th>
              </tr>
              <tr>
                <td>
                  <i>Established in 1996</i>
                  <br />
                  <br />
                </td>
              </tr>
            </table>
          </a>
          <br />
          <br />
          <h1>Contest Winners</h1>
          <table>
            <tr>
              <th class="header">Year</th>
              <th>Winner</th>
              <th>Correct</th>
              <th>Players</th>
            </tr>
            <xsl:for-each select="year">
              <tr>
                <td class="header">
                  <xsl:choose>
                    <xsl:when test="@year &gt;= 2000">
                      <a>
                        <xsl:attribute name="href">
                          <xsl:value-of select="concat('/', @year)" />
                        </xsl:attribute>
                        <xsl:value-of select="@year" />
                      </a>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="@year" />
                    </xsl:otherwise>
                  </xsl:choose>
                </td>
                <xsl:choose>
                  <xsl:when test="count(winner) = 0">
                    <xsl:variable name="players"
                      select="count(document(concat('/', @year, '/data/results.xml'))/results/standings/player)" />
                    <td style="background-color: silver" id="selection">
                      <xsl:attribute name="colspan">
                        <xsl:value-of select="floor(1 div ($players + 1)) + 2" />
                      </xsl:attribute>
                      <a>
                        <xsl:attribute name="href">
                          <xsl:value-of select="concat('/', @year)" />
                        </xsl:attribute>
                        <i>To Be Determined</i>
                      </a>
                    </td>
                    <xsl:if test="$players &gt; 0">
                      <td style="background-color: silver" id="selection">
                        <xsl:value-of select="$players" />
                      </td>
                    </xsl:if>
                  </xsl:when>
                  <xsl:otherwise>
                    <td>
                      <table style="border:0px">
                        <tr>
                          <xsl:if test="count(winner) &gt; 1">
                            <td
                              style="border:0px; padding:0px; padding-right:6px">
                              Tie:
                            </td>
                          </xsl:if>
                          <td style="border:0px; padding:0px">
                            <xsl:for-each select="winner">
                              <xsl:choose>
                                <xsl:when test="@link">
                                  <a>
                                    <xsl:attribute name="href">
                                      <xsl:value-of select="@link" />
                                    </xsl:attribute>
                                    <xsl:value-of select="@name" />
                                  </a>
                                </xsl:when>
                                <xsl:otherwise>
                                  <xsl:value-of select="@name" />
                                </xsl:otherwise>
                              </xsl:choose>
                              <xsl:if test="position() != last()">
                                <br />
                              </xsl:if>
                            </xsl:for-each>
                          </td>
                        </tr>
                      </table>
                    </td>
                    <xsl:choose>
                      <xsl:when test="@correct">
                        <td style="background-color: silver" id="selection">
                          <xsl:value-of select="@correct" />
                        </td>
                        <td style="background-color: silver" id="selection">
                          <xsl:value-of select="@players" />
                        </td>
                      </xsl:when>
                      <xsl:otherwise>
                        <td style="background-color: silver" id="selection"
                          colspan="2">
                          <i>Unavailable</i>
                        </td>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:otherwise>
                </xsl:choose>
              </tr>
            </xsl:for-each>
          </table>
          <br />
          <font color="gray">
            <i>Contest by Scott Takeda | Website by Scott McDonald</i>
          </font>
        </center>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>