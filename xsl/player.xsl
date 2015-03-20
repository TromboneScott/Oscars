<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/player">
    <html>
      <xsl:variable name="results"
        select="document('../results.xml')/results" />
      <head>
        <link rel="stylesheet" type="text/css" href="../oscars.css" />
        <title>
          <xsl:value-of select="$results/title" />
        </title>
      </head>
      <body>
        <center>
          <table id="header">
            <tr>
              <td rowspan="2">
                <img src="../trophy.png" id="trophy" />
              </td>
              <th>
                <xsl:value-of select="$results/title" />
              </th>
              <td rowspan="2">
                <img src="../trophy.png" id="trophy" />
              </td>
            </tr>
            <tr>
              <td>(Unofficial Results)</td>
            </tr>
          </table>
          <br />
          <br />
          <xsl:variable name="player" select="." />
          <xsl:for-each select="$results/players/player">
            <xsl:if
              test="firstName = $player/firstName and lastName = $player/lastName">
              <div id="name">
                <xsl:value-of select="concat(firstName, ' ', lastName)" />
              </div>
              <br />
              <xsl:choose>
                <xsl:when test="@type = 'pseudo'">
                  <u>NOT A COMPETITOR</u>
                  <br />
                  <br />
                  Guesses entered from this source for comparison
                  purposes only.
                  <br />
                </xsl:when>
                <xsl:otherwise>
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
                    </tr>
                  </table>
                  <br />
                  Best Possible Rank:
                  <xsl:value-of select="bpr" />
                  <br />
                  Worst Possible Rank:
                  <xsl:value-of select="wpr" />
                </xsl:otherwise>
              </xsl:choose>
              <br />
              <br />
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
                      <xsl:value-of select="$results/categories/count" />
                    </th>
                    <th>
                      <xsl:value-of select="score" />
                    </th>
                  </tr>
                </tfoot>
              </table>
            </xsl:if>
          </xsl:for-each>
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