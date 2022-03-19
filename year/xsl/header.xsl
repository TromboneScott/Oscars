<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template name="init">
    <xsl:param name="results" />
    <xsl:comment>OSCARS website created by Scott McDonald</xsl:comment>
    <head>
      <link rel="stylesheet" type="text/css" href="../../oscars.css" />
      <meta http-equiv="cache-control" content="no-cache" />
      <meta http-equiv="expires" content="0" />
      <meta http-equiv="pragma" content="no-cache" />
      <xsl:if test="$results/refresh > 0">
        <meta http-equiv="refresh" >
          <xsl:attribute name="content">
            <xsl:choose>
              <xsl:when test="$results/refresh > 30">
                <xsl:value-of select="$results/refresh" />
              </xsl:when>
              <xsl:otherwise>
                30
              </xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
        </meta>
      </xsl:if>
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
    <xsl:if test="not(string($results/showTime/end))">
      <br />
      <i>Refresh this page to get updated results as winners are announced.</i>
      <br />
      <br />
      <button onClick="window.location.reload();">&#10227; Refresh &#10227;</button>
    </xsl:if>
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
  <xsl:template name="tieBreaker">
    <xsl:param name="tieBreaker" />
    <xsl:choose>
      <xsl:when test="$tieBreaker=1">
        <xsl:value-of select="'&#10112;'" />
      </xsl:when>
      <xsl:when test="$tieBreaker=2">
        <xsl:value-of select="'&#10113;'" />
      </xsl:when>
      <xsl:when test="$tieBreaker=3">
        <xsl:value-of select="'&#10114;'" />
      </xsl:when>
      <xsl:when test="$tieBreaker=4">
        <xsl:value-of select="'&#10115;'" />
      </xsl:when>
      <xsl:when test="$tieBreaker=5">
        <xsl:value-of select="'&#10116;'" />
      </xsl:when>
      <xsl:when test="$tieBreaker=6">
        <xsl:value-of select="'&#10117;'" />
      </xsl:when>
      <xsl:when test="$tieBreaker=7">
        <xsl:value-of select="'&#10118;'" />
      </xsl:when>
      <xsl:when test="$tieBreaker=8">
        <xsl:value-of select="'&#10119;'" />
      </xsl:when>
      <xsl:when test="$tieBreaker=9">
        <xsl:value-of select="'&#10120;'" />
      </xsl:when>
      <xsl:when test="$tieBreaker=10">
        <xsl:value-of select="'&#10121;'" />
      </xsl:when>
      <xsl:when test="$tieBreaker != ''">
        <xsl:value-of select="concat('(', $tieBreaker, ')')" />
      </xsl:when>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
