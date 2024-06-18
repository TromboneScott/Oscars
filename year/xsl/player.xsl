<?xml version="1.0" encoding="utf-8"?>
<!-- OSCARS website created by Scott McDonald -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:include href="sort.xsl" />
  <xsl:template match="/player">
    <html>
      <xsl:call-template name="init" />
      <xsl:variable name="player" select="." />
      <body>
        <center>
          <xsl:call-template name="header" />
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
              <td id="rank"> Rank <div id="rank">
                  <xsl:value-of select="$playerResults/@rank" />
                </div>
          Out of <xsl:value-of select="count($results/standings/player)" />
              </td>
              <td class="header">
                <img>
                  <xsl:attribute name="src">
                    <xsl:value-of
                      select="concat('../rank/rank_', $playerResults/@rank, '.png')" />
                  </xsl:attribute>
                  <xsl:attribute name="alt">
                    <xsl:value-of select="$playerResults/@rank" /> out of <xsl:value-of
                      select="count($results/standings/player)" />
                  </xsl:attribute>
                  <xsl:attribute name="title">
                    <xsl:value-of select="$playerResults/@rank" /> out of <xsl:value-of
                      select="count($results/standings/player)" />
                  </xsl:attribute>
                </img>
              </td>
            </tr>
          </table>
          <xsl:if
            test="$inProgress">
            <br /> Best Possible Rank (BPR): <xsl:value-of
              select="$playerResults/@bpr" />
            <br /> Worst Possible Rank (WPR): <xsl:value-of
              select="$playerResults/@wpr" />
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
              <xsl:for-each select="$results/awards/category">
                <xsl:variable name="categoryName" select="@name" />
                <xsl:variable name="categoryDefinition"
                  select="$definitions/category[@name = $categoryName]" />
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
                          select="concat('../category/', @name, '.xml')" />
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
                          <xsl:value-of
                            select="translate($value, '1', '0')" />
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
                <xsl:apply-templates select="$playerResults" mode="attribute" />
                <td class="header">
                  <xsl:value-of select="'Show Running Time'" />
                  <xsl:apply-templates
                    select="$definitions/category[@name = 'Time']"
                    mode="tieBreaker" />
                </td>
                <td>
                  <center>
                    <xsl:apply-templates select="$playerResults" mode="time" />
                  </center>
                </td>
                <td>
                  <center>
                    <xsl:call-template name="time">
                      <xsl:with-param name="time">
                        <xsl:value-of select="$results/awards/@duration" />
                      </xsl:with-param>
                    </xsl:call-template>
                  </center>
                </td>
                <td>
                  <center>
                    <xsl:choose>
                      <xsl:when
                        test="$playerResults/@time &lt;= $results/awards/@duration">
                        <xsl:call-template name="time">
                          <xsl:with-param name="time">
                            <xsl:value-of
                              select="$results/awards/@duration - $playerResults/@time" />
                          </xsl:with-param>
                        </xsl:call-template>
                      </xsl:when>
                      <xsl:when test="$inProgress">
                        <xsl:value-of select="'-'" />
                        <xsl:call-template name="time">
                          <xsl:with-param name="time">
                            <xsl:value-of
                              select="$playerResults/@time - $results/awards/@duration" />
                          </xsl:with-param>
                        </xsl:call-template>
                      </xsl:when>
                      <xsl:otherwise>
                        OVER
                      </xsl:otherwise>
                    </xsl:choose>
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
          <xsl:apply-templates
            select="document('../sort/rank.xml')/sort" mode="player-table">
            <xsl:with-param name="inPlayer" select="$playerResults" />
          </xsl:apply-templates>
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
            name="updated" />
        </center>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>