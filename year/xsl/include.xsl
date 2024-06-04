<!-- OSCARS website created by Scott McDonald -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" encoding="utf-8" indent="yes" />
  <xsl:variable name="results" select="document('../results.xml')/results" />
  <xsl:variable name="categoryDefinitions"
    select="document('../categoryDefinitions.xml')/categories" />
  <xsl:variable name="categoryMaps"
    select="document('../categoryMaps.xml')/categories" />
  <xsl:variable name="categoryAll"
    select="document('../category/all.xml')/categories" />
  <xsl:template name="init">
    <xsl:comment>OSCARS website created by Scott McDonald</xsl:comment>
    <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
    <head>
      <link rel="stylesheet" type="text/css" href="../../oscars.css" />
      <meta http-equiv="cache-control"
        content="no-cache, no-store, must-revalidate" />
      <meta http-equiv="expires" content="0" />
      <meta http-equiv="pragma" content="no-cache" />
      <title>
        <xsl:value-of select="$results/@year" /> OSCARS </title>
      <style>body {background-color: PaleGoldenrod}</style>
    </head>
  </xsl:template>
  <xsl:template name="header">
    <a href="https://oscars.site44.com/{$results/@year}" style="all: unset">
      <table id="header"
        style="color:PaleGoldenrod; background-image: url('http://oscars.site44.com/RedCurtain.jpg'); background-repeat: no-repeat; background-size: 100%;">
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
            <xsl:value-of select="$results/@year" /> OSCARS </th>
        </tr>
        <tr>
          <td>
            <i>(Unofficial Results)</i>
            <br />
            <br />
          </td>
        </tr>
        <tr>
          <td colspan="5">
            <xsl:for-each
              select="$categoryDefinitions/category[@name = 'Best Picture']/nominee">
              <img width="50">
                <xsl:attribute name="src">
                  <xsl:value-of select="./@img" />
                </xsl:attribute>
                <xsl:attribute name="alt">
                  <xsl:value-of select="./@name" />
                </xsl:attribute>
                <xsl:attribute name="title">
                  <xsl:value-of select="./@name" />
                </xsl:attribute>
              </img>
            </xsl:for-each>
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
      <xsl:attribute name="alt">
        <xsl:value-of select="$categoryResults/@name" />
      </xsl:attribute>
      <xsl:attribute name="title">
        <xsl:value-of select="$categoryResults/@name" />
      </xsl:attribute>
    </img>
  </xsl:template>
  <xsl:template name="tieBreaker">
    <xsl:param name="tieBreaker" />
    <xsl:choose>
      <xsl:when test="$tieBreaker &gt;= 1 and $tieBreaker &lt;= 10">
        <xsl:value-of select="substring('➀➁➂➃➄➅➆➇➈➉', $tieBreaker, 1)" />
      </xsl:when>
      <xsl:when test="$tieBreaker">
        <xsl:value-of select="concat('(', $tieBreaker, ')')" />
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="updated">
    <table>
      <tr>
        <td id="rank"> Last updated: <xsl:value-of select="$results/@updated" />
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="status">
    <xsl:param name="nominee" />
    <xsl:param name="winners" />
    <xsl:attribute name="class">
      <xsl:choose>
        <xsl:when test="$winners/nominee/@name = $nominee">
          correct
        </xsl:when>
        <xsl:when test="$winners/nominee">
          incorrect
        </xsl:when>
        <xsl:otherwise>
          unannounced
        </xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>
  <xsl:template name="poster">
    <xsl:param name="category" />
    <xsl:param name="nominee" />
    <img>
      <xsl:attribute name="src">
        <xsl:value-of
          select="$categoryDefinitions/category[@name = $category]/nominee[@name = $nominee]/@img" />
      </xsl:attribute>
      <xsl:attribute name="alt">
        <xsl:value-of select="$nominee" />
      </xsl:attribute>
      <xsl:variable name="description"
        select="$categoryMaps/category[@name = $category]/map[@website = $nominee][last()]/@ballot" />
      <xsl:attribute name="title">
        <xsl:value-of select="$description" />
        <xsl:if test="not($description)">
          <xsl:value-of select="$nominee" />
        </xsl:if>
      </xsl:attribute>
    </img>
  </xsl:template>
  <xsl:template name="playerLink">
    <xsl:param name="player" />
    <a>
      <xsl:attribute name="href">
        <xsl:value-of select="concat('../player/', $player/@webPage)" />
      </xsl:attribute>
      <xsl:value-of select="$player/@lastName" />
      <xsl:if test="$player/@firstName and $player/@lastName">
        <xsl:value-of select="', '" />
      </xsl:if>
      <xsl:value-of select="$player/@firstName" />
    </a>
  </xsl:template>
</xsl:stylesheet>