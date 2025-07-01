<!-- OSCARS website created by Scott McDonald -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" encoding="utf-8" indent="yes" />
  <xsl:variable name="definitions"
    select="document('../data/definitions.xml')/definitions" />
  <xsl:variable name="rootDir" select="concat('/', $definitions/@year, '/')" />
  <xsl:variable name="results"
    select="document(concat($rootDir, 'data/results.xml'))/results" />
  <xsl:variable name="mappings"
    select="document(concat($rootDir, 'data/mappings.xml'))/mappings" />
  <xsl:variable name="ballots"
    select="document(concat($rootDir, 'data/ballots.xml'))/ballots" />
  <xsl:variable name="inProgress" select="$results/awards[not(@END)]" />
  <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
  <xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz'" />
  <xsl:template name="header">
    <xsl:comment>OSCARS website created by Scott McDonald</xsl:comment>
    <head>
      <link rel="stylesheet" type="text/css" href="/oscars.css" />
      <meta http-equiv="cache-control"
        content="no-cache, no-store, must-revalidate" />
      <meta http-equiv="expires" content="0" />
      <meta http-equiv="pragma" content="no-cache" />
      <title><xsl:value-of select="$definitions/@year" /> OSCARS</title>
      <style>body {background-color:PaleGoldenrod}</style>
    </head>
    <header>
      <center>
        <a style="all:unset">
          <xsl:attribute name="href">
            <xsl:value-of select="$rootDir" />
          </xsl:attribute>
          <table id="header"
            style="border:2px solid black; color:PaleGoldenrod; background-image:url('https://lh7-us.googleusercontent.com/4FfYgO9yHPZmqBKDPhJL2Xw2v0-ZPAVOkW-3MRGsLOmSFmWv6gXi2Q5KLSNwSEaZtFYV6lmW5Cc7Sal-zNtOPNkHorAY8XSecbSf3V_sPlbbcsLLbwHBwjmZqQ3TlyRmHfWbAbUmVTrd63b3XOk6lHVLMbmYjw'); background-repeat:no-repeat; background-size:100%">
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
              <th style="background-color:transparent">
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
                <xsl:apply-templates
                  select="$definitions/column[@name = 'Best Picture']/nominee"
                  mode="poster">
                  <xsl:with-param name="category" select="'Best Picture'" />
                  <xsl:with-param name="width" select="'50'" />
                </xsl:apply-templates>
              </td>
            </tr>
          </table>
        </a>
        <br />
        <hr
          width="500" />
        <a>
          <xsl:attribute name="href">
            <xsl:value-of select="concat($rootDir, 'player')" />
          </xsl:attribute>
        PLAYERS</a> &#160;&#160;-&#160;&#160; <a>
          <xsl:attribute name="href">
            <xsl:value-of select="concat($rootDir, 'category')" />
          </xsl:attribute>
        CATEGORIES</a> &#160;&#160;-&#160;&#160; <a href="/history">HISTORY</a>
        <hr
          width="500" />
        <br />
        <br />
      </center>
    </header>
    <xsl:if test="$inProgress">
      <script>
        function updated(action) {
          const http = new XMLHttpRequest();
          http.onload = action;
          http.open("GET", <xsl:value-of select="$rootDir"/> + 
            "data/updated.txt?_=" + new Date().getTime());
          http.send();
        }

        updated(function() {
          const start = this.responseText;
          setInterval(function() {
            updated(function() {
              if (this.responseText !== start)
                window.location.reload();
            });
          }, 3000);
        });
      </script>
    </xsl:if>
  </xsl:template>
  <xsl:template name="footer">
    <footer>
      <center>
        <br />Last updated: <xsl:value-of select="$results/@updated" />
        <br />
        <br />
        <font
          color="gray">
          <i>Contest by Scott Takeda | Website by Scott McDonald</i>
        </font>
      </center>
    </footer>
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
    <xsl:param name="width" />
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
      <xsl:attribute name="width">
        <xsl:value-of select="$width" />
      </xsl:attribute>
    </img>
  </xsl:template>
  <xsl:template match="/results/standings/player" mode="playerLink">
    <xsl:value-of
      select="concat($rootDir, 'player/', @firstName, '_', @lastName, '.xml')" />
  </xsl:template>
  <xsl:template match="player" mode="playerName">
    <xsl:value-of select="@lastName" />
    <xsl:if test="@firstName != '' and @lastName != ''">
      <xsl:value-of select="', '" />
    </xsl:if>
    <xsl:value-of select="@firstName" />
  </xsl:template>
</xsl:stylesheet>