<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template name="init">
    <xsl:param name="results" />
    <head>
      <link rel="stylesheet" type="text/css" href="../../oscars.css" />
      <meta http-equiv="cache-control" content="no-cache" />
      <meta http-equiv="expires" content="0" />
      <meta http-equiv="pragma" content="no-cache" />
      <title>
        <xsl:value-of select="$results/title" />
      </title>
    </head>
  </xsl:template>
  <xsl:template name="header">
    <xsl:param name="results" />
    <table id="header">
      <tr>
        <td rowspan="2">
          <img src="../../trophy.png" id="trophy" />
        </td>
        <th>
          <xsl:value-of select="$results/title" />
        </th>
        <td rowspan="2">
          <img src="../../trophy.png" id="trophy" />
        </td>
      </tr>
      <tr>
        <td>(Unofficial Results)</td>
      </tr>
    </table>
    <br />
    <i>Tip: Use ctrl-alt-shift when clicking reload to force page to refresh.</i>
    <br />
    <br />
    <br />
  </xsl:template>
  <xsl:template name="chart">
    <xsl:param name="categoryResults" />
    <img>
      <xsl:attribute name="src">
        <xsl:variable name="chartValues">
          <xsl:for-each select="$categoryResults/nominees/nominee">
            <xsl:choose>
              <xsl:when test="./@status = 'correct'">
                <xsl:value-of select="'1'" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'0'" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </xsl:variable>
        <xsl:value-of select="concat($categoryResults/name, '_', $chartValues, '.png')" />
      </xsl:attribute>
    </img>
  </xsl:template>
</xsl:stylesheet>
