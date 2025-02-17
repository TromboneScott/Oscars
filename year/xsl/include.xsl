<!-- OSCARS website created by Scott McDonald -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" encoding="utf-8" indent="yes" />
  <xsl:variable name="results" select="document('../data/results.xml')/results" />
  <xsl:variable name="definitions"
    select="document('../data/definitions.xml')/definitions" />
  <xsl:variable name="mappings"
    select="document('../data/mappings.xml')/mappings" />
  <xsl:variable name="ballots" select="document('../data/ballots.xml')/ballots" />
  <xsl:variable name="inProgress"
    select="$results/awards[not(@end)] or $results/awards/category[not(nominee)]" />
  <xsl:template name="init">
    <xsl:comment>OSCARS website created by Scott McDonald</xsl:comment>
    <head>
      <link rel="stylesheet" type="text/css" href="../../oscars.css" />
      <meta http-equiv="cache-control"
        content="no-cache, no-store, must-revalidate" />
      <meta http-equiv="expires" content="0" />
      <meta http-equiv="pragma" content="no-cache" />
      <title><xsl:value-of select="$definitions/@year" /> OSCARS</title>
      <style>body {background-color: PaleGoldenrod}</style>
    </head>
  </xsl:template>
  <xsl:template name="header">
    <a href="https://oscars.site44.com/{$definitions/@year}" style="all: unset">
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
            <xsl:value-of select="$definitions/@year" /> OSCARS</th>
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
              select="$definitions/column[@name = 'Best Picture']/nominee">
              <img width="50">
                <xsl:attribute name="src">
                  <xsl:value-of select="@img" />
                </xsl:attribute>
                <xsl:attribute name="alt">
                  <xsl:value-of select="@name" />
                </xsl:attribute>
                <xsl:attribute name="title">
                  <xsl:value-of select="@name" />
                </xsl:attribute>
              </img>
            </xsl:for-each>
          </td>
        </tr>
      </table>
    </a>
    <br />
    <hr
      width="500" />
    <a href="https://oscars.site44.com/{$definitions/@year}">
    PLAYERS</a> &#160;&#160;-&#160;&#160; <a
      href="https://oscars.site44.com/{$definitions/@year}/category/all.xml">
    CATEGORIES</a> &#160;&#160;-&#160;&#160; <a
      href="https://oscars.site44.com/history">
      HISTORY</a>
    <hr width="500" />
    <xsl:if test="$inProgress">
      <i>Refresh this page to get updated results during the contest.</i>
      <br />
      <button onClick="window.location.reload();">&#10227; Refresh &#10227;</button>
      <br />
    </xsl:if>
    <br />
    <br />
  </xsl:template>
  <xsl:template match="/definitions/column" mode="value">
    <xsl:if test="@tieBreaker">
      <xsl:value-of select="'1.'" />
      <xsl:call-template name="tieBreakerZeros">
        <xsl:with-param name="decimals" select="@tieBreaker" />
      </xsl:call-template>
    </xsl:if>
    <xsl:value-of select="'1'" />
  </xsl:template>
  <xsl:template name="tieBreakerZeros">
    <xsl:param name="decimals" />
    <xsl:if test="$decimals &gt; 1">
      <xsl:value-of select="'0'" />
      <xsl:call-template name="tieBreakerZeros">
        <xsl:with-param name="decimals" select="$decimals - 1" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  <xsl:template match="/definitions/column" mode="tieBreaker">
    <xsl:choose>
      <xsl:when test="@tieBreaker &gt;= 1 and @tieBreaker &lt;= 10">
        <xsl:value-of select="substring('➀➁➂➃➄➅➆➇➈➉', @tieBreaker, 1)" />
      </xsl:when>
      <xsl:when test="@tieBreaker">
        <xsl:value-of select="concat('(', @tieBreaker, ')')" />
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="updated">
    <table>
      <tr>
        <td id="rank">Last updated: <xsl:value-of select="$results/@updated" /></td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template name="credits">
    <br />
    <i>Contest by: Scott Takeda | Website by: Scott McDonald</i>
  </xsl:template>
  <xsl:template match="/results/awards/category" mode="attribute">
    <xsl:param name="nominee" />
    <xsl:attribute name="class">
      <xsl:choose>
        <xsl:when test="nominee/@name = $nominee">
          correct
        </xsl:when>
        <xsl:when test="nominee">
          incorrect
        </xsl:when>
        <xsl:otherwise>
          unannounced
        </xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>
  <xsl:template match="nominee" mode="poster">
    <xsl:param name="category" />
    <xsl:variable name="nominee" select="@name" />
    <img>
      <xsl:attribute name="src">
        <xsl:value-of
          select="$definitions/column[@name = $category]/nominee[@name = $nominee]/@img" />
      </xsl:attribute>
      <xsl:attribute name="alt">
        <xsl:value-of select="$nominee" />
      </xsl:attribute>
      <xsl:variable name="description"
        select="$mappings/column[@name = $category]/nominee[@name = $nominee][last()]/@ballot" />
      <xsl:attribute name="title">
        <xsl:value-of select="$description" />
        <xsl:if test="not($description)">
          <xsl:value-of select="$nominee" />
        </xsl:if>
      </xsl:attribute>
    </img>
  </xsl:template>
  <xsl:template match="/results/standings/player" mode="playerLink">
    <a>
      <xsl:attribute name="href">
        <xsl:value-of
          select="concat('../player/', @firstName, '_', @lastName, '.xml')" />
      </xsl:attribute>
      <xsl:value-of select="@lastName" />
      <xsl:if test="@firstName and @lastName">
        <xsl:value-of select="', '" />
      </xsl:if>
      <xsl:value-of select="@firstName" />
    </a>
  </xsl:template>
</xsl:stylesheet>