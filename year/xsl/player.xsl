<?xml version="1.0" encoding="utf-8"?>
<!-- OSCARS website created by Scott McDonald -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:include href="sort.xsl" />
  <xsl:template match="/player">
    <html>
      <xsl:call-template name="header" />
      <body>
        <center>
          <xsl:variable name="player" select="." />
          <xsl:variable name="playerResults"
            select="$results/standings/player[@firstName = $player/@firstName and @lastName = $player/@lastName]" />
          <xsl:variable
            name="playerName" select="concat(@firstName, ' ', @lastName)" />
          <div
            id="name">
            <xsl:value-of select="$playerName" />
          </div>
          <br />
          <table>
            <tr>
              <td id="rank">Rank <div id="rank">
                  <a id="player_rank">
                    <xsl:value-of select="$playerResults/@rank" />
                  </a>
                </div>
                Out of <xsl:value-of select="count($results/standings/player)" />
              </td>
            </tr>
          </table>
          <br />
          <xsl:if test="not($ended)">
            <a id="possible_rank" />
            <br />
          </xsl:if>
          <br />
          <h3>Guesses</h3>
          <table>
            <thead>
              <tr>
                <th class="header">
                  <a>
                    <xsl:attribute name="href">
                      <xsl:value-of select="concat($rootDir, 'category')" />
                    </xsl:attribute>
                    Category</a>
                </th>
                <th>Guess</th>
                <th>Actual</th>
                <th>Score</th>
              </tr>
            </thead>
            <tbody>
              <xsl:for-each select="$results/awards/category">
                <xsl:variable name="categoryName" select="@name" />
                <xsl:variable name="categoryDefinition"
                  select="$definitions/column[@name = $categoryName]" />
                <xsl:variable name="playerGuess"
                  select="$ballots/player[@firstName = $player/@firstName and @lastName = $player/@lastName]/category[@name = $categoryName]/@nominee" />
                <tr>
                  <xsl:apply-templates select="." mode="attribute">
                    <xsl:with-param name="nominee" select="$playerGuess" />
                  </xsl:apply-templates>
                  <td class="header">
                    <a>
                      <xsl:attribute name="href">
                        <xsl:value-of
                          select="concat($rootDir, 'category/', @name, '.xml')" />
                      </xsl:attribute>
                      <xsl:value-of select="@name" />
                    </a>
                    <xsl:apply-templates select="$categoryDefinition"
                      mode="tieBreaker" />
                  </td>
                  <td>
                    <xsl:value-of select="$playerGuess" />
                  </td>
                  <td>
                    <xsl:variable name="winners">
                      <xsl:for-each select="nominee">
                        <xsl:value-of select="', '" />
                        <xsl:value-of select="@name" />
                      </xsl:for-each>
                    </xsl:variable>
                    <xsl:value-of select="substring-after($winners, ', ')" />
                  </td>
                  <td>
                    <xsl:if test="nominee">
                      <xsl:variable name="value">
                        <xsl:apply-templates select="$categoryDefinition"
                          mode="value" />
                      </xsl:variable>
                      <xsl:choose>
                        <xsl:when test="nominee[@name = $playerGuess]">
                          <xsl:value-of select="$value" />
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:value-of select="translate($value, '1', '0')" />
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
                  <xsl:value-of select="floor($playerResults/@score)" />
                </th>
                <th>
                  <xsl:value-of
                    select="count($results/awards/category[nominee])" />
                </th>
                <th>
                  <xsl:value-of select="$playerResults/@score" />
                </th>
              </tr>
              <tr>
                <xsl:attribute name="class">
                  <xsl:choose>
                    <xsl:when
                      test="$ballots/player[@firstName = $playerResults/@firstName and @lastName = $playerResults/@lastName]/@time &lt;= $results/standings/@time">
                  correct
                    </xsl:when>
                    <xsl:when test="$ended">
                      incorrect
                    </xsl:when>
                    <xsl:otherwise>
                      unannounced
                    </xsl:otherwise>
                  </xsl:choose>
                  time</xsl:attribute>
                <td class="header">
                  <xsl:value-of select="'Show Running Time'" />
                  <xsl:apply-templates
                    select="$definitions/column[@name = 'Time']"
                    mode="tieBreaker" />
                </td>
                <td id="time_guess">
                  <center>
                    <A id="time_player" />
                  </center>
                </td>
                <td id="time_actual">
                  <center>
                    <A id="time_value" />
                  </center>
                </td>
                <td id="time_score">
                  <center>
                    <A id="time_difference" />
                  </center>
                </td>
              </tr>
            </tfoot>
          </table>
          <br />
          <br />
          <br />
          <h3 style="display:inline">Rankings</h3>
          <a id="decided_L" style="display:none">
            <br /> All players in <font color="red">red</font> will finish above <xsl:value-of
              select="$playerName" />
          </a>
          <a id="decided_W" style="display:none">
            <br /> All players in <font color="green">green</font> will finish
            below <xsl:value-of select="$playerName" />
          </a>
          <xsl:if test="contains($playerResults/@decided, 'T')">
            <br /> All players in <font color="SaddleBrown">brown</font> will
            finish tied with <xsl:value-of select="$playerName" />
          </xsl:if>
          <br />
          <br />
          <xsl:apply-templates
            select="document(concat($rootDir, 'player/sort/default.xml'))/sort"
            mode="player-table">
            <xsl:with-param name="inPlayer" select="$playerResults" />
          </xsl:apply-templates>
        </center>
      </body>
      <xsl:call-template name="footer" />
    </html>
  </xsl:template>
</xsl:stylesheet>