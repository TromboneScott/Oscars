<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" encoding="utf-8" indent="yes" />
  <xsl:include href="header.xsl" />
  <xsl:template match="/sort">
    <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
    <html>
      <xsl:variable name="results" select="document('../results.xml')/results" />
      <xsl:call-template name="init">
        <xsl:with-param name="results" select="$results" />
      </xsl:call-template>
      <body>
        <center>
          <xsl:call-template name="header">
            <xsl:with-param name="results" select="$results" />
          </xsl:call-template>
          <xsl:choose>
            <xsl:when test="count($results/players/player)=0">
              <table>
                <tr>
                  <td id="rank">
                    <br />
                    <b>BALLOTS ARE BEING COLLECTED</b>
                    <br />
                    <br />
                    &#9993; - &#9993; - &#9993; - &#9993; - &#9993;
                    <br />
                    <br />
                    Ballots will appear here a few minutes after they're entered.
                    <br />
                    Details will be added after all ballots have been collected.
                    <br />
                    <br />
                    <i>Check here for live results during the Oscars broadcast.</i>
                    <br />
                    <br />
                  </td>
                </tr>
              </table>
              <xsl:if test="count($results/entries/entry) > 0">
                <br />
                <br />
                <h3>
                  Ballots Received: 
                  <xsl:value-of select="count($results/entries/entry)" />
                </h3>
                <table>
                  <tr>
                    <th>
                      <xsl:choose>
                        <xsl:when test=". = 'rank'">
                          <xsl:call-template name="player-table-column-header">
                            <xsl:with-param name="text" select="'Timestamp'" />
                            <xsl:with-param name="link" select="'rankReverse.xml'" />
                          </xsl:call-template>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:call-template name="player-table-column-header">
                            <xsl:with-param name="text" select="'Timestamp'" />
                            <xsl:with-param name="link" select="'rank.xml'" />
                          </xsl:call-template>
                        </xsl:otherwise>
                      </xsl:choose>
                    </th>
                    <th>
                      <xsl:choose>
                        <xsl:when test=". = 'name'">
                          <xsl:call-template name="player-table-column-header">
                            <xsl:with-param name="text" select="'Name'" />
                            <xsl:with-param name="link" select="'nameReverse.xml'" />
                          </xsl:call-template>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:call-template name="player-table-column-header">
                            <xsl:with-param name="text" select="'Name'" />
                            <xsl:with-param name="link" select="'name.xml'" />
                          </xsl:call-template>
                        </xsl:otherwise>
                      </xsl:choose>
                    </th>
                  </tr>
                  <xsl:choose>
                    <xsl:when test=". = 'rank'">
                      <xsl:apply-templates select="$results/entries/entry">
                        <xsl:sort select="timestamp/@raw" />
                        <xsl:sort select="lastName" />
                        <xsl:sort select="firstName" />
                      </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test=". = 'rankReverse'">
                      <xsl:apply-templates select="$results/entries/entry">
                        <xsl:sort select="timestamp/@raw" order="descending" />
                        <xsl:sort select="lastName" />
                        <xsl:sort select="firstName" />
                      </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test=". = 'name'">
                      <xsl:apply-templates select="$results/entries/entry">
                        <xsl:sort select="lastName" />
                        <xsl:sort select="firstName" />
                      </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test=". = 'nameReverse'">
                      <xsl:apply-templates select="$results/entries/entry">
                        <xsl:sort select="lastName" order="descending" />
                        <xsl:sort select="firstName" order="descending" />
                      </xsl:apply-templates>
                    </xsl:when>
                  </xsl:choose>
                </table>
              </xsl:if>
            </xsl:when>
            <xsl:otherwise>
              <div class="info">
                <xsl:if test="not(string($results/showTime/end))">
                  <u>BPR / WPR</u>
                  - Best Possible Rank / Worst Possible Rank: If guesses for all remaining
                  <a href="../category/all.xml">categories</a>
                  turn out to be correct / incorrect.
                  <br />
                  <br />
                </xsl:if>
                <u>Score</u>
                - One point for each correct
                <a href="../category/all.xml">category</a>
                plus .1 for tie breaker #1, .01 for #2, .001 for #3,
                etc.
              </div>
              <br />
              <br />
              <xsl:call-template name="player-table">
                <xsl:with-param name="results" select="$results" />
                <xsl:with-param name="sort" select="." />
              </xsl:call-template>
              <br />
              <a href="../category/all.xml" id="return">All Categories</a>
            </xsl:otherwise>
          </xsl:choose>
          <br />
          <br />
          <a href="../../history">Oscars History</a>
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
  <xsl:template match="entry">
    <tr>
      <td>
        <xsl:value-of select="timestamp" />
      </td>
      <td>
        <xsl:value-of select="lastName" />
        <xsl:if test="firstName != '' and lastName != ''">
          <xsl:value-of select="', '" />
        </xsl:if>
        <xsl:value-of select="firstName" />
      </td>
    </tr>
  </xsl:template>
  <xsl:template name="player-table">
    <xsl:param name="results" />
    <xsl:param name="sort" />
    <xsl:param name="inPlayer" />
    <xsl:variable name="inProgress" select="not(string($results/showTime/end))" />
    <table>
      <thead>
        <tr>
          <th class="header">
            <xsl:choose>
              <xsl:when test="$sort = 'name'">
                <xsl:call-template name="player-table-column-header">
                  <xsl:with-param name="text" select="'Name'" />
                  <xsl:with-param name="link" select="'nameReverse.xml'" />
                  <xsl:with-param name="inPlayer" select="$inPlayer" />
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>
                <xsl:call-template name="player-table-column-header">
                  <xsl:with-param name="text" select="'Name'" />
                  <xsl:with-param name="link" select="'name.xml'" />
                  <xsl:with-param name="inPlayer" select="$inPlayer" />
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
          </th>
          <th>
            <xsl:choose>
              <xsl:when test="$sort = 'rank'">
                <xsl:call-template name="player-table-column-header">
                  <xsl:with-param name="text" select="'Rank'" />
                  <xsl:with-param name="link" select="'rankReverse.xml'" />
                  <xsl:with-param name="inPlayer" select="$inPlayer" />
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>
                <xsl:call-template name="player-table-column-header">
                  <xsl:with-param name="text" select="'Rank'" />
                  <xsl:with-param name="link" select="'rank.xml'" />
                  <xsl:with-param name="inPlayer" select="$inPlayer" />
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
          </th>
          <xsl:if test="$inProgress">
            <th>
              <xsl:choose>
                <xsl:when test="$sort = 'bpr'">
                  <xsl:call-template name="player-table-column-header">
                    <xsl:with-param name="text" select="'BPR'" />
                    <xsl:with-param name="link" select="'bprReverse.xml'" />
                    <xsl:with-param name="inPlayer" select="$inPlayer" />
                  </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:call-template name="player-table-column-header">
                    <xsl:with-param name="text" select="'BPR'" />
                    <xsl:with-param name="link" select="'bpr.xml'" />
                    <xsl:with-param name="inPlayer" select="$inPlayer" />
                  </xsl:call-template>
                </xsl:otherwise>
              </xsl:choose>
            </th>
            <th>
              <xsl:choose>
                <xsl:when test="$sort = 'wpr'">
                  <xsl:call-template name="player-table-column-header">
                    <xsl:with-param name="text" select="'WPR'" />
                    <xsl:with-param name="link" select="'wprReverse.xml'" />
                    <xsl:with-param name="inPlayer" select="$inPlayer" />
                  </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:call-template name="player-table-column-header">
                    <xsl:with-param name="text" select="'WPR'" />
                    <xsl:with-param name="link" select="'wpr.xml'" />
                    <xsl:with-param name="inPlayer" select="$inPlayer" />
                  </xsl:call-template>
                </xsl:otherwise>
              </xsl:choose>
            </th>
          </xsl:if>
          <th>
            <xsl:choose>
              <xsl:when test="$sort = 'score'">
                <xsl:call-template name="player-table-column-header">
                  <xsl:with-param name="text" select="'Score'" />
                  <xsl:with-param name="link" select="'scoreReverse.xml'" />
                  <xsl:with-param name="inPlayer" select="$inPlayer" />
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>
                <xsl:call-template name="player-table-column-header">
                  <xsl:with-param name="text" select="'Score'" />
                  <xsl:with-param name="link" select="'score.xml'" />
                  <xsl:with-param name="inPlayer" select="$inPlayer" />
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
          </th>
          <th>
            <xsl:choose>
              <xsl:when test="$sort = 'time'">
                <xsl:call-template name="player-table-column-header">
                  <xsl:with-param name="text" select="$results/showTime/header" />
                  <xsl:with-param name="link" select="'timeReverse.xml'" />
                  <xsl:with-param name="inPlayer" select="$inPlayer" />
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>
                <xsl:call-template name="player-table-column-header">
                  <xsl:with-param name="text" select="$results/showTime/header" />
                  <xsl:with-param name="link" select="'time.xml'" />
                  <xsl:with-param name="inPlayer" select="$inPlayer" />
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
          </th>
        </tr>
      </thead>
      <tbody>
        <xsl:choose>
          <xsl:when test="$sort = 'name'">
            <xsl:apply-templates select="$results/players/player">
              <xsl:sort select="lastName" />
              <xsl:sort select="firstName" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
              <xsl:with-param name="inProgress" select="$inProgress" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="$sort = 'nameReverse'">
            <xsl:apply-templates select="$results/players/player">
              <xsl:sort select="lastName" order="descending" />
              <xsl:sort select="firstName" order="descending" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
              <xsl:with-param name="inProgress" select="$inProgress" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="$sort = 'rank' or $sort = 'score'">
            <xsl:apply-templates select="$results/players/player">
              <xsl:sort select="rank" data-type="number" />
              <xsl:sort select="lastName" />
              <xsl:sort select="firstName" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
              <xsl:with-param name="inProgress" select="$inProgress" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when
            test="$sort = 'rankReverse' or $sort = 'scoreReverse'">
            <xsl:apply-templates select="$results/players/player">
              <xsl:sort select="rank" data-type="number" order="descending" />
              <xsl:sort select="lastName" />
              <xsl:sort select="firstName" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
              <xsl:with-param name="inProgress" select="$inProgress" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="$sort = 'bpr'">
            <xsl:apply-templates
              select="$results/players/player">
              <xsl:sort select="bpr" data-type="number" />
              <xsl:sort select="rank" data-type="number" />
              <xsl:sort select="lastName" />
              <xsl:sort select="firstName" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
              <xsl:with-param name="inProgress" select="$inProgress" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="$sort = 'bprReverse'">
            <xsl:apply-templates select="$results/players/player">
              <xsl:sort select="bpr" data-type="number" order="descending" />
              <xsl:sort select="rank" data-type="number" />
              <xsl:sort select="lastName" />
              <xsl:sort select="firstName" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
              <xsl:with-param name="inProgress" select="$inProgress" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="$sort = 'wpr'">
            <xsl:apply-templates select="$results/players/player">
              <xsl:sort select="wpr" data-type="number" />
              <xsl:sort select="rank" data-type="number" />
              <xsl:sort select="lastName" />
              <xsl:sort select="firstName" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
              <xsl:with-param name="inProgress" select="$inProgress" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="$sort = 'wprReverse'">
            <xsl:apply-templates select="$results/players/player">
              <xsl:sort select="wpr" data-type="number" order="descending" />
              <xsl:sort select="rank" data-type="number" />
              <xsl:sort select="lastName" />
              <xsl:sort select="firstName" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
              <xsl:with-param name="inProgress" select="$inProgress" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="$sort = 'time'">
            <xsl:apply-templates select="$results/players/player">
              <xsl:sort select="time" />
              <xsl:sort select="lastName" />
              <xsl:sort select="firstName" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
              <xsl:with-param name="inProgress" select="$inProgress" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="$sort = 'timeReverse'">
            <xsl:apply-templates select="$results/players/player">
              <xsl:sort select="time" order="descending" />
              <xsl:sort select="lastName" />
              <xsl:sort select="firstName" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
              <xsl:with-param name="inProgress" select="$inProgress" />
            </xsl:apply-templates>
          </xsl:when>
        </xsl:choose>
      </tbody>
    </table>
  </xsl:template>
  <xsl:template name="player-table-column-header">
    <xsl:param name="text" />
    <xsl:param name="link" />
    <xsl:param name="inPlayer" />
    <xsl:choose>
      <xsl:when test="$inPlayer">
        <xsl:value-of select="$text" />
      </xsl:when>
      <xsl:otherwise>
        <A>
          <xsl:attribute name="href">
            <xsl:value-of select="$link" />
          </xsl:attribute>
          <xsl:value-of select="$text" />
        </A>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="player">
    <xsl:param name="inPlayer" />
    <xsl:param name="inProgress" />
    <xsl:variable name="id" select="@id" />
    <tr>
      <xsl:variable name="playerName">
        <xsl:value-of select="firstName" />
        <xsl:if test="firstName != '' and lastName != ''">
          <xsl:value-of select="' '" />
        </xsl:if>
        <xsl:value-of select="lastName" />
      </xsl:variable>
      <xsl:variable name="playerFile" select="concat('../player/', $playerName, '.xml')" />
      <td>
        <xsl:attribute name="class">
          header
          <xsl:if test="$inPlayer">
            <xsl:choose>
              <xsl:when test="@id = $inPlayer/@id">
                unannounced
              </xsl:when>
              <xsl:when test="$inPlayer/opponents/player[number($id)] = 'BETTER' or $inPlayer/opponents/player[number($id)] = 'WORSE'">
                correct
              </xsl:when>
            </xsl:choose>
          </xsl:if>
        </xsl:attribute>
        <a>
          <xsl:attribute name="href">
            <xsl:value-of select="$playerFile" />
          </xsl:attribute>
          <xsl:value-of select="lastName" />
          <xsl:if test="firstName != '' and lastName != ''">
            <xsl:value-of select="', '" />
          </xsl:if>
          <xsl:value-of select="firstName" />
        </a>
      </td>
      <td class="rank">
        <xsl:value-of select="rank" />
      </td>
      <xsl:if test="$inProgress" >
        <td>
          <xsl:attribute name="class">
            rank
            <xsl:if test="bpr = wpr" >
              unannounced
            </xsl:if>
          </xsl:attribute>
          <xsl:value-of select="bpr" />
        </td>
        <td>
          <xsl:attribute name="class">
            rank
            <xsl:if test="bpr = wpr" >
              unannounced
            </xsl:if>
          </xsl:attribute>
          <xsl:value-of select="wpr" />
        </td>
      </xsl:if>
      <td class="rank">
        <xsl:value-of select="score" />
        --
        <xsl:variable name="scoreTieBreakers">
          <xsl:call-template name="tieBreakers">
            <xsl:with-param name="score" select="score" />
            <xsl:with-param name="value" select="8" />
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="scoreWidth" select="round(score) * 16 + $scoreTieBreakers" />
        <img src="../../bar_green.bmp" height="15" >
          <xsl:attribute name="width">
            <xsl:value-of select="$scoreWidth" />
          </xsl:attribute>
        </img>
        <img src="../../bar_grey.bmp" height="15" >
          <xsl:attribute name="width">
            <xsl:value-of select="16 * 19 - 1 - $scoreWidth" />
          </xsl:attribute>
        </img>
      </td>
      <td>
        <xsl:attribute name="class">
          <xsl:value-of select="time/@status" /> time
        </xsl:attribute>
        <xsl:value-of select="time" />
      </td>
    </tr>
  </xsl:template>
  <xsl:template name="tieBreakers">
    <xsl:param name="score" />
    <xsl:param name="value" />
    <xsl:choose>
      <xsl:when test="$value &lt; 1">
        0
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="tieBreakers">
          <xsl:call-template name="tieBreakers">
            <xsl:with-param name="score" select="$score * 10" />
            <xsl:with-param name="value" select="$value div 2" />
          </xsl:call-template>
        </xsl:variable>
        <xsl:value-of select="round($score * 10) mod 2 * $value + $tieBreakers" /> 
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
