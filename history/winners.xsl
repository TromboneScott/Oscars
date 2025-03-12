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
          <a href="/" style="all: unset">
            <table id="header"
              style="color:PaleGoldenrod; background-image: url('https://lh7-us.googleusercontent.com/4FfYgO9yHPZmqBKDPhJL2Xw2v0-ZPAVOkW-3MRGsLOmSFmWv6gXi2Q5KLSNwSEaZtFYV6lmW5Cc7Sal-zNtOPNkHorAY8XSecbSf3V_sPlbbcsLLbwHBwjmZqQ3TlyRmHfWbAbUmVTrd63b3XOk6lHVLMbmYjw'); background-repeat: no-repeat; background-size: 100%;">
              <tr>
                <td rowspan="3" />
                <td rowspan="3">
                  <img
                    src="https://lh7-us.googleusercontent.com/AsEK7mCWIBy7kUCEa01rhbohDBT_k4Xi2cPJtKD6dswxWz_zzGDhbYkNW-M3H8xcgcsIfNi7fn4-v5Arkom1RNV7dxxku0Im464ohRXq7aHSj9ktCHK1tRNh2nkVUlTRDCMjZcaEcmsgVpyvTJdi4ahKLMOoAw"
                    id="trophy" />
                </td>
                <td>
                  <br />
                </td>
                <td rowspan="3">
                  <img
                    src="https://lh7-us.googleusercontent.com/AsEK7mCWIBy7kUCEa01rhbohDBT_k4Xi2cPJtKD6dswxWz_zzGDhbYkNW-M3H8xcgcsIfNi7fn4-v5Arkom1RNV7dxxku0Im464ohRXq7aHSj9ktCHK1tRNh2nkVUlTRDCMjZcaEcmsgVpyvTJdi4ahKLMOoAw"
                    id="trophy" />
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
                    <xsl:when test="@name &gt;= 2000">
                      <a>
                        <xsl:attribute name="href">
                          <xsl:value-of select="concat('/', @name)" />
                        </xsl:attribute>
                        <xsl:value-of select="@name" />
                      </a>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="@name" />
                    </xsl:otherwise>
                  </xsl:choose>
                </td>
                <xsl:choose>
                  <xsl:when test="count(winner) = 0">
                    <td style="background-color: silver" id="selection"
                      colspan="3">
                      <a>
                        <xsl:attribute name="href">
                          <xsl:value-of select="concat('/', @name)" />
                        </xsl:attribute>
                        <i>To Be Determined</i>
                      </a>
                    </td>
                  </xsl:when>
                  <xsl:otherwise>
                    <td>
                      <xsl:if test="count(winner) &gt; 1">
                        Tie:
                      </xsl:if>
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
                          <xsl:value-of select="', '" />
                        </xsl:if>
                      </xsl:for-each>
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