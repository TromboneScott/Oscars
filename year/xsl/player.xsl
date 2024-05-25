<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" encoding="utf-8" indent="yes" />
  <xsl:include href="sort.xsl" />
  <xsl:template match="/player">
    <xsl:comment>OSCARS website created by Scott McDonald</xsl:comment>
    <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
    <html>
      <xsl:variable name="player" select="." />
      <xsl:variable name="categories"
        select="document('../category/all.xml')/categories" />
      <xsl:variable name="results" select="document('../results.xml')/results" />
      <xsl:call-template name="init">
        <xsl:with-param name="results" select="$results" />
      </xsl:call-template>
      <body>
        <center>
          <xsl:call-template name="header">
            <xsl:with-param name="results" select="$results" />
          </xsl:call-template>
          <xsl:variable
            name="playerResults"
            select="$results/players/player[@firstName = $player/@firstName and @lastName = $player/@lastName]" />
          <xsl:variable
            name="playerName" select="concat(@firstName, ' ', @lastName)" />
          <div
            id="name">
            <xsl:value-of select="$playerName" />
          </div>
          <br />
          <table>
            <tr>
              <td id="rank"> Rank <div id="rank">
                  <xsl:value-of select="$playerResults/rank" />
                </div>
          Out of <xsl:value-of select="count($results/players/player)" />
              </td>
              <td class="header">
                <img>
                  <xsl:attribute name="src">
                    <xsl:value-of
                      select="concat('../rank/', $playerResults/rank/@chart)" />
                  </xsl:attribute>
                  <xsl:attribute name="alt">
                    <xsl:value-of select="$playerResults/rank" /> out of <xsl:value-of
                      select="count($results/players/player)" />
                  </xsl:attribute>
                  <xsl:attribute name="title">
                    <xsl:value-of select="$playerResults/rank" /> out of <xsl:value-of
                      select="count($results/players/player)" />
                  </xsl:attribute>
                </img>
              </td>
            </tr>
          </table>
          <xsl:if
            test="not(string($results/showTime/end))">
            <br /> Best Possible Rank (BPR): <xsl:value-of
              select="$playerResults/bpr" />
            <br /> Worst Possible Rank (WPR): <xsl:value-of
              select="$playerResults/wpr" />
          </xsl:if>
          <br />
          <br />
          <h3>
          Guesses</h3>
          <table>
            <thead>
              <tr>
                <th class="header">
                  <a href="../category/all.xml">Category</a>
                </th>
                <th>Guess</th>
                <th>Actual</th>
                <th>Score</th>
              </tr>
            </thead>
            <tbody>
              <xsl:for-each select="$results/categories/category">
                <xsl:variable name="categoryName" select="@name" />
                <xsl:variable name="categoryData"
                  select="$categories/category[@name = $categoryName]" />
                <xsl:variable name="playerGuess"
                  select="$categoryData/player[@firstName = $player/@firstName and @lastName = $player/@lastName]/@guess" />
                <tr>
                  <xsl:call-template name="status">
                    <xsl:with-param name="nominee" select="$playerGuess" />
                    <xsl:with-param name="winners" select="winners" />
                  </xsl:call-template>
                  <td class="header">
                    <a>
                      <xsl:attribute name="href">
                        <xsl:value-of select="concat('../category/', @webPage)" />
                      </xsl:attribute>
                      <xsl:value-of select="$categoryName" />
                    </a>
                    <xsl:call-template name="tieBreaker">
                      <xsl:with-param name="tieBreaker"
                        select="document('../categoryDefinitions.xml')/categories/category[@name = $categoryName]/@tieBreaker" />
                    </xsl:call-template>
                  </td>
                  <td>
                    <xsl:value-of select="$playerGuess" />
                  </td>
                  <td>
                    <xsl:variable name="tempWinners">
                      <xsl:for-each select="winners/nominee">
                        <xsl:value-of select="', '" />
                        <xsl:value-of select="@name" />
                      </xsl:for-each>
                    </xsl:variable>
                    <xsl:value-of select="substring-after($tempWinners, ', ')" />
                  </td>
                  <td>
                    <xsl:if test="winners/nominee">
                      <xsl:choose>
                        <xsl:when test="winners/nominee[@name = $playerGuess]">
                          <xsl:value-of select="$categoryData/@value" />
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:value-of
                            select="translate($categoryData/@value, '1', '0')" />
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:if>
                  </td>
                </tr>
              </xsl:for-each>
            </tbody>
            <tfoot>
              <tr>
                <th class="header">Total</th>
                <th>
                  <xsl:value-of select="floor($playerResults/score)" />
                </th>
                <th>
                  <xsl:value-of
                    select="count($results/categories/category/winners[count(nominee) &gt; 0])" />
                </th>
                <th>
                  <xsl:value-of select="$playerResults/score" />
                </th>
              </tr>
              <tr>
                <xsl:attribute name="class">
                  <xsl:value-of select="$playerResults/time/@status" />
                </xsl:attribute>
                <td class="header">
                  <xsl:value-of select="'Show Running Time'" />
                  <xsl:call-template name="tieBreaker">
                    <xsl:with-param name="tieBreaker"
                      select="document('../categoryDefinitions.xml')/categories/category[@name = 'Time']/@tieBreaker" />
                  </xsl:call-template>
                </td>
                <td>
                  <center>
                    <xsl:value-of select="$playerResults/time" />
                  </center>
                </td>
                <td>
                  <center>
                    <xsl:value-of select="$results/showTime/length" />
                  </center>
                </td>
                <td>
                  <center>
                    <xsl:value-of select="$playerResults/time/@delta" />
                  </center>
                </td>
              </tr>
            </tfoot>
          </table>
          <br />
          <br />
          <h3>
          Rankings</h3> Names in green can no longer pass or be passed by <xsl:value-of
            select="$playerName" />
          <br />
          <br />
          <xsl:call-template
            name="player-table">
            <xsl:with-param name="results" select="$results" />
            <xsl:with-param name="sort" select="'rank'" />
            <xsl:with-param name="inPlayer" select="$playerResults" />
          </xsl:call-template>
          <br />
          <a
            href="../category/all.xml" id="return">All Categories</a>
          <br />
          <br />
          <a
            href=".." id="return">Return to Main Page</a>
          <br />
          <br />
          <xsl:call-template
            name="updated">
            <xsl:with-param name="results" select="$results" />
          </xsl:call-template>
        </center>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>