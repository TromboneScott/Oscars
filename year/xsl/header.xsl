<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template name="init">
    <xsl:param name="results" />
    <xsl:comment>OSCARS website created by Scott McDonald</xsl:comment>
    <head>
      <link rel="stylesheet" type="text/css" href="../../oscars.css" />
      <meta http-equiv="cache-control" content="no-cache, no-store, must-revalidate" />
      <meta http-equiv="expires" content="0" />
      <meta http-equiv="pragma" content="no-cache" />
      <title>
        <xsl:value-of select="$results/year" />
        OSCARS
      </title>
      <style>body {background-color: PaleGoldenrod}</style>
    </head>
  </xsl:template>
  <xsl:template name="header">
    <xsl:param name="results" />
    <a href="https://oscars.site44.com/{$results/year}" style="all: unset">
      <table id="header" style="color:PaleGoldenrod; background-image: url('http://oscars.site44.com/RedCurtain.jpg'); background-repeat: no-repeat; background-size: 100%;">
        <tr>
          <td rowspan="3" />
          <td rowspan="3">
            <img src="http://oscars.site44.com/trophy1.png" id="trophy" />
          </td>
          <td>
            <br />
          </td>
          <td rowspan="3">
            <img src="http://oscars.site44.com/trophy1.png" id="trophy" />
          </td>
          <td rowspan="3" />
        </tr>
        <tr>
          <th style="background-color: transparent">
            <xsl:value-of select="$results/year" />
            OSCARS
          </th>
        </tr>
        <tr>
          <td>
            <i>(Unofficial Results)</i>
            <br />
            <br />
          </td>
        </tr>
      </table>
    </a>
    <xsl:if test="not(string($results/showTime/end))">
      <br />
      <i>Refresh this page to get updated results during the contest.</i>
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
        <xsl:value-of select="$categoryResults/@chart" />
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
  <xsl:template name="updated">
    <xsl:param name="results" />
    <table>
      <tr>
        <td id="rank">
          Last updated:
          <xsl:value-of select="$results/updated" />
        </td>
      </tr>
    </table>
  </xsl:template>
</xsl:stylesheet>