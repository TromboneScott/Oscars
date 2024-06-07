<?xml version="1.0" encoding="utf-8"?>
<!-- OSCARS website created by Scott McDonald -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:include href="include.xsl" />
  <xsl:template match="/sort">
    <html>
      <xsl:call-template name="init" />
      <body>
        <center>
          <xsl:call-template name="header" />
          <xsl:choose>
            <xsl:when test="count($results/players/player)=0">
              <a href="javascript:history.go(0)" style="all: unset">
                <table>
                  <tr>
                    <td id="rank">
                      <br />
                      <b>BALLOTS ARE BEING COLLECTED</b>
                      <br />
                      <br />
                      &#9993; - &#9993; - &#9993; - &#9993; - &#9993; <br />
                      <br />
                      Ballot names will appear here a few minutes after being
                      cast. <br /> The "votes" will be loaded after all ballots
                      have been collected. <br />
                      <br />
                      <i>Check here for live
                      results during the Oscars broadcast.</i>
                      <br />
                      <br />
                    </td>
                  </tr>
                </table>
              </a>
              <xsl:if test="count($results/ballots/ballot) > 0">
                <br />
                <br />
                <h3> Ballots Received: <xsl:value-of
                    select="count($results/ballots/ballot)" />
                </h3>
                <table>
                  <tr>
                    <th>
                      <xsl:call-template name="player-table-column-header">
                        <xsl:with-param name="text" select="'Timestamp'" />
                        <xsl:with-param name="type" select="'rank'" />
                        <xsl:with-param name="sort" select="." />
                      </xsl:call-template>
                    </th>
                    <th>
                      <xsl:call-template name="player-table-column-header">
                        <xsl:with-param name="text" select="'Name'" />
                        <xsl:with-param name="type" select="'name'" />
                        <xsl:with-param name="sort" select="." />
                      </xsl:call-template>
                    </th>
                  </tr>
                  <xsl:choose>
                    <xsl:when test=". = 'rank'">
                      <xsl:apply-templates select="$results/ballots/ballot">
                        <xsl:sort select="timestamp/@raw" />
                        <xsl:sort select="@name" />
                      </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test=". = 'rankReverse'">
                      <xsl:apply-templates select="$results/ballots/ballot">
                        <xsl:sort select="timestamp/@raw" order="descending" />
                        <xsl:sort select="@name" />
                      </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test=". = 'name'">
                      <xsl:apply-templates select="$results/ballots/ballot">
                        <xsl:sort select="@name" />
                      </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test=". = 'nameReverse'">
                      <xsl:apply-templates select="$results/ballots/ballot">
                        <xsl:sort select="@name" order="descending" />
                      </xsl:apply-templates>
                    </xsl:when>
                  </xsl:choose>
                </table>
              </xsl:if>
            </xsl:when>
            <xsl:otherwise>
              <a href="../category/all.xml">
                <h2>OSCAR WINNERS</h2>
              </a>
              <table>
                <xsl:call-template name="winners">
                  <xsl:with-param name="start" select="0" />
                </xsl:call-template>
              </table>
              <br />
              <br />
              <div class="info">
                <xsl:if test="not(string($results/showTime/end))">
                  <u>BPR / WPR</u> - Best Possible Rank / Worst Possible Rank:
                If guesses for all remaining <a href="../category/all.xml">
                categories</a> turn out to be correct / incorrect. <br />
                  <br />
                </xsl:if>
                <u>
                Score</u> - One point for each correct <a
                  href="../category/all.xml">category</a> plus .1 for tie
                breaker #1, .01 for #2, .001 for #3, etc. </div>
              <br />
              <br />
              <xsl:call-template name="player-table">
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
          <xsl:call-template name="updated" />
        </center>
      </body>
    </html>
  </xsl:template>
  <xsl:template match="/results/ballots/ballot">
    <tr>
      <td>
        <xsl:value-of select="timestamp" />
      </td>
      <td>
        <xsl:value-of select="@name" />
      </td>
    </tr>
  </xsl:template>
  <xsl:template name="player-table">
    <xsl:param name="sort" />
    <xsl:param name="inPlayer" />
    <xsl:variable name="inProgress" select="not(string($results/showTime/end))" />
    <table>
      <thead>
        <tr>
          <th class="header">
            <xsl:call-template name="player-table-column-header">
              <xsl:with-param name="text" select="'Name'" />
              <xsl:with-param name="type" select="'name'" />
              <xsl:with-param name="sort" select="$sort" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
            </xsl:call-template>
          </th>
          <th>
            <xsl:call-template name="player-table-column-header">
              <xsl:with-param name="text" select="'Rank'" />
              <xsl:with-param name="type" select="'rank'" />
              <xsl:with-param name="sort" select="$sort" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
            </xsl:call-template>
          </th>
          <xsl:if test="$inProgress">
            <th>
              <xsl:call-template name="player-table-column-header">
                <xsl:with-param name="text" select="'BPR'" />
                <xsl:with-param name="type" select="'bpr'" />
                <xsl:with-param name="sort" select="$sort" />
                <xsl:with-param name="inPlayer" select="$inPlayer" />
              </xsl:call-template>
            </th>
            <th>
              <xsl:call-template name="player-table-column-header">
                <xsl:with-param name="text" select="'WPR'" />
                <xsl:with-param name="type" select="'wpr'" />
                <xsl:with-param name="sort" select="$sort" />
                <xsl:with-param name="inPlayer" select="$inPlayer" />
              </xsl:call-template>
            </th>
          </xsl:if>
          <th>
            <xsl:call-template name="player-table-column-header">
              <xsl:with-param name="text" select="'Score'" />
              <xsl:with-param name="type" select="'score'" />
              <xsl:with-param name="sort" select="$sort" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
            </xsl:call-template>
          </th>
          <th>
            <xsl:variable name="timeHeader">
              <xsl:value-of select="'Time'" />
              <xsl:choose>
                <xsl:when test="$inProgress">
                  <xsl:value-of select="'&gt;'" />
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="'='" />
                </xsl:otherwise>
              </xsl:choose>
              <xsl:value-of select="$results/showTime/@length" />
            </xsl:variable>
            <xsl:call-template name="player-table-column-header">
              <xsl:with-param name="text" select="$timeHeader" />
              <xsl:with-param name="type" select="'time'" />
              <xsl:with-param name="sort" select="$sort" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
            </xsl:call-template>
          </th>
        </tr>
      </thead>
      <tbody>
        <xsl:choose>
          <xsl:when test="$sort = 'name'">
            <xsl:apply-templates select="$results/players/player">
              <xsl:sort select="@lastName" />
              <xsl:sort select="@firstName" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
              <xsl:with-param name="inProgress" select="$inProgress" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="$sort = 'nameReverse'">
            <xsl:apply-templates select="$results/players/player">
              <xsl:sort select="@lastName" order="descending" />
              <xsl:sort select="@firstName" order="descending" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
              <xsl:with-param name="inProgress" select="$inProgress" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="$sort = 'rank' or $sort = 'score'">
            <xsl:apply-templates select="$results/players/player">
              <xsl:sort select="rank" data-type="number" />
              <xsl:sort select="bpr" data-type="number" />
              <xsl:sort select="wpr" data-type="number" />
              <xsl:sort select="@lastName" />
              <xsl:sort select="@firstName" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
              <xsl:with-param name="inProgress" select="$inProgress" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when
            test="$sort = 'rankReverse' or $sort = 'scoreReverse'">
            <xsl:apply-templates select="$results/players/player">
              <xsl:sort select="rank" data-type="number" order="descending" />
              <xsl:sort select="bpr" data-type="number" order="descending" />
              <xsl:sort select="wpr" data-type="number" order="descending" />
              <xsl:sort select="@lastName" />
              <xsl:sort select="@firstName" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
              <xsl:with-param name="inProgress" select="$inProgress" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="$sort = 'bpr'">
            <xsl:apply-templates
              select="$results/players/player">
              <xsl:sort select="bpr" data-type="number" />
              <xsl:sort select="rank" data-type="number" />
              <xsl:sort select="@lastName" />
              <xsl:sort select="@firstName" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
              <xsl:with-param name="inProgress" select="$inProgress" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="$sort = 'bprReverse'">
            <xsl:apply-templates select="$results/players/player">
              <xsl:sort select="bpr" data-type="number" order="descending" />
              <xsl:sort select="rank" data-type="number" order="descending" />
              <xsl:sort select="@lastName" />
              <xsl:sort select="@firstName" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
              <xsl:with-param name="inProgress" select="$inProgress" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="$sort = 'wpr'">
            <xsl:apply-templates select="$results/players/player">
              <xsl:sort select="wpr" data-type="number" />
              <xsl:sort select="rank" data-type="number" />
              <xsl:sort select="@lastName" />
              <xsl:sort select="@firstName" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
              <xsl:with-param name="inProgress" select="$inProgress" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="$sort = 'wprReverse'">
            <xsl:apply-templates select="$results/players/player">
              <xsl:sort select="wpr" data-type="number" order="descending" />
              <xsl:sort select="rank" data-type="number" order="descending" />
              <xsl:sort select="@lastName" />
              <xsl:sort select="@firstName" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
              <xsl:with-param name="inProgress" select="$inProgress" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="$sort = 'time'">
            <xsl:apply-templates select="$results/players/player">
              <xsl:sort select="time" />
              <xsl:sort select="@lastName" />
              <xsl:sort select="@firstName" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
              <xsl:with-param name="inProgress" select="$inProgress" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="$sort = 'timeReverse'">
            <xsl:apply-templates select="$results/players/player">
              <xsl:sort select="time" order="descending" />
              <xsl:sort select="@lastName" />
              <xsl:sort select="@firstName" />
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
    <xsl:param name="type" />
    <xsl:param name="sort" />
    <xsl:param name="inPlayer" />
    <xsl:choose>
      <xsl:when test="$inPlayer">
        <xsl:value-of select="$text" />
      </xsl:when>
      <xsl:otherwise>
        <A>
          <xsl:attribute name="href">
            <xsl:value-of select="$type" />
            <xsl:if test="$type = $sort">
              <xsl:value-of select="'Reverse'" />
            </xsl:if>
            <xsl:value-of select="'.xml'" />
          </xsl:attribute>
          <xsl:value-of select="$text" />
        </A>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="/results/players/player">
    <xsl:param name="inPlayer" />
    <xsl:param name="inProgress" />
    <tr>
      <td>
        <xsl:attribute name="class"> header <xsl:if test="$inPlayer">
            <xsl:choose>
              <xsl:when test="@id = $inPlayer/@id">
                unannounced
              </xsl:when>
              <xsl:when
                test="substring($inPlayer/opponents/@decided, number(@id), 1) = 'Y'">
          correct
              </xsl:when>
            </xsl:choose>
          </xsl:if>
        </xsl:attribute>
        <xsl:apply-templates select="." mode="playerLink" />
      </td>
      <td class="rank">
        <xsl:value-of select="rank" />
      </td>
      <xsl:if test="$inProgress">
        <td>
          <xsl:attribute name="class"> rank <xsl:if test="bpr = wpr">
              unannounced
            </xsl:if>
          </xsl:attribute>
          <xsl:value-of select="bpr" />
        </td>
        <td>
          <xsl:attribute name="class"> rank <xsl:if test="bpr = wpr">
              unannounced
            </xsl:if>
          </xsl:attribute>
          <xsl:value-of select="wpr" />
        </td>
      </xsl:if>
      <td class="rank">
        <xsl:value-of select="score" /> -- <xsl:variable name="scoreTieBreakers">
          <xsl:call-template name="tieBreakers">
            <xsl:with-param name="score" select="score" />
            <xsl:with-param name="value" select="8" />
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable
          name="scoreWidth" select="round(score) * 16 + $scoreTieBreakers" />
        <img
          src="../../bar_green.bmp" height="15">
          <xsl:attribute name="width">
            <xsl:value-of select="$scoreWidth" />
          </xsl:attribute>
        </img>
        <img
          src="../../bar_grey.bmp" height="15">
          <xsl:attribute name="width">
            <xsl:value-of select="16 * 19 - 1 - $scoreWidth" />
          </xsl:attribute>
        </img>
      </td>
      <td>
        <xsl:attribute name="class">
          <xsl:value-of select="time/@status" /> time </xsl:attribute>
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
  <xsl:template name="winners">
    <xsl:param name="start" />
    <xsl:if test="$start &lt; count($results/categories/category)">
      <xsl:variable name="end" select="$start + 6" />
      <tr>
        <xsl:for-each
          select="$results/categories/category[position() &gt; $start and position() &lt;= $end]">
          <td style="text-align: center">
            <a>
              <xsl:attribute name="id">
                <xsl:value-of select="@name" />
              </xsl:attribute>
              <xsl:attribute name="href">
                <xsl:value-of select="concat('../category/', @name, '.xml')" />
              </xsl:attribute>
              <xsl:apply-templates select="winners/nominee" mode="poster">
                <xsl:with-param name="category" select="@name" />
              </xsl:apply-templates>
              <xsl:if test="not(winners/nominee)">
                <img src="http://oscars.site44.com/trophy_poster.png"
                  title="Not Yet Announced" />
              </xsl:if>
              <br />
              <xsl:value-of select="@name" />
            </a>
          </td>
        </xsl:for-each>
      </tr>
      <xsl:call-template name="winners">
        <xsl:with-param name="start" select="$end" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>