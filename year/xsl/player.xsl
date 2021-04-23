<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:include href="sort.xsl" />
  <xsl:template match="/player">
    <html>
      <xsl:variable name="player" select="." />
      <xsl:choose>
        <xsl:when test="firstName = 'Rick' and lastName = 'Astley'">
        <head>
          <meta http-equiv="refresh" content="0; url=https://www.youtube.com/watch?v=gPkZS92WCIo"/>
        </head>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="results"
            select="document('../results.xml')/results" />
          <xsl:call-template name="init">
            <xsl:with-param name="results" select="$results" />
          </xsl:call-template>
          <body>
            <center>
              <xsl:call-template name="header">
                <xsl:with-param name="results" select="$results" />
              </xsl:call-template>
              <xsl:for-each select="$results/players/player">
                <xsl:if
                  test="firstName = $player/firstName and lastName = $player/lastName">
                  <xsl:variable name="playerName"
                    select="concat(firstName, ' ', lastName)" />
                  <div id="name">
                    <xsl:value-of select="$playerName" />
                  </div>
                  <br />
                  <table>
                    <tr>
                      <td id="rank">
                        Rank
                        <div id="rank">
                          <xsl:value-of select="rank" />
                        </div>
                        Out of
                        <xsl:value-of select="$results/players/count" />
                      </td>
                      <td>
                        <img>
                          <xsl:attribute name="src">
                            <xsl:value-of select="concat('../rank/rank_', rank, '.png')" />
                          </xsl:attribute>
                        </img>
                      </td>
                    </tr>
                  </table>
                  <br />
                  Best Possible Rank:
                  <xsl:value-of select="bpr" />
                  <br />
                  Worst Possible Rank:
                  <xsl:value-of select="wpr" />
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
                      <xsl:for-each select="$player/categories/category">
                        <xsl:variable name="categoryName"
                          select="name" />
                        <xsl:variable name="winners">
                          <xsl:for-each select="$results/categories/category">
                            <xsl:if test="name = $categoryName">
                              <xsl:for-each select="winner">
                                <xsl:value-of select="concat('|', ., '|')" />
                              </xsl:for-each>
                            </xsl:if>
                          </xsl:for-each>
                        </xsl:variable>
                        <tr>
                          <xsl:attribute name="class">
                            <xsl:choose>
                              <xsl:when test="$winners = ''">
                                unannounced
                              </xsl:when>
                              <xsl:when
                            test="contains($winners, concat('|', guess, '|'))">
                                correct
                              </xsl:when>
                              <xsl:otherwise>
                                incorrect
                              </xsl:otherwise>
                            </xsl:choose>
                          </xsl:attribute>
                          <td class="header">
                            <a>
                              <xsl:attribute name="href">
                                <xsl:value-of
                                select="concat('../category/', name, '.xml')" />
                              </xsl:attribute>
                              <xsl:value-of select="name" />
                              <xsl:if test="tieBreaker != ''">
                                <xsl:value-of
                                  select="concat(' (', tieBreaker, ')')" />
                              </xsl:if>
                            </a>
                          </td>
                          <td>
                            <xsl:value-of select="guess" />
                          </td>
                          <td>
                            <xsl:variable name="tempWinners">
                              <xsl:call-template name="string-replace-all">
                                <xsl:with-param name="text"
                                  select="$winners" />
                                <xsl:with-param name="replace"
                                  select="'||'" />
                                <xsl:with-param name="by"
                                  select="', '" />
                              </xsl:call-template>
                            </xsl:variable>
                            <xsl:value-of select="translate($tempWinners, '|', '')" />
                          </td>
                          <td>
                            <xsl:if test="$winners != ''">
                              <xsl:choose>
                                <xsl:when
                                  test="contains($winners, concat('|', guess, '|'))">
                                  <xsl:value-of select="value" />
                                </xsl:when>
                                <xsl:otherwise>
                                  <xsl:value-of select="0" />
                                  <xsl:if test="tieBreaker > 0">
                                    <xsl:value-of select="'.'" />
                                    <xsl:call-template
                                      name="zeros">
                                      <xsl:with-param name="count"
                                        select="tieBreaker" />
                                    </xsl:call-template>
                                  </xsl:if>
                                </xsl:otherwise>
                              </xsl:choose>
                            </xsl:if>
                          </td>
                        </tr>
                      </xsl:for-each>
                      <tr>
                        <xsl:attribute name="class">
                          <xsl:value-of select="time/@status" />
                        </xsl:attribute>
                        <td class="header">Show Running Time</td>
                        <td>
                          <xsl:value-of select="time" />
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
                          <xsl:value-of select="floor(score)" />
                        </th>
                        <th>
                          <xsl:value-of select="round($results/categories/points)" />
                        </th>
                        <th>
                          <xsl:value-of select="score" />
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
                    <xsl:with-param name="inPlayer" select="." />
                  </xsl:call-template>
                </xsl:if>
              </xsl:for-each>
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
        </xsl:otherwise>
      </xsl:choose>
    </html>
  </xsl:template>
  <xsl:template name="zeros">
    <xsl:param name="count" />
    <xsl:if test="$count > 0">
      <xsl:value-of select="0" />
      <xsl:call-template name="zeros">
        <xsl:with-param name="count" select="$count - 1" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  <xsl:template name="string-replace-all">
    <xsl:param name="text" />
    <xsl:param name="replace" />
    <xsl:param name="by" />
    <xsl:choose>
      <xsl:when test="contains($text, $replace)">
        <xsl:value-of select="substring-before($text,$replace)" />
        <xsl:value-of select="$by" />
        <xsl:call-template name="string-replace-all">
          <xsl:with-param name="text"
            select="substring-after($text,$replace)" />
          <xsl:with-param name="replace" select="$replace" />
          <xsl:with-param name="by" select="$by" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
