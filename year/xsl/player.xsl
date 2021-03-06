<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:include href="sort.xsl" />
  <xsl:template match="/player">
    <html>
      <xsl:variable name="player" select="." />
      <xsl:variable name="categories" select="document('../category/all.xml')/categories" />
      <xsl:variable name="results" select="document('../results.xml')/results" />
      <xsl:call-template name="init">
        <xsl:with-param name="results" select="$results" />
      </xsl:call-template>
      <body>
        <center>
          <xsl:call-template name="header">
            <xsl:with-param name="results" select="$results" />
          </xsl:call-template>
          <xsl:variable name="playerResults" select="$results/players/player[firstName = $player/firstName and lastName = $player/lastName]" />
          <xsl:variable name="playerName" select="concat(firstName, ' ', lastName)" />
          <div id="name">
            <xsl:value-of select="$playerName" />
          </div>
          <br />
          <table>
            <tr>
              <td id="rank">
                Rank
                <div id="rank">
                  <xsl:value-of select="$playerResults/rank" />
                </div>
                Out of
                <xsl:value-of select="count($results/players/player)" />
              </td>
              <td>
                <img>
                  <xsl:attribute name="src">
                    <xsl:value-of select="concat('../rank/rank_', $playerResults/rank, '.png')" />
                  </xsl:attribute>
                </img>
              </td>
            </tr>
          </table>
          <xsl:if test="not(string($results/showTime/end))">
            <br />
            Best Possible Rank:
            <xsl:value-of select="$playerResults/bpr" />
            <br />
            Worst Possible Rank:
            <xsl:value-of select="$playerResults/wpr" />
          </xsl:if>
          <br />
          <br />
          <h3>Guesses</h3>
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
                <xsl:variable name="categoryName" select="name" />
                <xsl:variable name="categoryData" select="$categories/category[name = $categoryName]" />
                <xsl:variable name="playerGuess" select="$categoryData/players/player[firstName = $player/firstName and lastName = $player/lastName]/guess" />
                <xsl:variable name="winners">
                  <xsl:for-each select="nominees/nominee[./@status = 'correct']">
                    <xsl:value-of select="concat('|', ., '|')" />
                  </xsl:for-each>
                </xsl:variable>
                <tr>
                  <xsl:attribute name="class">
                    <xsl:value-of select="nominees/nominee[. = $playerGuess]/@status" />
                  </xsl:attribute>
                  <td class="header">
                    <a>
                      <xsl:attribute name="href">
                        <xsl:value-of select="concat('../category/', $categoryName, '.xml')" />
                      </xsl:attribute>
                      <xsl:value-of select="$categoryName" />
                      <xsl:if test="$categoryData/tieBreaker != ''">
                        <xsl:value-of select="concat(' (', $categoryData/tieBreaker, ')')" />
                      </xsl:if>
                    </a>
                  </td>
                  <td>
                    <xsl:value-of select="$playerGuess" />
                  </td>
                  <td>
                    <xsl:variable name="tempWinners">
                      <xsl:for-each select="nominees/nominee[./@status = 'correct']">
                        <xsl:value-of select="', '" />
                        <xsl:value-of select="." />
                      </xsl:for-each>
                    </xsl:variable>
                    <xsl:value-of select="substring-after($tempWinners, ', ')" />
                  </td>
                  <td>
                    <xsl:if test="$winners != ''">
                      <xsl:choose>
                        <xsl:when test="contains($winners, concat('|', $playerGuess, '|'))">
                          <xsl:value-of select="$categoryData/value" />
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:value-of select="translate($categoryData/value, '1', '0')" />
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:if>
                  </td>
                </tr>
              </xsl:for-each>
              <tr>
                <xsl:attribute name="class">
                  <xsl:value-of select="$playerResults/time/@status" />
                </xsl:attribute>
                <td class="header">Show Running Time</td>
                <td>
                  <xsl:value-of select="$playerResults/time" />
                </td>
                <td>
                  <xsl:value-of select="$results/showTime/length" />
                </td>
                <td>N/A</td>
              </tr>
            </tbody>
            <tfoot>
              <tr>
                <th class="header">Total</th>
                <th>
                  <xsl:value-of select="floor($playerResults/score)" />
                </th>
                <th>
                  <xsl:value-of select="count($results/categories/category/nominees[nominee/@status = 'correct'])" />
                </th>
                <th>
                  <xsl:value-of select="$playerResults/score" />
                </th>
              </tr>
            </tfoot>
          </table>
          <br />
          <br />
          <h3>Rankings</h3>
          Names in green can no longer pass or be passed by
          <xsl:value-of select="$playerName" />
          <br />
          <br />
          <xsl:call-template name="player-table">
            <xsl:with-param name="results" select="$results" />
            <xsl:with-param name="sort" select="'rank'" />
            <xsl:with-param name="inPlayer" select="$playerResults" />
          </xsl:call-template>
          <br />
          <a href="../category/all.xml" id="return">All Categories</a>
          <br />
          <br />
          <a href="../index.html" id="return">Return to Main Page</a>
          <br />
          <br />
          <div id="date">
            Last updated:
            <xsl:value-of select="$results/updated" />
          </div>
          </center>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
